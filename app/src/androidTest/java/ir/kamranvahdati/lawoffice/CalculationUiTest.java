package ir.kamranvahdati.lawoffice;

import static org.junit.Assert.*;
import android.app.Instrumentation;
import android.content.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class CalculationUiTest {
    @Test public void agreedFeeSavesReopensAndRevisesWithoutChangingCase()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        for(String theme:new String[]{AppTheme.LIGHT,AppTheme.DARK}){
            c.getSharedPreferences("office_profile",0).edit().putString("theme_id",theme).putBoolean("lock_enabled",false).commit();
            MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
            final String[] original={null};final long[] caseId={0};final String title="Calculation UI "+UUID.randomUUID();
            try{
                ins.runOnMainSync(()->{
                    try{
                        original[0]=a.db.exportJson();OfficeDb.CaseRecord record=new OfficeDb.CaseRecord();record.title=title;record.feeAgreed=777;caseId[0]=a.db.addCase(record);
                        record.id=caseId[0];CalculationUi ui=new CalculationUi(a);ui.center(record);ui.fee(false,null);
                        assertEquals(View.LAYOUT_DIRECTION_RTL,a.root.getLayoutDirection());
                        input(a.page,"عنوان محاسبه").setText(title);
                        input(a.page,"مبلغ توافقی").setText("۱٬۲۳۴");
                        input(a.page,"توضیح مستند انتخاب مبلغ و مبنا").setText("توافق تست مستقل؛ پرونده تغییر نکند");
                        click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
                        List<CalculationSnapshot> list=new OfficeCalculations(a.db).list(caseId[0]);assertEquals(1,list.size());
                        CalculationSnapshot first=list.get(0);assertEquals("1,234 ریال",first.resultSnapshot.get("مبلغ"));
                        assertTrue(CalculationReport.text(first).contains("توافقی"));
                        assertEquals(777,findCase(a.db,caseId[0]).feeAgreed);
                        ui.show(first,true);click(a.page,"نسخه جدید / محاسبه مجدد");
                        input(a.page,"مبلغ توافقی").setText("۲٬۰۰۰");input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("توافق جدید تست");
                        click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
                        list=new OfficeCalculations(a.db).list(caseId[0]);assertEquals(2,list.size());
                        CalculationSnapshot next=list.get(0);assertEquals(first.id,next.previousSnapshotId);assertFalse(next.overrides.isEmpty());
                        assertEquals("1,234 ریال",new OfficeCalculations(a.db).get(first.id).resultSnapshot.get("مبلغ"));
                        assertEquals(777,findCase(a.db,caseId[0]).feeAgreed);
                        String report=CalculationReport.text(next),html=a.html(next.title,report);
                        assertTrue(html.contains("توافق جدید تست"));assertTrue(html.contains("2,000 ریال"));
                        a.go(MainActivity.CALCULATIONS,false);assertNotNull(findText(a.page,"حق‌الوکاله"));
                    }catch(Exception e){throw new AssertionError(e);}
                });ins.waitForIdleSync();
            }finally{
                ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();
            }
        }
    }
    private static OfficeDb.CaseRecord findCase(OfficeDb db,long id){for(OfficeDb.CaseRecord c:db.cases(null,"همه",null))if(c.id==id)return c;throw new AssertionError("case missing");}
    private static EditText input(View v,String hint){
        if(v instanceof EditText&&hint.contentEquals(((EditText)v).getHint()))return (EditText)v;
        if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){EditText result=input(((ViewGroup)v).getChildAt(i),hint);if(result!=null)return result;}
        return null;
    }
    private static TextView findText(View v,String text){
        if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return (TextView)v;
        if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){TextView result=findText(((ViewGroup)v).getChildAt(i),text);if(result!=null)return result;}
        return null;
    }
    private static void click(View root,String label){TextView button=findText(root,label);assertNotNull(label,button);assertTrue(button.performClick());}
}
