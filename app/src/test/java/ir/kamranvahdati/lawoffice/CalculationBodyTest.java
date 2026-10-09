package ir.kamranvahdati.lawoffice;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
public final class CalculationBodyTest {
    static int checks;
    static void check(boolean ok){checks++;if(!ok)throw new AssertionError("Check "+checks);}
    static void rejects(Runnable r){checks++;try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("Expected rejection "+checks);}
    public static void main(String[] args)throws Exception{
        final CalculationReference rate;
        try(Reader reader=new InputStreamReader(new FileInputStream(args[0]),StandardCharsets.UTF_8)){rate=CalculationBodyMath.readRate(reader);}
        check(rate.status==CalculationReference.Status.REVIEWED_PUBLICATION);check(!rate.manuallyEntered);
        check(rate.sourceType.contains("اصل بخشنامه مشاهده نشده"));
        check(CalculationBodyMath.assessedPercent(rate,"1405/01/01","۱","fixture").roundedRials(RoundingMode.HALF_UP)==210000000L);
        check(CalculationBodyMath.assessedPercent(rate,"1405/12/29","۲٫۵","fixture").roundedRials(RoundingMode.HALF_UP)==525000000L);
        check(CalculationBodyMath.assessedPercent(rate,"1405/05/01","100","fixture").roundedRials(RoundingMode.HALF_UP)==21000000000L);
        check(CalculationBodyMath.assessedPercent(rate,"1405/05/01","0","fixture").roundedRials(RoundingMode.HALF_UP)==0);
        check(CalculationBodyMath.assessedPercent(rate,"1405/05/01","0.000000001","fixture").toString().equals("21/100"));
        rejects(()->CalculationBodyMath.assessedPercent(rate,"1404/05/01","1","fixture"));
        rejects(()->CalculationBodyMath.assessedPercent(rate,"1406/01/01","1","fixture"));
        rejects(()->CalculationBodyMath.assessedPercent(rate,"1405/01/01","100.1","fixture"));
        rejects(()->CalculationBodyMath.assessedPercent(rate,"1405/01/01","-1","fixture"));
        rejects(()->CalculationBodyMath.assessedPercent(rate,"1405/01/01","1"," "));
        rejects(()->CalculationBodyMath.assessedPercent(null,"1405/01/01","1","fixture"));
        check(CalculationBodyMath.assessedAmount("۱۲۳٫۴",CalculationArithmetic.Currency.TOMAN,"fixture")==1234);
        check(CalculationBodyMath.assessedAmount("9223372036854775807",CalculationArithmetic.Currency.RIAL,"fixture")==Long.MAX_VALUE);
        rejects(()->CalculationBodyMath.assessedAmount("9223372036854775807",CalculationArithmetic.Currency.TOMAN,"fixture"));
        rejects(()->CalculationBodyMath.assessedAmount("1.1",CalculationArithmetic.Currency.RIAL,"fixture"));
        rejects(()->CalculationBodyMath.assessedAmount("100",CalculationArithmetic.Currency.RIAL,""));
        String rows="left injury | ۲٫۵ | judgment item1\nright injury | ۱ | judgment item2";
        CalculationBodyMath.IndependentTotal total=CalculationBodyMath.independentPercentages(rate,"1405/01/01",rows,"independence finding",true);
        check(total.exact.roundedRials(RoundingMode.HALF_UP)==735000000L);check(total.steps.size()==3);
        check(CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 100 | item1\nb | 100 | item2","fixture",true).exact.roundedRials(RoundingMode.HALF_UP)==42000000000L);
        // .42 + .42 = .84 -> 1 rial, not the sum of separately rounded zeroes.
        check(CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 0.000000002 | item1\nb | 0.000000002 | item2","fixture",true).exact.roundedRials(RoundingMode.HALF_UP)==1);
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01",rows,"fixture",false));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01",rows,"",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1404/01/01",rows,"fixture",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 1 | item1","fixture",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 1 | item1\na | 2 | item2","fixture",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 1 | item1\nb | 2 | ","fixture",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 0 | item1\nb | 2 | item2","fixture",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | -1 | item1\nb | 2 | item2","fixture",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 101 | item1\nb | 2 | item2","fixture",true));
        rejects(()->CalculationBodyMath.independentPercentages(rate,"1405/01/01","a | 1 | item1\nb | 2","fixture",true));
        check(CalculationBodyMath.independentPercentages(rate,"1405/01/01",rows+"\n","fixture",true).exact.toString().equals(total.exact.toString()));
        System.out.println("CalculationBodyTest: "+checks+" checks passed");
    }
}
