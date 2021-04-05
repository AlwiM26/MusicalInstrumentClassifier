package com.example.instrumentclassifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private TextView txtRecord;
    private LottieAnimationView btnRecord;
    private MediaRecorder mRecorder;
    private Calendar calendar;

    private static String fileName, savePath = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRecord = findViewById(R.id.txt_record);
        btnRecord = findViewById(R.id.btn_record);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();

                btnRecord.playAnimation();
                txtRecord.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(), "Start recording...", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnRecord.cancelAnimation();
                        btnRecord.setProgress(0f);
                        txtRecord.setVisibility(View.VISIBLE);

                        stopRecording();

                        connectToServer(fileName, savePath);
                    }
                }, 5000);
            }
        });
    }

    private void startRecording() {
        if (CheckPermissions()) {
            fileName = "rekaman_alat_musik-"
                    + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.getInstance().getTime()) + ".3gp";
            savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(savePath);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            mRecorder.start();
        } else {
            RequestPermissions();
        }
    }

    public void stopRecording() {
        try {
            mRecorder.stop();

            mRecorder.release();
            mRecorder = null;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // and set it to null
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    void connectToServer(String fileName, String savePath) {
        // create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // access the recorded sound at the savePath directory
        File audioFile = new File(savePath);

        // create multipart to prepare the upload request body
        MultipartBody.Builder mMultipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM).
                addFormDataPart("file",fileName, RequestBody.create(audioFile, MediaType.parse("*/*")));
        // build the multipart to request body
        RequestBody requestBody = mMultipartBody.build();

        // create a new request with the server url
        // set the post request to the request body
        Request req = new Request.Builder()
                .url("http://192.168.100.100:5000/uploadfile")
                .post(requestBody)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        call.cancel();
                    }
                });
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            showResponse(response.body().string());
                        } catch (IOException e) {
                            showResponse(e.toString());
                        }
                    }
                });
            }
        });
    }

    void showResponse(String res) {
        Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
    }
}