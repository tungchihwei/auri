package com.green.auri.favorites;

import android.graphics.Bitmap;

/**
 * A restaurant favorite to display for the user.
 */
public class Favorite {
    String restaurantName;
    Bitmap restaurantImage;
    String placeId;

    public Favorite(String name, Bitmap bitmap, String placeId) {
        this.restaurantName = name;
        this.restaurantImage = bitmap;
        this.placeId = placeId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public Bitmap getRestaurantImage() {
        return restaurantImage;
    }

    public String getPlaceId() {
        return placeId;
    }
}
