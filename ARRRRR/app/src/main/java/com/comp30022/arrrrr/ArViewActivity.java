package com.comp30022.arrrrr;

import com.comp30022.arrrrr.ar.*;
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
import java.io.IOException;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;

/**
 * Created by Xiaoyu GUO on 19/09/17
 */

public class ArViewActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        OnLocationChangedListener, OnAzimuthChangedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "ArViewActivity";
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";

    /**
     * Camera Class to get camera Preview
     * */
    private Camera mCamera;

    /**
     * SurfaceHolder is to track to surface layout
     * */
    private SurfaceHolder mSurfaceHolder;

    /**
     * boolean value to track whether the camera is on
     * */
    private boolean isCameraviewOn = false;

    /**
     * poi track the POI for ar, determining whether to show ar image
     **/
    private AugmentedPOI mPoi;

    /**
     * amzimuth factors
     * */
    private double mAzimuthReal = 0;
    private double mAzimuthTeoretical = 0;
    private MyCurrentAzimuth myCurrentAzimuth;

    /**
     * location factors
     * */
    private double mMyLatitude = 0;
    private double mMyLongitude = 0;
    private MyCurrentLocation myCurrentLocation;

    TextView descriptionTextView;
    ImageView pointerIcon;

    /**
     * cohesive in amzimuth calculation
     * */
    private AziCalculator calculator;

    /**
     * camera permission helper
     * */
    private CamPermissionHelper camPerm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setupListeners();
        setupLayout();
        setupUtil();

        setAugmentedRealityPoint();

    }

    @Override
    protected void onStop() {
        myCurrentAzimuth.stop();
        myCurrentLocation.stop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (!camPermissioncGranted()) {
            if (camPerm.getFromPref(this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (!camPermissioncGranted()) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        }
        super.onResume();
        myCurrentAzimuth.start();
        myCurrentLocation.start();
    }

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

    private void updateDescription() {
        descriptionTextView.setText(mPoi.getPoiName() + " azimuthTeoretical "
                + mAzimuthTeoretical + " azimuthReal " + mAzimuthReal + " latitude "
                + mMyLatitude + " longitude " + mMyLongitude);
    }

    @Override
    public void onLocationChanged(Location location) {
        mMyLatitude = location.getLatitude();
        mMyLongitude = location.getLongitude();
        mAzimuthTeoretical = calculateTeoreticalAzimuth();
        Toast.makeText(this,"latitude: "+location.getLatitude()+
                " longitude: "+location.getLongitude(), Toast.LENGTH_SHORT).show();
        updateDescription();
    }

    @Override
    public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {
        mAzimuthReal = azimuthChangedTo;
        mAzimuthTeoretical = calculateTeoreticalAzimuth();

        pointerIcon = (ImageView) findViewById(R.id.icon);

        double minAngle = calculator.calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
        double maxAngle = calculator.calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

        if (calculator.isBetween(minAngle, maxAngle, mAzimuthReal)) {
            pointerIcon.setVisibility(View.VISIBLE);
        } else {
            pointerIcon.setVisibility(View.INVISIBLE);
        }

        updateDescription();
    }


    private void setAugmentedRealityPoint() {
        mPoi = new AugmentedPOI(
                "Kościół Marciacki",
                "Kościół Marciacki w Krakowie",
                50.06169631,
                19.93919566
        );
    }

    /** Set Up Functions
     * TODO: retrieve POI data
     * set up listener: location and sensor
     * set up surface layout
     * set up utils: calcultor, camera preference helper
     * */

    private void setupListeners() {
        myCurrentLocation = new MyCurrentLocation(this, this);
        myCurrentLocation.buildGoogleApiClient(this);
        myCurrentLocation.start();

        myCurrentAzimuth = new MyCurrentAzimuth(this, this);
        myCurrentAzimuth.start();
    }

    private void setupLayout() {
        descriptionTextView = (TextView) findViewById(R.id.cameraTextView);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        //mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void setupUtil() {
        this.calculator = new AziCalculator();
        this.camPerm = new CamPermissionHelper();
    }

    /**
     * implements SurfaceHolder.Callback Here */

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

        if (camPermissioncGranted()) {
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


    /** permission related functions here
     * TODO: need to move it to mainViewActivity
     * */

    public boolean camPermissioncGranted(){
        return ContextCompat.checkSelfPermission(ArViewActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }


    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(ArViewActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(ArViewActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(ArViewActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        camPerm.startInstalledAppDetailsActivity(ArViewActivity.this);
                    }
                });

        alertDialog.show();
    }

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
                            showAlert();
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            camPerm.saveToPreferences(ArViewActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}