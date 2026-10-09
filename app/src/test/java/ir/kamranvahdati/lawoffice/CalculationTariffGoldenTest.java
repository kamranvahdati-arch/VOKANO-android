package ir.kamranvahdati.lawoffice;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.RoundingMode;

/** Expected amounts independently derived from the source article register, not
 * an online calculator. Limited legal goldens, NOT certification of all tariffs.
 */
public final class CalculationTariffGoldenTest {
    private static int assertions;
    private static final String BASIS = "اختبار: ماده ۹، غیرقطعی از حیث بها، بدون عوامل خاص";
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;
    private static void check(boolean condition) {
        assertions++; if (!condition) throw new AssertionError("Tariff check " + assertions);
    }
    private static void rejects(Runnable action) {
        assertions++; try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected rejection " + assertions);
    }
    private static CalculationTariff.Result financial(CalculationTariff.Rules r, long claim, CalculationTariff.Stage s) {
        return CalculationTariff.financial(r, claim, s, BASIS, false, ROUND);
    }
    private static CalculationTariff.Result range(CalculationTariff.Rules r, String id, long value,
            String quantity, CalculationTariff.Stage stage, boolean prosecutor, boolean finalTrial) {
        return CalculationTariff.ranged(r, id, value, quantity, stage, prosecutor, finalTrial, "انتخاب مستند برای آزمون", ROUND);
    }
    public static void main(String[] args) throws Exception {
        String data = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        CalculationTariff.Rules r = new CalculationTariff.Rules(new StringReader(data));
        // Each row: claim, article-9 whole, article-21 first, appeal. Hand sums in docs.
        long[][] goldens = {{500000000L,40000000,24000000,16000000},
            {2000000000L,145000000,87000000,58000000},
            {10000000000L,545000000,327000000,218000000},
            {30000000000L,1345000000,807000000,538000000},
            {31000000000L,1375000000,825000000,550000000}};
        for (long[] g : goldens) {
            check(financial(r,g[0],CalculationTariff.Stage.WHOLE).rials == g[1]);
            check(financial(r,g[0],CalculationTariff.Stage.FIRST).rials == g[2]);
            check(financial(r,g[0],CalculationTariff.Stage.APPEAL).rials == g[3]);
            check(financial(r,g[0],CalculationTariff.Stage.CIVIL_CASSATION).rials == g[3]);
        }
        // A rial on either side must use its own marginal band, without intermediate rounding.
        check(financial(r,499999999,CalculationTariff.Stage.WHOLE).exactRials.equals("999999998/25"));
        check(financial(r,500000001,CalculationTariff.Stage.WHOLE).exactRials.equals("4000000007/100"));
        check(financial(r,2000000001L,CalculationTariff.Stage.WHOLE).exactRials.equals("2900000001/20"));
        check(financial(r,10000000001L,CalculationTariff.Stage.WHOLE).exactRials.equals("13625000001/25"));
        check(financial(r,30000000001L,CalculationTariff.Stage.WHOLE).exactRials.equals("134500000003/100"));
        check(financial(r,Long.MAX_VALUE,CalculationTariff.Stage.WHOLE).rials > 0);
        CalculationTariff.Result details = financial(r,1,CalculationTariff.Stage.FIRST);
        check(details.exactRials.equals("6/125") && details.rials == 0);
        check(details.ruleVersion.equals(r.version) && details.sourceUrl.equals(r.sourceUrl));
        check(details.selectedBasis.equals(BASIS) && details.steps.size() == 2);
        try { details.steps.clear(); throw new AssertionError("mutable steps"); }
        catch (UnsupportedOperationException expected) { assertions++; }
        rejects(()->financial(r,0,CalculationTariff.Stage.WHOLE));
        rejects(()->financial(r,-1,CalculationTariff.Stage.WHOLE));
        rejects(()->financial(r,1,CalculationTariff.Stage.PROSECUTOR));
        check(CalculationTariff.financial(r,1,CalculationTariff.Stage.WHOLE,BASIS,true,ROUND).exactRials.equals("1/10"));
        rejects(()->CalculationTariff.financial(r,1,CalculationTariff.Stage.WHOLE,"",false,ROUND));
        rejects(()->financial(null,1,CalculationTariff.Stage.WHOLE));
        check(range(r,"FAMILY",5000000,"1",CalculationTariff.Stage.FIRST,true,false).rials == 3000000);
        check(range(r,"NONFINANCIAL",10000000,"1",CalculationTariff.Stage.APPEAL,true,false).rials == 4000000);
        check(range(r,"ADMINISTRATIVE",500000000,"1",CalculationTariff.Stage.APPEAL,true,false).rials == 200000000);
        check(range(r,"OTHER_FORUM",4000000,"1",CalculationTariff.Stage.FIRST,true,false).rials == 2400000);
        check(range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.PROSECUTOR,true,false).rials == 5000000);
        check(range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.FIRST,true,false).rials == 3000000);
        check(range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.APPEAL,true,false).rials == 2000000);
        check(range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.FIRST,false,false).rials == 8000000);
        check(range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.FIRST,true,true).rials == 5000000);
        check(range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.FIRST,false,true).rials == 10000000);
        check(range(r,"CONSULTATION_HOUR",500000,"۱٫۵",CalculationTariff.Stage.WHOLE,true,false).rials == 750000);
        check(range(r,"FILE_STUDY",2000000,"1",CalculationTariff.Stage.WHOLE,true,false).rials == 2000000);
        check(range(r,"DRAFTING",50000000,"1",CalculationTariff.Stage.WHOLE,true,false).rials == 50000000);
        check(r.ranges.get("DRAFTING").minimum == null);
        check(CalculationTariff.agreedFee("۱۲۳٫۴",CalculationArithmetic.Currency.TOMAN) == 1234);
        check(CalculationTariff.agreedFee("0",CalculationArithmetic.Currency.RIAL) == 0);
        rejects(()->CalculationTariff.agreedFee("-1",CalculationArithmetic.Currency.RIAL));
        rejects(()->range(r,"UNKNOWN",1,"1",CalculationTariff.Stage.WHOLE,true,false));
        rejects(()->range(r,"FAMILY",4999999,"1",CalculationTariff.Stage.WHOLE,true,false));
        rejects(()->range(r,"FAMILY",200000001,"1",CalculationTariff.Stage.WHOLE,true,false));
        rejects(()->range(r,"DRAFTING",-1,"1",CalculationTariff.Stage.WHOLE,true,false));
        rejects(()->range(r,"DRAFTING",1,"1",CalculationTariff.Stage.FIRST,true,false));
        rejects(()->range(r,"DRAFTING",1,"2",CalculationTariff.Stage.WHOLE,true,false));
        rejects(()->range(r,"CONSULTATION_HOUR",500000,"0",CalculationTariff.Stage.WHOLE,true,false));
        rejects(()->range(r,"CONSULTATION_HOUR",500000,"999999999999999999999",CalculationTariff.Stage.WHOLE,true,false));
        rejects(()->range(r,"FAMILY",5000000,"1",CalculationTariff.Stage.FIRST,false,false));
        rejects(()->range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.PROSECUTOR,false,false));
        rejects(()->range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.APPEAL,true,true));
        rejects(()->range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.CIVIL_CASSATION,true,false));
        // Synthetic alternate data proves the engine does not bury the 8% band or mutate history.
        CalculationTariff.Rules alternate = new CalculationTariff.Rules(new StringReader(data.replace("500000000:8", "500000000:9")));
        check(financial(alternate,500000000,CalculationTariff.Stage.WHOLE).rials == 45000000);
        check(financial(r,500000000,CalculationTariff.Stage.WHOLE).rials == 40000000);
        try { new CalculationTariff.Rules(new StringReader(data.replace("civil.first=60", "civil.first=61"))); throw new AssertionError("bad shares"); }
        catch (IllegalArgumentException expected) { assertions++; }
        check(CalculationTariff.enforcement(r,200000000,4000000,CalculationTariff.Stage.WHOLE,BASIS,ROUND).rials==4000000);
        check(CalculationTariff.enforcement(r,1000000000,12000000,CalculationTariff.Stage.WHOLE,BASIS,ROUND).rials==12000000);
        check(CalculationTariff.enforcement(r,200000049,4000000,CalculationTariff.Stage.WHOLE,BASIS,ROUND).rials==4000000);
        // A 4,000,000.98 ceiling may not be rounded up to admit 4,000,001.
        rejects(()->CalculationTariff.enforcement(r,200000049,4000001,CalculationTariff.Stage.WHOLE,BASIS,ROUND));
        rejects(()->CalculationTariff.enforcement(r,199999999,4000000,CalculationTariff.Stage.WHOLE,BASIS,ROUND));
        rejects(()->CalculationTariff.enforcement(r,1000000000,20000001,CalculationTariff.Stage.WHOLE,BASIS,ROUND));
        rejects(()->CalculationTariff.enforcement(r,1000000000,3999999,CalculationTariff.Stage.WHOLE,BASIS,ROUND));
        rejects(()->CalculationTariff.enforcement(r,0,4000000,CalculationTariff.Stage.WHOLE,BASIS,ROUND));
        rejects(()->CalculationTariff.enforcement(r,1000000000,4000000,CalculationTariff.Stage.FIRST,BASIS,ROUND));
        rejects(()->CalculationTariff.enforcement(r,1000000000,4000000,CalculationTariff.Stage.WHOLE,"",ROUND));
        check(CalculationTariff.enforcement(r,Long.MAX_VALUE,4000000,CalculationTariff.Stage.WHOLE,BASIS,ROUND).rials==4000000);
        CalculationTariff.Rules old = new CalculationTariff.Rules(new StringReader(data.replace("enforcement.minimum=4000000", "").replace("enforcement.maximum.percent=2", "")));
        check(financial(old,500000000,CalculationTariff.Stage.WHOLE).rials==40000000);
        rejects(()->CalculationTariff.enforcement(old,1000000000,4000000,CalculationTariff.Stage.WHOLE,BASIS,ROUND));
        CalculationTariff.Result firstStage=financial(r,500000000,CalculationTariff.Stage.FIRST);
        CalculationTariff.Result appealStage=financial(r,500000000,CalculationTariff.Stage.APPEAL);
        check(CalculationTariff.disposition(r,firstStage,"FINANCIAL",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE).rials==6000000);
        check(CalculationTariff.disposition(r,firstStage,"FINANCIAL",CalculationTariff.Disposition.REJECT_AFTER_DEFENSE).rials==12000000);
        check(CalculationTariff.disposition(r,appealStage,"FINANCIAL",CalculationTariff.Disposition.APPEAL_DROP_BEFORE_DEFENSE).rials==4000000);
        check(CalculationTariff.disposition(r,appealStage,"FINANCIAL",CalculationTariff.Disposition.APPEAL_DROP_AFTER_DEFENSE).rials==8000000);
        check(CalculationTariff.disposition(r,firstStage,"FINANCIAL",CalculationTariff.Disposition.ORDINARY)==firstStage);
        // 31 * .08 * .60 / 4 = .372 => 0, not round(1.488)/4.
        check(CalculationTariff.disposition(r,financial(r,31,CalculationTariff.Stage.FIRST),"FINANCIAL",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE).rials==0);
        // 41*.08*.6/4=.492 => 0; rounding the stage first would incorrectly give 1.
        check(CalculationTariff.disposition(r,financial(r,41,CalculationTariff.Stage.FIRST),"FINANCIAL",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE).rials==0);
        rejects(()->CalculationTariff.disposition(r,firstStage,"FINANCIAL",CalculationTariff.Disposition.APPEAL_DROP_AFTER_DEFENSE));
        rejects(()->CalculationTariff.disposition(r,appealStage,"FINANCIAL",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE));
        rejects(()->CalculationTariff.disposition(r,firstStage,"ADMINISTRATIVE",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE));
        rejects(()->CalculationTariff.disposition(r,firstStage,"CRIMINAL_TWO_GRADE6",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE));
        rejects(()->CalculationTariff.disposition(r,financial(r,1,CalculationTariff.Stage.WHOLE),"FINANCIAL",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE));
        CalculationTariff.Rules v2=new CalculationTariff.Rules(Files.newBufferedReader(Paths.get("app/src/main/assets/calculation/tariff-1398-reviewed-v2.properties"),StandardCharsets.UTF_8));
        rejects(()->CalculationTariff.disposition(v2,financial(v2,500000000,CalculationTariff.Stage.FIRST),"FINANCIAL",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE));
        rejects(()->CalculationTariff.disposition(v2,firstStage,"FINANCIAL",CalculationTariff.Disposition.ANNUL_BEFORE_DEFENSE));
        check(financial(v2,500000000,CalculationTariff.Stage.FIRST).rials==24000000);
        // Source goldens: whole civil 40m, per-counsel 40m/3, specialty 44m.
        CalculationTariff.Result whole=financial(r,500000000,CalculationTariff.Stage.WHOLE);
        check(CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.SETTLEMENT,1,BASIS,CalculationTariff.Disposition.ORDINARY).rials==40000000);
        check(CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.EQUAL_COUNSEL,3,BASIS,CalculationTariff.Disposition.ORDINARY).exactRials.equals("40000000/3"));
        check(CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.EQUAL_COUNSEL,3,BASIS,CalculationTariff.Disposition.ORDINARY).rials==13333333);
        check(CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.CERTIFIED_SPECIALTY,1,BASIS,CalculationTariff.Disposition.ORDINARY).rials==44000000);
        CalculationTariff.Result criminal=range(r,"CRIMINAL_TWO_GRADE6",10000000,"1",CalculationTariff.Stage.FIRST,true,false);
        check(CalculationTariff.special(r,criminal,"CRIMINAL_TWO_GRADE6",CalculationTariff.Special.MULTIPLE_CHARGES,3,BASIS,CalculationTariff.Disposition.ORDINARY).rials==4200000);
        for(int n=2;n<=20;n++) {
            CalculationTariff.Result result=CalculationTariff.special(r,criminal,"CRIMINAL_TWO_GRADE6",CalculationTariff.Special.MULTIPLE_CHARGES,n,BASIS,CalculationTariff.Disposition.ORDINARY);
            check(result.rials==3000000L+600000L*(n-1));
        }
        // 32*.08/2=1.28 rounds to 1, whereas premature base rounding gives 1.5 => 2.
        check(CalculationTariff.special(r,financial(r,32,CalculationTariff.Stage.WHOLE),"FINANCIAL",CalculationTariff.Special.EQUAL_COUNSEL,2,BASIS,CalculationTariff.Disposition.ORDINARY).rials==1);
        rejects(()->CalculationTariff.special(r,firstStage,"FINANCIAL",CalculationTariff.Special.SETTLEMENT,1,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.EQUAL_COUNSEL,1,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.MULTIPLE_CHARGES,2,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.CERTIFIED_SPECIALTY,2,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.CERTIFIED_SPECIALTY,1,"",CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.EQUAL_COUNSEL,2,BASIS,CalculationTariff.Disposition.REJECT_AFTER_DEFENSE));
        rejects(()->CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.NONE,0,"",CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,range(r,"FILE_STUDY",2000000,"1",CalculationTariff.Stage.WHOLE,true,false),"FILE_STUDY",CalculationTariff.Special.SETTLEMENT,1,BASIS,CalculationTariff.Disposition.ORDINARY));
        CalculationTariff.Rules v3=new CalculationTariff.Rules(Files.newBufferedReader(Paths.get("app/src/main/assets/calculation/tariff-1398-reviewed-v3.properties"),StandardCharsets.UTF_8));
        rejects(()->CalculationTariff.special(v3,financial(v3,500000000,CalculationTariff.Stage.WHOLE),"FINANCIAL",CalculationTariff.Special.CERTIFIED_SPECIALTY,1,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(v3,whole,"FINANCIAL",CalculationTariff.Special.NONE,1,"",CalculationTariff.Disposition.ORDINARY));
        check(CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.NONE,1,"",CalculationTariff.Disposition.ORDINARY)==whole);
        // Article 9 final-by-value: 10% with no second 60% stage reduction.
        for(CalculationTariff.Stage stage:new CalculationTariff.Stage[]{CalculationTariff.Stage.WHOLE,CalculationTariff.Stage.FIRST}) {
            check(CalculationTariff.financial(r,500000000,stage,"explicit final-by-value fixture",true,ROUND).rials==50000000);
            check(CalculationTariff.financial(r,15,stage,BASIS,true,ROUND).rials==2);
        }
        rejects(()->CalculationTariff.financial(r,500000000,CalculationTariff.Stage.APPEAL,BASIS,true,ROUND));
        rejects(()->CalculationTariff.financial(r,500000000,CalculationTariff.Stage.CIVIL_CASSATION,BASIS,true,ROUND));
        rejects(()->CalculationTariff.financial(r,0,CalculationTariff.Stage.FIRST,BASIS,true,ROUND));
        rejects(()->CalculationTariff.financial(r,1,CalculationTariff.Stage.FIRST,"",true,ROUND));
        check(CalculationTariff.financial(r,Long.MAX_VALUE,CalculationTariff.Stage.FIRST,BASIS,true,ROUND).rials==922337203685477581L);
        CalculationTariff.Rules v4=new CalculationTariff.Rules(Files.newBufferedReader(Paths.get("app/src/main/assets/calculation/tariff-1398-reviewed-v4.properties"),StandardCharsets.UTF_8));
        rejects(()->CalculationTariff.financial(v4,500000000,CalculationTariff.Stage.FIRST,BASIS,true,ROUND));
        check(financial(v4,500000000,CalculationTariff.Stage.FIRST).rials==24000000);
        // Article 8: twice the category minimum with the existing stage assignment.
        CalculationTariff.Result minFamily=range(r,"FAMILY",5000000,"1",CalculationTariff.Stage.FIRST,true,false);
        check(CalculationTariff.special(r,minFamily,"FAMILY",CalculationTariff.Special.APPOINTED_AID,1,BASIS,CalculationTariff.Disposition.ORDINARY).rials==6000000);
        CalculationTariff.Result minCriminal=range(r,"CRIMINAL_TWO_GRADE6",5000000,"1",CalculationTariff.Stage.FIRST,false,true);
        check(CalculationTariff.special(r,minCriminal,"CRIMINAL_TWO_GRADE6",CalculationTariff.Special.APPOINTED_AID,1,BASIS,CalculationTariff.Disposition.ORDINARY).rials==10000000);
        rejects(()->CalculationTariff.special(r,range(r,"FAMILY",10000000,"1",CalculationTariff.Stage.FIRST,true,false),"FAMILY",CalculationTariff.Special.APPOINTED_AID,1,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,whole,"FINANCIAL",CalculationTariff.Special.APPOINTED_AID,1,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,minFamily,"FAMILY",CalculationTariff.Special.APPOINTED_AID,1,"",CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(r,minFamily,"FAMILY",CalculationTariff.Special.APPOINTED_AID,2,BASIS,CalculationTariff.Disposition.ORDINARY));
        rejects(()->CalculationTariff.special(v4,range(v4,"FAMILY",5000000,"1",CalculationTariff.Stage.FIRST,true,false),"FAMILY",CalculationTariff.Special.APPOINTED_AID,1,BASIS,CalculationTariff.Disposition.ORDINARY));
        check(CalculationTariff.disposition(r,firstStage,"FINANCIAL",CalculationTariff.Disposition.NONHEARING_OR_RETRIAL_REFUSAL).rials==24000000);
        check(CalculationTariff.disposition(r,appealStage,"FINANCIAL",CalculationTariff.Disposition.NONHEARING_OR_RETRIAL_REFUSAL).rials==16000000);
        rejects(()->CalculationTariff.disposition(r,whole,"FINANCIAL",CalculationTariff.Disposition.NONHEARING_OR_RETRIAL_REFUSAL));
        rejects(()->CalculationTariff.disposition(v4,financial(v4,500000000,CalculationTariff.Stage.FIRST),"FINANCIAL",CalculationTariff.Disposition.NONHEARING_OR_RETRIAL_REFUSAL));
        System.out.println("CalculationTariffGoldenTest: " + assertions + " assertions passed (limited tariff coverage)");
    }
}
