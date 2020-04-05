package com.example.toolbox.movie.support;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Zhou Jingsen
 * Dependencies: HTTPComponents(HTTPClient), JSON-java.
 * */
public class MovieNetUtil {
    private static final String CHROME_AGENT="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36";
    private static final String INDEX_REQUEST_URL="https://movie.douban.com/j/search_subjects?type=movie&tag=%E7%83%AD%E9%97%A8";
    private static final String PAGE_OFFSET="page_start";
    private static final String PAGE_MAX="page_limit";
    /**
     * Get the recommend movie list through movie.douban.com.
     * It's not recommended to fetch lots of movies at once,
     * or this method will be blocking for a long time.
     *
     * @param offset Starting from the given movie number.
     * @param len The numbers of movie that will be in the returned list.
     * */
    public static ArrayList<Movie> getMovies(int offset,int len) throws IOException, JSONException {
        URL request=new URL(INDEX_REQUEST_URL+"&"+PAGE_OFFSET+"="+offset+"&"+PAGE_MAX+"="+(offset+len));
        HttpURLConnection connection=(HttpURLConnection)request.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent",CHROME_AGENT);
        if(connection.getResponseCode()!=200)
            throw new IOException("Failed to connect to target website:"+connection.getResponseCode());
        String responseJson= inputStreamToString(connection.getInputStream(),"UTF-8");
        JSONObject obj=new JSONObject(responseJson);
        JSONArray list=obj.getJSONArray("subjects");
        ArrayList<Movie> result=new ArrayList<>();

        Movie temp;
        JSONObject tobject;
        System.out.println(list.length());
        for(int i=0;i<len;i++) {
            tobject=list.getJSONObject(i);
            //The '/' symbols in the response JSON is represented as '\/', so it's necessary to replace back before use.
            temp=parseDetailPage(tobject.getString("url"));
            temp.detailPage=tobject.getString("url");
            temp.image=new URL(tobject.getString("cover"));
            temp.rank=(float)tobject.getDouble("rate");
            //The '/' in movie name will disturb poster file saving.
            temp.name=tobject.getString("title").replace("/","丨");
            result.add(temp);
        }

        return result;
    }
    public static ArrayList<Movie> getMoviesNoDetail(int offset,int len) throws IOException, JSONException {
        URL request = new URL(INDEX_REQUEST_URL + "&" + PAGE_OFFSET + "=" + offset + "&" + PAGE_MAX + "=" + (offset + len));
        HttpURLConnection connection = (HttpURLConnection) request.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", CHROME_AGENT);
        if (connection.getResponseCode() != 200)
            throw new IOException("Failed to connect to target website:" + connection.getResponseCode());
        String responseJson = inputStreamToString(connection.getInputStream(), "UTF-8");
        JSONObject obj = new JSONObject(responseJson);
        JSONArray list = obj.getJSONArray("subjects");
        ArrayList<Movie> result = new ArrayList<>();

        Movie temp;
        JSONObject tobject;
        System.out.println(list.length());
        for (int i = 0; i < len; i++) {
            tobject = list.getJSONObject(i);
            //The '/' symbols in the response JSON is represented as '\/', so it's necessary to replace back before use.
            temp=new Movie();
            temp.detailPage=tobject.getString("url");
            temp.image = new URL(tobject.getString("cover"));
            temp.rank = (float) tobject.getDouble("rate");
            temp.name = tobject.getString("title").replace("/","丨");
            result.add(temp);
        }
        return result;
    }
    /**
     * Parse the detail page of a movie.
     * In order to get the information that is inexistent in response JSON.
     *
     * @return A Movie object containing only description and info.
     * */
    public static Movie parseDetailPage(String url)throws IOException{
        Log.d("Movie","Parsing:"+url);
        URL request=new URL(url);
        Movie result=new Movie();
        HttpURLConnection connection=(HttpURLConnection)request.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent",CHROME_AGENT);
        if(connection.getResponseCode()!=200)
            throw new IOException("Failed to connect to target website:"+connection.getResponseCode());
        String html= inputStreamToString(connection.getInputStream(),"UTF-8");
        //Process HTML content to get desired information.
        String infoSection=html.split("<div id=\"info\">")[1]
                .split("<div id=\"interest_sectl\">")[0]
                .trim();
        String processedInfo=infoSection.replace(" ", "")
                .replaceAll("<.*?>","")
                .trim();
        String depictSection=html.split("<div class=\"related-info\" style=\"margin-bottom:-10px;\">")[1]
                .split("<div id=\"dale_movie_subject_banner_after_intro\">")[0]
                .trim();
        String processedDepict=depictSection
                //Sometimes '©豆瓣' will appear in this section, make sure it is deleted.
                .replace("&copy;豆瓣", "")
                //Replace Chinese space with English space, since \\s cannot recognize Chinese space.
                .replace((char)12288, ' ')
                .replaceAll("\\s","")
                .replaceAll("<.*?>", "")
                //Remove depict header: xxx简介
                .split("······",2)[1]
                .trim();

        result.info=processedInfo;
        result.description=processedDepict;

        return result;
    }
    private static String inputStreamToString(InputStream is,String encoding) throws IOException {
        InputStreamReader reader=new InputStreamReader(is,encoding);
        StringBuilder builder=new StringBuilder();
        char[] cbuf=new char[128];
        int size=0;
        while((size=reader.read(cbuf))!=-1)
            builder.append(cbuf,0,size);
        return builder.toString();
    }
}
