package com.j2y.familypop.server;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.client.FpcRoot;

import cps.mobilemaestro.library.MMDeviceInfo;
import cps.mobilemaestro.library.MMDeviceLayout;
import cps.mobilemaestro.library.MMListener;
import cps.mobilemaestro.library.MMLocationResult;
import cps.mobilemaestro.library.MMServer;
import cps.mobilemaestro.library.MMUtils;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsLocalization_Server
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// # localization 서버
public class FpsLocalization_Server
{
    public MMServer _server;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 1. 서버 시작.
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // Call this when the tick-tac-toe game starts.
    // CPS: layout argument 추가
    public void StartServer(Context context, MMDeviceLayout layout)
    {
        _server = new MMServer(context, _messageHandler, layout);
        _server.start();
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void StopServer(Context context)
    {
        _server.stop();
        _server = null;
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 2. Start Sync
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 3. Localization
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++




    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 4. Message Handler
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private int _connected_client_count; // TEMP:

    private String _prvLocID = "";
    public Handler _messageHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            //Log.i("[J2Y]","getLocator : "+_server.getLocator());
            if( FpsRoot.Instance._exitServer) return;

            switch(msg.what)
            {
                // A server is not started yet.
                // Players are not initialized yet.
                case 0:
                    Log.i("[J2Y]", "A server is not started yet.");
                    break;

                // A server has been started.
                case 1:
                    Log.i("[J2Y]", " - IP Addr: " + _server.getSelfInfo().getIpAddr());
                    break;

                case 2:
                    Log.i("[J2Y]", "[LocalizationServer] a new client is connected.");

                    ++_connected_client_count;
                    if(_connected_client_count == 4)
                    {
                        Log.i("[J2Y]", "[LocalizationServer] startSync");
                        _server.startSync();
                    }
                    break;
                // Players are synchronized.
                case 3:
                    Log.i("[J2Y]", "[LocalizationServer] Players are synchronized.");


                    break;

                // Localization
                case 4:
                    // CPS: location result class 생성 및 id도 같이 return
                    MMLocationResult locResult = (MMLocationResult)msg.obj;
                    Log.i("[J2Y]", "[LocalizationServer] Measurement for " + locResult.getId() + " is completed with (" + locResult.getLocX() + ", " + locResult.getLocY() + ")");
                    if(Activity_serverMain.Instance != null)
                    {
                        if( Activity_serverMain.Instance._netEvent_tttSytleSuccess )
                        {
                            Activity_serverMain.Instance._tictactoe._curStyle = FpsTicTacToe.eTictactoeImageIndex.getValue(Activity_serverMain.Instance._ttt_style);
                            Activity_serverMain.Instance._locX = locResult.getLocX(); //0 이 x 인가??
                            Activity_serverMain.Instance._locY = locResult.getLocY(); //1 이 y 인가??
                            Activity_serverMain.Instance.TicTacToe_tileChange(locResult.getId());

                            Activity_serverMain.Instance._netEvent_tttSytleSuccess = false;
                        }
                    }

                    break;
                case 5:

                    //Toast.makeText(Activity_serverMain.Instance, "failure due to network delay.", Toast.LENGTH_SHORT).show();

                    break;

                default:
                    break;

            }
        }
    };

}
