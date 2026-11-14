package com.example.simplecad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint paint, gridPaint, snapPaint, textPaint;
    private float startX, startY, currentX, currentY;
    private boolean drawing = false;
    private String tool = "line";
    private boolean snapEnabled = true;
    private String pendingText = null;

    private List<DrawObject> objects = new ArrayList<>();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f);

        snapPaint = new Paint();
        snapPaint.setColor(Color.RED);
        snapPaint.setStrokeWidth(2f);
        snapPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40f);
        textPaint.setAntiAlias(true);
    }

    public void setTool(String t) {
        tool = t;
        pendingText = null;
        Toast.makeText(getContext(), "ابزار: " + t, Toast.LENGTH_SHORT).show();
    }

    public void setTextTool(String text) {
        tool = "text";
        pendingText = text;
        Toast.makeText(getContext(), "لمس کنید تا متن قرار گیرد", Toast.LENGTH_SHORT).show();
    }

    public void toggleSnap() {
        snapEnabled = !snapEnabled;
        Toast.makeText(getContext(), "Snap: " + (snapEnabled ? "روشن" : "خاموش"), Toast.LENGTH_SHORT).show();
    }

    public void clearDrawing() {
        objects.clear();
        invalidate();
    }

    private float snap(float v) {
        if (!snapEnabled) return v;
        int grid = 50;
        return Math.round(v / grid) * grid;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        int w = getWidth();
        int h = getHeight();
        for (int i = 0; i < w; i += 50) {
            canvas.drawLine(i, 0, i, h, gridPaint);
        }
        for (int i = 0; i < h; i += 50) {
            canvas.drawLine(0, i, w, i, gridPaint);
        }

        for (DrawObject obj : objects) {
            drawObject(canvas, obj);
        }

        if (drawing) {
            if ("line".equals(tool)) {
                canvas.drawLine(startX, startY, currentX, currentY, paint);
            } else if ("rect".equals(tool)) {
                canvas.drawRect(startX, startY, currentX, currentY, paint);
            } else if ("circle".equals(tool)) {
                float radius = (float) Math.hypot(currentX - startX, currentY - startY);
                canvas.drawCircle(startX, startY, radius, paint);
            } else if ("dim".equals(tool)) {
                canvas.drawLine(startX, startY, currentX, currentY, paint);
                float dist = (float) Math.hypot(currentX - startX, currentY - startY);
                float midX = (startX + currentX) / 2;
                float midY = (startY + currentY) / 2;
                canvas.drawText(String.format("%.0f", dist), midX, midY - 10, textPaint);
            }
        }
    }

    private void drawObject(Canvas canvas, DrawObject obj) {
        switch (obj.type) {
            case "line":
                canvas.drawLine(obj.x1, obj.y1, obj.x2, obj.y2, paint);
                break;
            case "rect":
                canvas.drawRect(obj.x1, obj.y1, obj.x2, obj.y2, paint);
                break;
            case "circle":
                float r = (float) Math.hypot(obj.x2 - obj.x1, obj.y2 - obj.y1);
                canvas.drawCircle(obj.x1, obj.y1, r, paint);
                break;
            case "text":
                canvas.drawText(obj.text, obj.x1, obj.y1, textPaint);
                break;
            case "dim":
                canvas.drawLine(obj.x1, obj.y1, obj.x2, obj.y2, paint);
                float d = (float) Math.hypot(obj.x2 - obj.x1, obj.y2 - obj.y1);
                float mx = (obj.x1 + obj.x2) / 2;
                float my = (obj.y1 + obj.y2) / 2;
                canvas.drawText(String.format("%.0f", d), mx, my - 10, textPaint);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = snap(event.getX());
        float y = snap(event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                currentX = x;
                currentY = y;
                drawing = true;

                if ("text".equals(tool) && pendingText != null) {
                    objects.add(new DrawObject("text", startX, startY, 0, 0, pendingText));
                    pendingText = null;
                    tool = "line";
                    drawing = false;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                currentX = x;
                currentY = y;
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                drawing = false;
                if (!"text".equals(tool)) {
                    objects.add(new DrawObject(tool, startX, startY, currentX, currentY, null));
                }
                invalidate();
                break;
        }
        return true;
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    static class DrawObject {
        String type;
        float x1, y1, x2, y2;
        String text;

        DrawObject(String type, float x1, float y1, float x2, float y2, String text) {
            this.type = type;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.text = text;
        }
    }
}
