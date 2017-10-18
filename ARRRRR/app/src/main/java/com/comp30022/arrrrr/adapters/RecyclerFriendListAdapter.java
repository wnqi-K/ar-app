package com.comp30022.arrrrr.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.comp30022.arrrrr.utils.Constants;

import java.util.ArrayList;

/**
 * This adapter is used to display the friend list in a recycler list.
 * Created by Wenqiang Kuang on 9/10/2017.
 */

public class RecyclerFriendListAdapter extends RecyclerView.Adapter<RecyclerFriendListAdapter.FriendViewHolder> {
    private static final int AVATAR_SIZE = 200;
    private static ClickListener clickListener;
    private ArrayList<User> mAllFriends;
    private Context mContext;

    public static class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CardView mCardView;
        public TextView mFriendName;
        public TextView mFriendEmail;
        public ImageView mFriendAvatar;
        public TextView mFriendDistance;
        public FriendViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mCardView = (CardView) itemView.findViewById(R.id.friend_list_user_card);
            mFriendName = (TextView) itemView.findViewById(R.id.friend_list_username);
            mFriendEmail = (TextView) itemView.findViewById(R.id.friend_list_user_email);
            mFriendAvatar = (ImageView) itemView.findViewById(R.id.friend_list_user_avatar);
            mFriendDistance = (TextView) itemView.findViewById(R.id.friend_list_user_distance);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public RecyclerFriendListAdapter(ArrayList<User> friendList, Context context) {
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
        UserManagement userManagement = UserManagement.getInstance();
        User user = mAllFriends.get(position);
        String userID = user.getUid();
        holder.mFriendName.setText(user.getUsername());
        holder.mFriendEmail.setText(user.getEmail());
        if(userManagement.getFriendLocation(userID)!=null){
            holder.mFriendDistance.setText(Constants.DISTANCE_PREFIX + userManagement.getFriendLocation(userID).toString());
        }else{
            holder.mFriendDistance.setText(Constants.UNKNOWN_POSITION);
        }

        Bitmap avatar;
        try {
            avatar = UserManagement.getInstance().getUserProfileImage(userID, mContext);
        } catch (Exception e) {
            avatar = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.portrait_photo);
        }
        holder.mFriendAvatar.setImageBitmap(Bitmap.createScaledBitmap(avatar, AVATAR_SIZE, AVATAR_SIZE, false));
    }

    @Override
    public int getItemCount() {
        return this.mAllFriends.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecyclerFriendListAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }
}
