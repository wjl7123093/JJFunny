package com.snowapp.jjfunny.ui.publish;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.snowapp.jjfunny.R;
import com.snowapp.libnavannotation.ActivityDestination;

@ActivityDestination(pageUrl = "main/tabs/publish", asStarter = false)
public class CaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_capture);
        TextView textView = findViewById(R.id.text_capture);
        textView.setText("This is capture activity");
    }

}
