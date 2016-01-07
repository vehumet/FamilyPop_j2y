package com.j2y.familypop;

import android.content.Intent;
import com.j2y.familypop.activity.Activity_clientMain;
import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.activity.lobby.Activity_title;
import com.j2y.familypop.client.FpcRoot;
import com.j2y.familypop.client.FpcTalkRecord;
import com.j2y.familypop.server.FpsRoot;
import com.j2y.familypop.server.FpsScenarioDirector;
import com.j2y.network.base.FpNetConstants;
import com.nclab.familypop.R;
import com.nclab.sociophone.SocioPhone;
import com.nclab.sociophone.interfaces.DisplayInterface;
import com.nclab.sociophone.interfaces.EventDataListener;
import com.nclab.sociophone.interfaces.TurnDataListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// MainActivity
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class MainActivity extends Activity
{
	public static MainActivity Instance;
	public FpsRoot _fpsRoot;
	public FpcRoot _fpcRoot;

    public double _calibration_width_length;
    public double _calibration_height_length;

	public boolean _virtualServer;

    public SharedPreferences _familypopSetting;

    //debug
    public String _deviceRole;

    //client init info
    //public boolean _ready;
    //public int _curServerScenario;  // server -> client

    //------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        Log.i("[J2Y]", "MainActivity:onCreate");

        _familypopSetting = getSharedPreferences("familypopSetting",0);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

        Instance = this;
		_virtualServer = false;

		startActivity(new Intent(this, Activity_title.class));

		_fpsRoot = new FpsRoot();
		_fpcRoot = new FpcRoot();
        _fpcRoot.Initialize(this);

		// test record save
		_fpcRoot._talk_records.clear();
        FpcTalkRecord recorddata = test_recordData();
		_fpcRoot._talk_records.add(recorddata);

        Log.i("[J2Y]", "ThreadID:[Root]" + (int) Thread.currentThread().getId());


        SharedPreferences.Editor editor  = MainActivity.Instance._familypopSetting.edit();
        editor.clear();

        // calibration
        _calibration_width_length = 130;
        _calibration_height_length = 90;

        //client init info
        //_ready = false;
        //_curServerScenario = FpNetConstants.SCENARIO_NONE;
    }



    @Override
    protected void onDestroy() {
        Log.i("[J2Y]", "MainActivity:onDestroy");
        super.onDestroy();
    }



    private FpcTalkRecord test_recordData() {
        FpcTalkRecord recorddata = new FpcTalkRecord();
        recorddata._name = "testName1234";
        recorddata._filename = "testfilename1234";
        recorddata._startTime = 1;
        recorddata._endTime = 10;

        recorddata.AddBubble(2, 9, 0, 0, 30, R.color.red);
        recorddata.AddBubble(2, 9, 100, 100, 50, R.color.red);
        recorddata.AddBubble(2, 9, 150, 200, 100, R.color.red);
        recorddata.AddBubble(2, 9, 200, 150, 130, R.color.red);

        recorddata.AddSmileEvent(2, 0, R.drawable.scroll_smilepoint1);
        recorddata.AddSmileEvent(5, 0, R.drawable.scroll_smilepoint1);

        _fpcRoot._talk_records.add(recorddata);
        //

        recorddata = new FpcTalkRecord();
        recorddata._name = "testName";
        recorddata._filename = "testfilename";
        recorddata._startTime = 1;
        recorddata._endTime = 10;

        recorddata.AddBubble(2, 9, 0, 150, 30, R.color.red);
        recorddata.AddBubble(2, 9, 150, 150, 60, R.color.red);
        recorddata.AddBubble(2, 9, 200, 150, 120, R.color.red);
        recorddata.AddBubble(2, 9, 250, 150, 150, R.color.red);
        recorddata.AddBubble(2, 9, 300, 150, 200, R.color.red);

        _fpcRoot._talk_records.add(recorddata);
        //
        recorddata = new FpcTalkRecord();
        recorddata._name = "testName5678";
        recorddata._filename = "testfilename5678";
        recorddata._startTime = 1;
        recorddata._endTime = 10;

        recorddata.AddBubble(2, 9, 200, 0, 300, R.color.red);
        return recorddata;
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------
    //return : 키가 있으면 : 키에 해당하는 값 ,  키가 없으면 : "null"
    //value  : 가 null 이 아니면 해당 키에 데이터를 넣음.
    public String familypopSettingValue(String key, String value )
    {
        String ret = "null";

        String str = MainActivity.Instance._familypopSetting.getString(key, "null");
        if( value != null)
        {
            SharedPreferences.Editor editor  = MainActivity.Instance._familypopSetting.edit();
            editor.putString(key, value );

            //editor.apply();
            editor.commit();

            //전체 제거 : editor.clear();
            //부분 제거 : editor.remove(key);
            ret = value;
        }
        else{ ret = str; }

        return ret;
    }
    public int GetFamilypopSettingValue_int(String key)
    {
        int ret = MainActivity.Instance._familypopSetting.getInt(key, 0);

        return ret;
    }


    public void SetFamilypopSettingValue_int(String key, int value)
    {

        SharedPreferences.Editor editor  = MainActivity.Instance._familypopSetting.edit();
        editor.putInt(key, value);
        editor.commit();

        //int ret = MainActivity.Instance._familypopSetting.getInt(key, 0);

//        if( value != 0)
//        {
//            SharedPreferences.Editor editor  = MainActivity.Instance._familypopSetting.edit();
//            editor.putInt(key, value);
//            editor.commit();
//            ret = value;
//        }
        //return ret;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 유틸
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public static void Sleep(long time)
    {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static long _debug_timecount;
    private static String _debug_name;
    public static void Debug_begin_timecount(String name)
    {
        _debug_timecount = 0;
        _debug_name = name;
        _debug_timecount = System.currentTimeMillis();

        Log.i("[SI]", _debug_name + "_Start timecount : " + _debug_timecount);
    }
    public static void Debug_end_timecount()
    {
        long end = System.currentTimeMillis();

        Log.i("[SI]", _debug_name + "_End timecount : " + _debug_timecount);
        Log.i("[SI]", _debug_name + "_"+(end -_debug_timecount )  +" milliseconds");
    }
}
