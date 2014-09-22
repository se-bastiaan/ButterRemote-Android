package eu.se_bastiaan.popcorntimeremote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import eu.se_bastiaan.popcorntimeremote.Constants;

public class InstanceDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InstanceEntry.TABLE_NAME + " (" +
                    InstanceEntry._ID + " INTEGER PRIMARY KEY," +
                    InstanceEntry.COLUMN_NAME_IP + TEXT_TYPE + COMMA_SEP +
                    InstanceEntry.COLUMN_NAME_PORT + TEXT_TYPE + COMMA_SEP +
                    InstanceEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    InstanceEntry.COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
                    InstanceEntry.COLUMN_NAME_PASSWORD + TEXT_TYPE +
                    " )";

    public InstanceDbHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing, yet.
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean isEmpty() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(InstanceEntry.TABLE_NAME, null, null, null, null, null, null, "1");
        Boolean empty = cursor.getCount() == 0;
        cursor.close();
        db.close();
        return empty;
    }

}