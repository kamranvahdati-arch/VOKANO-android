package ir.kamranvahdati.lawoffice;

import java.io.Reader;
import java.util.Properties;
import java.math.RoundingMode;

/** Converts an externally assessed percentage/amount; never diagnoses an injury or awards arsh. */
final class CalculationBodyMath {
    static final String ENGINE_VERSION="body-assessed-arithmetic/2";
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
    /** Only sums separately assessed awards after an explicit non-overlap finding.
     * The caller cannot turn a diagnostic list into independently payable awards. */
    static final class IndependentTotal {
        final CalculationArithmetic.Fraction exact;
        final java.util.List<String> steps;
        IndependentTotal(CalculationArithmetic.Fraction exact,java.util.List<String> steps) {
            this.exact=exact;this.steps=java.util.Collections.unmodifiableList(new java.util.ArrayList<>(steps));
        }
    }
    static IndependentTotal independentPercentages(CalculationReference rate,String date,String rows,
            String assessment,boolean separatelyAwarded) {
        if(!separatelyAwarded)throw new IllegalArgumentException("استقلال اقلام و نبود تداخل باید در مستند مرجع صالح احراز شده باشد");
        CalculationReference.required(assessment,"مستند استقلال اقلام");
        String input=CalculationReference.required(rows,"اقلام مستقل");
        if(input.length()>20000)throw new IllegalArgumentException("فهرست اقلام بیش از حد طولانی است");
        String[] lines=input.split("\\r?\\n",-1);
        if(lines.length<2||lines.length>50)throw new IllegalArgumentException("دو تا پنجاه قلم مستقل وارد کنید");
        java.util.Set<String> identities=new java.util.HashSet<>();
        java.util.List<String> steps=new java.util.ArrayList<>();
        CalculationArithmetic.Fraction sum=CalculationArithmetic.Fraction.of(0,1);
        for(String line:lines) {
            String[] cells=line.split("\\|",-1);
            if(cells.length!=3)throw new IllegalArgumentException("هر سطر باید شامل شرح متمایز | درصد | مستند همان قلم باشد");
            String name=CalculationReference.required(cells[0],"شرح متمایز قلم");
            if(!identities.add(name))throw new IllegalArgumentException("شرح قلم تکراری است؛ صدمات متمایز باید شناسه یا شرح متمایز داشته باشند");
            String proof=CalculationReference.required(cells[2],"مستند همان قلم");
            CalculationArithmetic.Fraction part=assessedPercent(rate,date,cells[1].trim(),proof);
            if(part.numerator.signum()<=0)throw new IllegalArgumentException("درصد هر قلم باید مثبت باشد");
            sum=sum.add(part);steps.add(name+": "+rate.value+" × "+cells[1].trim()+" / 100 = "+part+" ریال؛ "+proof);
        }
        // No cap at one full diyah; independent awards can exceed it. Overflow is
        // checked when the final exact sum is converted to the monetary boundary.
        steps.add("جمع کسرهای دقیق اقلام مستقل: "+sum+" ریال؛ یک‌بار گرد کردن در پایان، بدون تعیین خودکار تداخل یا استحقاق");
        return new IndependentTotal(sum,steps);
    }
    private CalculationBodyMath(){}
}
