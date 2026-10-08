package ir.kamranvahdati.lawoffice;

import java.io.Reader;
import java.io.IOException;
import java.util.*;

/** Reviewed facsimile transcription. No interpolation, online claim or latest-month fallback. */
final class CalculationDelayPack {
    static final String VERSION="cbi-1395-historical-1399-1401/1";
    static final String SERIES="CBI-delay-base1395-monthly-table";
    static final String URL="https://www.ekhtebar.ir/wp-content/uploads/2023/04/shakhes-2.jpg";
    final List<CalculationReference> references;
    CalculationDelayPack(Reader reader)throws IOException {
        Properties p=new Properties();p.load(reader);
        if(p.size()!=3)throw new IllegalArgumentException("بسته شاخص تاریخی ناقص یا ناشناخته است");
        List<CalculationReference> all=new ArrayList<>();
        for(int year=1399;year<=1401;year++){
            String row=p.getProperty(Integer.toString(year),"");String[] values=row.split(",",-1);
            if(values.length!=12)throw new IllegalArgumentException("ردیف شاخص باید دوازده ماه داشته باشد");
            for(int month=1;month<=12;month++){
                String first=new JalaliDate(year,month,1).value();
                String last=new JalaliDate(year,month,JalaliDate.monthLength(year,month)).value();
                all.add(new CalculationReference(VERSION+"/"+year+"/"+month,SERIES,VERSION,"",
                    CalculationReference.Kind.ECONOMIC_INDEX,year,month,first,last,values[month-1],"INDEX-1395=100",
                    "جدول بانک مرکزی؛ شاخص محاسبه تأخیر تأدیه تا ۱۴۰۱","تصویر سند بازنشرشده؛ تطبیق تاریخی",
                    "02/10899","1402/01/22",URL,
                    "پوشش این بسته فقط ۱۳۹۹ تا ۱۴۰۱؛ میانگین سال و نرخ تورم درصدی جایگزین این اعداد نیستند. تصویر مرجع SHA256: c75badc7d6875d9104b8061d0625f25e57bb94fe43d4bfcbe8a89e8724076816",
                    CalculationReference.Status.REVIEWED_PUBLICATION,1791417600000L,1791417600000L,false,false,false));
            }
        }
        references=Collections.unmodifiableList(all);
    }
    CalculationReference at(String date){
        JalaliDate d=JalaliDate.parse(date);
        if(d.year<1399||d.year>1401)throw new IllegalArgumentException("شاخص معتبر ماه انتخابی در بسته موجود نیست؛ پوشش فعلی فقط ۱۳۹۹ تا ۱۴۰۱ است. عدد جایگزین نمی‌شود.");
        return CalculationReference.select(references,CalculationReference.Kind.ECONOMIC_INDEX,SERIES,d.year,d.month,d.value());
    }
    /** These are explicit lawyer attestations, never an automatic entitlement finding. */
    static String validateScope(CalculationDelayMath.Mode mode,String due,String demand,String start,String payment,
            String basis,boolean moneyDebt,boolean ordinaryConditions,boolean standardCheque,boolean noSpecialCases){
        if(mode==null||!moneyDebt||!noSpecialCases)throw new IllegalArgumentException("نوع دین و نبود موارد خاص پشتیبانی‌نشده را بررسی کنید");
        String d=CalculationReference.date(due),s=CalculationReference.date(start),end=CalculationReference.date(payment);
        String reason=CalculationReference.required(basis,"مستند حقوقی انتخاب مبدأ و شرایط پرونده");
        if(s.compareTo(d)<0||end.compareTo(s)<0)throw new IllegalArgumentException("ترتیب تاریخ سررسید، مبدأ و پرداخت معتبر نیست");
        if(mode==CalculationDelayMath.Mode.ORDINARY_DEBT){
            if(!ordinaryConditions)throw new IllegalArgumentException("شرایط ماده ۵۲۲ باید بررسی و تأیید شود");
            if(CalculationReference.date(demand).compareTo(end)>0)throw new IllegalArgumentException("تاریخ مطالبه پس از پرداخت است");
        }else if(!standardCheque||!s.equals(d))throw new IllegalArgumentException("در مسیر چک مشمول رأی ۸۱۲، مبدأ باید تاریخ مندرج در چک باشد؛ موارد استثنایی در این مسیر پشتیبانی نمی‌شوند");
        return reason;
    }
}
