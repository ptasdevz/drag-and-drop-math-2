package com.ptasdevz.draganddropmath2;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;

import static com.ptasdevz.draganddropmath2.MathElement.MathElementsNameRes;
import static com.ptasdevz.draganddropmath2.MathElement.remoteDataRecieved;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout workspaceLayout, mainLayout;
    public static final String TAG = "ptasdevz";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DragAndDropMathApplication.APPLICATION_ID = Installation.id(MainActivity.this);
        setupStompConnection();

        workspaceLayout = findViewById(R.id.workspace);
        mainLayout = findViewById(R.id.cLayoutMain);

        //Set up all maths elements with drag and drop capabilities.
        for (HashMap.Entry<String, Integer> entry : MathElementsNameRes.entrySet()) {

            String name = entry.getKey();
            int resId = entry.getValue();
            final ImageView mathEleSrcImg = findViewById(resId);

            MathElementFactory.getNewInstance(this,
                    mathEleSrcImg, name, 0, workspaceLayout, mainLayout, false,
                    false);
        }
    }

    @SuppressLint("CheckResult")
    private void setupStompConnection() {
        try {
            DragAndDropMathApplication.mStompClient = Stomp.over(
                    Stomp.ConnectionProvider.OKHTTP, "ws://192.168.137.1:8181/endpoint/websocket");
            DragAndDropMathApplication.mStompClient.connect();

            DragAndDropMathApplication.mStompClient.topic("/topic/math-element-message")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topicMessage -> {
                        String payload = topicMessage.getPayload();

                        Gson gson = new Gson();
                        Type mapType = new TypeToken<HashMap<String, Object>>() {
                        }.getType();
                        Type elePosType = new TypeToken<ArrayList<ElementPos>>() {
                        }.getType();
                        HashMap<String, Object> remoteDataMap = gson.fromJson(payload, mapType);
                        remoteDataRecieved = remoteDataMap;
                        String appId = (String) remoteDataMap.get("appId");

                        if (!appId.equalsIgnoreCase(DragAndDropMathApplication.APPLICATION_ID)) {

                            String elementPosLisStr = (String) remoteDataMap.get("ptrPosList");

                            ArrayList<ElementPos> elementPosArrayList = gson.fromJson(elementPosLisStr, elePosType);
                            Log.d(TAG, "setupStompConnection: remote map" + remoteDataMap);
                            executeRemoteGesture(remoteDataMap, elementPosArrayList);

                            /*
                            for (PtrPos ptrPos:ptrPosArrayList) {

                                float ratioX = (float) (ptrPos.X / parentWidth);
                                float ratioY = (float) (ptrPos.Y / parentHeight);
                                ptrPos.X = ratioX * localPwidth;
                                ptrPos.Y = ratioY * localPheight;

                                switch (ptrPos.action){
                                    case MotionEvent.ACTION_DOWN:{
                                        Log.d(TAG, "setupStompConnection: action down"  );
                                        DragAndDropMathApplication.actionDownLocalOptions(mathElement,
                                                ptrPos.X,ptrPos.Y);
                                    }
                                    break;
                                    case MotionEvent.ACTION_MOVE:{
                                        Log.d(TAG, "setupStompConnection: action move"  );
                                        DragAndDropMathApplication.actionMoveLocalOptions(mathElement,
                                                ptrPos.ptrIdx,ptrPos.X, ptrPos.Y);
                                    }
                                    break;
                                    case MotionEvent.ACTION_UP:{
                                        Log.d(TAG, "setupStompConnection: action up"  );
//                                        actionUpLocalOptionsEleOriginal(mathElement);
                                    }
                                    break;
                                }
                                SystemClock.sleep(50);
                            }


                            /*
                            MathElementRemote tutorMyPeerElement = MathElementRemote.fromJsonString(payload);
                            MathElementRemote elementRemote = tutorMyPeerElement;
                            MathElement mathElement = MathElement.mathEleList.get(elementRemote.name);
                            if (mathElement != null){

                                MathElementRemote elementRemoteLocal = DragAndDropMathApplication.getElementRemote();
                                Log.d(TAG, "setupStompConnection: element local " + elementRemoteLocal);
                                Log.d(TAG, "setupStompConnection: element remote " + elementRemote);

                                float motionPosRatioX = elementRemote.motionPositionX / elementRemote.parentWidth;
                                float motionPosRatioY = elementRemote.motionPositionY / elementRemote.parentHeight;

                                elementRemote.motionPositionX
                                        = motionPosRatioX * mainLayout.getWidth();
                                elementRemote.motionPositionY
                                        = motionPosRatioY *mainLayout.getHeight();


                                switch (elementRemote.remoteAction){
                                    case MotionEvent.ACTION_DOWN:{
                                        Log.d(TAG, "setupStompConnection: action down"  );
                                        DragAndDropMathApplication.actionDownLocalOptions(mathElement,
                                                elementRemote.motionPositionX,elementRemote.motionPositionY);
                                    }
                                    break;
                                    case MotionEvent.ACTION_MOVE:{
                                        Log.d(TAG, "setupStompConnection: action move"  );
                                        DragAndDropMathApplication.actionMoveLocalOptions(mathElement,
                                                elementRemote.pointerIndex,elementRemote.motionPositionX,
                                                elementRemote.motionPositionY);
                                    }
                                    break;
                                    case MotionEvent.ACTION_UP:{
                                        actionUpLocalOptionsEleOriginal(mathElement);
                                    }
                                    break;
                                }
                            }*/
                        }
                        final String msg = payload;
                        runOnUiThread(() -> {
                            try {

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }, throwable -> {
                        Log.e(DragAndDropMathApplication.TAG, "Error on subscribe topic", throwable);
                    });

            DragAndDropMathApplication.mStompClient.lifecycle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(lifecycleEvent -> {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                Log.d(DragAndDropMathApplication.TAG, "Stomp connection opened");
                                break;
                            case CLOSED:
                                Log.d(DragAndDropMathApplication.TAG, "Stomp connection closed");
                                break;
                            case ERROR:
                                Log.e(DragAndDropMathApplication.TAG, "Stomp connection error", lifecycleEvent.getException());
                                break;
                        }
                    }, throwable -> {
                        Log.e(DragAndDropMathApplication.TAG, "Error on subscribe lifecycle", throwable);
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void executeRemoteGesture(HashMap<String, Object> remoteDataMap,
                                      ArrayList<ElementPos> elementPosArrayList) {
        new Thread(() -> {

            int[] locationImg = new int[2];
            double parentWidth = (double) remoteDataMap.get("parentWidth");
            double parentHeight = (double) remoteDataMap.get("parentHeight");

            String eleName = (String) remoteDataMap.get("eleName");
            MathElement mathElement = MathElement.mathEleList.get(eleName);
            ImageView eleImage = mathElement.getEleImg();
            eleImage.setTag("remote");

            Instrumentation instrumentation = new Instrumentation();
            Rect imageRect = new Rect();
            eleImage.getLocalVisibleRect(imageRect);

            /*
                retrieves the x and y coordinates at the top-left pos of elements
                coordinates are in relation to the current window in which
                the elements are located.
             */
            eleImage.getLocationInWindow(locationImg);
            int width = eleImage.getWidth();
            int height = eleImage.getHeight();
            int halfWidthImageView = width / 2;
            int halfHeightImageView = height / 2;
            float eventX = locationImg[0] + halfHeightImageView;
            float eventY = locationImg[1] + halfHeightImageView;

            for (ElementPos elementPos : elementPosArrayList) {
                double ptrPosXRatio = elementPos.X / parentWidth;
                double ptrPosYRatio = elementPos.Y / parentHeight;
                elementPos.X = (float) ptrPosXRatio * mainLayout.getWidth();
                elementPos.Y = (float) ptrPosYRatio * mainLayout.getHeight();
                switch (elementPos.action) {

                    case MotionEvent.ACTION_DOWN: {
                        //auto press down on the center of the image
                        instrumentation.sendPointerSync(
                                MotionEvent.obtain(
                                        SystemClock.uptimeMillis(),
                                        SystemClock.uptimeMillis(),
                                        MotionEvent.ACTION_DOWN,
                                        locationImg[0] + halfWidthImageView,  //press would simulate center
                                        locationImg[1] + halfHeightImageView, // press would simulate center
                                        0));
                    }
                    break;
                    case MotionEvent.ACTION_MOVE: {
                        eventX += elementPos.X;
                        eventY += elementPos.Y;
                        MotionEvent.PointerProperties pointerProperties =
                                new MotionEvent.PointerProperties();
                        MotionEvent.PointerProperties[] pproperties =
                                new MotionEvent.PointerProperties[1];
                        pointerProperties.id = 0;
                        pproperties[0] = pointerProperties;

                        MotionEvent.PointerCoords pointerCoords =
                                new MotionEvent.PointerCoords();
                        MotionEvent.PointerCoords[] pcoords =
                                new MotionEvent.PointerCoords[1];

                        pointerCoords.x = eventX;
                        pointerCoords.y = eventY;

                        pointerCoords.pressure = 1;
                        pointerCoords.size = 1;
                        pointerCoords.orientation = 0;
                        pointerCoords.toolMajor = 0;
                        pointerCoords.touchMajor = 0;
                        pointerCoords.toolMinor = 0;
                        pointerCoords.touchMinor = 0;
                        pointerCoords.pressure = 0;
                        pcoords[0] = pointerCoords;

                        MotionEvent motionEventMove = MotionEvent.obtain(
                                SystemClock.uptimeMillis(),
                                SystemClock.uptimeMillis() + 1L,
                                MotionEvent.ACTION_MOVE,
                                1,
                                pproperties,
                                pcoords,
                                0,
                                0,
                                1,
                                1,
                                0,
                                0,
                                0,
                                0);

                        instrumentation.sendPointerSync(motionEventMove);

                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        instrumentation.sendPointerSync(
                                MotionEvent.obtain(
                                        SystemClock.uptimeMillis(),
                                        SystemClock.uptimeMillis(),
                                        MotionEvent.ACTION_UP,
                                        eventX,
                                        eventY,
                                        0));
                        eleImage.setTag(null);

                    }
                    break;
                }
            }
        }).start();
    }
}
