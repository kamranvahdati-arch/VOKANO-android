package ir.kamranvahdati.lawoffice;

import java.math.RoundingMode;

/** Synthetic index fixtures, not official annual index data or entitlement goldens. */
public final class CalculationDelaySmokeTest {
    private static int assertions;
    static CalculationReference index(String id, int month, String value, String series,
            boolean confirmed) {
        return new CalculationReference(id, series, "1", "", CalculationReference.Kind.ECONOMIC_INDEX,
                1405, month, "1405/01/01", "1405/12/29", value, "INDEX",
                "Synthetic test data", "TEST", "", "", "", "Not official data",
                CalculationReference.Status.USER_ENTERED, 1, 1, confirmed, true, false);
    }
    static CalculationDelayMath.Result calculate(CalculationDelayMath.Mode mode, long principal,
            String start, String end, CalculationReference first, CalculationReference last,
            RoundingMode rounding) {
        return CalculationDelayMath.calculate(mode, principal, start, end, "Test reviewed basis",
                "synthetic-annual", first, last, rounding);
    }
    static void check(boolean condition) {
        assertions++;
        if (!condition) throw new AssertionError("Check " + assertions);
    }
    static void rejects(Runnable action) {
        assertions++;
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected rejection " + assertions);
    }
    public static void main(String[] args) {
        CalculationReference first = index("a", 1, "100", "synthetic-annual", true);
        CalculationReference last = index("b", 2, "150", "synthetic-annual", true);
        for (CalculationDelayMath.Mode mode : CalculationDelayMath.Mode.values()) {
            CalculationDelayMath.Result r = calculate(mode, 1000000, "۱۴۰۵/۱/۱", "1405/02/01",
                    first, last, RoundingMode.HALF_UP);
            check(r.mode == mode);
            check(r.principalRials == 1000000 && r.adjustedRials == 1500000 && r.damagesRials == 500000);
            check(r.usesManualIndex && r.startIndex == first && r.paymentIndex == last);
            check(r.exactAdjustedRials.equals("1500000/1"));
            check(r.startDate.equals("1405/01/01") && r.startDateBasis.equals("Test reviewed basis"));
        }
        CalculationDelayMath.Mode mode = CalculationDelayMath.Mode.ORDINARY_DEBT;
        check(calculate(mode, 9, "1405/01/01", "1405/01/20", first, first,
                RoundingMode.UNNECESSARY).damagesRials == 0);
        CalculationReference three = index("c", 1, "3", "synthetic-annual", true);
        CalculationReference four = index("d", 2, "4", "synthetic-annual", true);
        CalculationDelayMath.Result fractional = calculate(mode, 2, "1405/01/01", "1405/02/01",
                three, four, RoundingMode.HALF_UP);
        check(fractional.exactAdjustedRials.equals("8/3") && fractional.adjustedRials == 3
                && fractional.damagesRials == 1);
        check(calculate(mode, 2, "1405/01/01", "1405/02/01", three, four,
                RoundingMode.DOWN).adjustedRials == 2);
        rejects(() -> calculate(mode, 2, "1405/01/01", "1405/02/01", three, four, RoundingMode.UNNECESSARY));
        rejects(() -> calculate(mode, Long.MAX_VALUE, "1405/01/01", "1405/02/01", first, last, RoundingMode.UP));
        rejects(() -> calculate(mode, 0, "1405/01/01", "1405/02/01", first, last, RoundingMode.UP));
        rejects(() -> calculate(mode, -1, "1405/01/01", "1405/02/01", first, last, RoundingMode.UP));
        rejects(() -> calculate(mode, 1, "1405/02/01", "1405/01/01", last, first, RoundingMode.UP));
        rejects(() -> calculate(mode, 1, "1405/01/01", "1405/03/01", first, last, RoundingMode.UP));
        rejects(() -> calculate(mode, 1, "1405/01/01", "1405/02/01", null, last, RoundingMode.UP));
        rejects(() -> calculate(null, 1, "1405/01/01", "1405/02/01", first, last, RoundingMode.UP));
        rejects(() -> calculate(mode, 1, "1405/01/01", "1405/02/01", first,
                index("x", 2, "150", "wrong-series", true), RoundingMode.UP));
        rejects(() -> calculate(mode, 1, "1405/01/01", "1405/02/01", first,
                index("x", 2, "150", "synthetic-annual", false), RoundingMode.UP));
        rejects(() -> calculate(mode, 1, "1405/01/01", "1405/02/01", first,
                index("x", 2, "99", "synthetic-annual", true), RoundingMode.UP));
        rejects(() -> calculate(mode, 1, "1405/01/01", "1405/01/20", first,
                index("x", 1, "100", "synthetic-annual", true), RoundingMode.UP));
        rejects(() -> CalculationDelayMath.calculate(mode, 1, "1405/01/01", "1405/02/01", "",
                "synthetic-annual", first, last, RoundingMode.UP));
        System.out.println("CalculationDelaySmokeTest: " + assertions + " assertions passed");
    }
}
