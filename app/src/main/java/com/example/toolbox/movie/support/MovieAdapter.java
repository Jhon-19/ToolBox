package com.example.toolbox.movie.support;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.toolbox.R;
import com.example.toolbox.movie.DetailActivity;
import com.example.toolbox.movie.MovieActivity;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The implementation of RecyclerView.Adapter.
 * Used for displaying movie item in list.
 *
 * @author Zhou Jingsen
 * */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    public ArrayList<Movie> movieList=new ArrayList<>();
    public File cacheDir=null;
    public MovieActivity main;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView name;
        LinearLayout frame;
        ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.movie_poster);
            name = view.findViewById(R.id.movie_name);
            frame= view.findViewById(R.id.movie_item_frame);
        }

    }

    public MovieAdapter(ArrayList<Movie> movieList, File cacheDir) {
        this.movieList = movieList;
        this.cacheDir=cacheDir;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position<movieList.size()) {
            final Movie m = movieList.get(position);
            holder.poster.setImageURI(Uri.parse(m.imageStorage.toString()));
            holder.name.setText(m.name);

            holder.frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent switcher=new Intent(main,DetailActivity.class);
                    DetailActivity.m=m;
                    main.startActivity(switcher);
                }
            });
        }else{
            holder.name.setText("努力加载中...");
            holder.poster.setImageResource(R.drawable.white);
        }
    }

    @Override
    public int getItemCount() {
        return movieList.size()+1;
    }
    /**
     * Append a new movie item to the end of this list.
     *
     * @param m The movie to be added.
     *          Should contain movie name and poster image that is downloaded to cache folder
     *          at least.
     * */
    public void addItem(Movie m){
        movieList.add(m);
        notifyItemInserted(movieList.size()-2);
    }

}