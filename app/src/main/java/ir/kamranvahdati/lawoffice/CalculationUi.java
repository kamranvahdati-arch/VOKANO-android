package ir.kamranvahdati.lawoffice;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
import java.util.*;

/** Scoped calculation screens using the existing theme and PDF pipeline.
 * Unverified legal domains remain visibly unavailable in this development candidate.
 */
final class CalculationUi {
    private final MainActivity a;
    private final OfficeCalculations repo;
    private OfficeDb.CaseRecord related;
    CalculationUi(MainActivity a){this.a=a;repo=new OfficeCalculations(a.db);}
    void center(OfficeDb.CaseRecord c){
        related=c;a.clear("مرکز محاسبات حقوقی",c==null?"محاسبات مستقل و تاریخچه":"محاسبات پرونده: "+c.title);
        a.detailBack=c==null?a::dashboard:()->a.caseDetail(c);
        button("حق‌الوکاله",()->new AlertDialog.Builder(a).setTitle("نوع حق‌الوکاله")
            .setItems(new String[]{"توافقی قراردادی","تعرفه‌ای؛ فروض پشتیبانی‌شده"},(d,n)->fee(n==1,null)).show());
        button("خسارت تأخیر تأدیه",()->delay(null));
        button("دیه",()->new AlertDialog.Builder(a).setTitle("نوع محاسبه دیه").setItems(new String[]{"صدمات یا مقدار اعلام‌شده","دیه نفس و بررسی تغلیظ"},(d,n)->{if(n==0)body(false,null);else death(null);}).show());button("ارش",()->body(true,null));
        button("تاریخچه محاسبات",this::history);
        button("منابع محاسبات و وضعیت به‌روزرسانی",this::sources);
        a.page.addView(a.info("دامنه ابزارها","تعرفه فروض مشخص متن مصوب ۱۳۹۸ را پوشش می‌دهد؛ دیه شامل بررسی تغلیظ نفس، جدول محدود صدمات سر و صورت و تبدیل مقدار اعلام‌شده است؛ ارش تبدیل مقدار اعلام‌شده است. تعیین خودکار صدمه و همه موارد خاص هنوز ارائه نشده است."));
    }
    private void unavailable(String name){a.preview(name,"تأیید مبانی رسمی این بخش هنوز تکمیل نشده؛ محاسبه حقوقی فعال نیست.");}
    private void button(String title,Runnable action){TextView v=a.action(title);v.setOnClickListener(x->{try{action.run();}catch(Exception e){a.toast(message(e));}});a.page.addView(v);}
    private EditText field(String label,String value){a.page.addView(a.txt(label,12,a.MUTED));EditText v=a.input(label);v.setText(value);a.page.addView(v);return v;}
    private Spinner pick(String label,String[] values){a.page.addView(a.txt(label,12,a.MUTED));Spinner s=a.spinner(values);a.page.addView(s);return s;}
    private CheckBox confirm(String label){CheckBox c=new CheckBox(a);c.setText(label);c.setTextColor(a.INK);a.page.addView(c);return c;}
    private String previous(CalculationSnapshot p,String key,String fallback){return p==null?fallback:p.inputSnapshot.getOrDefault(key,fallback);}

