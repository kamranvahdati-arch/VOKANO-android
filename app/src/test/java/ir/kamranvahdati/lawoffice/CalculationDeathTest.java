package ir.kamranvahdati.lawoffice;
import java.nio.file.*;
import java.math.RoundingMode;
public final class CalculationDeathTest {
 static int count;static CalculationDeath.Rules rules;static CalculationReference rate;
 static void check(boolean b){count++;if(!b)throw new AssertionError("death check "+count);}
 static void reject(Runnable r){count++;try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("expected rejection "+count);}
 static CalculationDeath.Result run(String pct,CalculationDeath.Finding... f){return CalculationDeath.calculate(rules,rate,"1405/01/01","1405/01/02","1405/01/03",pct,"verified fixture",true,f[0],f[1],f[2],f[3]);}
 public static void main(String[] args)throws Exception{
  rules=new CalculationDeath.Rules(Files.newBufferedReader(Paths.get(args[0])));rate=CalculationBodyMath.readRate(Files.newBufferedReader(Paths.get(args[1])));
  CalculationDeath.Finding Y=CalculationDeath.Finding.YES,N=CalculationDeath.Finding.NO,U=CalculationDeath.Finding.UNKNOWN;
  check(run("100",Y,Y,N,N).total.roundedRials(RoundingMode.HALF_UP)==28000000000L);
  check(run("50",N,N,Y,Y).total.roundedRials(RoundingMode.HALF_UP)==14000000000L);
  check(run("100",Y,Y,Y,Y).additional.roundedRials(RoundingMode.HALF_UP)==7000000000L);
  check(run("100",N,N,N,N).total.roundedRials(RoundingMode.HALF_UP)==21000000000L);
  // Independent exhaustive oracle: expand unknown findings to all possible facts.
  // A single monetary answer is valid only if all compatible fact assignments agree.
  for(CalculationDeath.Finding a:CalculationDeath.Finding.values())for(CalculationDeath.Finding b:CalculationDeath.Finding.values())for(CalculationDeath.Finding c:CalculationDeath.Finding.values())for(CalculationDeath.Finding d:CalculationDeath.Finding.values()){
   CalculationDeath.Finding[] f={a,b,c,d};java.util.Set<Boolean> answers=new java.util.HashSet<>();
   for(int mask=0;mask<16;mask++){boolean match=true;for(int i=0;i<4;i++)if(f[i]!=U&&((mask&(1<<i))!=0)!=(f[i]==Y))match=false;
    if(match)answers.add((mask&3)==3||(mask&12)==12);}
   if(answers.size()==2)reject(()->run("100",f));else check(run("100",f).enhanced==answers.iterator().next());
  }
  reject(()->CalculationDeath.calculate(rules,rate,"1405/01/03","1405/01/02","1405/01/04","100","basis",true,Y,Y,N,N));
  reject(()->CalculationDeath.calculate(rules,rate,"1405/01/01","1405/01/03","1405/01/02","100","basis",true,Y,Y,N,N));
  reject(()->CalculationDeath.calculate(rules,rate,"1405/01/01","1405/01/02","1405/01/03","100","basis",false,Y,Y,N,N));
  reject(()->CalculationDeath.calculate(rules,rate,"1404/01/01","1404/01/02","1404/01/03","100","basis",true,Y,Y,N,N));
  reject(()->run("0",Y,Y,N,N));reject(()->run("101",Y,Y,N,N));
  reject(()->CalculationDeath.calculate(rules,rate,"1405/01/01","1405/01/02","1405/01/03","100","",true,Y,Y,N,N));
  check(run("۰٫۱",Y,Y,N,N).total.roundedRials(RoundingMode.HALF_UP)==28000000L);
  System.out.println("CalculationDeathTest: "+count+" assertions passed (81 complete/unknown fact combinations)");
 }
}
