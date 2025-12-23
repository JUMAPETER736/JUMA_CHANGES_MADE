package com.uyscuti.social.circuit.colorimagebottomnav;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.uyscuti.social.circuit.R;


public class DrawableBadge extends View {
    private int count;
    private Paint circlePaint;
    private Paint textPaint;
    private boolean isVisible;
    public DrawableBadge(Context context, int count) {
        super(context);
        this.count = count;
        this.isVisible = count > 0;
        init();
    }

    private void init() {
        // Set up paint for drawing the circle
        circlePaint = new Paint();
        circlePaint.setColor(Color.RED); // Set your desired badge color
        circlePaint.setAntiAlias(true);

        // Set up paint for drawing the badge count
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE); // Set your desired text color
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.badge_text_size)); // Set your desired text size
        textPaint.setTextAlign(Paint.Align.CENTER);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);



        if (isVisible) { // Only draw if badge is visible
            float radius = Math.min(getWidth(), getHeight()) / 2.0f;
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius, circlePaint);

            // Draw the badge count
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(radius);
            textPaint.setTextAlign(Paint.Align.CENTER);

            float yPos = (getHeight() / 2.0f) - ((textPaint.descent() + textPaint.ascent()) / 2.0f);
            canvas.drawText(String.valueOf(count), getWidth() / 2.0f, yPos, textPaint);
        }
    }

    public void updateVisibility(int newCount) {
        isVisible = newCount > 0;
        count = newCount;
        invalidate(); // Trigger redraw
    }
}