    private CalculationReference bodyRate()throws Exception{
        try(InputStreamReader reader=new InputStreamReader(a.getAssets().open("calculation/diyah-1405.properties"),StandardCharsets.UTF_8)){
            return CalculationBodyMath.readRate(reader);
        }
    }
    void sources(){
        a.clear("منابع محاسبات","بسته همراه برنامه؛ اتصال مرکزی هنوز راه‌اندازی نشده است");a.detailBack=()->center(related);
        try{CalculationReference r=bodyRate();a.page.addView(a.info(r.sourceTitle,r.value+" ریال\n"+r.version+"\n"+r.sourceType+"\n"+r.sourceUrl+"\n"+r.notes));}
        catch(Exception e){a.toast(message(e));}
        a.page.addView(a.info("شاخص رسمی تا شهریور ۱۴۰۵","پایه ۱۴۰۰؛ پوشش فروردین ۱۴۰۰ تا شهریور ۱۴۰۵؛ دریافت مستقیم از بانک مرکزی. با بسته قدیمی پایه ۱۳۹۵ مخلوط نمی‌شود.\n"+CalculationCurrentIndices.VERSION+"\n"+CalculationCurrentIndices.URL));
        a.page.addView(a.info("شاخص‌های تأخیر تأدیه","پوشش: ۱۳۹۹ تا ۱۴۰۱؛ نسخه "+CalculationDelayPack.VERSION+"\nمنبع تصویر: "+CalculationDelayPack.URL+"\nشاخص جدیدتر یا متعارض در این نسخه جایگزین نمی‌شود."));
        a.page.addView(a.info("به‌روزرسانی مرکزی","پس از آماده شدن سرور، انتشار نرخ‌ها و شاخص‌ها از پنل مدیر انجام خواهد شد. این نسخه اتصال زنده یا به‌روزرسانی خودکار ندارد. ورود دستی نرخ مرجع برای کاربران عمومی ارائه نمی‌شود؛ سابقه داده‌های قبلی حذف نشده است."));
    }
    private CalculationDelayPack delayPack()throws Exception{
        try(InputStreamReader reader=new InputStreamReader(a.getAssets().open("calculation/delay-indices-1399-1401.properties"),StandardCharsets.UTF_8)){
            return new CalculationDelayPack(reader);
        }
    }
    private CalculationCurrentIndices currentIndices()throws Exception{
        try(InputStreamReader r=new InputStreamReader(a.getAssets().open("calculation/cbi-base1400-1405-06-v1.properties"),StandardCharsets.UTF_8)){return new CalculationCurrentIndices(r);}
    }
    void delay(CalculationSnapshot prior){
        a.clear("خسارت تأخیر تأدیه","شاخص رسمی تا شهریور ۱۴۰۵؛ انتخاب یک سری هم‌مبنا برای هر دو تاریخ");a.detailBack=()->center(related);
        EditText title=field("عنوان محاسبه",prior==null?"خسارت تأخیر تأدیه":prior.title);
        List<OfficeDb.CaseRecord> cases=a.db.cases(null,"همه",null);List<String> names=new ArrayList<>();names.add("مستقل از پرونده");for(OfficeDb.CaseRecord c:cases)names.add(c.title);
        Spinner casePick=pick("ارتباط محاسبه",names.toArray(new String[0]));Long selected=prior==null?(related==null?null:related.id):prior.caseId;
        for(int i=0;i<cases.size();i++)if(selected!=null&&cases.get(i).id==selected)casePick.setSelection(i+1);
        Spinner indexPack=pick("بسته شاخص برای هر دو تاریخ",new String[]{"رسمی؛ پایه ۱۴۰۰، فروردین ۱۴۰۰ تا شهریور ۱۴۰۵","تاریخی؛ پایه ۱۳۹۵، سال‌های ۱۳۹۹ تا ۱۴۰۱"});
        indexPack.setSelection(prior!=null&&!"CURRENT1400".equals(previous(prior,"indexPack","HISTORICAL1395"))?1:0);
        Spinner mode=pick("نوع دین",new String[]{"دین عادی؛ شرایط ماده ۵۲۲","چک مشمول قاعده عمومی رأی ۸۱۲"});
        mode.setSelection(prior!=null&&prior.type==CalculationSnapshot.Type.CHEQUE_DELAY?1:0);
        Spinner currency=pick("واحد اصل دین",new String[]{"ریال","تومان"});currency.setSelection("TOMAN".equals(previous(prior,"currency","RIAL"))?1:0);
        EditText amount=field("اصل دین؛ بدون خسارت قبلی",previous(prior,"amount",""));
        EditText due=field("تاریخ سررسید / تاریخ مندرج در چک",previous(prior,"dueDate",""));a.bindJalaliPicker(due);
        EditText demand=field("تاریخ مطالبه؛ الزامی برای دین عادی",previous(prior,"demandDate",""));a.bindJalaliPicker(demand);
        EditText start=field("مبدأ حقوقی بررسی‌شده",previous(prior,"startDate",""));a.bindJalaliPicker(start);
        EditText end=field("تاریخ پرداخت / پایان محاسبه",previous(prior,"effectiveDate",""));a.bindJalaliPicker(end);
        EditText basis=field("مستند انتخاب مبدأ و احراز شرایط",previous(prior,"basis",""));
        EditText reason=prior==null?null:field("علت تغییر نسبت به نسخه قبلی","");
        CheckBox money=confirm("دین از نوع وجه رایج ایران است و مبلغ، اصل دین بدون خسارت قبلی است");
        CheckBox ordinary=confirm("در دین عادی، مطالبه، تمکن، امتناع، تغییر فاحش شاخص و نبود مصالحه مغایر را بررسی کرده‌ام");
        CheckBox cheque=confirm("در مسیر چک، شمول قاعده رأی ۸۱۲ و تاریخ مندرج در چک را بررسی کرده‌ام");
        Spinner paymentsMode=pick("روش پرداخت",new String[]{"پرداخت یک‌جا؛ بدون پرداخت قبلی","پرداخت‌های جزئی مستند از محکوم‌به","توقف خسارت از تاریخ ثبوت اعسار","تأخیر یک قسط معوقِ حکم تقسیط"});
        paymentsMode.setSelection(CalculationDelayExceptions.Route.valueOf(previous(prior,"paymentMethod","SINGLE")).ordinal());
        EditText payments=field("پرداخت‌های قبلی؛ هر سطر: تاریخ | مبلغ | مستند",previous(prior,"payments",""));
        payments.setSingleLine(false);payments.setMinLines(3);
        CheckBox partialScope=confirm("در پرداخت جزئی، محکومیت به دین با تعدیل ارزش و تخصیص نسبی پرداخت را بررسی کرده‌ام؛ بدون اعسار، تقسیط قضایی، ورشکستگی یا استثنای دیگر");
        EditText insolvencyDate=field("تاریخ ثبوت اعسار طبق حکم؛ فقط مسیر اعسار یا قسط",previous(prior,"insolvencyDate",""));a.bindJalaliPicker(insolvencyDate);
        EditText judgmentBasis=field("مشخصات حکم و مستند تاریخ اعسار یا سررسید قسط",previous(prior,"judgmentBasis",""));
        CheckBox exceptionScope=confirm("حکم و انطباق رأی ۸۲۴ را بررسی کرده‌ام؛ این محاسبه فقط دوره پیش از اعسار یا یک قسط معوق است و پرداخت جزئی، ورشکستگی یا استثنای دیگری ندارد");
        a.page.addView(a.info("اعسار و اقساط معوق","در توقف، تاریخ ثبوت اعسار طبق حکم ملاک است؛ صرف دادخواست کافی نیست. این نتیجه شامل خسارت اقساط معوق پس از حکم نیست. برای هر قسط معوق، مسیر جدا را انتخاب و فقط مبلغ همان قسط و سررسید آن را وارد کنید؛ مبلغ کل حکم و تاریخ چک را جایگزین نکنید. احراز تمکن در حدود قسط و مفاد حکم باید مستند باشد."));
        CheckBox simple=confirm("پرونده فاقد پرداخت جزئی، اقساط، اعسار، ورشکستگی، وجه التزام یا استثنای مؤثر دیگر است");
        a.page.addView(a.info("دامنه و منابع","این فرم استحقاق یا مبدأ را خودکار تعیین نمی‌کند. ماده ۵۲۲، قاعده عمومی رأی ۸۱۲ و فرمول رأی ۸۵۰ مبنای بررسی‌اند. پرداخت جزئی، توقف به علت اعسار و یک قسط معوق مسیر مستقل و تأیید شرایط خود را دارند؛ سایر موارد خاص پشتیبانی نمی‌شوند. بسته رسمی پایه ۱۴۰۰ تا شهریور ۱۴۰۵ و بسته تاریخی پایه ۱۳۹۵ فقط ۱۳۹۹ تا ۱۴۰۱ را پوشش می‌دهند. شاخص‌های دو سبد قابل اختلاط نیستند؛ ماه فاقد داده تخمین زده نمی‌شود. اصل دین در شاخص پایان تقسیم بر شاخص مبدأ ضرب می‌شود؛ سود مرکب و محاسبه روزشمار اعمال نمی‌شود."));
        button("محاسبه و نمایش نتیجه",()->{try{
            CalculationDelayMath.Mode selectedMode=mode.getSelectedItemPosition()==0?CalculationDelayMath.Mode.ORDINARY_DEBT:CalculationDelayMath.Mode.CHEQUE;
            String d=CalculationReference.date(due.getText().toString()),s=CalculationReference.date(start.getText().toString()),e=CalculationReference.date(end.getText().toString());
            String demandValue=demand.getText().toString().trim();if(!demandValue.isEmpty())demandValue=CalculationReference.date(demandValue);
            CalculationDelayExceptions.Route route=CalculationDelayExceptions.Route.values()[paymentsMode.getSelectedItemPosition()];
            boolean partial=route==CalculationDelayExceptions.Route.PARTIAL;
            boolean exception=route==CalculationDelayExceptions.Route.INSOLVENCY_STOP||route==CalculationDelayExceptions.Route.OVERDUE_INSTALLMENT;
            String indexedEnd=CalculationDelayExceptions.indexedEnd(route,selectedMode,d,s,e,insolvencyDate.getText().toString(),judgmentBasis.getText().toString(),exceptionScope.isChecked());
            if(!partial&&!payments.getText().toString().trim().isEmpty())throw new IllegalArgumentException("برای ثبت پرداخت‌های قبلی، روش پرداخت جزئی را انتخاب کنید");
            String explanation=CalculationDelayPack.validateScope(selectedMode,d,demandValue,s,e,basis.getText().toString(),money.isChecked(),ordinary.isChecked(),exception?exceptionScope.isChecked():cheque.isChecked(),exception?exceptionScope.isChecked():partial?partialScope.isChecked():simple.isChecked());
            CalculationArithmetic.Currency unit=currency.getSelectedItemPosition()==0?CalculationArithmetic.Currency.RIAL:CalculationArithmetic.Currency.TOMAN;
            long principal=CalculationArithmetic.money(amount.getText().toString(),unit);
            boolean current=indexPack.getSelectedItemPosition()==0;CalculationReference first,last;
            if(current){CalculationCurrentIndices pack=currentIndices();first=pack.at(s);last=pack.at(indexedEnd);}
            else{CalculationDelayPack pack=delayPack();first=pack.at(s);last=pack.at(indexedEnd);}
            String selectedVersion=current?CalculationCurrentIndices.VERSION:CalculationDelayPack.VERSION;
            CalculationDelayMath.Result r=CalculationDelayMath.calculate(selectedMode,principal,s,indexedEnd,explanation,first.series,first,last,RoundingMode.HALF_UP);
            Long caseId=casePick.getSelectedItemPosition()==0?null:cases.get(casePick.getSelectedItemPosition()-1).id;
            Map<String,String> inputs=new LinkedHashMap<>(),results=new LinkedHashMap<>();
            inputs.put("title",title.getText().toString());inputs.put("caseId",caseId==null?"":caseId.toString());inputs.put("amount",amount.getText().toString());inputs.put("currency",unit.name());
            inputs.put("dueDate",d);inputs.put("demandDate",demandValue);inputs.put("startDate",s);inputs.put("effectiveDate",e);inputs.put("basis",explanation);
            inputs.put("indexPack",current?"CURRENT1400":"HISTORICAL1395");inputs.put("indexSeries",first.series);inputs.put("delayMode",selectedMode.name());inputs.put("scopeConfirmed","وجه رایج و اصل دین؛ بدون موارد خاص؛ تأیید شرایط مسیر انتخابی توسط کاربر");
            inputs.put("sourceUrl","https://nezamat.ir/post-31264/ | https://dotic.ir/news/17037/ | https://guilanbar.ir/wp-content/uploads/2021/07/images_1398_09_812.jpeg.webp");
            results.put("اصل دین",CalculationArithmetic.display(principal,CalculationArithmetic.Currency.RIAL));
            results.put("خسارت تأخیر",CalculationArithmetic.display(r.damagesRials,CalculationArithmetic.Currency.RIAL));
            results.put("اصل و خسارت",CalculationArithmetic.display(r.adjustedRials,CalculationArithmetic.Currency.RIAL));
            results.put("معادل تومان اصل و خسارت",CalculationArithmetic.display(r.adjustedRials,CalculationArithmetic.Currency.TOMAN));results.put("کسر دقیق ریالی",r.exactAdjustedRials);
            List<String> steps=new ArrayList<>(Arrays.asList(principal+" × "+last.value+" ÷ "+first.value+" = "+r.exactAdjustedRials+" ریال", "خسارت = مبلغ تعدیل‌شده منهای اصل دین؛ گرد کردن فقط در پایان"));
            List<String> warnings=Arrays.asList("نتیجه مشروط به صحت بررسی حقوقی کاربر است؛ تعیین استحقاق و مبلغ قابل وصول بر عهده مرجع صالح است. فقط مسیر و شرایط ثبت‌شده در این گزارش بررسی شده‌اند.",current?"شاخص‌ها مستقیم از گزارش رسمی بانک مرکزی دریافت شده‌اند؛ پایه ۱۴۰۰ و پوشش تا شهریور ۱۴۰۵. اتصال به‌روزرسانی زنده وجود ندارد.":"شاخص‌ها از تصویر بازنشرشده جدول بانک مرکزی تطبیق داده شده‌اند؛ پوشش فقط ۱۳۹۹ تا ۱۴۰۱ است. اتصال به‌روزرسانی زنده وجود ندارد.");
            List<CalculationReference> delayRefs=first.id.equals(last.id)?Collections.singletonList(first):Arrays.asList(first,last);
            String delayEngine=CalculationDelayMath.ENGINE_VERSION,delayRule="reviewed-simple-delay-522-812-850/1";
            inputs.put("paymentMethod",route.name());
            if(exception){
                inputs.put("insolvencyDate",CalculationReference.date(insolvencyDate.getText().toString()));inputs.put("judgmentBasis",judgmentBasis.getText().toString());
                inputs.put("indexedEnd",indexedEnd);inputs.put("paymentSource",CalculationDelayExceptions.SOURCE);
                inputs.put("scopeConfirmed","حکم و شرایط مسیر رأی ۸۲۴ تأیید شده؛ بدون پرداخت جزئی و سایر استثناها");
                inputs.put("resultScope",route==CalculationDelayExceptions.Route.INSOLVENCY_STOP?"فقط تا ثبوت اعسار؛ بدون خسارت اقساط معوق بعدی":"فقط یک قسط معوق؛ نه کل محکوم‌به");
                steps.add(route==CalculationDelayExceptions.Route.INSOLVENCY_STOP?"تاریخ پایان درخواست "+e+"؛ پایان مؤثر تعدیل "+indexedEnd+" طبق تاریخ ثبوت اعسار. از توقف تا پایان درخواست، تعدیل افزوده نشده است.":"مبدأ فقط سررسید همین قسطِ حکم است: "+s+"؛ خسارت دوره پیش از اعسار در این نتیجه جمع نشده است.");
                warnings=new ArrayList<>(warnings);warnings.add("رأی ۸۲۴؛ متن بازنشرشده بررسی‌شده. تاریخ و مبلغ از حکم و شرایط پرونده وارد می‌شود؛ نرم‌افزار اعسار یا معوق بودن قسط را احراز نمی‌کند.");
                delayRule=CalculationDelayExceptions.VERSION;delayEngine="reviewed-delay-exceptions/1";
            }
            if(partial){
                CalculationPartialPayments.Indices lookup;
                if(current){CalculationCurrentIndices pack=currentIndices();lookup=pack::at;}
                else{CalculationDelayPack pack=delayPack();lookup=pack::at;}
                CalculationPartialPayments.Result paid=CalculationPartialPayments.calculate(selectedMode,principal,s,e,explanation,
                    CalculationPartialPayments.parse(payments.getText().toString(),unit),lookup,RoundingMode.HALF_UP);
                inputs.put("payments",payments.getText().toString());inputs.put("scopeConfirmed","پرداخت جزئی از محکوم‌به با تعدیل ارزش؛ فاقد سایر استثناها؛ تأیید مستند توسط کاربر");
                inputs.put("paymentSource",CalculationPartialPayments.SOURCE);
                results.clear();results.put("اصل اولیه",CalculationArithmetic.display(principal,CalculationArithmetic.Currency.RIAL));
                results.put("جمع پرداخت‌های واقعی",CalculationArithmetic.display(paid.paidRials,CalculationArithmetic.Currency.RIAL));
                results.put("مانده قابل محاسبه",CalculationArithmetic.display(paid.remainingRials,CalculationArithmetic.Currency.RIAL));
                results.put("خسارت تجمعی تا پایان",CalculationArithmetic.display(paid.totalDamagesRials,CalculationArithmetic.Currency.RIAL));
                results.put("کسر دقیق مانده ریالی",paid.exactRemaining.toString());
                steps=new ArrayList<>(paid.steps);delayRefs=paid.references;delayEngine="partial-delay-math/1";delayRule=CalculationPartialPayments.VERSION;
                warnings=new ArrayList<>(warnings);warnings.add("تخصیص نسبی بر مبنای بند چهارم نظریه مشورتی ۷/۱۴۰۲/۲۲۰، بازنشر بررسی‌شده؛ نظریه مشورتی رأی لازم‌الاتباع نیست. مانده نتیجه، جایگزین مبلغ اصل و خسارت پیش از کسر پرداخت‌هاست.");
            }
            long now=Math.max(System.currentTimeMillis(),prior==null?0:prior.createdAt+1);List<CalculationSnapshot.Override> audit=new ArrayList<>();
            if(prior!=null){String why=CalculationReference.required(reason.getText().toString(),"علت تغییر");Set<String> keys=new LinkedHashSet<>(prior.inputSnapshot.keySet());keys.addAll(inputs.keySet());for(String key:keys){String old=prior.inputSnapshot.getOrDefault(key,""),value=inputs.getOrDefault(key,"");if(!old.equals(value))audit.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.INPUT,key,old,value,now,why));}}
            show(new CalculationSnapshot(UUID.randomUUID().toString(),prior==null?"":prior.id,title.getText().toString(),caseId,
                selectedMode==CalculationDelayMath.Mode.ORDINARY_DEBT?CalculationSnapshot.Type.ORDINARY_DEBT_DELAY:CalculationSnapshot.Type.CHEQUE_DELAY,
                delayEngine,delayRule,selectedVersion,JalaliDate.today().value(),e,
                inputs,results,delayRefs,steps,warnings,audit,RoundingMode.HALF_UP,now,now),false);
        }catch(Exception e){a.toast(message(e));}});
    }
    private Spinner finding(String label,CalculationSnapshot prior,String key){
        Spinner s=pick(label,new String[]{"بررسی نشده","بله؛ مستند احراز شده","خیر؛ مستند رد شده"});
        s.setSelection(CalculationDeath.Finding.valueOf(previous(prior,key,"UNKNOWN")).ordinal());return s;
    }
    void death(CalculationSnapshot prior){
        a.clear("دیه نفس و بررسی تغلیظ","مبنای عادی و شرایط حقوقی را از مستندات پرونده وارد کنید");a.detailBack=()->center(related);
        final CalculationDeath.Rules rules;
        try(InputStreamReader r=new InputStreamReader(a.getAssets().open("calculation/death-1392-v1.properties"),StandardCharsets.UTF_8)){rules=new CalculationDeath.Rules(r);}
        catch(Exception e){a.toast(message(e));return;}
        EditText title=field("عنوان محاسبه",prior==null?"دیه نفس":prior.title);
        List<OfficeDb.CaseRecord> cases=a.db.cases(null,"همه",null);List<String> names=new ArrayList<>();names.add("مستقل از پرونده");for(OfficeDb.CaseRecord c:cases)names.add(c.title);
        Spinner casePick=pick("ارتباط محاسبه",names.toArray(new String[0]));Long selected=prior==null?(related==null?null:related.id):prior.caseId;
        for(int i=0;i<cases.size();i++)if(selected!=null&&cases.get(i).id==selected)casePick.setSelection(i+1);
        EditText percent=field("درصد مبنای دیه نفس عادی از نرخ کامل؛ بدون افزایش",previous(prior,"basePercent",""));
        EditText event=field("تاریخ رفتار مرتکب",previous(prior,"occurredDate",""));a.bindJalaliPicker(event);
        EditText death=field("تاریخ فوت",previous(prior,"deathDate",""));a.bindJalaliPicker(death);
        EditText valueDate=field("تاریخ پرداخت / ارزش‌گذاری انتخابی",previous(prior,"effectiveDate",JalaliDate.today().value()));a.bindJalaliPicker(valueDate);
        Spinner actMonth=finding("رفتار در ماه حرام واقع شده است؟",prior,"actMonth");
        Spinner deathMonth=finding("فوت در ماه حرام واقع شده است؟",prior,"deathMonth");
        Spinner actMecca=finding("رفتار در محدوده حرم مکه واقع شده است؟",prior,"actMecca");
        Spinner deathMecca=finding("فوت در محدوده حرم مکه واقع شده است؟",prior,"deathMecca");
        EditText basis=field("مستند مبنای عادی، زمان و مکان رفتار و فوت",previous(prior,"assessment",""));
        EditText dateBasis=field("مستند انتخاب تاریخ ارزش‌گذاری",previous(prior,"dateBasis",""));
        EditText reason=prior==null?null:field("علت تغییر نسبت به نسخه قبلی","");
        CheckBox scope=confirm("موضوع دیه نفس است؛ مبنای عادی و شرایط مواد ۵۵۵ تا ۵۵۷ را بررسی کرده‌ام و مبلغ ورودی شامل افزایش نیست");
        a.page.addView(a.info("مبنای محاسبه","افزایش فقط یک‌بار به میزان یک‌سوم مبنای عادی محاسبه می‌شود، حتی اگر هر دو سبب زمانی و مکانی احراز شوند. تاریخ شمسی به‌تنهایی ماه حرام را اثبات نمی‌کند؛ محرم، رجب، ذی‌قعده و ذی‌حجه و مرز مغرب شرعی باید مستند بررسی شوند. سایر اماکن متبرک مشمول نیستند. این مسیر سهم وراث، مسئولیت بیمه یا صندوق و مبنای ویژه اشخاص را تعیین نمی‌کند؛ جراحت و ارش در این مسیر نیستند."));
        button("محاسبه و نمایش نتیجه",()->{try{
            CalculationReference rate=bodyRate();String e=CalculationReference.date(event.getText().toString()),d=CalculationReference.date(death.getText().toString()),v=CalculationReference.date(valueDate.getText().toString());
            CalculationDeath.Finding am=CalculationDeath.Finding.values()[actMonth.getSelectedItemPosition()],dm=CalculationDeath.Finding.values()[deathMonth.getSelectedItemPosition()],ap=CalculationDeath.Finding.values()[actMecca.getSelectedItemPosition()],dp=CalculationDeath.Finding.values()[deathMecca.getSelectedItemPosition()];
            CalculationDeath.Result r=CalculationDeath.calculate(rules,rate,e,d,v,percent.getText().toString(),basis.getText().toString(),scope.isChecked(),am,dm,ap,dp);
            Long caseId=casePick.getSelectedItemPosition()==0?null:cases.get(casePick.getSelectedItemPosition()-1).id;
            Map<String,String> in=new LinkedHashMap<>(),out=new LinkedHashMap<>();
            in.put("title",title.getText().toString());in.put("caseId",caseId==null?"":caseId.toString());in.put("bodyMode","DEATH");in.put("basePercent",percent.getText().toString());
            in.put("occurredDate",e);in.put("deathDate",d);in.put("effectiveDate",v);in.put("actMonth",am.name());in.put("deathMonth",dm.name());in.put("actMecca",ap.name());in.put("deathMecca",dp.name());
            in.put("assessment",basis.getText().toString());in.put("dateBasis",CalculationReference.required(dateBasis.getText().toString(),"مستند ارزش‌گذاری"));in.put("sourceUrl",rules.sourceUrl);in.put("_rulePackSnapshot",rules.serialized);in.put("scopeConfirmed","دیه نفس و مبنای عادی بدون افزایش؛ بررسی مستندات توسط کاربر");
            out.put("مبنای عادی؛ کسر دقیق ریالی",r.base.toString());out.put("افزایش؛ کسر دقیق ریالی",r.additional.toString());out.put("تغلیظ",r.enhanced?"اعمال شد؛ یک‌سوم":"اعمال نشد؛ فقدان شرایط احراز شد");
            long total=r.total.roundedRials(RoundingMode.HALF_UP);out.put("مبلغ",CalculationArithmetic.display(total,CalculationArithmetic.Currency.RIAL));out.put("معادل تومان",CalculationArithmetic.display(total,CalculationArithmetic.Currency.TOMAN));
            List<String> steps=Arrays.asList("نرخ عادی × درصد مبنای مستند / ۱۰۰ = "+r.base,"افزایش = "+r.additional,"جمع دقیق = "+r.total+"؛ گرد کردن فقط در پایان");
            List<String> warnings=Arrays.asList("نتیجه مشروط به احراز قضایی مبنای عادی و زمان و مکان است؛ تعیین سهم وراث یا تعهد بیمه و صندوق نیست. متن تاریخی مواد ۵۵۵ تا ۵۵۷ بررسی شده؛ بررسی همه اصلاحات و موارد خاص تکمیل نشده است.",rate.sourceType);
            long now=Math.max(System.currentTimeMillis(),prior==null?0:prior.createdAt+1);List<CalculationSnapshot.Override> audit=new ArrayList<>();
            if(prior!=null){Set<String> keys=new LinkedHashSet<>(prior.inputSnapshot.keySet());keys.addAll(in.keySet());for(String key:keys){String old=prior.inputSnapshot.getOrDefault(key,""),n=in.getOrDefault(key,"");if(!old.equals(n))audit.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.INPUT,key,old,n,now,CalculationReference.required(reason.getText().toString(),"علت تغییر")));}}
            show(new CalculationSnapshot(UUID.randomUUID().toString(),prior==null?"":prior.id,title.getText().toString(),caseId,CalculationSnapshot.Type.DIYAH,CalculationDeath.ENGINE_VERSION,rules.version,rate.version,JalaliDate.today().value(),v,in,out,Collections.singletonList(rate),steps,warnings,audit,RoundingMode.HALF_UP,now,now),false);
        }catch(Exception e){a.toast(message(e));}});
    }
    void body(boolean arsh,CalculationSnapshot prior){
        a.clear(arsh?"محاسبات ارش اعلام‌شده":"محاسبات دیه","جدول محدود یا مقدار اعلام‌شده؛ تشخیص صدمه با مرجع صالح است");a.detailBack=()->center(related);
        EditText title=field("عنوان محاسبه",prior==null?(arsh?"ارش اعلام‌شده":"دیه اعلام‌شده"):prior.title);
        List<OfficeDb.CaseRecord> cases=a.db.cases(null,"همه",null);List<String> names=new ArrayList<>();names.add("مستقل از پرونده");for(OfficeDb.CaseRecord c:cases)names.add(c.title);
        Spinner casePick=pick("ارتباط محاسبه",names.toArray(new String[0]));Long selected=prior==null?(related==null?null:related.id):prior.caseId;
        for(int i=0;i<cases.size();i++)if(selected!=null&&cases.get(i).id==selected)casePick.setSelection(i+1);
        final CalculationPrescribedBody.Rules prescribedRules;
        try(InputStreamReader reader=new InputStreamReader(a.getAssets().open("calculation/prescribed-head-1392-v1.properties"),StandardCharsets.UTF_8)){prescribedRules=new CalculationPrescribedBody.Rules(reader);}
        catch(Exception e){a.toast(message(e));return;}
        Spinner mode=pick("نوع ورودی",arsh?new String[]{"درصد اعلام‌شده از دیه کامل عادی","مبلغ قطعی اعلام‌شده؛ بدون تعدیل خودکار"}:new String[]{"درصد اعلام‌شده از دیه کامل عادی","مبلغ قطعی اعلام‌شده؛ بدون تعدیل خودکار","جدول صدمات مستقل سر و صورت؛ ماده ۷۰۹","جمع درصدهای مستقل تعیین‌شده؛ با احراز عدم تداخل"});
        String previousMode=previous(prior,"bodyMode","PERCENT");mode.setSelection("INDEPENDENT_TOTAL".equals(previousMode)&&!arsh?3:"PRESCRIBED".equals(previousMode)&&!arsh?2:"AMOUNT".equals(previousMode)?1:0);
        final List<String> injuryIds=new ArrayList<>(prescribedRules.injuries.keySet());
        final Spinner injuryPick;
        if(!arsh){List<String> injuryLabels=new ArrayList<>();for(CalculationPrescribedBody.Injury i:prescribedRules.injuries.values())injuryLabels.add(i.label);
            injuryPick=pick("صدمه؛ فقط برای مسیر جدول ماده ۷۰۹",injuryLabels.toArray(new String[0]));injuryPick.setSelection(Math.max(0,injuryIds.indexOf(previous(prior,"prescribedInjury","HARISA"))));}
        else injuryPick=null;
        EditText value=field("درصد یا مبلغ اعلام‌شده",previous(prior,"assessedValue",""));
        EditText independentRows=arsh?null:field("اقلام مستقل؛ هر سطر: شرح متمایز | درصد | مستند",previous(prior,"independentRows",""));
        if(independentRows!=null){independentRows.setSingleLine(false);independentRows.setMinLines(3);}
        CheckBox independentChecked=arsh?null:confirm("درصد هر قلم از دیه کامل عادی و استقلال آن در تصمیم مرجع صالح مشخص است؛ هیچ قلم تداخلی، سرایت‌یافته، تکراری یا تغلیظ‌شده در جمع نیست");
        Spinner currency=pick("واحد مبلغ؛ فقط مسیر مبلغ قطعی",new String[]{"ریال","تومان"});currency.setSelection("TOMAN".equals(previous(prior,"currency","RIAL"))?1:0);
        EditText injury=field("شرح صدمه و عضو یا منفعت",previous(prior,"injury",""));
        EditText occurred=field("تاریخ وقوع صدمه",previous(prior,"occurredDate",""));a.bindJalaliPicker(occurred);
        EditText effective=field("تاریخ پرداخت / ارزش‌گذاری انتخابی",previous(prior,"effectiveDate",JalaliDate.today().value()));a.bindJalaliPicker(effective);
        EditText basis=field("مرجع، شماره و تاریخ مستند تعیین مبلغ یا درصد",previous(prior,"assessment",""));
        EditText opinion=field("نظر کارشناسی؛ جدا از تصمیم مرجع",previous(prior,"expertOpinion",""));
        EditText dateBasis=field("مستند انتخاب تاریخ ارزش‌گذاری",previous(prior,"dateBasis",""));
        EditText reason=prior==null?null:field("علت تغییر نسبت به نسخه قبلی","");
        CheckBox checked=confirm("مقدار توسط مرجع صالح تعیین شده و مبنای درصد، دیه کامل عادی است؛ این ورودی یک صدمه مستقل و بدون تغلیظ است");
        CheckBox prescribedChecked=arsh?null:confirm("تشخیص مستند سر یا صورت و استقلال این صدمه بررسی شده؛ فاقد فوت، تداخل، آثار اضافه و حکم خاص است");
        a.page.addView(a.info("دامنه محاسبه",(arsh?CalculationSnapshot.ARSH_WARNING+"\n":"")+"جدول ماده ۷۰۹ فقط هفت عنوان حارصه تا منقله سر و صورت را پوشش می‌دهد. جراحات بدن، مأمومه، دامغه، آثار باقی‌مانده و موارد تداخل نیازمند مسیر دیگرند. نرم‌افزار تشخیص صدمه، مسئول پرداخت یا تغلیظ را تعیین نمی‌کند. جمع مستقل تنها برای درصدهای جداگانه تعیین‌شده و پس از احراز عدم تداخل در مستند قضایی است. درصد از دیه عضو، فوت با شرایط تغلیظ و استثناهای ارزش‌گذاری در این مسیر قابل محاسبه خودکار نیستند. سال فاقد نرخ جایگزین نمی‌شود."));
        button("محاسبه و نمایش نتیجه",()->{try{
            boolean prescribed=!arsh&&mode.getSelectedItemPosition()==2;
            boolean independent=!arsh&&mode.getSelectedItemPosition()==3;
            if(independent?!independentChecked.isChecked():prescribed?!prescribedChecked.isChecked():!checked.isChecked())throw new IllegalArgumentException("مستند و دامنه محاسبه را تأیید کنید");
            String date=CalculationReference.date(effective.getText().toString()),event=CalculationReference.date(occurred.getText().toString());
            if(date.compareTo(event)<0)throw new IllegalArgumentException("تاریخ ارزش‌گذاری پیش از وقوع صدمه است");
            String assessment=CalculationReference.required(basis.getText().toString(),"مستند تعیین مبلغ یا درصد");
            CalculationArithmetic.Currency unit=currency.getSelectedItemPosition()==0?CalculationArithmetic.Currency.RIAL:CalculationArithmetic.Currency.TOMAN;
            boolean percent=mode.getSelectedItemPosition()==0;String entered=value.getText().toString();
            List<CalculationReference> refs=new ArrayList<>();List<String> steps=new ArrayList<>(),warnings=new ArrayList<>();
            long money;String data="assessed-fixed-amount",exact;
            if(independent){CalculationReference rate=bodyRate();
                CalculationBodyMath.IndependentTotal result=CalculationBodyMath.independentPercentages(rate,date,independentRows.getText().toString(),assessment,independentChecked.isChecked());
                money=result.exact.roundedRials(RoundingMode.HALF_UP);exact=result.exact.toString();refs.add(rate);data=rate.version;
                steps.addAll(result.steps);warnings.add("جمع صرفاً بر مبنای تصمیم مستند مرجع درباره استقلال اقلام است؛ نرم‌افزار تداخل موضوع مواد ۵۳۸ تا ۵۴۸، تفاوت دیه و تعهد صندوق یا آثار سرایت را تشخیص نمی‌دهد.");
            }else if(prescribed){CalculationReference rate=bodyRate();String id=injuryIds.get(injuryPick.getSelectedItemPosition());
                CalculationArithmetic.Fraction result=CalculationPrescribedBody.calculate(prescribedRules,id,rate,date,assessment,true);
                money=result.roundedRials(RoundingMode.HALF_UP);exact=result.toString();refs.add(rate);data=rate.version+" / "+prescribedRules.version;
                steps.add(prescribedRules.injuries.get(id).label+"؛ ماده ۷۰۹: "+rate.value+" × "+prescribedRules.injuries.get(id).ratio+" = "+exact+" ریال");
                warnings.add("نسبت از متن تاریخی مجلس، با تشخیص مستند و تأیید انطباق توسط کاربر؛ جراحات بدن و موارد خاص در این جدول نیستند. چند عمق یک زخم یا آثار اضافه را مستقل جمع نکنید.");
            }else if(percent){CalculationReference rate=bodyRate();CalculationArithmetic.Fraction result=CalculationBodyMath.assessedPercent(rate,date,entered,assessment);
                money=result.roundedRials(RoundingMode.HALF_UP);exact=result.toString();refs.add(rate);data=rate.version;
                steps.add(rate.value+" × "+entered+" / 100 = "+exact+" ریال؛ گرد کردن فقط در پایان");warnings.add(rate.sourceType);
            }else{money=CalculationBodyMath.assessedAmount(entered,unit,assessment);exact=money+"/1";steps.add("تبدیل دقیق مبلغ اعلام‌شده به ریال؛ بدون تعدیل سالانه یا تغلیظ");}
            Map<String,String> inputs=new LinkedHashMap<>(),results=new LinkedHashMap<>();
            Long caseId=casePick.getSelectedItemPosition()==0?null:cases.get(casePick.getSelectedItemPosition()-1).id;
            inputs.put("title",title.getText().toString());inputs.put("caseId",caseId==null?"":caseId.toString());inputs.put("bodyMode",independent?"INDEPENDENT_TOTAL":prescribed?"PRESCRIBED":percent?"PERCENT":"AMOUNT");
            if(independent){inputs.put("independentRows",independentRows.getText().toString());inputs.put("scopeConfirmed","درصدهای مستقل تعیین‌شده از دیه کامل عادی؛ عدم تداخل با مستند مرجع صالح");}
            if(prescribed){inputs.put("prescribedInjury",injuryIds.get(injuryPick.getSelectedItemPosition()));inputs.put("_rulePackSnapshot",prescribedRules.serialized);inputs.put("sourceTitle",prescribedRules.title);inputs.put("scopeConfirmed","صدمه مستقل سر یا صورت؛ فاقد عوامل خاص، با تأیید کاربر");}
            inputs.put("assessedValue",prescribed||independent?"":entered);inputs.put("currency",unit.name());inputs.put("injury",CalculationReference.required(injury.getText().toString(),"شرح صدمه"));
            inputs.put("occurredDate",event);inputs.put("effectiveDate",date);inputs.put("assessment",assessment);inputs.put("expertOpinion",opinion.getText().toString());
            inputs.put("dateBasis",CalculationReference.required(dateBasis.getText().toString(),"مستند تاریخ ارزش‌گذاری"));
            inputs.put("sourceUrl","https://natlex.ilo.org/dyn/natlex2/natlex2/files/download/103202/penal%20code.pdf");
            results.put("کسر دقیق ریالی",exact);results.put("مبلغ",CalculationArithmetic.display(money,CalculationArithmetic.Currency.RIAL));results.put("معادل تومان",CalculationArithmetic.display(money,CalculationArithmetic.Currency.TOMAN));
            warnings.add("تبدیل عددی مقدار اعلام‌شده با مسئولیت بررسی مستندات پرونده؛ نه تعیین استحقاق، درصد، تغلیظ، تداخل یا مبلغ قابل وصول. بررسی همه اصلاحات و استثناها تکمیل نشده است.");
            long now=Math.max(System.currentTimeMillis(),prior==null?0:prior.createdAt+1);List<CalculationSnapshot.Override> audit=new ArrayList<>();
            if(prior!=null){Set<String> keys=new LinkedHashSet<>(prior.inputSnapshot.keySet());keys.addAll(inputs.keySet());for(String key:keys){String old=prior.inputSnapshot.getOrDefault(key,""),n=inputs.getOrDefault(key,"");if(!old.equals(n))audit.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.INPUT,key,old,n,now,CalculationReference.required(reason.getText().toString(),"علت تغییر")));}}
            show(new CalculationSnapshot(UUID.randomUUID().toString(),prior==null?"":prior.id,title.getText().toString(),caseId,arsh?CalculationSnapshot.Type.ARSH_ESTIMATE:CalculationSnapshot.Type.DIYAH,
                prescribed?CalculationPrescribedBody.ENGINE_VERSION:CalculationBodyMath.ENGINE_VERSION,prescribed?prescribedRules.version:independent?"assessed-independent-awards/1":"assessed-input-conversion/1",data,JalaliDate.today().value(),date,inputs,results,refs,steps,warnings,audit,RoundingMode.HALF_UP,now,now),false);
        }catch(Exception e){a.toast(message(e));}});
    }

    void fee(boolean tariff,CalculationSnapshot prior){
        a.clear(tariff?"حق‌الوکاله تعرفه‌ای":"حق‌الوکاله توافقی","ورودی‌ها را بررسی کنید؛ ثبت محاسبه مبلغ پرونده یا قرارداد را تغییر نمی‌دهد");
        a.detailBack=()->center(related);
        final CalculationTariff.Rules rules;
        try(InputStreamReader reader=new InputStreamReader(a.getAssets().open("calculation/tariff-1398-reviewed-v6.properties"),StandardCharsets.UTF_8)){
            rules=new CalculationTariff.Rules(reader);
        }catch(Exception e){a.toast(message(e));return;}
        EditText title=field("عنوان محاسبه",prior==null?(tariff?"محاسبه تعرفه":"حق‌الوکاله توافقی"):prior.title);
        List<OfficeDb.CaseRecord> cases=a.db.cases(null,"همه",null);
        List<String> names=new ArrayList<>();names.add("مستقل از پرونده");for(OfficeDb.CaseRecord c:cases)names.add(c.title);
        Spinner casePick=pick("ارتباط محاسبه",names.toArray(new String[0]));
        Long selected=prior!=null?prior.caseId:related==null?null:related.id;
        for(int i=0;i<cases.size();i++)if(selected!=null&&cases.get(i).id==selected)casePick.setSelection(i+1);
        Spinner currency=pick("واحد ورود مبلغ",new String[]{"ریال","تومان"});
        currency.setSelection("TOMAN".equals(previous(prior,"currency","RIAL"))?1:0);
        String suggestion=!tariff&&related!=null?Long.toString(related.feeAgreed):"";
        EditText amount=field(tariff?"بهای خواسته مالی یا مبلغ منتخب کل تعرفه در سایر دسته‌ها":"مبلغ توافقی",previous(prior,"amount",suggestion));
        if(!tariff&&prior==null&&related!=null)a.page.addView(a.info("پیشنهاد از پرونده","مبلغ فعلی پرونده به ریال پیشنهاد شده؛ پیش از ثبت آن را بررسی کنید."));
        EditText effective=field("تاریخ مبنای محاسبه",previous(prior,"effectiveDate",JalaliDate.today().value()));a.bindJalaliPicker(effective);
        EditText basis=field("توضیح مستند انتخاب مبلغ و مبنا",previous(prior,"basis",""));
        EditText reason=prior==null?null:field("علت تغییر نسبت به نسخه قبلی","");
        final List<String> ids=new ArrayList<>();final Spinner category,stage,disposition,special;final CheckBox supported,prosecutor,finalTrial;
        if(tariff){
            ids.add("FINANCIAL");ids.add("CIVIL_ENFORCEMENT");ids.add("FINANCIAL_FINAL");ids.addAll(rules.ranges.keySet());List<String> labels=new ArrayList<>();labels.add("مالی؛ پلکانی ماده ۹، غیرقطعی از حیث بها");labels.add("اجرای احکام حقوقی و اسناد رسمی؛ ماده ۲۵");labels.add("مالی؛ حکم بدوی قطعی از حیث بها، صدر ماده ۹");
            for(CalculationTariff.Range r:rules.ranges.values())labels.add(r.label);
            ids.add("REPLACEMENT_AFTER_REVERSAL");labels.add("وکیل جدید پس از نقض رأی؛ ماده ۱۸");
            category=pick("دسته تعرفه",labels.toArray(new String[0]));
            int index=ids.indexOf(previous(prior,"category","FINANCIAL"));category.setSelection(Math.max(0,index));
            stage=pick("مرحله",new String[]{"کل","بدوی","تجدیدنظر","فرجام حقوقی","دادسرا"});
            String oldStage=previous(prior,"stage","WHOLE");
            for(int i=0;i<CalculationTariff.Stage.values().length;i++)if(CalculationTariff.Stage.values()[i].name().equals(oldStage))stage.setSelection(i);
            disposition=pick("نتیجه دادرسی؛ قرارهای مشخص ماده ۱۲",new String[]{"عادی؛ بدون قرار خاص","ابطال دادخواست پیش از دفاع؛ بدوی","رد دادخواست پس از دفاع؛ بدوی","سقوط دعوای تجدیدنظر پیش از دفاع","سقوط دعوای تجدیدنظر پس از دفاع","عدم استماع، رد دعوای مشمول یا رد اعاده دادرسی؛ بند پ ماده ۱۲"});
            String oldDisposition=previous(prior,"disposition","ORDINARY");
            for(int i=0;i<CalculationTariff.Disposition.values().length;i++)if(CalculationTariff.Disposition.values()[i].name().equals(oldDisposition))disposition.setSelection(i);
            special=pick("حالت ویژه تعرفه",new String[]{"عادی؛ بدون حالت ویژه","سازش یا داوری منتهی به رأی؛ کل دعوا","سهم مساوی هر وکیل؛ بدون قرارداد و توافق متفاوت","چند اتهام؛ مبلغ مبنا برای جرم اشد","تخصص؛ گواهی کانون یا مرکز در حدود صلاحیت","وکالت تسخیری یا معاضدتی؛ دو برابر حداقل مصرح","ادامه وکالت کیفری پس از نقض؛ تبصره ۲ ماده ۱۴"});
            String oldSpecial=previous(prior,"tariffSpecial","NONE");
            for(int i=0;i<CalculationTariff.Special.values().length;i++)if(CalculationTariff.Special.values()[i].name().equals(oldSpecial))special.setSelection(i);
            prosecutor=confirm("در مسیر کیفری، پرونده مرحله دادسرا دارد");prosecutor.setChecked(Boolean.parseBoolean(previous(prior,"hasProsecutor","true")));
            finalTrial=confirm("در مسیر کیفری، رأی بدوی قطعی است");finalTrial.setChecked(Boolean.parseBoolean(previous(prior,"finalTrial","false")));
            supported=confirm("انطباق این دسته و نسخه تعرفه را بررسی کرده‌ام؛ این محاسبه فاقد عوامل ویژه پشتیبانی‌نشده است");
            a.page.addView(a.info("دامنه محاسبه","در مسیر حکم قطعی از حیث بها، مستند قطعیت قانونی را وارد کنید؛ عدم اعتراض یا انقضای مهلت، قطعیت از حیث بها نیست. برای وکیل جدید پس از نقض، دسته ماده ۱۸ را انتخاب کنید و مبلغ تعرفه‌ای مستند پیش از نقض، مشخصات رأی نقض‌شده و قبول وکالت جدید را بنویسید؛ مبلغ قرارداد سابق یا کل مراحل خودکار مبنا نیست. ادامه وکالت همان وکیل در مرحله جدید کیفری مسیر جداگانه تبصره ۲ ماده ۱۴ است؛ نتیجه فقط مبلغ مرحله جدید است. در وکالت تسخیری/معاضدتی، حداقل مصرح دسته مبناست و مبلغ انتخابی شما جایگزین آن نمی‌شود؛ معرفی‌نامه و شرایط مرحله باید بررسی شود. حالت‌های ویژه فهرست مستقل‌اند؛ ترکیب چند حالت ویژه با هم یا با قرار پشتیبانی نمی‌شود. برای سازش مرحله کل را انتخاب کنید. در تعدد وکلا نتیجه سهم هر وکیل است. قرارهای مشخص ماده ۱۲ از فهرست نتیجه دادرسی قابل انتخاب‌اند؛ مرحله باید منطبق باشد. در تعرفه بازه‌ای، مبلغ کل منتخب را با دلیل وارد کنید."));
            TextView bounds=a.txt("",12,a.MUTED);a.page.addView(bounds);
            category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                public void onNothingSelected(AdapterView<?> p){}
                public void onItemSelected(AdapterView<?> p,View v,int position,long id){
                    CalculationTariff.Range r=rules.ranges.get(ids.get(position));
                    bounds.setText("REPLACEMENT_AFTER_REVERSAL".equals(ids.get(position))?"در فیلد مبلغ، حق‌الوکاله تعرفه‌ای مستند پیش از نقض را وارد کنید؛ مرحله بدوی یا تجدیدنظر و مستند وکیل جدید لازم است. نصف مبلغ بدون تقسیم مجدد مراحل؛ حالت ویژه و قرار عادی، مقدار ۱.":"CIVIL_ENFORCEMENT".equals(ids.get(position))?"مبلغ منتخب حق‌الوکاله را در فیلد مبلغ و محکوم‌به را در فیلد جدا وارد کنید؛ حداقل ۴٬۰۰۰٬۰۰۰ ریال و سقف ۲٪ محکوم‌به. مرحله کل و مقدار خدمت ۱ لازم است.":"FINANCIAL_FINAL".equals(ids.get(position))?"بهای خواسته را وارد کنید؛ کل تعرفه صدر ماده ۹ بدون تقسیم ۶۰/۴۰. فقط مرحله کل یا بدوی و بدون ترکیب با حالت ویژه یا قرار. مستند قطعیت الزامی است.":r==null?"بهای خواسته را وارد کنید؛ نرخ هر پله جدا اعمال می‌شود.":
                        "حدود کل تعرفه: "+(r.minimum==null?"حداقل تعیین نشده":CalculationArithmetic.display(r.minimum,CalculationArithmetic.Currency.RIAL))+
                        " تا "+CalculationArithmetic.display(r.maximum,CalculationArithmetic.Currency.RIAL)+"؛ ماده "+r.article);
                }
            });
        }else{category=null;stage=null;disposition=null;special=null;supported=null;prosecutor=null;finalTrial=null;}
        EditText specialCount=tariff?field("تعداد وکلا یا اتهام‌ها؛ در سایر حالت‌ها ۱",previous(prior,"specialCount","1")):null;
        EditText specialBasis=tariff?field("مستند شرایط حالت ویژه",previous(prior,"specialBasis","")):null;
        EditText award=tariff?field("محکوم‌به یا مورد اجرا؛ فقط دسته اجرای احکام، با واحد انتخابی",previous(prior,"enforcementAward","")):null;
        EditText quantity=tariff?field("ساعت مشاوره؛ در سایر دسته‌ها عدد ۱",previous(prior,"quantity","1")):null;
        button("محاسبه و نمایش نتیجه",()->{
            try{
                if(tariff&&!supported.isChecked())throw new IllegalArgumentException("انطباق دامنه تعرفه را تأیید کنید");
                CalculationArithmetic.Currency unit=currency.getSelectedItemPosition()==0?CalculationArithmetic.Currency.RIAL:CalculationArithmetic.Currency.TOMAN;
                boolean appointed=tariff&&CalculationTariff.Special.values()[special.getSelectedItemPosition()]==CalculationTariff.Special.APPOINTED_AID;
                long money=appointed?0:CalculationTariff.agreedFee(amount.getText().toString(),unit);
                String date=CalculationReference.date(effective.getText().toString());
                String explanation=CalculationReference.required(basis.getText().toString(),"مستند انتخاب مبنا");
                Long caseId=casePick.getSelectedItemPosition()==0?null:cases.get(casePick.getSelectedItemPosition()-1).id;
                Map<String,String> inputs=new LinkedHashMap<>(),results=new LinkedHashMap<>();
                inputs.put("title",title.getText().toString());inputs.put("caseId",caseId==null?"":caseId.toString());
                inputs.put("amount",amount.getText().toString());inputs.put("currency",unit.name());inputs.put("effectiveDate",date);inputs.put("basis",explanation);
                List<String> steps=new ArrayList<>(),warnings=new ArrayList<>();String engine="agreed-fee/1",legal="user-contract",data="user-input";
                if(tariff){
                    if(date.compareTo(rules.adoptionDate)<0)throw new IllegalArgumentException("تاریخ مبنا پیش از تصویب این نسخه تعرفه است");
                    String id=ids.get(category.getSelectedItemPosition());CalculationTariff.Stage stageValue=CalculationTariff.Stage.values()[stage.getSelectedItemPosition()];
                    if(("FINANCIAL".equals(id)||"FINANCIAL_FINAL".equals(id)||"CIVIL_ENFORCEMENT".equals(id)||"REPLACEMENT_AFTER_REVERSAL".equals(id))&&CalculationArithmetic.decimal(quantity.getText().toString()).compareTo(java.math.BigDecimal.ONE)!=0)
                        throw new IllegalArgumentException("در دسته مالی و اجرای احکام مقدار خدمت باید ۱ باشد");
                    if(appointed) {
                        CalculationTariff.Range selectedRange=rules.ranges.get(id);
                        if(selectedRange==null||selectedRange.minimum==null||selectedRange.family==CalculationTariff.Family.SERVICE)
                            throw new IllegalArgumentException("این مسیر نیازمند دسته دعوای دارای حداقل مصرح است");
                        money=selectedRange.minimum;
                        inputs.put("amount",Long.toString(money));inputs.put("currency","RIAL");
                        inputs.put("minimumBasis","حداقل مصرح دسته به ریال؛ مبلغ ورودی آزاد در این مسیر اعمال نشده است");
                    }
                    inputs.put("category",id);inputs.put("stage",stageValue.name());inputs.put("quantity",quantity.getText().toString());
                    boolean criminal=rules.ranges.containsKey(id)&&rules.ranges.get(id).family==CalculationTariff.Family.CRIMINAL;
                    boolean hasProsecutor=!criminal||prosecutor.isChecked(),isFinal=criminal&&finalTrial.isChecked();
                    inputs.put("hasProsecutor",Boolean.toString(hasProsecutor));inputs.put("finalTrial",Boolean.toString(isFinal));
                    inputs.put("sourceUrl",rules.sourceUrl);inputs.put("sourceTitle",rules.sourceTitle);inputs.put("rulePack",rules.version);
                    inputs.put("sourceAdoptionDate",rules.adoptionDate);inputs.put("sourceReviewDate",rules.reviewDate);
                    inputs.put("_rulePackSnapshot",rules.serializedRules);
                    if("CIVIL_ENFORCEMENT".equals(id))inputs.put("enforcementAward",award.getText().toString());
                    CalculationTariff.Result r="REPLACEMENT_AFTER_REVERSAL".equals(id)?CalculationTariff.replacementAfterReversal(rules,money,stageValue,explanation,specialBasis.getText().toString(),RoundingMode.HALF_UP):"CIVIL_ENFORCEMENT".equals(id)?CalculationTariff.enforcement(rules,CalculationArithmetic.money(award.getText().toString(),unit),money,stageValue,explanation,RoundingMode.HALF_UP):("FINANCIAL".equals(id)||"FINANCIAL_FINAL".equals(id))?CalculationTariff.financial(rules,money,stageValue,explanation,"FINANCIAL_FINAL".equals(id),RoundingMode.HALF_UP):
                        CalculationTariff.ranged(rules,id,money,quantity.getText().toString(),stageValue,hasProsecutor,isFinal,explanation,RoundingMode.HALF_UP);
                    CalculationTariff.Disposition dispositionValue=CalculationTariff.Disposition.values()[disposition.getSelectedItemPosition()];
                    if("FINANCIAL_FINAL".equals(id)) {
                        inputs.put("finalityBasis",explanation);
                        inputs.put("opinionStatus","REVIEWED_PUBLICATION");
                        inputs.put("opinionSource",rules.finalOpinionUrl);
                    }
                    inputs.put("disposition",dispositionValue.name());
                    r=CalculationTariff.disposition(rules,r,id,dispositionValue);
                    CalculationTariff.Special specialValue=CalculationTariff.Special.values()[special.getSelectedItemPosition()];
                    int count=CalculationArithmetic.decimal(specialCount.getText().toString()).intValueExact();
                    String proof=specialBasis.getText().toString();
                    r=CalculationTariff.special(rules,r,id,specialValue,count,proof,dispositionValue);
                    inputs.put("tariffSpecial",specialValue.name());inputs.put("specialCount",Integer.toString(count));inputs.put("specialBasis",proof);
                    inputs.put("resultScope","REPLACEMENT_AFTER_REVERSAL".equals(id)?"حق‌الوکاله وکیل جدید پس از نقض":specialValue==CalculationTariff.Special.CONTINUED_AFTER_REVERSAL?"فقط حق‌الوکاله مرحله جدید":specialValue==CalculationTariff.Special.EQUAL_COUNSEL?"سهم هر وکیل":"حق‌الوکاله مسیر انتخابی");
                    money=r.rials;steps.addAll(r.steps);results.put("کسر دقیق ریالی",r.exactRials);
                    warnings.add("بر پایه متن بررسی‌شده تعرفه مصوب ۱۳۹۸ و فرض انتخابی؛ بررسی همه تغییرات بعدی و موارد خاص تکمیل نشده است. این نتیجه حکم دادگاه یا تعیین مبلغ قابل وصول از طرف مقابل نیست.");
                    engine=CalculationTariff.ENGINE_VERSION;legal=rules.version;data=rules.version;
                }else{steps.add("ثبت مبلغ توافقی مستقل با تبدیل دقیق واحد به ریال");warnings.add("این مبلغ توافقی است؛ تعرفه یا محکوم‌به قابل وصول از طرف مقابل محسوب نمی‌شود.");}
                results.put("مبلغ",CalculationArithmetic.display(money,CalculationArithmetic.Currency.RIAL));
                results.put("معادل تومان",CalculationArithmetic.display(money,CalculationArithmetic.Currency.TOMAN));
                long now=Math.max(System.currentTimeMillis(),prior==null?0:prior.createdAt+1);
                List<CalculationSnapshot.Override> audit=new ArrayList<>();
                if(prior!=null){Set<String> keys=new LinkedHashSet<>(prior.inputSnapshot.keySet());keys.addAll(inputs.keySet());
                    for(String key:keys){String old=prior.inputSnapshot.getOrDefault(key,""),value=inputs.getOrDefault(key,"");if(!old.equals(value))
                        audit.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.INPUT,key,old,value,now,CalculationReference.required(reason.getText().toString(),"علت تغییر")));}}
                CalculationSnapshot snapshot=new CalculationSnapshot(UUID.randomUUID().toString(),prior==null?"":prior.id,title.getText().toString(),caseId,
                    tariff?CalculationSnapshot.Type.TARIFF_FEE:CalculationSnapshot.Type.AGREED_FEE,engine,legal,data,JalaliDate.today().value(),date,
                    inputs,results,Collections.emptyList(),steps,warnings,audit,RoundingMode.HALF_UP,now,now);
                show(snapshot,false);
            }catch(Exception e){a.toast(message(e));}
        });
    }
    void history(){
        a.clear("تاریخچه محاسبات",related==null?"همه محاسبات ثبت‌شده":"پرونده: "+related.title);a.detailBack=()->center(related);
        try{List<CalculationSnapshot> list=repo.list(related==null?null:related.id);
            if(list.isEmpty())a.page.addView(a.empty("محاسبه‌ای ثبت نشده است."));
            for(CalculationSnapshot s:list)button(s.title+" · "+s.calculationDate+" · "+CalculationReport.type(s.type),()->show(s,true));
        }catch(Exception e){a.toast(message(e));}
    }
    void show(CalculationSnapshot s,boolean saved){
        a.clear(s.title,saved?"نتیجه ثبت‌شده؛ مبناهای تاریخی حفظ می‌شوند":"پیش‌نمایش؛ هنوز ذخیره نشده است");a.detailBack=this::history;
        String report=CalculationReport.text(s);a.page.addView(a.info("جزئیات محاسبه",report));
        if(!saved)button("ذخیره محاسبه",()->{try{repo.save(s);show(s,true);}catch(Exception e){a.toast(message(e));}});
        button("PDF / چاپ",()->a.preview(s.title,report));
        button("اشتراک متن نتیجه",()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report);a.startActivity(Intent.createChooser(i,"اشتراک محاسبه"));});
        if(saved&&(s.type==CalculationSnapshot.Type.TARIFF_FEE||s.type==CalculationSnapshot.Type.AGREED_FEE))
            button("نسخه جدید / محاسبه مجدد",()->fee(s.type==CalculationSnapshot.Type.TARIFF_FEE,s));
        if(saved&&(s.type==CalculationSnapshot.Type.ORDINARY_DEBT_DELAY||s.type==CalculationSnapshot.Type.CHEQUE_DELAY))
            button("نسخه جدید / محاسبه مجدد",()->delay(s));
        if(saved&&(s.type==CalculationSnapshot.Type.DIYAH||s.type==CalculationSnapshot.Type.ARSH_ESTIMATE))
            button("نسخه جدید / محاسبه مجدد",()->{if("DEATH".equals(s.inputSnapshot.get("bodyMode")))death(s);else body(s.type==CalculationSnapshot.Type.ARSH_ESTIMATE,s);});
    }
    private static String message(Exception e){return e.getMessage()==null?"محاسبه قابل انجام نیست":e.getMessage();}
}
