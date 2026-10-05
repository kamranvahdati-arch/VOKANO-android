package ir.kamranvahdati.lawoffice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure Java technical fixtures, NOT legally verified golden cases. */
public final class CalculationFoundationSmokeTest {
    private static int checks;
    public static void main(String[] args) {
        numbers(); fractions(); references(); snapshots();
        System.out.println("Calculation foundation: " + checks + " assertions passed; no legal rule pack verified.");
    }

    private static void numbers() {
        check(CalculationArithmetic.money("۲۱٬۰۰۰٬۰۰۰٬۰۰۰", CalculationArithmetic.Currency.RIAL) == 21000000000L, "Persian large amount");
        check(CalculationArithmetic.money("٢٬١٠٠٬٠٠٠٬٠٠٠", CalculationArithmetic.Currency.TOMAN) == 21000000000L, "Arabic digits and toman");
        check(CalculationArithmetic.money(" 0 ", CalculationArithmetic.Currency.RIAL) == 0, "zero");
        check(CalculationArithmetic.money("۰٫۱", CalculationArithmetic.Currency.TOMAN) == 1, "exact fractional toman");
        check(CalculationArithmetic.money("9223372036854775807", CalculationArithmetic.Currency.RIAL) == Long.MAX_VALUE, "long boundary");
        for (String bad : Arrays.asList("", "-1", "+1", "1e3", "NaN", "Infinity", "12,34", "1 000", "1,000,", "1.1.1", ".1", "1.", "1/2", "1.0000000000001"))
            reject(() -> CalculationArithmetic.decimal(bad), "invalid number " + bad);
        reject(() -> CalculationArithmetic.decimal(null), "missing number");
        reject(() -> CalculationArithmetic.money("9223372036854775808", CalculationArithmetic.Currency.RIAL), "rial overflow");
        reject(() -> CalculationArithmetic.money("922337203685477581", CalculationArithmetic.Currency.TOMAN), "toman overflow");
        reject(() -> CalculationArithmetic.money("0.1", CalculationArithmetic.Currency.RIAL), "fractional rial");
        reject(() -> CalculationArithmetic.money("1", null), "missing unit");
        reject(() -> CalculationArithmetic.percent("100.01"), "percentage range");
        reject(() -> CalculationArithmetic.percent("-1"), "negative percentage");
        check(CalculationArithmetic.percent("0").toString().equals("0/1"), "zero percentage");
        check(CalculationArithmetic.percent("۱۰۰").toString().equals("1/1"), "full percentage");
        check(CalculationArithmetic.display(1234567891, CalculationArithmetic.Currency.TOMAN).equals("123,456,789.1 تومان"), "toman display preserves rial");
        check(CalculationArithmetic.display(21000000000L, CalculationArithmetic.Currency.RIAL).equals("21,000,000,000 ریال"), "grouping and rial");
    }

    private static void fractions() {
        CalculationArithmetic.Fraction third = CalculationArithmetic.Fraction.of(1, 3);
        check(third.add(third).add(third).roundedRials(RoundingMode.UNNECESSARY) == 1, "no intermediate rounding");
        check(CalculationArithmetic.Fraction.of(Long.MAX_VALUE, 1).multiply(third)
                .multiply(CalculationArithmetic.Fraction.of(3, 1)).roundedRials(RoundingMode.UNNECESSARY) == Long.MAX_VALUE,
                "intermediate product wider than long");
        check(CalculationArithmetic.Fraction.of(5, 2).roundedRials(RoundingMode.HALF_UP) == 3, "half up");
        check(CalculationArithmetic.Fraction.of(5, 2).roundedRials(RoundingMode.HALF_EVEN) == 2, "explicit alternate rounding");
        check(CalculationArithmetic.Fraction.decimal(new BigDecimal("1E+3")).toString().equals("1000/1"), "exact decimal exponent representation");
        check(CalculationArithmetic.Fraction.of(21000000000L, 1).multiply(CalculationArithmetic.percent("۱٫۵"))
                .roundedRials(RoundingMode.UNNECESSARY) == 315000000, "owner-provided numeric fixture only");
        reject(() -> CalculationArithmetic.Fraction.of(1, 0), "zero denominator");
        reject(() -> CalculationArithmetic.Fraction.of(-1, 2), "negative ratio");
        reject(() -> third.divide(CalculationArithmetic.Fraction.of(0, 1)), "zero divisor");
        reject(() -> third.multiply(null), "missing operand");
        reject(() -> third.roundedRials(RoundingMode.UNNECESSARY), "unnecessary rounding prohibited");
        reject(() -> CalculationArithmetic.Fraction.of(Long.MAX_VALUE, 1).multiply(CalculationArithmetic.Fraction.of(2, 1))
                .roundedRials(RoundingMode.HALF_UP), "final overflow");
        reject(() -> CalculationArithmetic.Fraction.decimal(new BigDecimal("1E+2147483647")), "hostile scale");
    }

    private static CalculationReference rate(String id, int year, String amount, boolean confirmed) {
        return new CalculationReference(id, "diyah-normal", "test-" + id, "", CalculationReference.Kind.ANNUAL_DIYAH,
                year, 0, year + "/01/01", year + "/12/29", amount, "IRR", "Synthetic test fixture", "TEST",
                "", "", "", "Not an official yearly rate", CalculationReference.Status.USER_ENTERED,
                1000, 1000, confirmed, true, false);
    }

