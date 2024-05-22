package com.ex.dguide.search;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class CustomDivider extends RecyclerView.ItemDecoration {
    private final Drawable divider;

    public CustomDivider(Context context, int drawableResId) {
        this.divider = ContextCompat.getDrawable(context, drawableResId);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i < childCount - 1) {
                // Draw the divider only between items (not after the last one)
                int top = parent.getChildAt(i).getBottom();
                int bottom = top + divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }
}

