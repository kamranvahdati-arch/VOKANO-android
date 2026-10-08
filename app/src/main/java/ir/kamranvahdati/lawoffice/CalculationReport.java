package ir.kamranvahdati.lawoffice;

import java.util.Map;

/** One canonical text for on-screen review, print/PDF and Android share intent. */
final class CalculationReport {
    static String text(CalculationSnapshot s) {
        StringBuilder b=new StringBuilder(s.title).append('\n').append(type(s.type)).append('\n');
        // Limitations appear first and are never dropped from export/share.
        for(String warning:s.warnings)b.append("توجه: ").append(warning).append('\n');
        b.append("شناسه محاسبه: ").append(s.id).append("\nنسخه قبلی: ")
            .append(s.previousSnapshotId.isEmpty()?"ندارد":s.previousSnapshotId)
            .append("\nشناسه پرونده در زمان ثبت: ").append(s.caseId==null?"مستقل":s.caseId)
            .append("\nتاریخ محاسبه: ").append(s.calculationDate)
            .append("\nتاریخ مبنا: ").append(s.effectiveLegalDate)
            .append("\nنسخه موتور: ").append(s.engineVersion)
            .append("\nنسخه قواعد: ").append(s.legalBasisVersion)
            .append("\nنسخه داده: ").append(s.dataVersion)
            .append("\nگرد کردن نهایی: ").append(s.roundingRule)
            .append("\nمبنای دستی یا تغییر ورودی: ").append(s.manualOverride?"بله":"خیر")
            .append("\n\nورودی‌های ثبت‌شده\n");
        entries(b,s.inputSnapshot);b.append("\nنتیجه\n");entries(b,s.resultSnapshot);
        b.append("\nمراحل محاسبه\n");for(String step:s.steps)b.append(step).append('\n');
        b.append("\nمبانی و منابع\n");
        for(CalculationReference r:s.references){
            b.append(r.sourceTitle).append(" | ").append(r.sourceType).append(" | ").append(r.sourceNumber)
                .append("\nتاریخ منبع: ").append(r.sourceDate).append("\nنشانی: ").append(r.sourceUrl)
                .append("\nشناسه و نسخه: ").append(r.id).append(" / ").append(r.version)
                .append("\nسری: ").append(r.series).append(" | سال/ماه: ").append(r.year).append('/').append(r.month)
                .append("\nمقدار و واحد: ").append(r.value).append(' ').append(r.unit)
                .append("\nاعتبار ثبت‌شده: ").append(r.effectiveFrom).append(" تا ").append(r.effectiveTo)
                .append("\nوضعیت مبنا: ").append(r.status).append(" | ورود دستی: ").append(r.manuallyEntered)
                .append("\nیادداشت: ").append(r.notes).append('\n');
        }
        b.append("\nسابقه تغییرات\n");
        for(CalculationSnapshot.Override o:s.overrides)b.append(o.type).append(" | ").append(o.field)
            .append("\nقبل: ").append(o.oldValue).append("\nبعد: ").append(o.newValue)
            .append("\nعلت: ").append(o.reason).append(" | زمان ثبت: ").append(o.changedAt).append('\n');
        return b.toString();
    }
    static String type(CalculationSnapshot.Type type){
        switch(type){
            case AGREED_FEE:return "حق‌الوکاله توافقی";
            case TARIFF_FEE:return "حق‌الوکاله تعرفه‌ای";
            case ORDINARY_DEBT_DELAY:return "خسارت تأخیر تأدیه دین عادی";
            case CHEQUE_DELAY:return "خسارت تأخیر تأدیه چک";
            case DIYAH:return "دیه";
            case ARSH_ESTIMATE:return "برآورد محاسباتی ارش";
            default:throw new IllegalArgumentException("نوع محاسبه ناشناخته است");
        }
    }
    private static void entries(StringBuilder b,Map<String,String> values){
        for(Map.Entry<String,String> e:values.entrySet()){
            // Full pack stays in the immutable payload; report includes used formulas,
            // source/version and rates instead of dumping unrelated machine-readable rules.
            if("_rulePackSnapshot".equals(e.getKey()))continue;
            b.append(label(e.getKey())).append(": ").append(e.getValue()).append('\n');
        }
    }
    private static String label(String key){
        switch(key){
            case "tariffSpecial":return "حالت ویژه تعرفه";case "specialCount":return "تعداد وکلا یا اتهام‌ها";
            case "specialBasis":return "مستند شرایط حالت ویژه";case "resultScope":return "دامنه مبلغ نتیجه";
            case "disposition":return "نوع نتیجه دادرسی";
            case "indexPack":return "بسته شاخص منتخب";case "indexSeries":return "سری و سال پایه شاخص";
            case "basePercent":return "درصد مبنای عادی نفس";case "deathDate":return "تاریخ فوت";
            case "actMonth":return "ماه حرام رفتار";case "deathMonth":return "ماه حرام فوت";
            case "actMecca":return "حرم مکه؛ رفتار";case "deathMecca":return "حرم مکه؛ فوت";
            case "dueDate":return "سررسید / تاریخ چک";case "demandDate":return "تاریخ مطالبه";
            case "startDate":return "مبدأ حقوقی";case "delayMode":return "نوع دین";case "scopeConfirmed":return "شرایط تأییدشده";
            case "bodyMode":return "روش تبدیل دیه / ارش";case "assessedValue":return "مقدار اعلام‌شده";
            case "injury":return "صدمه و عضو یا منفعت";case "occurredDate":return "تاریخ وقوع";
            case "assessment":return "مستند تعیین مرجع";case "expertOpinion":return "نظر کارشناسی مستقل";
            case "enforcementAward":return "محکوم‌به / مورد اجرا با واحد ورودی";case "prescribedInjury":return "شناسه صدمه جدول قانونی";
            case "dateBasis":return "مستند تاریخ ارزش‌گذاری";
            case "title":return "عنوان";case "caseId":return "شناسه پرونده";
            case "amount":return "مبلغ ورودی";case "currency":return "واحد ورودی";
            case "effectiveDate":return "تاریخ مبنا";case "basis":return "مستند انتخاب";
            case "category":return "دسته تعرفه";case "stage":return "مرحله";
            case "quantity":return "مقدار خدمت";case "hasProsecutor":return "وجود مرحله دادسرا";
            case "finalTrial":return "قطعیت بدوی کیفری";case "sourceUrl":return "نشانی منبع";
            case "sourceTitle":return "عنوان منبع";case "rulePack":return "نسخه بسته قواعد";
            case "sourceAdoptionDate":return "تاریخ تصویب مبنا";case "sourceReviewDate":return "تاریخ بررسی منبع (میلادی)";
            default:return key;
        }
    }
}
