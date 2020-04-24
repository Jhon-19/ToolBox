package com.example.toolbox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class HelperActivity extends AppCompatActivity {

    private ColumnChartView timeChart;
    private ColumnChartData timeDatas;
    private List<AxisValue> axisXValues;
    private List<AxisValue> axisYValues;
    private Button timeStart;
    private Button timeStop;
    private Calendar startDate;
    private Calendar stopDate;
    private int startMinute;
    private int startHour;
    private int stopMinute;
    private int stopHour;
    private List<SubcolumnValue> values;
    private List<Column> columnList;
    private boolean isExist;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        timeChart = (ColumnChartView)findViewById(R.id.time_chart);
        timeStart = (Button)findViewById(R.id.time_start);
        timeStop = (Button)findViewById(R.id.time_stop);
        initialize();
        handleClick();
    }

    private void handleClick(){
        timeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDate = Calendar.getInstance();
                Toast.makeText(HelperActivity.this, "专注模式，请勿离开此页面", Toast.LENGTH_LONG).show();
                startHour = startDate.get(Calendar.HOUR_OF_DAY);
                startMinute = startDate.get(Calendar.MINUTE);
                getRestore();
            }
        });
        timeStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDate = Calendar.getInstance();
                Toast.makeText(HelperActivity.this, "已结束专注模式", Toast.LENGTH_LONG).show();
                stopHour = stopDate.get(Calendar.HOUR_OF_DAY);
                stopMinute = stopDate.get(Calendar.MINUTE);
                handleTime();
            }
        });
    }

    private void getRestore(){
        SharedPreferences preferences = getSharedPreferences("state", MODE_PRIVATE);
        isExist = preferences.getBoolean("isExist", false);
        if(isExist){
            List<ConcernedTime> concernedTimes = LitePal.findAll(ConcernedTime.class);
            ConcernedTime time = concernedTimes.get(0);
            Calendar currentTime = Calendar.getInstance();
            if(time.getMonth() == currentTime.get(Calendar.MONTH) &&
                    time.getDay() == currentTime.get(Calendar.DAY_OF_MONTH)){
                startHour = time.getStartHour();
                stopHour = time.getStopHour();
                String[] minuteArray = time.getMinutes().split(",");
                int[] minutes = new int[minuteArray.length];
                for(int i = 0; i < minuteArray.length; i++){
                    minutes[i] = Integer.parseInt(minuteArray[i]);
                }
                for(int i = startHour, index = 0; i <= stopHour; i++, index++){
                    columnList.remove(i);
                    columnList.add(i, createValues(minutes[index]));
                }
                timeDatas.setColumns(columnList);
                timeChart.setColumnChartData(timeDatas);
            }else{
                LitePal.deleteAll(ConcernedTime.class);
                isExist = false;
            }
        }
    }

    @Override
    protected void onStop() {
        Toast.makeText(this, "离开专注模式，计时清空", Toast.LENGTH_SHORT).show();
        stopDate = Calendar.getInstance();
        stopHour = stopDate.get(Calendar.HOUR_OF_DAY);
        stopMinute = stopDate.get(Calendar.MINUTE);
        handleTime();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startDate = Calendar.getInstance();
        startHour = startDate.get(Calendar.HOUR_OF_DAY);
        startMinute = startDate.get(Calendar.MINUTE);
    }

    @Override
    protected void onPause() {
        Toast.makeText(this, "离开专注模式，计时清空", Toast.LENGTH_SHORT).show();
        stopDate = Calendar.getInstance();
        stopHour = stopDate.get(Calendar.HOUR_OF_DAY);
        stopMinute = stopDate.get(Calendar.MINUTE);
        handleTime();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDate = Calendar.getInstance();
        startHour = startDate.get(Calendar.HOUR_OF_DAY);
        startMinute = startDate.get(Calendar.MINUTE);
    }

    private void handleTime(){
        ConcernedTime time = new ConcernedTime();
        int[] concernedMinutes = new int[stopHour-startHour+1];
        for(int i = startHour, index = 0; i <= stopHour; i++, index++){
            columnList.remove(i);
            if(stopHour > startHour) {
                if(i == startHour){
                    concernedMinutes[index] = 60-startMinute;
                    columnList.add(i, createValues(60-startMinute));
                }else if(i == stopHour){
                    concernedMinutes[index] = stopMinute;
                    columnList.add(i, createValues(stopMinute));
                }
            }else{
                concernedMinutes[index] = stopMinute-startMinute;
                columnList.add(i, createValues(stopMinute-startMinute));
            }
        }
        timeDatas.setColumns(columnList);
        timeChart.setColumnChartData(timeDatas);
        time.setDay(stopDate.get(Calendar.DAY_OF_MONTH));
        time.setMonth(stopDate.get(Calendar.MONTH));
        time.setStartHour(startHour);
        time.setStopHour(stopHour);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < concernedMinutes.length; i++){
            builder.append(concernedMinutes[i]);
            if(i != concernedMinutes.length-1){
                builder.append(",");
            }
        }
        time.setMinutes(builder.toString());
        if(!isExist){
            time.save();
        }else{
            time.update(1);
        }
        editor.putBoolean("isExist", true);
        editor.apply();
    }

    private Column createValues(int value){
        values = new ArrayList<>();
        values.add(new SubcolumnValue(value, ChartUtils.pickColor()));
        Column column = new Column(values);
        column.setHasLabelsOnlyForSelected(true);
        return column;
    }

    private void initialize(){
        int columns = 24;
        columnList = new ArrayList<>();
        for(int i = 0; i < columns; i++){
            columnList.add(createValues(0));
        }
        timeDatas = new ColumnChartData(columnList);

        axisXValues = new ArrayList<>();
        axisYValues = new ArrayList<>();
        for(int i = 0; i < 13; i++){
            axisYValues.add(new AxisValue(i*5));
        }
        for(int i = 0; i < 6; i++){
            axisXValues.add(new AxisValue(i*4));
        }
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setTextColor(ChartUtils.COLOR_BLUE);
        axisY.setTextColor(ChartUtils.COLOR_BLUE);
        axisX.setValues(axisXValues);
        axisY.setValues(axisYValues);
        axisX.setTextSize(15);
        axisY.setTextSize(15);
        axisX.setName("时段/h");
        axisY.setName("专注时间/min");
        timeDatas.setAxisXBottom(axisX);
        timeDatas.setAxisYLeft(axisY);
        timeChart.setZoomEnabled(false);
        timeChart.setColumnChartData(timeDatas);

        editor = getSharedPreferences("state", MODE_PRIVATE).edit();
        editor.putBoolean("isExist", isExist);
        editor.apply();
    }
}
