package homework;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import static homework.HomeworkAdapter.getStandardDay;
/**
 *
 * @author Zhou Jingsen
 * */
public class HomeworkNotifyService extends Service {
    public static final String lastRunRecord="notifyServiceLastRun";
    private static final String TAG="HomeworkNotifyService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind() invoked.");
        return new HWNSKeepalive.Stub() {
            @Override
            public String getServiceName() {
                return TAG;
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
        Log.d(TAG,"Service started.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Intent intentx = new Intent(this,HomeworkActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,1,intentx,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder nb = new Notification.Builder(this,"foreground")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("作业提醒服务")
                .setContentText(generateHomeworkMessage(matchTimePeriod(new GregorianCalendar(),"00011")))
                .setContentIntent(pi)
                .setShowWhen(false);

        startForeground(2,nb.build());
        Log.d(TAG,"Service restarted.");
        executeTask();
        scheduleNextRun();
        if(HomeworkActivity.calendarPermissionGranted)addCalendarNotifications();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager nm=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.deleteNotificationChannel("homework");
        nm.deleteNotificationChannel("foreground");
        Log.d(TAG,"Service stopped.");
    }
    /**
     * Use AlarmManager to schedule the approximate run time of next evaluation.
     * */
    private void scheduleNextRun(){
        AlarmManager am=(AlarmManager)this.getSystemService(Service.ALARM_SERVICE);
        Intent intent=new Intent(this,HomeworkNotifyService.class);
        PendingIntent pi=PendingIntent.getService(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        timePeriod now=getTimePeriod();
        GregorianCalendar gc=HomeworkAdapter.getStandardDay(new GregorianCalendar());
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
        gc.set(Calendar.MINUTE,0);
        long startMills=gc.getTimeInMillis();
        am.setWindow(AlarmManager.RTC_WAKEUP,startMills,1000*60*40,pi);
        Log.d(TAG,"Scheduled next run at "+gc.toString());
    }
    /**
     * Add notifications to calendar in case notify service is stopped.
     * */
    private void addCalendarNotifications(){
        int[] homework=getHomeworkCount();
        if(homework==null||(homework[0]+homework[1]+homework[2]<=0)){
            stopSelf();
            return;
        }
        CalendarUtils.deleteCalendarEvent(this,"有作业已经过了截止日期");
        CalendarUtils.deleteCalendarEvent(this,"已经过了截止日期的作业还未完成");
        CalendarUtils.deleteCalendarEvent(this,"有作业截止到今天");
        CalendarUtils.deleteCalendarEvent(this,"有截止到今天的作业未完成");
        CalendarUtils.deleteCalendarEvent(this,"有作业截止到明天");
        CalendarUtils.deleteCalendarEvent(this,"有作业截止到明天");
        GregorianCalendar morning=getStandardDay(new GregorianCalendar());
        GregorianCalendar evening=getStandardDay(new GregorianCalendar());
        morning.set(Calendar.HOUR_OF_DAY,8);
        evening.set(Calendar.HOUR_OF_DAY,18);
        long m=morning.getTimeInMillis();
        long e=evening.getTimeInMillis();
        if(homework[0]>0) {
            CalendarUtils.addCalendarEvent(this,"有作业已经过了截止日期", homework[0] + "项作业已经过了截止日期。\n打开\"e学友\"查看详情。\n赶快抓住最后机会弥补一下。",m);
            CalendarUtils.addCalendarEvent(this,"已经过了截止日期的作业还未完成",homework[0]+"项作业已经过了截止日期但仍未完成。\n打开\"e学友\"查看详情。\n再拖时间就没机会了，加油！",e);
        }else if(homework[1]>0) {
            CalendarUtils.addCalendarEvent(this,"有作业截止到今天", homework[1] + "项作业截止到今天。\n打开\"e学友\"查看详情。\n开始一天的奋斗吧~",m);
            CalendarUtils.addCalendarEvent(this,"有截止到今天的作业未完成",homework[1]+"项作业截止到今天但仍未完成。\n打开\"e学友\"查看详情。\n不拖时间才能被老师认可，加油！",e);
        }else {
            CalendarUtils.addCalendarEvent(this,"有作业截止到明天", homework[2] + "项作业截止到明天。\n打开\"e学友\"查看详情。\n今天的作业已经完成了呢，那么把明天的作业也写完怎么样?",m);
            CalendarUtils.addCalendarEvent(this,"有作业截止到明天",homework[2]+"项作业截止到明天。\n打开\"e学友\"查看详情。\n要为明天的自己着想呀，再多写一些吧~",e);
        }
    }
    /**
     * Execute the task of checking homework whose deadline is near.
     * */
    private void executeTask(){
        if(!judgeTaskNecessity()){
            Log.d(TAG,"Task isn't needed to be run. Exiting.");
            return;
        }
        Log.d(TAG,"Updating last run record.");
        File f=getFilesDir();
        f=new File(f.getParent()+"/"+lastRunRecord);
        try {
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(new GregorianCalendar());
        } catch (IOException e) {
            Log.e(TAG,"Error while operating with state file.",e);
        }
        int[] homework=getHomeworkCount();
        if(homework==null||(homework[0]+homework[1]+homework[2]<=0)){
            stopSelf();
            return;
        }
        if(matchTimePeriod("11000")){
            if(homework[0]>0)sendNotification("有作业已经过了截止日期",homework[0]+"项作业已经过了截止日期。","赶快抓住最后机会弥补一下。");
            else if(homework[1]>0)sendNotification("有作业截止到今天",homework[1]+"项作业截止到今天。","开始一天的奋斗吧~");
            else sendNotification("有作业截止到明天",homework[2]+"项作业截止到明天","今天的作业已经完成了呢，那么把明天的作业也写完怎么样?");
        }else{
            if(homework[0]>0)sendNotification("已经过了截止日期的作业还未完成",homework[0]+"项作业已经过了截止日期但仍未完成","再拖时间就没机会了，加油！");
            else if(homework[1]>0)sendNotification("有截止到今天的作业未完成",homework[1]+"项作业截止到今天但仍未完成","不拖时间才能被老师认可，加油！");
            else if(homework[2]>0)sendNotification("有作业截止到明天",homework[2]+"项作业截止到明天","要为明天的自己着想呀，再多写一些吧~");
        }
    }
    /**
     * Return null if there's not homework whose deadline is near.
     *
     * @param tomorrowIncluded if tomorrow's homework included.
     * */
    private String generateHomeworkMessage(boolean tomorrowIncluded){
        int[] information=getHomeworkCount();
        if(information==null)return null;
        int passed=information[0];
        int today=information[1];
        int tomorrow=information[2];
        String detail="";
        if (passed + today + tomorrow <= 0) return null;
        if (passed > 0) detail += passed + "项已过截止日期 ";
        if (today > 0) detail += today + "项今天截止 ";
        if (tomorrow > 0 &&tomorrowIncluded) detail += tomorrow + "明天截止";
        return detail;
    }
    /**
     * Returns an array contains : [passed, today, tomorrow] in order.
     * */
    @SuppressWarnings("unchecked")
    private int[] getHomeworkCount(){
        File listData=new File(getFilesDir().getParent()+"/"+HomeworkAdapter.DATA_FILE_NAME);
        ArrayList<Homework> homeworkList;
        if(!listData.exists())return null;
        try {
            ObjectInputStream read = new ObjectInputStream(new FileInputStream(listData));
            homeworkList = (ArrayList<Homework>) read.readObject();
            GregorianCalendar now = getStandardDay(new GregorianCalendar());
            Iterator<Homework> iterator = homeworkList.iterator();
            int passed = 0, today = 0, tomorrow = 0;
            Homework temp;
            while (iterator.hasNext()) {
                temp = iterator.next();
                if (temp.completed) continue;
                long distance = (temp.deadline.getTimeInMillis() - now.getTimeInMillis()) / (24 * 60 * 60 * 1000);
                if (distance == 1) tomorrow++;
                else if (distance == 0) today++;
                else if (distance <= -1) passed++;
            }
            return new int[]{passed,today,tomorrow};
        }catch(Exception e){
            Log.e(TAG,"Error while processing homework list.",e);
        }
        return null;
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

        GregorianCalendar lastRun;
        try{
            ObjectInputStream ois=new ObjectInputStream(new FileInputStream(f));
            lastRun=(GregorianCalendar)ois.readObject();
        }catch(IOException | ClassNotFoundException e){
            return isTaskScheduledNow();
        }
        GregorianCalendar now= getStandardDay(new GregorianCalendar());
        int days=(int)((now.getTimeInMillis()-getStandardDay(lastRun).getTimeInMillis())/(1000*60*60*24));

        if(days>1)return true;//More than 1 day passed since last run. So run task immediately.
        if(days==1)return !matchTimePeriod(lastRun, "00011") || isTaskScheduledNow();//If task ran after last evening, run task only if it's the scheduled time.

        //days==0 : Task has run today.
        if(matchTimePeriod("01100")&&matchTimePeriod(lastRun,"10000"))return true;
        return matchTimePeriod("00011") && matchTimePeriod(lastRun, "11100");
    }
    /**
     * Return whether the checking homework task is scheduled to run now.
     * The task is scheduled to run from 8 a.m. to 9 a.m. and from 18 a.m. to 19 a.m.
     * This method is not meant to be called directly, if you want to check if task needs to be
     * run now, please invoke judgeTaskNecessity(). Because this method will not guarantee
     * this task has already run in this time period.
     * */
    private boolean isTaskScheduledNow(){
        timePeriod time=getTimePeriod();
        return time==timePeriod.morning||time==timePeriod.evening;
    }
    /**
     * A simplified form for judging the time period now.
     * */
    private boolean matchTimePeriod(String exp){
        return matchTimePeriod(new GregorianCalendar(),exp);
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
        timePeriod tgt=getTimePeriod(gc);
        boolean result=(exp.charAt(0)=='1'&&tgt==timePeriod.beforeMorning);
        result=result||(exp.charAt(1)=='1'&&tgt==timePeriod.morning);
        result=result||(exp.charAt(2)=='1'&&tgt==timePeriod.beforeEvening);
        result=result||(exp.charAt(3)=='1'&&tgt==timePeriod.evening);
        return result||(exp.charAt(4)=='1'&&tgt==timePeriod.afterEvening);
    }
    /**
     * A simplified form for get the time period of now.
     * */
    private timePeriod getTimePeriod(){
        return getTimePeriod(new GregorianCalendar());
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
