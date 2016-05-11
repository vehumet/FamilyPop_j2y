package com.j2y.familypop.client;

import android.os.RemoteException;
import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.Activity_clientMain;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.data.FpNetDataRes_recordInfoList;
import com.j2y.network.client.FpNetFacade_client;
import com.nclab.sociophone.SocioPhone;
import com.nclab.sociophone.interfaces.DisplayInterface;
import com.nclab.sociophone.interfaces.EventDataListener;
import com.nclab.sociophone.interfaces.TurnDataListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import kr.ac.kaist.resl.cmsp.iotapp.library.IoTAppService;
import kr.ac.kaist.resl.cmsp.iotapp.library.ThingServiceInfo;
import kr.ac.kaist.resl.cmsp.iotapp.library.impl.AndroidIoTAppService;
import kr.ac.kaist.resl.cmsp.iotappwrapper.FamilyPopService;
import kr.ac.kaist.resl.cmsp.iotappwrapper.FamilyPopServiceImpl;
import kr.ac.kaist.resl.cmsp.iotappwrapper.FpsUtil;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpcRoot
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpcRoot implements TurnDataListener, DisplayInterface, EventDataListener
{
	public static FpcRoot Instance;
	public static FpNetFacade_client _client;

    private Context _main_context;
    public SocioPhone _socioPhone;
    public int _clientId;

    // # localization  클라이언트용 선언
    public FpcLocalization_Client _localization;


    public FpcScenarioDirectorProxy _scenarioDirectorProxy;
    public ArrayList<FpcTalkRecord> _talk_records = new ArrayList<FpcTalkRecord>();
    public FpcTalkRecord _selected_talk_record;
    public int _bubble_color_type;
    public String _user_name; // id?
    public int _user_posid;

    private String _serverIP;

    // For IoTApp
    private static final String ID_HEADER_SERVER = "FamilyPop_Server_";
    private static final String ID_HEADER_CLIENT= "FamilyPop_Client_";
    public static String _myThingId = null;
    public static String _serverThingId = null;
    public static IoTAppService _iotAppService;
    public static FamilyPopService _fpService;

	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void Initialize(Context context)
	{
		Instance = this;
        _main_context = context;

        _scenarioDirectorProxy = new FpcScenarioDirectorProxy();
        _clientId = -1;

        // For IoTApp
        _iotAppService = new AndroidIoTAppService(_main_context);
        _iotAppService.connectPlatform(null);
        _myThingId = ID_HEADER_CLIENT + FpsUtil.getLocalIpAddress();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 네트워크
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void ConnectToServer(String ip)
    {
        Log.i("[J2Y]", "FpcRoot:ConnectToServer");

        _serverIP = ip;
        InitSocioPhone();
        //InitLocalization(ip);
        _client = new FpNetFacade_client();
        // Don't connect, because the connection would be done by IoTApp Platform
        //_client.ConnectServer(ip);
        // Pass _client as parameter, to utilize its callback functions
        _fpService = new FamilyPopServiceImpl(_main_context, _myThingId, FamilyPopService.class.getSimpleName(), _client);

        // Wait until server object joins cluster
        (new Thread(new Runnable() {
            @Override
            public void run() {
                boolean connected = false;
                try {
                    _iotAppService.registerLocalServiceObject(FamilyPopService.class, _fpService);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                while (!connected) {
                    try {
                        Thread.sleep(1000);
                        try {
                            if (getServerObject() != null) {
                                Log.d("IoTApp", "Got service object of server");
                                FpNetIncomingMessage msg = new FpNetIncomingMessage();
                                msg._socket = null;
                                _client._messageHandler.obtainMessage(FpNetConstants.Connected, msg).sendToTarget();
                                _client._recv_connected_message = true;
                                connected = true;
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();

        _socioPhone.connectToServer(ip);

        //_localization._client.start();

    }

    public static FamilyPopService getServerObject() throws RemoteException {
        if (_serverThingId == null) {
            List<ThingServiceInfo> infos = _iotAppService.getAvailableServicesNoScan(FamilyPopService.class);
            for (ThingServiceInfo info : infos) {
                if (info.getThingId().startsWith(ID_HEADER_SERVER)) {
                    Log.d("IoTApp", "Found FamilyPop server: " + info.getThingId());
                    _serverThingId = info.getThingId();
                    break;
                }
            }
        }
        if (_serverThingId != null) {
            FamilyPopService serverObj = _iotAppService.getServiceObject(FamilyPopService.class, _serverThingId);
            if (serverObj == null)
                Log.e("IoTApp", "FamilyPop server is out of the cluster");
            return serverObj;
        } else {
            //Log.e("IoTApp", "There is no FamilyPop server object in the cluster");
            return null;
        }
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void DisconnectServer()
    {
        DestroySocioPhone();
        DestroyLocalization();

        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _iotAppService.unregisterLocalServiceObject(_fpService);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                _fpService = null;
            }
        })).start();

        if(_client != null) {
            // Don't destroy, because there is no connection to destroy
            //_client.destroy();
            _client = null;
        }
        //MainActivity.Sleep(500);

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

        Log.i("[J2Y]", "FpcRoot:InitSocioPhone");

        _socioPhone = new SocioPhone(MainActivity.Instance, this, this, this, false);
        _socioPhone.setNetworkMode(true);
        _socioPhone.setVolumeOrderMode(true);

        // back 160211
        //_socioPhone.isServer = false;

        SocioPhone.isServer = false;
    }
    // # localization (client) 생성.
    public void InitLocalization()
    {
        Log.i("[J2Y]", "FpcRoot:InitLocalization");
        _localization = new FpcLocalization_Client();

        //CPS
        boolean isLocator = _user_name.equalsIgnoreCase("locator");
        _localization.ConnectToServer(MainActivity.Instance.getApplicationContext(), _user_name, isLocator, _serverIP  );
    }


    public void DestroySocioPhone()
    {
        // 보류
        Log.i("[J2Y]", "FpsRoot:DestroySocioPhone");

        //_socioPhone.stopRecord();
        //MainActivity.Sleep(500);

        // back 160211
//        if(_socioPhone != null){ _socioPhone.destroy(); }
//        _socioPhone = null;

    }
    // # localization 접속 종료.
    public void DestroyLocalization()
    {
        if( _localization != null)
            _localization.Disconnect();

        _localization = null;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onInteractionEventOccured(int speakerID, int eventType, long timestamp)
    {
        if(Activity_clientMain.Instance != null)
            Activity_clientMain.Instance.OnInteractionEventOccured(speakerID, eventType, timestamp);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onDisplayMessageArrived(int type, String message)
    {
        if(Activity_clientMain.Instance != null)
            Activity_clientMain.Instance.OnDisplayMessageArrived(type, message);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onTurnDataReceived(int[] speakerID)
    {
        if(Activity_clientMain.Instance != null)
            Activity_clientMain.Instance.OnTurnDataReceived(speakerID);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //  TalkRecord
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    public FpcTalkRecord NewTalkRecord()
    {
        _selected_talk_record = new FpcTalkRecord();
        return _selected_talk_record;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void AddTalkRecord(FpcTalkRecord talk)
    {
        if(talk._list_added)
            return;
        if(talk._filename == "")
            return;

        talk._list_added = true;
        _talk_records.add(talk);
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void AddSelectedTalkRecord()
    {
        if(_selected_talk_record != null)
            _talk_records.add(_selected_talk_record);
        _selected_talk_record = null;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void RemoveTalkRecord(FpcTalkRecord talk)
    {
        _talk_records.remove(talk);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public int GetTalkRecordCount()
    {
        return _talk_records.size();
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 서버에서 버블 정보들 수신됨
    public void RecordTalkBubbles(FpNetDataRes_recordInfoList data) {

        if(_selected_talk_record == null)
            return;

        _selected_talk_record._bubbles.clear();

        for(FpNetDataRes_recordInfoList.FpNetDataRes_recordInfoData bubble : data._bubbles) {

            //Log.i("[J2Y]", String.format("[NetClient]:%f,%f", bubble._x, bubble._y));
            _selected_talk_record.AddBubble(bubble._start_time, bubble._end_time, bubble._x - data._attractor._x, bubble._y - data._attractor._y, bubble._size, bubble._color);

        }

        _selected_talk_record._endTime = System.currentTimeMillis();

        AddTalkRecord(_selected_talk_record);
        SaveTalkRecords();
    }



    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 파일 기록
    private String s_filename_talk_record = "talk_record.bin";

    public void SaveTalkRecords()
    {
        //if(GetTalkRecordCount() <= 0)
        //    return;

        Log.i("[J2Y]", "FpcRoot:SaveTalkRecords");
        try
        {
            FileOutputStream fos  = _main_context.openFileOutput(s_filename_talk_record, Context.MODE_PRIVATE);

            ObjectOutputStream objStream = new ObjectOutputStream(fos);

//            if(_talk_records.size() > 0) {
//                FpcTalkRecord talk_record = _talk_records.get(_talk_records.size() - 1);
//                for (FpcTalkRecord.Bubble bubble : talk_record._bubbles)
//                    Log.i("[J2Y]", String.format("[Save][Bubble]:%f,%f", bubble._x, bubble._y));
//            }

            for( int i=0; i<_talk_records.size(); i++)
            {
                Log.i("[J2Y]", String.format("talk Name : %s",_talk_records.get(i)._filename ));
            }

            objStream.writeObject(_talk_records);
            objStream.close();

            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void LoadTalkRecords(Context ctx)
    {
        try
        {
            FileInputStream fileInputStream = ctx.openFileInput(s_filename_talk_record);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            _talk_records = (ArrayList<FpcTalkRecord>) objectInputStream.readObject();

//            FpcTalkRecord talk_record =_talk_records.get(0);
//            for(FpcTalkRecord.Bubble bubble : talk_record._bubbles)
//                Log.i("[J2Y]", String.format("[Load][Bubble]:%f,%f", bubble._x, bubble._y));

            objectInputStream.close();
            fileInputStream.close();
        }
        catch (FileNotFoundException e)
        {
            // 기록된 정보가 없음
            //e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}

