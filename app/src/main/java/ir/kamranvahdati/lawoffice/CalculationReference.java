package ir.kamranvahdati.lawoffice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Immutable reference value. Editing/confirmation creates a new version, never mutates this one. */
final class CalculationReference {
    enum Kind { ANNUAL_DIYAH, ECONOMIC_INDEX, TARIFF, LEGAL_RULE }
    enum Status { OFFICIAL_VERIFIED, USER_ENTERED, NEEDS_REVIEW, SUPERSEDED }

    final String id, series, version, previousVersionId;
    final Kind kind;
    final int year, month;
    final String effectiveFrom, effectiveTo, value, unit;
    final String sourceTitle, sourceType, sourceNumber, sourceDate, sourceUrl, notes;
    final Status status;
    final long createdAt, updatedAt;
    final boolean userConfirmed, manuallyEntered, superseded;

    CalculationReference(String id, String series, String version, String previousVersionId,
            Kind kind, int year, int month, String effectiveFrom, String effectiveTo,
            String value, String unit, String sourceTitle, String sourceType,
            String sourceNumber, String sourceDate, String sourceUrl, String notes,
            Status status, long createdAt, long updatedAt,
            boolean userConfirmed, boolean manuallyEntered, boolean superseded) {
        this.id = required(id, "شناسه مبنا");
        this.series = required(series, "سری مبنا");
        this.version = required(version, "نسخه مبنا");
        this.previousVersionId = optional(previousVersionId);
        if (id.equals(this.previousVersionId)) throw invalid("نسخه جدید باید شناسه مستقل داشته باشد");
        if (kind == null || status == null) throw invalid("نوع و وضعیت مبنا الزامی است");
        if (year < 1 || year > 9999 || month < 0 || month > 12) throw invalid("سال یا ماه نامعتبر است");
        if (kind == Kind.ECONOMIC_INDEX && month == 0) throw invalid("ماه شاخص الزامی است");
        if (kind == Kind.ANNUAL_DIYAH && month != 0) throw invalid("نرخ سالانه دیه ماه ندارد");
        this.kind = kind; this.year = year; this.month = month;
        this.effectiveFrom = date(effectiveFrom);
        this.effectiveTo = optional(effectiveTo).isEmpty() ? "" : date(effectiveTo);
        if (!this.effectiveTo.isEmpty() && this.effectiveFrom.compareTo(this.effectiveTo) > 0)
            throw invalid("پایان اعتبار پیش از شروع اعتبار است");
        this.value = required(value, "مقدار مبنا");
        this.unit = required(unit, "واحد مبنا");
        if (kind == Kind.ANNUAL_DIYAH) {
            if (!"IRR".equals(unit)) throw invalid("واحد ذخیره نرخ دیه باید ریال باشد");
            if (CalculationArithmetic.money(value, CalculationArithmetic.Currency.RIAL) <= 0)
                throw invalid("نرخ دیه باید مثبت باشد");
        }
        if (kind == Kind.ECONOMIC_INDEX && CalculationArithmetic.decimal(value).compareTo(BigDecimal.ZERO) <= 0)
            throw invalid("شاخص باید مثبت باشد");
        this.sourceTitle = optional(sourceTitle); this.sourceType = optional(sourceType);
        this.sourceNumber = optional(sourceNumber);
        this.sourceDate = optional(sourceDate).isEmpty() ? "" : date(sourceDate);
        this.sourceUrl = optional(sourceUrl); this.notes = optional(notes);
        if (status == Status.OFFICIAL_VERIFIED && (manuallyEntered || this.sourceTitle.isEmpty()
                || this.sourceType.isEmpty() || this.sourceDate.isEmpty() || this.sourceUrl.isEmpty()))
            throw invalid("مبنای رسمی به مشخصات منبع تأییدشده نیاز دارد؛ ورود دستی رسمی محسوب نمی‌شود");
        if (status == Status.USER_ENTERED && !manuallyEntered)
            throw invalid("مبنای کاربر باید با نشان ورود دستی ثبت شود");
        if (superseded != (status == Status.SUPERSEDED)) throw invalid("وضعیت نسخه منسوخ ناسازگار است");
        if (createdAt <= 0 || updatedAt < createdAt) throw invalid("زمان ثبت یا ویرایش نامعتبر است");
        this.status = status; this.createdAt = createdAt; this.updatedAt = updatedAt;
        this.userConfirmed = userConfirmed; this.manuallyEntered = manuallyEntered; this.superseded = superseded;
    }

    void requireUsable(String effectiveDate, int requestedYear, int requestedMonth, String expectedSeries) {
        String normalized = date(effectiveDate);
        if (requestedYear != year || requestedMonth != month || !series.equals(expectedSeries))
            throw invalid("مبنای همان سال، ماه و سری شاخص موردنیاز موجود نیست");
        if (normalized.compareTo(effectiveFrom) < 0 || (!effectiveTo.isEmpty() && normalized.compareTo(effectiveTo) > 0))
            throw invalid("تاریخ مؤثر خارج از اعتبار مبنای انتخاب‌شده است");
        if (superseded || status == Status.NEEDS_REVIEW
                || (status == Status.USER_ENTERED && !userConfirmed))
            throw invalid("مبنا برای محاسبه جدید تأیید نشده است");
    }

    /** No latest-year or arbitrary-first fallback. Ambiguity requires explicit user selection. */
    static CalculationReference select(List<CalculationReference> references, Kind kind,
            String series, int year, int month, String effectiveDate) {
        if (references == null) throw invalid("داده مبنا موجود نیست");
        List<CalculationReference> matches = new ArrayList<>();
        for (CalculationReference ref : references) {
            if (ref == null) throw invalid("رکورد مبنا نامعتبر است");
            if (ref.kind == kind && ref.series.equals(series) && ref.year == year && ref.month == month
                    && !ref.superseded && ref.status != Status.NEEDS_REVIEW
                    && (ref.status != Status.USER_ENTERED || ref.userConfirmed)) {
                String date = date(effectiveDate);
                if (date.compareTo(ref.effectiveFrom) >= 0 && (ref.effectiveTo.isEmpty() || date.compareTo(ref.effectiveTo) <= 0))
                    matches.add(ref);
            }
        }
        if (matches.isEmpty()) throw invalid("مبنای تأییدشده موجود نیست: " + year + "/" + month + " — " + series);
        if (matches.size() != 1) throw invalid("چند نسخه معتبر وجود دارد؛ نسخه مبنا را صریحاً انتخاب کنید");
        return matches.get(0);
    }

    static String date(String input) { return JalaliDate.parse(required(input, "تاریخ")).value(); }
    static String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw invalid(field + " الزامی است");
        return value.trim();
    }
    static String optional(String value) { return value == null ? "" : value.trim(); }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
