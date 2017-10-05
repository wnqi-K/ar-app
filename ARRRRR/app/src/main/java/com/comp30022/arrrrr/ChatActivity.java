package com.comp30022.arrrrr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.adapters.ChatRecyclerAdapter;
import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.utils.ChatInterface;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;

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

    private ProgressDialog mProgressDialog;
    private ChatRecyclerAdapter mChatRecyclerAdapter;
    private ChatRoomManager mChatRoomManager;

    private String receiverUid;
    private String receiver;
    private String receiverFirebaseToken;
    private String sender;
    private String senderUid;

    public static void startActivity(Context context,
                                     String receiverUid) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set all the views
        String username = FindUserInfoHelper.getUserName(getIntent().
                getExtras().
                getString(Constants.ARG_RECEIVER_UID),this);
        setTitle(username);
        setContentView(R.layout.activity_chat);
        mRecyclerViewChat = (RecyclerView) findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) findViewById(R.id.edit_text_message);
        mETxtMessage.setOnEditorActionListener(this);

        // start a chat room
        init();
    }

    /**
     * get all the information needed for creating a new chat object and start
     * a chat room
     * */
    private void init(){
        receiverUid = getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID);
        receiver = FindUserInfoHelper.getReceiverEmail(receiverUid,this);
        receiverFirebaseToken = FindUserInfoHelper.
                getReceiverFirebaseToken(receiverUid,this);
        sender = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
    protected void onResume() {
        super.onResume();
        MainActivity.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.setChatActivityOpen(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            v.setText(null);
            return true;
        }
        return false;
    }

    /*
    * create a new chat object and send it to the firebase database
    * */
    private void sendMessage() {
        // Need to check message(length..etc)!!!!
        String message = mETxtMessage.getText().toString();
        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis());
        mChatRoomManager.sendMessageToFirebaseUser(chat);
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
