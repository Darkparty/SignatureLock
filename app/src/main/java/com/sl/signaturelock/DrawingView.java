package com.sl.signaturelock;

import android.content.Context;

import android.graphics.*;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class DrawingView extends View {

    private long startTime;
    private long endTime;
    private Path drawPath;
    private Paint drawPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        startTime = Long.MAX_VALUE;
        endTime = 0;
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(0xFF000000);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int i = 1; i < h; i++) {
            for (int j = 1; j < w; j++) {
                canvasBitmap.setPixel(j, i, 0xFFFFFFFF);
            }
        }
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, drawPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                if (System.currentTimeMillis() < startTime) startTime = System.currentTimeMillis();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                if (System.currentTimeMillis() > endTime) endTime = System.currentTimeMillis();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        for (int i = 1; i < canvasBitmap.getHeight(); i++) {
            for (int j = 1; j < canvasBitmap.getWidth(); j++) {
                canvasBitmap.setPixel(j, i, 0xFFFFFFFF);
            }
        }
        startTime = Long.MAX_VALUE;
        endTime = 0;
        invalidate();
    }

    public double getTime() {
        double answer = (endTime - startTime)/1000.0;
        if (answer < 0) return 0.00;
        else return answer;
    }

    public Bitmap getCanvasBitmap() {
        return canvasBitmap;
    }

    public String getBase64Picture(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}

