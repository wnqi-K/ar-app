package com.comp30022.arrrrr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.comp30022.arrrrr.services.LocationSharingService;

public class AccountPrivacyActivity extends AppCompatActivity {

    private Button mButtonClearRecords;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account_privacy);

        mButtonClearRecords = (Button)findViewById(R.id.button_clear_records);
        mButtonClearRecords.setOnClickListener(mOnClearRecordsClickListener);

    }

    /**
     * OnClickListener for clearing records button
     */
    private View.OnClickListener mOnClearRecordsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mButtonClearRecords.setEnabled(false);
            mButtonClearRecords.setText(R.string.text_removing_location_records);
           // LocationSharingService.requestClearLocationRecords(this);
        }
    };

}
