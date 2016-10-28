package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

import beans.RegistrationBean;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "VehicleRegistration.db";
    public static final String TABLE_NAME = "registrations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_RFID = "rfid";
    public static final String COLUMN_VEHICLE_NUMBER = "vehicle_number";
    public static final String COLUMN_Employee_NUMBER = "employee_number";
    public static final String COLUMN_Employee_NAME = "employee_name";

    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table registrations " +
                        "(id integer primary key, rfid text,vehicle_number text,employee_name text, employee_number text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertVehicle(String rfid, String vehiclenummber, String employeeName, String employeeNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RFID, rfid);
        contentValues.put(COLUMN_VEHICLE_NUMBER, vehiclenummber);
        contentValues.put(COLUMN_Employee_NUMBER, employeeNumber);
        contentValues.put(COLUMN_Employee_NAME, employeeName);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }


    public Integer deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, null, null);
    }

    public ArrayList<RegistrationBean> getAllVehicles() {
        ArrayList<RegistrationBean> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            String rfid = res.getString(res.getColumnIndex(COLUMN_RFID));
            String vehicleNumber = res.getString(res.getColumnIndex(COLUMN_VEHICLE_NUMBER));
            String employeeNumber = res.getString(res.getColumnIndex(COLUMN_Employee_NUMBER));
            String employeeName = res.getString(res.getColumnIndex(COLUMN_Employee_NAME));

            RegistrationBean registrationBean = new RegistrationBean(rfid, vehicleNumber, employeeName, employeeNumber);
            array_list.add(registrationBean);
            res.moveToNext();
        }

        res.close();
        db.close();
        return array_list;
    }
}