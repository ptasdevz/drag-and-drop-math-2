package com.ptasdevz.draganddropmath2;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MathElementConstraintLayout extends ConstraintLayout {

    public static final String TAG = "ptasdevz";
    Activity activity;

    // a list of drag events from the generating source
    public static ConcurrentLinkedQueue<ShadowDragEvent> shadowDragEvents = new ConcurrentLinkedQueue<>();
    public static ArrayList<MathElementConstraintLayout> layouts = new ArrayList<>();
    public static Object eventAction = new Object();

    public static Thread thread;
    private int statusHeight;
    private boolean hasEntered;

    // Step 1 - This interface defines the type of messages one wants to communicate to the owner
    public interface OnShadowDragListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onShadowDrag(View view, ShadowDragEvent shadowDragEvent);
    }

    // Step 2 - This variable represents a listener which is passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private OnShadowDragListener shadowDragListener;

    public MathElementConstraintLayout(Context context) {
        super(context);
        init();
    }

    public MathElementConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public MathElementConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    // Assign the listener implementing events interface that will receive the events
    // registering the callback
    public void setOnShadowDragEventListener(OnShadowDragListener listener) {
        shadowDragListener = listener;

        //add instances so that they can be notified with events in the future.
        layouts.add(this);
    }

    void init() {

        processDragEvents();
    }


    private static void processDragEvents() {

        //ensures a single thread is always processing the drag events
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(() -> {

                while (true) {
                    try {

                        ShadowDragEvent dragEvent = shadowDragEvents.poll();
                        if (dragEvent != null) {
                            for (MathElementConstraintLayout layout : layouts) {
                                layout.dispatchShadowDragEvent(dragEvent);
                            }

                        } else {
                            synchronized (eventAction) {
                                eventAction.wait();
                            }

                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error occurred in process drag events: "
                                + Log.getStackTraceString(e));
                    }
                }
            });
            thread.start();
        }
    }


    private boolean dispatchShadowDragEvent(ShadowDragEvent shadowDragEvent) {
        if (statusHeight == 0) statusHeight = getStatusBarHeight();
        int[] coord = new int[2];
        this.getLocationOnScreen(coord);
        float viewRawX = coord[0];
        float viewRawY = coord[1];
        float shadowRawX = shadowDragEvent.getX();
        float shadowRawY = shadowDragEvent.getY();
        boolean isDispatchEvent;
        switch (shadowDragEvent.getAction()) {
            case MathElement.MathMotionEvent.PRESS_DOWN:
                shadowDragEvent.setAction(DragEvent.ACTION_DRAG_STARTED);
                if (shadowRawX >= viewRawX && shadowRawY >= viewRawY) {
                    covertPositionRelativeToView(shadowDragEvent, viewRawX, viewRawY, shadowRawX,
                            shadowRawY);
                }
                isDispatchEvent = true;
                break;
            case MathElement.MathMotionEvent.MOVE_AROUND: {
                if (shadowRawX >= viewRawX && shadowRawY >= viewRawY) {
                    shadowDragEvent.setAction(DragEvent.ACTION_DRAG_ENTERED);
                    hasEntered = true;
                    isDispatchEvent = true;
                } else if ((shadowRawX < viewRawX || shadowRawY < viewRawY) && hasEntered) {
                    shadowDragEvent.setAction(DragEvent.ACTION_DRAG_EXITED);
                    hasEntered = false;
                    isDispatchEvent = true;
                } else if (hasEntered) {
                    shadowDragEvent.setAction(DragEvent.ACTION_DRAG_LOCATION);
                    isDispatchEvent = true;
                } else  isDispatchEvent = false;

                if (isDispatchEvent && hasEntered)
                    covertPositionRelativeToView(shadowDragEvent, viewRawX, viewRawY, shadowRawX,
                            shadowRawY);

            }
            break;
            case MathElement.MathMotionEvent.DROP: {
                if (hasEntered && shadowDragEvent.getAction() == MathElement.MathMotionEvent.DROP) {
                    shadowDragEvent.setAction(DragEvent.ACTION_DROP);
                    isDispatchEvent = true;
                    covertPositionRelativeToView(shadowDragEvent, viewRawX, viewRawY, shadowRawX,
                            shadowRawY);

                } else {
                    isDispatchEvent = false;
                }
            }
            break;
            case MathElement.MathMotionEvent.LIFT_UP:
                shadowDragEvent.setAction(DragEvent.ACTION_DRAG_ENDED);
                if (hasEntered)
                    covertPositionRelativeToView(shadowDragEvent, viewRawX, viewRawY, shadowRawX,
                            shadowRawY);

                isDispatchEvent = true;
                hasEntered = false;
                break;
            default:
                isDispatchEvent = false;
        }
        if (isDispatchEvent) {
            shadowDragListener.onShadowDrag(this, shadowDragEvent);
        }

        return true;
    }

    private void covertPositionRelativeToView(ShadowDragEvent shadowDragEvent, float viewRawX,
                                              float viewRawY, float shadowRawX, float shadowRawY) {
        //convert shadow position from absolute to relative to view
        shadowDragEvent.setX(shadowRawX - viewRawX);
        shadowDragEvent.setY(shadowRawY - viewRawY);
    }

    public int getStatusBarHeight() {
        if (activity == null) activity = getActivity();
        Rect r = new Rect();
        Window w = activity.getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        return r.top;
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
