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
    @Test public void delayHistoricalFormPersistsRevisionsAndRejectsMissingIndex()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
        final String[] original={null};
        try{ins.runOnMainSync(()->{try{
            original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);int before=repo.list(null).size();
            CalculationUi ui=new CalculationUi(a);ui.center(null);click(a.page,"خسارت تأخیر تأدیه");selectItem(a.page,"تاریخی؛ پایه ۱۳۹۵، سال‌های ۱۳۹۹ تا ۱۴۰۱");
            input(a.page,"اصل دین؛ بدون خسارت قبلی").setText("۲۲۹۹۰۰۰");
            input(a.page,"تاریخ سررسید / تاریخ مندرج در چک").setText("1399/01/01");
            input(a.page,"تاریخ مطالبه؛ الزامی برای دین عادی").setText("1399/01/01");
            input(a.page,"مبدأ حقوقی بررسی‌شده").setText("1399/01/01");
            input(a.page,"تاریخ پرداخت / پایان محاسبه").setText("1401/12/29");
            input(a.page,"مستند انتخاب مبدأ و احراز شرایط").setText("Synthetic reviewed case fixture");
            click(a.page,"محاسبه و نمایش نتیجه");assertNull(findText(a.page,"ذخیره محاسبه"));
            confirmDelay(a.page);
            input(a.page,"تاریخ پرداخت / پایان محاسبه").setText("1405/01/01");
            click(a.page,"محاسبه و نمایش نتیجه");assertNull(findText(a.page,"ذخیره محاسبه"));
            input(a.page,"تاریخ پرداخت / پایان محاسبه").setText("1401/12/29");
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot first=repo.list(null).get(0);assertEquals(before+1,repo.list(null).size());
            assertEquals("7,943,000 ریال",first.resultSnapshot.get("اصل و خسارت"));assertEquals(2,first.references.size());
            String report=CalculationReport.text(first);assertTrue(report.contains("229.9"));assertTrue(report.contains("794.3"));assertTrue(report.contains("۱۳۹۹ تا ۱۴۰۱"));
            click(a.page,"نسخه جدید / محاسبه مجدد");input(a.page,"اصل دین؛ بدون خسارت قبلی").setText("4598000");
            input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("Corrected principal fixture");confirmDelay(a.page);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot next=repo.list(null).get(0);assertEquals(first.id,next.previousSnapshotId);assertFalse(next.overrides.isEmpty());
            assertEquals("15,886,000 ریال",next.resultSnapshot.get("اصل و خسارت"));
            String backup=a.db.exportJson();a.db.importJson(backup);assertEquals("7,943,000 ریال",repo.get(first.id).resultSnapshot.get("اصل و خسارت"));
        }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();}
        finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
    @Test public void currentOfficialIndicesPreserveSeriesAndHistory()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();final String[] original={null};
        try{ins.runOnMainSync(()->{try{
            original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);new CalculationUi(a).delay(null);
            input(a.page,"اصل دین؛ بدون خسارت قبلی").setText("6153000");
            input(a.page,"تاریخ سررسید / تاریخ مندرج در چک").setText("1405/01/01");input(a.page,"تاریخ مطالبه؛ الزامی برای دین عادی").setText("1405/01/01");
            input(a.page,"مبدأ حقوقی بررسی‌شده").setText("1405/01/01");input(a.page,"تاریخ پرداخت / پایان محاسبه").setText("1405/07/01");
            input(a.page,"مستند انتخاب مبدأ و احراز شرایط").setText("Official CBI fixture");confirmDelay(a.page);
            click(a.page,"محاسبه و نمایش نتیجه");assertNull(findText(a.page,"ذخیره محاسبه"));
            input(a.page,"تاریخ پرداخت / پایان محاسبه").setText("1405/06/31");click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot first=repo.list(null).get(0);assertEquals("8,000,000 ریال",first.resultSnapshot.get("اصل و خسارت"));
            assertEquals("CURRENT1400",first.inputSnapshot.get("indexPack"));assertEquals(CalculationCurrentIndices.VERSION,first.dataVersion);
            for(CalculationReference r:first.references){assertEquals(CalculationReference.Status.OFFICIAL_VERIFIED,r.status);assertEquals(CalculationCurrentIndices.SERIES,r.series);}
            assertTrue(CalculationReport.text(first).contains(CalculationCurrentIndices.URL));
            click(a.page,"نسخه جدید / محاسبه مجدد");input(a.page,"تاریخ پرداخت / پایان محاسبه").setText("1405/05/31");
            input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("corrected payment month");confirmDelay(a.page);click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot next=repo.list(null).get(0);assertEquals(first.id,next.previousSnapshotId);assertEquals("7,703,000 ریال",next.resultSnapshot.get("اصل و خسارت"));
            a.db.importJson(a.db.exportJson());assertEquals("8,000,000 ریال",repo.get(first.id).resultSnapshot.get("اصل و خسارت"));
            assertEquals(CalculationCurrentIndices.SERIES,repo.get(next.id).references.get(0).series);
        }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();}
        finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
    private static void confirmDelay(View root){
        for(String label:new String[]{"دین از نوع وجه رایج ایران است و مبلغ، اصل دین بدون خسارت قبلی است","در دین عادی، مطالبه، تمکن، امتناع، تغییر فاحش شاخص و نبود مصالحه مغایر را بررسی کرده‌ام","پرونده فاقد پرداخت جزئی، اقساط، اعسار، ورشکستگی، وجه التزام یا استثنای مؤثر دیگر است"})
            ((CheckBox)findText(root,label)).setChecked(true);
    }
    @Test public void assessedArshRetainsWarningReferenceAndHistory()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
        final String[] original={null};
        try{ins.runOnMainSync(()->{try{
            original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);int before=repo.list(null).size();
            CalculationUi ui=new CalculationUi(a);ui.center(null);assertNull(findText(a.page,"نرخ‌ها و شاخص‌ها"));
            click(a.page,"ارش");input(a.page,"درصد یا مبلغ اعلام‌شده").setText("۱");
            input(a.page,"شرح صدمه و عضو یا منفعت").setText("Synthetic assessed injury");
            input(a.page,"تاریخ وقوع صدمه").setText("1404/11/01");input(a.page,"تاریخ پرداخت / ارزش‌گذاری انتخابی").setText("1405/01/01");
            input(a.page,"مرجع، شماره و تاریخ مستند تعیین مبلغ یا درصد").setText("Synthetic court decision 1");
            input(a.page,"نظر کارشناسی؛ جدا از تصمیم مرجع").setText("Expert proposed 2 percent, not adopted");
            input(a.page,"مستند انتخاب تاریخ ارزش‌گذاری").setText("Synthetic payment basis");
            click(a.page,"محاسبه و نمایش نتیجه");assertEquals(before,repo.list(null).size());assertNotNull(input(a.page,"درصد یا مبلغ اعلام‌شده"));
            ((CheckBox)findText(a.page,"مقدار توسط مرجع صالح تعیین شده و مبنای درصد، دیه کامل عادی است؛ این ورودی یک صدمه مستقل و بدون تغلیظ است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot first=repo.list(null).get(0);assertEquals(before+1,repo.list(null).size());
            assertEquals("210,000,000 ریال",first.resultSnapshot.get("مبلغ"));assertEquals(1,first.references.size());
            assertEquals(CalculationReference.Status.REVIEWED_PUBLICATION,first.references.get(0).status);
            String report=CalculationReport.text(first);assertTrue(report.contains(CalculationSnapshot.ARSH_WARNING));
            assertTrue(a.html(first.title,report).contains(CalculationSnapshot.ARSH_WARNING));assertTrue(report.contains("Expert proposed 2 percent"));
            click(a.page,"نسخه جدید / محاسبه مجدد");input(a.page,"درصد یا مبلغ اعلام‌شده").setText("۲");
            input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("New assessment fixture");
            ((CheckBox)findText(a.page,"مقدار توسط مرجع صالح تعیین شده و مبنای درصد، دیه کامل عادی است؛ این ورودی یک صدمه مستقل و بدون تغلیظ است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot next=repo.list(null).get(0);assertEquals(first.id,next.previousSnapshotId);assertFalse(next.overrides.isEmpty());
            assertEquals("420,000,000 ریال",next.resultSnapshot.get("مبلغ"));assertEquals("210,000,000 ریال",repo.get(first.id).resultSnapshot.get("مبلغ"));
            String backup=a.db.exportJson();a.db.importJson(backup);assertEquals(CalculationReference.Status.REVIEWED_PUBLICATION,repo.get(first.id).references.get(0).status);
            ui.sources();assertNotNull(findText(a.page,"به‌روزرسانی مرکزی"));
        }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();}
        finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
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
    @Test public void enforcementFeePreservesAwardBoundsAndRevision()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
        final String[] original={null};
        try{ins.runOnMainSync(()->{try{
            original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);
            int before=repo.list(null).size();CalculationUi ui=new CalculationUi(a);ui.fee(true,null);
            selectItem(a.page,"اجرای احکام حقوقی و اسناد رسمی؛ ماده ۲۵");
            input(a.page,"بهای خواسته مالی یا مبلغ منتخب کل تعرفه در سایر دسته‌ها").setText("12000000");
            input(a.page,"محکوم‌به یا مورد اجرا؛ فقط دسته اجرای احکام، با واحد انتخابی").setText("1000000000");
            input(a.page,"تاریخ مبنای محاسبه").setText("1405/01/01");
            input(a.page,"توضیح مستند انتخاب مبلغ و مبنا").setText("Article 25 independent enforcement fixture");
            ((CheckBox)findText(a.page,"انطباق این دسته و نسخه تعرفه را بررسی کرده‌ام؛ این محاسبه فاقد عوامل ویژه پشتیبانی‌نشده است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot first=repo.list(null).get(0);assertEquals(before+1,repo.list(null).size());
            assertEquals("12,000,000 ریال",first.resultSnapshot.get("مبلغ"));
            assertEquals("1000000000",first.inputSnapshot.get("enforcementAward"));
            assertTrue(first.inputSnapshot.get("_rulePackSnapshot").contains("enforcement.maximum.percent=2"));
            assertTrue(CalculationReport.text(first).contains("ماده ۲۵"));
            click(a.page,"نسخه جدید / محاسبه مجدد");
            assertEquals("1000000000",input(a.page,"محکوم‌به یا مورد اجرا؛ فقط دسته اجرای احکام، با واحد انتخابی").getText().toString());
            input(a.page,"بهای خواسته مالی یا مبلغ منتخب کل تعرفه در سایر دسته‌ها").setText("20000001");
            input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("above exact ceiling");
            ((CheckBox)findText(a.page,"انطباق این دسته و نسخه تعرفه را بررسی کرده‌ام؛ این محاسبه فاقد عوامل ویژه پشتیبانی‌نشده است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");assertEquals(before+1,repo.list(null).size());
            assertNotNull(input(a.page,"محکوم‌به یا مورد اجرا؛ فقط دسته اجرای احکام، با واحد انتخابی"));
            input(a.page,"بهای خواسته مالی یا مبلغ منتخب کل تعرفه در سایر دسته‌ها").setText("20000000");
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot next=repo.list(null).get(0);assertEquals(first.id,next.previousSnapshotId);
            assertEquals("20,000,000 ریال",next.resultSnapshot.get("مبلغ"));
            a.db.importJson(a.db.exportJson());assertEquals("12,000,000 ریال",repo.get(first.id).resultSnapshot.get("مبلغ"));
        }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();}
        finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
    @Test public void civilDispositionPreservesExactStageAndRevision()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
        final String[] original={null};
        try{ins.runOnMainSync(()->{try{
            original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);
            int before=repo.list(null).size();CalculationUi ui=new CalculationUi(a);ui.fee(true,null);
            selectItem(a.page,"بدوی");
            selectItem(a.page,"ابطال دادخواست پیش از دفاع؛ بدوی");
            input(a.page,"بهای خواسته مالی یا مبلغ منتخب کل تعرفه در سایر دسته‌ها").setText("500000000");
            input(a.page,"تاریخ مبنای محاسبه").setText("1405/01/01");
            input(a.page,"توضیح مستند انتخاب مبلغ و مبنا").setText("Article 12 first-stage annulment before defense fixture");
            ((CheckBox)findText(a.page,"انطباق این دسته و نسخه تعرفه را بررسی کرده‌ام؛ این محاسبه فاقد عوامل ویژه پشتیبانی‌نشده است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot first=repo.list(null).get(0);assertEquals(before+1,repo.list(null).size());
            assertEquals("6,000,000 ریال",first.resultSnapshot.get("مبلغ"));
            assertEquals("ANNUL_BEFORE_DEFENSE",first.inputSnapshot.get("disposition"));
            assertTrue(first.inputSnapshot.get("_rulePackSnapshot").contains("disposition.ANNUL_BEFORE_DEFENSE=25"));
            assertTrue(CalculationReport.text(first).contains("ماده ۱۲"));
            click(a.page,"نسخه جدید / محاسبه مجدد");
            assertEquals("500000000",input(a.page,"بهای خواسته مالی یا مبلغ منتخب کل تعرفه در سایر دسته‌ها").getText().toString());
            selectItem(a.page,"تجدیدنظر");
            input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("incompatible stage then corrected disposition");
            ((CheckBox)findText(a.page,"انطباق این دسته و نسخه تعرفه را بررسی کرده‌ام؛ این محاسبه فاقد عوامل ویژه پشتیبانی‌نشده است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");assertEquals(before+1,repo.list(null).size());
            assertNotNull(input(a.page,"بهای خواسته مالی یا مبلغ منتخب کل تعرفه در سایر دسته‌ها"));
            selectItem(a.page,"سقوط دعوای تجدیدنظر پس از دفاع");
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot next=repo.list(null).get(0);assertEquals(first.id,next.previousSnapshotId);
            assertEquals("8,000,000 ریال",next.resultSnapshot.get("مبلغ"));
            a.db.importJson(a.db.exportJson());assertEquals("6,000,000 ریال",repo.get(first.id).resultSnapshot.get("مبلغ"));
        }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();}
        finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
    @Test public void prescribedHeadScheduleRetainsSourceAndRevises()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
        final String[] original={null};
        try{ins.runOnMainSync(()->{try{
            original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);int before=repo.list(null).size();
            CalculationUi ui=new CalculationUi(a);ui.body(false,null);
            assertTrue(selectItem(a.page,"جدول صدمات مستقل سر و صورت؛ ماده ۷۰۹"));
            assertTrue(selectItem(a.page,"دامیه سر و صورت"));
            input(a.page,"شرح صدمه و عضو یا منفعت").setText("Synthetic independent head injury");
            input(a.page,"تاریخ وقوع صدمه").setText("1405/01/01");input(a.page,"تاریخ پرداخت / ارزش‌گذاری انتخابی").setText("1405/02/01");
            input(a.page,"مرجع، شماره و تاریخ مستند تعیین مبلغ یا درصد").setText("Medical classification fixture");
            input(a.page,"مستند انتخاب تاریخ ارزش‌گذاری").setText("Synthetic payment date");
            click(a.page,"محاسبه و نمایش نتیجه");assertEquals(before,repo.list(null).size());
            ((CheckBox)findText(a.page,"تشخیص مستند سر یا صورت و استقلال این صدمه بررسی شده؛ فاقد فوت، تداخل، آثار اضافه و حکم خاص است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot first=repo.list(null).get(0);assertEquals("420,000,000 ریال",first.resultSnapshot.get("مبلغ"));
            assertEquals("DAMIYA",first.inputSnapshot.get("prescribedInjury"));assertEquals("PRESCRIBED",first.inputSnapshot.get("bodyMode"));
            assertTrue(first.inputSnapshot.get("_rulePackSnapshot").contains("injury.DAMIYA.ratio=2/100"));
            String report=CalculationReport.text(first);assertTrue(report.contains("ماده ۷۰۹"));assertTrue(report.contains("متن تاریخی مجلس"));
            click(a.page,"نسخه جدید / محاسبه مجدد");assertTrue(selectItem(a.page,"حارصه سر و صورت"));
            input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("Corrected medical classification fixture");
            ((CheckBox)findText(a.page,"تشخیص مستند سر یا صورت و استقلال این صدمه بررسی شده؛ فاقد فوت، تداخل، آثار اضافه و حکم خاص است")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");
            CalculationSnapshot next=repo.list(null).get(0);assertEquals(first.id,next.previousSnapshotId);assertEquals("210,000,000 ریال",next.resultSnapshot.get("مبلغ"));
            a.db.importJson(a.db.exportJson());assertEquals("420,000,000 ریال",repo.get(first.id).resultSnapshot.get("مبلغ"));
        }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();}
        finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
    @Test public void deathTaghlizRejectsUnknownAndPreservesRevision()throws Exception{
        Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
        MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
        final String[] original={null};
        try{ins.runOnMainSync(()->{try{
            original[0]=a.db.exportJson();OfficeCalculations repo=new OfficeCalculations(a.db);int before=repo.list(null).size();new CalculationUi(a).death(null);
            input(a.page,"درصد مبنای دیه نفس عادی از نرخ کامل؛ بدون افزایش").setText("100");
            input(a.page,"تاریخ رفتار مرتکب").setText("1405/01/01");input(a.page,"تاریخ فوت").setText("1405/01/02");
            input(a.page,"تاریخ پرداخت / ارزش‌گذاری انتخابی").setText("1405/01/03");
            input(a.page,"مستند مبنای عادی، زمان و مکان رفتار و فوت").setText("independent 555-557 facts");
            input(a.page,"مستند انتخاب تاریخ ارزش‌گذاری").setText("valuation fixture");
            ((CheckBox)findText(a.page,"موضوع دیه نفس است؛ مبنای عادی و شرایط مواد ۵۵۵ تا ۵۵۷ را بررسی کرده‌ام و مبلغ ورودی شامل افزایش نیست")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");assertEquals(before,repo.list(null).size());assertNotNull(input(a.page,"تاریخ فوت"));
            findingSelection(a,"رفتار در ماه حرام واقع شده است؟",1);findingSelection(a,"فوت در ماه حرام واقع شده است؟",1);
            findingSelection(a,"رفتار در محدوده حرم مکه واقع شده است؟",1);findingSelection(a,"فوت در محدوده حرم مکه واقع شده است؟",1);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");CalculationSnapshot first=repo.list(null).get(0);
            assertEquals("28,000,000,000 ریال",first.resultSnapshot.get("مبلغ"));assertEquals("DEATH",first.inputSnapshot.get("bodyMode"));
            assertTrue(first.inputSnapshot.get("_rulePackSnapshot").contains("increment.denominator=3"));
            assertTrue(CalculationReport.text(first).contains("۵۵۵ تا ۵۵۷"));
            click(a.page,"نسخه جدید / محاسبه مجدد");assertEquals("1405/01/02",input(a.page,"تاریخ فوت").getText().toString());
            findingSelection(a,"رفتار در ماه حرام واقع شده است؟",2);findingSelection(a,"رفتار در محدوده حرم مکه واقع شده است؟",2);
            input(a.page,"علت تغییر نسبت به نسخه قبلی").setText("corrected act findings");
            ((CheckBox)findText(a.page,"موضوع دیه نفس است؛ مبنای عادی و شرایط مواد ۵۵۵ تا ۵۵۷ را بررسی کرده‌ام و مبلغ ورودی شامل افزایش نیست")).setChecked(true);
            click(a.page,"محاسبه و نمایش نتیجه");click(a.page,"ذخیره محاسبه");CalculationSnapshot next=repo.list(null).get(0);
            assertEquals(first.id,next.previousSnapshotId);assertEquals("21,000,000,000 ریال",next.resultSnapshot.get("مبلغ"));
            assertFalse(next.overrides.isEmpty());a.db.importJson(a.db.exportJson());
            assertEquals("28,000,000,000 ریال",repo.get(first.id).resultSnapshot.get("مبلغ"));assertEquals("NO",repo.get(next.id).inputSnapshot.get("actMonth"));
        }catch(Exception e){throw new AssertionError(e);}});ins.waitForIdleSync();}
        finally{ins.runOnMainSync(()->{try{if(original[0]!=null)a.db.importJson(original[0]);}catch(Exception e){throw new AssertionError(e);}finally{a.finish();}});ins.waitForIdleSync();}
    }
    private static void findingSelection(MainActivity a,String label,int index){
        for(int i=0;i<a.page.getChildCount()-1;i++)if(a.page.getChildAt(i) instanceof TextView&&label.contentEquals(((TextView)a.page.getChildAt(i)).getText())&&a.page.getChildAt(i+1) instanceof Spinner){((Spinner)a.page.getChildAt(i+1)).setSelection(index);return;}
        throw new AssertionError("finding field missing: "+label);
    }
    private static boolean selectItem(View v,String label){
        if(v instanceof Spinner){Spinner spinner=(Spinner)v;for(int i=0;i<spinner.getCount();i++)if(label.equals(spinner.getItemAtPosition(i).toString())){spinner.setSelection(i);return true;}}
        if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)if(selectItem(((ViewGroup)v).getChildAt(i),label))return true;
        return false;
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
