package attentive;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class DiagramView extends View {
    ArrayList<AttentiveRecord> data;
    boolean startDraw=false;
    Paint pillar=new Paint();
    Paint other=new Paint();
    int progress=0;
    public DiagramView(Context context, AttributeSet set) {
        super(context,set);
        other.setTextSize(30);
        pillar.setColor(Color.BLUE);
    }
    public synchronized void showData(ArrayList<AttentiveRecord> data){
        this.data=data;

    }
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(startDraw&&progress<100){
            progress++;
            canvas.drawARGB(1,255,255,255);
            canvas.drawLine(50,950,50,950,other);
            canvas.drawLine(50,950,950,950,other);
            Iterator<AttentiveRecord> it=data.iterator();
            int interval=900/data.size();
            AttentiveRecord temp;
            for(int j=0;it.hasNext();j++){
                temp=it.next();
                canvas.drawText(temp.date,50+interval*j,1000,other);
                canvas.drawRect(translate(50+interval*j,950,interval-50,temp.attentiveSeconds*progress/(100*60)),pillar);
                canvas.drawText(Integer.toString(temp.attentiveSeconds/60),50+interval*j,950-(temp.attentiveSeconds*progress/(100*60)),other);
            }
        }
    }
    public void drawData(){
        startDraw=true;
    }
    private Rect translate(int x,int y,int w,int h){
        return new Rect(x,y-h,x+w,y);
    }
}
