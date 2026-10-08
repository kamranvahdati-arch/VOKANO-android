package ir.kamranvahdati.lawoffice;

import java.math.RoundingMode;
import java.util.*;

/** Reviewed advisory 7/1402/220 paragraph four: subtract each actual payment from
 * the indexed balance at its date, then index only the remaining balance.
 * No allocation to principal alone, interest rate, or premature rounding. */
final class CalculationPartialPayments {
    static final String VERSION="reviewed-partial-payments-7-1402-220/1";
    static final String SOURCE="https://vakilan.net/Vots/Show/103569";
    interface Indices { CalculationReference at(String date); }
    static final class Payment {
        final String date,receipt;final long rials;
        Payment(String date,long rials,String receipt){
            this.date=CalculationReference.date(date);this.receipt=CalculationReference.required(receipt,"مستند پرداخت");
            if(rials<=0)throw new IllegalArgumentException("مبلغ هر پرداخت باید مثبت باشد");this.rials=rials;
        }
    }
    static List<Payment> parse(String text,CalculationArithmetic.Currency unit){
        if(text==null||text.length()>20000)throw new IllegalArgumentException("فهرست پرداخت‌ها نامعتبر است");
        List<Payment> out=new ArrayList<>();
        for(String line:text.trim().split("\\r?\\n")){
            String[] cells=line.split("\\|",-1);
            if(cells.length!=3)throw new IllegalArgumentException("هر پرداخت: تاریخ | مبلغ | مستند؛ هر مورد در یک سطر");
            out.add(new Payment(cells[0].trim(),CalculationArithmetic.money(cells[1].trim(),unit),cells[2].trim()));
            if(out.size()>100)throw new IllegalArgumentException("حداکثر ۱۰۰ پرداخت در یک محاسبه مجاز است");
        }
        return Collections.unmodifiableList(out);
    }
    static final class Result {
        final long remainingRials,paidRials,totalDamagesRials;
        final CalculationArithmetic.Fraction exactRemaining;
        final List<String> steps;final List<CalculationReference> references;
        Result(CalculationArithmetic.Fraction remaining,long paid,long principal,List<String> steps,List<CalculationReference> refs,RoundingMode rounding){
            exactRemaining=remaining;remainingRials=remaining.roundedRials(rounding);paidRials=paid;
            totalDamagesRials=remaining.add(CalculationArithmetic.Fraction.of(paid,1)).subtract(CalculationArithmetic.Fraction.of(principal,1)).roundedRials(rounding);
            this.steps=Collections.unmodifiableList(new ArrayList<>(steps));references=Collections.unmodifiableList(new ArrayList<>(refs));
        }
    }
    static Result calculate(CalculationDelayMath.Mode mode,long principal,String start,String end,String basis,
            List<Payment> payments,Indices indices,RoundingMode rounding){
        if(payments==null||payments.isEmpty()||payments.size()>100||indices==null)throw new IllegalArgumentException("پرداخت‌های مستند و شاخص‌ها الزامی‌اند");
        start=CalculationReference.date(start);end=CalculationReference.date(end);
        CalculationReference first=indices.at(start),last=indices.at(end);
        // Validate legal numerical endpoints independently from payment allocation.
        CalculationDelayMath.calculate(mode,principal,start,end,basis,first.series,first,last,rounding);
        CalculationArithmetic.Fraction balance=CalculationArithmetic.Fraction.of(principal,1);
        List<String> steps=new ArrayList<>();Map<String,CalculationReference> refs=new LinkedHashMap<>();refs.put(first.id,first);
        String previous=start;CalculationReference previousIndex=first;long paid=0;
        for(Payment payment:payments){
            if(payment==null||payment.date.compareTo(previous)<0||payment.date.compareTo(end)>0)throw new IllegalArgumentException("پرداخت‌ها باید به ترتیب تاریخ و در بازه محاسبه باشند");
            CalculationReference at=indices.at(payment.date);
            CalculationDelayMath.calculate(mode,1,previous,payment.date,basis,first.series,previousIndex,at,rounding);
            CalculationArithmetic.Fraction indexed=balance.multiply(value(at)).divide(value(previousIndex));
            balance=indexed.subtract(CalculationArithmetic.Fraction.of(payment.rials,1));
            try{paid=Math.addExact(paid,payment.rials);}catch(ArithmeticException e){throw new IllegalArgumentException("جمع پرداخت‌ها خارج از محدوده است");}
            steps.add(payment.date+"؛ "+payment.receipt+": مانده تعدیل‌شده "+indexed+" − پرداخت "+payment.rials+" = "+balance+" ریال؛ شاخص "+previousIndex.value+" ← "+at.value);
            previous=payment.date;previousIndex=at;refs.put(at.id,at);
        }
        CalculationDelayMath.calculate(mode,1,previous,end,basis,first.series,previousIndex,last,rounding);
        balance=balance.multiply(value(last)).divide(value(previousIndex));refs.put(last.id,last);
        steps.add("تعدیل مانده پرداخت‌نشده تا "+end+"؛ شاخص "+previousIndex.value+" ← "+last.value+"؛ مانده دقیق "+balance+" ریال");
        steps.add("خسارت تجمعی = مانده نهایی + پرداخت‌های واقعی − اصل اولیه؛ گرد کردن فقط در پایان. مبنای تخصیص پرداخت، نسبت اصل و خسارت در تاریخ پرداخت است.");
        return new Result(balance,paid,principal,steps,new ArrayList<>(refs.values()),rounding);
    }
    private static CalculationArithmetic.Fraction value(CalculationReference r){return CalculationArithmetic.Fraction.decimal(CalculationArithmetic.decimal(r.value));}
}
