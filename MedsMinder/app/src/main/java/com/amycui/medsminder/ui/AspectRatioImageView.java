package com.amycui.medsminder.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.amycui.medsminder.R;


public class AspectRatioImageView  extends ImageView{
    Context mContext;

    public AspectRatioImageView(Context context) {
        super(context);
        mContext = context;
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mContext.getResources().getFraction(R.fraction.card_image_aspect_ratio, 1, 1)));
    }
}
