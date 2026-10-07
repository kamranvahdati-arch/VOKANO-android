package ir.kamranvahdati.lawoffice;

import android.view.View;
import android.widget.*;
import java.util.UUID;

/** Manual reference entry cannot confer official-source verification. Every edit is a new row. */
final class CalculationReferenceUi {
    private final MainActivity a;
    private final OfficeCalculations repo;
    private final Runnable back;
    CalculationReferenceUi(MainActivity a,Runnable back){this.a=a;this.repo=new OfficeCalculations(a.db);this.back=back;}

    void list(boolean history){
        a.clear(history?"تاریخچه نرخ‌ها و شاخص‌ها":"نرخ‌ها و شاخص‌ها","ورود دستی با حفظ منبع، تأیید کاربر و نسخه‌های پیشین");a.detailBack=back;
        button("ثبت نرخ سالانه دیه",()->edit(CalculationReference.Kind.ANNUAL_DIYAH,null));
        button("ثبت شاخص اقتصادی ماهانه",()->edit(CalculationReference.Kind.ECONOMIC_INDEX,null));
        button(history?"نمایش نسخه‌های جاری":"نمایش همه نسخه‌ها",()->list(!history));
        a.page.addView(a.info("اعتبار داده","ورود و تأیید شما به معنی تأیید رسمی منبع توسط نرم‌افزار نیست. سری شاخص باید نوع شاخص و سال پایه را مشخص کند؛ شاخص‌های با سال پایه یا تعریف متفاوت قابل ترکیب نیستند."));
        try{
            java.util.List<CalculationReference> values=history?repo.referenceHistory():repo.currentReferences();
            if(values.isEmpty())a.page.addView(a.empty("مبنایی ثبت نشده است؛ نرخ سال دیگر جایگزین نمی‌شود."));
            for(CalculationReference r:values)button(r.series+" · "+r.year+(r.month==0?"":"/"+r.month)+" · "+r.version+" · "+status(r),()->show(r));
        }catch(Exception e){a.toast(message(e));}
    }

    void show(CalculationReference r){
        a.clear("جزئیات مبنا",status(r));a.detailBack=()->list(false);
        a.page.addView(a.info(r.series,"مقدار: "+r.value+" "+r.unit+"\nسال/ماه: "+r.year+"/"+r.month+
            "\nنسخه: "+r.version+"\nشناسه: "+r.id+"\nنسخه قبلی: "+r.previousVersionId+
            "\nاعتبار: "+r.effectiveFrom+" تا "+r.effectiveTo+"\nمنبع: "+r.sourceTitle+
            "\nنوع منبع: "+r.sourceType+"\nشماره: "+r.sourceNumber+"\nتاریخ منبع: "+r.sourceDate+
            "\nنشانی: "+r.sourceUrl+"\nورود دستی: "+(r.manuallyEntered?"بله":"خیر")+"\nیادداشت: "+r.notes));
        if(r.kind==CalculationReference.Kind.ANNUAL_DIYAH||r.kind==CalculationReference.Kind.ECONOMIC_INDEX)
            button("اصلاح / تأیید در نسخه جدید",()->edit(r.kind,r));
    }

