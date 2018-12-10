package com.green.auri.arview;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.gigamole.infinitecycleviewpager.OnInfiniteCyclePageTransformListener;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.green.auri.R;
import com.green.auri.RestaurantCardAdapter;
import com.green.auri.RestaurantResult;

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

    public View getView() {
        return restaurantBucket;
    }

    public void updateAdapter(List<RestaurantResult> bucket) {
        if (pager == null) {
            return;
        }
        pager.setAdapter(new RestaurantCardAdapter(bucket, context, true));
    }

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

    @Override
    public void onUpdate(FrameTime frameTime) {
        // Typically, getScene() will never return null because onUpdate() is only called when the node
        // is in the scene.
        // However, if onUpdate is called explicitly or if the node is removed from the scene on a
        // different thread during onUpdate, then getScene may be null.
        if (getScene() == null) {
            return;
        }

        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        setWorldRotation(lookRotation);
    }

    float startX1;

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (startX1 != e1.getX()) {
            startX1 = e1.getX();

            float diffX = e2.getX() - e1.getX();
            if (diffX > 0) {
                pager.setCurrentItem((pager.getCurrentItem() - 1) % pager.getAdapter().getCount());
                Log.i("SWIPE", "PARENT: " + "SCROLL RIGHT" + " x");
            } else {
                pager.setCurrentItem((pager.getCurrentItem() + 1) % pager.getAdapter().getCount());
                Log.i("SWIPE", "PARENT: " + "SCROLL RIGHT" + " x");
            }
        }

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (startX1 != e1.getX()) {
            startX1 = e1.getX();

            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    pager.setCurrentItem((pager.getCurrentItem() - 1) % pager.getAdapter().getCount());
                    Log.i("SWIPE", "PARENT: " + "FLING RIGHT" + " v: " + velocityX + " x");
                } else {
                    pager.setCurrentItem((pager.getCurrentItem() + 1) % pager.getAdapter().getCount());
                    Log.i("SWIPE", "PARENT: " + "FLING LEFT");
                }
            }
        }

        return true;
    }

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
