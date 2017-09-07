package com.comp30022.arrrrr;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.comp30022.arrrrr.UsersFragment.OnListFragmentInteractionListener;
import com.comp30022.arrrrr.models.User;

import java.util.List;


public class MyUsersRecyclerViewAdapter extends RecyclerView.Adapter<MyUsersRecyclerViewAdapter.ViewHolder> {

    private final List<User> users_list;
    private final OnListFragmentInteractionListener mListener;

    public MyUsersRecyclerViewAdapter(List<User> users, OnListFragmentInteractionListener listener) {
        users_list = users;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mUser = users_list.get(position);
        holder.mIdView.setText("1");
        holder.mContentView.setText(users_list.get(position).email);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mUser);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users_list.size();
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
