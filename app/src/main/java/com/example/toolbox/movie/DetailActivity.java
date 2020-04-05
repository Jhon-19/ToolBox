package com.example.toolbox.movie;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.toolbox.R;
import com.example.toolbox.movie.support.Movie;
import com.example.toolbox.movie.support.MovieNetUtil;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Movie detail page.
 *
 * @author Zhou Jingsen
 * */
public class DetailActivity extends AppCompatActivity {
    ImageView poster;
    TextView info;
    TextView basic;
    public static Movie m;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);
        poster=findViewById(R.id.poster_detail);
        info=findViewById(R.id.info);
        basic=findViewById(R.id.basic);
        poster.setImageURI(Uri.parse(m.imageStorage.toString()));
        info.setText("加载中...");
        basic.setText(m.name+"\r\n评分:"+m.rank);
        final DetailActivity _this=this;
        new Thread(new Runnable(){
            @Override
            public void run(){
                Movie detail;
                try {
                    detail= MovieNetUtil.parseDetailPage(m.detailPage);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                detail.image=m.image;
                detail.imageStorage=m.imageStorage;
                detail.name=m.name;
                detail.rank=m.rank;
                runOnUiThread(new ViewUpdateThread(detail,_this));
            }
        }).start();
    }
}

/**
 * Used for update text view in detail page.
 * Only used in this file
 *
 * @author Zhou Jingsen
 * */
class ViewUpdateThread implements Runnable{
    Movie m;
    DetailActivity target;
    public ViewUpdateThread(Movie m, DetailActivity target){
        this.m=m;
        this.target=target;
    }
    @Override
    public void run(){
        target.info.setText(m.info+"\r\n\r\n剧情简介:"+m.description);
    }
}