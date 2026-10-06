package ir.kamranvahdati.lawoffice;

import java.io.IOException;
import java.io.Reader;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Exact tariff arithmetic over an explicitly selected, immutable rule pack.
 * Does not decide jurisdiction, appealability, special-case eligibility or recoverability.
 * Adoption date is metadata, NOT an inferred effective date.
 */
final class CalculationTariff {
    static final String ENGINE_VERSION = "tariff-math/1";
    enum Stage { WHOLE, FIRST, APPEAL, CIVIL_CASSATION, PROSECUTOR }
    enum Family { CIVIL, CRIMINAL, SERVICE }

    static final class Band {
        final long upper;
        final CalculationArithmetic.Fraction rate;
        Band(long upper, String percent) {
            if (upper <= 0) throw invalid("حد پله تعرفه باید مثبت باشد");
            this.upper = upper; rate = CalculationArithmetic.percent(percent);
        }
    }

    static final class Range {
        final Long minimum;
        final long maximum;
        final String article, label;
        final Family family;
        Range(Long minimum, long maximum, String article, String label, Family family) {
            if (maximum <= 0 || minimum != null && (minimum < 0 || minimum > maximum))
                throw invalid("حدود تعرفه ناسازگار است");
            if (family == null) throw invalid("گروه تعرفه الزامی است");
            this.minimum = minimum; this.maximum = maximum;
            this.article = CalculationReference.required(article, "ماده تعرفه");
            this.label = CalculationReference.required(label, "عنوان تعرفه");
            this.family = family;
        }
    }

    static final class Rules {
        final String version, sourceUrl, sourceTitle, adoptionDate, reviewDate;
        final List<Band> bands;
        final Map<String, Range> ranges;
        final CalculationArithmetic.Fraction civilFirst, civilAppeal;
        final CalculationArithmetic.Fraction criminalProsecutor, criminalFirst, criminalAppeal;

        Rules(Reader reader) throws IOException {
            if (reader == null) throw invalid("بسته قواعد موجود نیست");
            Properties p = new Properties(); p.load(reader);
            version = required(p, "version"); sourceUrl = required(p, "source.url");
            sourceTitle = required(p, "source.title");
            adoptionDate = CalculationReference.date(required(p, "adoption.date"));
            reviewDate = required(p, "review.date.utc");
            if (!"historical-text-reviewed".equals(required(p, "review.status")))
                throw invalid("وضعیت بررسی متن بسته قواعد شناخته‌شده نیست");
            ArrayList<Band> values = new ArrayList<>(); long previous = 0;
            for (String row : required(p, "financial.bands").split(";")) {
                String[] pair = row.split(":", -1);
                if (pair.length != 2) throw invalid("پله تعرفه نامعتبر است");
                Band b = new Band(Long.parseLong(pair[0]), pair[1]);
                if (b.upper <= previous) throw invalid("پله‌های تعرفه مرتب نیستند");
                previous = b.upper; values.add(b);
            }
            if (previous != Long.MAX_VALUE) throw invalid("پوشش پله آخر ناقص است");
            bands = Collections.unmodifiableList(values);
            civilFirst = percent(p, "civil.first"); civilAppeal = percent(p, "civil.appeal");
            criminalProsecutor = percent(p, "criminal.prosecutor");
            criminalFirst = percent(p, "criminal.first"); criminalAppeal = percent(p, "criminal.appeal");
            requireOne(civilFirst.add(civilAppeal));
            requireOne(criminalProsecutor.add(criminalFirst).add(criminalAppeal));
            Map<String, Range> rs = new LinkedHashMap<>();
            for (String id : required(p, "range.ids").split(",")) {
                id = id.trim(); String key = "range." + id + ".";
                String min = required(p, key + "min");
                Range range = new Range("unknown".equals(min) ? null : Long.valueOf(min),
                        Long.parseLong(required(p, key + "max")), required(p, key + "article"),
                        required(p, key + "label"), Family.valueOf(required(p, key + "family")));
                if (rs.put(id, range) != null) throw invalid("شناسه تعرفه تکراری است");
            }
            ranges = Collections.unmodifiableMap(rs);
        }

        private static String required(Properties p, String key) {
            return CalculationReference.required(p.getProperty(key), key);
        }
        private static CalculationArithmetic.Fraction percent(Properties p, String key) {
            return CalculationArithmetic.percent(required(p, key));
        }
        private static void requireOne(CalculationArithmetic.Fraction f) {
            if (!f.numerator.equals(f.denominator)) throw invalid("جمع سهم مراحل باید صد درصد باشد");
        }
    }

    static final class Result {
        final String ruleVersion, sourceUrl, sourceTitle, article, exactRials, selectedBasis;
        final Stage stage;
        final long rials;
        final RoundingMode rounding;
        final List<String> steps;
        Result(Rules rules, String article, Stage stage, CalculationArithmetic.Fraction exact,
                String selectedBasis, List<String> steps, RoundingMode rounding) {
            ruleVersion = rules.version; sourceUrl = rules.sourceUrl; sourceTitle = rules.sourceTitle;
            this.article = article; this.stage = stage; this.exactRials = exact.toString();
            this.selectedBasis = selectedBasis; this.rounding = rounding;
            rials = exact.roundedRials(rounding);
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        }
    }

