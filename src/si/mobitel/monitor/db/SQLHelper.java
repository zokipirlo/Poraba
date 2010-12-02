package si.mobitel.monitor.db;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class SQLHelper {
	
	public static final String DATABASE_NAME  = "poraba_settings.db";
	public static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "Settings";
	
	private Context context;
	private SQLiteDatabase db;
	
	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into " + TABLE_NAME + " (phone_num,key,value) values (?,?,?)";
	
	
	public SQLHelper (Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement(INSERT);
	}
	
	public void insertAll (HashMap<String, String> allSettings, String phoneNum) {
		if (phoneNum == null) phoneNum = "";
		deleteAll(phoneNum);
		for (String key : allSettings.keySet()) {
			insertOne(phoneNum, key, allSettings.get(key));
		}
	}
	
	private long insertOne (String phoneNum, String key, String value) {
		if (value == null) value = "";
		
		this.insertStmt.bindString(1, phoneNum);
		this.insertStmt.bindString(2, key);
		this.insertStmt.bindString(3, value);
		Log.d("sql", "insert "+ phoneNum + " ; k: " + key + " ; v :" + value);
		return this.insertStmt.executeInsert();
	}
	
	private void deleteAll(String phoneNum) {
		if (phoneNum == null) phoneNum = "";
		this.db.delete(TABLE_NAME, "phone_num == ?", new String[] {phoneNum});
	}
	
	public HashMap<String, String> getAllSettingsForPhoneNum(String phoneNum) {
		if (phoneNum == null) phoneNum = "";
		HashMap<String,String> result = new HashMap<String,String>();
		Cursor cursor = this.db.query(TABLE_NAME, new String[] {"key", "value"}, "phone_num == ?",new String[] {phoneNum},null,null, null);
		if (cursor.moveToFirst()) {
			do {
				Log.d("sql","SELECT " + phoneNum + " ; k: " + cursor.getString(0) + " ; v: " +  cursor.getString(1));
				result.put(cursor.getString(0), cursor.getString(1));
			} while (cursor.moveToNext());
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return result;
		
	}
	
	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		 
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY, phone_num TEXT, key TEXT, value TEXT)");
		}
		 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("DB", "Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

}
