package com.green.auri.arview;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.gigamole.infinitecycleviewpager.OnInfiniteCyclePageTransformListener;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.BaseGesture;
import com.google.ar.sceneform.ux.BaseGestureRecognizer;
import com.google.ar.sceneform.ux.DragGesture;
import com.google.ar.sceneform.ux.DragGestureRecognizer;
import com.google.ar.sceneform.ux.GesturePointersUtility;
import com.google.ar.sceneform.ux.SelectionVisualizer;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;
import com.green.auri.R;
import com.green.auri.RestaurantCardAdapter;
import com.green.auri.RestaurantResult;

import java.util.ArrayList;
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
    private GestureDetectorCompat gestureDetectorCompat;

    public RestaurantBucketNode(Context context, List<RestaurantResult> bucket) {
        // Load attributes
        this.context = context;
        restaurantBucket = LayoutInflater.from(context).inflate(R.layout.layout_restaurant_bucket,null);

        // Build infinite scroller from list of restaurants
        HorizontalInfiniteCycleViewPager pager = restaurantBucket.findViewById(R.id.horizontal_cycle);
        RestaurantCardAdapter adapter = new RestaurantCardAdapter(bucket, context);
        gestureDetectorCompat = new GestureDetectorCompat(context, this);

        pager.setAdapter(adapter);
        pager.setOnInfiniteCyclePageTransformListener(new OnInfiniteCyclePageTransformListener() {

            @Override
            public void onPreTransform(View page, float position) {
                Log.i("SWIPE", "SWIPE DETECTED");
            }

            @Override
            public void onPostTransform(View page, float position) {
                Log.i("SWIPE", "SWIPED");
            }
        });

        pager.setOnTouchListener(this);
    }

    public View getView() {
        return restaurantBucket;
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

    float scrollstartX1;

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (scrollstartX1 != e1.getX()) {
            scrollstartX1 = e1.getX();
            //***************************************
            //code run only once for a scroll action...
            //****************************************
            HorizontalInfiniteCycleViewPager pager = restaurantBucket.findViewById(R.id.horizontal_cycle);
            pager.setCurrentItem(pager.getCurrentItem()+1);

            Log.i("SWIPE", "PARENT: " + "SCROLL");
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        return;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        HorizontalInfiniteCycleViewPager pager = restaurantBucket.findViewById(R.id.horizontal_cycle);
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
                pager.setCurrentItem((pager.getCurrentItem() - 1) % pager.getAdapter().getCount());
            } else {
                pager.setCurrentItem((pager.getCurrentItem() + 1) % pager.getAdapter().getCount());
            }
        }

        Log.i("SWIPE", "PARENT: " + "FLING");
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }
}
