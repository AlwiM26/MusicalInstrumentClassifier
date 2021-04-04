package com.example.instrumentclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    TextView txt_record;
    LottieAnimationView btn_record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_record = findViewById(R.id.txt_record);
        btn_record = findViewById(R.id.btn_record);

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_record.setVisibility(View.INVISIBLE);
                btn_record.playAnimation();

                // Start to count 5 s to record the audio sample
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btn_record.setProgress(0f);
                        btn_record.cancelAnimation();
                        txt_record.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "Sudah 5 detik", Toast.LENGTH_SHORT).show();
                    }
                }, 5000);
            }
        });
    }
}