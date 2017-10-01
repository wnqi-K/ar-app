package com.comp30022.arrrrr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import com.comp30022.arrrrr.MainActivity;
import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.ChatFragment;
import com.comp30022.arrrrr.utils.Constants;

public class ChatActivity extends AppCompatActivity {

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

        // set the register screen fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_content_chat,
                ChatFragment.newInstance(getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID)),
                ChatFragment.class.getSimpleName());
        fragmentTransaction.commit();
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
}
