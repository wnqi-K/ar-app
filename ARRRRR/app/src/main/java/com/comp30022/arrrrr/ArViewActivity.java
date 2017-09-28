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

/**
 * This class is to render the view of AR
 * 1. Open camera
 * 2. show the icon and distance between the destination when the
 * azimuth angle is within the right range
 * 3. Azimuth calculation(steps are listed below):
 *            i. get the GPS location of the device and destination point
 *            ii. calculate the theoretical azimuth based on GPS data
 *            iii. get the real azimuth of the device
 *            iv. compare both azimuths based on accuracy and show AR icon
 **/

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

    public TextView descriptionTextView;
    public ImageView pointerIcon;

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

        // set up
        setupListeners();
        setupLayout();
        setupUtil();

        // get User's POI
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

        //check camera permission
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

    /**
     * calculate the current theoretical azimuth
     * */
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
     * */
    private void updateDescription() {
        descriptionTextView.setText(mPoi.getPoiName() + " azimuthTeoretical "
                + mAzimuthTeoretical + " azimuthReal " + mAzimuthReal + " latitude "
                + mMyLatitude + " longitude " + mMyLongitude);
    }

    /**
     * update POI here
     * TODO: need to update the real-time location by connecting the listerner
     * */
    private void setAugmentedRealityPoint() {
        mPoi = new AugmentedPOI(
                "Kościół Marciacki",
                "Kościół Marciacki w Krakowie",
                50.06169631,
                19.93919566
        );
    }

    /** -------------------------------- Set Up Functions ---------------------------------------*/

    /**
     * set up listener: location and sensor
     * */
    private void setupListeners() {
        myCurrentLocation = new MyCurrentLocation(this, this);
        myCurrentLocation.buildGoogleApiClient(this);
        myCurrentLocation.start();

        myCurrentAzimuth = new MyCurrentAzimuth(this, this);
        myCurrentAzimuth.start();
    }

    /**
     * set up surface layout
     * */
    private void setupLayout() {
        descriptionTextView = (TextView) findViewById(R.id.cameraTextView);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        //mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * set up utils: calculator, camera preference helper
     * */
    private void setupUtil() {
        this.calculator = new AziCalculator();
        this.camPerm = new CamPermissionHelper();
    }

    /** --------------------------------- interfaces implementation ----------------------------------------*/

    /**
     * This function upgrades user's real-time location
     * and meanwhile updates description
     * */
    @Override
    public void onLocationChanged(Location location) {
        mMyLatitude = location.getLatitude();
        mMyLongitude = location.getLongitude();
        mAzimuthTeoretical = calculateTeoreticalAzimuth();
        Toast.makeText(this,"latitude: "+location.getLatitude()+
                " longitude: "+location.getLongitude(), Toast.LENGTH_SHORT).show();
        updateDescription();
    }

    /**
     * This function determine when to show the AR ICON.
     * When the orientation of the phone changes, we need to calculate accuracy,compare both angles
     * and if the current angle is within accuracy we can show a pointer on the screen.
     * */
    @Override
    public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {
        mAzimuthReal = azimuthChangedTo;
        mAzimuthTeoretical = calculateTeoreticalAzimuth();

        pointerIcon = (ImageView) findViewById(R.id.icon);

        double minAngle = calculator.calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
        double maxAngle = calculator.calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

        //if within the accuracy, show ICON
        if (calculator.isBetween(minAngle, maxAngle, mAzimuthReal)) {
            pointerIcon.setVisibility(View.VISIBLE);
        } else {
            pointerIcon.setVisibility(View.INVISIBLE);
        }

        updateDescription();
    }

    /** -------------------  implements SurfaceHolder.Callback Here -----------------------------*/

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


    /** ----------------------- permission related functions here ------------------------------- */

    /**
     * this function return a boolean value
     * to check whether app granted camera permission or not
     * */
    public boolean camPermissioncGranted(){
        return ContextCompat.checkSelfPermission(ArViewActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * */
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
