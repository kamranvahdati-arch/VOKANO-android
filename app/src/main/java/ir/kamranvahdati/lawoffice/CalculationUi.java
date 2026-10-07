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
        button("خسارت تأخیر تأدیه",()->a.preview("وضعیت بررسی محاسبه تأخیر",
            "حساب عددی شاخص‌ها آماده است؛ تکمیل کنترل شرایط دین عادی و استثناهای چک هنوز لازم است. این بخش در این نسخه توسعه‌ای برای محاسبه نهایی فعال نیست."));
        button("دیه",()->unavailable("دیه"));button("ارش",()->unavailable("ارش"));
        button("تاریخچه محاسبات",this::history);
        button("نرخ‌ها و شاخص‌ها",()->new CalculationReferenceUi(a,()->center(related)).list(false));
        a.page.addView(a.info("وضعیت نسخه توسعه‌ای","مرکز محاسبات هنوز کامل نشده است. تعرفه فقط فروض مشخص متن مصوب ۱۳۹۸ را پوشش می‌دهد؛ بررسی همه اصلاحات و موارد خاص هنوز تکمیل نشده است."));
    }
    private void unavailable(String name){a.preview(name,"تأیید مبانی رسمی این بخش هنوز تکمیل نشده؛ محاسبه حقوقی فعال نیست.");}
    private void button(String title,Runnable action){TextView v=a.action(title);v.setOnClickListener(x->{try{action.run();}catch(Exception e){a.toast(message(e));}});a.page.addView(v);}
    private EditText field(String label,String value){a.page.addView(a.txt(label,12,a.MUTED));EditText v=a.input(label);v.setText(value);a.page.addView(v);return v;}
    private Spinner pick(String label,String[] values){a.page.addView(a.txt(label,12,a.MUTED));Spinner s=a.spinner(values);a.page.addView(s);return s;}
    private CheckBox confirm(String label){CheckBox c=new CheckBox(a);c.setText(label);c.setTextColor(a.INK);a.page.addView(c);return c;}
    private String previous(CalculationSnapshot p,String key,String fallback){return p==null?fallback:p.inputSnapshot.getOrDefault(key,fallback);}

    void fee(boolean tariff,CalculationSnapshot prior){
        a.clear(tariff?"حق‌الوکاله تعرفه‌ای":"حق‌الوکاله توافقی","ورودی‌ها را بررسی کنید؛ ثبت محاسبه مبلغ پرونده یا قرارداد را تغییر نمی‌دهد");
        a.detailBack=()->center(related);
        final CalculationTariff.Rules rules;
        try(InputStreamReader reader=new InputStreamReader(a.getAssets().open("calculation/tariff-1398-reviewed.properties"),StandardCharsets.UTF_8)){
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
            ids.add("FINANCIAL");ids.addAll(rules.ranges.keySet());List<String> labels=new ArrayList<>();labels.add("مالی؛ پلکانی ماده ۹، غیرقطعی از حیث بها");
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
                    bounds.setText(r==null?"بهای خواسته را وارد کنید؛ نرخ هر پله جدا اعمال می‌شود.":
                        "حدود کل تعرفه: "+(r.minimum==null?"حداقل تعیین نشده":CalculationArithmetic.display(r.minimum,CalculationArithmetic.Currency.RIAL))+
                        " تا "+CalculationArithmetic.display(r.maximum,CalculationArithmetic.Currency.RIAL)+"؛ ماده "+r.article);
                }
            });
        }else{category=null;stage=null;supported=null;prosecutor=null;finalTrial=null;}
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
                    if("FINANCIAL".equals(id)&&CalculationArithmetic.decimal(quantity.getText().toString()).compareTo(java.math.BigDecimal.ONE)!=0)
                        throw new IllegalArgumentException("در دسته مالی مقدار خدمت باید ۱ باشد");
                    inputs.put("category",id);inputs.put("stage",stageValue.name());inputs.put("quantity",quantity.getText().toString());
                    boolean criminal=rules.ranges.containsKey(id)&&rules.ranges.get(id).family==CalculationTariff.Family.CRIMINAL;
                    boolean hasProsecutor=!criminal||prosecutor.isChecked(),isFinal=criminal&&finalTrial.isChecked();
                    inputs.put("hasProsecutor",Boolean.toString(hasProsecutor));inputs.put("finalTrial",Boolean.toString(isFinal));
                    inputs.put("sourceUrl",rules.sourceUrl);inputs.put("sourceTitle",rules.sourceTitle);inputs.put("rulePack",rules.version);
                    inputs.put("sourceAdoptionDate",rules.adoptionDate);inputs.put("sourceReviewDate",rules.reviewDate);
                    inputs.put("_rulePackSnapshot",rules.serializedRules);
                    CalculationTariff.Result r="FINANCIAL".equals(id)?CalculationTariff.financial(rules,money,stageValue,explanation,false,RoundingMode.HALF_UP):
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
    }
    private static String message(Exception e){return e.getMessage()==null?"محاسبه قابل انجام نیست":e.getMessage();}
}
