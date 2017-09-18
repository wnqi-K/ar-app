package com.comp30022.arrrrr;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.arrrrr.adapters.MyUsersRecyclerViewAdapter;
import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This fragment is to contain five admin friends and newly added friends.
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

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.added_friend_list);
        Context context = view.getContext();
        ArrayList<User> friendList = (ArrayList<User>) ((MainViewActivity)getActivity()).getRequestUsers().getFriendManagement().getFriendList();
        ArrayList<User> adminList = (ArrayList<User>) ((MainViewActivity)getActivity()).getRequestUsers().getAdminFriends().getFriendList();

        HashMap<String, ArrayList<User>> expandableList = new HashMap<>();
        expandableList.put("Pre-set Friends", adminList);
        expandableList.put("All_Users", friendList);

        recyclerView.setAdapter(new MyUsersRecyclerViewAdapter(adminList, context));
        return view;
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
