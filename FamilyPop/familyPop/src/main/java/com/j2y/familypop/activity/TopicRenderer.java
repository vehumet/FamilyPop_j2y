package com.j2y.familypop.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.LinearLayout;
import android.widget.TextView;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Created by lsh on 2016-03-23.
 */
public class TopicRenderer
{
    Context _context;
    PImage _topic = null;

    final int NON = -1;
    final int SHOW  = 0;
    final int STAY  = 1;

    private int _topicStatic = NON;
    private long _rotate_time = 0;
    private long _stay_time = 0;

    private void rotateTime(long millTime)
    {
        _rotate_time = System.currentTimeMillis() + millTime;
    }
    private void stayTime(long millTime)
    {
        _stay_time = System.currentTimeMillis() + millTime;
    }
    void Add_text(Context context, PApplet applet, String text)
    {

        float width = text.length()*30;

        _context = context;
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( (int)width, 100);
        tv.setLayoutParams(layoutParams);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.BLUE);

        Bitmap bitmap;

        bitmap = Bitmap.createBitmap((int)width,100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        tv.layout(0, 0, (int) width, 100);
        tv.draw(c);

        _topic = applet.createImage(bitmap.getWidth(), bitmap.getHeight(), applet.ARGB);

        int imagebuffer[] = new int[bitmap.getWidth()*bitmap.getHeight()];
        bitmap.getPixels(imagebuffer, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        System.arraycopy(imagebuffer, 0, _topic.pixels, 0, bitmap.getWidth() * bitmap.getHeight());
        _topic.updatePixels();

        _topicStatic = SHOW;
        counter = 0;
        rotateTime(2000);

    }
    int counter = 0;
    void draw(PApplet applet)
    {
        if( _topic == null) return;

        switch(_topicStatic)
        {
            case SHOW:

                if( System.currentTimeMillis() < _rotate_time)
                {

                    // world rotation
//                    applet.background(0);
//                    counter+=10;
//                    applet.translate(applet.width / 2 - _topic.width / 2, applet.height / 2 - _topic.height / 2);
//                    applet.rotate(counter * applet.TWO_PI / 360);
//                    applet.translate(-_topic.width / 2, -_topic.height / 2);
//                    applet.image(_topic,0,0);

                    //applet.HALF_PI
                    // 회전
                    int posX, posY;
                    posX = applet.width/2;
                    posY = applet.height/2;

//                    // object rotation
                    applet.pushMatrix();
                    counter+=30;
                    applet.translate(posX + (_topic.width / 2), posY + (_topic.height / 2));
                    applet.rotate(counter * applet.TWO_PI / 360);
                    applet.translate(-(posX + _topic.width / 2), -(posY + _topic.height / 2));
                    applet.image(_topic, posX,  posY);
                    applet.popMatrix();

                    //applet.image(_topic, applet.width / 2, applet.height / 2);
                }
                else
                {
                    _topicStatic = STAY;
                    stayTime(5000);
                    counter = 0;
                }

                break;
            case STAY:

                if( System.currentTimeMillis() < _stay_time )
                {
                    applet.image(_topic, applet.width / 2, applet.height / 2);
                }
                else
                {
                    _topicStatic = NON;
                    _topic = null;
                }
                break;
        }

    }
}
