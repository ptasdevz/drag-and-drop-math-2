package com.ptasdevz.draganddropmath2;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.ptasdevz.draganddropmath2.MathElement.mathEleNameRes;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout workspaceLayout, mainLayout;
    public static final String TAG = "ptasdevz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DragAndDropMathApplication.APPLICATION_ID = Installation.id(MainActivity.this);
        DragAndDropMathApplication.getInstance().setupStompConnection();

        workspaceLayout = findViewById(R.id.workspace);
        mainLayout = findViewById(R.id.canvas);
        DragAndDropMathApplication.getInstance().setupRemoteMotion(this);

        workspaceLayout.setOnDragListener((v, event) -> {

            int action = event.getAction();
            MathElement mathElement = (MathElement) event.getLocalState();
            String imageName = mathElement.getName();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:

                    Log.d(TAG, "workspace: drag Started for " + imageName);
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED: {
                    Log.d(TAG, imageName + " has entered workspace at location x:"+event.getX()
                            + " y:"+event.getY());
                }
                return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d(TAG,imageName + " is at location x:"+ event.getX()+ " y:"+ event.getY()
                            +"within the workspace" );
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG,imageName + " has exited workspace at location x:"+event.getX()
                            + " y:"+event.getY());
                    return true;
                case DragEvent.ACTION_DROP:
                    Log.d(MainActivity.TAG, imageName + " has been dropped in the workspace " +
                            "at location x:"+event.getX() + " y:"+event.getY());
                    return true;
                case DragEvent.ACTION_DRAG_ENDED: {
                    Log.d(MainActivity.TAG, "workspace: drag has ended for " + imageName);
                }
                return true;
            }
            return false;
        });

        //Set up all maths elements with drag and drop capabilities.
        for (HashMap.Entry<String, Integer> entry : mathEleNameRes.entrySet()) {

            String name = entry.getKey();
            int resId = entry.getValue();
            final ImageView mathEleSrcImg = findViewById(resId);

            MathElement mathElement = MathElementFactory.getNewInstance(this,
                    mathEleSrcImg, name, 0, workspaceLayout, mainLayout, false,
                    null);

            //wait for measurements to be ready
            mathEleSrcImg.post(() -> {
                mathElement.setInitialElePosX(mathEleSrcImg.getX());
                mathElement.setInitialElePosY(mathEleSrcImg.getY());
            });
        }

    }
}
