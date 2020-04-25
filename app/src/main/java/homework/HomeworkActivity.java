package homework;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.toolbox.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import homework_edit.HomeworkEditActivity;

/**
 * Homework list Activity.
 *
 * @author Zhou Jingsen
 * */
public class HomeworkActivity extends AppCompatActivity {
    HomeworkAdapter adapter;
    String TAG="HomeworkActivity";
    public static boolean calendarPermissionGranted=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homework_activity);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HomeworkAdapter(getFilesDir());
        HomeworkEditActivity.adapter=adapter;
        adapter.main=this;
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab=findViewById(R.id.homework_add);
        final HomeworkActivity _this=this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switcher=new Intent(_this,HomeworkEditActivity.class);
                _this.startActivity(switcher);
            }
        });
        calendarPermissionGranted=requestCalendarPermission();

    }

    private boolean requestCalendarPermission() {

        Log.i(TAG,"requestPermission");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG,"checkSelfPermission");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALENDAR)||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_CALENDAR)) {
                Log.i(TAG,"shouldShowRequestPermissionRationale");

                new AlertDialog.Builder(HomeworkActivity.this)
                        .setTitle("e学友需要日历权限")
                        .setMessage("e学友需要日历权限来添加作业提醒。如果拒绝此权限申请，当作业提醒服务被系统清除时将无法正常提醒作业。")
                        .setPositiveButton("确定",null)
                        .show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR},
                        200);

            } else {
                Log.i(TAG,"requestPermissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR},
                        200);
            }
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED&&
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_CALENDAR)
                            == PackageManager.PERMISSION_GRANTED;
        }else{
            return true;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        adapter.saveList();
        Intent intent=new Intent(this,HomeworkNotifyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }

}