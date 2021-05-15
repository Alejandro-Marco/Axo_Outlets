package com.axolotl.axo.customview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.axolotl.axo.R;

public class NestedScrollViewWithMaxHeight extends NestedScrollView {
    public NestedScrollViewWithMaxHeight(@NonNull Context context) {
        super(context);
    }

    public NestedScrollViewWithMaxHeight(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedScrollViewWithMaxHeight(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float height = getResources().getDimension(R.dimen.controller_list_max_height);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)height, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
