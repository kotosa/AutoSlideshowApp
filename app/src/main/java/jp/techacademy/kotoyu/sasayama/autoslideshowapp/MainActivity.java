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

    Handler mHandler = new Handler();

    Button mStartButton;
    Button mBackButton;
    Button mNextButton;

    Cursor mCursor;

    int m_iOffset = 0;  //初期位置からのオフセット

    ImageView mImageVIew;
    ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_iOffset = 0;

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo(m_iOffset);
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo(m_iOffset);
        }

        mStartButton = (Button) findViewById(R.id.start_button);
        mBackButton = (Button) findViewById(R.id.back_button);
        mNextButton = (Button) findViewById(R.id.next_button);

        // 再生ボタンを押すとスライドショー開始
        mStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                    if (mTimer == null) {
                        // タイマーの作成
                        mTimer = new Timer();

                        if (mCursor.moveToFirst()) {
                            int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                            Long id = mCursor.getLong(fieldIndex);
                            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                            mImageVIew = (ImageView) findViewById(R.id.imageView);
                            mImageVIew.setImageURI(imageUri);
                        }

                        // タイマーの始動
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // 画像の情報を取得する
                                mResolver = getContentResolver();
                                mCursor = mResolver.query(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                                        null, // 項目(null = 全項目)
                                        null, // フィルタ条件(null = フィルタなし)
                                        null, // フィルタ用パラメータ
                                        null // ソート (null ソートなし)
                                );

                                mCursor.moveToFirst();
                                mCursor.move(m_iOffset);

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCursor.moveToNext()) {
                                            m_iOffset++;
                                        } else {
                                            mCursor.moveToFirst();
                                            m_iOffset = 0;
                                        }

                                        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                                        Long id = mCursor.getLong(fieldIndex);
                                        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                                        mImageVIew = (ImageView) findViewById(R.id.imageView);
                                        mImageVIew.setImageURI(imageUri);

                                        mStartButton.setText("停止");
                                        mBackButton.setText("戻る(操作不可)");
                                        mNextButton.setText("進む(操作不可)");
                                    }
                                });
                            }

                        }, 100, 2000);    // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
                    }
                    else {
                        mTimer.cancel();
                        mTimer = null;
                        mStartButton.setText("再生");
                        mBackButton.setText("戻る");
                        mNextButton.setText("進む");
                    }

            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTimer == null) {
                    if (!mCursor.moveToPrevious()) {
                        mCursor.moveToLast();
                    }
                    m_iOffset = mCursor.getPosition();
                    getContentsInfo(m_iOffset);
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTimer == null) {
                    if (mCursor.moveToNext()) {
                        m_iOffset++;
                    } else {
                        mCursor.moveToFirst();
                        m_iOffset = 0;
                    }
                    m_iOffset = mCursor.getPosition();
                    getContentsInfo(m_iOffset);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo(m_iOffset);
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(int liOffset) {

        // 画像の情報を取得する
        mResolver = getContentResolver();
        mCursor = mResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

            mCursor.moveToFirst();

        if (mCursor.move(liOffset)) {
            int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = mCursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            mImageVIew = (ImageView) findViewById(R.id.imageView);
            mImageVIew.setImageURI(imageUri);
        }

    }

}