package com.comp30022.arrrrr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rondo on 9/16/17.
 */

public class DatabaseManager extends SQLiteOpenHelper {

    /**
     * database basic information
     * */

    private static final int DATABASE_VERSION = 1;
    //file name
    private static final String DATABASE_NAME = "users.db";
    // mySQL table name
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_TOKEN = "firebaseToken";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PHONE_NUM = "phoneNum";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ADMIN =  "admin";



    /**
     * user information from Firebase
     * */
    public List<User> mUsers =  new ArrayList<User>();
    public List<User> admins = new ArrayList<User>();





    public DatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    /**
     * create database
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create a new tabel called users
        String query = "CREATE TABLE " + TABLE_USERS + "\n(" +
                //COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCERMENT,\n" +
                COLUMN_USER_ID + " varchar(255),\n" +
                COLUMN_EMAIL + " varchar(255),\n" +
                COLUMN_TOKEN + " varchar(255),\n" +
                COLUMN_USERNAME + " varchar(255),\n" +
                COLUMN_PHONE_NUM + " varchar(255),\n" +
                COLUMN_GENDER + " varchar(255),\n" +
                COLUMN_ADDRESS + " varchar(255),\n"+
                COLUMN_ADMIN + " varchar(255)\n"+
                ");";


        // execute query
        db.execSQL(query);

        for(User user:mUsers){
            addUser(user,db);
        }
    }

    /**
     * update datebase
     * */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_USERS);
        onCreate(db);
    }

    /**
     * add a new user to the database
     * */
    public void addUser(User user,SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID,user.getUid());
        values.put(COLUMN_EMAIL,user.getEmail());
        values.put(COLUMN_TOKEN,user.getFirebaseToken());
        values.put(COLUMN_USERNAME,user.getUsername());
        values.put(COLUMN_PHONE_NUM,user.getPhoneNum());
        values.put(COLUMN_GENDER,user.getGender());
        values.put(COLUMN_ADDRESS,user.getAddress());
        values.put(COLUMN_ADMIN,user.getAdmin());
        db.insert(TABLE_USERS,null,values);
    }

    /**
     * add a user from the database
     * */
    public void deleteUser(){

    }

    /**
     * Print out the database as a string
     * */
    public String databseToString(){
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE 1";

        //Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //Move to the first row in your results
        c.moveToFirst();

        while(!c.isAfterLast()){
            if(c.getString(c.getColumnIndex(COLUMN_USER_ID)) != null){
                dbString += c.getString(c.getColumnIndex(COLUMN_USER_ID));
                dbString += "\n";
            }
            c.moveToNext();
        }
        db.close();
        return dbString;


    }



    /**
     *  if succcess add user to the list
     * */
    public void getUsersSuccessfully(List<User> users){
        for(User user:users){
            this.mUsers.add(user);
        }
        databseToString();
    }

    /**
     * if failure print error
     * */
    public void getUsersUnsuccessfully(String message){}

    /**
     * return users information stored locally
     * */
    public List<User> getAllUsers() {
        List<User> mLocalUsers =  new ArrayList<User>();
        transferData(mLocalUsers);
        return mLocalUsers;
    }



    public List<User> getAdminFriends() {
        List<User> mLocalUsers =  getAllUsers();

        for(User user:mLocalUsers){
            if ((user.getAdmin() != null)&&(TextUtils.equals(user.getAdmin(), "True"))) {
                admins.add(user);
            }
        }

        return admins;
    }

    /**
     * transfer data from database to a list
     * */
    private void transferData(List<User> mLocalUsers) {

        System.out.println("printing....");
        System.out.println(databseToString());

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE 1";

        //Cursor point to a location in your results
        Cursor cursor = db.rawQuery(query, null);

        try{
            // looping through all rows and adding to list
            if(cursor.moveToFirst()){
                do{
                    String uid = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                    String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                    String firebaseToken = cursor.getString(cursor.getColumnIndex(COLUMN_TOKEN));
                    String admin = cursor.getString(cursor.getColumnIndex(COLUMN_ADMIN));
                    String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                    String gender = cursor.getString(cursor.getColumnIndex(COLUMN_GENDER));
                    String phoneNum = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUM));
                    String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
                    mLocalUsers.add(new User(uid,email,firebaseToken,username,
                            phoneNum,gender,address,admin));
                }while(cursor.moveToNext());

            }
        }
        catch (SQLiteException e){
            Log.d("SQL ERROR",e.getMessage());
        }
        finally {
            cursor.close();
            db.close();

        }
    }

}
