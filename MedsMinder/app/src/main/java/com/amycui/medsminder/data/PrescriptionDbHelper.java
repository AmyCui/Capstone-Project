package com.amycui.medsminder.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.amycui.medsminder.data.PrescriptionContract.UserEntry;
import com.amycui.medsminder.data.PrescriptionContract.PrescriptionEntry;
import com.amycui.medsminder.data.PrescriptionContract.RemindersEntry;

public class PrescriptionDbHelper  extends SQLiteOpenHelper{

    // Database version. Needs to be changed on each schema change. Starting value is 1.
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "prescription.db";

    public PrescriptionDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create User table
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY," +
                UserEntry.COLUMN_UID + " TEXT UNIQUE NOT NULL, " +
                UserEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                UserEntry.COLUMN_EMAIL + " TEXT NOT NULL " +
                " );";

        // Create Prescription table
        final String SQL_CREATE_PRESCRIPTION_TABLE = "CREATE TABLE " + PrescriptionEntry.TABLE_NAME + " (" +
                PrescriptionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PrescriptionEntry.COLUMN_NAME + " TEXT, " +
                PrescriptionEntry.COLUMN_UNIT + " TEXT, " +
                PrescriptionEntry.COLUMN_DATE + " TEXT, " +
                PrescriptionEntry.COLUMN_IMAGE_URL + " TEXT, " +
                PrescriptionEntry.COLUMN_DOSAGE + " TEXT," +
                PrescriptionEntry.COLUMN_FREQUENCY + " TEXT, " +
                PrescriptionEntry.COLUMN_REPEAT_UNIT + " TEXT, " +
                PrescriptionEntry.COLUMN_USER_KEY + " TEXT NOT NULL, " +
                // Set up the user column as a foreign key to user table.
                " FOREIGN KEY (" + PrescriptionEntry.COLUMN_USER_KEY + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry._ID + ") " +
                " );";

        // Create Reminders table
        final String SQL_CREATE_REMINDERS_TABLE = "CREATE TABLE " + PrescriptionContract.RemindersEntry.TABLE_NAME + " (" +
                RemindersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RemindersEntry.COLUMN_PRESCRIPTION_ID + " TEXT NOT NULL, " +
                RemindersEntry.COLUMN_TRIGGER_TIME + " TEXT NOT NULL, " +
                RemindersEntry.COLUMN_REPEAT_MS + " TEXT NOT NULL, " +
                RemindersEntry.COLUMN_START_DATE + " TEXT NOT NULL, " +
                RemindersEntry.COLUMN_END_DATE + " TEXT NOT NULL, " +
                // Set up the prescription id column as a foreign key to prescription table.
                " FOREIGN KEY (" + RemindersEntry.COLUMN_PRESCRIPTION_ID + ") REFERENCES " +
                PrescriptionEntry.TABLE_NAME + " (" + PrescriptionEntry._ID + ") " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PRESCRIPTION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REMINDERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int i1) {
        // The two tables stores important user data
        // On upgrade, save the user data, Just change schema accordingly
        onCreate(sqLiteDatabase);
    }
}
