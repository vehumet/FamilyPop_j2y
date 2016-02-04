package com.j2y.network.client;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.Activity_clientMain;
import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.activity.Interaction_Target;
import com.j2y.familypop.client.FpcRoot;
import com.j2y.familypop.client.FpcScenarioDirectorProxy;
import com.j2y.familypop.server.FpsRoot;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.base.FpNetFacade_base;
import com.j2y.network.base.FpNetIOStream;
import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetMessageCallBack;
import com.j2y.network.base.FpNetOutgoingMessage;
import com.j2y.network.base.FpNetUtil;
import com.j2y.network.base.FpPacketData;
import com.j2y.network.base.FpPacketHeader;
import com.j2y.network.base.data.FpNetDataNoti_changeScenario;
import com.j2y.network.base.data.FpNetDataNoti_roomInfo;
import com.j2y.network.base.data.FpNetDataNoti_serverInfo;
import com.j2y.network.base.data.FpNetDataReq_TicTacToe_Start;
import com.j2y.network.base.data.FpNetDataReq_TicTacToe_index;
import com.j2y.network.base.data.FpNetDataReq_bubbleMove;
import com.j2y.network.base.data.FpNetDataReq_changeScenario;
import com.j2y.network.base.data.FpNetDataReq_regulation_info;
import com.j2y.network.base.data.FpNetDataReq_shareImage;
import com.j2y.network.base.data.FpNetDataRes_recordInfoList;
import com.j2y.network.base.data.FpNetData_base;
import com.j2y.network.base.data.FpNetData_familyTalk;
import com.j2y.network.base.data.FpNetData_setUserInfo;
import com.j2y.network.base.data.FpNetData_smileEvent;
import com.j2y.network.base.data.FpNetData_userInteraction;
import com.nclab.familypop.R;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpNetFacade_client
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpNetFacade_client extends FpNetFacade_base
{
	public static FpNetFacade_client Instance;
	private FpTCPConnector _connector;
	private FpNetIOStream _ioStream;
	public boolean _recv_connected_message;

	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public FpNetFacade_client()
	{
		Instance = this;
		_ioStream = null;

        RegisterMessageCallBackList();
	}


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 클라이언트 네트워크
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public boolean ConnectServer(String targetIP) 
	{
		_connector = new FpTCPConnector(targetIP, 7778, _messageHandler);
		_connector.start();
        _recv_connected_message = false;
		
		return true;
	}
	



	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void destroy() 
	{
		_connector.destroy();
        _connector = null;

        Disconnect();
	}


    //------------------------------------------------------------------------------------------------------------------------------------------------------
	public void sendMessage(int msgID, FpNetData_base outPacket)
	{
        if(_ioStream != null)
        {
            FpNetOutgoingMessage outMsg = new FpNetOutgoingMessage();
            outPacket.Packing(outMsg);

            FpPacketData packetData = new FpPacketData();
            packetData._header = new FpPacketHeader();
            packetData._header._size = outMsg.GetPacketSize();
            packetData._header._type = msgID;
            packetData._data = outMsg.GetPacketToByte();

            _ioStream.SendPacket(packetData);
        }
	}


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 메시지 핸들러
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 메시지 콜백 클래스 등록
    public void RegisterMessageCallBackList()
    {
        RegisterMessageCallBack(FpNetConstants.Connected, onConnected);
        RegisterMessageCallBack(FpNetConstants.ServerDisconnected, onDisConnected);

        //RegisterMessageCallBack(FpNetConstants.SCNoti_getServerState, onServerState);
        //RegisterMessageCallBack(FpNetConstants.SCNoti_OnStartScenario, onNotiStartScenario
        // SCNoti_roomUserInfo);
        RegisterMessageCallBack(FpNetConstants.SCNoti_startGame, onNoti_start_game);
        RegisterMessageCallBack(FpNetConstants.SCNoti_roomUserInfo, onNotiRoomInfo);
        RegisterMessageCallBack(FpNetConstants.SCNoti_ChangeScenario, onNotiChangeScenario);
        RegisterMessageCallBack(FpNetConstants.SCRes_TalkRecordInfo, onRes_talk_record_info);
        RegisterMessageCallBack(FpNetConstants.SCNoti_quitRoom, onNoti_quit_room);
        RegisterMessageCallBack(FpNetConstants.CSC_ShareImage, onSCS_share_image);
        RegisterMessageCallBack(FpNetConstants.SCNoti_smileEvent, onNoti_smile_event);

        RegisterMessageCallBack(FpNetConstants.SCReq_userBang, onReq_userBang);
        RegisterMessageCallBack(FpNetConstants.SCNoti_bombRunning, onNoti_bombRunning);
        RegisterMessageCallBack(FpNetConstants.SCNoti_endBomb, onNoti_endBomb);

        //tic tac toe
        RegisterMessageCallBack(FpNetConstants.SCNoti_Start_Tic_Tac_Toe, onNoti_startGame_Tic_Tac_Toe);
        RegisterMessageCallBack(FpNetConstants.SCNoti_end_Tic_Tac_Toe, onNoti_endGame_Tic_Tac_Toe);
        RegisterMessageCallBack(FpNetConstants.SCReq_winUser_Tic_Tac_Toe, onReq_userWin_Tic_Tac_Toe);
        RegisterMessageCallBack(FpNetConstants.SCReq_loseUser_Tic_Tac_Toe, onReq_userLose_Tic_Tac_Toe);


    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 서버 연결
    FpNetMessageCallBack onConnected = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            _socket = inMsg._socket;
            _ioStream = new FpNetIOStream(FpNetFacade_client.Instance, false, _messageHandler);
            _ioStream.start();

            Log.i("[J2Y]", "[Network] 서버 연결");

            _recv_connected_message = true;
        }
    };

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 서버 연결 끊김
    FpNetMessageCallBack onDisConnected = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Log.i("[J2Y]", "[Network] 서버 연결 끊김");

        }
    };

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 서버의 현재 상태를 받는다.

    FpNetMessageCallBack onServerState = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            // server state
            FpNetDataNoti_serverInfo msg = new FpNetDataNoti_serverInfo();
            msg.Parse(inMsg);

            //MainActivity.Instance._curServerScenario = msg._curScenario;
           // MainActivity.Instance._ready = true;
        }
    };

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 게임 시작
    FpNetMessageCallBack onNoti_start_game = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Log.i("[J2Y]", "[패킷수신] [게임 시작]");

            // todo: 게임 시작 이미지 띄우기
            //if(Activity_clientMain.Instance != null)
            //    Activity_clientMain.Instance._text_user.setText(data._userNames);
        }
    };

    // 틱텍토 시작
    FpNetMessageCallBack onNoti_startGame_Tic_Tac_Toe = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Log.i("[J2Y]", "[패킷수신] [Tic_Tac_Toe_게임 시작]");

            Activity_clientMain.Instance.Init_TicTacToe();
            Activity_clientMain.Instance._layout_ttt.setVisibility(View.VISIBLE);
            Activity_clientMain.Instance._layout_roomInfo.setVisibility(View.GONE);
            Activity_clientMain.Instance._layout_bubbleImage.setVisibility(View.GONE);


            //대화 내용 그냥 날려버림.
              FpcRoot.Instance._socioPhone.stopRecord(); // audio record 를 종료 한다.
              FpcRoot.Instance._socioPhone.recordRelease();

