/*
 * Copyright (C) 2016 ceryle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uyscuti.social.circuit.colorimagebottomnav;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.VectorDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.badge.BadgeDrawable;
import com.uyscuti.social.circuit.R;


import ru.nikartm.support.BadgePosition;
import ru.nikartm.support.ImageBadgeView;


public class NavigationItem extends LinearLayout {

    public NavigationItem(Context context, int drawable) {
        super(context);
        this.init(drawable);
    }

    public NavigationItem(Context context, Drawable drawable) {
        super(context);
        this.init(drawable);
    }

    private void init(Drawable drawable) {
        getAttributes();

        initViews(drawable);

        setDrawableGravity();
        setState();

        super.setPadding(16, 0, 16, 0);
        setPaddingAttrs();
    }

    private void init(int drawable) {
        getAttributes();
        badgeDrawable = new DrawableBadge(getContext(), 0); // Initial count is 0
        initViews(drawable);

        setDrawableGravity();
        setState();
        super.setPadding(16, 0, 16, 0);
        setPaddingAttrs();
    }

    public void hideBadge() {
        badgeDrawable.setVisibility(View.GONE);
    }

    public void showBadge() {
        badgeDrawable.setVisibility(View.VISIBLE);
    }

    //    private AppCompatImageView imageView;
    private ImageBadgeView imageBadgeView;
    private AppCompatTextView textView;
    private DrawableBadge badgeDrawable;
    private FrameLayout badgeContainer;

    public void setBadge(int count) {
        if (badgeDrawable != null) {
//            badgeDrawable.setCount(count);
            badgeDrawable.updateVisibility(count);
        } else {
            Log.d("Badge", "(setBadge) failed to set badge");

        }
    }

    public void setBadges(int tabResId, int badgeValue) {
        TextView badge = getOrCreateBadge(this, tabResId);
        if (badge != null) {
            badge.setVisibility(badgeValue > 0 ? View.VISIBLE : View.GONE);
            badge.setText(String.valueOf(badgeValue));
        }
    }

    private TextView getOrCreateBadge(View bottomBar, int tabResId) {
        ViewGroup parentView = bottomBar.findViewById(tabResId);
        if (parentView != null) {
            TextView badge = parentView.findViewById(R.id.menuItemBadge);
            if (badge == null) {
                LayoutInflater.from(parentView.getContext()).inflate(R.layout.bottom_bad_custom_badge, parentView, true);
                badge = parentView.findViewById(R.id.menuItemBadge);
            }
            return badge;
        }
        return null;
    }

    private void initViews(int drawable) {
        setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        imageBadgeView = new ImageBadgeView(getContext());
        imageBadgeView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) {{
            gravity = Gravity.CENTER;
            padding = 20;
        }});
        setDrawable(drawable);
        setDrawableAttrs();
        addView(imageBadgeView);

        textView = new AppCompatTextView(getContext());

        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) {{
            gravity = Gravity.TOP;

        }});
        setTextAttrs();
        addView(textView);

        badgeDrawable = new DrawableBadge(getContext(), 0); // Initial count is 0
        badgeDrawable.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) {{
            gravity = Gravity.START;

        }});
        setBadgeAttrs();
        addView(badgeDrawable);

        if (show) {
            this.setVisibility(View.VISIBLE);
        } else {
            this.setVisibility(View.GONE);
        }
    }

    private void initViews(Drawable drawable) {
        setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        imageBadgeView = new ImageBadgeView(getContext());
        imageBadgeView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) {{
            gravity = Gravity.CENTER;
        }});
        setDrawable(drawable);
//        setDrawableAttrs();
        addView(imageBadgeView);

        textView = new AppCompatTextView(getContext());
        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) {{
            gravity = Gravity.CENTER;
        }});
        setTextAttrs();
        addView(textView);
        if (show) {
            this.setVisibility(View.VISIBLE);
        } else {
            this.setVisibility(View.GONE);
        }
    }

    private Typeface defaultTypeface, textTypeface;

    private String text, textTypefacePath;

    private int textStyle, textSize, drawable, drawableTint, drawableTintTo, textColor, textColorTo, drawableWidth,
            drawableHeight, selectorColor, padding, paddingLeft, paddingRight, paddingTop, paddingBottom, drawablePadding,
            backgroundColor, textGravity;

    private boolean hasPaddingLeft, hasPaddingRight, hasPaddingTop, hasPaddingBottom, hasDrawableTint, hasTextTypefacePath,
            hasDrawable, hasText, hasDrawableWidth, hasDrawableHeight, checked, enabled, hasEnabled, clickable, hasClickable,
            hasTextStyle, hasTextSize, hasTextColor, textFillSpace, hasSelectorColor,
            hasDrawableTintTo, hasTextColorTo, show;

    /***
     * GET ATTRIBUTES FROM XML
     */
    private void getAttributes() {

        drawable = -1;
        drawableTint = 0;
        drawableTintTo = 0;
        drawableWidth = -1;
        drawableHeight = -1;

        hasDrawable = true;
        hasDrawableTint = false;
        hasDrawableTintTo = false;
        hasDrawableWidth = false;
        hasDrawableHeight = false;

        text = "";
        hasText = false;
        textColor = Color.BLACK;
        textColorTo = Color.BLACK;

        hasTextColor = false;
        hasTextColorTo = false;
        textSize = -1;
        hasTextSize = false;
        textStyle = -1;
        hasTextStyle = false;
        show = true;

        int typeface = -1;
        switch (typeface) {
            case 0:
                textTypeface = Typeface.MONOSPACE;
                break;
            case 1:
                textTypeface = Typeface.DEFAULT;
                break;
            case 2:
                textTypeface = Typeface.SANS_SERIF;
                break;
            case 3:
                textTypeface = Typeface.SERIF;
                break;
        }
        textTypefacePath = "";
        hasTextTypefacePath = false;

        backgroundColor = Color.TRANSPARENT;

        int defaultPadding = Helper.dpToPx(getContext(), 5);
        padding = defaultPadding;
        paddingLeft = 0;
        paddingRight = 0;
        paddingTop = 0;
        paddingBottom = 0;

        hasPaddingLeft = false;
        hasPaddingRight = false;
        hasPaddingTop = false;
        hasPaddingBottom = false;

        drawablePadding = 4;

        drawableGravity = DrawableGravity.TOP;

        checked = false;

        enabled = true;
        hasEnabled = false;
        clickable = true;
        hasClickable = false;

        textGravity = Gravity.CENTER_HORIZONTAL;
        textFillSpace = false;

        selectorColor = Color.TRANSPARENT;
        hasSelectorColor = false;
    }

    public void show() {
        this.show = true;
        this.setVisibility(View.VISIBLE);
    }

    public void hide() {
        this.show = false;
        this.setVisibility(View.GONE);
    }

    public boolean isShown() {
        return this.show;
    }

    public int getTextColorTo() {
        return textColorTo;
    }

    public void setTextColorTo(int textColorTo) {
        this.textColorTo = textColorTo;
    }

    public boolean hasTextColorTo() {
        return hasTextColorTo;
    }

    public boolean hasTextColor() {
        return hasTextColor;
    }

    public void setHasTextColor(boolean hasTextColor) {
        this.hasTextColor = hasTextColor;
    }

    public void setHasTextColorTo(boolean hasTextColorTo) {
        this.hasTextColorTo = hasTextColorTo;
    }

    public boolean hasDrawableTintTo() {
        return hasDrawableTintTo;
    }

    public void setHasDrawableTintTo(boolean hasDrawableTintTo) {
        this.hasDrawableTintTo = hasDrawableTintTo;
    }

    public int getDrawableTintTo() {
        return drawableTintTo;
    }

    public void setDrawableTintTo(int drawableTintTo) {
        this.drawableTintTo = drawableTintTo;
    }

    public int getSelectorColor() {
        return selectorColor;
    }

    public void setSelectorColor(int selectorColor) {
        this.selectorColor = selectorColor;
        onSelectorColorChangedListener.onSelectorColorChanged(position, selectorColor);
    }

    public boolean hasSelectorColor() {
        return hasSelectorColor;
    }

    private OnSelectorColorChangedListener onSelectorColorChangedListener;
    private int position;

    void setOnSelectorColorChangedListener(OnSelectorColorChangedListener onSelectorColorChangedListener, int position) {
        this.onSelectorColorChangedListener = onSelectorColorChangedListener;
        this.position = position;
    }

    interface OnSelectorColorChangedListener {
        void onSelectorColorChanged(int position, int selectorColor);
    }

    public boolean hasEnabled() {
        return hasEnabled;
    }

    public boolean hasClickable() {
        return hasClickable;
    }

    private void setDrawableAttrs() {
        if (hasDrawable) {
            imageBadgeView.setImageResource(drawable);
            if (hasDrawableTint)
                imageBadgeView.setColorFilter(drawableTint);

            if (hasDrawableWidth)
                setDrawableWidth(drawableWidth);
            else
                setDrawableWidth(Helper.dpToPx(getContext(), 36));
            if (hasDrawableHeight)
                setDrawableHeight(drawableHeight);
            else
                setDrawableHeight(Helper.dpToPx(getContext(), 36));
        } else {
            imageBadgeView.setVisibility(GONE);
        }
    }

    private void setDrawableGravity() {
        if (!hasDrawable)
            return;
        // If the first child is a TextView, swap it with the ImageView
        if (drawableGravity == DrawableGravity.LEFT || drawableGravity == DrawableGravity.TOP) {
            if (getChildAt(0) instanceof AppCompatTextView) {
                removeViewAt(0);
                addView(textView, 1);
                addView(badgeDrawable, 1);

                // If textFillSpace is true, adjust the LayoutParams of the TextView
                if (textFillSpace) {
                    LayoutParams params = (LayoutParams) textView.getLayoutParams();
                    params.weight = 0;
                    params.width = LayoutParams.WRAP_CONTENT;
                }
            }
        } else {
            if (getChildAt(0) instanceof AppCompatImageView) {
                removeViewAt(0);
                addView(imageBadgeView, 1);

                if (textFillSpace) {
                    LayoutParams params = (LayoutParams) textView.getLayoutParams();
                    params.weight = 1;
                    params.width = 0;
                }
            }
        }

        if (hasText && hasDrawable)
            if (drawableGravity == DrawableGravity.TOP || drawableGravity == DrawableGravity.BOTTOM)
                setOrientation(VERTICAL);
            else
                setOrientation(HORIZONTAL);
    }

    private void setBadgeAttrs() {
        int gravity;
        gravity = Gravity.START;
    }

    private void setTextAttrs() {
        defaultTypeface = textView.getTypeface();

        textView.setText(text);

        int gravity;

        if (textGravity == 2) {
            gravity = Gravity.END;
        } else if (textGravity == 1) {
            gravity = Gravity.CENTER;
        } else {
            gravity = Gravity.START;
        }

        textView.setGravity(gravity);

        if (hasTextColor)
            textView.setTextColor(textColor);
        if (hasTextSize)
            setTextSizePX(textSize);
        if (hasTextTypefacePath)
            setTypeface(textTypefacePath);
        else if (null != textTypeface) {
            setTypeface(textTypeface);
        }
        if (hasTextStyle)
            setTextStyle(textStyle);
    }

    private void setPaddingAttrs() {
        if (hasPaddingBottom || hasPaddingTop || hasPaddingLeft || hasPaddingRight)
            setPaddings(paddingLeft, paddingTop, paddingRight, paddingBottom);
        else
            setPaddings(padding, padding, padding, padding);
    }

    /**
     * GRAVITY
     */

    private DrawableGravity drawableGravity;

    public enum DrawableGravity {
        LEFT(0),
        TOP(1),
        RIGHT(2),
        BOTTOM(3);

        private final int intValue;

        DrawableGravity(int intValue) {
            this.intValue = intValue;
        }

        private int getIntValue() {
            return intValue;
        }

        public static DrawableGravity getById(int id) {
            for (DrawableGravity e : values()) {
                if (e.intValue == id) return e;
            }
            return null;
        }

        public boolean isHorizontal() {
            return intValue == 0 || intValue == 2;
        }
    }

    /**
     * TEXT VIEW
     */
    public AppCompatTextView getTextView() {
        return textView;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;

        textView.setTextColor(textColor);
    }

    void setCheckedTextColor(int color) {
        textView.setTextColor(color);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        textView.setText(text);
    }

    public void setTextSizePX(int size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void setTextSizeSP(float size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public int getTextSize() {
        return textSize;
    }

    public int getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(int typeface) {
        textView.setTypeface(textView.getTypeface(), typeface);
    }

    public void restoreTypeface() {
        textView.setTypeface(defaultTypeface);
    }

    public String getTypefacePath() {
        return textTypefacePath;
    }

    /**
     * Typeface.NORMAL: 0
     * Typeface.BOLD: 1
     * Typeface.ITALIC: 2
     * Typeface.BOLD_ITALIC: 3
     *
     * @param typeface you can use above variations using the bitwise OR operator
     */
    public void setTypeface(Typeface typeface) {
        textView.setTypeface(typeface);
    }

    public void setTypeface(String location) {
        if (null != location && !location.equals("")) {
            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), location);
            textView.setTypeface(typeface);
        }
    }

    public Typeface getDefaultTypeface() {
        return defaultTypeface;
    }

    /**
     * PADDING
     */
    public int getPadding() {
        return padding;
    }

    public void setPaddings(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        this.paddingLeft = paddingLeft;
        this.paddingTop = paddingTop;
        this.paddingRight = paddingRight;
        this.paddingBottom = paddingBottom;

        updatePaddings();
    }

    private void updatePaddings() {
        if (hasText)
            updatePadding(textView, hasDrawable);

        if (hasDrawable)
            updatePadding(imageBadgeView, hasText);
    }

    private void updatePadding(View view, boolean hasOtherView) {
        if (null == view)
            return;

        int[] paddings = {paddingLeft, paddingTop, paddingRight, paddingBottom};

        if (hasOtherView) {
            int g = drawableGravity.getIntValue();
            if (view instanceof AppCompatImageView) {
                g = g > 1 ? g - 2 : g + 2;
            }
            paddings[g] = drawablePadding / 2;
        }

        MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        params.setMargins(paddings[0], paddings[1], paddings[2], paddings[3]);
    }

    @Override
    public int getPaddingLeft() {
        return paddingLeft;
    }

    @Override
    public int getPaddingRight() {
        return paddingRight;
    }

    @Override
    public int getPaddingTop() {
        return paddingTop;
    }

    @Override
    public int getPaddingBottom() {
        return paddingBottom;
    }

    public boolean hasPaddingLeft() {
        return hasPaddingLeft;
    }

    public boolean hasPaddingRight() {
        return hasPaddingRight;
    }

    public boolean hasPaddingTop() {
        return hasPaddingTop;
    }

    public boolean hasPaddingBottom() {
        return hasPaddingBottom;
    }

    /**
     * DRAWABLE
     */
    public ImageBadgeView getImageView() {
        return imageBadgeView;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        imageBadgeView.setImageDrawable(drawable);
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;

        imageBadgeView.setImageResource(drawable);

    }

    public int getDrawableTint() {
        return drawableTint;
    }

    public void setDrawableTint(int color) {
        this.drawableTint = color;

        imageBadgeView.setColorFilter(color);
    }

    void setCheckedDrawableTint(int color) {
        imageBadgeView.setColorFilter(color);
    }

    public boolean hasDrawableTint() {
        return hasDrawableTint;
    }

    public void setDrawableTint(boolean hasColor) {
        this.hasDrawableTint = hasColor;

        if (hasColor)
            imageBadgeView.setColorFilter(drawableTint);
        else
            imageBadgeView.clearColorFilter();
    }

    public int getDrawableWidth() {
        return drawableWidth;
    }

    public void setDrawableWidth(int drawableWidth) {
        this.drawableWidth = drawableWidth;

        ViewGroup.LayoutParams params = imageBadgeView.getLayoutParams();
        if (null != params) {
            params.width = drawableWidth;
        }
    }



    public int getDrawableHeight() {
        return drawableHeight;
    }

    public void setDrawableHeight(int drawableHeight) {
        this.drawableHeight = drawableHeight;

        ViewGroup.LayoutParams params = imageBadgeView.getLayoutParams();
        if (null != params) {
            params.height = drawableHeight;
        }
    }

    public void setDrawableSizeByPx(int width, int height) {
        this.drawableWidth = width;
        this.drawableHeight = height;

        ViewGroup.LayoutParams params = imageBadgeView.getLayoutParams();
        if (null != params) {
            params.width = width;
            params.height = height;
        }
    }

    public void setDrawableSizeByDp(int width, int height) {
        width = Helper.dpToPx(getContext(), width);
        height = Helper.dpToPx(getContext(), height);
        setDrawableSizeByPx(width, height);
    }

    public DrawableGravity getDrawableGravity() {
        return drawableGravity;
    }

    public void setDrawableGravity(DrawableGravity gravity) {
        this.drawableGravity = gravity;

        setDrawableGravity();
        setPaddingAttrs();
    }

    public int getDrawablePadding() {
        return drawablePadding;
    }

    public void setDrawablePadding(int drawablePadding) {
        this.drawablePadding = drawablePadding;
        updatePaddings();
    }

    private void setEnabledAlpha(boolean enabled) {
        float alpha = 1f;
        if (!enabled)
            alpha = 0.5f;
        setAlpha(alpha);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setClickable(enabled);
        this.enabled = enabled;
        setEnabledAlpha(enabled);
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        this.clickable = clickable;
    }

    private void setState() {
        if (hasEnabled)
            setEnabled(enabled);
        else
            setClickable(clickable);
        if (show) {
            show();
        } else {
            hide();
            setClickable(false);
        }
    }

    @Override
    public boolean isClickable() {
        return clickable;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setsBadge(int value) {
        imageBadgeView.setBadgeValue(value)

                .setBadgePosition(BadgePosition.TOP_RIGHT)
                .setBadgeBackground(getResources().getDrawable(R.drawable.badge_background))
                .setBadgeTextSize(7)
                .setBadgeTextColor(Color.WHITE)
                .setBadgeColor(Color.RED)
                .setBadgePadding(4)
                .setMaxBadgeValue(999)
                .setBadgeTextStyle(Typeface.NORMAL)
                .setFixedRadius(true)


        ;

    }
}