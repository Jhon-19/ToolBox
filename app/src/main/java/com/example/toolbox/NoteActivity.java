package com.example.toolbox;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import notes.Note;

public class NoteActivity extends AppCompatActivity {

    private static List<Note> myNotes;
    private ListView noteList;
    private static ArrayAdapter<notes.Note> adapter;
    private boolean isLongClick;
    private static int sign;

    public final int SORT_BY_CREATED_TIME = 0;
    public final int SORT_BY_EDIT_TIME = 1;
    public final int SORT_BY_ID = 2;
    public final int SORT_BY_LENGTH = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);
        sign = 1;
        isLongClick = false;
        noteList = (ListView) findViewById(R.id.note_list);
        myNotes = LitePal.order("id desc").find(Note.class);
        adapter = new ArrayAdapter<Note>(
                NoteActivity.this, android.R.layout.simple_list_item_1, myNotes
        );
        sortNotes(SORT_BY_ID);
        noteList.setAdapter(adapter);
        hanldeItem();
    }

    private void sortNotes(int type){
        switch(type){
            case SORT_BY_CREATED_TIME:
                Collections.sort(myNotes, new Comparator<Note>() {
                    @Override
                    public int compare(Note note1, Note note2) {
                        if(note1.getCreatedTime().after(note2.getCreatedTime())){
                            return 1*sign;
                        }else if(note1.getCreatedTime().before(note2.getCreatedTime())){
                            return -1*sign;
                        }else{
                            return 0;
                        }
                    }
                });
                break;
            case SORT_BY_EDIT_TIME:
                Collections.sort(myNotes, new Comparator<Note>() {
                    @Override
                    public int compare(Note note1, Note note2) {
                        if(note1.getLastEditedTime().after(note2.getLastEditedTime())){
                            return 1*sign;
                        }else if(note1.getLastEditedTime().before(note2.getLastEditedTime())){
                            return -1*sign;
                        }else{
                            return 0;
                        }
                    }
                });
                break;
            case SORT_BY_ID:
                Collections.sort(myNotes, new Comparator<Note>() {
                    @Override
                    public int compare(Note note1, Note note2) {
                        if(note1.getId() > note2.getId()){
                            return 1*sign;
                        }else if(note1.getId() < note2.getId()){
                            return -1*sign;
                        }else{
                            return 0;
                        }
                    }
                });
                break;
            case SORT_BY_LENGTH:
                Collections.sort(myNotes, new Comparator<Note>() {
                    @Override
                    public int compare(Note note1, Note note2) {
                        if(note1.getLength() > note2.getLength()){
                            return 1*sign;
                        }else if(note1.getLength() < note2.getLength()){
                            return -1*sign;
                        }else {
                            return 0;
                        }
                    }
                });
                break;
        }
        adapter.notifyDataSetChanged();
        sign *= -1;
    }

    private void hanldeItem(){
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isLongClick){
                    Note selectedNote = myNotes.get(position);
                    Intent intent = new Intent(NoteActivity.this, CreateNoteActivity.class);
                    intent.putExtra("note", selectedNote.getText());
                    intent.putExtra("noteTitle", selectedNote.getTitle());
                    intent.putExtra("isEdit", true);
                    intent.putExtra("noteId", selectedNote.getId());
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            }
        });
        noteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                isLongClick = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setMessage("确定删除？");
                builder.setTitle("提示");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = myNotes.get(position).getId();
                        LitePal.delete(Note.class, id);
                        if(myNotes.remove(position) != null){
                            Toast.makeText(NoteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                        isLongClick = false;
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(NoteActivity.this, "已取消", Toast.LENGTH_SHORT).show();
                        isLongClick = false;
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item:
                Intent intent = new Intent(NoteActivity.this, CreateNoteActivity.class);
                startActivity(intent);
                break;
            case R.id.sort_by_create_time:
                sortNotes(SORT_BY_CREATED_TIME);
                break;
            case R.id.sort_by_edit_time:
                sortNotes(SORT_BY_EDIT_TIME);
                break;
            case R.id.sort_by_id:
                sortNotes(SORT_BY_ID);
                break;
            case R.id.sort_by_length:
                sortNotes(SORT_BY_LENGTH);
                break;
        }
        return true;
    }

    public static void changeNotes(){
        myNotes.removeAll(myNotes);
        myNotes.addAll(LitePal.order("id desc").find(Note.class));
        adapter.notifyDataSetChanged();
    }
}