//            // 대화 녹음 중이였다면 내용을 저장 여부를 물어본다.
//            if( FpcScenarioDirectorProxy.Instance._activeScenarioType == FpNetConstants.SCENARIO_RECORD)
//            {
//                Activity_clientMain.Instance.onClick_Quitdialogue(false, false, true);  // 여지껏 이야기한 내용을 저장한다.
//                //FpcRoot.Instance._socioPhone.stopRecord(); // audio record 를 종료 한다.
////                FpcRoot.Instance._socioPhone.recordRelease();
//            }

            // 서버에게 틱텍토 스타일을 받는다.
            FpNetDataReq_TicTacToe_Start inMag = new FpNetDataReq_TicTacToe_Start();
            inMag.Parse(inMsg);
            Activity_clientMain.Instance._ttt_style = inMag._style;

            // 받은 스타일로 이미지를 선택한다.
            switch (inMag._style)
            {
                case 0:
                    Activity_clientMain.Instance._layout_ttt.setVisibility(View.GONE);
                    Activity_clientMain.Instance._layout_regulation.setVisibility(View.INVISIBLE);
                    Activity_clientMain.Instance._layout_roomInfo.setVisibility(View.GONE);
                    Activity_clientMain.Instance._layout_bubbleImage.setVisibility(View.GONE);
                case 1:
                    Activity_clientMain.Instance._button_ttt_Style.setBackgroundResource(R.drawable.image_ttt_active_o);
                    break;
                case 2:
                    Activity_clientMain.Instance._button_ttt_Style.setBackgroundResource(R.drawable.image_ttt_active_x);
                    break;
            }
            Activity_clientMain.Instance._selectScenario = FpNetConstants.SCENARIO_TIC_TAC_TOE;
            FpcRoot.Instance._scenarioDirectorProxy.ChangeScenario(FpNetConstants.SCENARIO_NONE);
        }
    };
    // 틱텍토 종료
    FpNetMessageCallBack onNoti_endGame_Tic_Tac_Toe = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Activity_clientMain.Instance._layout_ttt.setVisibility(View.INVISIBLE);
            Activity_clientMain.Instance._layout_roomInfo.setVisibility(View.VISIBLE);
            Activity_clientMain.Instance._layout_bubbleImage.setVisibility(View.VISIBLE);
        }
    };
    FpNetMessageCallBack onReq_userWin_Tic_Tac_Toe = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Activity_clientMain.Instance.OnEventSC_win_Tic_Tac_toe();
        }
    };
    FpNetMessageCallBack onReq_userLose_Tic_Tac_Toe = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Activity_clientMain.Instance.OnEventSC_lose_Tic_Tac_Toe();
        }
    };

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 방 정보
    FpNetMessageCallBack onNotiRoomInfo = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            FpNetDataNoti_roomInfo data = new FpNetDataNoti_roomInfo();
            data.Parse(inMsg);
            Log.i("[J2Y]", "[패킷수신] [방 정보]" + data._userNames);

            if(Activity_clientMain.Instance != null)
            {
                Activity_clientMain.Instance._text_user.setText(data._userNames);


                String[] bubbleDatas = data._bubblesInfo.split("/");
                String[] clientDatas = data._clientsInfo.split("/");

                Activity_clientMain.Instance.allDeactive_targetImage();
                Activity_clientMain.Instance.clear_touchViewObject();

                for( int i=0; i<bubbleDatas.length; i++)
                {
                    Interaction_Target target = target = new Interaction_Target();
                    target._bubbleColorType = Integer.parseInt(bubbleDatas[i]);
                    target._clientId = Integer.parseInt(clientDatas[i]);
                    target._targetImage = Activity_clientMain.Instance.active_targetImage(target._bubbleColorType);
                    Activity_clientMain.Instance.add_touchViewObject(target);
                }

//                Interaction_Target target = target = new Interaction_Target();
//                target._bubbleColorType = data._bubbleColorType;
//                target._clientId = data._client_id;
//                target._targetImage = Activity_clientMain.Instance.active_targetImage(data._bubbleColorType);
//
//                Activity_clientMain.Instance.add_touchViewObject(target);
            }
        }
    };


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 시나리오 변경됨 공지
    FpNetMessageCallBack onNotiChangeScenario = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {

            Log.i("[J2Y]", "[패킷수신] 시나리오 변경됨 공지");

            FpNetDataNoti_changeScenario data = new FpNetDataNoti_changeScenario();
            data.Parse(inMsg);

            FpcRoot.Instance._scenarioDirectorProxy.ChangeScenario(data._changeScenario);

//            if( Activity_clientMain.Instance._selectScenario !=  data._changeScenario)
//            {
//                FpcRoot.Instance._scenarioDirectorProxy.ChangeScenario(data._changeScenario);
//            }
        }
    };

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 버블 정보 응답
    FpNetMessageCallBack onRes_talk_record_info = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {

            Log.i("[J2Y]", "[패킷수신] 버블 정보 응답");

            FpNetDataRes_recordInfoList data = new FpNetDataRes_recordInfoList();
            data.Parse(inMsg);

            FpcRoot.Instance.RecordTalkBubbles(data);
        }
    };

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 방 종료
    FpNetMessageCallBack onNoti_quit_room = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {

            Log.i("[J2Y]", "[패킷수신] 방 종료");

            if(Activity_clientMain.Instance != null)
                Activity_clientMain.Instance.OnEventSC_exitRoom();
        }
    };



    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 이미지 공유
    FpNetMessageCallBack onSCS_share_image = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {

            Log.i("[J2Y]", "[패킷수신] 이미지 공유");

            FpNetDataReq_shareImage data = new FpNetDataReq_shareImage();
            data.Parse(inMsg);

            if(Activity_clientMain.Instance != null)
                Activity_clientMain.Instance.SetupSharedImage(data._bitMapByteArray);
        }
    };

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 웃음 이벤트
    FpNetMessageCallBack onNoti_smile_event = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Log.i("[J2Y]", "[패킷수신] 웃음 이벤트");
            FpNetData_smileEvent data = new FpNetData_smileEvent();
            data.Parse(inMsg);

            if(Activity_clientMain.Instance != null)
                Activity_clientMain.Instance.OnEventSC_smileEvent(data._time);
        }
    };

    FpNetMessageCallBack onReq_userBang = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {

            Activity_clientMain.Instance.OnEventSC_bang();

        }
    };
    FpNetMessageCallBack onNoti_bombRunning = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Activity_clientMain.Instance.OnEventSC_bombRunning();
        }
    };
    FpNetMessageCallBack onNoti_endBomb = new FpNetMessageCallBack()
    {
        @Override
        public void CallBack(FpNetIncomingMessage inMsg)
        {
            Activity_clientMain.Instance.OnEventSC_endBomb();
        }
    };

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 메시지 보내기
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 사용자 정보 보내기
    public void SendPacket_setUserInfo(String name, int bubbleColorType, int user_posid)
    {
        Log.i("[J2Y]", "[C->S] 사용자 정보 보내기");
        FpNetData_setUserInfo reqPaket = new FpNetData_setUserInfo();
        reqPaket._userName = name;
        reqPaket._bubbleColorType = bubbleColorType;
        reqPaket._user_posid = user_posid;
        sendMessage(FpNetConstants.CSReq_setUserInfo, reqPaket);
    }

