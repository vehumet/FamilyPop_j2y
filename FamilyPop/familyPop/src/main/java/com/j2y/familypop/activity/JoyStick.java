package com.j2y.familypop.activity;

/**
 * Created by gmpguru on 2016-02-29.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.j2y.familypop.MainActivity;
import com.j2y.network.client.FpNetFacade_client;
import com.nclab.familypop.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class JoyStick
{
    // stick state
    public static final int STICK_NONE = 0;
    public static final int STICK_UP = 1;
    public static final int STICK_UPRIGHT = 2;
    public static final int STICK_RIGHT = 3;
    public static final int STICK_DOWNRIGHT = 4;
    public static final int STICK_DOWN = 5;
    public static final int STICK_DOWNLEFT = 6;
    public static final int STICK_LEFT = 7;
    public static final int STICK_UPLEFT = 8;


    //        0xffE64496,     // pink
//                0xffE44742,     // red
//                0xffEBEC4B,     // yellow
//                0xff45D18C,     // green
//                0xff47D4CD,		// phthalogreen
//                0xff4D82D6,     // blue
//                0xff66ff66, 	// юс╫ц
    // item
    public static final String ITEM_PINK = "0";
    public static final String ITEM_RED = "1";
    public static final String ITEM_YELLOW = "2";
    public static final String ITEM_GREEN = "3";
    public static final String ITEM_PHTHALOGREEN = "4";
    public static final String ITEM_BLUE = "5";

    // stick style
    public static final int STICK_STYLE_BLUE = 0;
    public static final int STICK_STYLE_GREEN = 1;
    public static final int STICK_STYLE_PHTHALOGREEN = 2;
    public static final int STICK_STYLE_PINK = 3;
    public static final int STICK_STYLE_RED = 4;
    public static final int STICK_STYLE_YELLOW = 5;

    private int STICK_ALPHA = 200;
    private int LAYOUT_ALPHA = 200;
    private int OFFSET = 0;

    private Context mContext;
    private ViewGroup mLayout;
    //private LayoutParams params;
    private LayoutParams params;
    private int stick_width, stick_height;

    private int position_x = 0, position_y = 0, min_distance = 0;
    private float distance = 0, angle = 0;

    private DrawCanvas draw;
    private Paint paint;
    private Bitmap stick;

    private boolean touch_state = false;

    public static userPos _select_userPos = null;

    //
    public HashMap<String, userPos> _drawItems = new HashMap<String, userPos>();

    public void deactive_userPos_all()
    {
        for( userPos u : _drawItems.values())
        {
            u.setDeactive();
        }
    }

    public JoyStick (Context context, ViewGroup layout, int stick_res_id)
    {
        mContext = context;

        stick = BitmapFactory.decodeResource(mContext.getResources(), stick_res_id);

        stick_width = stick.getWidth();
        stick_height = stick.getHeight();

        draw = new DrawCanvas(mContext);
        paint = new Paint();
        mLayout = layout;
        params = mLayout.getLayoutParams();
    }

    public JoyStick (Context context, ViewGroup layout, Drawable drw)
    {
        mContext = context;


        //stick = ((BitmapDrawable)drw).getBitmap();
        stick = drawableToBitmap(drw);

        stick_width = stick.getWidth();
        stick_height = stick.getHeight();

        draw = new DrawCanvas(mContext);
        paint = new Paint();
        mLayout = layout;
        params = mLayout.getLayoutParams();
        //Init_item();
    }

    //
    Handler handler = new Handler();

    int numberOfTaps = 0;
    long lastTapTimeMs = 0;
    long touchDownMs = 0;
    public void drawStick(MotionEvent arg1)
    {
        position_x = (int) (arg1.getX() - (params.width / 2));
        position_y = (int) (arg1.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
        angle = (float) cal_angle(position_x, position_y);

        if(arg1.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(distance <= (params.width / 2) - OFFSET)
            {
                draw.position(arg1.getX(), arg1.getY());
                draw();
                touch_state = true;

                Log.d("[joy]", "MotionEvent.ACTION_DOWN");
            }
        }
        else if(arg1.getAction() == MotionEvent.ACTION_MOVE && touch_state)
        {
            if(distance <= (params.width / 2) - OFFSET)
            {
                draw.position(arg1.getX(), arg1.getY());
                draw();
            }
            else if(distance > (params.width / 2) - OFFSET)
            {
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                draw.position(x, y);
                //draw.position(params.width / 2, params.height / 2);
                draw();
            }
            else
            {
                //mLayout.removeView(draw);
                draw.position(params.width / 2, params.height / 2);
            }
            Log.d("[joy]", "MotionEvent.ACTION_MOVE");
        }
        else if(arg1.getAction() == MotionEvent.ACTION_UP)
        {
            Log.d("[joy]", "MotionEvent.ACTION_UP");
            //mLayout.removeView(draw);
            draw.position(params.width / 2, params.height / 2);

            touch_state = false;
        }
        else
        {
            draw.position(params.width / 2, params.height / 2);
            touch_state = false;
        }


        // double touch
        switch (arg1.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownMs = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacksAndMessages(null);

                if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                    //it was not a tap

                    numberOfTaps = 0;
                    lastTapTimeMs = 0;
                    break;
                }

                if (numberOfTaps > 0
                        && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                    numberOfTaps += 1;
                } else {
                    numberOfTaps = 1;
                }

                lastTapTimeMs = System.currentTimeMillis();

                if (numberOfTaps == 3)
                {
                    Toast.makeText(Activity_clientMain.Instance, "triple", Toast.LENGTH_SHORT).show();
                    //handle triple tap
                }
                else if (numberOfTaps == 2)
                {
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //handle double tap
                            //Toast.makeText(Activity_clientMain.Instance, "double", Toast.LENGTH_SHORT).show();
                            if( _select_userPos != null)
                            {
                                if( _select_userPos != null){ FpNetFacade_client.Instance.SendPacket_req_userInteraction(_select_userPos._clientId); }
                                deactive_userPos_all();
                            }
                        }
                    }, ViewConfiguration.getDoubleTapTimeout());
                }
        }


    }

    // event touch
    public void Action_up()
    {
        draw.position(params.width / 2, params.height / 2);
        touch_state = false;
    }

    public int[] getPosition() {
        if(distance > min_distance && touch_state) {
            return new int[] { position_x, position_y };
        }
        return new int[] { 0, 0 };
    }

    public int getX() {
        if(distance > min_distance && touch_state) {
            return position_x;
        }
        return 0;
    }

    public int getY() {
        if(distance > min_distance && touch_state) {
            return position_y;
        }
        return 0;
    }

    public float getAngle() {
        if(distance > min_distance && touch_state) {
            return angle;
        }
        return 0;
    }

    public float getDistance() {
        if(distance > min_distance && touch_state) {
            return distance;
        }
        return 0;
    }
    public boolean gettouchState()
    {
        return touch_state;
    }

    public void setMinimumDistance(int minDistance) {
        min_distance = minDistance;
    }

    public int getMinimumDistance() {
        return min_distance;
    }

    public int get8Direction() {
        if(distance > min_distance && touch_state) {
            if(angle >= 247.5 && angle < 292.5 ) {
                return STICK_UP;
            } else if(angle >= 292.5 && angle < 337.5 ) {
                return STICK_UPRIGHT;
            } else if(angle >= 337.5 || angle < 22.5 ) {
                return STICK_RIGHT;
            } else if(angle >= 22.5 && angle < 67.5 ) {
                return STICK_DOWNRIGHT;
            } else if(angle >= 67.5 && angle < 112.5 ) {
                return STICK_DOWN;
            } else if(angle >= 112.5 && angle < 157.5 ) {
                return STICK_DOWNLEFT;
            } else if(angle >= 157.5 && angle < 202.5 ) {
                return STICK_LEFT;
            } else if(angle >= 202.5 && angle < 247.5 ) {
                return STICK_UPLEFT;
            }
        } else if(distance <= min_distance && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }

    public int get4Direction() {
        if(distance > min_distance && touch_state) {
            if(angle >= 225 && angle < 315 ) {
                return STICK_UP;
            } else if(angle >= 315 || angle < 45 ) {
                return STICK_RIGHT;
            } else if(angle >= 45 && angle < 135 ) {
                return STICK_DOWN;
            } else if(angle >= 135 && angle < 225 ) {
                return STICK_LEFT;
            }
        } else if(distance <= min_distance && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }

    public void setOffset(int offset) {
        OFFSET = offset;
    }

    public int getOffset() {
        return OFFSET;
    }

    public void setStickAlpha(int alpha) {
        STICK_ALPHA = alpha;
        paint.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return STICK_ALPHA;
    }

    public void setLayoutAlpha(int alpha) {
        LAYOUT_ALPHA = alpha;
        mLayout.getBackground().setAlpha(alpha);
    }

    public int getLayoutAlpha() {
        return LAYOUT_ALPHA;
    }

    public void setStickSize(int width, int height) {
        stick = Bitmap.createScaledBitmap(stick, width, height, false);
        stick_width = stick.getWidth();
        stick_height = stick.getHeight();
    }

    public void setStickWidth(int width) {
        stick = Bitmap.createScaledBitmap(stick, width, stick_height, false);
        stick_width = stick.getWidth();
    }

    public void setStickHeight(int height) {
        stick = Bitmap.createScaledBitmap(stick, stick_width, height, false);
        stick_height = stick.getHeight();
    }

    public int getStickWidth() {
        return stick_width;
    }

    public int getStickHeight() {
        return stick_height;
    }

    public void setLayoutSize(int width, int height)
    {
        params.width = width;
        params.height = height;
    }

    public int getLayoutWidth() {
        return params.width;
    }

    public int getLayoutHeight() {
        return params.height;
    }

    private double cal_angle(float x, float y) {
        if(x >= 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x));
        else if(x < 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if(x < 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if(x >= 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    //private void draw() {
    public void draw()
    {
        try
        {
            mLayout.removeView(draw);
        }
        catch (Exception e) { }
        mLayout.addView(draw);
    }
    public void draw(float x, float y)
    {
        position_x = (int) (x - (params.width / 2));
        position_y = (int) (y - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
        angle = (float) cal_angle(position_x, position_y);

        try
        {
            mLayout.removeView(draw);
        }
        catch (Exception e) { }
        draw.position(x, y);
        mLayout.addView(draw);
    }

    private class DrawCanvas extends View
    {
        float x, y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        public void onDraw(Canvas canvas)
        {
            canvas.drawBitmap(stick, x, y, paint);
        }

        private void position(float pos_x, float pos_y)
        {
            x = pos_x - (stick_width / 2);
            y = pos_y - (stick_height / 2);
        }
    }

//    // draw item test
//    private void Init_item()
//    {
//        ImageView image = new ImageView(mContext);
//        Resources res = mContext.getResources();
//        Drawable drawble = res.getDrawable(R.drawable.image_stickupos_green);
//        image.setImageDrawable(drawble);
//        mLayout.addView(image);
//
//        float tempX = 1;
//        float tempY = 0;
//
//        float x = (float) (Math.cos(Math.toRadians(cal_angle(tempX, tempY))) * ((params.width / 2) - OFFSET));
//        float y = (float) (Math.sin(Math.toRadians(cal_angle(tempX, tempY))) * ((params.height / 2) - OFFSET));
//        x += (params.width / 2);
//        y += (params.height / 2);
//        image.setX(x);
//        image.setY(y);
//
//        //AddItem(ITEM_GREEN, image, 100, 100);
//    }
    public void AddItem(String key, int clientId, float pos_x, float pos_y)
    {
        //ImageView item = new ImageView(mContext);
        userPos item = new userPos(mContext);
        Resources res = mContext.getResources();

        item._clientId = clientId;
        //Drawable drawble = null;
        switch (key)
        {
            case JoyStick.ITEM_BLUE:
                item.active = res.getDrawable(R.drawable.image_stickupos_non);
                item.deactive = res.getDrawable(R.drawable.image_stickupos_non);
                break;
            case JoyStick.ITEM_GREEN:
                item.active = res.getDrawable(R.drawable.image_stickupos_green_active);
                item.deactive = res.getDrawable(R.drawable.image_stickupos_green);
                break;
            case JoyStick.ITEM_PHTHALOGREEN:
                item.active = res.getDrawable(R.drawable.image_stickupos_non);
                item.deactive = res.getDrawable(R.drawable.image_stickupos_non);
                break;
            case JoyStick.ITEM_PINK:
                item.active = res.getDrawable(R.drawable.image_stickupos_pink_active);
                item.deactive = res.getDrawable(R.drawable.image_stickupos_pink);
                break;
            case JoyStick.ITEM_RED:
                item.active = res.getDrawable(R.drawable.image_stickupos_red_active);
                item.deactive = res.getDrawable(R.drawable.image_stickupos_red);
                break;
            case JoyStick.ITEM_YELLOW:
                item.active = res.getDrawable(R.drawable.image_stickupos_yellow_active);
                item.deactive = res.getDrawable(R.drawable.image_stickupos_yellow);
                break;
        }


        //item._button.setImageDrawable(drawble);
        mLayout.addView(item._button);
        item.setDeactive();

        float x = (float) (Math.cos(Math.toRadians(cal_angle(pos_x, pos_y))) * ((params.width / 2) - 32));
        float y = (float) (Math.sin(Math.toRadians(cal_angle(pos_x, pos_y))) * ((params.height / 2) - 16));
        x += (params.width / 2);
        y += (params.height / 2);
        item._button.setX(x - 64);
        item._button.setY(y - 32);

        _drawItems.put(key, item);
    }
    public void Remove_itemAll()
    {
        if( _drawItems == null) return;
        if( _drawItems.size() == 0 ) return;

        //mLayout.removeAllViews();
        //_drawItem.clear();

        _drawItems.values();
       for( userPos v : _drawItems.values() )
       {
           //mLayout.removeView((ImageView)v);
           mLayout.removeView(v._button);
       }
        _drawItems.clear();
    }
    public void SetPos_Item(String key, float pos_x, float pos_y)
    {
        float x = (float) (Math.cos(Math.toRadians(cal_angle(pos_x, pos_y))) * ((params.width / 2) -32));
        float y = (float) (Math.sin(Math.toRadians(cal_angle(pos_x, pos_y))) * ((params.height / 2) - 32));

        x += (params.width / 2);
        y += (params.height / 2);

        _drawItems.get(key)._button.setX(x);
        _drawItems.get(key)._button.setY(y);
    }

    public void Active()
    {
        mLayout.addView(draw);
        mLayout.setVisibility(View.VISIBLE);
    }
    public void Deactive()
    {
        mLayout.removeView(draw);
        deactive_userPos_all();
        mLayout.setVisibility(View.GONE);
    }
    public static Bitmap drawableToBitmap (Drawable drawable)
    {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable)
        {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null)
            {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
        {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        }
        else
        {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    //user item
    public class userPos implements View.OnClickListener
    {
        public ImageButton _button = null;
        private Context _Context;

        public Drawable active;
        public Drawable deactive;

        public int _clientId;

        public boolean _selectState = false;

        public userPos(Context context)
        {
            _Context = context;
            _button = new ImageButton(_Context);
            _button.setOnClickListener(this);
            _button.setBackgroundColor(256);
        }

        public void setActive()
        {
            _select_userPos = this;
            _button.setImageDrawable(active);
            _selectState = true;
        }
        public void setDeactive()
        {
            _select_userPos = null;
            _button.setImageDrawable(deactive);
            _selectState = false;
        }

        @Override
        public void onClick(View v)
        {
            _selectState = _selectState ? false : true;
            if( _selectState)
            {
                setActive();
            }
            else
            {
                setDeactive();
            }
           // if( _select_userPos != null){ FpNetFacade_client.Instance.SendPacket_req_userInteraction(_select_userPos._clientId); }
        }
    }
}


