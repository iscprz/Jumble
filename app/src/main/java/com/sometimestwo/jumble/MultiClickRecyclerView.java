package com.sometimestwo.jumble;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MultiClickRecyclerView extends RecyclerView {
    boolean handleTouchEvents = true;

    public MultiClickRecyclerView(Context context) {
        super(context);
    }

    public MultiClickRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiClickRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isHandleTouchEvents() {
        return handleTouchEvents;
    }

    public void setHandleTouchEvents(boolean handleTouchEvents) {
        this.handleTouchEvents = handleTouchEvents;
    }

    /*
        This part was added to handle the GIF preview hover image view. Recyclerview was scrolling
        when hover view was open so we ignore touch events until hover view is closed this way
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return handleTouchEvents ? super.onInterceptTouchEvent(event) : false;
    }
}
