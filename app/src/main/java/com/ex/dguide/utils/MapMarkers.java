package com.ex.dguide.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.ex.dguide.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.EdgeInsets;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @noinspection ALL
 */
public class MapMarkers {

    private static final double MARKERS_EDGE_OFFSET = dpToPx(64F);
    private static final double PLACE_CARD_HEIGHT = dpToPx(300F);
    private static final EdgeInsets MARKERS_INSETS = new EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET
    );
    private static final EdgeInsets MARKERS_INSETS_OPEN_CARD = new EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
    );
    private final Context context;
    private final PointAnnotationManager pointAnnotationManager;
    private final Map<Long, Point> markers = new HashMap<>();
    private Runnable onMarkersChangeListener;

    public MapMarkers(MapView mapView) {

        context = mapView.getContext();
        MapboxMap mapboxMap = mapView.getMapboxMap();
        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);

    }

    private static float dpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public boolean hasMarkers() {
        return !markers.isEmpty();
    }

    public void clearMarkers() {
        markers.clear();
        pointAnnotationManager.deleteAll();
        onMarkersChangeListener = null;
    }

    public void addMarker(Point coordinate) {
        showMarkers(Collections.singletonList(coordinate));


    }

    public void showMarkers(List<Point> coordinates) {
        clearMarkers();
        if (coordinates.isEmpty()) {
            if (onMarkersChangeListener != null) {
                onMarkersChangeListener.run();
            }
            return;
        }


        Bitmap bitmap = BitmapUtils.convertDrawableToBitmap(
                AppCompatResources.getDrawable(context, R.drawable.red_pin)
        );

        for (Point coordinate : coordinates) {
            PointAnnotationOptions pointAnnotationOptions = (bitmap != null) ?
                    new PointAnnotationOptions()
                            .withTextAnchor(TextAnchor.CENTER)
                            .withIconImage(bitmap)
                            .withIconAnchor(IconAnchor.BOTTOM)
                            .withPoint(coordinates.get(0))
                            .withIconSize(0.1) :
                    null;

            PointAnnotation annotation = (pointAnnotationOptions != null) ?
                    pointAnnotationManager.create(pointAnnotationOptions) :
                    null;

            if (annotation != null) {
                markers.put(annotation.getId(), coordinate);


            }
        }
        if (onMarkersChangeListener != null) {
            onMarkersChangeListener.run();

        }
    }

    public static class BitmapUtils {


        public static Bitmap convertDrawableToBitmap(Drawable sourceDrawable) {
            if (sourceDrawable == null) {
                return null;
            }

            if (sourceDrawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) sourceDrawable).getBitmap();
            } else {
                Drawable.ConstantState constantState = sourceDrawable.getConstantState();
                if (constantState == null) {
                    return null;
                }

                Drawable drawable = constantState.newDrawable().mutate();
                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                return bitmap;
            }
        }

    }


}
