package ir.kamranvahdati.lawoffice;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/** Bounded historical article-709 schedule. Never diagnoses injury or determines
 * overlap, aggravation, fund entitlement, or the amount in a final judgment. */
final class CalculationPrescribedBody {
    static final String ENGINE_VERSION="prescribed-head-arithmetic/1";
    static final class Injury {
        final String id,label;final CalculationArithmetic.Fraction ratio;
        Injury(String id,String label,String ratio){
            this.id=id;this.label=CalculationReference.required(label,"عنوان صدمه");
            String[] parts=CalculationReference.required(ratio,"نسبت صدمه").split("/",-1);
            if(parts.length!=2)throw new IllegalArgumentException("نسبت صدمه نامعتبر است");
            this.ratio=CalculationArithmetic.Fraction.of(Long.parseLong(parts[0]),Long.parseLong(parts[1]));
            // This pack expressly excludes the >= one-third article-560 interaction.
            if(this.ratio.numerator.signum()<=0||this.ratio.numerator.multiply(java.math.BigInteger.valueOf(3)).compareTo(this.ratio.denominator)>=0)
                throw new IllegalArgumentException("نسبت خارج از دامنه صدمات این بسته است");
        }
    }
    static final class Rules {
        final String version,url,title,article,serialized;
        final Map<String,Injury> injuries;
        Rules(Reader reader)throws Exception{
            StringBuilder b=new StringBuilder();char[] chars=new char[2048];int count;
            while((count=reader.read(chars))!=-1){b.append(chars,0,count);if(b.length()>30000)throw new IllegalArgumentException("بسته صدمات بیش از حد بزرگ است");}
            serialized=b.toString();Properties p=new Properties();p.load(new StringReader(serialized));
            version=required(p,"version");url=required(p,"source.url");title=required(p,"source.title");article=required(p,"source.article");
            if(!"historical-primary-text-reviewed".equals(required(p,"review.status")))throw new IllegalArgumentException("بسته صدمات بررسی نشده است");
            Map<String,Injury> data=new LinkedHashMap<>();
            for(String id:required(p,"injury.ids").split(",")){
                if(data.put(id,new Injury(id,required(p,"injury."+id+".label"),required(p,"injury."+id+".ratio")))!=null)throw new IllegalArgumentException("صدمه تکراری در بسته");
            }
            injuries=Collections.unmodifiableMap(data);
        }
        private static String required(Properties p,String key){return CalculationReference.required(p.getProperty(key),key);}
    }
    static CalculationArithmetic.Fraction calculate(Rules rules,String injuryId,CalculationReference annualRate,
            String date,String medicalBasis,boolean scopeConfirmed){
        if(rules==null||!scopeConfirmed)throw new IllegalArgumentException("انطباق صدمه مستقل سر یا صورت و نبود عوامل خاص را تأیید کنید");
        CalculationReference.required(medicalBasis,"مستند تشخیص و انطباق صدمه");
        Injury injury=rules.injuries.get(injuryId);
        if(injury==null)throw new IllegalArgumentException("این صدمه در جدول پشتیبانی نمی‌شود");
        // Reuse existing annual validation; no fallback to a different year.
        return CalculationBodyMath.assessedPercent(annualRate,date,"100",medicalBasis).multiply(injury.ratio);
    }
    private CalculationPrescribedBody(){}
}
