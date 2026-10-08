package ir.kamranvahdati.lawoffice;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
public class CalculationDelayExceptionsTest {
    static int checks;static void check(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
    static void rejects(Runnable r){try{r.run();throw new AssertionError("accepted invalid scope");}catch(IllegalArgumentException expected){checks++;}}
    static String end(CalculationDelayExceptions.Route route,String start,String finish,String stop){return CalculationDelayExceptions.indexedEnd(route,CalculationDelayMath.Mode.CHEQUE,start,start,finish,stop,"judgment fixture",true);}
    public static void main(String[] args)throws Exception{
        CalculationDelayExceptions.Route stop=CalculationDelayExceptions.Route.INSOLVENCY_STOP,inst=CalculationDelayExceptions.Route.OVERDUE_INSTALLMENT;
        check(end(stop,"1405/01/01","1405/07/01","1405/05/01").equals("1405/05/01"));
        check(end(stop,"1405/01/01","1405/05/01","1405/06/01").equals("1405/05/01"));
        check(end(stop,"1405/01/01","1405/05/01","1404/12/29").equals("1405/01/01"));
        check(end(stop,"1405/01/01","1405/05/01","1405/01/01").equals("1405/01/01"));
        check(end(stop,"1405/01/01","1405/05/01","1405/05/01").equals("1405/05/01"));
        check(end(inst,"1405/03/01","1405/06/01","1405/02/01").equals("1405/06/01"));
        rejects(()->end(inst,"1405/01/01","1405/06/01","1405/02/01"));
        rejects(()->end(stop,"1405/06/01","1405/05/01","1405/02/01"));
        rejects(()->end(stop,"1405/01/01","1405/06/01",""));
        rejects(()->CalculationDelayExceptions.indexedEnd(inst,CalculationDelayMath.Mode.CHEQUE,"1405/02/01","1405/03/01","1405/06/01","1405/01/01","judgment",true));
        rejects(()->CalculationDelayExceptions.indexedEnd(stop,CalculationDelayMath.Mode.CHEQUE,"1405/01/01","1405/01/01","1405/06/01","1405/03/01","",true));
        rejects(()->CalculationDelayExceptions.indexedEnd(stop,CalculationDelayMath.Mode.CHEQUE,"1405/01/01","1405/01/01","1405/06/01","1405/03/01","judgment",false));
        CalculationCurrentIndices pack=new CalculationCurrentIndices(Files.newBufferedReader(Paths.get("app/src/main/assets/calculation/cbi-base1400-1405-06-v1.properties"),StandardCharsets.UTF_8));
        String effective=end(stop,"1405/01/01","1405/07/01","1405/05/01");
        CalculationDelayMath.Result r=CalculationDelayMath.calculate(CalculationDelayMath.Mode.CHEQUE,6153000,"1405/01/01",effective,"judgment",CalculationCurrentIndices.SERIES,pack.at("1405/01/01"),pack.at(effective),RoundingMode.HALF_UP);
        check(r.adjustedRials==7703000);check(r.damagesRials==1550000);
        r=CalculationDelayMath.calculate(CalculationDelayMath.Mode.CHEQUE,7166000,"1405/03/01",end(inst,"1405/03/01","1405/06/31","1405/02/01"),"instalment",CalculationCurrentIndices.SERIES,pack.at("1405/03/01"),pack.at("1405/06/31"),RoundingMode.HALF_UP);
        check(r.adjustedRials==8000000);check(r.damagesRials==834000);
        System.out.println("CalculationDelayExceptionsTest: "+checks+" checks passed");
    }
}
