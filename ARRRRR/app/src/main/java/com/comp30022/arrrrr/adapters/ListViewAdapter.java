package com.comp30022.arrrrr.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.List;


/**
 * Create a list view for searching and return matched user when typing in char.
 * Created by Wenqiang Kuang on 19/09/2017.
 */

public class ListViewAdapter extends BaseAdapter implements Filterable {

    Context mContext;
    LayoutInflater inflater;
    private ItemFilter mFilter = new ItemFilter();
    private List<User> userLibrary = null;
    private List<User> matchedUsers = null;

    public ListViewAdapter(Context context, List<User> userList) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        userLibrary = userList;
        matchedUsers = userList;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class ViewHolder {
        TextView result;
    }

    @Override
    public int getCount() {
        return matchedUsers.size();
    }

    @Override
    public User getItem(int position) {
        return matchedUsers.get(position);
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
        holder.result.setText(matchedUsers.get(position).getEmail());
        return view;
    }

    /**
     * Inner class ItemFilter to filter all matched users that would be displayed on the list view.
     */
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String inputChars = constraint.toString().toLowerCase();

            // Output containing all matched users.
            FilterResults matchedUsers = new FilterResults();

            final List<User> list = userLibrary;
            int count = list.size();
            final ArrayList<User> returnList = new ArrayList<>(count);

            // For all users in the library, if contain 'inputChars', display on list.
            User filterableUser;
            for (int i = 0; i < count; i++) {
                filterableUser = list.get(i);
                if (filterableUser.getEmail().toString().toLowerCase().contains(inputChars)) {
                    returnList.add(filterableUser);
                }
            }

            matchedUsers.values = returnList;
            matchedUsers.count = returnList.size();

            return matchedUsers;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            matchedUsers = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }

    }
}