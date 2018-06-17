package jp.co.sharp.sample.scenario;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;
import jp.co.sharp.sample.scenario.customize.ScenarioDefinitions;
import jp.co.sharp.sample.scenario.util.VoiceUIManagerUtil;
import jp.co.sharp.sample.scenario.util.VoiceUIVariableUtil;
import jp.co.sharp.sample.scenario.util.VoiceUIVariableUtil.VoiceUIVariableListHelper;


/**
 * 各種シナリオのサンプルを実行するためのアプリ.
 */
public class MainActivity extends Activity implements MainActivityVoiceUIListener.MainActivityScenarioCallback, CameraBridgeViewBase.CvCameraViewListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private CascadeClassifier detector;
    private static int noFaceFrameNum = 0;
    private Size minFaceSize;
    /**
     * 音声UI制御.
     */
    private VoiceUIManager mVoiceUIManager = null;
    /**
     * 音声UIイベントリスナー.
     */
    private MainActivityVoiceUIListener mMainActivityVoiceUIListener = null;
    /**
     * 音声UIの再起動イベント検知.
     */
    private VoiceUIStartReceiver mVoiceUIStartReceiver = null;
    /**
     * ホームボタンイベント検知.
     */
    private HomeEventReceiver mHomeEventReceiver;
    /**
     * UIスレッド処理用.
     */
    private Handler mHandler = new Handler();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
        if(!OpenCVLoader.initDebug()){
            Log.i("OpenCV", "Failed");
        }else{
            Log.i("OpenCV", "successfully built !");
        }
        detector = new CascadeClassifier("/storage/emulated/0/DCIM/100SHARP/haarcascade_frontalface_default.xml");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        minFaceSize = new Size(640/10, 480/10);

//        // Yes/No 聞き返しボタン
//        Button ButtonYesNo = (Button)findViewById(R.id.button_yesno);
//        ButtonYesNo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_YESNO);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // ruleタグボタン
//        Button ButtonTagRule = (Button)findViewById(R.id.button_tag_rule);
//        ButtonTagRule.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_TAG_RULE);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // aタグボタン
//        Button ButtonTagA = (Button)findViewById(R.id.button_tag_a);
//        ButtonTagA.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_TAG_A);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // nextタグボタン
//        Button ButtonTagNext = (Button)findViewById(R.id.button_tag_next);
//        ButtonTagNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_TAG_NEXT);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // speechタグボタン
//        Button ButtonTagSpeech = (Button)findViewById(R.id.button_tag_speech);
//        ButtonTagSpeech.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_TAG_SPEECH);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // memoryタグボタン
//        Button ButtonTagMemory = (Button)findViewById(R.id.button_tag_memory);
//        ButtonTagMemory.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_TAG_MEMORY);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // controlタグボタン
//        Button ButtonTagControl = (Button)findViewById(R.id.button_tag_control);
//        ButtonTagControl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_TAG_CONTROL);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // behaviorタグボタン
//        Button ButtonTagBehavior = (Button)findViewById(R.id.button_tag_behavior);
//        ButtonTagBehavior.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_TAG_BEHAVIOR);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // 演算子ボタン
//        Button ButtonOperator = (Button)findViewById(R.id.button_operator);
//        ButtonOperator.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_OPERATOR);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // 変数ボタン
//        Button ButtonVariable = (Button)findViewById(R.id.button_variable);
//        ButtonVariable.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_VARIABLE);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // name_for_speechボタン
//        Button ButtonNameForSpeech = (Button)findViewById(R.id.button_name_for_speech);
//        ButtonNameForSpeech.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_NAME_FOR_SPEECH);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });
//
//        // finish app：アプリ終了ボタン
//        Button finishAppButton = (Button)findViewById(R.id.finish_app_button);
//        finishAppButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mVoiceUIManager != null) {
//                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_END_APP);
//                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//                }
//            }
//        });

        //ホームボタンの検知登録.
        mHomeEventReceiver = new HomeEventReceiver();
        IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeEventReceiver, filterHome);

        //VoiceUI再起動の検知登録.
        mVoiceUIStartReceiver = new VoiceUIStartReceiver();
        IntentFilter filter = new IntentFilter(VoiceUIManager.ACTION_VOICEUI_SERVICE_STARTED);
        registerReceiver(mVoiceUIStartReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");

        //VoiceUIManagerのインスタンス取得.
        if (mVoiceUIManager == null) {
            mVoiceUIManager = VoiceUIManager.getService(getApplicationContext());
        }
        //MainActivityVoiceUIListener生成.
        if (mMainActivityVoiceUIListener == null) {
            mMainActivityVoiceUIListener = new MainActivityVoiceUIListener(this);
        }
        //VoiceUIListenerの登録.
        VoiceUIManagerUtil.registerVoiceUIListener(mVoiceUIManager, mMainActivityVoiceUIListener);

        //Scene有効化.
        VoiceUIManagerUtil.enableScene(mVoiceUIManager, ScenarioDefinitions.SCENE_COMMON);

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        if (mVoiceUIManager != null) {
            VoiceUIVariableUtil.VoiceUIVariableListHelper helper = new VoiceUIVariableUtil.VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_YESNO);
            VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
        }
