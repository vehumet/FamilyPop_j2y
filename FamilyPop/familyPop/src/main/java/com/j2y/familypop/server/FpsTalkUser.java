package com.j2y.familypop.server;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsTableDisplyer
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import android.util.Log;

import com.j2y.familypop.server.render.FpsBubble;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.server.FpNetServer_client;
import com.j2y.familypop.server.render.FpsAttractor;

import java.util.ArrayList;

import shiffman.box2d.Box2DProcessing;

public class FpsTalkUser
{
    public FpNetServer_client _net_client;

    // Game(FamilyBomb)
    public ArrayList<FpsBubble> _bubble;
    public FpsAttractor _attractor;
    public ArrayList<Integer> _smile_events = new ArrayList<Integer>();
    public int _bubble_color_type;
    public int _user_posid;

    // Calibration
    public float _calibrationX;
    public float _calibrationY;
    public boolean _isSetCalibration;


    //------------------------------------------------------------------------------------------------------------------------------------------------------
	public FpsTalkUser(FpNetServer_client net_client)
	{
        _net_client = net_client;

        _calibrationX = 0.0f;
        _calibrationY = 0.0f;
        _isSetCalibration = false;

        _user_posid = -1;

    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void ResetMovers()
    {
        if( _bubble == null) return;
        if( _bubble.size() == 0 ) return;

        for(FpsBubble move : _bubble)
            move.DestroyMover();
        _bubble.clear();
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void CreateAttractor(Box2DProcessing box2d, int width, int height)
    {

        /*
        5 |  4  | 3
        0 |  1  | 2
         */
        //Log.i("[J2Y]", "---------------------------------- CreateAttractor --------------------------------------------------");
        if(_attractor == null)
        {

            //switch(_net_client._clientID)
            switch(_user_posid)
           {
                case 0: //1
                    _calibrationX = width / 10.0f;
                    //_calibrationY = (height / 5.0f);
                    _calibrationY = (height / 2.1f) ;
                    break;
                case 1:
//                    //_calibrationX = 5 * width / 10.0f;
//                    //_calibrationY = height / 5.0f;
//                    _calibrationX = 5 * width / 10.0f;
//                    _calibrationY = 4 * height / 5.0f;

                    _calibrationX = width / 10.0f;
                    //_calibrationY = (height / 5.0f);
                    _calibrationY = (height / 2.0f) ;

                    break;
                case 2://3
//                    _calibrationX = 9 * width / 10.0f;
//                    //_calibrationY = height / 5.0f;
//                    _calibrationY = (height / 2.0f);

                    _calibrationX = 5 * width / 9.5f;
                    _calibrationY = 4 * height / 5.0f;
                    break;
                case 3:
                    _calibrationX = 9 * width / 10.0f;
                    _calibrationY = 4 * height / 5.0f;

                    break;
                case 4://5
//                    _calibrationX = 5 * width / 10.0f;
//                    _calibrationY = 4 * height / 5.0f;

                    _calibrationX = 9 * width / 10.0f;
                    //_calibrationY = height / 5.0f;
                    _calibrationY = (height / 2.1f);
                    break;
                case 5:
                    _calibrationX = width / 10.0f;
                    _calibrationY = 4 * height / 5.0f;


                    break;

//               switch(_user_posid)
//               {
//                   case 0:
//                       _calibrationX = width / 10.0f;
//                       _calibrationY = height / 5.0f;
//                       break;
//                   case 1:
//                       _calibrationX = 5 * width / 10.0f;
//                       _calibrationY = height / 5.0f;
//                       break;
//                   case 2:
//                       _calibrationX = 9 * width / 10.0f;
//                       _calibrationY = height / 5.0f;
//                       break;
//                   case 3:
//                       _calibrationX = 9 * width / 10.0f;
//                       _calibrationY = 4 * height / 5.0f;
//                       break;
//                   case 4:
//                       _calibrationX = 5 * width / 10.0f;
//                       _calibrationY = 4 * height / 5.0f;
//                       break;
//                   case 5:
//                       _calibrationX = width / 10.0f;
//                       _calibrationY = 4 * height / 5.0f;
//                       break;

//                case 0:
//                    _calibrationX = width / 10.0f;
//                    _calibrationY = height / 5.0f;
//                    break;
//                case 1:
//                    _calibrationX = 9 * width / 10.0f;
//                    _calibrationY = height / 5.0f;
//                    break;
//                case 2:
//                    _calibrationX = 9 * width / 10.0f;
//                    _calibrationY = 4 * height / 5.0f;
//                    break;
//                case 3:
//                    _calibrationX = width / 10.0f;
//                    _calibrationY = 4 * height / 5.0f;
//                    break;
            }

            //Random random = new Random();
            //int color = random.nextInt(FpNetConstants.ColorSize - 1);
            _attractor = new FpsAttractor(box2d, 100, _calibrationX, _calibrationY, FpNetConstants.ColorArray[_net_client._clientID]);
            _bubble = new ArrayList<FpsBubble>();

        }
        //Log.i("[J2Y]", "---------------------------------- END CreateAttractor --------------------------------------------------");
    }

}
