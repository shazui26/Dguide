package com.ex.dguide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.Objects;

public class BitmapUtils {

    /**
     * Convert given drawable id to bitmap.
     */
    public static Bitmap bitmapFromDrawableRes(Context context, @DrawableRes int resourceId) {
        return drawableToBitmap(AppCompatResources.getDrawable(context, resourceId));
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        return drawableToBitmap(drawable, false, false, null);
    }


    public static Bitmap drawableToBitmap(
            Drawable sourceDrawable,
            boolean flipX,
            boolean flipY,
            @ColorInt Integer tint) {
        if (sourceDrawable == null) {
            return null;
        }

        if (sourceDrawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) sourceDrawable).getBitmap();
        } else {
            // copying drawable object to not manipulate on the same reference
            Drawable drawable = Objects.requireNonNull(sourceDrawable.getConstantState()).newDrawable().mutate();
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );

            if (tint != null) {
                drawable.setTint(tint);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.scale(
                    flipX ? -1f : 1f,
                    flipY ? -1f : 1f,
                    canvas.getWidth() / 2f,
                    canvas.getHeight() / 2f
            );
            drawable.draw(canvas);

            return bitmap;
        }

    }

}


