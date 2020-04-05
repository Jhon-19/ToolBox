package com.example.toolbox.movie.support;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 * Contains the information of a movie.
 * Including the URL of poster image, movie name, description, basic information and
 * ranking on a particular movie site.
 *
 * @author Zhou Jingsen
 * */
public class Movie {
    //The URL of poster image.
    public URL image;
    //The path to the local storage of poster image.
    public File imageStorage;
    public String name;
    public String info;
    public String description;
    public float rank;
    public String detailPage;
    /**
     * Download the poster image to the specified path.
     *
     * @param path The specified path. Will be saved in variable 'imageStorage'.
     * */
    public void downloadImageTo(File path) throws IOException{
        HttpURLConnection con=(HttpURLConnection)image.openConnection();
        BufferedInputStream read=new BufferedInputStream(con.getInputStream());
        FileOutputStream write=new FileOutputStream(path);
        byte[] buf=new byte[128];
        int size=0;

        while((size=read.read(buf))!=-1)
            write.write(buf, 0, size);

        write.flush();
        write.close();
        read.close();

        imageStorage=path;
    }
    @Override
    @NonNull
    public String toString() {
        return name+":"+description;
    }
}
