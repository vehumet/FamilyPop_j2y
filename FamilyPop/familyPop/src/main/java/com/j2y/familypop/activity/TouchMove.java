package com.j2y.familypop.activity;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by gmpguru on 2016-01-27.
 */
public class TouchMove implements View.OnTouchListener
{
    protected final int _START_DRAG = 0;
    protected final int _END_DRAG = 1;
    protected int _isMoving;
    protected float _offset_x, _offset_y;
    protected float _action_downX;
    protected float _action_downY;

    public boolean _actionDown = false;
    protected boolean _start_yn = true;
    public boolean _isClick = false;
    public boolean _move = true;
    public boolean _active = true;

    protected float _startPosx = 780.0f;
    protected float _startPosy;


    public float _normalX = 0.0f;
    public float _normalY = 0.0f;

    public float _touchDirVectorRotation = 0.0f;

    // 충돌
    private ArrayList<View> _collisionViews;
    private int _collisionView_ID;

    private View _src;
    public TouchMove(View src , float startPosX, float startPosy )
    {
        _src = src;
        _src.setOnTouchListener(this);

        _startPosx = startPosX;
        _startPosy = startPosy;

        if( src != null)
        {
            _src.setX(_startPosx);
            _src.setY(_startPosy);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if( _active )
        {
            int eventaction = event.getAction();
            switch (eventaction)
            {
                case MotionEvent.ACTION_DOWN: // 손가락이 스크린에 닿았을 때

                    _normalX = 0.0f;
                    _normalY = 0.0f;

                    if (_start_yn)
                    {
                        _offset_x = event.getRawX();
                        _offset_y = event.getRawY();
                        _start_yn = false;
                    }

                    if( !_actionDown )
                    {
                        _actionDown = true;

                        _action_downX = event.getRawX();
                        _action_downY = event.getRawY();
                    }

                    _isMoving = _START_DRAG;

                    _isClick = true;

                    break;
                case MotionEvent.ACTION_MOVE: // 닿은 채로 손가락을 움직일 때

                    int posx = (int) ((event.getRawX()) - (_offset_x - _startPosx));
                    int posy = (int) (event.getRawY() - _offset_y);


                    float nposx = event.getRawX() - _action_downX;
                    float nposy = event.getRawY() - _action_downY;

                    float effectiveAreaX = Math.abs(event.getRawX() - _action_downX );
                    float effectiveAreaY = Math.abs(event.getRawY() - _action_downY);

                    if( effectiveAreaX > 50 ||
                            effectiveAreaY  > 50   )
                    {
                        _isClick = false;
                    }

                    if( _move )
                    {
                        v.setX(posx);
                        v.setY(posy);
                    }
                    double dv = Math.sqrt(nposx * nposx + nposy * nposy);

                    nposx /= dv;
                    nposy /= dv;

                    //Math.toDegrees(angrad)
                    //Math.toRadians()

                    Vector2 v2 = new Vector2(nposx, nposy);
                    v2.rotate(_touchDirVectorRotation);

                    _normalX = v2.x;
                    _normalY = v2.y;

                    //Log.i("[J2Y]", "x : " + _normalX + " y : " + _normalY);
                    break;
                case MotionEvent.ACTION_UP: // 닿았던 손가락을 스크린에서 뗄때
                    _isMoving = _END_DRAG;
                    _actionDown = false;

                    _collisionView_ID = collision_chack(event.getRawX(), event.getRawY() );
                    break;
            }
        }
        return false;
    }

    //==============================================================================================================
    // private
    private int collision_chack(float x, float y)
    {
        int ret = -1;

//        for( View v : _collisionViews)
//        {
//            float posX = v.getX();
//            float posY = v.getY();
//            int destWidth  = v.getWidth();
//            int destHeight = v.getHeight();
//
//
//
//        }

        return ret;
    }

    //public
    public int GetCollision_viewID()
    {
        return _collisionView_ID;
    }

    public void AddView(View v)
    {
        _collisionViews.add(v);
    }


}