//        try {
//            Thread.sleep(18000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (mVoiceUIManager != null) {
//            VoiceUIVariableUtil.VoiceUIVariableListHelper helper = new VoiceUIVariableUtil.VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_OPERATOR
//            );
//            VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");

        //バックに回ったら発話を中止する.
        VoiceUIManagerUtil.stopSpeech();

        //VoiceUIListenerの解除.
        VoiceUIManagerUtil.unregisterVoiceUIListener(mVoiceUIManager, mMainActivityVoiceUIListener);

        //Scene無効化.
        VoiceUIManagerUtil.disableScene(mVoiceUIManager, ScenarioDefinitions.SCENE_COMMON);

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        //単一Activityの場合はonPauseでアプリを終了する.
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        //ホームボタンの検知破棄.
        this.unregisterReceiver(mHomeEventReceiver);

        //VoiceUI再起動の検知破棄.
        this.unregisterReceiver(mVoiceUIStartReceiver);

        //インスタンスのごみ掃除.
        mVoiceUIManager = null;
        mMainActivityVoiceUIListener = null;
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(Mat inputFrame) {
//        if (mVoiceUIManager != null) {
//            VoiceUIVariableUtil.VoiceUIVariableListHelper helper = new VoiceUIVariableUtil.VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_OPERATOR);
//            VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
//        }
        Mat src = inputFrame;
        Mat detected = src.clone();
        try{
            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
            MatOfRect faces = new MatOfRect();
            detector.detectMultiScale(src, faces, 1.1, 2, 2, minFaceSize, new Size());
            Rect[] facesArray = faces.toArray();
            if (faces.empty()){
                noFaceFrameNum++;
//                Log.i("OpenCV", String.valueOf(noFaceFrameNum));
            }
            else{
                noFaceFrameNum = 0;
            }
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(detected, facesArray[i].tl(), facesArray[i].br(), new Scalar(255, 0, 0), 3);
            }
        }
        catch (Exception ex){
            Log.i("OpenCV", ex.getMessage());
        }

        if (noFaceFrameNum >= 60){
            VoiceUIManagerUtil.stopSpeech();
            if (mVoiceUIManager != null) {
                VoiceUIVariableUtil.VoiceUIVariableListHelper helper = new VoiceUIVariableUtil.VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_NAME_FOR_SPEECH);
                VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
            }
            Log.i("OpenCV", "おきて！");
            noFaceFrameNum = 0;
        }

        return detected;
    }
    /**
     * VoiceUIListenerクラスからのコールバックを実装する.
     */
    @Override
    public void onExecCommand(String command, List<VoiceUIVariable> variables) {
        Log.v(TAG, "onExecCommand() : " + command);
        switch (command) {
            case ScenarioDefinitions.FUNC_END_APP:
                finish();
                break;
            case ScenarioDefinitions.FUNC_TEST_CONTROL:
                final String contents = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_TOAST_CONTENTS);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!isFinishing()) {
                            Toast.makeText(getApplicationContext(), contents , Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * ホームボタンの押下イベントを受け取るためのBroadcastレシーバークラス.<br>
     * <p/>
     * アプリは必ずホームボタンで終了する..
     */
    private class HomeEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Receive Home button pressed");
            // ホームボタン押下でアプリ終了する.
            finish();
        }
    }

    /**
     * 音声UI再起動イベントを受け取るためのBroadcastレシーバークラス.<br>
     * <p/>
     * 稀に音声UIのServiceが再起動することがあり、その場合アプリはVoiceUIの再取得とListenerの再登録をする.
     */
    private class VoiceUIStartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (VoiceUIManager.ACTION_VOICEUI_SERVICE_STARTED.equals(action)) {
                Log.d(TAG, "VoiceUIStartReceiver#onReceive():VOICEUI_SERVICE_STARTED");
                //VoiceUIManagerのインスタンス取得.
                mVoiceUIManager = VoiceUIManager.getService(getApplicationContext());
                if (mMainActivityVoiceUIListener == null) {
                    mMainActivityVoiceUIListener = new MainActivityVoiceUIListener(getApplicationContext());
                }
                //VoiceUIListenerの登録.
                VoiceUIManagerUtil.registerVoiceUIListener(mVoiceUIManager, mMainActivityVoiceUIListener);
            }
        }
    }
}
