package ir.kamranvahdati.lawoffice;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Numerical layer only. Does not determine entitlement or choose the legal start date.
 * Basis: ruling 850, 1403/05/16, official Dotic publication 17037.
 * No runtime index series or legal eligibility rule pack is bundled here.
 */
final class CalculationDelayMath {
    static final String ENGINE_VERSION = "delay-math-1";
    enum Mode { ORDINARY_DEBT, CHEQUE }

    static final class Result {
        final Mode mode;
        final long principalRials, adjustedRials, damagesRials;
        final String startDate, paymentDate, startDateBasis, exactAdjustedRials;
        final CalculationReference startIndex, paymentIndex;
        final RoundingMode rounding;
        final boolean usesManualIndex;

        private Result(Mode mode, long principal, long adjusted, String start, String payment,
                String basis, CalculationArithmetic.Fraction exact, CalculationReference first,
                CalculationReference last, RoundingMode rounding) {
            this.mode = mode;
            principalRials = principal; adjustedRials = adjusted;
            damagesRials = adjusted - principal;
            startDate = start; paymentDate = payment; startDateBasis = basis;
            exactAdjustedRials = exact.toString(); startIndex = first; paymentIndex = last;
            this.rounding = rounding;
            usesManualIndex = first.manuallyEntered || last.manuallyEntered;
        }
    }

    /** The caller supplies a legally reviewed date and records its basis separately per mode.
     * Missing/unreviewed legal conditions must be blocked by the future eligibility layer.
     */
    static Result calculate(Mode mode, long principalRials, String reviewedStartDate,
            String paymentDate, String startDateBasis, String expectedAnnualSeries,
            CalculationReference first, CalculationReference last, RoundingMode rounding) {
        if (mode == null || first == null || last == null || rounding == null)
            throw invalid("نوع محاسبه، شاخص‌ها و روش گرد کردن الزامی است");
        if (principalRials <= 0) throw invalid("اصل دین باید مثبت باشد");
        String start = CalculationReference.date(reviewedStartDate);
        String payment = CalculationReference.date(paymentDate);
        String basis = CalculationReference.required(startDateBasis, "مستند انتخاب مبدأ");
        String series = CalculationReference.required(expectedAnnualSeries, "سری شاخص سالانه");
        if (payment.compareTo(start) < 0) throw invalid("تاریخ پرداخت پیش از مبدأ است");
        validateIndex(first, start, series);
        validateIndex(last, payment, series);
        if (!first.unit.equals(last.unit)) throw invalid("واحد شاخص‌ها یکسان نیست");
        if (first.year == last.year && first.month == last.month && !first.id.equals(last.id))
            throw invalid("برای یک ماه باید همان نسخه شاخص انتخاب شود");
        BigDecimal a = CalculationArithmetic.decimal(first.value);
        BigDecimal b = CalculationArithmetic.decimal(last.value);
        // No invented negative-damages or zero-clamping policy for a declining index.
        if (b.compareTo(a) < 0) throw invalid("کاهش شاخص نیازمند بررسی حقوقی مستقل است");
        CalculationArithmetic.Fraction exact = CalculationArithmetic.Fraction.of(principalRials, 1)
                .multiply(CalculationArithmetic.Fraction.decimal(b))
                .divide(CalculationArithmetic.Fraction.decimal(a));
        long adjusted = exact.roundedRials(rounding);
        return new Result(mode, principalRials, adjusted, start, payment, basis,
                exact, first, last, rounding);
    }

    private static void validateIndex(CalculationReference ref, String date, String series) {
        if (ref.kind != CalculationReference.Kind.ECONOMIC_INDEX)
            throw invalid("مبنای انتخاب‌شده شاخص اقتصادی نیست");
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        ref.requireUsable(date, year, month, series);
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }
}
