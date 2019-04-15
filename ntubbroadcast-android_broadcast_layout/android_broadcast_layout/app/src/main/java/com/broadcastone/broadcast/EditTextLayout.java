package com.broadcastone.broadcast;

import android.widget.EditText;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;


/**
 * Created by ChuLay on 2015/5/22.
 */
public class EditTextLayout extends EditText{
    private Paint mPaint;

    public EditTextLayout(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        //圖底線
        canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1, mPaint);
    }
}
