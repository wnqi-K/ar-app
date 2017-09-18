package com.comp30022.arrrrr.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.models.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This adapter is used to display admin friend lists in a expandable list.
 * Created by Wenqiang Kuang on 18/09/2017.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> mListTitle;
    private HashMap<String, ArrayList<User>> mUserFriends;


    public ExpandableListAdapter(Context context, List<String> listTitle, HashMap<String, ArrayList<User>> userFriends) {
        this.mContext = context;
        this.mListTitle = listTitle;
        this.mUserFriends = userFriends;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.mUserFriends.get(this.mListTitle.get(listPosition)).get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String userId = ((User) getChild(listPosition, expandedListPosition)).getUid();
        final String userContent = ((User) getChild(listPosition, expandedListPosition)).getEmail();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_user, null);
        }
        TextView expandedListTextViewOne = (TextView) convertView
                .findViewById(R.id.user_id);
        expandedListTextViewOne.setText(userId);
        TextView expandedListTextViewTwo = (TextView) convertView
                .findViewById(R.id.user_content);
        expandedListTextViewTwo.setText(userContent);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.mUserFriends.get(this.mListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.mListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_list_title, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.group_title);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