//    //------------------------------------------------------------------------------------------------------------------------------------------------------
//    // 스마일 이벤트
//    public void SendPacket_smileEvent(int time, int eventType)
//    {
//        Log.i("[J2Y]", "[C->S] 스마일 이벤트");
//        FpNetData_smileEvent reqPaket = new FpNetData_smileEvent();
//        reqPaket._time = time;
//        reqPaket._eventType = eventType;
//        sendMessage(FpNetConstants.CSReq_smileEvent, reqPaket);
//    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 게임 시작 요청
    public void SendPacket_req_startGame()
    {
        Log.i("[J2Y]", "[C->S] 게임 시작 요청");
        FpNetData_base reqPaket = new FpNetData_base();

        sendMessage(FpNetConstants.CSReq_OnStartGame, reqPaket);
    }

    // 틱텍톡 요청
    public void SendPacket_req_start_Tic_Tac_Toe()
    {
        Log.i("[J2Y]", "Tic_Tac_Toe 게임 시작 요청");
        FpNetData_base reqPaket = new FpNetData_base();

        sendMessage(FpNetConstants.CSReq_OnStart_Tic_Tac_Toe, reqPaket);
    }
    //CSReq_selectIndex_Tic_Tac_Toe
    public void SendPacket_req_selectIndex_TicTacToe(int tileStyle)
    {
        Log.i("[J2Y]", "tic_tac_toe 선택타일 인덱스 서버로 전달");

        FpNetDataReq_TicTacToe_index reqPaket = new FpNetDataReq_TicTacToe_index();
        //reqPaket._index = tileIndex;
        reqPaket._style = tileStyle;


        sendMessage(FpNetConstants.CSReq_selectIndex_Tic_Tac_Toe, reqPaket);
    }
    public void SendPacket_req_throwback_Tic_Tac_Toe()
    {
        Log.i("[J2Y]", "tic_tac_toe 타일 선택 취소");
        FpNetData_base reqPaket = new FpNetData_base();
        sendMessage(FpNetConstants.CSReq_throwback_Tic_Tac_Toe, reqPaket );
    }
    public void SendPacket_req_end_Tic_Tac_Toe()
    {

        Log.i("[J2Y]", "Tic_Tac_Toe 게임 종료 요청");
        FpNetData_base reqPaket = new FpNetData_base();

        sendMessage(FpNetConstants.CSReq_OnEnd_Tic_Tac_Toe, reqPaket);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 시나리오 변경 요청
    public void SendPacket_req_changeScenario(int changeScenarioType)
    {
        Log.i("[J2Y]", "[C->S] 시나리오 변경 요청");

        //if( changeScenarioType != FpNetConstants.SCENARIO_NONE)
        {
            FpNetDataReq_changeScenario reqPaket = new FpNetDataReq_changeScenario();
            reqPaket._changeScenario = changeScenarioType;

            sendMessage(FpNetConstants.CSReq_ChangeScenario, reqPaket);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 이미지 공유 요청
    public void SendPacket_req_shareImage(Bitmap shareBitmap)
    {
        Log.i("[J2Y]", "[C->S] 이미지 공유 요청");
        FpNetDataReq_shareImage reqPaket = new FpNetDataReq_shareImage();
        if(shareBitmap != null)
            reqPaket._bitMapByteArray = FpNetUtil.BitmapToByteArray(shareBitmap);
        else
            reqPaket._bitMapByteArray = null;

        sendMessage(FpNetConstants.CSC_ShareImage, reqPaket);
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 대화 정보(버블, 웃음 이벤트 목록) 요청
    public void SendPacket_req_talk_record_info()
    {
        Log.i("[J2Y]", "[C->S] 대화 정보(버블, 웃음 이벤트 목록) 요청");
        FpNetData_base reqPaket = new FpNetData_base();
        sendMessage(FpNetConstants.CSReq_TalkRecordInfo, reqPaket);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // regulation 서버로 전송
    public  void SendPacket_req_regulation_info(int seekBar_0, int seekBar_1, int seekBar_2, int seekBar_3, int seekBar_voice_hold , int voiceProcessingMode, int smileEffect, int plusBubbleSize)
    {
        Log.i("[J2Y]", "[C->S] regulation 서버로 전송");

        FpNetDataReq_regulation_info reqPaket = new FpNetDataReq_regulation_info();
        reqPaket._seekBar_0 = seekBar_0;
        reqPaket._seekBar_1 = seekBar_1;
        reqPaket._seekBar_2 = seekBar_2;
        reqPaket._seekBar_3 = seekBar_3;
        reqPaket._seekBar_voice_hold = seekBar_voice_hold;
        reqPaket._voiceProcessingMode = voiceProcessingMode;
        reqPaket._seekBar_regulation_smileEffect = smileEffect;
        reqPaket._seekBar_bubble_plusSIze = plusBubbleSize;

        sendMessage(FpNetConstants.CSReq_regulation_Info, reqPaket);
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 거품 제거
    public void SendPacket_req_clearBubble()
    {
        Log.i("[J2Y]", "[C->S] 버블 제거");

        if( FpcScenarioDirectorProxy.Instance._activeScenarioType == FpNetConstants.SCENARIO_RECORD ||
                FpcScenarioDirectorProxy.Instance._activeScenarioType == FpNetConstants.SCENARIO_GAME)
        {
            FpNetData_base reqPaket = new FpNetData_base();
            sendMessage(FpNetConstants.CSReq_clearBubble, reqPaket);
        }
    }

    public void SendPacket_familyTalk_voice(float voice)
    {
        FpNetData_familyTalk reqPaket = new FpNetData_familyTalk();
        reqPaket._voice = voice;

        sendMessage(FpNetConstants.CSReq_familyTalk_voice, reqPaket);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    // 사용자 메세지
    public void SendPacket_req_userInput_bubbleMove(float dirX, float dirY)
    {
        FpNetDataReq_bubbleMove reqPaket = new FpNetDataReq_bubbleMove();
        reqPaket._dirX = dirX;
        reqPaket._dirY = dirY;

        sendMessage(FpNetConstants.CSReq_userInput_bubbleMove, reqPaket);
    }

    public void SendPacket_req_userInteraction(int clientId)
    {
        FpNetData_userInteraction reqPaket = new FpNetData_userInteraction();
        reqPaket._clientid = clientId;

        sendMessage(FpNetConstants.CSReq_userInteraction, reqPaket);
    }
}
