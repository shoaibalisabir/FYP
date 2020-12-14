package com.example.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DetectObjects extends AppCompatActivity {

    TextView textView;
    String detctobjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_objects);

        textView=findViewById(R.id.textView);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detctobjects = bundle.getString("detctobjects");
            textView.setText(detctobjects);

            Log.d("detctobjects",detctobjects);

        }

    }
}
