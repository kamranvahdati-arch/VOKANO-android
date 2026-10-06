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
        rejects(()->CalculationTariff.financial(r,1,CalculationTariff.Stage.WHOLE,BASIS,true,ROUND));
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
        System.out.println("CalculationTariffGoldenTest: " + assertions + " assertions passed (limited tariff coverage)");
    }
}
