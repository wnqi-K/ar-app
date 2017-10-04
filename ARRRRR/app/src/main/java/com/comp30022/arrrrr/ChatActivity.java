package com.comp30022.arrrrr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.MainActivity;
import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.ChatFragment;
import com.comp30022.arrrrr.adapters.ChatRecyclerAdapter;
import com.comp30022.arrrrr.chat.ChatPresenter;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements TextView.OnEditorActionListener{


    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;

    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    public static void startActivity(Context context,
                                     String receiverUid) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        mRecyclerViewChat = (RecyclerView) findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) findViewById(R.id.edit_text_message);
        mETxtMessage.setOnEditorActionListener(this);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu_chat, menu);
        return true;
    }


    private void init() {
        // set title
        setTitle(getIntent().getExtras().getString(Constants.ARG_RECEIVER));


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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            v.setText(null);
            return true;
        }
        return false;
    }

    private void sendMessage() {
        // Need to check message(length..etc)!!!!
        String message = mETxtMessage.getText().toString();
        String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
        String receiver = getReceiverEmail(receiverUid);
        String receiverFirebaseToken = getReceiverFirebaseToken(receiverUid);
        String sender = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis());
    }

    /**
     * Using user id to find this user's Email
     * */
    private String getReceiverEmail(String receiverUid) {
        User usr = getUserUsingID(receiverUid);
        String Email = null;
        if(usr == null){
            Toast.makeText(getContext(), "get receiver error", Toast.LENGTH_SHORT).show();
        }else{
            Email = usr.getEmail();
        }
        return Email;
    }

    /**
     * Using user id to find this user's FirebaseToken
     * */
    private String getReceiverFirebaseToken(String receiverUid) {
        User usr = getUserUsingID(receiverUid);
        String FirebaseToken = null;
        if(usr == null){
            Toast.makeText(getContext(), "get receiver error", Toast.LENGTH_SHORT).show();
        }else{
            FirebaseToken = usr.getFirebaseToken();
        }
        return FirebaseToken;
    }

    /**
     *  find instance User using user id
     * */
    private User getUserUsingID(String Uid){
        User usr = null;
        UserManagement friendManagement = UserManagement.getInstance();
        ArrayList<User> friendList = (ArrayList<User>) friendManagement.getFriendList();
        for(User user:friendList){
            if(user.getUid().equals(Uid)){
                usr = user;
                break;
            }
        }
        return usr;
    }
}
