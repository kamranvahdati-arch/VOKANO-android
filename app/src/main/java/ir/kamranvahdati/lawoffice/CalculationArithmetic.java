package ir.kamranvahdati.lawoffice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** Android-independent arithmetic. This class contains no legal rates or legal decisions. */
final class CalculationArithmetic {
    private CalculationArithmetic() { }

    enum Currency { RIAL, TOMAN }

    /** Reject malformed grouping rather than silently changing the amount. */
    static BigDecimal decimal(String input) {
        if (input == null || input.length() > 96) throw invalid("عدد الزامی یا بیش از حد طولانی است");
        StringBuilder normalized = new StringBuilder();
        for (char c : input.trim().toCharArray()) {
            if (c >= '\u06f0' && c <= '\u06f9') c = (char) ('0' + c - '\u06f0');
            else if (c >= '\u0660' && c <= '\u0669') c = (char) ('0' + c - '\u0660');
            else if (c == '\u066c') c = ',';
            else if (c == '\u066b') c = '.';
            normalized.append(c);
        }
        String number = normalized.toString();
        if (!number.matches("(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]{1,12})?"))
            throw invalid("عدد نامعتبر است؛ گروه‌بندی سه‌رقمی و حداکثر ۱۲ رقم اعشار مجاز است");
        return new BigDecimal(number.replace(",", ""));
    }

    /** Stored money is integral rials; a fractional rial is rejected, never silently rounded. */
    static long money(String input, Currency unit) {
        if (unit == null) throw invalid("واحد پول را مشخص کنید");
        BigDecimal value = decimal(input);
        if (unit == Currency.TOMAN) value = value.multiply(BigDecimal.TEN);
        try { return value.longValueExact(); }
        catch (ArithmeticException e) { throw invalid("مبلغ باید ریال صحیح و در محدوده مجاز باشد"); }
    }

    static Fraction percent(String input) {
        BigDecimal value = decimal(input);
        if (value.compareTo(new BigDecimal("100")) > 0) throw invalid("درصد باید بین صفر و صد باشد");
        return Fraction.decimal(value).divide(Fraction.of(100, 1));
    }

    static long nonnegative(long value) {
        if (value < 0) throw invalid("مبلغ منفی مجاز نیست");
        return value;
    }

    static String display(long rials, Currency unit) {
        nonnegative(rials);
        if (unit == null) throw invalid("واحد پول را مشخص کنید");
        BigDecimal value = BigDecimal.valueOf(rials);
        if (unit == Currency.TOMAN) value = value.movePointLeft(1);
        String[] parts = value.stripTrailingZeros().toPlainString().split("\\.");
        String integer = parts[0];
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < integer.length(); i++) {
            if (i > 0 && (integer.length() - i) % 3 == 0) grouped.append(',');
            grouped.append(integer.charAt(i));
        }
        if (parts.length == 2) grouped.append('.').append(parts[1]);
        return grouped + (unit == Currency.RIAL ? " ریال" : " تومان");
    }

    static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }

    /** Exact rational representation prevents premature rounding of thirds and index ratios. */
    static final class Fraction {
        final BigInteger numerator;
        final BigInteger denominator;

        private Fraction(BigInteger numerator, BigInteger denominator) {
            if (numerator == null || denominator == null || numerator.signum() < 0 || denominator.signum() <= 0)
                throw invalid("کسر باید نامنفی و مخرج آن مثبت باشد");
            BigInteger gcd = numerator.gcd(denominator);
            this.numerator = numerator.divide(gcd);
            this.denominator = denominator.divide(gcd);
        }

        static Fraction of(long numerator, long denominator) {
            return new Fraction(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
        }

        static Fraction decimal(BigDecimal value) {
            if (value == null || value.signum() < 0 || Math.abs((long) value.scale()) > 96 || value.precision() > 96)
                throw invalid("مقدار اعشاری خارج از محدوده است");
            if (value.scale() < 0)
                return new Fraction(value.unscaledValue().multiply(BigInteger.TEN.pow(-value.scale())), BigInteger.ONE);
            return new Fraction(value.unscaledValue(), BigInteger.TEN.pow(value.scale()));
        }

        Fraction add(Fraction other) {
            require(other);
            return new Fraction(numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
                    denominator.multiply(other.denominator));
        }

        Fraction multiply(Fraction other) {
            require(other);
            return new Fraction(numerator.multiply(other.numerator), denominator.multiply(other.denominator));
        }

        Fraction divide(Fraction other) {
            require(other);
            if (other.numerator.signum() == 0) throw invalid("تقسیم بر صفر مجاز نیست");
            return new Fraction(numerator.multiply(other.denominator), denominator.multiply(other.numerator));
        }

        /** The caller must choose and snapshot the rounding policy. Overflow is a validation failure. */
        long roundedRials(RoundingMode mode) {
            if (mode == null) throw invalid("روش گرد کردن را مشخص کنید");
            try {
                return new BigDecimal(numerator).divide(new BigDecimal(denominator), 0, mode).longValueExact();
            } catch (ArithmeticException e) {
                throw invalid("نتیجه در محدوده ریال صحیح نیست یا روش گرد کردن اجازه تبدیل نمی‌دهد");
            }
        }

        private static void require(Fraction other) {
            if (other == null) throw invalid("مبنای محاسبه موجود نیست");
        }

        @Override public String toString() { return numerator + "/" + denominator; }
    }
}
