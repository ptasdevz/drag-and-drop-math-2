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

    public static String APPLICATION_ID;
    public static int mActivePointerId = INVALID_POINTER_ID;
    ConstraintLayout workspace, cLayoutMain;
    boolean isConfig = false;
    String lastDragDataStr;
    ImageView trash;
    WebSocketClient webSocketClient;
    private StompClient mStompClient;
    private String sendMathElementURI = "/app/send-math-element-message";
    MathElementRemote elementRemote = new MathElementRemote();


    static String TAG = "ptasdevz";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        APPLICATION_ID = Installation.id(MainActivity.this);
        setupStompConnection();
        workspace = findViewById(R.id.workspace);
        workspace.setTag("workspace");
        cLayoutMain = findViewById(R.id.cLayoutMain);
        cLayoutMain.setTag("main");
        ImageView trash = findViewById(R.id.trash);

        MathElement trashEle = MathElementFactory.getNewInstance(this, trash,
                MathElement.TRASH, workspace.getId(), workspace, false);

        MathElement.mathEleList.put(trashEle.getName(), trashEle);

        //Set up all maths elements with drag and drop capabilities.
        for (HashMap.Entry<String, Integer> entry : MathElementsNameRes.entrySet()) {

            String name = entry.getKey();
            int resId = entry.getValue();
            final ImageView mathEleSrcImg = findViewById(resId);
            final MathElement mathEleOriginal;

            mathEleOriginal = MathElementFactory.getNewInstance(this,
                    mathEleSrcImg, name, 0, cLayoutMain, false);
            MathElement.mathEleList.put(name, mathEleOriginal);

            mathEleSrcImg.setOnTouchListener((view, ev) -> {
                final int action = ev.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        actionDownOptions(mathEleOriginal, view, ev);
                        mathEleOriginal.remoteAction = MotionEvent.ACTION_DOWN;
                        elementRemote.remoteAction = MotionEvent.ACTION_DOWN;
                        elementRemote.mathEleRemoteImage.top = mathEleOriginal.eleImg.getTop();
                        elementRemote.mathEleRemoteImage.left = mathEleOriginal.eleImg.getLeft();
                        elementRemote.mathEleRemoteImage.bottom = mathEleOriginal.eleImg.getBottom();
                        elementRemote.mathEleRemoteImage.right = mathEleOriginal.eleImg.getRight();
                        ConstraintLayout layout = (ConstraintLayout) mathEleOriginal.eleImg.getParent();
                        elementRemote.mathELeRemoteLayout.top = layout.getTop();
                        elementRemote.mathELeRemoteLayout.left = layout.getLeft();
                        elementRemote.mathELeRemoteLayout.right = layout.getRight();
                        elementRemote.mathELeRemoteLayout.bottom = layout.getBottom();
                        elementRemote.mathELeRemoteLayout.type = Constant.LayoutTypes.MAIN;
                        elementRemote.name = mathEleOriginal.getName();
                        elementRemote.appId = APPLICATION_ID;

                        String payload = TutorMyPeerElement.toJsonString(elementRemote);
                        Log.d(TAG, "onCreate: payload down"+payload);
                        mStompClient.send(sendMathElementURI, payload).subscribe();
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        actionMoveOptions(mathEleOriginal, view, ev);
                        elementRemote.changeOfPosX = mathEleOriginal.getChangeOfPosX();
                        elementRemote.changeOfPosY = mathEleOriginal.getChangeOfPosY();
                        elementRemote.remoteAction = MotionEvent.ACTION_MOVE;
                        String payload = TutorMyPeerElement.toJsonString(elementRemote);
                        Log.d(TAG, "onCreate: payload move "+payload);
                        mStompClient.send(sendMathElementURI, payload).subscribe();
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
                            mathEleOriginal.setDropPosX(viewXConverted + view.getWidth() / 2);
                            mathEleOriginal.setDropPosY(viewYConverted + view.getHeight() / 2);

                            //place math element within workspace
                            MathElement mathEleCopy = null;
                            mathEleCopy = mathEleOriginal.dropElement(workspace);

                            if (mathEleCopy != null) {
                                mathEleCopy.repositionElement();
                                mathEleCopy.learnNeighbouringElements();
                            }
                        }

                        mathEleOriginal.resetMathElePosition();
                        mActivePointerId = INVALID_POINTER_ID;
//                        mathEleOriginal.remoteAction = MotionEvent.ACTION_UP;
//                        String payload = MathElement.toJsonString(mathEleOriginal);
//                        mStompClient.send(sendMathElementURI,payload);
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

    //=================================Helpers======================================================
    public static void actionDownOptions(MathElement mathElement, View view, MotionEvent ev) {

        //get x and y coordinates of point with respect to element
        final int pointerIndex = ev.getActionIndex();
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        // Remember where we started (for dragging)
        mathElement.setLastPtrPosX(x);
        mathElement.setLastPtrPosY(y);

        // Save the ID of this pointer (for dragging)
        mActivePointerId = ev.getPointerId(0);

        // Save initial element position with respect to element's parent
        mathElement.setInitialElePosX(view.getX());
        mathElement.setInitialElePosY(view.getY());
    }

    public static void actionMoveOptions(MathElement mathElement, View view, MotionEvent ev) {
        // Find the index of the active pointer and fetch its position
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);

        if (pointerIndex != -1) {
            final float movingPtrPosX = ev.getX(pointerIndex);
            final float movingPtrPosY = ev.getY(pointerIndex);

            //calculate change of distance from lastPtrPos
            final float dx = movingPtrPosX - mathElement.getLastPtrPosX();
            final float dy = movingPtrPosY - mathElement.getLastPtrPosY();

            //calculate  the future lastPtrPosX and lastPtrPosY of view
            float futurePosYdown = dy + view.getY() + view.getHeight();
            float futurePosYup = dy + view.getY();
            float futurePosXright = dx + view.getX() + view.getWidth();
            float futurePosXleft = dx + view.getX();

        /*
        only update if view position remains within the limits of the layout which is known
        from the future values.
        */
            ConstraintLayout parent = (ConstraintLayout) view.getParent();

            if (futurePosYdown < parent.getHeight() && futurePosYup > 0) {
                mathElement.setChangeOfPosY(mathElement.getChangeOfPosY() + dy);
                mathElement.setLastPtrPosY(movingPtrPosY);// Remember this touch position for the next move event
            }
            if (futurePosXright < parent.getWidth() && futurePosXleft > 0) {

                mathElement.setChangeOfPosX(mathElement.getChangeOfPosX() + dx);
                mathElement.setLastPtrPosX(movingPtrPosX);// Remember this touch position for the next move event
            }

            view.bringToFront();
            mathElement.positionMathEle(false);
        }
    }


    @SuppressLint("CheckResult")
    private void setupStompConnection() {
        try {
            mStompClient = Stomp.over(
                    Stomp.ConnectionProvider.OKHTTP, "ws://192.168.137.1:8181/endpoint/websocket");

            mStompClient.topic("/topic/math-element-message")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        String payload = topicMessage.getPayload();
                        Log.d(TAG, "receiveStompConnectionMsg: " + payload);
                        TutorMyPeerElement tutorMyPeerElement = TutorMyPeerElement.fromJsonString(payload);
                        if (tutorMyPeerElement instanceof MathElementRemote){
                            MathElementRemote elementRemote = (MathElementRemote) tutorMyPeerElement;
                            if (!tutorMyPeerElement.appId.equalsIgnoreCase(APPLICATION_ID)){
                                MathElement mathElement = MathElement.mathEleList.get(elementRemote.name);
                                if (mathElement != null){
                                    Constant.LayoutTypes type = elementRemote.mathELeRemoteLayout.type;

                                    float lLeft = elementRemote.mathELeRemoteLayout.left;
                                    float lTop = elementRemote.mathELeRemoteLayout.top;
                                    float lRight = elementRemote.mathELeRemoteLayout.right;
                                    float lBottom = elementRemote.mathELeRemoteLayout.bottom;

                                    float iLeft = elementRemote.mathEleRemoteImage.left;
                                    float iTop = elementRemote.mathEleRemoteImage.top;
                                    float iRight = elementRemote.mathEleRemoteImage.right;
                                    float iBottom = elementRemote.mathEleRemoteImage.bottom;

                                    float chgOfPosX = elementRemote.changeOfPosX;
                                    float chgOfPosY = elementRemote.changeOfPosY;

                                    if (type == Constant.LayoutTypes.MAIN){
                                        float remoteLayoutWidth = lRight-lLeft;
                                        float localLyoutWidth = cLayoutMain.getWidth();
                                        float posRatioX = iLeft / remoteLayoutWidth;
                                        float posRatioY = iTop / remoteLayoutWidth;
                                        float posX = posRatioX/localLyoutWidth;
                                        float posY = posRatioY/localLyoutWidth;

                                        switch (elementRemote.remoteAction){
                                            case MotionEvent.ACTION_DOWN:{
                                                mathElement.setLastPtrPosX(posX);
                                                mathElement.setLastPtrPosX(posY);

                                                mathElement.setInitialElePosX(posX);
                                                mathElement.setInitialElePosY(posY);
                                            }
                                            break;
                                            case MotionEvent.ACTION_MOVE:{
                                                mathElement.setChangeOfPosX(chgOfPosX);
                                                mathElement.setChangeOfPosY(chgOfPosY);
                                                break;
                                            }
                                        }


                                        mathElement.positionMathEle(false);
                                        Log.d(TAG, "setupStompConnection: mathele "+ mathElement);

                                    }else {

                                    }


                                }
                            }
                        }
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

            mStompClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
