package com.green.auri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;

/**
 * TODO: document your custom view class.
 */
public class RestaurantCard extends Node implements Node.OnTapListener {
    public static int MAX_RATING = 5;
    public static int MIN_RATING = 0;

    public String restaurantName;
    public String restaurantLogoUrl;
    public int restaurantRating;

    private Context context;
    private View restaurantCard;
    private TextView restaurantNameView;
    private RatingBar restaurantRatingBar;
    private ImageView restaurantLogoView;

    public RestaurantCard(Context context, String name) {
        this(context, name, "", -1);
    }

    public RestaurantCard(Context context, String name, String logoUrl, int rating) {
        // Load attributes
        this.context = context;
        restaurantCard = LayoutInflater.from(context).inflate(R.layout.restaurant_card,null);
        restaurantNameView = restaurantCard.findViewById(R.id.restaurant_name);
        restaurantRatingBar = restaurantCard.findViewById(R.id.restaurant_rating);
        restaurantLogoView = restaurantCard.findViewById(R.id.restaurant_logo);

        // Initialize the values correctly
        setRestaurantName(name);
        setRestaurantRating(rating);
        setRestaurantLogo(logoUrl);

        // Set Interaction Listeners
        setOnTapListener(this);
    }

    public View getView() {
        return restaurantCard;
    }

    /**
     * Sets the restaurantCard's name to the TextView
     *
     * @param name The name of the restaurant to appear.
     */
    public void setRestaurantName(String name) {
        restaurantName = name;
        restaurantNameView.setText(name);
    }

    /**
     * Sets the restaurantCard's rating to the input rating
     * if it is not equal to -1.
     *
     * @param rating The rating of the restaurant to set to.
     */
    public void setRestaurantRating(int rating) {
        restaurantRating = rating;
        if (rating >= MIN_RATING && rating <= MAX_RATING) {
            restaurantRatingBar.setRating(rating);
        }
    }

    /**
     * Sets the restaurantCard's logo to the image
     * of the url it references.
     *
     * @param imageUrl The url of the restaurant's preview image.
     */
    public void setRestaurantLogo(String imageUrl) {
        restaurantLogoUrl = imageUrl;
        if (imageUrl != "") {
            // get the image from the web and set
            // the imageView's image to the reference
        }
    }

    @Override
    public void onActivate() {
        if (getScene() == null) {
            throw new IllegalStateException("Scene is null.");
        }

        ViewRenderable.builder()
                .setView(context, restaurantCard)
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
    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        // ... ?
        getParent().removeChild(this);
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
