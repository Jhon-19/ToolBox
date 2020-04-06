package com.zjs.development;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zjs.development.movie.HomeworkAdapter;
/**
 * Homework list Activity.
 *
 * @author Zhou Jingsen
 * */
public class HomeworkActivity extends AppCompatActivity {
    HomeworkAdapter adapter;

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

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        adapter.saveList();
    }

}