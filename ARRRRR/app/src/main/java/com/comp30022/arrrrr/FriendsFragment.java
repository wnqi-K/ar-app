package com.comp30022.arrrrr;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.arrrrr.adapters.RecyclerFriendListAdapter;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.services.FirebaseIDService;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

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
        // refresh Firebase Token if user change devices
        /*String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentToken = UserManagement.getInstance().getReceiverFirebaseToken(Uid,getActivity());*/

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        new FirebaseIDService().sendRegistrationToServer(refreshedToken,getActivity());

        /*if(!refreshedToken.equals(currentToken)){
            new FirebaseIDService().
                    sendRegistrationToServer(refreshedToken,getActivity());
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_friend_list, container, false);
        final Context context = view.getContext();

        RecyclerView recyclerView;
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_friend_list);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(llm);

        final ArrayList<User> allFriends = getFriendList();
        RecyclerFriendListAdapter adapter = new RecyclerFriendListAdapter(allFriends, getActivity());
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new RecyclerFriendListAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                User user = allFriends.get(position);
                ChatActivity.startActivity(context, user.getUid());
            }
        });

        return view;
    }

    /**
     * This method is to create an arraylist of friends.
     * It would be used by the recycler view to display and lead to create new chat room.
     */
    private ArrayList<User> getFriendList(){
        MainViewActivity activity = (MainViewActivity)getActivity();
        UserManagement friendManagement = activity.getUserManagement();
        ArrayList<User> friendList = (ArrayList<User>) friendManagement.getFriendList();
        ArrayList<User> adminList = (ArrayList<User>) friendManagement.getAdminList();
      
        ArrayList<User> allFriends = new ArrayList<>(friendList);
        allFriends.addAll(adminList);
        return allFriends;
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
