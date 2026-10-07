package ir.kamranvahdati.lawoffice;

import java.io.Reader;
import java.util.Properties;
import java.math.RoundingMode;

/** Converts an externally assessed percentage/amount; never diagnoses an injury or awards arsh. */
final class CalculationBodyMath {
    static final String ENGINE_VERSION="body-assessed-arithmetic/1";
    static CalculationReference readRate(Reader reader)throws Exception{
        Properties p=new Properties();p.load(reader);
        return new CalculationReference(p.getProperty("id"),p.getProperty("series"),p.getProperty("version"),"",
            CalculationReference.Kind.ANNUAL_DIYAH,Integer.parseInt(p.getProperty("year")),0,p.getProperty("from"),p.getProperty("to"),p.getProperty("value"),"IRR",
            p.getProperty("sourceTitle"),p.getProperty("sourceType"),"",p.getProperty("sourceDate"),p.getProperty("sourceUrl"),p.getProperty("notes"),
            CalculationReference.Status.REVIEWED_PUBLICATION,1791331200000L,1791331200000L,false,false,false);
    }
    static CalculationArithmetic.Fraction assessedPercent(CalculationReference rate,String date,String percent,String assessment){
        CalculationReference.required(assessment,"مستند درصد تعیین‌شده");
        if(rate==null||rate.kind!=CalculationReference.Kind.ANNUAL_DIYAH)throw new IllegalArgumentException("نرخ سالانه دیه موجود نیست");
        String d=CalculationReference.date(date);rate.requireUsable(d,JalaliDate.parse(d).year,0,rate.series);
        return CalculationArithmetic.Fraction.of(CalculationArithmetic.money(rate.value,CalculationArithmetic.Currency.RIAL),1)
            .multiply(CalculationArithmetic.percent(percent));
    }
    static long assessedAmount(String amount,CalculationArithmetic.Currency currency,String assessment){
        CalculationReference.required(assessment,"مستند مبلغ تعیین‌شده");
        return CalculationArithmetic.money(amount,currency);
    }
    private CalculationBodyMath(){}
}
