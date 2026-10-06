package ir.kamranvahdati.lawoffice;

import android.content.ContentValues;
import android.database.Cursor;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Additive storage. Existing office records are never modified by calculation saves.
 * Previous payloads remain immutable; new revisions have new IDs and an audit chain.
 */
final class OfficeCalculations {
    static final String REFERENCES = "calculation_references";
    static final String SNAPSHOTS = "calculation_snapshots";
    private final OfficeDb office;
    OfficeCalculations(OfficeDb office) { this.office = office; }

    static void migrate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS calculation_references(" +
            "id TEXT PRIMARY KEY NOT NULL,previous_id TEXT,series TEXT NOT NULL,kind TEXT NOT NULL," +
            "year INTEGER NOT NULL,month INTEGER NOT NULL,payload TEXT NOT NULL,created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS calculation_snapshots(" +
            "id TEXT PRIMARY KEY NOT NULL,previous_id TEXT,case_id INTEGER,title TEXT NOT NULL," +
            "type TEXT NOT NULL,payload TEXT NOT NULL,created_at INTEGER NOT NULL," +
            "FOREIGN KEY(case_id) REFERENCES cases(id) ON DELETE SET NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS calculation_case_idx ON calculation_snapshots(case_id,created_at)");
        db.execSQL("CREATE INDEX IF NOT EXISTS calculation_reference_idx ON calculation_references(series,year,month)");
    }

    void saveReference(CalculationReference r) throws Exception {
        SQLiteDatabase db = office.getWritableDatabase(); db.beginTransaction();
        try { insertReference(db, r); db.setTransactionSuccessful(); } finally { db.endTransaction(); }
    }
    private static void insertReference(SQLiteDatabase db, CalculationReference r) throws Exception {
        String payload = CalculationJson.reference(r).toString();
        try (Cursor c = db.rawQuery("SELECT payload FROM calculation_references WHERE id=?",new String[]{r.id})) {
            if (c.moveToFirst()) {
                // An existing historical reference may be reused, never replaced.
                if (!CalculationJson.reference(CalculationJson.reference(new JSONObject(c.getString(0)))).toString().equals(payload))
                    throw new IllegalArgumentException("شناسه مبنا با محتوای قبلی متفاوت است");
                return;
            }
        }
        if (!r.previousVersionId.isEmpty()) {
            CalculationReference previous = reference(db, r.previousVersionId);
            if (previous == null || previous.kind != r.kind || !previous.series.equals(r.series)
                    || previous.year != r.year || previous.month != r.month || previous.createdAt >= r.createdAt)
                throw new IllegalArgumentException("زنجیره نسخه مبنا نامعتبر است");
            CalculationReference.required(r.notes, "توضیح تغییر مبنا");
        }
        ContentValues v = new ContentValues(); v.put("id",r.id);
        nullable(v,"previous_id",r.previousVersionId); v.put("series",r.series); v.put("kind",r.kind.name());
        v.put("year",r.year); v.put("month",r.month); v.put("payload",payload); v.put("created_at",r.createdAt);
        db.insertOrThrow(REFERENCES,null,v);
    }
    void save(CalculationSnapshot s) throws Exception {
        SQLiteDatabase db = office.getWritableDatabase(); db.beginTransaction();
        try {
            if (!s.previousSnapshotId.isEmpty()) {
                CalculationSnapshot previous = snapshot(db,s.previousSnapshotId);
                if (previous == null || previous.type != s.type || previous.createdAt >= s.createdAt)
                    throw new IllegalArgumentException("نسخه قبلی محاسبه نامعتبر است");
            }
            for (CalculationSnapshot.Override o : s.overrides) CalculationReference.required(o.reason,"علت تغییر");
            for (CalculationReference r : s.references) insertReference(db,r);
            if (s.caseId != null) try (Cursor c = db.rawQuery("SELECT id FROM cases WHERE id=? AND deleted_at IS NULL",new String[]{s.caseId.toString()})) {
                if (!c.moveToFirst()) throw new IllegalArgumentException("پرونده فعال برای ارتباط یافت نشد");
            }
            ContentValues v = new ContentValues(); v.put("id",s.id); nullable(v,"previous_id",s.previousSnapshotId);
            if (s.caseId == null) v.putNull("case_id"); else v.put("case_id",s.caseId);
            v.put("title",s.title); v.put("type",s.type.name()); v.put("payload",CalculationJson.snapshot(s).toString());
            v.put("created_at",s.createdAt); db.insertOrThrow(SNAPSHOTS,null,v);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
    CalculationSnapshot get(String id) throws Exception { return snapshot(office.getReadableDatabase(),id); }
    List<CalculationSnapshot> list(Long caseId) throws Exception {
        List<CalculationSnapshot> result = new ArrayList<>();
        String sql = "SELECT payload FROM calculation_snapshots" + (caseId == null ? "" : " WHERE case_id=?") + " ORDER BY created_at DESC,id";
        try (Cursor c = office.getReadableDatabase().rawQuery(sql,caseId == null ? null : new String[]{caseId.toString()})) {
            while (c.moveToNext()) result.add(CalculationJson.snapshot(new JSONObject(c.getString(0))));
        }
        return result;
    }
    List<CalculationReference> currentReferences() throws Exception {
        List<CalculationReference> result = new ArrayList<>();
        try (Cursor c = office.getReadableDatabase().rawQuery("SELECT r.payload FROM calculation_references r WHERE NOT EXISTS(SELECT 1 FROM calculation_references n WHERE n.previous_id=r.id) ORDER BY r.created_at,r.id",null)) {
            while (c.moveToNext()) result.add(CalculationJson.reference(new JSONObject(c.getString(0))));
        }
        return result;
    }
    private static CalculationReference reference(SQLiteDatabase db,String id) throws Exception {
        try (Cursor c = db.rawQuery("SELECT payload FROM calculation_references WHERE id=?",new String[]{id})) {
            return c.moveToFirst() ? CalculationJson.reference(new JSONObject(c.getString(0))) : null;
        }
    }
    private static CalculationSnapshot snapshot(SQLiteDatabase db,String id) throws Exception {
        try (Cursor c = db.rawQuery("SELECT payload FROM calculation_snapshots WHERE id=?",new String[]{id})) {
            return c.moveToFirst() ? CalculationJson.snapshot(new JSONObject(c.getString(0))) : null;
        }
    }
    /** Called inside restore's transaction; malformed payload/index/lineage rolls back everything. */
    static void validateRestored(SQLiteDatabase db) throws Exception {
        try (Cursor c = db.rawQuery("SELECT id,previous_id,series,kind,year,month,created_at,payload FROM calculation_references",null)) {
            while (c.moveToNext()) {
                CalculationReference r = CalculationJson.reference(new JSONObject(c.getString(7)));
                if (!r.id.equals(c.getString(0)) || !r.previousVersionId.equals(optional(c.getString(1)))
                        || !r.series.equals(c.getString(2)) || !r.kind.name().equals(c.getString(3))
                        || r.year != c.getInt(4) || r.month != c.getInt(5) || r.createdAt != c.getLong(6))
                    throw new IllegalArgumentException("اطلاعات مبنای پشتیبان ناسازگار است");
                if (!r.previousVersionId.isEmpty()) {
                    CalculationReference previous = reference(db,r.previousVersionId);
                    if (previous == null || previous.createdAt >= r.createdAt || previous.kind != r.kind
                            || !previous.series.equals(r.series) || previous.year != r.year || previous.month != r.month)
                        throw new IllegalArgumentException("تاریخچه مبنای پشتیبان ناقص است");
                    CalculationReference.required(r.notes,"علت تغییر مبنا");
                }
            }
        }
        try (Cursor c = db.rawQuery("SELECT id,previous_id,case_id,title,type,created_at,payload FROM calculation_snapshots",null)) {
            while (c.moveToNext()) {
                CalculationSnapshot s = CalculationJson.snapshot(new JSONObject(c.getString(6)));
                if (!s.id.equals(c.getString(0)) || !s.previousSnapshotId.equals(optional(c.getString(1)))
                        || !s.title.equals(c.getString(3)) || !s.type.name().equals(c.getString(4)) || s.createdAt != c.getLong(5)
                        || !c.isNull(2) && (s.caseId == null || s.caseId != c.getLong(2)))
                    throw new IllegalArgumentException("اطلاعات محاسبه پشتیبان ناسازگار است");
                if (!s.previousSnapshotId.isEmpty()) {
                    CalculationSnapshot previous = snapshot(db,s.previousSnapshotId);
                    if (previous == null || previous.type != s.type || previous.createdAt >= s.createdAt)
                        throw new IllegalArgumentException("تاریخچه محاسبه پشتیبان ناقص است");
                }
                for (CalculationSnapshot.Override o : s.overrides) CalculationReference.required(o.reason,"علت تغییر");
                for (CalculationReference r : s.references) {
                    CalculationReference saved = reference(db,r.id);
                    if (saved == null || !CalculationJson.reference(saved).toString().equals(CalculationJson.reference(r).toString()))
                        throw new IllegalArgumentException("مبنای snapshot با تاریخچه پشتیبان متفاوت است");
                }
            }
        }
    }
    private static void nullable(ContentValues v,String key,String value) { if(value.isEmpty()) v.putNull(key); else v.put(key,value); }
    private static String optional(String value) { return value == null ? "" : value; }
}
