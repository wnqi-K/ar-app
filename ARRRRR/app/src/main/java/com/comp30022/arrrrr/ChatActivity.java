package com.comp30022.arrrrr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.adapters.ChatRecyclerAdapter;
import com.comp30022.arrrrr.database.DatabaseManager;
import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.utils.ChatInterface;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;;

import java.util.ArrayList;

/**
 * This class is responsible of setting up all the UI interface which will
 * help the user interact with Firebase achieve the functionality of chat
 *
 * @author Zijie Shen
 */
public class ChatActivity extends AppCompatActivity implements ChatInterface.Listener ,TextView.OnEditorActionListener{

    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;
    private String message;

    private ProgressDialog mProgressDialog;
    private ChatRecyclerAdapter mChatRecyclerAdapter;
    private ChatRoomManager mChatRoomManager;

    private String receiverUid;
    private String receiver;
    private String receiverFirebaseToken;
    private String sender;
    private String senderUid;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Chat chat = null;

    @RestrictTo(RestrictTo.Scope.TESTS)
    public String getMessage() {
        return message;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public void setMessage(String message) {
        this.message = message;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public Chat getChat() {
        return chat;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public String getReceiverUid() {
        return receiverUid;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public String getSenderUid() {
        return senderUid;
    }

    private static boolean isActivityOpen = false;

    @RestrictTo(RestrictTo.Scope.TESTS)
    public ChatRoomManager getmChatRoomManager() {
        return mChatRoomManager;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public void setChatRoomManager(ChatRoomManager chatRoomManager) {
        this.mChatRoomManager = chatRoomManager;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public ChatRecyclerAdapter getmChatRecyclerAdapter() {
        return mChatRecyclerAdapter;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public void setChatRecyclerAdapter(ChatRecyclerAdapter mChatRecyclerAdapter) {
        this.mChatRecyclerAdapter = mChatRecyclerAdapter;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public RecyclerView getmRecyclerViewChat() {
        return mRecyclerViewChat;
    }


    public static void startActivity(Context context,
                                     String receiverUid) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        context.startActivity(intent);
    }

    public static boolean isActivityOpen() {
        return isActivityOpen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set all the views
        String username = DatabaseManager.getUserName(getIntent().
                getExtras().
                getString(Constants.ARG_RECEIVER_UID),this);
        setTitle(username);
        setContentView(R.layout.activity_chat);
        mRecyclerViewChat = (RecyclerView) findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) findViewById(R.id.edit_text_message);
        mETxtMessage.setOnEditorActionListener(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        // start a chat room
        init();
    }

    /**
     * get all the information needed for creating a new chat object and start
     * a chat room
     * */
    private void init(){
        receiverUid = getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID);
        receiver = DatabaseManager.getReceiverEmail(receiverUid,this);
        receiverFirebaseToken = DatabaseManager.
                getReceiverFirebaseToken(receiverUid,this);
        if(currentUser != null){
            sender = currentUser.getEmail();
            senderUid = currentUser.getUid();
        }
        mChatRoomManager = new ChatRoomManager(this,
                senderUid,
                receiverUid,
                receiverFirebaseToken,
                this);
        mChatRoomManager.getMessageFromFirebaseUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_history:
                clearHistory(senderUid,receiverUid);
                break;
        }
        return true;
    }

    /**
     * This method will clear history of a chatroom
     * */
    private void clearHistory(String senderUid, String receiverUid) {
        String chat_room_1 = senderUid + "_" + receiverUid;
        String chat_room_2 = receiverUid + "_" + senderUid;
        FirebaseDatabase.getInstance().
                getReference().
                child(Constants.ARG_CHAT_ROOMS).
                child(chat_room_1).
                removeValue();
        FirebaseDatabase.getInstance().
                getReference().
                child(Constants.ARG_CHAT_ROOMS).
                child(chat_room_2).
                removeValue();
        finish();
        startActivity(this,getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.isActivityOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isActivityOpen = false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            message = mETxtMessage.getText().toString();
            sendMessage();
            v.setText(null);
            return true;
        }
        return false;
    }

    /*
    * create a new chat object and send it to the firebase database
    * */
    public void sendMessage() {
        // Need to check message(length..etc)!!!!
        if(message.isEmpty()){
            mETxtMessage.setError(Constants.NON_EMPTY);
        }else{
            chat = new Chat(sender,
                    receiver,
                    senderUid,
                    receiverUid,
                    message,
                    System.currentTimeMillis());
            mChatRoomManager.sendMessageToFirebaseUser(chat);
        }
    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText(Constants.EMPTY_STRING);
        Toast.makeText(this,Constants.MESSAGE_SENT , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