    /** Article 9 progressive branch ONLY: final-by-value, family, dispositions and
     * specialist/multiple-counsel adjustments require their separate reviewed pathways.
     * The caller must record why this rule version and category apply to this matter.
     */
    static Result financial(Rules rules, long claimRials, Stage stage, String applicabilityBasis,
            boolean finalByValue, RoundingMode rounding) {
        require(rules, stage, rounding);
        String basis = CalculationReference.required(applicabilityBasis, "مبنای انطباق تعرفه با پرونده");
        if (finalByValue) throw invalid("شاخه حکم قطعی از حیث بها نیازمند بررسی مستقل مواد ۹ و ۲۱ است");
        if (claimRials <= 0) throw invalid("بهای خواسته باید مشخص و مثبت باشد");
        CalculationArithmetic.Fraction amount = CalculationArithmetic.Fraction.of(0, 1);
        List<String> steps = new ArrayList<>(); long previous = 0;
        for (Band band : rules.bands) {
            if (claimRials <= previous) break;
            long slice = Math.min(claimRials, band.upper) - previous;
            CalculationArithmetic.Fraction part = CalculationArithmetic.Fraction.of(slice, 1).multiply(band.rate);
            amount = amount.add(part);
            steps.add("ماده ۹: " + slice + " × " + band.rate + " = " + part + " ریال");
            previous = band.upper;
        }
        CalculationArithmetic.Fraction share = share(rules, Family.CIVIL, stage, true, false);
        steps.add("مواد ۱۶ و ۲۱ حسب مرحله: " + amount + " × " + share);
        return new Result(rules, "9/16/21", stage, amount.multiply(share), basis, steps, rounding);
    }

    /** Returns a supported user selection within a legal interval; never chooses a
     * midpoint/maximum automatically or treats an unknown minimum as statutory zero.
     * For CONSULTATION_HOUR, quantity is the exact number of hours. Otherwise it is one.
     */
    static Result ranged(Rules rules, String category, long selectedWholeRials, String quantity,
            Stage stage, boolean hasProsecutor, boolean finalCriminalTrial,
            String selectionBasis, RoundingMode rounding) {
        require(rules, stage, rounding);
        String basis = CalculationReference.required(selectionBasis, "مبنای انتخاب مبلغ و انطباق تعرفه");
        Range range = rules.ranges.get(category);
        if (range == null) throw invalid("این دسته تعرفه پشتیبانی نشده است");
        if (stage == Stage.CIVIL_CASSATION && !"FAMILY".equals(category) && !"NONFINANCIAL".equals(category))
            throw invalid("ماده ۱۶ را نمی‌توان به این مرجع تسری داد");
        if (selectedWholeRials < 0 || selectedWholeRials > range.maximum
                || range.minimum != null && selectedWholeRials < range.minimum)
            throw invalid("مبلغ انتخابی خارج از حدود تعرفه است");
        CalculationArithmetic.Fraction q = CalculationArithmetic.Fraction.decimal(CalculationArithmetic.decimal(quantity));
        if (q.numerator.signum() == 0) throw invalid("مقدار خدمت باید مثبت باشد");
        if (!"CONSULTATION_HOUR".equals(category) && !q.numerator.equals(q.denominator))
            throw invalid("تعداد یا ساعت فقط برای مشاوره ساعتی مجاز است");
        if (range.family != Family.CRIMINAL && (!hasProsecutor || finalCriminalTrial))
            throw invalid("گزینه‌های مرحله کیفری به این دسته تعلق ندارد");
        CalculationArithmetic.Fraction s = share(rules, range.family, stage, hasProsecutor, finalCriminalTrial);
        CalculationArithmetic.Fraction exact = CalculationArithmetic.Fraction.of(selectedWholeRials, 1).multiply(q).multiply(s);
        List<String> steps = new ArrayList<>();
        steps.add("ماده " + range.article + ": " + range.label);
        steps.add("مبلغ انتخابی مستند: " + selectedWholeRials + " ریال؛ مقدار خدمت " + q + "؛ سهم مرحله " + s);
        if (range.minimum == null) steps.add("این ماده فقط سقف دارد؛ حداقل قانونی در این بسته تعیین نشده است");
        return new Result(rules, range.article, stage, exact, basis, steps, rounding);
    }

    static long agreedFee(String amount, CalculationArithmetic.Currency currency) {
        // Independent contractual input; never substitutes the tariff or modifies a contract.
        return CalculationArithmetic.money(amount, currency);
    }

    private static CalculationArithmetic.Fraction share(Rules r, Family family, Stage stage,
            boolean prosecutor, boolean finalTrial) {
        if (family == Family.SERVICE) {
            if (stage != Stage.WHOLE) throw invalid("خدمت مستقل سهم مرحله دادرسی ندارد");
            return CalculationArithmetic.Fraction.of(1, 1);
        }
        if (family == Family.CIVIL) {
            switch (stage) {
                case WHOLE: return CalculationArithmetic.Fraction.of(1, 1);
                case FIRST: return r.civilFirst;
                case APPEAL: case CIVIL_CASSATION: return r.civilAppeal;
                default: throw invalid("مرحله دادسرا برای تعرفه مدنی معتبر نیست");
            }
        }
        if (stage == Stage.WHOLE) return CalculationArithmetic.Fraction.of(1, 1);
        if (stage == Stage.PROSECUTOR && prosecutor) return r.criminalProsecutor;
        if (stage == Stage.APPEAL && !finalTrial) return r.criminalAppeal;
        if (stage == Stage.FIRST) {
            CalculationArithmetic.Fraction part = r.criminalFirst;
            if (!prosecutor) part = part.add(r.criminalProsecutor);
            if (finalTrial) part = part.add(r.criminalAppeal);
            return part;
        }
        throw invalid("مرحله انتخابی با مسیر کیفری پرونده سازگار نیست");
    }

    private static void require(Rules rules, Stage stage, RoundingMode rounding) {
        if (rules == null || stage == null || rounding == null) throw invalid("قواعد، مرحله و روش گرد کردن الزامی است");
    }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
