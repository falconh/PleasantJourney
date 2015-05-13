package com.example.han.pleasantjourney;

/**
 * Created by Han on 5/13/2015.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "DataCollect";

    // Contacts table name
    private static final String TABLE_ACCELEROMETER = "accelerometer";
    private static final String TABLE_ROTATION_VECTOR = "rotationvector";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LONG = "longitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_PROCESSED_VALUE = "p_value";
    private static final String KEY_RAW_VALUE = "r_value";
    private static final String KEY_PLAT_NO = "platno";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ACCELEROMETER_TABLE = "CREATE TABLE " + TABLE_ACCELEROMETER + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_LAT + " REAL,"
                + KEY_LONG + " REAL," + KEY_SPEED + " INTEGER,"  + KEY_PROCESSED_VALUE + " REAL,"
                + KEY_PLAT_NO + " TEXT,"
                + KEY_RAW_VALUE + " REAL" + ")";

        String CREATE_ROTATION_VECTOR_TABLE = "CREATE TABLE " + TABLE_ROTATION_VECTOR + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_LAT + " REAL,"
                + KEY_LONG + " REAL," + KEY_SPEED + " INTEGER," + KEY_PLAT_NO + " TEXT,"
                + KEY_RAW_VALUE + " REAL" + ")";

        db.execSQL(CREATE_ACCELEROMETER_TABLE);
        db.execSQL(CREATE_ROTATION_VECTOR_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCELEROMETER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROTATION_VECTOR);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    void addRecordToAccTable(SensorDatabase sensorRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, sensorRecord.latitude); // Contact Name
        values.put(KEY_LONG, sensorRecord.longitude); // Contact Phone
        values.put(KEY_SPEED, sensorRecord.speed);
        values.put(KEY_PROCESSED_VALUE, sensorRecord.p_value);
        values.put(KEY_RAW_VALUE, sensorRecord.r_value);
        values.put(KEY_PLAT_NO, sensorRecord.platno);

        // Inserting Row
        db.insert(TABLE_ACCELEROMETER, null, values);
        db.close(); // Closing database connection
    }

    void addRecordToRotationTable(SensorDatabase sensorRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, sensorRecord.latitude); // Contact Name
        values.put(KEY_LONG, sensorRecord.longitude); // Contact Phone
        values.put(KEY_SPEED, sensorRecord.speed);
        values.put(KEY_RAW_VALUE, sensorRecord.r_value);
        values.put(KEY_PLAT_NO, sensorRecord.platno);

        // Inserting Row
        db.insert(TABLE_ROTATION_VECTOR, null, values);
        db.close(); // Closing database connection
    }



}
