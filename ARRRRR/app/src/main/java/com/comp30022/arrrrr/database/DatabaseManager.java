package com.comp30022.arrrrr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.models.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rondo on 16/09/17.
 * Modified by Wenqiang Kuang on 19/09/17.
 */

public class DatabaseManager extends SQLiteOpenHelper {
    //database basic information
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "users.db";
    // mySQL table name
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CHAT_ROOMS = "chat_rooms";
    private static final String TABLE_MESSGAE = "messages";

    //user table column name
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_TOKEN = "firebaseToken";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PHONE_NUM = "phoneNum";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_ADMIN =  "admin";

    //chat&message table column name
    private static final String COLUMN_CHAT_ROOM_ID = "chat_room_id";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_RECEIVER = "receiver";
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_RECEIVER_ID = "receiver_id";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_MESSAGE_ID = "message_id";


    // user information from Firebase
    public List<User> allUsers =  new ArrayList<>();
    public List<User> admins = new ArrayList<>();
    public List<Chat> allChats = new ArrayList<Chat>();

    private static DatabaseManager dbManager;

    public static synchronized DatabaseManager getInstance(Context context) {
        if (dbManager == null) {
            dbManager = new DatabaseManager(context.getApplicationContext());
        }
        return dbManager;
    }

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create users table
        String query = "CREATE TABLE " + TABLE_USERS + "\n(" +
                //COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCERMENT,\n" +
                COLUMN_USER_ID + " varchar(255),\n" +
                COLUMN_EMAIL + " varchar(255),\n" +
                COLUMN_TOKEN + " varchar(255),\n" +
                COLUMN_USERNAME + " varchar(255),\n" +
                COLUMN_PHONE_NUM + " varchar(255),\n" +
                COLUMN_GENDER + " varchar(255),\n" +
                COLUMN_ADDRESS + " varchar(255),\n"+
                COLUMN_ADMIN + " varchar(255)\n"+ ");";
        db.execSQL(query);

        for(User user:allUsers){
            addUser(user,db);
        }

        //create chat rooms table
        String query_chat_rooms = "CREATE TABLE " + TABLE_CHAT_ROOMS + "\n(" +
                COLUMN_CHAT_ROOM_ID + " varchar(255),\n" +
                COLUMN_SENDER + " varchar(255),\n" +
                COLUMN_RECEIVER + " varchar(255)\n" + ");";
        db.execSQL(query_chat_rooms);

        //create messages table
        String query_messgaes = "CREATE TABLE " + TABLE_MESSGAE + "\n(" +
                COLUMN_MESSAGE_ID + " varchar(255),\n" +
                COLUMN_CHAT_ROOM_ID +" varchar(255),\n" +
                COLUMN_SENDER_ID + " varchar(255),\n" +
                COLUMN_RECEIVER_ID + " varchar(255),\n" +
                COLUMN_MESSAGE + " varchar(255)\n" + ");";
        db.execSQL(query_messgaes);

        for(Chat chat:allChats){
            addChatRoom(chat,db);
            addMessage(chat,db);
        }

    }

    /**
     * add message details to the databse
     * */
    private void addMessage(Chat chat, SQLiteDatabase db) {
        String chat_room_id = chat.senderUid + "_" + chat.receiverUid;
        String message_id = chat_room_id + Long.toString(chat.timestamp);
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_ROOM_ID,chat_room_id);
        values.put(COLUMN_MESSAGE_ID,message_id);
        values.put(COLUMN_SENDER_ID,chat.senderUid);
        values.put(COLUMN_RECEIVER_ID,chat.receiverUid);
        values.put(COLUMN_MESSAGE,chat.message);
        if(getCount(db,TABLE_MESSGAE,message_id) == 0){
            db = getWritableDatabase();
            db.insert(TABLE_MESSGAE,null,values);
        }
        else{
            System.out.println(message_id +" already exists in table "+ TABLE_MESSGAE);
        }
    }

    /**
     * add chat room details to the databse
     * */
    private void addChatRoom(Chat chat, SQLiteDatabase db) {
        String chat_room_id = chat.senderUid + "_" + chat.receiverUid;
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_ROOM_ID,chat_room_id);
        values.put(COLUMN_SENDER,chat.sender);
        values.put(COLUMN_RECEIVER,chat.receiver);
        if(getCount(db,TABLE_CHAT_ROOMS,chat_room_id) == 0){
            db = getWritableDatabase();
            db.insert(TABLE_CHAT_ROOMS,null,values);
        }
        else{
            System.out.println(chat_room_id +" already exists in table "+ TABLE_CHAT_ROOMS);
        }
    }

    /**
     * add message details to the databse
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
        if(getCount(db,TABLE_USERS,user.getUid()) == 0){
            db = getWritableDatabase();
            db.insert(TABLE_USERS,null,values);
        }
        else{
            System.out.println(user.getUid() +" already exists in table "+TABLE_USERS);
        }

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
            this.allUsers.add(user);
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
        List<User> mLocalUsers =  new ArrayList<>();
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

    /**
     * check if value exists in table
     * */
    private int getCount(SQLiteDatabase db,String tablename,String value) {
        Cursor c = null;
        try {
            db = getReadableDatabase();
            String query = "select count(*) from " + tablename + " where name = " + value;
            c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        }
        finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

}