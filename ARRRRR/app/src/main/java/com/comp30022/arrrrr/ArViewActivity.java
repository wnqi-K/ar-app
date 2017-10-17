package com.comp30022.arrrrr;

import com.comp30022.arrrrr.ar.*;
import com.comp30022.arrrrr.database.UserManagement;

import android.content.Context;
import android.support.annotation.NonNull;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import java.io.IOException;

import com.comp30022.arrrrr.utils.Constants;
import com.firebase.geofire.util.GeoUtils;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by Xiaoyu GUO on 19/09/17
 */

/**
 * This class is to render the view of AR
 * 1. Open camera
 * 2. show the icon and distance between the destination when the
 * azimuth angle is within the right range
 * 3. Azimuth calculation(steps are listed below):
 * i. get the GPS location of the device and destination point
 * ii. calculate the theoretical azimuth based on GPS data
 * iii. get the real azimuth of the device
 * iv. compare both azimuths based on accuracy and show AR icon
 **/

public class ArViewActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        OnLocationChangedListener, OnAzimuthChangedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "ArViewActivity";

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final int DISTANCE_POP_UP_BOUND = 50;

    /**
     * Camera Class to get camera Preview
     */
    private Camera mCamera;

    /**
     * SurfaceHolder is to track to surface layout
     */
    private SurfaceHolder mSurfaceHolder;

    /**
     * boolean value to track whether the camera is on
     */
    private boolean isCameraviewOn = false;

    /**
     * poi track the POI for ar, location of friend
     **/
    private AugmentedPOI mPoi;

    /**
     * amzimuth factors
     */
    private double mAzimuthReal = 0; //my real azimuth
    private double mAzimuthTeoretical = 0; //friend's azimuth
    private MyCurrentAzimuth myCurrentAzimuth; // my azimuth listener

    /**
     * location factors
     */
    private double mMyLatitude = 0;
    private double mMyLongitude = 0;
    private MyCurrentLocation myCurrentLocation;
    private int distance = 0;

    /**
     * rendering view object
     */
    public TextView descriptionTextView;
    public TextView msgTextView;
    public TextView disTextView;
    public ImageView pointerIcon;
    public ImageView rightIcon;
    public ImageView leftIcon;

    /**
     * cohesive in amzimuth calculation
     */
    private AziCalculator calculator;

    /**
     * camera permission helper
     */
    private CamPermissionHelper camPerm;

    /**
     * Ar UI helper
     */
    private ArUIHelper UIHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // set up
        setupLayout();
        setupListeners();
        setupUtil();

        // get User's POI
        setAugmentedRealityPoint();

    }

    @Override
    protected void onResume() {

        super.onResume();

        camPerm.requestPermission(ArViewActivity.this, ArViewActivity.this); //request camera permission
        myCurrentAzimuth.start(); //start sensor listener
        myCurrentLocation.start();  //start location listener
    }

    @Override
    protected void onStop() {
        super.onStop();
        myCurrentAzimuth.stop();
        myCurrentLocation.stop();
    }


    @Override
    protected void onPause() {
        super.onPause();
        myCurrentAzimuth.stop();
        myCurrentLocation.stop();
    }

    /**
     * start Activity and get the POI
     */

    public static void startActivity(Context context, String uid, LatLng latlng) {
        Intent intent = new Intent(context, ArViewActivity.class);

        intent.putExtra(Constants.UID_Key, uid);
        intent.putExtra(Constants.LATLNG_Key, latlng);

        context.startActivity(intent);
    }

    /**
     * calculate the current theoretical azimuth
     */
    public double calculateTeoreticalAzimuth() {
        double dX = mPoi.getPoiLatitude() - mMyLatitude;
        double dY = mPoi.getPoiLongitude() - mMyLongitude;

        double phiAngle;
        double tanPhi;
        double azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quater
            return azimuth = phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            return azimuth = 180 - phiAngle;
        } else if (dX < 0 && dY < 0) { // III
            return azimuth = 180 + phiAngle;
        } else if (dX > 0 && dY < 0) { // IV
            return azimuth = 360 - phiAngle;
        }

        return phiAngle;
    }

    /**
     * update the text descirption when the location changes
     */
    private void updateDescription() {
        descriptionTextView.setText(mPoi.getPoiName() + " azimuthTeoretical "
                + mAzimuthTeoretical + " azimuthReal " + mAzimuthReal + " latitude "
                + mMyLatitude + " longitude " + mMyLongitude);
    }

    /**
     * update the distance between when the location changes
     */
    private void updateDistanceText() {
        disTextView.setText(this.distance + "m");
    }

    /**
     * update the ar instruction text
     */
    private void updateMsg(String msg) {
        msgTextView.setText(msg);
    }

    /**
     * input POI here
     */
    private void setAugmentedRealityPoint() {

        Intent prevIntent = getIntent();
        String uid = prevIntent.getStringExtra(Constants.UID_Key);
        LatLng latlng = prevIntent.getParcelableExtra(Constants.LATLNG_Key);

        if (latlng == null) {
            //send a default location
            mPoi = new AugmentedPOI(
                    "lweo27942jl3sdsk",      //uid
                    "Xiaoyu Guo",            //username
                    50.06169631,             //Latitude
                    19.93919566              //Longitude
            );
            return;
        } else {
            double lat = latlng.latitude;
            double lng = latlng.longitude;
            String name = UserManagement.getInstance().getUserDisplayName(uid);

            mPoi = new AugmentedPOI(uid, name, lat, lng);
            return;
        }
    }


    /** -------------------------------- Set Up Functions ---------------------------------------*/

    /**
     * set up listener: location and sensor
     */
    private void setupListeners() {

        myCurrentLocation = new MyCurrentLocation(this, this, this);
        myCurrentLocation.buildGoogleApiClient(this);
        myCurrentLocation.start();

        //set up sensor receiver
        myCurrentAzimuth = new MyCurrentAzimuth(this, this);
        myCurrentAzimuth.start();
    }

    /**
     * set up surface layout
     */
    private void setupLayout() {
        descriptionTextView = (TextView) findViewById(R.id.cameraTextView);
        msgTextView = (TextView) findViewById(R.id.msg);
        disTextView = (TextView) findViewById(R.id.distance);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    /**
     * set up utils: calculator, camera preference helper
     */
    private void setupUtil() {
        this.calculator = new AziCalculator();
        this.camPerm = new CamPermissionHelper();
        this.UIHelper = new ArUIHelper();
    }

    /** --------------------------------- interfaces implementation ----------------------------------------*/

    /**
     * This function upgrades user's real-time location
     * and meanwhile updates description
     */

    @Override
    public void onLocationChanged(Location location) {
        mMyLatitude = location.getLatitude();
        mMyLongitude = location.getLongitude();
        mAzimuthTeoretical = calculateTeoreticalAzimuth();
        this.distance = (int)GeoUtils.distance(mMyLatitude, mMyLongitude,
                mPoi.getPoiLatitude(), mPoi.getPoiLongitude());

        boolean hasDisplayed = false;
        if (this.distance < DISTANCE_POP_UP_BOUND){
            if(!hasDisplayed){
                UIHelper.showAlert(this, this, this.mPoi);
                hasDisplayed = true;
            }
        }

        Toast.makeText(this, "latitude: " + location.getLatitude() + " longitude: "  //show new LatLng
                + location.getLongitude(), Toast.LENGTH_SHORT).show();

        updateDescription();     //update location in textView
        updateDistanceText();    //update new distance
    }

    /**
     * This function determine when to show the AR ICON.
     * When the orientation of the phone changes, we need to calculate accuracy,compare both angles
     * and if the current angle is within accuracy we can show a pointer on the screen.
     */
    @Override
    public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {
        mAzimuthReal = azimuthChangedTo;
        mAzimuthTeoretical = calculateTeoreticalAzimuth();

        pointerIcon = (ImageView) findViewById(R.id.icon);
        rightIcon = (ImageView) findViewById(R.id.right);
        leftIcon = (ImageView) findViewById(R.id.left);

        double minAngle = calculator.calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
        double maxAngle = calculator.calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

        //if within the range, show ICON
        if (calculator.isBetween(minAngle, maxAngle, mAzimuthReal)) {
            //UIHelper.setVisibility(pointerIcon, rightIcon, leftIcon);
            pointerIcon.setVisibility(View.VISIBLE);
            rightIcon.setVisibility(View.INVISIBLE);
            leftIcon.setVisibility(View.INVISIBLE);
            updateMsg(Constants.CORRECT_MSG);

        } else {
            pointerIcon.setVisibility(View.INVISIBLE);
            if (calculator.turnLeft(mAzimuthTeoretical, mAzimuthReal)) {
                rightIcon.setVisibility(View.INVISIBLE);

                leftIcon.setVisibility(View.VISIBLE);
                //UIHelper.setVisibility(leftIcon, rightIcon, pointerIcon);
                updateMsg(Constants.TURN_LEFT_MSG);
            } else {
                leftIcon.setVisibility(View.INVISIBLE);

                rightIcon.setVisibility(View.VISIBLE);
                //UIHelper.setVisibility(rightIcon, pointerIcon, leftIcon);
                updateMsg(Constants.TURN_RIGHT_MSG);
            }
        }

        updateDescription();
    }

    /**
     * -------------------  implements SurfaceHolder.Callback Here -----------------------------
     */

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (isCameraviewOn) {
            mCamera.stopPreview();
            isCameraviewOn = false;
        }

        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                isCameraviewOn = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (camPerm.camPermissioncGranted(ArViewActivity.this)) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        isCameraviewOn = false;
    }


    /** ----------------------- permission related functions here ------------------------------- */

    /**
     * implement ActivityCompat.OnRequestPermissionsResultCallback interface
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            camPerm.showSettingsAlert(ArViewActivity.this, ArViewActivity.this);
                        } else if (!showRationale) {
                            camPerm.saveToPreferences(ArViewActivity.this, Constants.ALLOW_KEY, true);
                        }
                    }
                }
            }
        }
    }
}
