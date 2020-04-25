package com.example.toolbox;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.toolbox.movie.MovieActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import attentive.AttentiveActivity;
import homework.HomeworkActivity;

public class MainActivity extends AppCompatActivity {

    public final String WEMEET = "com.tencent.wemeet.app";
    public final String BILIBILI = "tv.danmaku.bili";
    public final String QQ = "com.tencent.mobileqq";
    public final String RIMET = "com.alibaba.android.rimet";
    public final String CHAOXING = "com.chaoxing.mobile";
    public final String ICOURSE = "https://www.icourse163.org/";
    public final String WECHAT = "com.tencent.mm";
    public final String EDU = "com.tencent.edu";
    public final String ICOURSE_APP = "com.netease.edu.ucmooc";
    public final String BILIONLINE = "https://www.bilibili.com/";

    private Button techButton;
    private Button movieButton;
    private Button noteButton;
    private Button homeworkButton;
    private FloatingActionButton courseButton;
    private Button helperButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        techButton = (Button)findViewById(R.id.tech_button);
        movieButton = (Button)findViewById(R.id.movie_button);
        noteButton = (Button)findViewById(R.id.note_button);
        homeworkButton = (Button)findViewById(R.id.homework_button);
        courseButton = (FloatingActionButton) findViewById(R.id.course_button);
        helperButton = (Button)findViewById(R.id.helper_button);
        handleClick();
    }

    private void handleClick(){
        techButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://bkjw.whu.edu.cn/stu/stu_index.jsp"));
                startActivity(Intent.createChooser(intent, "访问教务系统"));
            }
        });
        movieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MovieActivity.class);
                startActivity(intent);
            }
        });
        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
            }
        });
        registerForContextMenu(courseButton);
        homeworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeworkActivity.class);
                startActivity(intent);
            }
        });
        helperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, AttentiveActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.wemeet:
                openApp(WEMEET);
                break;
            case R.id.edu:
                openApp(EDU);
                break;
            case R.id.bilibili:
                openApp(BILIBILI);
                break;
            case R.id.bilionline:
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse(BILIONLINE));
                startActivity(Intent.createChooser(intent1, "打开b站"));
                break;
            case R.id.qq:
                openApp(QQ);
                break;
            case R.id.wechat:
                openApp(WECHAT);
                break;
            case R.id.rimet:
                openApp(RIMET);
                break;
            case R.id.chaoxing:
                openApp(CHAOXING);
                break;
            case R.id.icourse:
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(ICOURSE));
                startActivity(Intent.createChooser(intent2, "进入中国大学MOOC"));
                break;
            case R.id.icourse_app:
                openApp(ICOURSE_APP);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void openApp(String packageName){
        if (checkPackInfo(packageName)) {
            openPackage(this,packageName);
        } else {
            Toast.makeText(this, "该应用未安装", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPackInfo(String packagename) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    public static Intent getAppOpenIntentByPackageName(Context context,String packageName){
        String mainActivity = null;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainActivity = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainActivity)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainActivity));
        return intent;
    }

    public static Context getPackageContext(Context context, String packageName) {
        Context packageContext = null;
        if (context.getPackageName().equals(packageName)) {
            packageContext = context;
        } else {
            try {
                packageContext = context.createPackageContext(packageName,
                        Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return packageContext;
    }

    public static boolean openPackage(Context context, String packageName) {
        Context packageContext = getPackageContext(context, packageName);
        Intent intent = getAppOpenIntentByPackageName(context, packageName);
        if (packageContext != null && intent != null) {
            packageContext.startActivity(intent);
            return true;
        }
        return false;
    }
}
