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
    @Test public void referenceEntryRequiresReviewAndPreservesVersions()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
        final String[] original={null};
        try{
            ins.runOnMainSync(()->{try{
                original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);
                int before=repo.referenceHistory().size();
                String series="synthetic-rate-"+UUID.randomUUID();
                CalculationReferenceUi ui=new CalculationReferenceUi(a,()->{});
                ui.edit(CalculationReference.Kind.ANNUAL_DIYAH,null);
                input(a.page,"شناسه سری؛ شامل نوع و سال پایه").setText(series);
                input(a.page,"سال مبنا").setText("۱۴۰۴");input(a.page,"نرخ کامل عادی به ریال").setText("۱٬۰۰۰");
                fillReferenceSource(a,"1404/01/01","1404/12/29");input(a.page,"توضیح مبنا").setText("داده مصنوعی آزمون؛ نرخ رسمی نیست");
                click(a.page,"ثبت نسخه مبنا");
                CalculationReference draft=repo.referenceHistory().get(0);
                assertEquals(before+1,repo.referenceHistory().size());assertEquals(CalculationReference.Status.NEEDS_REVIEW,draft.status);
                try{draft.requireUsable("1404/05/01",1404,0,series);fail("unreviewed reference accepted");}catch(IllegalArgumentException expected){}
                click(a.page,"اصلاح / تأیید در نسخه جدید");
                assertFalse(input(a.page,"سال مبنا").isEnabled());assertFalse(input(a.page,"شناسه سری؛ شامل نوع و سال پایه").isEnabled());
                input(a.page,"علت ایجاد نسخه جدید").setText("بررسی داده مصنوعی");
                ((CheckBox)findText(a.page,"مقدار، واحد، سال، سری و منبع را بررسی و برای استفاده تأیید می‌کنم")).setChecked(true);
                click(a.page,"ثبت نسخه مبنا");
                CalculationReference confirmed=repo.referenceHistory().get(0);
                assertEquals(draft.id,confirmed.previousVersionId);assertEquals(CalculationReference.Status.USER_ENTERED,confirmed.status);
                assertTrue(confirmed.manuallyEntered);assertTrue(confirmed.userConfirmed);
                assertEquals(confirmed.id,CalculationReference.select(repo.currentReferences(),CalculationReference.Kind.ANNUAL_DIYAH,series,1404,0,"1404/05/01").id);
                try{CalculationReference.select(repo.currentReferences(),CalculationReference.Kind.ANNUAL_DIYAH,series,1405,0,"1405/05/01");fail("wrong year fallback");}catch(IllegalArgumentException expected){}
                ui.edit(CalculationReference.Kind.ANNUAL_DIYAH,draft);input(a.page,"علت ایجاد نسخه جدید").setText("stale revision");click(a.page,"ثبت نسخه مبنا");
                assertEquals(before+2,repo.referenceHistory().size());
                assertEquals("1000",repo.referenceHistory().get(1).value);assertFalse(repo.referenceHistory().get(1).userConfirmed);
                ui.edit(CalculationReference.Kind.ECONOMIC_INDEX,null);
                input(a.page,"شناسه سری؛ شامل نوع و سال پایه").setText("synthetic-annual-monthly-base1400");
                input(a.page,"سال مبنا").setText("۱۴۰۴");input(a.page,"ماه مبنا؛ ۱ تا ۱۲").setText("۱۳");
                input(a.page,"مقدار شاخص؛ عدد مثبت").setText("۱۲۳٫۴۵");fillReferenceSource(a,"1404/01/01","1404/12/29");input(a.page,"توضیح مبنا").setText("synthetic index");
                click(a.page,"ثبت نسخه مبنا");assertEquals(before+2,repo.referenceHistory().size());
                input(a.page,"ماه مبنا؛ ۱ تا ۱۲").setText("۲");click(a.page,"ثبت نسخه مبنا");
                CalculationReference index=repo.referenceHistory().get(0);assertEquals(before+3,repo.referenceHistory().size());assertEquals(2,index.month);assertEquals("123.45",index.value);
                ui.list(true);assertNotNull(findText(a.page,"نمایش نسخه‌های جاری"));
            }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();
        }finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
    private static void fillReferenceSource(MainActivity a,String start,String end){
        input(a.page,"شروع اعتبار").setText(start);input(a.page,"پایان اعتبار").setText(end);
        input(a.page,"عنوان منبع").setText("Synthetic UI fixture");input(a.page,"نوع منبع یا سند").setText("test fixture");input(a.page,"تاریخ منبع").setText("1404/01/01");
    }
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