    private static void references() {
        CalculationReference old = rate("year-a", 1404, "10000000", true);
        CalculationReference current = rate("year-b", 1405, "21000000000", true);
        List<CalculationReference> data = Arrays.asList(old, current);
        check(CalculationReference.select(data, CalculationReference.Kind.ANNUAL_DIYAH, "diyah-normal", 1404, 0, "1404/07/01") == old, "historical year");
        check(CalculationReference.select(data, CalculationReference.Kind.ANNUAL_DIYAH, "diyah-normal", 1405, 0, "۱۴۰۵/۰۷/۰۱") == current, "current exact year");
        reject(() -> CalculationReference.select(data, CalculationReference.Kind.ANNUAL_DIYAH, "diyah-normal", 1406, 0, "1406/01/01"), "no prior-year fallback");
        reject(() -> CalculationReference.select(Collections.singletonList(rate("unconfirmed", 1405, "10", false)),
                CalculationReference.Kind.ANNUAL_DIYAH, "diyah-normal", 1405, 0, "1405/07/01"), "manual confirmation required");
        reject(() -> CalculationReference.select(Arrays.asList(current, rate("ambiguous", 1405, "20", true)),
                CalculationReference.Kind.ANNUAL_DIYAH, "diyah-normal", 1405, 0, "1405/07/01"), "ambiguous versions");
        reject(() -> current.requireUsable("1405/07/01", 1404, 0, "diyah-normal"), "wrong year");
        reject(() -> current.requireUsable("1405/07/01", 1405, 0, "different-base-year"), "wrong series");
        reject(() -> current.requireUsable("1406/01/01", 1405, 0, "diyah-normal"), "expired effective interval");
        reject(() -> CalculationReference.date("1405/13/01"), "invalid effective date");
        reject(() -> rate("zero", 1405, "0", true), "nonpositive rate");
        reject(() -> new CalculationReference("official", "diyah-normal", "1", "", CalculationReference.Kind.ANNUAL_DIYAH,
                1405, 0, "1405/01/01", "", "21000000000", "IRR", "", "", "", "", "", "",
                CalculationReference.Status.OFFICIAL_VERIFIED, 1000, 1000, false, false, false), "official provenance required");
    }

    private static CalculationSnapshot snapshot(String id, String parent, Map<String,String> inputs,
            List<String> warnings, List<CalculationSnapshot.Override> overrides) {
        return new CalculationSnapshot(id, parent, "برآورد آزمایشی", null, CalculationSnapshot.Type.ARSH_ESTIMATE,
                CalculationSnapshot.ENGINE_VERSION, "unverified-test-fixture", "fixture-1", "1405/07/14", "1405/07/14",
                inputs, Collections.singletonMap("amount_rial", "1"), Collections.singletonList(rate("snapshot-rate", 1405, "100", true)),
                Collections.singletonList("100 × 1/100 = 1 IRR (synthetic arithmetic only)"), warnings, overrides,
                RoundingMode.HALF_UP, 2000, 2000);
    }

    private static void snapshots() {
        Map<String,String> inputs = new LinkedHashMap<>(); inputs.put("percent", "1");
        List<String> warnings = new ArrayList<>();
        List<CalculationSnapshot.Override> overrides = new ArrayList<>();
        overrides.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.PERCENTAGE, "percent", "2", "1", 1001, "expert correction"));
        CalculationSnapshot old = snapshot("s1", "", inputs, warnings, overrides);
        inputs.put("percent", "10"); warnings.add("later warning"); overrides.clear();
        check(old.inputSnapshot.get("percent").equals("1"), "input snapshot detached");
        check(old.overrides.size() == 1 && old.overrides.get(0).oldValue.equals("2"), "old override preserved");
        check(old.warnings.size() == 1 && old.warnings.contains(CalculationSnapshot.ARSH_WARNING), "mandatory arsh warning survives snapshot");
        check(old.manualOverride, "manual origin cannot be hidden");
        CalculationSnapshot newer = snapshot("s2", "s1", inputs, warnings, overrides);
        check(newer.previousSnapshotId.equals(old.id) && old.inputSnapshot.get("percent").equals("1"), "recalculation lineage");
        immutable(() -> old.inputSnapshot.put("percent", "99"));
        immutable(() -> old.warnings.clear());
        immutable(() -> old.references.clear());
        immutable(() -> old.overrides.clear());
        reject(() -> snapshot("s1", "s1", inputs, warnings, overrides), "reject overwrite identity");
        reject(() -> new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.DATE, "date", "a", "a", 1001, ""), "audit actual changes only");
    }

    private static void check(boolean value, String label) { checks++; if (!value) throw new AssertionError(label); }
    private static void reject(Runnable runnable, String label) {
        try { runnable.run(); } catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError(label);
    }
    private static void immutable(Runnable runnable) {
        try { runnable.run(); } catch (UnsupportedOperationException expected) { checks++; return; }
        throw new AssertionError("Snapshot unexpectedly mutable");
    }
}
