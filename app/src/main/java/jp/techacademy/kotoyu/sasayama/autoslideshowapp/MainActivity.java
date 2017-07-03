package jp.techacademy.kotoyu.sasayama.autoslideshowapp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;
import android.content.ContentResolver;
import java.util.Timer;
import java.util.TimerTask;
import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.Manifest;
import android.content.ContentUris;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Timer mTimer;
    TextView mTimerText;

    // タイマー用の時間のための変数
    double mTimerSec = 0.0;

    Handler mHandler = new Handler();

    Button mStartButton;
    Button mBackButton;
    Button mNextButton;

    Cursor mCursor;
    int mColumnMaxNum;
    int m_CurrentID;

    boolean mbStartFlag = true;    //再生ボタン表示中：TRUE、停止ボタン表示中：FALSE
    ImageView mImageVIew;
    ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }


        mStartButton = (Button) findViewById(R.id.start_button);
        mBackButton = (Button) findViewById(R.id.back_button);
        mNextButton = (Button) findViewById(R.id.next_button);

        // 再生ボタンを押すとスライドショー開始
        mStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mTimer == null) {
                    // タイマーの作成
                    mTimer = new Timer();
                    // タイマーの始動
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                            Long id = mCursor.getLong(fieldIndex);
                            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                            mImageVIew.setImageURI(imageUri);
                            if(!mCursor.moveToNext()) {
                                mCursor.moveToFirst();
                            }

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setContentView(mStartButton);
                                }
                            });
                        }

                    }, 100, 2000);    // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!mCursor.moveToPrevious()) {
                    mCursor.moveToLast();
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!mCursor.moveToNext()) {
                    mCursor.moveToFirst();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        mResolver = getContentResolver();
        mCursor = mResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor.moveToFirst()) {
//            mColumnMaxNum = mCursor.getColumnCount();
            int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = mCursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            mImageVIew = (ImageView) findViewById(R.id.imageView);
            mImageVIew.setImageURI(imageUri);

            mCursor.moveToNext();
            int fieldIndex2 = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id2 = mCursor.getLong(fieldIndex2);

            // おためし


        }
//        cursor.close();

    }

}