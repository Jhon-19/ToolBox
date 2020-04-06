package com.zjs.development;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zjs.development.movie.Homework;
import com.zjs.development.movie.HomeworkAdapter;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Homework edit page.
 *
 * @author Zhou Jingsen
 * */
public class HomeworkEditActivity extends AppCompatActivity {
    public static HomeworkAdapter adapter;
    public static boolean editExistHomework=false;
    public static Homework current=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homework_edit);
        TextView subjectName=findViewById(R.id.homework_edit_subject_name);
        TextView description=findViewById(R.id.homework_edit_description);
        DatePicker deadline=findViewById(R.id.homework_edit_deadline);
        Button submit=findViewById(R.id.homework_edit_submit);
        Button delete=findViewById(R.id.hoemwork_edit_delete);
        if(editExistHomework){
            subjectName.setText(current.subjectName);
            description.setText(current.description);
            deadline.updateDate(current.deadline.get(Calendar.YEAR),current.deadline.get(Calendar.MONTH),current.deadline.get(Calendar.DAY_OF_MONTH));
        }
        HomeworkEditActivity _this=this;
        AddHomeworkClickListener listener=new AddHomeworkClickListener();
        listener.cActivity=this;
        listener.adapter=adapter;
        listener.deadline=deadline;
        listener.description=description;
        listener.subjectName=subjectName;
        listener.parent=submit;
        submit.setOnClickListener(listener);
        DeleteHomeworkClickListener dlistener=new DeleteHomeworkClickListener();
        dlistener.adapter=adapter;
        dlistener.cActivity=this;
        dlistener.parent=delete;
        delete.setOnClickListener(dlistener);
    }
}
class AddHomeworkClickListener implements View.OnClickListener{
    TextView subjectName;
    TextView description;
    DatePicker deadline;
    HomeworkEditActivity cActivity;
    HomeworkAdapter adapter;
    Button parent;
    @Override
    public void onClick(View v) {
        parent.setClickable(false);
        Homework h = new Homework();
        h.description = description.getText().toString();
        h.subjectName = subjectName.getText().toString();
        h.completed = false;
        GregorianCalendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, deadline.getYear());
        date.set(Calendar.MONTH, deadline.getMonth());
        date.set(Calendar.DAY_OF_MONTH, deadline.getDayOfMonth());
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        h.deadline = date;
        if (HomeworkEditActivity.editExistHomework) {
            h.completed=HomeworkEditActivity.current.completed;
            h.completeTime=HomeworkEditActivity.current.completeTime;
            int index = adapter.homeworkList.indexOf(HomeworkEditActivity.current);
            adapter.homeworkList.remove(index);
            adapter.notifyItemRemoved(index);
        }
        HomeworkEditActivity.current=h;
        adapter.addItem(HomeworkEditActivity.current);
        adapter.saveList();
        HomeworkEditActivity.editExistHomework = false;
        cActivity.finish();
    }
}
class DeleteHomeworkClickListener implements View.OnClickListener{
    HomeworkEditActivity cActivity;
    HomeworkAdapter adapter;
    Button parent;
    @Override
    public void onClick(View v) {
        parent.setClickable(false);
        if (HomeworkEditActivity.editExistHomework) {
            int index = adapter.homeworkList.indexOf(HomeworkEditActivity.current);
            adapter.homeworkList.remove(index);
            adapter.notifyItemRemoved(index);
            adapter.saveList();
        }
        HomeworkEditActivity.editExistHomework = false;
        cActivity.finish();
    }
}