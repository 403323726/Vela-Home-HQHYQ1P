package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Fills an icon-only Dock tile with an app-coloured backdrop and a large, centred app icon. */
final class IconBannerDrawable extends Drawable {
    private final Drawable icon;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panel = new RectF();
    private final int light, dark;
    private final float radius;

    IconBannerDrawable(Drawable icon, int accent, float density) {
        this.icon = icon.mutate();
        this.light = blend(accent, Color.WHITE, 0.16f);
        this.dark = blend(accent, Color.BLACK, 0.68f);
        this.radius = 14f * density;
    }

    private static int blend(int from, int to, float amount) {
        int r = Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * amount);
        int g = Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * amount);
        int b = Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * amount);
        return Color.rgb(r, g, b);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) return;
        panel.set(bounds);
        paint.setShader(new LinearGradient(bounds.left, bounds.top, bounds.right, bounds.bottom,
                light, dark, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(panel, radius, radius, paint);

        int size = Math.round(Math.min(bounds.width(), bounds.height()) * 0.74f);
        int left = bounds.centerX() - size / 2;
        int top = bounds.centerY() - size / 2;
        icon.setBounds(left, top, left + size, top + size);
        icon.draw(canvas);
    }

    @Override public void setAlpha(int alpha) {
        icon.setAlpha(alpha);
        paint.setAlpha(alpha);
    }

    @Override public void setColorFilter(@Nullable ColorFilter colorFilter) {
        icon.setColorFilter(colorFilter);
        paint.setColorFilter(colorFilter);
    }

    @Override public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
