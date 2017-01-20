package com.fsh.scaleseekbar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fsh.scaleseekbar.R;

import java.util.List;

/**
 * 带刻度的seekBar
 */
public class ScaleSeekBar<T extends WithTag> extends View {
    private Drawable thumb;
    private int dotPadding;//设置左右两边刻度的边距
    private Drawable thumbNormal;
    private int progressHeight;
    private int progressColor;
    private int normalBackground;
    private int topTextMargin;
    private int thumbSize;
    private int dotNum;
    private int topTextSize;
    private int topTextColor;

    private List<T> topTexts;//圆点上面的字

    private int position = 1;
    private int offset = 0;

    private Paint mPaint;

    private int downX;
    private int space;

    public ScaleSeekBar(Context context) {
        this(context,null);
    }

    public ScaleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScaleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScaleSeekBar);
        thumbSize = (int) ta.getDimension(R.styleable.ScaleSeekBar_thumbSize,dp2px(20));
        dotPadding = (int) ta.getDimension(R.styleable.ScaleSeekBar_dotPadding,dp2px(15));
        progressHeight = (int) ta.getDimension(R.styleable.ScaleSeekBar_progressHeight,dp2px(10));
        progressColor = ta.getColor(R.styleable.ScaleSeekBar_progressColor, Color.RED);
        topTextMargin = (int) ta.getDimension(R.styleable.ScaleSeekBar_topTextMargin,dp2px(8));
        normalBackground = ta.getColor(R.styleable.ScaleSeekBar_normalBackground, Color.GRAY);
        thumbNormal = ta.getDrawable(R.styleable.ScaleSeekBar_normal_thumb);
        thumb = ta.getDrawable(R.styleable.ScaleSeekBar_thumb);
        topTextSize = (int) ta.getDimension(R.styleable.ScaleSeekBar_topTextSize,dp2px(8));
        topTextColor = ta.getColor(R.styleable.ScaleSeekBar_topTextColor, Color.BLACK);
        dotNum = ta.getInteger(R.styleable.ScaleSeekBar_dot_num,4);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(topTextSize);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        /**
         *  ------------------------------------------------------------------------
         *  \                                                                       \
         *  \paddingleft---dotPadding----------------------dotPadding---paddingRight\
         *  \                                                                       \
         *  -------------------------------------------------------------------------
         */
        //绘制普通背景
        mPaint.setColor(normalBackground);
        mPaint.setStrokeWidth(progressHeight);
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        space = (width - dotPadding*2 + thumbSize)/(dotNum-1);
        canvas.drawLine(getPaddingLeft(),getHeight()/2,getWidth()-getPaddingRight(),getHeight()/2,mPaint);
        //绘制进度
        mPaint.setColor(progressColor);
        if (getPaddingLeft()+dotPadding+position*space+offset > width){//宽度
            canvas.drawLine(getPaddingLeft(),getHeight()/2,getWidth() - getPaddingRight(),getHeight()/2,mPaint);
        }else{
            canvas.drawLine(getPaddingLeft(),getHeight()/2,
                    getPaddingLeft()+dotPadding+position*space+offset-thumbSize/2,getHeight()/2,mPaint);
        }
        //测量文字之间的间距
        int textSpan = 0;
        if (topTexts != null && !topTexts.isEmpty()){
            int totalTextWidth=0;
            for (int i=0;i<topTexts.size();i++){
                float textWidth = mPaint.measureText(topTexts.get(i).getTag());
                totalTextWidth += textWidth;
            }
            textSpan = (width-dotPadding*2  - totalTextWidth)/(topTexts.size()-1);
        }
        for (int i=0;i<dotNum;i++){//绘制没有被选中的点|绘制文字
            thumbNormal.setBounds(getPaddingLeft()+dotPadding+space*i - thumbSize /2 * i,
                    getHeight()/2- thumbSize /2,
                    getPaddingLeft()+space*i+ thumbSize +dotPadding - thumbSize /2 * i,
                    getHeight()/2+ thumbSize /2);
            thumbNormal.draw(canvas);
            if (topTexts == null || topTexts.isEmpty()) continue;
            //绘制文字
            mPaint.setColor(topTextColor);
            float textWidth = 0;
            if (i > 0){
                for (int j =0 ;j<i;j++){
                    textWidth += mPaint.measureText(topTexts.get(j).getTag());
                }
            }
            float textHeight = (mPaint.descent() + mPaint.ascent())/2;
            canvas.drawText(topTexts.get(i).getTag(),getPaddingLeft()+dotPadding+textSpan*i+textWidth,
                    getHeight()/2-progressHeight/2- topTextMargin -textHeight/2,mPaint);
        }
        for(int i=0;i<=position;i++){//绘制选中的点
            thumb.setBounds(getPaddingLeft()+dotPadding+space*i - thumbSize /2 * i,
                    getHeight()/2- thumbSize /2,
                    getPaddingLeft()+space*i+ thumbSize +dotPadding - thumbSize /2 * i,
                    getHeight()/2+ thumbSize /2);
            thumb.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        if (heightModel == MeasureSpec.EXACTLY){
            float textHeight = (mPaint.descent() + mPaint.ascent());
            int exceptHeight = (int) (getPaddingTop() + getPaddingBottom() + (Math.abs(textHeight)+ topTextMargin +
                    Math.max(thumbSize,progressHeight)/2)*2);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(exceptHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                caculetePosition(downX,false);
            case MotionEvent.ACTION_MOVE:
                downX = (int) event.getX();
                caculetePosition(downX,false);
                break;
            case MotionEvent.ACTION_UP:
                downX = (int) event.getX();
                caculetePosition(downX,true);
                break;
        }
        return true;
}
    private void caculetePosition(int x,boolean end){
        if(x < (getPaddingLeft() + dotPadding)){//当x位于第一个点的前面时
            position = 0;
            offset = 0;
            invalidate();
            return;
        }
        if (x > getWidth() - getPaddingRight() - dotPadding){//大于最后一个点
            position = dotNum-1;
            offset = dotPadding;
            invalidate();
            return;
        }
        offset = 0;
        position = (x-dotPadding-getPaddingLeft()) / space;
        if (listener != null) listener.onItemSelected(position);
        if (!end)
            offset = x - dotPadding - getPaddingLeft() - space * position;
        else offset =0;
        invalidate();
    }

    private OnItemSelectedListener listener;
    public interface OnItemSelectedListener{
        void onItemSelected(int position);
    }

    public ScaleSeekBar setOnItemSelectedListener(OnItemSelectedListener listener){
        this.listener = listener;
        return this;
    }

    public ScaleSeekBar setTopText(List<T> datas){
        topTexts = datas;
        dotNum = datas.size();
        return this;
    }

    public T getSelect(){
        return topTexts.get(position);
    }

    private int dp2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
