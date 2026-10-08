package ir.kamranvahdati.lawoffice;

/** Ruling 824: judicially established insolvency stops accrual; a separately
 * overdue court instalment accrues only from that instalment's due date.
 * No finding of insolvency, default, ability to pay or judgment scope is inferred. */
final class CalculationDelayExceptions {
    static final String VERSION="reviewed-delay-824/1";
    static final String SOURCE="https://nezamat.ir/post-44019/";
    enum Route { SINGLE, PARTIAL, INSOLVENCY_STOP, OVERDUE_INSTALLMENT }
    static String indexedEnd(Route route,CalculationDelayMath.Mode mode,String due,String start,String end,
            String establishedDate,String judgmentBasis,boolean confirmed){
        if(route==null||mode==null)throw new IllegalArgumentException("مسیر حقوقی مشخص نیست");
        due=CalculationReference.date(due);start=CalculationReference.date(start);end=CalculationReference.date(end);
        if(start.compareTo(due)<0||end.compareTo(start)<0)throw new IllegalArgumentException("ترتیب تاریخ‌ها معتبر نیست");
        if(route==Route.SINGLE||route==Route.PARTIAL)return end;
        if(!confirmed)throw new IllegalArgumentException("حکم، تاریخ ثبوت اعسار و شرایط مسیر انتخابی را تأیید کنید");
        CalculationReference.required(judgmentBasis,"مستند حکم و شرایط اعسار یا قسط معوق");
        String stop=CalculationReference.date(establishedDate);
        if(route==Route.OVERDUE_INSTALLMENT){
            if(!start.equals(due)||due.compareTo(stop)<0)throw new IllegalArgumentException("مبدأ این مسیر سررسید قسطِ حکم است و نباید پیش از ثبوت اعسار باشد");
            return end;
        }
        // A debt arising after an already effective insolvency finding has no
        // pre-finding interval in this bounded scenario. Return zero duration.
        if(stop.compareTo(start)<0)return start;
        return stop.compareTo(end)<0?stop:end;
    }
}
