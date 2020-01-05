package com.ptasdevz.draganddropmath2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;

import static com.ptasdevz.draganddropmath2.MathElement.mathEleNameRes;

public class MainActivity extends AppCompatActivity {

    MathElementConstraintLayout workspaceLayout;
    MathElementConstraintLayout mainLayout;
    MathElementConstraintLayout canvas;
    public static final String TAG = "ptasdevz";
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        askPermission();
        DragAndDropMathApplication.APPLICATION_ID = Installation.id(MainActivity.this);
        DragAndDropMathApplication.getInstance().setupStompConnection();

        workspaceLayout = findViewById(R.id.workspace_layout);
        mainLayout = findViewById(R.id.main_layout);
        canvas = findViewById(R.id.canvas);
        MathElement.setMainLayout(mainLayout);
        MathElement.setWorkspaceLayout(workspaceLayout);
        MathElement.setupCallBackOnWorkspace();
        workspaceLayout.post(new Runnable() {
            @Override
            public void run() {
                int[] coor = new int[2];
                workspaceLayout.getLocationOnScreen(coor);
                Log.d(TAG, "run: workspace absX:"+ coor[0] + " absY: "+ coor[1]);
            }
        });

        DragAndDropMathApplication.getInstance().setupRemoteMotion(this);


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

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }
}
