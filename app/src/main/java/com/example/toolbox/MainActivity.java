package com.example.toolbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button techButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        techButton = (Button)findViewById(R.id.tech_button);
        techButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://bkjw.whu.edu.cn/stu/stu_index.jsp"));
                //wer
                startActivity(Intent.createChooser(intent, "访问教务系统"));
            }
        });
    }
}
