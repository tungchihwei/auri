package com.green.auri.arview;

import android.content.Context;
import android.util.Log;
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
import com.green.auri.R;
import com.green.auri.RestaurantCardAdapter;
import com.green.auri.RestaurantResult;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class RestaurantBucketNode extends Node {
    public static int MAX_RATING = 5;
    public static int MIN_RATING = 0;

//    public String restaurantName;
//    public String restaurantAddress;
//    public String restaurantLogoUrl;
//    public float restaurantRating;

    private Context context;
    private View restaurantBucket;
//    private TextView tv_restaurantName;
//    private TextView tv_restaurantAddress;
//    private RatingBar rb_restaurantRating;
//    private ImageView img_restaurantPreview;

    public RestaurantBucketNode(Context context, List<RestaurantResult> bucket) {
        // Load attributes
        this.context = context;
        restaurantBucket = LayoutInflater.from(context).inflate(R.layout.layout_restaurant_bucket,null);
        HorizontalInfiniteCycleViewPager pager = restaurantBucket.findViewById(R.id.horizontal_cycle);
        RestaurantCardAdapter adapter = new RestaurantCardAdapter(bucket, context);
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
}
