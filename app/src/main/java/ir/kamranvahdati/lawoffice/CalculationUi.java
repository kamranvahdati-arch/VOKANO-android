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
        button("دیه",()->body(false,null));button("ارش",()->body(true,null));
        button("تاریخچه محاسبات",this::history);
        button("منابع محاسبات و وضعیت به‌روزرسانی",this::sources);
        a.page.addView(a.info("دامنه ابزارها","تعرفه فروض مشخص متن مصوب ۱۳۹۸ را پوشش می‌دهد؛ دیه و ارش تبدیل عددی مبلغ یا درصد اعلام‌شده‌اند. تعیین خودکار صدمه و همه موارد خاص هنوز ارائه نشده است."));
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
        a.page.addView(a.info("شاخص‌های تأخیر تأدیه","پوشش: ۱۳۹۹ تا ۱۴۰۱؛ نسخه "+CalculationDelayPack.VERSION+"\nمنبع تصویر: "+CalculationDelayPack.URL+"\nشاخص جدیدتر یا متعارض در این نسخه جایگزین نمی‌شود."));
        a.page.addView(a.info("به‌روزرسانی مرکزی","پس از آماده شدن سرور، انتشار نرخ‌ها و شاخص‌ها از پنل مدیر انجام خواهد شد. این نسخه اتصال زنده یا به‌روزرسانی خودکار ندارد. ورود دستی نرخ مرجع برای کاربران عمومی ارائه نمی‌شود؛ سابقه داده‌های قبلی حذف نشده است."));
    }
    private CalculationDelayPack delayPack()throws Exception{
        try(InputStreamReader reader=new InputStreamReader(a.getAssets().open("calculation/delay-indices-1399-1401.properties"),StandardCharsets.UTF_8)){
            return new CalculationDelayPack(reader);
        }
    }
    void delay(CalculationSnapshot prior){
        a.clear("خسارت تأخیر تأدیه","پوشش شاخص فعلی: ۱۳۹۹ تا ۱۴۰۱؛ محاسبه مشروط به بررسی حقوقی پرونده");a.detailBack=()->center(related);
        EditText title=field("عنوان محاسبه",prior==null?"خسارت تأخیر تأدیه":prior.title);
        List<OfficeDb.CaseRecord> cases=a.db.cases(null,"همه",null);List<String> names=new ArrayList<>();names.add("مستقل از پرونده");for(OfficeDb.CaseRecord c:cases)names.add(c.title);
        Spinner casePick=pick("ارتباط محاسبه",names.toArray(new String[0]));Long selected=prior==null?(related==null?null:related.id):prior.caseId;
        for(int i=0;i<cases.size();i++)if(selected!=null&&cases.get(i).id==selected)casePick.setSelection(i+1);
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
        CheckBox simple=confirm("پرونده فاقد پرداخت جزئی، اقساط، اعسار، ورشکستگی، وجه التزام یا استثنای مؤثر دیگر است");
        a.page.addView(a.info("دامنه و منابع","این فرم استحقاق یا مبدأ را خودکار تعیین نمی‌کند. ماده ۵۲۲، قاعده عمومی رأی ۸۱۲ و فرمول رأی ۸۵۰ مبنای بررسی‌اند. موارد خاص و شاخص ماه‌های خارج از ۱۳۹۹ تا ۱۴۰۱ پشتیبانی نمی‌شوند. اصل دین در شاخص پایان تقسیم بر شاخص مبدأ ضرب می‌شود؛ سود مرکب و محاسبه روزشمار اعمال نمی‌شود."));
        button("محاسبه و نمایش نتیجه",()->{try{
            CalculationDelayMath.Mode selectedMode=mode.getSelectedItemPosition()==0?CalculationDelayMath.Mode.ORDINARY_DEBT:CalculationDelayMath.Mode.CHEQUE;
            String d=CalculationReference.date(due.getText().toString()),s=CalculationReference.date(start.getText().toString()),e=CalculationReference.date(end.getText().toString());
            String demandValue=demand.getText().toString().trim();if(!demandValue.isEmpty())demandValue=CalculationReference.date(demandValue);
            String explanation=CalculationDelayPack.validateScope(selectedMode,d,demandValue,s,e,basis.getText().toString(),money.isChecked(),ordinary.isChecked(),cheque.isChecked(),simple.isChecked());
            CalculationArithmetic.Currency unit=currency.getSelectedItemPosition()==0?CalculationArithmetic.Currency.RIAL:CalculationArithmetic.Currency.TOMAN;
            long principal=CalculationArithmetic.money(amount.getText().toString(),unit);CalculationDelayPack pack=delayPack();
            CalculationReference first=pack.at(s),last=pack.at(e);
            CalculationDelayMath.Result r=CalculationDelayMath.calculate(selectedMode,principal,s,e,explanation,CalculationDelayPack.SERIES,first,last,RoundingMode.HALF_UP);
            Long caseId=casePick.getSelectedItemPosition()==0?null:cases.get(casePick.getSelectedItemPosition()-1).id;
            Map<String,String> inputs=new LinkedHashMap<>(),results=new LinkedHashMap<>();
            inputs.put("title",title.getText().toString());inputs.put("caseId",caseId==null?"":caseId.toString());inputs.put("amount",amount.getText().toString());inputs.put("currency",unit.name());
            inputs.put("dueDate",d);inputs.put("demandDate",demandValue);inputs.put("startDate",s);inputs.put("effectiveDate",e);inputs.put("basis",explanation);
            inputs.put("delayMode",selectedMode.name());inputs.put("scopeConfirmed","وجه رایج و اصل دین؛ بدون موارد خاص؛ تأیید شرایط مسیر انتخابی توسط کاربر");
            inputs.put("sourceUrl","https://nezamat.ir/post-31264/ | https://dotic.ir/news/17037/ | https://guilanbar.ir/wp-content/uploads/2021/07/images_1398_09_812.jpeg.webp");
            results.put("اصل دین",CalculationArithmetic.display(principal,CalculationArithmetic.Currency.RIAL));
            results.put("خسارت تأخیر",CalculationArithmetic.display(r.damagesRials,CalculationArithmetic.Currency.RIAL));
            results.put("اصل و خسارت",CalculationArithmetic.display(r.adjustedRials,CalculationArithmetic.Currency.RIAL));
            results.put("معادل تومان اصل و خسارت",CalculationArithmetic.display(r.adjustedRials,CalculationArithmetic.Currency.TOMAN));results.put("کسر دقیق ریالی",r.exactAdjustedRials);
            List<String> steps=Arrays.asList(principal+" × "+last.value+" ÷ "+first.value+" = "+r.exactAdjustedRials+" ریال", "خسارت = مبلغ تعدیل‌شده منهای اصل دین؛ گرد کردن فقط در پایان");
            List<String> warnings=Arrays.asList("نتیجه مشروط به صحت بررسی حقوقی کاربر است؛ تعیین استحقاق و مبلغ قابل وصول بر عهده مرجع صالح است. موارد خاص در این مسیر پشتیبانی نمی‌شوند.","شاخص‌ها از تصویر بازنشرشده جدول بانک مرکزی تطبیق داده شده‌اند؛ پوشش فقط ۱۳۹۹ تا ۱۴۰۱ است. اتصال به‌روزرسانی زنده وجود ندارد.");
            long now=Math.max(System.currentTimeMillis(),prior==null?0:prior.createdAt+1);List<CalculationSnapshot.Override> audit=new ArrayList<>();
            if(prior!=null){String why=CalculationReference.required(reason.getText().toString(),"علت تغییر");Set<String> keys=new LinkedHashSet<>(prior.inputSnapshot.keySet());keys.addAll(inputs.keySet());for(String key:keys){String old=prior.inputSnapshot.getOrDefault(key,""),value=inputs.getOrDefault(key,"");if(!old.equals(value))audit.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.INPUT,key,old,value,now,why));}}
            List<CalculationReference> refs=first.id.equals(last.id)?Collections.singletonList(first):Arrays.asList(first,last);
            show(new CalculationSnapshot(UUID.randomUUID().toString(),prior==null?"":prior.id,title.getText().toString(),caseId,
                selectedMode==CalculationDelayMath.Mode.ORDINARY_DEBT?CalculationSnapshot.Type.ORDINARY_DEBT_DELAY:CalculationSnapshot.Type.CHEQUE_DELAY,
                CalculationDelayMath.ENGINE_VERSION,"reviewed-simple-delay-522-812-850/1",CalculationDelayPack.VERSION,JalaliDate.today().value(),e,
                inputs,results,refs,steps,warnings,audit,RoundingMode.HALF_UP,now,now),false);
        }catch(Exception e){a.toast(message(e));}});
    }
    void body(boolean arsh,CalculationSnapshot prior){
        a.clear(arsh?"محاسبات ارش اعلام‌شده":"محاسبات دیه اعلام‌شده","تبدیل عددی یک مبلغ یا درصد تعیین‌شده؛ نه تشخیص صدمه یا صدور حکم");a.detailBack=()->center(related);
        EditText title=field("عنوان محاسبه",prior==null?(arsh?"ارش اعلام‌شده":"دیه اعلام‌شده"):prior.title);
        List<OfficeDb.CaseRecord> cases=a.db.cases(null,"همه",null);List<String> names=new ArrayList<>();names.add("مستقل از پرونده");for(OfficeDb.CaseRecord c:cases)names.add(c.title);
        Spinner casePick=pick("ارتباط محاسبه",names.toArray(new String[0]));Long selected=prior==null?(related==null?null:related.id):prior.caseId;
        for(int i=0;i<cases.size();i++)if(selected!=null&&cases.get(i).id==selected)casePick.setSelection(i+1);
        Spinner mode=pick("نوع ورودی",new String[]{"درصد اعلام‌شده از دیه کامل عادی","مبلغ قطعی اعلام‌شده؛ بدون تعدیل خودکار"});mode.setSelection("AMOUNT".equals(previous(prior,"bodyMode","PERCENT"))?1:0);
        EditText value=field("درصد یا مبلغ اعلام‌شده",previous(prior,"assessedValue",""));
        Spinner currency=pick("واحد مبلغ؛ فقط مسیر مبلغ قطعی",new String[]{"ریال","تومان"});currency.setSelection("TOMAN".equals(previous(prior,"currency","RIAL"))?1:0);
        EditText injury=field("شرح صدمه و عضو یا منفعت",previous(prior,"injury",""));
        EditText occurred=field("تاریخ وقوع صدمه",previous(prior,"occurredDate",""));a.bindJalaliPicker(occurred);
        EditText effective=field("تاریخ پرداخت / ارزش‌گذاری انتخابی",previous(prior,"effectiveDate",JalaliDate.today().value()));a.bindJalaliPicker(effective);
        EditText basis=field("مرجع، شماره و تاریخ مستند تعیین مبلغ یا درصد",previous(prior,"assessment",""));
        EditText opinion=field("نظر کارشناسی؛ جدا از تصمیم مرجع",previous(prior,"expertOpinion",""));
        EditText dateBasis=field("مستند انتخاب تاریخ ارزش‌گذاری",previous(prior,"dateBasis",""));
        EditText reason=prior==null?null:field("علت تغییر نسبت به نسخه قبلی","");
        CheckBox checked=confirm("مقدار توسط مرجع صالح تعیین شده و مبنای درصد، دیه کامل عادی است؛ این ورودی یک صدمه مستقل و بدون تغلیظ است");
        a.page.addView(a.info("دامنه محاسبه",(arsh?CalculationSnapshot.ARSH_WARNING+"\n":"")+"نرم‌افزار درصد صدمه، تداخل دیات، مسئول پرداخت یا تغلیظ را تعیین نمی‌کند. درصد از دیه عضو، تعدد صدمات، فوت با شرایط تغلیظ و استثناهای ارزش‌گذاری در این مسیر قابل محاسبه خودکار نیستند. سال فاقد نرخ جایگزین نمی‌شود."));
        button("محاسبه و نمایش نتیجه",()->{try{
            if(!checked.isChecked())throw new IllegalArgumentException("مستند و دامنه محاسبه را تأیید کنید");
            String date=CalculationReference.date(effective.getText().toString()),event=CalculationReference.date(occurred.getText().toString());
            if(date.compareTo(event)<0)throw new IllegalArgumentException("تاریخ ارزش‌گذاری پیش از وقوع صدمه است");
            String assessment=CalculationReference.required(basis.getText().toString(),"مستند تعیین مبلغ یا درصد");
            CalculationArithmetic.Currency unit=currency.getSelectedItemPosition()==0?CalculationArithmetic.Currency.RIAL:CalculationArithmetic.Currency.TOMAN;
            boolean percent=mode.getSelectedItemPosition()==0;String entered=value.getText().toString();
            List<CalculationReference> refs=new ArrayList<>();List<String> steps=new ArrayList<>(),warnings=new ArrayList<>();
            long money;String data="assessed-fixed-amount",exact;
            if(percent){CalculationReference rate=bodyRate();CalculationArithmetic.Fraction result=CalculationBodyMath.assessedPercent(rate,date,entered,assessment);
                money=result.roundedRials(RoundingMode.HALF_UP);exact=result.toString();refs.add(rate);data=rate.version;
                steps.add(rate.value+" × "+entered+" / 100 = "+exact+" ریال؛ گرد کردن فقط در پایان");warnings.add(rate.sourceType);
            }else{money=CalculationBodyMath.assessedAmount(entered,unit,assessment);exact=money+"/1";steps.add("تبدیل دقیق مبلغ اعلام‌شده به ریال؛ بدون تعدیل سالانه یا تغلیظ");}
            Map<String,String> inputs=new LinkedHashMap<>(),results=new LinkedHashMap<>();
            Long caseId=casePick.getSelectedItemPosition()==0?null:cases.get(casePick.getSelectedItemPosition()-1).id;
            inputs.put("title",title.getText().toString());inputs.put("caseId",caseId==null?"":caseId.toString());inputs.put("bodyMode",percent?"PERCENT":"AMOUNT");
            inputs.put("assessedValue",entered);inputs.put("currency",unit.name());inputs.put("injury",CalculationReference.required(injury.getText().toString(),"شرح صدمه"));
            inputs.put("occurredDate",event);inputs.put("effectiveDate",date);inputs.put("assessment",assessment);inputs.put("expertOpinion",opinion.getText().toString());
            inputs.put("dateBasis",CalculationReference.required(dateBasis.getText().toString(),"مستند تاریخ ارزش‌گذاری"));
            inputs.put("sourceUrl","https://natlex.ilo.org/dyn/natlex2/natlex2/files/download/103202/penal%20code.pdf");
            results.put("کسر دقیق ریالی",exact);results.put("مبلغ",CalculationArithmetic.display(money,CalculationArithmetic.Currency.RIAL));results.put("معادل تومان",CalculationArithmetic.display(money,CalculationArithmetic.Currency.TOMAN));
            warnings.add("تبدیل عددی مقدار اعلام‌شده با مسئولیت بررسی مستندات پرونده؛ نه تعیین استحقاق، درصد، تغلیظ، تداخل یا مبلغ قابل وصول. بررسی همه اصلاحات و استثناها تکمیل نشده است.");
            long now=Math.max(System.currentTimeMillis(),prior==null?0:prior.createdAt+1);List<CalculationSnapshot.Override> audit=new ArrayList<>();
            if(prior!=null){Set<String> keys=new LinkedHashSet<>(prior.inputSnapshot.keySet());keys.addAll(inputs.keySet());for(String key:keys){String old=prior.inputSnapshot.getOrDefault(key,""),n=inputs.getOrDefault(key,"");if(!old.equals(n))audit.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.INPUT,key,old,n,now,CalculationReference.required(reason.getText().toString(),"علت تغییر")));}}
            show(new CalculationSnapshot(UUID.randomUUID().toString(),prior==null?"":prior.id,title.getText().toString(),caseId,arsh?CalculationSnapshot.Type.ARSH_ESTIMATE:CalculationSnapshot.Type.DIYAH,
                CalculationBodyMath.ENGINE_VERSION,"assessed-input-conversion/1",data,JalaliDate.today().value(),date,inputs,results,refs,steps,warnings,audit,RoundingMode.HALF_UP,now,now),false);
        }catch(Exception e){a.toast(message(e));}});
    }

    void fee(boolean tariff,CalculationSnapshot prior){
        a.clear(tariff?"حق‌الوکاله تعرفه‌ای":"حق‌الوکاله توافقی","ورودی‌ها را بررسی کنید؛ ثبت محاسبه مبلغ پرونده یا قرارداد را تغییر نمی‌دهد");
        a.detailBack=()->center(related);
        final CalculationTariff.Rules rules;
        try(InputStreamReader reader=new InputStreamReader(a.getAssets().open("calculation/tariff-1398-reviewed-v2.properties"),StandardCharsets.UTF_8)){
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
        final List<String> ids=new ArrayList<>();final Spinner category,stage;final CheckBox supported,prosecutor,finalTrial;
        if(tariff){
            ids.add("FINANCIAL");ids.add("CIVIL_ENFORCEMENT");ids.addAll(rules.ranges.keySet());List<String> labels=new ArrayList<>();labels.add("مالی؛ پلکانی ماده ۹، غیرقطعی از حیث بها");labels.add("اجرای احکام حقوقی و اسناد رسمی؛ ماده ۲۵");
            for(CalculationTariff.Range r:rules.ranges.values())labels.add(r.label);
            category=pick("دسته تعرفه",labels.toArray(new String[0]));
            int index=ids.indexOf(previous(prior,"category","FINANCIAL"));category.setSelection(Math.max(0,index));
            stage=pick("مرحله",new String[]{"کل","بدوی","تجدیدنظر","فرجام حقوقی","دادسرا"});
            String oldStage=previous(prior,"stage","WHOLE");
            for(int i=0;i<CalculationTariff.Stage.values().length;i++)if(CalculationTariff.Stage.values()[i].name().equals(oldStage))stage.setSelection(i);
            prosecutor=confirm("در مسیر کیفری، پرونده مرحله دادسرا دارد");prosecutor.setChecked(Boolean.parseBoolean(previous(prior,"hasProsecutor","true")));
            finalTrial=confirm("در مسیر کیفری، رأی بدوی قطعی است");finalTrial.setChecked(Boolean.parseBoolean(previous(prior,"finalTrial","false")));
            supported=confirm("انطباق این دسته و نسخه تعرفه را بررسی کرده‌ام؛ این محاسبه فاقد عوامل ویژه پشتیبانی‌نشده است");
            a.page.addView(a.info("دامنه محاسبه","حکم قطعی از حیث بها، قرارهای خاص، تخصص، تعدد وکلا یا جرایم، تسخیری/معاضدتی، سازش و تغییر وکیل در این مسیر محاسبه نمی‌شوند. در تعرفه بازه‌ای، مبلغ کل منتخب را با دلیل وارد کنید."));
            TextView bounds=a.txt("",12,a.MUTED);a.page.addView(bounds);
            category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                public void onNothingSelected(AdapterView<?> p){}
                public void onItemSelected(AdapterView<?> p,View v,int position,long id){
                    CalculationTariff.Range r=rules.ranges.get(ids.get(position));
                    bounds.setText("CIVIL_ENFORCEMENT".equals(ids.get(position))?"مبلغ منتخب حق‌الوکاله را در فیلد مبلغ و محکوم‌به را در فیلد جدا وارد کنید؛ حداقل ۴٬۰۰۰٬۰۰۰ ریال و سقف ۲٪ محکوم‌به. مرحله کل و مقدار خدمت ۱ لازم است.":r==null?"بهای خواسته را وارد کنید؛ نرخ هر پله جدا اعمال می‌شود.":
                        "حدود کل تعرفه: "+(r.minimum==null?"حداقل تعیین نشده":CalculationArithmetic.display(r.minimum,CalculationArithmetic.Currency.RIAL))+
                        " تا "+CalculationArithmetic.display(r.maximum,CalculationArithmetic.Currency.RIAL)+"؛ ماده "+r.article);
                }
            });
        }else{category=null;stage=null;supported=null;prosecutor=null;finalTrial=null;}
        EditText award=tariff?field("محکوم‌به یا مورد اجرا؛ فقط دسته اجرای احکام، با واحد انتخابی",previous(prior,"enforcementAward","")):null;
        EditText quantity=tariff?field("ساعت مشاوره؛ در سایر دسته‌ها عدد ۱",previous(prior,"quantity","1")):null;
        button("محاسبه و نمایش نتیجه",()->{
            try{
                if(tariff&&!supported.isChecked())throw new IllegalArgumentException("انطباق دامنه تعرفه را تأیید کنید");
                CalculationArithmetic.Currency unit=currency.getSelectedItemPosition()==0?CalculationArithmetic.Currency.RIAL:CalculationArithmetic.Currency.TOMAN;
                long money=CalculationTariff.agreedFee(amount.getText().toString(),unit);
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
                    if(("FINANCIAL".equals(id)||"CIVIL_ENFORCEMENT".equals(id))&&CalculationArithmetic.decimal(quantity.getText().toString()).compareTo(java.math.BigDecimal.ONE)!=0)
                        throw new IllegalArgumentException("در دسته مالی و اجرای احکام مقدار خدمت باید ۱ باشد");
                    inputs.put("category",id);inputs.put("stage",stageValue.name());inputs.put("quantity",quantity.getText().toString());
                    boolean criminal=rules.ranges.containsKey(id)&&rules.ranges.get(id).family==CalculationTariff.Family.CRIMINAL;
                    boolean hasProsecutor=!criminal||prosecutor.isChecked(),isFinal=criminal&&finalTrial.isChecked();
                    inputs.put("hasProsecutor",Boolean.toString(hasProsecutor));inputs.put("finalTrial",Boolean.toString(isFinal));
                    inputs.put("sourceUrl",rules.sourceUrl);inputs.put("sourceTitle",rules.sourceTitle);inputs.put("rulePack",rules.version);
                    inputs.put("sourceAdoptionDate",rules.adoptionDate);inputs.put("sourceReviewDate",rules.reviewDate);
                    inputs.put("_rulePackSnapshot",rules.serializedRules);
                    if("CIVIL_ENFORCEMENT".equals(id))inputs.put("enforcementAward",award.getText().toString());
                    CalculationTariff.Result r="CIVIL_ENFORCEMENT".equals(id)?CalculationTariff.enforcement(rules,CalculationArithmetic.money(award.getText().toString(),unit),money,stageValue,explanation,RoundingMode.HALF_UP):"FINANCIAL".equals(id)?CalculationTariff.financial(rules,money,stageValue,explanation,false,RoundingMode.HALF_UP):
                        CalculationTariff.ranged(rules,id,money,quantity.getText().toString(),stageValue,hasProsecutor,isFinal,explanation,RoundingMode.HALF_UP);
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
            button("نسخه جدید / محاسبه مجدد",()->body(s.type==CalculationSnapshot.Type.ARSH_ESTIMATE,s));
    }
    private static String message(Exception e){return e.getMessage()==null?"محاسبه قابل انجام نیست":e.getMessage();}
}
