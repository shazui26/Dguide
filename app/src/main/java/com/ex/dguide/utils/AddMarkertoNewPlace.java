package com.ex.dguide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;

import com.ex.dguide.R;
import com.ex.dguide.firbasehelper.Places;
import com.ex.dguide.sqlitehelper.DbHelper;
import com.mapbox.geojson.Point;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMarkertoNewPlace {

    private static PointAnnotationManager pointAnnotationManager = null;
    private final Map<Long, Point> markers = new HashMap<>();
    private final Context context;
    private Runnable onMarkersChangeListener;

    public AddMarkertoNewPlace(MapView mapView) {
        context = mapView.getContext();
        MapboxMap mapboxMap = mapView.getMapboxMap();
        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);
    }

    public void addMarkersFromDatabase(DbHelper dbHelper) {
        try {
            List<Places> placesList = dbHelper.getAllPlaces();

            for (Places place : placesList) {
                Log.d("PlacesData", "Latitude: " + place.getLatitude() + ", Longitude: " + place.getLongitude() + ", Name: " + place.getPlaceName());
                double latitude = place.getLatitude();
                double longitude = place.getLongitude();
                String placeName = place.getPlaceName();

                // Create a Point from the retrieved latitude and longitude
                Point coordinate = Point.fromLngLat(longitude, latitude);

                // Add the marker with the Point and placeName
                addMarker(coordinate, placeName);
            }
        } catch (Exception e) {
            // Handle exceptions here, e.g., log or show an error message
            e.printStackTrace();
        }
        dbHelper.close();
    }

    public void addMarker(Point coordinates, String placeName) {
        Bitmap bitmap = MapMarkers.BitmapUtils.convertDrawableToBitmap(
                AppCompatResources.getDrawable(context, R.drawable.baseline_circle_24));

        if (pointAnnotationManager != null) {
            assert bitmap != null;
            PointAnnotationOptions markerOptions = new PointAnnotationOptions()
                    .withIconAnchor(IconAnchor.CENTER)
                    .withIconSize(0.3f)
                    .withIconImage(bitmap)
                    .withPoint(coordinates)
                    .withTextField(placeName)
                    .withTextSize(12f)
                    .withTextAnchor(TextAnchor.TOP);

            PointAnnotation annotation = pointAnnotationManager.create(markerOptions);
        } else {
            Log.e("AddMarkertoNewPlace", "pointAnnotationManager is null");
        }
    }
}

