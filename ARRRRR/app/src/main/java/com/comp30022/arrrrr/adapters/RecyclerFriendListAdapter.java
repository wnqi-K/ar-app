package com.comp30022.arrrrr.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;

/**
 * Created by Ricky_KUANG on 9/10/2017.
 */

public class RecyclerFriendListAdapter extends RecyclerView.Adapter<RecyclerFriendListAdapter.FriendViewHolder>{

    private ArrayList<User> mAllFriends;
    private Context mContext;

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView mFriendName;
        TextView mFriendGender;
        ImageView mFriendAvatar;

        FriendViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView)itemView.findViewById(R.id.friend_list_user_card);
            mFriendName = (TextView)itemView.findViewById(R.id.friend_list_username);
            mFriendGender = (TextView)itemView.findViewById(R.id.friend_list_user_gender);
            mFriendAvatar = (ImageView)itemView.findViewById(R.id.friend_list_user_avatar);
        }
    }

    public RecyclerFriendListAdapter(ArrayList<User> friendList, Context context){
        this.mAllFriends = friendList;
        this.mContext = context;
    }


    @Override
    public RecyclerFriendListAdapter.FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_card_user, parent, false);
        FriendViewHolder holder = new FriendViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerFriendListAdapter.FriendViewHolder holder, int position) {
        User user = mAllFriends.get(position);
        String userID = user.getUid();
        holder.mFriendName.setText(user.getUsername());
        holder.mFriendGender.setText(user.getGender());
        Bitmap imageBitmap = UserManagement.getInstance().getUserProfileImage(userID, mContext);
        holder.mFriendAvatar.setImageBitmap(imageBitmap);
    }

    @Override
    public int getItemCount() {
        return this.mAllFriends.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
