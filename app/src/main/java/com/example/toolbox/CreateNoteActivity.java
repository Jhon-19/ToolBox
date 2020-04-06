package com.example.toolbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText noteText;
    private EditText titleText;
    private Button finishedButton;
    private Button cancelButton;
    private static boolean isTempRestored;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        noteText = (EditText)findViewById(R.id.note_text);
        titleText = (EditText)findViewById(R.id.title_text);
        finishedButton = (Button)findViewById(R.id.finish_button);
        cancelButton = (Button)findViewById(R.id.cancel_button);
        initialize();
        handleClick();
    }

    private void handleClick(){
        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = noteText.getText().toString();
                String title = titleText.getText().toString();
                if(TextUtils.isEmpty(text) && TextUtils.isEmpty(title)){
                    finish();
                    return;
                }
                notes.Note currentNote = new notes.Note();
                currentNote.setLength(text.length());
                if(!TextUtils.isEmpty(title)){
                    currentNote.setTitle(title);
                }else {
                    if (text.length() > 6) {
                        currentNote.setTitle(text.substring(0, 6));
                    } else {
                        currentNote.setTitle(text);
                    }
                }
                currentNote.setText(text);
                boolean isEdit = getIntent().getBooleanExtra("isEdit", false);
                if(isEdit){
                    currentNote.setLast_edited_time(new Date(System.currentTimeMillis()));
                    currentNote.update(getIntent().getIntExtra("noteId", -1));
                }else {
                    currentNote.setCreated_time(new Date(System.currentTimeMillis()));
                    currentNote.setLast_edited_time(new Date(System.currentTimeMillis()));
                    currentNote.save();
                }
                NoteActivity.changeNotes();
                isTempRestored = false;
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = noteText.getText().toString();
                String title = titleText.getText().toString();
                SharedPreferences.Editor editor =
                        getSharedPreferences("tempNote", MODE_PRIVATE).edit();
                editor.putString("title", title);
                editor.putString("text", text);
                editor.apply();
                isTempRestored = true;
                finish();
            }
        });
    }

    private void initialize(){
        Intent intent = getIntent();
        String note = intent.getStringExtra("note");
        String noteTitle = intent.getStringExtra("noteTitle");
        if(!TextUtils.isEmpty(note) & !TextUtils.isEmpty(noteTitle)){
            noteText.setText(note);
            titleText.setText(noteTitle);
        }else if(isTempRestored){
            SharedPreferences preferences = getSharedPreferences("tempNote", MODE_PRIVATE);
            String title = preferences.getString("title", "");
            String text = preferences.getString("text", "");
            titleText.setText(title);
            noteText.setText(text);
        }
    }
}
