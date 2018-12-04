package com.green.auri.utils;

import java.util.HashMap;
import java.util.List;

public interface PlaceSearchListener {
    void onPlaceSearchComplete(List<HashMap<String, String>> nearbyPlacesList);
}
