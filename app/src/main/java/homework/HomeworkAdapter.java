package homework;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.toolbox.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import homework_edit.HomeworkEditActivity;

/**
 * The implementation of RecyclerView.Adapter.
 * Used for displaying movie item in list.
 *
 * @author Zhou Jingsen
 * */
public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder> {

    public ArrayList<Homework> homeworkList;
    public HomeworkActivity main;
    public static final String DATA_FILE_NAME="homework.dat";
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView dueDate;
        CheckBox complete;

         ViewHolder(View view) {
            super(view);
            description=view.findViewById(R.id.homework_desc);
            dueDate=view.findViewById(R.id.due_date);
            complete=view.findViewById(R.id.complete_check);
        }

    }
    @SuppressWarnings("unchecked")
    public HomeworkAdapter(File dataDir) {
        File listData=new File(dataDir.getParent()+"/"+DATA_FILE_NAME);
        if(!listData.exists()){
            homeworkList=new ArrayList<>();
        }
        try{
            ObjectInputStream read=new ObjectInputStream(new FileInputStream(listData));
            homeworkList=(ArrayList<Homework>)read.readObject();
            GregorianCalendar now=getCurrentStandardDay();
            ArrayList<Integer> removeIndexes=new ArrayList<>();
            for(int i=0;i<homeworkList.size();i++){
                if(homeworkList.get(i).completed){
                    if(divThousandIgnoreDetail(now.getTimeInMillis()-homeworkList.get(i).completeTime.getTimeInMillis())>=24*60*60)
                        removeIndexes.add(i);
                }
            }
            Iterator<Integer> removeIterator=removeIndexes.iterator();
            while(removeIterator.hasNext())
                homeworkList.remove(removeIterator.next().intValue());
        }catch(IOException|ClassNotFoundException e){
            Log.e("Homework","Error in reading list data file.",e);
            homeworkList=new ArrayList<>();
        }
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.homework_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Homework h = homeworkList.get(position);
        holder.complete.setText(h.subjectName);
        holder.complete.setChecked(h.completed);
        holder.description.setText(h.description);
        GregorianCalendar now=getCurrentStandardDay();
        Log.d("Homework[Debug]",Long.toString(h.deadline.getTimeInMillis()-now.getTimeInMillis()));
        int dayRemain=(int)(divThousandIgnoreDetail(h.deadline.getTimeInMillis()-now.getTimeInMillis())/(24*60*60));
        String dueDateText=
                        (dayRemain<-2)?"已逾期"+Math.abs(dayRemain)+"天":
                        (dayRemain==-2)?"前天":
                        (dayRemain==-1)?"昨天":
                        (dayRemain==0)?"今天":
                        (dayRemain==1)?"明天":
                        (dayRemain==2)?"后天":dayRemain+"天";
        int dueDateColor=
                (dayRemain<=0)?R.color.homework_deadline_danger:
                (dayRemain<=3)?R.color.homework_deadline_near:R.color.homework_deadline_far;
        holder.dueDate.setText(dueDateText);
        holder.dueDate.setTextColor(main.getResources().getColor(dueDateColor));
        holder.description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeworkEditActivity.current=h;
                HomeworkEditActivity.editExistHomework=true;
                Intent switcher=new Intent(main,HomeworkEditActivity.class);
                main.startActivity(switcher);
            }
        });
        final HomeworkAdapter _this=this;
        holder.complete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                h.completed=isChecked;
                if(isChecked)h.completeTime=getCurrentStandardDay();
                _this.saveList();
            }
        });
    }

    @Override
    public int getItemCount() {
        return homeworkList.size();
    }
    /**
     * Append a new homework item to the end of this list.
     *
     * @param m The homework to be added.
     * */
    public void addItem(Homework m){
        homeworkList.add(m);
        sortList();
        notifyItemRangeChanged(0,homeworkList.size());
    }
    public void saveList(){
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = main.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            main.requestPermissions(
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        try{
            ObjectOutputStream write=new ObjectOutputStream(new FileOutputStream(main.getFilesDir().getParent()+"/"+HomeworkAdapter.DATA_FILE_NAME));
            write.writeObject(homeworkList);
        }catch(IOException e){
            Log.e("Homework","Exception in writing list file",e);
        }
    }
    /**
     * Sort the homework list.
     * The completed homework and the homework with a later deadline will be in the bottom.
     * */
    public void sortList(){
        homeworkList.sort(new Comparator<Homework>() {
            @Override
            public int compare(Homework o1, Homework o2) {
                boolean xor=o1.completed&&!o2.completed||!o1.completed&&o2.completed;
                return xor?(o1.completed?1:-1):(int)(divThousandIgnoreDetail(o1.deadline.getTimeInMillis()-o2.deadline.getTimeInMillis())/(60*60*24));
            }
        });

        notifyItemRangeChanged(0,homeworkList.size());
    }
    /**
     * Get the gregorian calendar representing the 00:00 of today,
     * */
    public static GregorianCalendar getCurrentStandardDay(){
        GregorianCalendar now=new GregorianCalendar();
        now.set(Calendar.HOUR_OF_DAY,0);
        now.set(Calendar.MINUTE,0);
        now.set(Calendar.SECOND,0);
        return now;
    }
    /**
     * Divide a number by 1000, using half-up rounding.
     * */
    public static long divThousandIgnoreDetail(long input){
        boolean negative=input<0;
        input=Math.abs(input);
        long mod1000=input%1000;
        boolean addOrSub=input%10000>5000;
        return (negative?-1:1)*(addOrSub?input+(1000-mod1000):input-mod1000)/1000;
    }
}