package com.green.auri.augmentedreality;

import android.annotation.SuppressLint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;
import com.green.auri.utils.place.GetNearbyPlacesTask;
import com.green.auri.R;
import com.green.auri.restaurant.RestaurantResult;
import com.green.auri.dsensor.DProcessedSensor;
import com.green.auri.dsensor.DSensor;
import com.green.auri.dsensor.DSensorEvent;
import com.green.auri.dsensor.DSensorManager;
import com.green.auri.dsensor.interfaces.DProcessedEventListener;
import com.green.auri.utils.location.LocationListener;
import com.green.auri.utils.location.LocationUtils;
import com.green.auri.utils.photo.PhotoLoadingUtil;
import com.green.auri.utils.place.PlaceSearchListener;
import com.green.auri.utils.place.PlaceSearchUtils;
import com.green.auri.utils.place.PlaceSearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/* The main activity that is loaded by the launcher to display the camera screen */
public class ARActivity extends AppCompatActivity implements LocationListener, PlaceSearchListener {
    /* Requested to install the ARCore package. */
    private boolean installRequested;
    private DisplayRotationHelper displayRotationHelper;

    private SensorManager sm;
    /* List of Anchors for the current session */
    private final ArrayList<Node> nodes = new ArrayList<>();

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

    Handler handler;

    // Locks to coordinate async execution
    private boolean gotLocation = false;    // Will wait until we receive a location
    private boolean gotPlaces = false;      // Will wait until we receive place list
    private boolean executed = false;       // Will only allow one execution to trigger
    private boolean finishedExecuting = true; // Will prevent polling while execution in progress


    private AnchorNode anchorNode;

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

        // Create the Handler object (on the main thread by default)
        handler = new Handler();

        startPollUpdating();
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
            startPollUpdating();
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
        handler.removeCallbacks(null);

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

    private void startPollUpdating() {
        // Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                if (finishedExecuting) {
                    handler.removeCallbacks(this);
                    updateNearbyPlaces();
                    handler.postDelayed(this, 1200000);
                }
            }
        };

        // Run after 1 second
        handler.postDelayed(runnableCode, 1000);
    }

    //Execute updates
    private void updateNearbyPlaces() {

        // Locks initialization to prevent early rendering
        gotLocation = false;
        gotPlaces = false;
        executed = false;
        finishedExecuting = false;

        String url = PlaceSearchUtils.buildQueryUrl(latitude, longitude, "restaurant"); // get the url of nearby restaurant
        new GetNearbyPlacesTask().execute(url, ARActivity.this);
        LocationUtils.getCurrentLocation(ARActivity.this, this);
    }

    /**
     * The bulk of the logic to position the cards onto
     * the activity screen.
     */
    private void getPositionedPlaces() {


        // 1. First set the anchor at the current location and orientation of the camera.
        try {
            //Get camera pose to update where we are facing
            Pose newPose = arSceneView.getArFrame().getCamera().getPose();
            Anchor anchor = arSceneView.getSession().createAnchor(newPose);

            // Remove all existing elements
            deleteAllCards();

            // Update global anchor
            anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arSceneView.getScene());

        } catch (Exception e) {
            /* Camera is not tracking yet, return and wait for next poll */
            finishedExecuting = true;
            handler.removeCallbacks(null);
            startPollUpdating();
            return;
        }


        // 2. Using the nearbyplaces list, call the positioning logic to get the relative angles
        //    to place the restaurants.
        HashMap<Double, List<PlaceSearchResult>> result = SearchAndPosition.PositionNearbyPlaces(nearbyPlaceList, latitude, longitude, angle);


        // 3. For each bucket, we get the restaurants in the restaurant bucket,
        //    record the locaton and angle and build a positioning vector.
        for(Double bucket: result.keySet()){
            List<PlaceSearchResult> placesInBucket = result.get(bucket);
            List<RestaurantResult> bucketPlaces = new ArrayList<>();

            double currentAngle = result.get(bucket).get(0).getBucket();
            double currentDistance = result.get(bucket).get(0).getDistance();

            Vector3 cardVector = ARUtils.buildVectorFromAngle(currentAngle, currentDistance);
            RestaurantBucketNode node = addAndCreateRestaurantBucket(bucketPlaces, cardVector);

            for (int i = 0; i < placesInBucket.size(); i++) {
                PlaceSearchResult currentGooglePlace = placesInBucket.get(i);

                // 4. Get the corresponding photo the restaurant and place it when it returns.
                RestaurantResult restaurantResult = currentGooglePlace.toRestaurantResult();
                restaurantResult.setRestaurantDistance(currentDistance);
                PhotoLoadingUtil.getPhotoFromPlaceId(restaurantResult.getRestaurantId(), (bitmapString) -> {
                    restaurantResult.setRestaurantPhoto(bitmapString);
                    bucketPlaces.add(restaurantResult);
                    node.updateAdapter(bucketPlaces);
                });
            }
        }

        // 5. Set finished to true, to restart the timer.
        finishedExecuting = true;
    }


    /**
     * Adds and create a new restaurant bucket, based on the list
     * of restaurants and the resulting direction to place it in.
     * @param bucket
     * @param direction
     * @return
     */
    public RestaurantBucketNode addAndCreateRestaurantBucket(List<RestaurantResult> bucket, Vector3 direction) {
        RestaurantBucketNode card = new RestaurantBucketNode(this, bucket);
        addBucket(card, direction);
        return card;
    }


    /**
     * Adds a new bucket to the vector space
     * from the direction that is provided
     * @param bucket
     * @param direction
     */
    public void addBucket(Node bucket, Vector3 direction) {
        bucket.setLocalPosition(direction);
        anchorNode.addChild(bucket);
    }


    /**
     * Clears all of the cards on the AR space
     * Including the current anchor node that was used.
     */
    public void deleteAllCards() {
        if (anchorNode != null) {
            List<Node> children = anchorNode.getChildren();

            int numChildren = children.size();
            for (int i = 0; i < numChildren; i++) {
                Node child = children.get(0);
                anchorNode.removeChild(child);
            }
            arSceneView.getScene().removeChild(anchorNode);

        }
    }


    /**
     * Listener callback for the location updated task.
     * If the return is successful, then it updates the current position
     * with the returned latitude and longitude.
     * @param success
     * @param latitude
     * @param longitude
     */
    @Override
    public void onLocationUpdated(boolean success, double latitude, double longitude) {
        if (!success) {
            LocationUtils.getCurrentLocation(ARActivity.this, this);
            return;
        }

        this.latitude = latitude;
        this.longitude = longitude;
        if (gotPlaces && !executed) {
            executed = true;
            getPositionedPlaces();

        }
        gotLocation = true;
    }

    /**
     * Listener return for the nearbyPlacestask
     * Parses out the information and calls the positioned places.
     * @param nearbyPlacesList
     */
    @Override
    public void onPlaceSearchComplete(List<HashMap<String, String>> nearbyPlacesList) {
        this.nearbyPlaceList = nearbyPlacesList;
        if (nearbyPlacesList != null && !nearbyPlacesList.isEmpty()) {
            if (gotLocation && !executed) {
                executed = true;
                getPositionedPlaces();
            }
            gotPlaces = true;
        }
        if (nearbyPlacesList.isEmpty()) {
            String url = PlaceSearchUtils.buildQueryUrl(latitude, longitude, "restaurant"); // get the url of nearby restaurant
            new GetNearbyPlacesTask().execute(url, ARActivity.this);
        }
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

    /**
     * Make the activity full screen and change
     * the decorations of the display.
     * @param hasFocus
     */
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

}
