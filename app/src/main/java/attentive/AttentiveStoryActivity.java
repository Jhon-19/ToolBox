package attentive;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.toolbox.R;

public class AttentiveStoryActivity extends AppCompatActivity {
    MediaPlayer mp;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attentive_story_activity);
        mp = MediaPlayer.create(this, R.raw.bgm_city);
        mp.start();
        new AlertDialog.Builder(this)
                .setTitle("In development")
                .setMessage("This feature is not yet to be released.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AttentiveStoryActivity.this.finish();
                    }
                })
                .create()
                .show();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
    }
}
