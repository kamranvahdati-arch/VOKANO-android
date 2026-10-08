package ir.kamranvahdati.lawoffice;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
import java.util.*;
public class CalculationPartialPaymentsTest {
    static int checks;
    static void check(boolean ok){checks++;if(!ok)throw new AssertionError("check "+checks);}
    static void rejects(Runnable r){try{r.run();throw new AssertionError("accepted invalid payment");}catch(IllegalArgumentException expected){checks++;}}
    static CalculationPartialPayments.Payment p(String d,long n){return new CalculationPartialPayments.Payment(d,n,"receipt fixture");}
    static CalculationPartialPayments.Result calc(CalculationPartialPayments.Indices ix,long principal,List<CalculationPartialPayments.Payment> ps){
        return CalculationPartialPayments.calculate(CalculationDelayMath.Mode.CHEQUE,principal,"1405/01/01","1405/06/31","reviewed judgment",ps,ix,RoundingMode.HALF_UP);
    }
    public static void main(String[] args)throws Exception{
        CalculationCurrentIndices pack=new CalculationCurrentIndices(Files.newBufferedReader(Paths.get("app/src/main/assets/calculation/cbi-base1400-1405-06-v1.properties"),StandardCharsets.UTF_8));
        CalculationPartialPayments.Result r=calc(pack::at,6153000,Arrays.asList(p("1405/02/01",3337500)));
        check(r.remainingRials==4000000);check(r.paidRials==3337500);check(r.totalDamagesRials==1184500);check(r.exactRemaining.toString().equals("4000000/1"));check(r.references.size()==3);
        // Equivalent same-day subdivisions must not cause penny loss from intermediate rounding.
        for(int n=1;n<=30;n++){
            CalculationPartialPayments.Result a=calc(pack::at,6153,Arrays.asList(p("1405/02/01",n),p("1405/02/01",31-n)));
            CalculationPartialPayments.Result b=calc(pack::at,6153,Arrays.asList(p("1405/02/01",31)));
            check(a.exactRemaining.toString().equals(b.exactRemaining.toString()));check(a.totalDamagesRials==b.totalDamagesRials);
        }
        check(calc(pack::at,6153000,Arrays.asList(p("1405/02/01",6675000))).remainingRials==0);
        check(calc(pack::at,6153000,Arrays.asList(p("1405/01/01",6153000))).totalDamagesRials==0);
        check(calc(pack::at,6153000,Arrays.asList(p("1405/06/31",8000000))).totalDamagesRials==1847000);
        rejects(()->calc(pack::at,6153000,Arrays.asList(p("1405/02/01",6675001))));
        rejects(()->calc(pack::at,6153000,Arrays.asList(p("1405/02/01",6675000),p("1405/03/01",1))));
        rejects(()->calc(pack::at,6153000,Arrays.asList(p("1404/12/29",1))));
        rejects(()->calc(pack::at,6153000,Arrays.asList(p("1405/07/01",1))));
        rejects(()->calc(pack::at,6153000,Arrays.asList(p("1405/03/01",1),p("1405/02/01",1))));
        rejects(()->calc(pack::at,6153000,Collections.emptyList()));
        rejects(()->p("1405/02/01",0));rejects(()->new CalculationPartialPayments.Payment("1405/02/01",1,""));
        rejects(()->CalculationPartialPayments.parse("1405/02/01 | 1",CalculationArithmetic.Currency.RIAL));
        rejects(()->CalculationPartialPayments.parse("1405/02/01 | 1.1 | receipt",CalculationArithmetic.Currency.RIAL));
        check(CalculationPartialPayments.parse("۱۴۰۵/۰۲/۰۱ | ۳۳۳۷۵۰ | رسید",CalculationArithmetic.Currency.TOMAN).get(0).rials==3337500);
        check(CalculationArithmetic.Fraction.of(1,3).subtract(CalculationArithmetic.Fraction.of(1,6)).toString().equals("1/6"));
        rejects(()->CalculationArithmetic.Fraction.of(1,3).subtract(CalculationArithmetic.Fraction.of(1,2)));
        try{r.steps.clear();throw new AssertionError();}catch(UnsupportedOperationException expected){checks++;}
        System.out.println("CalculationPartialPaymentsTest: "+checks+" checks passed");
    }
}
