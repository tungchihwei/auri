package com.green.auri;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;

/* The main activity that is loaded by the launcher to display the camera screen */
public class IndexActivity extends AppCompatActivity {

    /* A renderable Restaurant Card = A Customized 2D Layout: res/layout/restaurant_card.xml */
    private ViewRenderable restaurantCard;

    /*
     *  A provided fragment from the Sceneform/AR library.
     *  1. Checks for camera and ARCore requirements on load and during application use.
     *  2. Provides the functionality to render and add the rendered component to the activity.
     *  3. Provides listeners for actions on top of the camera/AR display.
     *  No visible component, just a helper fragment
     */
    private ArFragment arFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Uses the CompatUtils helper class to check is the device is a supported ARCore device */
        if (!CompatUtils.checkIsSupportedDeviceOrFinish(this)) {
            // Not a supported device. So return and exit.
            return;
        }

        /* Sets the ContentView to the custom Layout: res/layout/activity_index.xml */
        setContentView(R.layout.activity_index);

        /* Finds the ArFragment added to the activity through the fragment manager. */
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        /* Builds the renderable card component and saves it to the instance variable.
         *  Becomes a modular piece that can be added multiple times.*/
        ViewRenderable.builder()
                .setView(this, R.layout.restaurant_card)
                .build()
                .thenAccept(renderable -> restaurantCard = renderable);
        /*
         * Sets the OnTopListener for when a user interacts with the Camera/ARPlane.
         * Creates a card at the location an user taps onto the AR plane.
         */
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (restaurantCard == null) {
                        return;
                    }

                    /*
                     * Create the Anchor.
                     * Pins the location on the AR plane for rendered elements to be added to it.
                     * The parent element for the node based structures that other nodes can be attached to.
                     */
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    /* A generic node that can become a renderable to be displayed. */
                    TransformableNode restCard = new TransformableNode(arFragment.getTransformationSystem());

                    /* Sets the parent node to be the anchor node so that it is attached to that parent's position */
                    restCard.setParent(anchorNode);

                    /* Sets its rendered display to be the restaurant card we built. */
                    restCard.setRenderable(restaurantCard);
                    restCard.select();
                });
    }

    /* Makes sure that the app has permissions to access the camera.
     *  Does not ask if it was denied with the option of do not ask again.*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        /* If the Camera permission has not been given. */
        if (!CompatUtils.hasCameraPermission(this)) {

            /* If the user still wants us to ask for permissions */
            if (!CompatUtils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied without checking "Do not ask again".
                // Launch the permissions settings again for the user to select.
                CompatUtils.launchPermissionSettings(this);
            } else {
                Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }


}
