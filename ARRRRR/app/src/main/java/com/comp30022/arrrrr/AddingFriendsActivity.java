package com.comp30022.arrrrr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import com.comp30022.arrrrr.adapters.ListViewAdapter;
import com.comp30022.arrrrr.database.DatabaseManager;
import com.comp30022.arrrrr.models.User;
import java.util.ArrayList;

/**
 * Adding new friends by searching the precise user email account.
 * Created by Wenqiang Kuang on 16/09/2017.
 */
public class AddingFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    DatabaseManager dbManager = DatabaseManager.getInstance(null);
    ListView mListView;
    ListViewAdapter mViewAdapter;
    SearchView mSearchView;
    ArrayList<User> allUsers = (ArrayList<User>)dbManager.getAllUsers();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_friends);

        mListView = (ListView) findViewById(R.id.search_result_list);

        // Pass results to ListViewAdapter Class
        mViewAdapter = new ListViewAdapter(this, allUsers);

        // Binds the Adapter to the ListView
        mListView.setAdapter(mViewAdapter);

        // Locate the EditText in listview_main.xml
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        mViewAdapter.filter(text);
        return false;
    }
}
