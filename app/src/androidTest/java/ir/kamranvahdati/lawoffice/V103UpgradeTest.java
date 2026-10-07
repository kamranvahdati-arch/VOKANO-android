package ir.kamranvahdati.lawoffice;

import static org.junit.Assert.*;
import android.content.Context;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Iterator;

/** Compiles unchanged on baseline 10.2. Real installed upgrade, not synthetic schema SQL. */
@RunWith(AndroidJUnit4.class)
public class V103UpgradeTest {
    @Test public void installedSchema15To16PreservesOffice()throws Exception{
        String phase=InstrumentationRegistry.getArguments().getString("upgrade_phase","");
        Assume.assumeTrue("seed".equals(phase)||"baseline-reopen".equals(phase)||"verify".equals(phase));
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        File expected=new File(c.getFilesDir(),"v103-upgrade-before.json");
        try(OfficeDb db=new OfficeDb(c)){
            if("seed".equals(phase)){
                assertEquals(15,db.getReadableDatabase().getVersion());assertEquals(0,db.countClients());
                long person=db.addClient("موکل ارتقای ۱۰.۳","0013540831","پدر","1360/01/01","09120000001","نشانی","یادداشت");
                OfficeDb.CaseRecord record=new OfficeDb.CaseRecord();record.title="پرونده حفظ اطلاعات ۱۰.۳";record.clientId=person;record.feeAgreed=987654321;long caseId=db.addCase(record);
                db.addLedger(caseId,"income","حق‌الوکاله",1234567,JalaliDate.today().value(),"موکل","شرح حفظ شود");
                db.saveAppointment("جلسه دادگاه","شخص","",person,caseId,JalaliDate.today().value(),"13:00","14:00","محل","یادداشت","1","مرجع","شهر");
                File media=new File(c.getCacheDir(),"v103-fixture.pdf");try(FileOutputStream out=new FileOutputStream(media)){out.write("%PDF-1.4\n10.3 upgrade fixture".getBytes(StandardCharsets.UTF_8));}
                Uri uri=MediaStorage.copy(c,Uri.fromFile(media));db.addCaseAttachment(caseId,"fixture.pdf","application/pdf",uri.toString());media.delete();
                c.getSharedPreferences("office_profile",0).edit().putString("name","وکیل آزمون").putString("theme_id","dark").commit();
                JSONObject before=new JSONObject(db.exportJson());before.put("attachment",hash(c,uri));
                before.put("settings",new JSONObject(c.getSharedPreferences("office_profile",0).getAll()));
                try(FileOutputStream out=new FileOutputStream(expected)){out.write(before.toString().getBytes(StandardCharsets.UTF_8));}
            }else{
                boolean baseline="baseline-reopen".equals(phase);
                assertEquals(baseline?15:16,db.getReadableDatabase().getVersion());assertTrue(expected.isFile());
                String saved;try(InputStream in=new FileInputStream(expected);ByteArrayOutputStream out=new ByteArrayOutputStream()){
                    byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);saved=out.toString("UTF-8");
                }
                JSONObject before=new JSONObject(saved),after=new JSONObject(db.exportJson());
                JSONObject oldTables=before.getJSONObject("tables"),newTables=after.getJSONObject("tables");
                for(Iterator<String> i=oldTables.keys();i.hasNext();){
                    String table=i.next();assertEquals(table,oldTables.getJSONArray(table).toString(),newTables.getJSONArray(table).toString());
                }
                assertEquals(before.getString("attachment"),hash(c,Uri.parse(newTables.getJSONArray("case_attachments").getJSONObject(0).getString("content_uri"))));
                assertEquals(before.getJSONObject("settings").toString(),new JSONObject(c.getSharedPreferences("office_profile",0).getAll()).toString());
                if(!baseline){
                    assertEquals(0,newTables.getJSONArray("calculation_snapshots").length());
                    assertEquals(0,newTables.getJSONArray("calculation_references").length());
                }
                assertEquals(0,db.countDemoRows());
            }
        }
    }
    private String hash(Context c,Uri uri)throws Exception{
        MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=c.getContentResolver().openInputStream(uri)){
            byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)d.update(b,0,n);
        }return android.util.Base64.encodeToString(d.digest(),android.util.Base64.NO_WRAP);
    }
}
