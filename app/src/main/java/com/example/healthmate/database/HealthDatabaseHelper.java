package com.example.healthmate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import com.example.healthmate.model.HealthRecord;
import com.example.healthmate.utils.PasswordUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/** All local database operations are kept in this class. */
public class HealthDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "healthmate.db";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_RECORDS = "health_records";

    public HealthDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createHealthTable(db);
        createUsersTable(db);
    }

    private void createHealthTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS health_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, " +
                "steps INTEGER NOT NULL, calories INTEGER NOT NULL, water INTEGER NOT NULL)");
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) createUsersTable(db);
        if (oldVersion < 3) {
            createHealthTable(db);
            // Keep records created by older versions. Copy before removing the old table.
            if (tableExists(db, "records")) {
                db.execSQL("INSERT OR IGNORE INTO health_records (id,date,steps,calories,water) " +
                        "SELECT id,date,steps,calories,water FROM records");
                db.execSQL("DROP TABLE records");
            }
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public long insertRecord(String date, int steps, int calories, int water) {
        ContentValues values = recordValues(date, steps, calories, water);
        try { return getWritableDatabase().insert(TABLE_RECORDS, null, values); }
        catch (SQLiteException e) { return -1; }
    }

    public int updateRecord(int id, String date, int steps, int calories, int water) {
        try {
            return getWritableDatabase().update(TABLE_RECORDS, recordValues(date, steps, calories, water),
                    "id=?", new String[]{String.valueOf(id)});
        } catch (SQLiteException e) { return 0; }
    }

    public int deleteRecord(int id) {
        try { return getWritableDatabase().delete(TABLE_RECORDS, "id=?", new String[]{String.valueOf(id)}); }
        catch (SQLiteException e) { return 0; }
    }

    private ContentValues recordValues(String date, int steps, int calories, int water) {
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("steps", steps);
        values.put("calories", calories);
        values.put("water", water);
        return values;
    }

    public ArrayList<HealthRecord> getAllRecords() {
        return readRecords(null, null);
    }

    public ArrayList<HealthRecord> searchRecordsByDate(String datePart) {
        return readRecords("date LIKE ?", new String[]{"%" + datePart.trim() + "%"});
    }

    private ArrayList<HealthRecord> readRecords(String selection, String[] args) {
        ArrayList<HealthRecord> records = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECORDS,
                new String[]{"id", "date", "steps", "calories", "water"},
                selection, args, null, null, "date DESC, id DESC");
        try {
            while (cursor.moveToNext()) {
                records.add(new HealthRecord(cursor.getInt(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getInt(3), cursor.getInt(4)));
            }
        } finally {
            cursor.close();
        }
        return records;
    }

    public HealthRecord findRecord(int id) {
        ArrayList<HealthRecord> result = readRecords("id=?", new String[]{String.valueOf(id)});
        return result.isEmpty() ? null : result.get(0);
    }

    public int[] getTodaySummary(String today) {
        int[] totals = new int[]{0, 0, 0};
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(steps),0), COALESCE(SUM(calories),0), COALESCE(SUM(water),0) " +
                        "FROM health_records WHERE date=?", new String[]{today});
        try {
            if (cursor.moveToFirst()) {
                totals[0] = cursor.getInt(0);
                totals[1] = cursor.getInt(1);
                totals[2] = cursor.getInt(2);
            }
        } finally {
            cursor.close();
        }
        return totals;
    }

    public void addSampleData(String today) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM health_records", null);
        int count = 0;
        try { if (cursor.moveToFirst()) count = cursor.getInt(0); }
        finally { cursor.close(); }
        if (count == 0) {
            insertRecord(today, 7850, 420, 2000);
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DAY_OF_YEAR, -1);
            insertRecord(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime()), 6200, 350, 1600);
            date.add(Calendar.DAY_OF_YEAR, -1);
            insertRecord(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime()), 9100, 510, 2200);
        }
    }

    public boolean userExists(String username) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id FROM users WHERE username=?",
                new String[]{username});
        try { return cursor.moveToFirst(); }
        finally { cursor.close(); }
    }

    public boolean createUser(String username, String password) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", PasswordUtils.hashPassword(password));
        try { return getWritableDatabase().insert("users", null, values) != -1; }
        catch (SQLiteException e) { return false; }
    }

    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id,password FROM users WHERE username=?",
                new String[]{username});
        try {
            if (!cursor.moveToFirst()) return false;
            int userId = cursor.getInt(0);
            String savedPassword = cursor.getString(1);
            if (PasswordUtils.isHashed(savedPassword)) {
                return PasswordUtils.verifyPassword(password, savedPassword);
            }
            // Upgrade an old plain-text password after a successful login.
            if (savedPassword != null && savedPassword.equals(password)) {
                ContentValues values = new ContentValues();
                values.put("password", PasswordUtils.hashPassword(password));
                db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
                return true;
            }
            return false;
        } finally {
            cursor.close();
        }
    }
}
