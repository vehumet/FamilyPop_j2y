package com.j2y.familypop.client;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cps.mobilemaestro.library.MMClient;
import cps.mobilemaestro.library.MMUtils;
import cps.mobilemaestro.library.MMMeasureResult;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpcLocalization_Client
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// # localization 클라이언트
public class FpcLocalization_Client
{
    public MMClient _client;
    //public int _clientId;


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 1. ?쒕쾭 ?쒖옉
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //-------------------------------------------------------   -----------------------------------------------------------------------------------------------
    // Call this when the tick-tac-toe game starts.
    public void ConnectToServer(Context context, String id, boolean isLocator, String serverIpAddr)
    {
        Log.i("Locator", ""+id);
        //_clientId = Integer.valueOf(id);
        _client = new MMClient(context, _messageHandler, id, isLocator, serverIpAddr);
        _client.start();
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void Disconnect()
    {
        _client.stop();
        _client = null;
    }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Localization
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // Call this when a client requests to click the tic-tac-toe menu.
    public void PlayerLocalization()
    {
        if(_client != null && _client.connected)
            _client.requestLocalization();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 4. Message Handler
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public Handler _messageHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 0:
                    break;

                case 1:	// start client from TClientControlHandler
                    _client.connect();
                    Log.i("[J2Y]", "FpcLocalization_Client:Connect");
                    break;

                case 2:
                    // coordTv.setText(" - IP Addr: " + client.getSelfInfo().getIpAddr() + "\n - ID: " + client.getSelfInfo().getId());
                    Log.i("[J2Y]", " - IP Addr: " + _client.getSelfInfo().getIpAddr() + "\n - ID: " + _client.getSelfInfo().getId());
                    break;

            }
        }
    };
}
