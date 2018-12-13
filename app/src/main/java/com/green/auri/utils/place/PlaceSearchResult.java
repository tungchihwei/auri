package com.green.auri.utils.place;

import com.green.auri.restaurant.RestaurantResult;

import java.util.HashMap;

public class PlaceSearchResult {
    private String placeName;
    private String placeId;
    private double lat;
    private double lng;
    private double x;
    private double y;
    private double distance;
    private double rating;
    private String photoRef;
    private double bucket;
    private String vicinity;

    public PlaceSearchResult(HashMap<String, String> googlePlace){
        this.lat = Double.parseDouble(googlePlace.get("lat"));
        this.lng = Double.parseDouble(googlePlace.get("lng"));
        this.placeName = googlePlace.get("place_name");
        this.placeId = googlePlace.get("place_id");

        String currentRating = googlePlace.get("rating");
        if(currentRating == ""){

            // We will show 3 for rating if there isn't one
            currentRating="3.0";
        }

        this.rating = Double.parseDouble(currentRating);
        this.photoRef = googlePlace.get("photoURL");
        this.vicinity = googlePlace.get("vicinity");

    }

    public RestaurantResult toRestaurantResult(){
        RestaurantResult restaurantResult = new RestaurantResult(
                placeName,
                vicinity,
                rating,
                placeId
        );
        restaurantResult.setRestaurantDistance(distance);
        restaurantResult.setRestaurantPhoto(photoRef);
        return restaurantResult;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPhotoRef() {
        return photoRef;
    }

    public void setPhotoRef(String photoRef) {
        this.photoRef = photoRef;
    }

    public double getBucket() {
        return bucket;
    }

    public void setBucket(double bucket) {
        this.bucket = bucket;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
