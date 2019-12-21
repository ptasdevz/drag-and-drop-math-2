package com.ptasdevz.draganddropmath2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tech.gusavila92.websocketclient.WebSocketClient;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

import static android.view.MotionEvent.INVALID_POINTER_ID;
import static com.ptasdevz.draganddropmath2.MathElement.MathElementsNameRes;

public class MainActivity extends AppCompatActivity {

    public static int mActivePointerId = INVALID_POINTER_ID;
    ConstraintLayout workspace, cLayoutMain;
    boolean isConfig = false;
    String lastDragDataStr;
    ImageView trash;
    WebSocketClient webSocketClient;
    private StompClient mStompClient;

    static String TAG = "ptasdevz";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupStompConnection();
        workspace = findViewById(R.id.workspace);
        cLayoutMain = findViewById(R.id.cLayoutMain);
        ImageView trash = findViewById(R.id.trash);
        MathElement trashEle = MathElementFactory.getNewInstance(this, trash,
                MathElement.TRASH, workspace.getId(), workspace, false);
        MathElement.mathEleList.put(trashEle.getName(),trashEle);

           /*
        Set up all maths elements with drag and drop capabilities.
         */
        for (HashMap.Entry<String, Integer> entry : MathElementsNameRes.entrySet()) {

            String name = entry.getKey();
            int resId = entry.getValue();
            final ImageView mathEleSrcImg = findViewById(resId);
            final MathElement mathEleOriginal = MathElementFactory.getNewInstance(this,
                    mathEleSrcImg,name, 0, cLayoutMain, false);

            MathElement.mathEleList.put(name,mathEleOriginal);

            mathEleSrcImg.setOnTouchListener((view, ev) -> {
                final int action = ev.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        setupActionDownOptions(mathEleOriginal, view, ev);
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        setupActionMoveOptions(mathEleOriginal, view, ev);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        int x = (int) view.getX();
                        int y = (int) view.getY();
                        Rect workspaceBounds = new Rect();
                        workspace.getHitRect(workspaceBounds);

                        //get view coordinates with reference to workspace
                        if (workspaceBounds.contains(x, y)) {

                            //x and y pos are converted to that with respect to workspace
                            int workspaceX = (int) workspace.getX();
                            int workspaceY = (int) workspace.getY();
                            int viewXConverted = x - workspaceX;
                            int viewYConverted = y - workspaceY;

                            // add extra so that x and y coordinates are at the center of element
                            mathEleOriginal.setViewPosX(viewXConverted + view.getWidth()/2);
                            mathEleOriginal.setViewPosY(viewYConverted + view.getHeight()/2);

                            //place math element within workspace
                            MathElement mathEleCopy = mathEleOriginal.dropElement(workspace);
                            if (mathEleCopy != null){
                                mathEleCopy.repositionElement();
                                mathEleCopy.learnNeighbouringElements();
                            }
                            Log.d(TAG, "on action up : in workspace view");
                        } else {
                            Log.d(TAG, "on action up : not in workspace view");
                        }
                        mathEleOriginal.resetMathElePosition();
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL: {
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_POINTER_UP: {

                        final int pointerIndex = ev.getActionIndex();
                        final int pointerId = ev.getPointerId(pointerIndex);

                        if (pointerId == mActivePointerId) {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                            mathEleOriginal.resetMathElePosition();
                        }
                        break;
                    }
                }
                return true;
            });
        }
    }

    public static void setupActionDownOptions(MathElement mathElement, View view, MotionEvent ev) {

        //get x and y coordinates of point with respect to element
        final int pointerIndex = ev.getActionIndex();
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);
        Log.d(TAG, "onTouch: img down: lastPtrPosX:" + x + "----lastPtrPosY:" + y);

        // Remember where we started (for dragging)
        mathElement.setLastPtrPosX(x);
        mathElement.setLastPtrPosY(y);

        // Save the ID of this pointer (for dragging)
        mActivePointerId = ev.getPointerId(0);

        // Save initial element position with respect to element's parent
        mathElement.setInitialElePosX(view.getX());
        mathElement.setInitialElePosY(view.getY());
    }

    public static void setupActionMoveOptions(MathElement mathElement, View view, MotionEvent ev) {
        // Find the index of the active pointer and fetch its position
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);

        final float movingPtrPosX = ev.getX(pointerIndex);
        final float movingPtrPosY = ev.getY(pointerIndex);

        //calculate distance moved
        final float dx = movingPtrPosX - mathElement.getLastPtrPosX();
        final float dy = movingPtrPosY - mathElement.getLastPtrPosY();

        //calculate  the future lastPtrPosX and lastPtrPosY of view
        float futurePosYdown = dy + view.getY() + view.getHeight();
        float futurePosYup = dy + view.getY();
        float futurePosXright = dx + view.getX() + view.getWidth();
        float futurePosXleft = dx + view.getX();

        /*
        only update if view position remains within the limits of the layout.
        */
        ConstraintLayout parent = (ConstraintLayout) view.getParent();

        if (futurePosYdown < parent.getHeight() && futurePosYup > 0) {
            mathElement.setPosY(mathElement.getPosY() + dy);
            mathElement.setLastPtrPosY(movingPtrPosY);// Remember this touch position for the next move event
        }
        if (futurePosXright < parent.getWidth() && futurePosXleft > 0) {

            mathElement.setPosX(mathElement.getPosX() + dx);
            mathElement.setLastPtrPosX(movingPtrPosX);// Remember this touch position for the next move event
        }

        view.bringToFront();
        mathElement.positionViewInCLayout(mathElement.getPosX(), mathElement.getPosY(), view,
                (ConstraintLayout) view.getParent());
    }


    private void setupStompConnection() {
        try {
            mStompClient = Stomp.over(
                    Stomp.ConnectionProvider.OKHTTP, "ws://192.168.137.1:8080/endpoint/websocket");
            mStompClient.connect();

            mStompClient.topic("/topic/send-remote-message")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        String payload = topicMessage.getPayload();
                        Log.d(TAG, "receiveStompConnectionMsg: " + payload);
                        final String msg = payload;
                        runOnUiThread(() -> {
                            try {

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }, throwable -> {
                        Log.e(TAG, "Error on subscribe topic", throwable);
                    });

            mStompClient.lifecycle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(lifecycleEvent -> {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                Log.d(TAG, "Stomp connection opened");
                                break;
                            case CLOSED:
                                Log.d(TAG, "Stomp connection closed");
                                break;
                            case ERROR:
                                Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                                break;
                        }
                    }, throwable -> {
                        Log.e(TAG, "Error on subscribe lifecycle", throwable);
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
