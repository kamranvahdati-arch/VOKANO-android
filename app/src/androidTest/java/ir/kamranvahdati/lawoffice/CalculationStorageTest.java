package ir.kamranvahdati.lawoffice;

import static org.junit.Assert.*;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONObject;
import java.io.*;
import java.math.RoundingMode;
import java.util.*;

@RunWith(AndroidJUnit4.class)
public class CalculationStorageTest {
    private Context context() { return InstrumentationRegistry.getInstrumentation().getTargetContext(); }
    private CalculationReference ref(String id, String previous, long timestamp, String amount) {
        return new CalculationReference(id,"synthetic-test-diyah","test-"+timestamp,previous,
            CalculationReference.Kind.ANNUAL_DIYAH,1405,0,"1405/01/01","1405/12/29",amount,"IRR",
            "Synthetic fixture", "TEST", "", "", "", "Test revision explanation",
            CalculationReference.Status.USER_ENTERED,timestamp,timestamp,true,true,false);
    }
    private CalculationSnapshot snap(String id, String previous, Long caseId, CalculationReference ref, long time) {
        Map<String,String> inputs = new LinkedHashMap<>(); inputs.put("percent","۱٫۲۵"); inputs.put("expertOpinion","نظر آزمایشی؛ قطعی نیست");
        Map<String,String> results = new LinkedHashMap<>(); results.put("rials","12500000");
        return new CalculationSnapshot(id,previous,"آزمون ارش",caseId,CalculationSnapshot.Type.ARSH_ESTIMATE,
            "test","test-only","test-ref","1405/07/01","1405/07/01",inputs,results,
            Collections.singletonList(ref),Collections.singletonList("synthetic 1% × base"),Collections.emptyList(),
            previous.isEmpty()?Collections.emptyList():Collections.singletonList(new CalculationSnapshot.Override(
                CalculationSnapshot.OverrideType.RATE,"annualRate","1000000000","2000000000",time,"دلیل تست")),
            RoundingMode.HALF_UP,time,time);
    }
    @Test public void immutableRevisionsAndExactPayloadsSurviveEncryptedBackup() throws Exception {
        try (OfficeDb db = new OfficeDb(context())) {
            String original=db.exportJson();
            try {
                OfficeCalculations repo = new OfficeCalculations(db);
                String prefix=UUID.randomUUID().toString();
                OfficeDb.CaseRecord c=new OfficeDb.CaseRecord();c.title="ارتباط محاسبه"; long caseId=db.addCase(c);
                CalculationReference first=ref(prefix+"r1","",100,"1000000000");
                CalculationReference next=ref(prefix+"r2",first.id,200,"2000000000");
                CalculationSnapshot one=snap(prefix+"s1","",caseId,first,1000);
                CalculationSnapshot two=snap(prefix+"s2",one.id,caseId,next,2000);
                repo.save(one);repo.save(two);
                assertEquals(2,repo.list(caseId).size());
                assertEquals("1000000000",repo.get(one.id).references.get(0).value);
                assertEquals("2000000000",repo.get(two.id).references.get(0).value);
                assertEquals("دلیل تست",repo.get(two.id).overrides.get(0).reason);
                assertTrue(repo.get(one.id).warnings.contains(CalculationSnapshot.ARSH_WARNING));
                assertEquals("نظر آزمایشی؛ قطعی نیست",repo.get(one.id).inputSnapshot.get("expertOpinion"));
                boolean failed=false;try{repo.save(one);}catch(Exception expected){failed=true;}assertTrue(failed);
                assertEquals(2,repo.list(caseId).size());
                String before=db.exportJson();
                JSONObject root=new JSONObject().put("database",new JSONObject(before)).put("profile",new JSONObject());
                root.getJSONObject("database").getJSONObject("tables").put("case_attachments",new org.json.JSONArray());
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                FullBackup.write(context(),root.toString(),bytes,"calculation-test-password");
                try(FullBackup.Prepared prepared=FullBackup.read(context(),new ByteArrayInputStream(bytes.toByteArray()),"calculation-test-password")){
                    db.importJson(new JSONObject(prepared.bundle).getJSONObject("database").toString());
                    assertEquals(CalculationJson.snapshot(one).toString(),CalculationJson.snapshot(repo.get(one.id)).toString());
                    assertEquals(CalculationJson.snapshot(two).toString(),CalculationJson.snapshot(repo.get(two.id)).toString());
                }
                int matching=0;for(CalculationReference r:repo.currentReferences())if(r.id.startsWith(prefix)){matching++;assertEquals(next.id,r.id);}
                assertEquals(1,matching);
            } finally { db.importJson(original); }
        }
    }
    @Test public void incompleteAndCorruptCalculationBackupRollBackLiveData() throws Exception {
        try(OfficeDb db=new OfficeDb(context())){
            String original=db.exportJson();try{
                String id=UUID.randomUUID().toString();OfficeCalculations repo=new OfficeCalculations(db);
                repo.save(snap(id,"",null,ref(id+"r","",100,"1000000000"),1000));
                String good=db.exportJson();
                JSONObject missing=new JSONObject(good);missing.getJSONObject("tables").remove(OfficeCalculations.SNAPSHOTS);
                assertRejected(db,missing.toString());assertNotNull(repo.get(id));
                JSONObject corrupt=new JSONObject(good);
                org.json.JSONArray rows=corrupt.getJSONObject("tables").getJSONArray(OfficeCalculations.SNAPSHOTS);
                rows.getJSONObject(rows.length()-1).put("payload","{}");
                assertRejected(db,corrupt.toString());assertNotNull(repo.get(id));
                JSONObject noRefs=new JSONObject(good);noRefs.getJSONObject("tables").put(OfficeCalculations.REFERENCES,new org.json.JSONArray());
                assertRejected(db,noRefs.toString());assertNotNull(repo.get(id));
                OfficeDb.validateBackup(context(),good,true);
            }finally{db.importJson(original);}
        }
    }
    @Test public void validLegacySchema15BackupStillRestores()throws Exception{
        try(OfficeDb db=new OfficeDb(context())){
            String original=db.exportJson();try{
                JSONObject legacy=new JSONObject(db.exportJson());legacy.remove("schema");legacy.remove("calculation_format");
                JSONObject tables=legacy.getJSONObject("tables");tables.remove(OfficeCalculations.REFERENCES);tables.remove(OfficeCalculations.SNAPSHOTS);
                OfficeDb.validateBackup(context(),legacy.toString(),true);
                db.importJson(legacy.toString());assertTrue(new OfficeCalculations(db).list(null).isEmpty());
                assertEquals(16,db.getReadableDatabase().getVersion());
            }finally{db.importJson(original);}
        }
    }
    private void assertRejected(OfficeDb db,String json)throws Exception{
        boolean rejected=false;try{db.importJson(json);}catch(Exception expected){rejected=true;}assertTrue(rejected);
    }
}
