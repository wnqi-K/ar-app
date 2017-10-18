package com.comp30022.arrrrr.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * This class is used to display messages appropriately in a chat room
 *
 * * @author zijie shen
 * */
public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private static final int AVATAR_SIZE = 200;

    private List<Chat> mChats;
    private FirebaseUser currentUser;
    private Context mContext;


    public ChatRecyclerAdapter(List<Chat> chats,Context context) {
        mChats = chats;
        mContext = context;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void add(Chat chat) {
        mChats.add(chat);
        notifyItemInserted(mChats.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.item_chat_mine, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.item_chat_other, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(currentUser != null){
            if (TextUtils.equals(mChats.get(position).senderUid,
                    currentUser.getUid())) {
                configureMyChatViewHolder((MyChatViewHolder) holder, position);
            } else {
                configureOtherChatViewHolder((OtherChatViewHolder) holder, position);
            }
        }
    }


    @Override
    public int getItemCount() {
        if (mChats != null) {
            return mChats.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(currentUser != null){
            if (TextUtils.equals(mChats.get(position).senderUid,
                    currentUser.getUid())) {
                return VIEW_TYPE_ME;
            } else {
                return VIEW_TYPE_OTHER;
            }
        }
        return VIEW_TYPE_ME;
    }

    /**
     * generate a view which contains the current user's image and his/her delivered message
     * */
    private void configureMyChatViewHolder(MyChatViewHolder myChatViewHolder, int position) {
        Chat chat = mChats.get(position);
        myChatViewHolder.txtChatMessage.setText(chat.message);

        Bitmap avatar;
        try {
            avatar = UserManagement.getInstance().getUserProfileImage(chat.senderUid, mContext);
        } catch (Exception e) {
            avatar = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.portrait_photo);
        }
        myChatViewHolder.userImage.setImageBitmap(Bitmap.createScaledBitmap(avatar, AVATAR_SIZE, AVATAR_SIZE, false));
    }

    /**
     * generate a view which contains the other user's image and his/her delivered message
     * */
    private void configureOtherChatViewHolder(OtherChatViewHolder otherChatViewHolder, int position) {
        Chat chat = mChats.get(position);
        otherChatViewHolder.txtChatMessage.setText(chat.message);

        Bitmap avatar;
        try {
            avatar = UserManagement.getInstance().getUserProfileImage(chat.senderUid, mContext);
        } catch (Exception e) {
            avatar = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.portrait_photo);
        }
        otherChatViewHolder.userImage.setImageBitmap(Bitmap.createScaledBitmap(avatar, AVATAR_SIZE, AVATAR_SIZE, false));

    }



    /**
     * inner classe that handles the manipulation of two elements needed for a chat user
     * */
    private static class MyChatViewHolder extends RecyclerView.ViewHolder {
        private TextView txtChatMessage;
        private ImageView  userImage;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (TextView) itemView.findViewById(R.id.text_view_chat_message);
            userImage = (ImageView)itemView.findViewById(R.id.image_mine);
        }
    }

    /**
     * inner classe that handles the manipulation of two elements needed for a chat user
     * */
    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        private TextView txtChatMessage;
        private ImageView  userImage;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (TextView) itemView.findViewById(R.id.text_view_chat_message);
            userImage = (ImageView)itemView.findViewById(R.id.image_other);
        }
    }
}
