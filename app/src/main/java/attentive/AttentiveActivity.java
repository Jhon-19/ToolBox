package attentive;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.toolbox.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;


/**
 * Unpublished feature.
 *
 * @author Zhou Jingsen
 * */
public class AttentiveActivity extends AppCompatActivity {
    DiagramView dv;
    TextView total;
    TextView lastWeek;
    Button button;
    Button story;
    Thread timingThread=null;
    ArrayList<AttentiveRecord> attentiveData;
    Integer oldData;
    public static final String dataFile="attentiveData";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attentive_activity);
        dv=findViewById(R.id.attentive_diagram);
        total=findViewById(R.id.attentive_time_total);
        lastWeek=findViewById(R.id.attentive_time_lastweek);
        button=findViewById(R.id.attentive_button);
        story=findViewById(R.id.attentive_story);
        readData();
        sortMap();
        digestData();
        handleStory();
        button.setText("开始专注\n\n今日已专注"+(getData(formatDate(new GregorianCalendar()))/60)+"min");
        dv.data=attentiveData;
        dv.drawData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<100;i++){
                    dv.invalidate();
                    try{
                        Thread.sleep(10);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTimingState();
            }
        });
    }
    public void toggleTimingState(){
        if(timingThread==null){
            button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            button.setTextColor(Color.WHITE);
            timingThread=new Thread(new Runnable() {
                long startMills;
                @Override
                public void run() {
                    startMills=System.currentTimeMillis();
                    while(true){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setText(formatTime((int)((System.currentTimeMillis()-startMills)/1000))+"\n\n正在专注 - 点击停止\n离开此界面将停止计时");
                            }
                        });
                        try{
                            Thread.sleep(1000);
                        }catch(InterruptedException ie){
                            ie.printStackTrace();
                            return;
                        }
                    }
                }
            });
            timingThread.start();
        }else{
            timingThread.interrupt();
            button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            button.setTextColor(Color.BLACK);
            timingThread=null;
            String time=button.getText().toString().split("\n")[0];
            AttentiveRecord ar=new AttentiveRecord();
            ar.date=formatDate(new GregorianCalendar());
            ar.attentiveSeconds=getData(ar.date)+deformatTime(time);
            attentiveData.set(indexOf(ar.date),ar);
            button.setText("开始专注\n\n今日已专注"+(getData(formatDate(new GregorianCalendar()))/60)+"min");
            dv.invalidate();
            digestData();
            handleStory();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timingThread!=null)toggleTimingState();
        saveData();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    public void saveData(){
        File f=new File(getFilesDir().getParent()+"/"+dataFile);
        try {
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(attentiveData);
            oos.writeObject(oldData);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            Log.e("AttentiveActivity","Error while writing data file",e);
        }
    }
    public void readData(){
        File f=new File(getFilesDir().getParent()+"/"+dataFile);
        if(!f.exists()){
            attentiveData=new ArrayList<>();
            GregorianCalendar gc=new GregorianCalendar();
            gc.roll(Calendar.DAY_OF_MONTH,-6);
            for(int i=0;i<7;i++){
                AttentiveRecord ar=new AttentiveRecord();
                ar.date=formatDate(gc);
                ar.attentiveSeconds=0;
                attentiveData.add(ar);
                gc.roll(Calendar.DAY_OF_MONTH,1);
            }
            oldData=0;
            return;
        }
        try {
            ObjectInputStream ois=new ObjectInputStream(new FileInputStream(f));
            attentiveData=(ArrayList<AttentiveRecord>) ois.readObject();
            oldData=(Integer)ois.readObject();
            ois.close();
        } catch (Exception e) {
            Log.e("AttentiveActivity","Error while writing data file",e);
        }
    }
    private void sortMap(){
        String oldestDate=attentiveData.iterator().next().date;
        GregorianCalendar now=homework.HomeworkAdapter.getStandardDay(new GregorianCalendar());
        GregorianCalendar old=homework.HomeworkAdapter.getStandardDay(new GregorianCalendar());
        old.set(Calendar.MONTH,Integer.parseInt(oldestDate.split("-")[0])-1);
        old.set(Calendar.DAY_OF_MONTH,Integer.parseInt(oldestDate.split("-")[1]));
        int days=(int)((now.getTimeInMillis()-old.getTimeInMillis())/(1000*3600*24));
        days-=6;
        for(int i=0;i<Math.min(days,7);i++){
            oldData+=getData(formatDate(old));
            attentiveData.remove(getRecord(formatDate(old)));
            AttentiveRecord ar=new AttentiveRecord();
            ar.attentiveSeconds=0;
            ar.date=formatDate(now);
            attentiveData.add(ar);
            now.roll(Calendar.DAY_OF_MONTH,-1);
            old.roll(Calendar.DAY_OF_MONTH,1);
        }
    }
    private int getData(String date){
        return getRecord(date).attentiveSeconds;
    }
    private int indexOf(String date){
        return attentiveData.indexOf(getRecord(date));
    }
    private AttentiveRecord getRecord(String date){
        Iterator<AttentiveRecord> it=attentiveData.iterator();
        AttentiveRecord temp;
        while(it.hasNext()){
            temp=it.next();
            if(temp.date.equalsIgnoreCase(date))return temp;
        }
        return null;
    }
    public String formatTime(int second){
        return String.format("%02d:%02d:%02d",second/3600,second/60,second%60);
    }
    public int deformatTime(String time){
        String[] temp=time.split(":");
        return Integer.parseInt(temp[0])*3600+Integer.parseInt(temp[1])*60+Integer.parseInt(temp[2]);
    }
    public String formatDate(GregorianCalendar gc){
        return String.format("%02d-%02d",gc.get(Calendar.MONTH)+1,gc.get(Calendar.DAY_OF_MONTH));
    }
    public void digestData(){
        Iterator<AttentiveRecord> keyIterator=attentiveData.iterator();
        int totalx=0;
        while(keyIterator.hasNext()){
            totalx+=keyIterator.next().attentiveSeconds;
        }
        lastWeek.setText(totalx/60+"min");
        totalx+=oldData;
        total.setText(totalx/60+"min");
    }
    /**
     * In development::
     * */
    public void handleStory(){
        String totalx=total.getText().toString();
        totalx=totalx.substring(0,totalx.length()-3);
        int time=Integer.parseInt(totalx);
        if(time<5)story.setText("锁定\n\n专注\n"+(5-time)+"min\n后解锁");
        else{
            story.setBackgroundColor(getResources().getColor(R.color.attentive_story_button));
            story.setText("已解锁\n\n???");
            story.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(AttentiveActivity.this,AttentiveStoryActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
