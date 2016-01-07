package com.j2y.familypop.server;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.Activity_clientMain;
import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.base.data.FpNetData_smileEvent;
import com.j2y.network.server.FpNetFacade_server;
import com.j2y.network.server.FpNetServer_client;
import com.nclab.sociophone.SocioPhone;
import com.nclab.sociophone.interfaces.DisplayInterface;
import com.nclab.sociophone.interfaces.EventDataListener;
import com.nclab.sociophone.interfaces.TurnDataListener;

import cps.mobilemaestro.library.MMDeviceLayout;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsRoot
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsRoot implements TurnDataListener, DisplayInterface, EventDataListener {
    public static FpsRoot Instance;
    public static FpNetFacade_server _server;

    public FpsMobileDeviceManager _mobileDeviceManager;  // 사용안하남 ?
    public FpsScenarioDirector _scenarioDirector;
    public FpsTableDisplyer _tableDisplayer;
    public SocioPhone _socioPhone;

    // # localization  서버용 선언
    public FpsLocalization_Server _localization;

    //public ArrayList<FpNetServer_client> _clients = new ArrayList<FpNetServer_client>();
    public String _room_user_names = "";

    public boolean _exitServer;
    public static boolean _using_sociophone_voice = false;  // [페밀리 토크] 소시오폰 라이브러리 사용 또는 J2y에서 개발된 모드 사용

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public FpsRoot() {
        Instance = this;

        _server = new FpNetFacade_server();

        _mobileDeviceManager = new FpsMobileDeviceManager();
        _tableDisplayer = new FpsTableDisplyer();
        _scenarioDirector = new FpsScenarioDirector();

        _exitServer = true;
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 서버
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void StartServer()
    {

        Log.i("[J2Y]", "FpsRoot:StartServer");

        _server.StartServer(7778);

        InitSocioPhone();
        InitLocalization();

        //MainActivity.Sleep(500);

        _exitServer = false;
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void CloseServer() {
        DestroySocioPhone();
        DestroyLocalization();

        _server.CloseServer();
        //MainActivity.Sleep(500);
        _room_user_names = "";
        _scenarioDirector.ChangeScenario(FpNetConstants.SCENARIO_NONE);
        _exitServer = true;
        //if(Activity_serverMain.Instance != null)
        //    Activity_serverMain.Instance.CloseServer();
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // SocioPhone
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void InitSocioPhone()
    {

        Log.i("[J2Y]", "FpsRoot:InitSocioPhone");

        _socioPhone = new SocioPhone(MainActivity.Instance, this, this, this, false);
        _socioPhone.setNetworkMode(true);
        _socioPhone.setVolumeOrderMode(true);

        _socioPhone.isServer = true;
        _socioPhone.openServer();
    }

    // # localization (server) 생성
    private void InitLocalization()
    {

        Log.i("[J2Y]", "FpsRoot:InitLocalization");

        _localization = new FpsLocalization_Server();
        //CPS: layout determine
        //2의 width와 height가 반대로 입력됨
        MMDeviceLayout layout = new MMDeviceLayout(
                MainActivity.Instance._familypopSetting.getInt("_calibration_width_length2", 42),
                MainActivity.Instance._familypopSetting.getInt("_calibration_height_length2", 28),
                MainActivity.Instance._familypopSetting.getInt("_calibration_width_length", 84),
                MainActivity.Instance._familypopSetting.getInt("_calibration_height_length", 56));

        //MMDeviceLayout layout = new MMDeviceLayout(42, 26, 84, 52);
        //MMDeviceLayout layout = new MMDeviceLayout(0, 0, MainActivity.Instance._familypopSetting.getInt("_calibration_height_length2", 15), MainActivity.Instance._familypopSetting.getInt("_calibration_width_length2", 15));
        //제대로 변경 후,
        //MMDeviceLayout layout = new MMDeviceLayout(MainActivity.Instance._familypopSetting.getInt("_calibration_width_length", 15), MainActivity.Instance._familypopSetting.getInt("_calibration_height_length", 15),  MainActivity.Instance._familypopSetting.getInt("_calibration_width_length2", 15), MainActivity.Instance._familypopSetting.getInt("_calibration_height_length2", 15));
        Log.i("[J2Y]", "Layout: " + layout.getCenterX() + ", " + layout.getCenterY() + ", " + layout.getWidth() + ", " + layout.getHeight());
        _localization.StartServer(MainActivity.Instance.getApplicationContext(), layout);
    }

//
//    public void StartSocioServer() {
//
//
////        Timer timer = new Timer();
////        timer.schedule(new TimerTask() {
////            @Override
////            public void run() {
////                //_socioPhone.startRecord(0, "temp");
////            }
////        }, 3000);
//    }


    public void DestroySocioPhone()
    {
        // 보류
        Log.i("[J2Y]", "FpsRoot:DestroySocioPhone");

//        if (_socioPhone != null)
//            _socioPhone.stopRecord();

        stopRecord();
        MainActivity.Sleep(500);

        if (_socioPhone != null)
            _socioPhone.destroy();
        _socioPhone = null;
    }
    public void stopRecord(){ if( _socioPhone != null){ _socioPhone.stopRecord(); } }

    // # localization (server) 종료
    private void DestroyLocalization()
    {
        Log.i("[J2Y]", "FpsRoot:DestroySocioPhone");

        if(_localization != null)
            _localization.StopServer(MainActivity.Instance.getApplicationContext());
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onInteractionEventOccured(int speakerID, int eventType, long timestamp)
    {

    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onDisplayMessageArrived(int type, String message)
    {
        if(Activity_serverMain.Instance != null)
            Activity_serverMain.Instance.OnDisplayMessageArrived(type, message);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onTurnDataReceived(int[] speakerID)
    {
        if(!_using_sociophone_voice)
            return;

        if (Activity_serverMain.Instance != null)
            Activity_serverMain.Instance.OnTurnDataReceived(speakerID[0]);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 기타
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public float[] _speaker_voice = new float[6];
    //private final float _voice_threadhold = 1000f;
    public float _voice_threadhold = 1000f;
    public long _smile_event_timer;

    // 1. 모든 클라이에서 음성 크기를 받음
    // 2. 가장 목소리가 큰 사용자가 대화 함
    public void onJ2yTurnDataReceived(int speakerID, float voice)
    {
        if( FpsRoot.Instance._exitServer) return;

        if(_using_sociophone_voice)
            return;
        if (Activity_serverMain.Instance == null)
            return;

        // voice 값이 너무 작으면 제거
        if(voice < _voice_threadhold)
            voice = 1f;
        _speaker_voice[speakerID + 2] = voice;

        boolean smile = true;

        boolean allClientTalk = true;
        for (FpNetServer_client client : FpNetFacade_server.Instance._clients)
        {
            if (_speaker_voice[client._clientID + 2] <= 0)
            {
                allClientTalk = false;

            }
//            if (_speaker_voice[client._clientID + 2] >= 10000)
//                smile = true;

            if (_speaker_voice[client._clientID + 2] <= Activity_serverMain.Instance._regulation_seekBar_smileEffect)
                smile = false;

        }

        if(smile && (System.currentTimeMillis() - _smile_event_timer > 5000))
        {
            int event_time = (int) FpsRoot.Instance._socioPhone.GetRecordTime();
            _smile_event_timer = System.currentTimeMillis();

            //FpsTalkUser talk_user = Activity_serverMain.Instance.GetTalkUser(client);
            //talk_user._smile_events.add(event_time);

            Activity_serverMain.Instance.OnEvent_smile(event_time);
            // 이벤트 전파
            FpNetData_smileEvent outMsg = new FpNetData_smileEvent();
            outMsg._time = event_time;
            FpNetFacade_server.Instance.BroadcastPacket(FpNetConstants.SCNoti_smileEvent, outMsg);
        }
        else if(allClientTalk)
        {
            int maxVoiceSpeaker = finde_max_speaker_voice();

            Log.i("[J2Y]", "[C->S] familyTalk_voice:" + maxVoiceSpeaker + ":" + _speaker_voice[maxVoiceSpeaker]);

            Activity_serverMain.Instance.OnTurnDataReceived(maxVoiceSpeaker);
            reset_speaker_voice();
        }
    }

    private void reset_speaker_voice()
    {
        for(int i = 0; i < _speaker_voice.length; ++i)
            _speaker_voice[i] = 0f;
        _speaker_voice[0] = _voice_threadhold;
    }
    private int finde_max_speaker_voice()
    {
        int speaker = 0;
        float max_voice = 0;
        for(int i = 0; i < _speaker_voice.length; ++i)
        {
            if(max_voice < _speaker_voice[i])
            {
                max_voice = _speaker_voice[i];
                speaker = i;
            }
        }
        return speaker;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 기타
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


}


