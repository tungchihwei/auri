package com.green.auri.augmentedreality;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.green.auri.R;
import com.green.auri.restaurant.RestaurantCardAdapter;
import com.green.auri.restaurant.RestaurantResult;

import java.util.List;

/**
 * Stores a bucket of restaurants that represents all restaurants in a portion of the view.
 * Uses a HorizontalInfiniteCycleViewPager to show the restaurant
 */
public class RestaurantBucketNode extends Node implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private Context context;
    private View restaurantBucket;
    private HorizontalInfiniteCycleViewPager pager;
    private GestureDetectorCompat gestureDetectorCompat;

    public RestaurantBucketNode(Context context, List<RestaurantResult> bucket) {
        // Load attributes
        this.context = context;
        restaurantBucket = LayoutInflater.from(context).inflate(R.layout.ar_restaurant_bucket,null);

        // Build infinite scroller from list of restaurants
        pager = restaurantBucket.findViewById(R.id.horizontal_cycle);
        gestureDetectorCompat = new GestureDetectorCompat(context, this);
        pager.setOnTouchListener(this);
        updateAdapter(bucket);
    }

    /**
     * Get the corresponding view for this node.
     * @return
     */
    public View getView() {
        return restaurantBucket;
    }

    /**
     * Update the adapter to the new list of items.
     * @param bucket
     */
    public void updateAdapter(List<RestaurantResult> bucket) {
        if (pager == null) {
            return;
        }
        pager.setAdapter(new RestaurantCardAdapter(bucket, context, true));
    }

    /**
     * On activation, create the 2D renderable and place it
     * on the location of the node.
     */
    @Override
    public void onActivate() {
        if (getScene() == null) {
            throw new IllegalStateException("Scene is null.");
        }

        ViewRenderable.builder()
                .setView(context, restaurantBucket)
                .build()
                .thenAccept(
                        (renderable) -> {
                            this.setRenderable(renderable);
                        })
                .exceptionally(
                        (throwable) -> {
                            throw new AssertionError("Could not load restaurant card.", throwable);
                        });
    }

    /**
     * On update, correct the rotation of the 2D frame to stay facing the user.
     * @param frameTime
     */
    @Override
    public void onUpdate(FrameTime frameTime) {
        // If onUpdate is called explicitly or if the node is removed from the scene on a
        // different thread during onUpdate, then getScene may be null.
        if (getScene() == null) {
            return;
        }

        // 1. Get the position of the camera in the world coordinate system built by ARCore
        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();

        // 2. Get the position of the card in the world.
        Vector3 cardPosition = getWorldPosition();

        // 3. Subtract the difference to get the vector direction difference.
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);

        // 4. Convert the difference in direction to a quaternion (3d rotation).
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());

        // 5. Set the new rotation of the card.
        setWorldRotation(lookRotation);
    }

    // The start of the motion to compare to, so that duplicate scrolls
    // in one motion do not happen.
    float startX1;

    /**
     * Default touches do not go through to the layout of the node, we
     * have to implement these swipes our selves.
     * On swipe, change the card based on the direction of the swipe.
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (startX1 != e1.getX()) {
            startX1 = e1.getX();

            float diffX = e2.getX() - e1.getX();
            if (diffX > 0) {
                pager.setCurrentItem((pager.getCurrentItem() - 1) % pager.getAdapter().getCount());
            } else {
                pager.setCurrentItem((pager.getCurrentItem() + 1) % pager.getAdapter().getCount());
            }
        }

        return true;
    }

    /**
     * Same as on scroll, change the current card in the display based on the
     * direction of the scroll.
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     * @return
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (startX1 != e1.getX()) {
            startX1 = e1.getX();

            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    pager.setCurrentItem((pager.getCurrentItem() - 1) % pager.getAdapter().getCount());
                } else {
                    pager.setCurrentItem((pager.getCurrentItem() + 1) % pager.getAdapter().getCount());
                }
            }
        }

        return true;
    }


    /**
     * Dispatch the touch event to our gesture detector to figure out
     * if the parent should consume or not.
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }

    /* Unused gesture methods required by the GestureDetector interface. */

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}
