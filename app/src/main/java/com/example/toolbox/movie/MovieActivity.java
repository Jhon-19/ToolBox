package com.example.toolbox.movie;

import android.os.Bundle;

import com.example.toolbox.R;
import com.example.toolbox.movie.support.Movie;
import com.example.toolbox.movie.support.MovieAdapter;
import com.example.toolbox.movie.support.MovieNetUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
/**
 * Movie list Activity.
 *
 * @author Zhou Jingsen
 * */
public class MovieActivity extends AppCompatActivity {

    ArrayList<Movie> movieList=new ArrayList<>();
    MovieAdapter adapter;
    /**
     * The number of movies that are already in list.
     * This variable is used to make sure movie in list will not repeat.
     * */
    int loadedCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);
        initList();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MovieAdapter(movieList,getCacheDir());
        adapter.main=this;
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new LoadMoreListener(this));
    }

    private void initList() {
        loadMovies(8);
    }
    /**
     * Load movie from a particular website.
     * This function can be called multiple times, in order to load movies according to
     * user's need. Loaded movies will be automatically added to movie list after information
     * downloaded to cache folder. View updating is async, so this method does not block the
     * current thread.
     *
     * @param count The number of movies to load.
     * */
    void loadMovies(final int count){
        Log.d("Movie","Preparing to load "+count+" movies. "+loadedCount+" movies alread loaded.");
        loadedCount+=count;
        final MovieActivity _this=this;
        new Thread(new Runnable(){
            ArrayList<Movie> list;
            @Override
            public void run(){
                try{
                    Log.d("Movie","Requesting for movie list...");
                    list=MovieNetUtil.getMoviesNoDetail(loadedCount,count);
                    for(int i=0;i< list.size();i++) {
                        Log.d("Movie","Downloading poster ("+(i+1)+"/"+count+").");
                        list.get(i).downloadImageTo(new File(_this.getCacheDir().toString()+"/"+list.get(i).name+".jpg"));
                        runOnUiThread(new UpdateViewThread(list.get(i),adapter));
                    }
                }catch(Exception e){
                    Log.d("Movie","Internet Error");
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
/**
 * Used for update movie item view after loaded from Internet.
 * Only used in this file.
 *
 * @author Zhou Jingsen
 * */
class UpdateViewThread implements Runnable{
    private Movie m;
    private MovieAdapter adapter;
    UpdateViewThread(Movie m,MovieAdapter adapter){
        this.m=m;
        this.adapter=adapter;
    }
    @Override
    public void run(){
        adapter.addItem(m);
    }
}
/**
 * Used for monitoring if it's necessary to load more movies in list.
 * Only used in this file.
 *
 * @author Zhou Jingsen
 * */
class LoadMoreListener extends RecyclerView.OnScrollListener{
    private MovieActivity m;
    LoadMoreListener(MovieActivity m){
        this.m=m;
    }
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
        int totalItemCount = recyclerView.getAdapter().getItemCount();
        int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
        int visibleItemCount = recyclerView.getChildCount();

        if (newState == RecyclerView.SCROLL_STATE_IDLE
                && lastVisibleItemPosition == totalItemCount - 1
                && visibleItemCount > 0) {
            Log.d("Movie","Loading more...");
            m.loadMovies(8);
        }
    }
}