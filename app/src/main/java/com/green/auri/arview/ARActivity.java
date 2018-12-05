package com.green.auri.arview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.green.auri.GetNearbyPlacesTask;
import com.green.auri.R;
import com.green.auri.RestaurantResult;
import com.green.auri.SearchAndPosition;
import com.green.auri.dsensor.DProcessedSensor;
import com.green.auri.dsensor.DSensor;
import com.green.auri.dsensor.DSensorEvent;
import com.green.auri.dsensor.DSensorManager;
import com.green.auri.dsensor.interfaces.DProcessedEventListener;
import com.green.auri.utils.ARUtils;
import com.green.auri.utils.LocationListener;
import com.green.auri.utils.LocationUtils;
import com.green.auri.utils.PlaceSearchListener;
import com.green.auri.utils.PlaceSearchUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/* The main activity that is loaded by the launcher to display the camera screen */
public class ARActivity extends AppCompatActivity implements LocationListener, PlaceSearchListener {
    /* Requested to install the ARCore package. */
    private boolean installRequested;
    private DisplayRotationHelper displayRotationHelper;

    /* List of Anchors for the current session */
    private final ArrayList<Node> nodes = new ArrayList<>();
    private final int MAX_LOCK_SIZE = 2;

    /* A renderable Restaurant RestaurantCardDisplay = A Customized 2D Layout: res/layout/unused_restaurant_card.xmlard.xml */
    private ViewRenderable restaurantCard;

    /*
    *  A provided fragment from the Sceneform/AR library.
    *  1. Checks for camera and ARCore requirements on load and during application use.
    *  2. Provides the functionality to render and add the rendered component to the activity.
    *  3. Provides listeners for actions on top of the camera/AR display.
    *  No visible component, just a helper fragment
    */
    private ArFragment arFragment;
    private ArSceneView arSceneView;

    private double latitude;
    private double longitude;
    private double angle;
    private List<HashMap<String, String>> nearbyPlaceList;
    private int lock = MAX_LOCK_SIZE;
    private boolean gotLocation = false;
    private boolean gotPlaces = false;
    private boolean executed = false;
    private boolean checking = false;
    private int tryCounter = MAX_LOCK_SIZE;

    private boolean done;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* First check if the device is supported or not. */
        if (!CompatUtils.checkIsSupportedDeviceOrFinish(this)) {
            CompatUtils.displayError(this, "Not a supported device.", null);
        }

        /* Sets the ContentView to the custom Layout: res/layout/activity_index.xml */
        setContentView(R.layout.activity_index);
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

        /* Finds the ArFragment added to the activity through the fragment manager. */
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arSceneView = arFragment.getArSceneView();
        arSceneView.getPlaneRenderer().setEnabled(false);

        installRequested = false;

        arSceneView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (arSceneView.getSession() == null) {
                    return false;
                }

