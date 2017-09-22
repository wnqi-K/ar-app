package com.comp30022.arrrrr;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.comp30022.arrrrr.adapters.ExpandableListAdapter;
import com.comp30022.arrrrr.database.DatabaseManager;
import com.comp30022.arrrrr.models.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This fragment is to contain five pre-placed friends and newly added friends.
 * Created by Wenqiang Kuang on 01/09/2017.
 */

public class FriendsFragment extends Fragment{

    private OnListFragmentInteractionListener mListener;

    public FriendsFragment() {
    }

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);
        final Context context = view.getContext();

        //Used to contain all the friends, including pre-placed friends and new added ones.
        final HashMap<String, ArrayList<User>> expandableList = getFriendLists();

        //Set up the expandable view.
        ExpandableListView expandableListView;
        ExpandableListAdapter expandableListAdapter;
        final List<String> expandableListTitle;
        expandableListView = (ExpandableListView) view.findViewById(R.id.admin_friend_list);
        expandableListTitle = new ArrayList<>(expandableList.keySet());
        expandableListAdapter = new ExpandableListAdapter(context, expandableListTitle, expandableList);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.expandGroup(1);

        //Start a new chat room once friend is clicked.
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                User user = expandableList.get(expandableListTitle.get(groupPosition)).get(childPosition);
                ChatActivity.startActivity(context,
                        user.getEmail(),
                        user.getUid(),
                        user.getFirebaseToken());
                return false;
            }
        });
        return view;
    }

    /**
     * This method is to create a hashmap<String, ArrayList<User>> to contain two required friend list.
     * It would be used by the expandable List view to display and lead to create new chat room.
     */
    private HashMap<String, ArrayList<User>> getFriendLists() {
        DatabaseManager dbManager = DatabaseManager.getInstance(getActivity().getBaseContext());
        ArrayList<User> friendList = (ArrayList<User>) dbManager.allUsers;
        ArrayList<User> adminList = (ArrayList<User>) dbManager.admins;

        HashMap<String, ArrayList<User>> expandableList = new HashMap<>();
        expandableList.put("Pre-placed Friends", adminList);
        expandableList.put("Users", friendList);
        return expandableList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(User user);
    }
}
