package ir.kamranvahdati.lawoffice;
import java.nio.file.*;
import java.io.StringReader;
import java.math.RoundingMode;
public final class CalculationPrescribedBodyTest {
 static int count;
 static void check(boolean b){count++;if(!b)throw new AssertionError("prescribed check "+count);}
 static void reject(Runnable r){count++;try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("expected rejection "+count);}
 public static void main(String[] args)throws Exception{
  String raw=new String(Files.readAllBytes(Paths.get(args[0])),java.nio.charset.StandardCharsets.UTF_8);
  CalculationPrescribedBody.Rules rules=new CalculationPrescribedBody.Rules(new StringReader(raw));
  CalculationReference rate=CalculationBodyMath.readRate(Files.newBufferedReader(Paths.get(args[1])));
  // Source-derived percentages of 21,000,000,000, independently expanded per row.
  String[] ids={"HARISA","DAMIYA","MUTALAHIMA","SIMHAQ","MUDIHA","HASHIMA","MUNAQQILA"};
  long[] expected={210000000L,420000000L,630000000L,840000000L,1050000000L,2100000000L,3150000000L};
  for(int i=0;i<ids.length;i++)check(CalculationPrescribedBody.calculate(rules,ids[i],rate,"1405/01/01","medical fixture",true).roundedRials(RoundingMode.HALF_UP)==expected[i]);
  reject(()->CalculationPrescribedBody.calculate(rules,"HARISA",rate,"1405/01/01","medical fixture",false));
  reject(()->CalculationPrescribedBody.calculate(rules,"HARISA",rate,"1405/01/01","",true));
  reject(()->CalculationPrescribedBody.calculate(rules,"HARISA",rate,"1404/01/01","medical fixture",true));
  reject(()->CalculationPrescribedBody.calculate(rules,"BODY_WOUND",rate,"1405/01/01","medical fixture",true));
  reject(()->CalculationPrescribedBody.calculate(rules,"MAMUMA",rate,"1405/01/01","medical fixture",true));
  reject(()->CalculationPrescribedBody.calculate(rules,"HARISA",null,"1405/01/01","medical fixture",true));
  try{new CalculationPrescribedBody.Rules(new StringReader(raw.replace("1/100","1/3")));throw new AssertionError("threshold");}catch(IllegalArgumentException expectedFailure){count++;}
  check(rules.serialized.equals(raw));
  System.out.println("CalculationPrescribedBodyTest: "+count+" assertions passed");
 }
}