                updateNearbyPlaces();

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSceneView == null) {
            return;
        }

        if (arSceneView.getSession() == null) {
            try {
                /* Uses the CompatUtils helper class to check is the device is a supported ARCore device */
                Session session = CompatUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = CompatUtils.hasCameraPermission(this);
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                CompatUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            CompatUtils.displayError(this, "Camera not available. Please restart the app.", null);
            finish();
            return;
        }

        displayRotationHelper.onResume();


        DSensorManager.startDProcessedSensor(this, DProcessedSensor.TYPE_COMPASS_FLAT_ONLY_AND_DEPRECIATED_ORIENTATION,
                new DProcessedEventListener() {
                    @Override
                    public void onProcessedValueChanged(DSensorEvent dSensorEvent) {
                        // update UI
                        // dSensorEvent.values[0] is the azimuth.
                        if (dSensorEvent.sensorType == DSensor.TYPE_DEPRECIATED_ORIENTATION) {
                            angle = Math.round(dSensorEvent.values[0]);
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        DSensorManager.stopDSensor();

        if (arSceneView != null) {
            // GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            arSceneView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateNearbyPlaces() {
        if (done) return;
        gotLocation = false;
        gotPlaces = false;
        executed = false;
        tryCounter = MAX_LOCK_SIZE;
        String Restaurant = "restaurant";
        String url = PlaceSearchUtils.getUrl(latitude, longitude, Restaurant); // get the url of nearby restaurant
        Log.d("onClick", url);
        new GetNearbyPlacesTask().execute(url, ARActivity.this);
        LocationUtils.getCurrentLocation(ARActivity.this, this);
    }

    private void getPositionedPlaces(){
        Log.i("TIMER", "STARTED POSITIONING");

        Log.i("POSITIONED", String.valueOf(nearbyPlaceList));
        Log.i("POSITIONED", String.valueOf(latitude));
        Log.i("POSITIONED", String.valueOf(longitude));
        Log.i("POSITIONED", String.valueOf(angle));
        Log.i("POSITIONED", String.valueOf(lock));
        Pose cameraRelativePose = Pose.makeTranslation(0,0,0);
        Anchor anchor = arSceneView.getSession().createAnchor(cameraRelativePose);
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arSceneView.getScene());
        HashMap<String, List<HashMap<String,String>>> result = SearchAndPosition.PositionNearbyPlaces(nearbyPlaceList, latitude, longitude, angle);


        Log.i("TIMER", "DONE POSITIONING");

        for(String bucket: result.keySet()){
            List<HashMap<String,String>> placesInBucket = result.get(bucket);
            List<RestaurantResult> bucketPlaces = new ArrayList<>();

            for (int i = 0; i < placesInBucket.size(); i++) {
                HashMap<String, String> currentGooglePlace = placesInBucket.get(i);
                String currentName = currentGooglePlace.get("Name");
                String currentRating = currentGooglePlace.get("Rating");
                String currentDistance = currentGooglePlace.get("Distance");
                String currentPhotoRef = currentGooglePlace.get("photoRef");

                RestaurantResult restaurantResult = new RestaurantResult(
                        currentName,
                        "", // address
                        Double.valueOf(currentRating),
                        ""
                );

                // Get pictures before hand
                restaurantResult.setRestaurantDistance(Double.valueOf(currentDistance));
                bucketPlaces.add(restaurantResult);
            }

            double currentAngle = Double.parseDouble(result.get(bucket).get(0).get("Bucket"));
            double currentDistance = Double.parseDouble(result.get(bucket).get(0).get("Distance"));

            Log.i("APOS", String.valueOf(bucketPlaces));
            Vector3 cardVector = ARUtils.buildVectorFromAngle(currentAngle, currentDistance);
            addAndCreateCard(anchorNode, bucketPlaces, cardVector);
        }

        done = true;
    }

    // TESTING
    public void createDirectionalCards() {
        Pose cameraRelativePose = Pose.makeTranslation(0,0,0);
        Anchor anchor = arSceneView.getSession().createAnchor(cameraRelativePose);
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arSceneView.getScene());
    }

    public void addCard(AnchorNode anchorNode, Node card, Vector3 direction) {
        card.setLocalPosition(direction);
        anchorNode.addChild(card);
    }

    public void addAndCreateCard(AnchorNode anchorNode, List<RestaurantResult> bucket, Vector3 direction) {
        Node card = new RestaurantBucketNode(this, bucket);
        addCard(anchorNode, card, direction);
    }

    /*
    *  Makes sure that the app has permissions to access the camera.
    *  Does not ask if it was denied with the option of do not ask again.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        /* If the Camera permission has not been given. */
        if (!CompatUtils.hasCameraPermission(this)) {

            /* If the user still wants us to ask for permissions */
            if (CompatUtils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied without checking "Do not ask again".
                // Launch the permissions settings again for the user to select.
                CompatUtils.launchPermissionSettings(this);
            } else {
                Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onLocationUpdated(double latitude, double longitude) {
        Log.i("POSITIONED", "Location updated");
        this.latitude = latitude;
        this.longitude = longitude;
        if(gotPlaces && !executed){
            executed = true;
            getPositionedPlaces();
        }
        gotLocation = true;
    }

    @Override
    public void onPlaceSearchComplete(List<HashMap<String, String>> nearbyPlacesList) {
        Log.i("POSITIONED", "Nearby Places updated: " + String.valueOf(nearbyPlacesList));
        this.nearbyPlaceList = nearbyPlacesList;
        if(!nearbyPlacesList.isEmpty()){
            if(gotLocation && !executed){
                executed = true;
                getPositionedPlaces();
            }
            gotPlaces = true;
        }


    }
}
