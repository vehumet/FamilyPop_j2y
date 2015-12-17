package com.j2y.familypop.activity.lobby;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.BaseActivity;
import com.j2y.familypop.client.FpcLocalization_Client;
import com.j2y.familypop.server.FpsLocalization_Server;
import com.nclab.familypop.R;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// Activity_locatorStart
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class Activity_locatorStart extends BaseActivity implements View.OnClickListener
{
    ImageButton _button_home;
    ImageButton _button_next;

    TextView _textView_locator_serverIP;

    // # localization locator 역할용 클라이언트.
    FpcLocalization_Client _localization;

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialogue_start_locator);

        _button_home = (ImageButton) findViewById(R.id.button_start_locator_waiting_topmenu_home);
        _button_next = (ImageButton) findViewById(R.id.button_start_locator_next);

        _button_home.setOnClickListener(this);
        _button_next.setOnClickListener(this);

        _textView_locator_serverIP = (TextView) findViewById(R.id.textView_locator_serverIP);

        _textView_locator_serverIP.setText(MainActivity.Instance._familypopSetting.getString("locator_serverIP", "192.168.0.104"));

    }

    // # localization  locator 역할 선택 시 생성.
    public void InitLocalization()
    {
        if( _localization == null )
        {
            _localization = new  FpcLocalization_Client();
            _localization.ConnectToServer(MainActivity.Instance.getApplicationContext(), "locator", true, _textView_locator_serverIP.getText().toString());
        }
    }
    // # localization  locator 종료시 호출.
    // todo: ( 아직 종료 이벤트를 만들지 않음.)
    public void DisconnectLocalization()
    {
        if( _localization != null)
            _localization.Disconnect();

        _localization = null;
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_start_locator_waiting_topmenu_home:

                startActivity(new Intent(MainActivity.Instance, Activity_talkHistory.class));

                break;
            case R.id.button_start_locator_next:

                // # localization  locator 생성
                InitLocalization();

                SharedPreferences.Editor editor = MainActivity.Instance._familypopSetting.edit();
                editor.putString("locator_serverIP",_textView_locator_serverIP.getText().toString());
                editor.commit();

                startActivity(new Intent(MainActivity.Instance, Activity_locatorWaitingForConnection.class));
                break;
        }
    }
}