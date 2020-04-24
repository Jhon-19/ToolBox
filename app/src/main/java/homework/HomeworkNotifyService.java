package homework;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.toolbox.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import static homework.HomeworkAdapter.getStandardDay;

public class HomeworkNotifyService extends Service {
    public static final String lastRunRecord="notifyServiceLastRun";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("HomeworkNotifyService","onBind() invoked.");
        return new HWNSKeepalive.Stub() {
            @Override
            public String getServiceName() {
                return "HomeworkNotifyService";
            }
        };
    }
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel nc=new NotificationChannel("homework","HomeworkNotification", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager nm=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(nc);
        nc=new NotificationChannel("foreground","ForegroundServiceNotification", NotificationManager.IMPORTANCE_MIN);
        nm.createNotificationChannel(nc);
        Log.d("HomeworkNotifyService","Service started.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Intent intentx = new Intent(this,HomeworkActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,1,intentx,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder nb = new Notification.Builder(this,"foreground")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("e学友")
                .setContentText("作业提醒服务正在运行")
                .setAutoCancel(true)
                .setContentIntent(pi);

        startForeground(2,nb.build());
        Log.d("HomeworkNotifyService","Service restarted.");
        executeTask();
        scheduleNextRun();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager nm=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.deleteNotificationChannel("homework");
        nm.deleteNotificationChannel("foreground");
        Log.d("HomeworkNotifyService","Service stopped.");
    }
    /**
     * Use AlarmManager to schedule the approximate run time of next evaluation.
     * */
    private void scheduleNextRun(){
        AlarmManager am=(AlarmManager)this.getSystemService(Service.ALARM_SERVICE);
        Intent intent=new Intent(this,HomeworkNotifyService.class);
        PendingIntent pi=PendingIntent.getService(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        GregorianCalendar gc=new GregorianCalendar();
        timePeriod now=getTimePeriod(gc);
        gc=HomeworkAdapter.getStandardDay(gc);
        switch(now){
            case beforeMorning:
                gc.set(Calendar.HOUR_OF_DAY,8);
                break;
            case morning:
            case beforeEvening:
                gc.set(Calendar.HOUR_OF_DAY,18);
                break;
            case evening:
            case afterEvening:
                gc.set(Calendar.HOUR_OF_DAY,8);
                gc.roll(Calendar.DAY_OF_MONTH,1);
                break;
        }
        gc.set(Calendar.MINUTE,10);
        long startMills=gc.getTimeInMillis();
        am.setWindow(AlarmManager.RTC_WAKEUP,startMills,1000*60*40,pi);
    }
    /**
     * Execute the task of checking homework whose deadline is near.
     * */
    @SuppressWarnings("unchecked")
    private void executeTask(){
        if(!judgeTaskNecessity()){
            Log.d("HomeworkNotifyService","Task isn't needed to be run. Exiting.");
            return;
        }

        Log.d("HomeworkNotifyService","Updating last run record.");
        File f=getFilesDir();
        f=new File(f.getParent()+"/"+lastRunRecord);
        try {
            if(f.exists()) {
                Log.d("HomeworkNotifyService","Deleting last run record.");
                if (!f.delete()) Log.e("HomeworkNotifyService", "Error while deleting state file.");
            }
            Log.d("HomeworkNotifyService","Creating new last run record.");
            if(!f.createNewFile())Log.e("HomeworkNotifyService","Error while creating state file.");
        } catch (IOException e) {
            Log.e("HomeworkNotifyService","Error while creating state file.",e);
        }

        File listData=new File(getFilesDir().getParent()+"/"+HomeworkAdapter.DATA_FILE_NAME);
        ArrayList<Homework> homeworkList;
        if(!listData.exists()){
            Log.d("HomeworkNotifyService","No homework data found. Exiting.");
            return;
        }

        try{
            Log.d("HomeworkNotifyService","Executing task...");
            ObjectInputStream read=new ObjectInputStream(new FileInputStream(listData));
            homeworkList=(ArrayList<Homework>)read.readObject();
            GregorianCalendar now=getStandardDay(new GregorianCalendar());
            Iterator<Homework> iterator=homeworkList.iterator();
            int passed=0,today=0,tomorrow=0;
            Homework temp;
            while(iterator.hasNext()){
                temp=iterator.next();
                if(temp.completed)continue;
                long distance=(temp.deadline.getTimeInMillis()-now.getTimeInMillis())/(24*60*60*1000);
                if(distance==1)tomorrow++;
                else if(distance==0)today++;
                else if(distance<=-1)passed++;
            }
            if(passed+today+tomorrow<=0)return;
            if(getTimePeriod(new GregorianCalendar())==timePeriod.morning){
                String detail="";
                if(passed>0)detail+="有"+passed+"项作业已经过了截止日期！\n";
                if(today>0)detail+="有"+today+"项作业截止到今天。\n";
                detail+="开始一天的奋斗吧~~~";
                sendNotification("有临近截止日期的作业","你有"+(passed+today+tomorrow)+"项作业临近截止日期。",
                        detail);
            }else{
                String detail="";
                if(passed>0)detail+="有"+passed+"项作业已经过了截止日期！\n";
                if(today>0)detail+="有"+today+"项作业截止到今天！\n";
                if(tomorrow>0)detail+="有"+tomorrow+"项作业截止到明天。\n";
                detail+="继续加油哦，坚持就是胜利~";
                sendNotification("有临近截止日期的作业","你有"+(passed+today+tomorrow)+"项作业临近截止日期。",
                        detail);
            }
        }catch(IOException|ClassNotFoundException e){
            Log.e("Homework","Error in reading list data file.",e);
        }
    }
    /**
     * Send Homework Notification in BigText Style in Notification Channel 'homework'.
     * */
    public void sendNotification(String title,String summary,String detail) {
        Intent intent = new Intent(this,HomeworkActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle()
                .setBigContentTitle(title)
                .setSummaryText(summary)
                .bigText(detail);

        NotificationManager nm=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder nb = new Notification.Builder(this,"default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(this.getString(R.string.homework_notify_title))
                .setContentText(this.getString(R.string.homework_notify_desc))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setStyle(bigTextStyle);

        nm.notify(1,nb.build());
    }
    /**
     * Return whether task need to run now.
     * This method guarantee that task will not run twice in the same scheduled time period.
     * If task failed to run at last scheduled time (service killed), task will run now.
     * */
    private boolean judgeTaskNecessity(){
        File f=getFilesDir();
        f=new File(f.getParent()+"/"+lastRunRecord);
        if(!f.exists())return isTaskScheduledNow();//No last run record. So run task only if it's the scheduled time.

        GregorianCalendar lastRun=new GregorianCalendar();
        lastRun.setTimeInMillis(f.lastModified());
        lastRun= getStandardDay(lastRun);
        GregorianCalendar now= getStandardDay(new GregorianCalendar());
        int days=(int)((now.getTimeInMillis()-lastRun.getTimeInMillis())/(1000*60*60*24));

        if(days>1)return true;//More than 1 day passed since last run. So run task immediately.
        lastRun.setTimeInMillis(f.lastModified());
        now=new GregorianCalendar();
        if(days==1)return !matchTimePeriod(lastRun, "00011") || isTaskScheduledNow();//If task ran after last evening, run task only if it's the scheduled time.

        //days==0 : Task has run today.
        if(matchTimePeriod(now,"01100")&&matchTimePeriod(lastRun,"10000"))return true;
        return matchTimePeriod(now, "00011") && matchTimePeriod(lastRun, "11100");
    }
    /**
     * Return whether the checking homework task is scheduled to run now.
     * The task is scheduled to run from 8 a.m. to 9 a.m. and from 18 a.m. to 19 a.m.
     * This method is not meant to be called directly, if you want to check if task needs to be
     * run now, please invoke judgeTaskNecessity(). Because this method will not guarantee
     * this task has already run in this time period.
     * */
    private boolean isTaskScheduledNow(){
        GregorianCalendar gc=new GregorianCalendar();
        timePeriod time=getTimePeriod(gc);
        return time==timePeriod.morning||time==timePeriod.evening;
    }
    /**
     * Return whether the calendar match the given time period.
     *
     * @param exp A string representing the time periods you desired to match the calendar.
     *            Format: contains 5 characters, coordinating the 5 time periods.
     *            Character '1' stands for you want to match the calendar with the coordinating
     *            time period.
     * */
    private boolean matchTimePeriod(GregorianCalendar gc,String exp){
        boolean result=false;
        timePeriod tgt=getTimePeriod(gc);
        result=result||(exp.charAt(0)=='1'&&tgt==timePeriod.beforeMorning);
        result=result||(exp.charAt(1)=='1'&&tgt==timePeriod.morning);
        result=result||(exp.charAt(2)=='1'&&tgt==timePeriod.beforeEvening);
        result=result||(exp.charAt(3)=='1'&&tgt==timePeriod.evening);
        result=result||(exp.charAt(4)=='1'&&tgt==timePeriod.afterEvening);
        return result;
    }
    /**
     * Return the time period of the given calendar.
     * */
    private timePeriod getTimePeriod(GregorianCalendar gc){
        int hour=gc.get(Calendar.HOUR_OF_DAY);
        if(hour==8)return timePeriod.morning;
        else if(hour>=9&&hour<=17)return timePeriod.beforeEvening;
        else if(hour==18)return timePeriod.evening;
        else if(hour>=19)return timePeriod.afterEvening;
        else return timePeriod.beforeMorning;
    }
    public enum timePeriod{
        beforeMorning,morning,beforeEvening,evening,afterEvening
    }
}
