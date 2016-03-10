package com.testjar2.gmpguru.joystick_example2;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5;

    Joystick js;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView5 = (TextView)findViewById(R.id.textView5);

        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        Resources res = getResources();

        //js = new Joystick(getApplicationContext() , layout_joystick, R.drawable.image_stick_yellow);
        js = new Joystick(getApplicationContext() , layout_joystick, res.getDrawable(R.drawable.image_stick_yellow));

        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

        layout_joystick.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE)
                {
                    textView1.setText("X : " + String.valueOf(js.getX()));
                    textView2.setText("Y : " + String.valueOf(js.getY()));
                    textView3.setText("Angle : " + String.valueOf(js.getAngle()));
                    textView4.setText("Distance : " + String.valueOf(js.getDistance()));

                    int direction = js.get8Direction();
                    if(direction == Joystick.STICK_UP) {
                        textView5.setText("Direction : Up");
                    } else if(direction == Joystick.STICK_UPRIGHT) {
                        textView5.setText("Direction : Up Right");
                    } else if(direction == Joystick.STICK_RIGHT) {
                        textView5.setText("Direction : Right");
                    } else if(direction == Joystick.STICK_DOWNRIGHT) {
                        textView5.setText("Direction : Down Right");
                    } else if(direction == Joystick.STICK_DOWN) {
                        textView5.setText("Direction : Down");
                    } else if(direction == Joystick.STICK_DOWNLEFT) {
                        textView5.setText("Direction : Down Left");
                    } else if(direction == Joystick.STICK_LEFT) {
                        textView5.setText("Direction : Left");
                    } else if(direction == Joystick.STICK_UPLEFT) {
                        textView5.setText("Direction : Up Left");
                    } else if(direction == Joystick.STICK_NONE) {
                        textView5.setText("Direction : Center");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView1.setText("X :");
                    textView2.setText("Y :");
                    textView3.setText("Angle :");
                    textView4.setText("Distance :");
                    textView5.setText("Direction :");
                }
                return true;
            }
        });
    }
}
