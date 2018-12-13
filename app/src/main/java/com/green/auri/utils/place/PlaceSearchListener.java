package com.green.auri.utils.place;

import java.util.HashMap;
import java.util.List;

public interface PlaceSearchListener {
    void onPlaceSearchComplete(List<HashMap<String, String>> nearbyPlacesList);
}
