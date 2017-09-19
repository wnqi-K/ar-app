package com.comp30022.arrrrr.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by Wenqiang Kuang on 19/09/2017.
 */

public class ListViewAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    private List<User> mUserList = null;
    private ArrayList<User> mUsers;

    public ListViewAdapter(Context context, List<User> userList) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mUserList = userList;
        this.mUsers = new ArrayList<>();
        this.mUsers.addAll(userList);
    }

    public class ViewHolder {
        TextView result;
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public User getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_view_items, null);
            // Locate the TextViews in list_view_item.xml
            holder.result = (TextView) view.findViewById(R.id.search_result_email);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.result.setText(mUsers.get(position).getEmail());
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mUserList.clear();
        if (charText.length() == 0) {
            mUserList.addAll(mUsers);
        } else {
            for (User u : mUsers) {
                if (u.getEmail().toLowerCase(Locale.getDefault()).contains(charText)) {
                    mUserList.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }
}