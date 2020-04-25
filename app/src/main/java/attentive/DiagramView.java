package attentive;

import android.content.Context;
import android.view.View;

import java.util.HashMap;


public class DiagramView extends View {
    HashMap<String,Integer> data;
    public DiagramView(Context context) {
        super(context);
    }
    public synchronized void showData(HashMap<String,Integer> data){
        this.data=data;
    }
    static class DataAnimator implements Runnable{
        @Override
        public void run() {
        }
    }
}
