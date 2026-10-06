package ir.kamranvahdati.lawoffice;

import org.json.JSONArray;
import org.json.JSONObject;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lossless, versioned payloads. Monetary values remain decimal strings/integer rials.
 * Strict required reads fail closed; absent audit/source fields are not defaulted away.
 */
final class CalculationJson {
    static JSONObject reference(CalculationReference r) throws Exception {
        JSONObject j = new JSONObject();
        j.put("format", 1); j.put("id", r.id); j.put("series", r.series); j.put("version", r.version);
        j.put("previousVersionId", r.previousVersionId); j.put("kind", r.kind.name());
        j.put("year", r.year); j.put("month", r.month); j.put("effectiveFrom", r.effectiveFrom);
        j.put("effectiveTo", r.effectiveTo); j.put("value", r.value); j.put("unit", r.unit);
        j.put("sourceTitle", r.sourceTitle); j.put("sourceType", r.sourceType);
        j.put("sourceNumber", r.sourceNumber); j.put("sourceDate", r.sourceDate);
        j.put("sourceUrl", r.sourceUrl); j.put("notes", r.notes); j.put("status", r.status.name());
        j.put("createdAt", r.createdAt); j.put("updatedAt", r.updatedAt);
        j.put("userConfirmed", r.userConfirmed); j.put("manuallyEntered", r.manuallyEntered);
        j.put("superseded", r.superseded); return j;
    }
    static CalculationReference reference(JSONObject j) throws Exception {
        format(j);
        return new CalculationReference(j.getString("id"), j.getString("series"), j.getString("version"),
            j.getString("previousVersionId"), CalculationReference.Kind.valueOf(j.getString("kind")),
            j.getInt("year"), j.getInt("month"), j.getString("effectiveFrom"), j.getString("effectiveTo"),
            j.getString("value"), j.getString("unit"), j.getString("sourceTitle"), j.getString("sourceType"),
            j.getString("sourceNumber"), j.getString("sourceDate"), j.getString("sourceUrl"), j.getString("notes"),
            CalculationReference.Status.valueOf(j.getString("status")), j.getLong("createdAt"), j.getLong("updatedAt"),
            j.getBoolean("userConfirmed"), j.getBoolean("manuallyEntered"), j.getBoolean("superseded"));
    }
    static JSONObject snapshot(CalculationSnapshot s) throws Exception {
        JSONObject j = new JSONObject(); j.put("format", 1);
        j.put("id", s.id); j.put("previousSnapshotId", s.previousSnapshotId); j.put("title", s.title);
        j.put("caseId", s.caseId == null ? JSONObject.NULL : s.caseId); j.put("type", s.type.name());
        j.put("engineVersion", s.engineVersion); j.put("legalBasisVersion", s.legalBasisVersion);
        j.put("dataVersion", s.dataVersion); j.put("calculationDate", s.calculationDate);
        j.put("effectiveLegalDate", s.effectiveLegalDate); j.put("inputs", new JSONObject(s.inputSnapshot));
        j.put("results", new JSONObject(s.resultSnapshot)); j.put("steps", new JSONArray(s.steps));
        j.put("warnings", new JSONArray(s.warnings)); j.put("rounding", s.roundingRule.name());
        j.put("createdAt", s.createdAt); j.put("updatedAt", s.updatedAt);
        JSONArray refs = new JSONArray(); for (CalculationReference r : s.references) refs.put(reference(r));
        j.put("references", refs); JSONArray overrides = new JSONArray();
        for (CalculationSnapshot.Override o : s.overrides) {
            JSONObject v = new JSONObject(); v.put("type", o.type.name()); v.put("field", o.field);
            v.put("oldValue", o.oldValue); v.put("newValue", o.newValue); v.put("reason", o.reason);
            v.put("changedAt", o.changedAt); overrides.put(v);
        }
        j.put("overrides", overrides); return j;
    }
    static CalculationSnapshot snapshot(JSONObject j) throws Exception {
        format(j);
        List<CalculationReference> refs = new ArrayList<>(); JSONArray a = j.getJSONArray("references");
        for (int i = 0; i < a.length(); i++) refs.add(reference(a.getJSONObject(i)));
        List<CalculationSnapshot.Override> overrides = new ArrayList<>(); a = j.getJSONArray("overrides");
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.getJSONObject(i);
            overrides.add(new CalculationSnapshot.Override(CalculationSnapshot.OverrideType.valueOf(o.getString("type")),
                o.getString("field"), o.getString("oldValue"), o.getString("newValue"), o.getLong("changedAt"), o.getString("reason")));
        }
        if (!j.has("caseId")) throw new IllegalArgumentException("ارتباط پرونده در محاسبه مشخص نیست");
        return new CalculationSnapshot(j.getString("id"), j.getString("previousSnapshotId"), j.getString("title"),
            j.isNull("caseId") ? null : j.getLong("caseId"), CalculationSnapshot.Type.valueOf(j.getString("type")),
            j.getString("engineVersion"), j.getString("legalBasisVersion"), j.getString("dataVersion"),
            j.getString("calculationDate"), j.getString("effectiveLegalDate"), map(j.getJSONObject("inputs")),
            map(j.getJSONObject("results")), refs, strings(j.getJSONArray("steps")), strings(j.getJSONArray("warnings")),
            overrides, RoundingMode.valueOf(j.getString("rounding")), j.getLong("createdAt"), j.getLong("updatedAt"));
    }
    private static Map<String, String> map(JSONObject j) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        for (Iterator<String> i = j.keys(); i.hasNext();) {
            String k = i.next(); Object value = j.get(k);
            if (!(value instanceof String)) throw new IllegalArgumentException("مقادیر محاسبات باید متن دقیق باشند");
            result.put(k, (String)value);
        }
        return result;
    }
    private static List<String> strings(JSONArray j) throws Exception {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < j.length(); i++) {
            Object value = j.get(i);
            if (!(value instanceof String)) throw new IllegalArgumentException("متن محاسبه نامعتبر است");
            result.add((String)value);
        }
        return result;
    }
    private static void format(JSONObject j) throws Exception {
        if (j.getInt("format") != 1) throw new IllegalArgumentException("نسخه محاسبه پشتیبانی نمی‌شود");
    }
}
