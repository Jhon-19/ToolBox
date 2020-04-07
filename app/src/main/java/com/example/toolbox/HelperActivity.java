package com.example.toolbox;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
            }
        });
        timeStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDate = Calendar.getInstance();
                Toast.makeText(HelperActivity.this, "已结束专注模式", Toast.LENGTH_LONG).show();
                stopHour = stopDate.get(Calendar.HOUR_OF_DAY);
                stopMinute = stopDate.get(Calendar.MINUTE);
                hanldeTime();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startDate = Calendar.getInstance();
        startHour = startDate.get(Calendar.HOUR_OF_DAY);
        startMinute = startDate.get(Calendar.MINUTE);
    }

    @Override
    protected void onStop() {
        Toast.makeText(this, "离开专注模式，计时清空", Toast.LENGTH_SHORT).show();
        stopDate = Calendar.getInstance();
        stopHour = stopDate.get(Calendar.HOUR_OF_DAY);
        stopMinute = stopDate.get(Calendar.MINUTE);
        hanldeTime();
        super.onStop();
    }

    @Override
    protected void onPause() {
        Toast.makeText(this, "离开专注模式，计时清空", Toast.LENGTH_SHORT).show();
        stopDate = Calendar.getInstance();
        stopHour = stopDate.get(Calendar.HOUR_OF_DAY);
        stopMinute = stopDate.get(Calendar.MINUTE);
        hanldeTime();
        super.onPause();
    }

    private void hanldeTime(){
        for(int i = startHour; i <= stopHour; i++){
            columnList.remove(i);
            if(stopHour > startHour) {
                if(i == startHour){
                    columnList.add(i, createValues(60-startMinute));
                }else if(i == stopHour){
                    columnList.add(i, createValues(stopMinute));
                }
            }else{
                columnList.add(i, createValues(stopMinute-startMinute));
            }
        }
        timeDatas.setColumns(columnList);
        timeChart.setColumnChartData(timeDatas);
    }

    private Column createValues(int value){
        values = new ArrayList<>();
        values.add(new SubcolumnValue(value, ChartUtils.pickColor()));
        Column column = new Column(values);
        column.setHasLabels(true);
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
        timeChart.setColumnChartData(timeDatas);
    }
}