    void edit(CalculationReference.Kind kind,CalculationReference prior){
        boolean annual=kind==CalculationReference.Kind.ANNUAL_DIYAH;
        if(!annual&&kind!=CalculationReference.Kind.ECONOMIC_INDEX)throw new IllegalArgumentException("نوع مبنا پشتیبانی نمی‌شود");
        a.clear(annual?"ثبت نرخ سالانه دیه":"ثبت شاخص اقتصادی ماهانه",prior==null?"داده دستی؛ ثبت منبع الزامی است":"نسخه قبلی و نتایج ثبت‌شده حفظ می‌شوند");a.detailBack=()->list(false);
        EditText series=field("شناسه سری؛ شامل نوع و سال پایه",prior==null?"":prior.series);
        EditText year=field("سال مبنا",prior==null?"":Integer.toString(prior.year));
        EditText month=annual?null:field("ماه مبنا؛ ۱ تا ۱۲",prior==null?"":Integer.toString(prior.month));
        if(prior!=null){series.setEnabled(false);year.setEnabled(false);if(month!=null)month.setEnabled(false);}
        EditText value=field(annual?"نرخ کامل عادی به ریال":"مقدار شاخص؛ عدد مثبت",prior==null?"":prior.value);
        EditText from=field("شروع اعتبار",prior==null?"":prior.effectiveFrom);a.bindJalaliPicker(from);
        EditText to=field("پایان اعتبار",prior==null?"":prior.effectiveTo);a.bindJalaliPicker(to);
        EditText title=field("عنوان منبع",prior==null?"":prior.sourceTitle);
        EditText sourceType=field("نوع منبع یا سند",prior==null?"":prior.sourceType);
        EditText number=field("شماره سند؛ در صورت وجود",prior==null?"":prior.sourceNumber);
        EditText date=field("تاریخ منبع",prior==null?"":prior.sourceDate);a.bindJalaliPicker(date);
        EditText url=field("نشانی منبع؛ در صورت وجود",prior==null?"":prior.sourceUrl);
        EditText notes=field(prior==null?"توضیح مبنا":"علت ایجاد نسخه جدید","");
        CheckBox confirmed=new CheckBox(a);confirmed.setText("مقدار، واحد، سال، سری و منبع را بررسی و برای استفاده تأیید می‌کنم");confirmed.setTextColor(a.INK);a.page.addView(confirmed);
        a.page.addView(a.info("نحوه ثبت","بدون علامت تأیید، داده در انتظار بررسی ثبت می‌شود و قابل استفاده در محاسبه نیست. تأیید نیز یک نسخه دستی می‌سازد، نه یک نرخ رسمی تأییدشده. هر سال یا ماه جدید باید جداگانه ثبت شود."));
        button("ثبت نسخه مبنا",()->{
            try{
                // Reject stale editing rather than branch the reference lineage invisibly.
                if(prior!=null){boolean current=false;for(CalculationReference r:repo.currentReferences())if(r.id.equals(prior.id))current=true;
                    if(!current)throw new IllegalArgumentException("این نسخه تاریخی است؛ اصلاح را از نسخه جاری آغاز کنید");}
                int y=CalculationArithmetic.decimal(year.getText().toString()).intValueExact();
                int m=annual?0:CalculationArithmetic.decimal(month.getText().toString()).intValueExact();
                String start=CalculationReference.date(from.getText().toString()),end=CalculationReference.date(to.getText().toString());
                if(!start.startsWith(String.format(java.util.Locale.US,"%04d/",y))||!end.startsWith(String.format(java.util.Locale.US,"%04d/",y)))
                    throw new IllegalArgumentException("بازه اعتبار باید در سال مبنا باشد");
                String amount=annual?Long.toString(CalculationArithmetic.money(value.getText().toString(),CalculationArithmetic.Currency.RIAL)):
                    CalculationArithmetic.decimal(value.getText().toString()).stripTrailingZeros().toPlainString();
                long now=Math.max(System.currentTimeMillis(),prior==null?0:prior.createdAt+1);
                String id=UUID.randomUUID().toString();
                CalculationReference r=new CalculationReference(id,series.getText().toString(),"manual/"+id,prior==null?"":prior.id,kind,y,m,start,end,amount,annual?"IRR":"INDEX",
                    CalculationReference.required(title.getText().toString(),"عنوان منبع"),CalculationReference.required(sourceType.getText().toString(),"نوع منبع"),number.getText().toString(),
                    CalculationReference.date(date.getText().toString()),url.getText().toString(),CalculationReference.required(notes.getText().toString(),prior==null?"توضیح مبنا":"علت ایجاد نسخه جدید"),
                    confirmed.isChecked()?CalculationReference.Status.USER_ENTERED:CalculationReference.Status.NEEDS_REVIEW,now,now,confirmed.isChecked(),true,false);
                repo.saveReference(r);show(r);
            }catch(Exception e){a.toast(message(e));}
        });
    }
    private static String status(CalculationReference r){
        if(r.superseded)return "منسوخ";
        if(r.status==CalculationReference.Status.OFFICIAL_VERIFIED)return "منبع رسمی بررسی‌شده";
        if(r.status==CalculationReference.Status.USER_ENTERED&&r.userConfirmed)return "ورود دستی؛ تأیید کاربر";
        return "در انتظار بررسی؛ غیرقابل استفاده";
    }
    private EditText field(String label,String value){a.page.addView(a.txt(label,12,a.MUTED));EditText v=a.input(label);v.setText(value);a.page.addView(v);return v;}
    private void button(String label,Runnable action){TextView v=a.action(label);v.setOnClickListener(x->action.run());a.page.addView(v);}
    private static String message(Exception e){return e.getMessage()==null?"ثبت مبنا انجام نشد؛ ورودی‌ها را بررسی کنید":e.getMessage();}
}
