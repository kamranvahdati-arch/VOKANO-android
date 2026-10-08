package ir.kamranvahdati.lawoffice;

import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

/** Articles 555-557: explicit findings, never infers lunar dates or Mecca boundaries. */
final class CalculationDeath {
    static final String ENGINE_VERSION="death-taghliz/1";
    enum Finding { UNKNOWN, YES, NO }
    static final class Rules {
        final String version, sourceUrl, serialized;
        final CalculationArithmetic.Fraction increment;
        Rules(Reader reader)throws Exception {
            StringBuilder b=new StringBuilder();char[] c=new char[1024];int n;
            while((n=reader.read(c))!=-1){b.append(c,0,n);if(b.length()>16000)throw new IllegalArgumentException("بسته قواعد بیش از حد بزرگ است");}
            serialized=b.toString();Properties p=new Properties();p.load(new StringReader(serialized));
            version=CalculationReference.required(p.getProperty("version"),"نسخه قاعده");
            sourceUrl=CalculationReference.required(p.getProperty("source.url"),"منبع قاعده");
            if(!"historical-primary-text-reviewed".equals(p.getProperty("review.status")))throw new IllegalArgumentException("قاعده بررسی نشده است");
            increment=CalculationArithmetic.Fraction.of(Long.parseLong(p.getProperty("increment.numerator")),Long.parseLong(p.getProperty("increment.denominator")));
            if(increment.numerator.signum()<=0||increment.numerator.compareTo(increment.denominator)>=0)throw new IllegalArgumentException("ضریب افزایش نامعتبر است");
        }
    }
    static final class Result {
        final CalculationArithmetic.Fraction base, additional, total;
        final boolean enhanced;
        Result(CalculationArithmetic.Fraction base,Rules rules,boolean enhanced){
            this.base=base;this.enhanced=enhanced;
            additional=base.multiply(enhanced?rules.increment:CalculationArithmetic.Fraction.of(0,1));
            total=base.add(additional);
        }
    }
    private static Finding both(Finding a,Finding b){
        if(a==null||b==null)throw new IllegalArgumentException("وضعیت زمان و مکان ناقص است");
        if(a==Finding.NO||b==Finding.NO)return Finding.NO;
        return a==Finding.YES&&b==Finding.YES?Finding.YES:Finding.UNKNOWN;
    }
    static Result calculate(Rules rules,CalculationReference rate,String event,String death,String valuation,
            String basePercent,String basis,boolean deathConfirmed,Finding actMonth,Finding deathMonth,
            Finding actMecca,Finding deathMecca){
        if(rules==null||!deathConfirmed)throw new IllegalArgumentException("این مسیر فقط برای دیه نفس با مبنای مستند است؛ جراحت و ارش مشمول نیستند");
        String e=CalculationReference.date(event),d=CalculationReference.date(death),v=CalculationReference.date(valuation);
        if(d.compareTo(e)<0||v.compareTo(d)<0)throw new IllegalArgumentException("ترتیب وقوع، فوت و ارزش‌گذاری صحیح نیست");
        Finding month=both(actMonth,deathMonth),place=both(actMecca,deathMecca);
        boolean enhanced=month==Finding.YES||place==Finding.YES;
        if(!enhanced&&(month==Finding.UNKNOWN||place==Finding.UNKNOWN))throw new IllegalArgumentException("احراز یا رد شرایط زمانی و مکانی تغلیظ کامل نیست");
        CalculationArithmetic.Fraction base=CalculationBodyMath.assessedPercent(rate,v,basePercent,basis);
        if(base.numerator.signum()<=0)throw new IllegalArgumentException("مبنای دیه نفس باید مثبت باشد");
        return new Result(base,rules,enhanced);
    }
    private CalculationDeath(){}
}
