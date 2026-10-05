package ir.kamranvahdati.lawoffice;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable calculation payload; persistence must insert a new revision rather than replace it. */
final class CalculationSnapshot {
    static final String ENGINE_VERSION = "calculation-core/1";
    static final String ARSH_WARNING = "این مبلغ برآورد محاسباتی است و تعیین نهایی ارش بر عهده مرجع قضایی با لحاظ نظر کارشناسی و مقررات قانونی است.";
    enum Type { AGREED_FEE, TARIFF_FEE, ORDINARY_DEBT_DELAY, CHEQUE_DELAY, DIYAH, ARSH_ESTIMATE }
    enum OverrideType { RATE, INDEX, TARIFF, PERCENTAGE, DATE, RULE, INPUT }

    final String id, previousSnapshotId, title;
    final Long caseId;
    final Type type;
    final String engineVersion, legalBasisVersion, dataVersion;
    final String calculationDate, effectiveLegalDate;
    final Map<String, String> inputSnapshot, resultSnapshot;
    final List<CalculationReference> references;
    final List<String> steps, warnings;
    final List<Override> overrides;
    final RoundingMode roundingRule;
    final boolean manualOverride;
    final long createdAt, updatedAt;

    CalculationSnapshot(String id, String previousSnapshotId, String title, Long caseId, Type type,
            String engineVersion, String legalBasisVersion, String dataVersion,
            String calculationDate, String effectiveLegalDate,
            Map<String, String> inputs, Map<String, String> results,
            List<CalculationReference> references, List<String> steps, List<String> warnings,
            List<Override> overrides, RoundingMode roundingRule, long createdAt, long updatedAt) {
        this.id = required(id, "شناسه محاسبه");
        this.previousSnapshotId = CalculationReference.optional(previousSnapshotId);
        if (this.id.equals(this.previousSnapshotId)) throw invalid("محاسبه مجدد باید شناسه جدید داشته باشد");
        this.title = required(title, "عنوان محاسبه");
        if (caseId != null && caseId <= 0) throw invalid("شناسه پرونده نامعتبر است");
        if (type == null || roundingRule == null) throw invalid("نوع محاسبه و روش گرد کردن الزامی است");
        this.caseId = caseId; this.type = type;
        this.engineVersion = required(engineVersion, "نسخه موتور");
        this.legalBasisVersion = required(legalBasisVersion, "نسخه مبنای حقوقی");
        this.dataVersion = required(dataVersion, "نسخه داده");
        this.calculationDate = CalculationReference.date(calculationDate);
        this.effectiveLegalDate = CalculationReference.date(effectiveLegalDate);
        this.inputSnapshot = immutableMap(inputs);
        this.resultSnapshot = immutableMap(results);
        if (this.inputSnapshot.isEmpty() || this.resultSnapshot.isEmpty()) throw invalid("ورودی و نتیجه الزامی است");
        this.references = immutableList(references);
        this.steps = immutableTextList(steps);
        if (this.steps.isEmpty()) throw invalid("مراحل محاسبه الزامی است");
        List<String> preservedWarnings = new ArrayList<>(immutableTextList(warnings));
        if (type == Type.ARSH_ESTIMATE && !preservedWarnings.contains(ARSH_WARNING)) preservedWarnings.add(ARSH_WARNING);
        this.warnings = Collections.unmodifiableList(preservedWarnings);
        this.overrides = immutableList(overrides);
        this.manualOverride = !this.overrides.isEmpty() || hasManualReference(this.references);
        if (createdAt <= 0 || updatedAt != createdAt)
            throw invalid("نسخه محاسبه تغییرناپذیر است؛ اصلاح باید نسخه جدید بسازد");
        for (Override override : this.overrides)
            if (override.changedAt > createdAt) throw invalid("زمان تغییر مبنا پس از ثبت نتیجه است");
        this.roundingRule = roundingRule; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    static final class Override {
        final OverrideType type;
        final String field, oldValue, newValue, reason;
        final long changedAt;

        Override(OverrideType type, String field, String oldValue, String newValue, long changedAt, String reason) {
            if (type == null || oldValue == null || newValue == null || changedAt <= 0)
                throw invalid("مشخصات سابقه تغییر ناقص است");
            if (oldValue.equals(newValue)) throw invalid("مقدار جدید با مقدار قبلی برابر است");
            this.type = type; this.field = required(field, "فیلد تغییر");
            this.oldValue = oldValue; this.newValue = newValue;
            this.changedAt = changedAt; this.reason = CalculationReference.optional(reason);
        }
    }

    private static boolean hasManualReference(List<CalculationReference> references) {
        for (CalculationReference ref : references) if (ref.manuallyEntered) return true;
        return false;
    }

    private static Map<String, String> immutableMap(Map<String, String> values) {
        if (values == null) throw invalid("داده محاسبه الزامی است");
        Map<String, String> copy = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = required(entry.getKey(), "نام ورودی");
            if (!key.equals(entry.getKey()) || entry.getValue() == null) throw invalid("کلید یا مقدار snapshot نامعتبر است");
            copy.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(copy);
    }

    private static <T> List<T> immutableList(List<T> values) {
        if (values == null) throw invalid("فهرست داده الزامی است");
        List<T> copy = new ArrayList<>(values);
        for (T item : copy) if (item == null) throw invalid("عضو خالی در فهرست مجاز نیست");
        return Collections.unmodifiableList(copy);
    }

    private static List<String> immutableTextList(List<String> values) {
        List<String> copy = immutableList(values);
        for (String value : copy) required(value, "متن");
        return copy;
    }

    private static String required(String value, String field) { return CalculationReference.required(value, field); }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
