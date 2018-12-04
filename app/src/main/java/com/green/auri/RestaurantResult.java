package com.green.auri;

public class RestaurantResult {
    private String restaurantName = "Restaurant Name";
    private String restaurantAddress = "Restaurant Address";
    private double restaurantRating = 3.0;
    private String restaurantId;
    private double restaurantDistance;
    private String restaurantPhoto = "Bitmap Photo";

    public RestaurantResult(String name, String address, double rating, String placeId) {
        restaurantName = name;
        restaurantAddress = address;
        restaurantRating = rating;
        restaurantId = placeId;
    }

    public void setRestaurantPhoto(String photoString) {
        restaurantPhoto = photoString;
    }

    public void setRestaurantDistance(double distance) {
        restaurantDistance = distance;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public double getRestaurantRating() {
        return restaurantRating;
    }

    public String getRestaurantPhoto() {
        return restaurantPhoto;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public double getRestaurantDistance() {
        return restaurantDistance;
    }

    @Override
    public String toString() {
        return restaurantName;
    }
}
