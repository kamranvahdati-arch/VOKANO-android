package ir.kamranvahdati.lawoffice;
import java.io.*;import java.nio.file.*;import java.math.RoundingMode;
public final class CalculationCurrentIndicesTest {
 static int count;static void check(boolean b){count++;if(!b)throw new AssertionError("current index check "+count);}
 static void reject(Runnable r){count++;try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("expected rejection "+count);}
 public static void main(String[] args)throws Exception{
  CalculationCurrentIndices p=new CalculationCurrentIndices(Files.newBufferedReader(Paths.get("app/src/main/assets/calculation/cbi-base1400-1405-06-v1.properties")));
  CalculationDelayPack old=new CalculationDelayPack(Files.newBufferedReader(Paths.get("app/src/main/assets/calculation/delay-indices-1399-1401.properties")));
  check(p.references.size()==66);check(p.at("۱۴۰۰/۰۱/۰۱").value.equals("83.3"));check(p.at("1405/06/31").value.equals("800.0"));
  check(p.at("1404/12/29").value.equals("575.0"));check(p.at("1403/12/30").value.equals("354.8"));
  for(CalculationReference r:p.references){check(r.status==CalculationReference.Status.OFFICIAL_VERIFIED&&!r.manuallyEntered);r.requireUsable(r.effectiveTo,r.year,r.month,CalculationCurrentIndices.SERIES);}
  reject(()->p.at("1399/12/30"));reject(()->p.at("1405/07/01"));reject(()->p.at("1406/01/01"));
  for(CalculationDelayMath.Mode mode:CalculationDelayMath.Mode.values()){
   CalculationDelayMath.Result r=CalculationDelayMath.calculate(mode,6153000,"1405/01/01","1405/06/31","reviewed fixture",CalculationCurrentIndices.SERIES,p.at("1405/01/01"),p.at("1405/06/31"),RoundingMode.HALF_UP);
   check(r.adjustedRials==8000000&&r.damagesRials==1847000);check(!r.usesManualIndex);
  }
  // Same numeric year is not permission to mix baskets or rebase old snapshots.
  reject(()->CalculationDelayMath.calculate(CalculationDelayMath.Mode.ORDINARY_DEBT,1000000,"1400/01/01","1405/06/31","fixture",CalculationCurrentIndices.SERIES,old.at("1400/01/01"),p.at("1405/06/31"),RoundingMode.HALF_UP));
  check(old.at("1400/01/01").value.equals("378.3"));
  CalculationDelayMath.Result same=CalculationDelayMath.calculate(CalculationDelayMath.Mode.CHEQUE,123456,"1405/06/01","1405/06/31","fixture",CalculationCurrentIndices.SERIES,p.at("1405/06/01"),p.at("1405/06/31"),RoundingMode.HALF_UP);check(same.damagesRials==0);
  System.out.println("CalculationCurrentIndicesTest: "+count+" checks PASS");
 }
}
