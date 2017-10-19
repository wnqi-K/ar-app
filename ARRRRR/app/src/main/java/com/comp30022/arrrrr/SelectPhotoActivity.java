package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.comp30022.arrrrr.userProfile.CropOption;
import com.comp30022.arrrrr.userProfile.CropOptionAdapter;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectPhotoActivity extends Activity {

    private Uri selectedImage;
    private ImageView viewImage;
    private Bitmap photo;
    private Button btn_selectPhoto;
    private Button btn_confirmPhoto;
    private String userID;
    private ProgressDialog mProgressDialog;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 3;
    private static final int PICK_FROM_FILE = 2;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo);
        btn_selectPhoto = (Button) findViewById(R.id.btnSelectPhoto);
        btn_confirmPhoto = (Button) findViewById(R.id.btnConfirmPhoto);
        viewImage = (ImageView) findViewById(R.id.viewImage);
        mProgressDialog = new ProgressDialog(SelectPhotoActivity.this);

        //declare the database reference object. This is what we use to access the database.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        btn_selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btn_confirmPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmImage(v);
            }
        });

    }

    /**
     * @Override public boolean onCreateOptionsMenu(Menu menu) {
     * // Inflate the menu; this adds options to the action bar if it is present.
     * getMenuInflater().inflate(R.menu.main, menu);
     * return true;
     * }
     **/


    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SelectPhotoActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, PICK_FROM_CAMERA);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_FROM_FILE);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {

            //Take picture use camera
            case PICK_FROM_CAMERA:
                selectedImage = data.getData();
                doCrop();
                break;

            //Choose picture from gallery
            case PICK_FROM_FILE:
                selectedImage = data.getData();
                doCrop();
                break;

            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    photo = extras.getParcelable("data");
                    viewImage.setImageBitmap(photo);
                    btn_confirmPhoto.setVisibility(View.VISIBLE);
                }
                File file = new File(selectedImage.getPath());
                if (file.exists()) file.delete();
                break;
        }
    }

    /**
     * Crop Photo
     **/
    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            return;
        } else {
            intent.setData(selectedImage);
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);

                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
                    co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);

                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (selectedImage != null) {
                            getContentResolver().delete(selectedImage, null, null);
                            selectedImage = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    /**
     * Confirm Photo and upload to database
     **/
    public void confirmImage(View v) {
        mProgressDialog.setMessage("Uploading Image...");
        mProgressDialog.show();

        encodeBitmapAndSaveToFirebase(photo);

        mProgressDialog.cancel();

        Intent intent = new Intent(this, MainViewActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        myRef.child(Constants.ARG_USERS).child(userID).child("imageUrl").setValue(imageEncoded);
    }
}
