package ir.kamranvahdati.lawoffice;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
public final class CalculationDelayPackTest {
    static int checks;
    static void check(boolean ok){checks++;if(!ok)throw new AssertionError("check "+checks);}
    static void rejects(Runnable r){try{r.run();throw new AssertionError("accepted invalid input");}catch(IllegalArgumentException expected){checks++;}}
    public static void main(String[] args)throws Exception{
        CalculationDelayPack p;try(Reader r=new InputStreamReader(new FileInputStream("app/src/main/assets/calculation/delay-indices-1399-1401.properties"),StandardCharsets.UTF_8)){p=new CalculationDelayPack(r);}
        check(p.references.size()==36);
        check(p.at("۱۳۹۹/۰۱/۰۱").value.equals("229.9"));check(p.at("1401/12/29").value.equals("794.3"));
        check(p.at("1399/12/30").value.equals("374.2"));
        rejects(()->p.at("1400/12/30"));rejects(()->p.at("1402/01/01"));rejects(()->p.at("1398/12/29"));
        for(CalculationReference ref:p.references){check(!ref.manuallyEntered&&ref.status==CalculationReference.Status.REVIEWED_PUBLICATION);ref.requireUsable(ref.effectiveTo,ref.year,ref.month,CalculationDelayPack.SERIES);}
        CalculationDelayMath.Result r=CalculationDelayMath.calculate(CalculationDelayMath.Mode.ORDINARY_DEBT,2299000,"1399/01/01","1401/12/29","reviewed case",CalculationDelayPack.SERIES,p.at("1399/01/01"),p.at("1401/12/29"),RoundingMode.HALF_UP);
        check(r.adjustedRials==7943000&&r.damagesRials==5644000);
        check(!r.usesManualIndex);
        for(CalculationDelayMath.Mode mode:CalculationDelayMath.Mode.values()){
            check(CalculationDelayPack.validateScope(mode,"1399/01/01","1399/02/01","1399/01/01","1400/01/01","basis",true,true,true,true).equals("basis"));
            rejects(()->CalculationDelayPack.validateScope(mode,"1399/01/01","1399/02/01","1399/01/01","1400/01/01","basis",true,true,true,false));
            rejects(()->CalculationDelayPack.validateScope(mode,"1399/01/01","1399/02/01","1399/01/01","1400/01/01","",true,true,true,true));
        }
        rejects(()->CalculationDelayPack.validateScope(CalculationDelayMath.Mode.ORDINARY_DEBT,"1399/01/01","","1399/01/01","1400/01/01","basis",true,true,true,true));
        rejects(()->CalculationDelayPack.validateScope(CalculationDelayMath.Mode.ORDINARY_DEBT,"1399/01/01","1401/01/01","1399/01/01","1400/01/01","basis",true,true,true,true));
        rejects(()->CalculationDelayPack.validateScope(CalculationDelayMath.Mode.ORDINARY_DEBT,"1399/01/01","1399/01/01","1399/01/01","1400/01/01","basis",true,false,true,true));
        rejects(()->CalculationDelayPack.validateScope(CalculationDelayMath.Mode.CHEQUE,"1399/01/01","","1399/02/01","1400/01/01","basis",true,true,true,true));
        rejects(()->CalculationDelayPack.validateScope(CalculationDelayMath.Mode.CHEQUE,"1399/01/01","","1399/01/01","1400/01/01","basis",true,true,false,true));
        try{new CalculationDelayPack(new StringReader("1399=1"));throw new AssertionError("incomplete pack");}catch(IllegalArgumentException expected){checks++;}
        System.out.println("Delay historical pack and scope: "+checks+" checks PASS");
    }
}
