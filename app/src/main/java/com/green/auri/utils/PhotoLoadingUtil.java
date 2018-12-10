package com.green.auri.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.tasks.Task;
import com.green.auri.R;

import java.io.ByteArrayOutputStream;

public class PhotoLoadingUtil {
    private static GeoDataClient mGeoDataClient;
    private static String NA_BITMAP_STRING;

    public static void initPhotoLoadingUtil(GeoDataClient geoDataClient, Bitmap naString) {
        mGeoDataClient = geoDataClient;
        NA_BITMAP_STRING = PhotoLoadingUtil.convertBitmapToString(naString);
    }

    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static void getPhotoFromPlaceId(String restaurantId, PhotoLoadingListener photoLoadingListener) {
        if (mGeoDataClient == null) {
            photoLoadingListener.onPhotoLoad(NA_BITMAP_STRING);
        }

        Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(restaurantId);
        photoMetadataResponse.addOnCompleteListener(task1 -> {
            // Get the list of photos.

            PlacePhotoMetadataBuffer photoMetadataBuffer = null;

            try {
                PlacePhotoMetadataResponse photos = task1.getResult();
                photoMetadataBuffer = photos.getPhotoMetadata();

                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);

                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(task11 -> {
                    PlacePhotoResponse photo = task11.getResult();
                    Bitmap restaurantPhoto = photo.getBitmap();
                    photoLoadingListener.onPhotoLoad(PhotoLoadingUtil.convertBitmapToString(restaurantPhoto));
                });
            } catch (Exception e) {
                // Set default photo and change photo bitmap to string
                photoLoadingListener.onPhotoLoad(NA_BITMAP_STRING);

            } finally {
                if (photoMetadataBuffer != null) {
                    photoMetadataBuffer.release();
                }
            }
        });
    }
}
