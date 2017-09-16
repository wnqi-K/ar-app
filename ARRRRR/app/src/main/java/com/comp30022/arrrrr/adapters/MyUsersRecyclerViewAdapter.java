package com.comp30022.arrrrr.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.ChatActivity;

import java.util.List;


public class MyUsersRecyclerViewAdapter extends RecyclerView.Adapter<MyUsersRecyclerViewAdapter.ViewHolder> {

    private final List<User> mlist;
    private final Context mContext;

    public MyUsersRecyclerViewAdapter(List<User> users, Context context) {
        mlist = users;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mUser = mlist.get(position);
        // set icon
        holder.mIdView.setText(Integer.toString(position+1));
        // set email
        holder.mContentView.setText(mlist.get(position).email);
        holder.mContentView.setOnClickListener(new View.OnClickListener(){

            // click event
            @Override
            public void onClick(View v){
                ChatActivity.startActivity(mContext,
                        holder.mUser.email,
                        holder.mUser.uid,
                        holder.mUser.firebaseToken);
//                Toast.makeText(mContext, "hello", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public User mUser;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
