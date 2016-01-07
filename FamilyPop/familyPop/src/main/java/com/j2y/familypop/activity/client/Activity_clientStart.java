package com.j2y.familypop.activity.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.Activity_clientMain;
import com.j2y.familypop.activity.server.Activity_serverCalibration;
import com.j2y.familypop.backup.Dialog_MessageBox_ok_cancel;
import com.j2y.familypop.client.FpcRoot;
import com.j2y.network.client.FpNetFacade_client;
import com.nclab.familypop.R;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// Activity_clientStart
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//public enum eTictactoeImageIndex
//{   EMPTY(0), O(1), X(2);
//
//    private int value;
//    eTictactoeImageIndex(int i)
//    {
//        value = i;
//    }
//    public int getValue()
//    {
//        return value;
//    }
//
//    public boolean Compare(int i){return value == i;}
//    public static eTictactoeImageIndex getValue(int value)
//    {
//        eTictactoeImageIndex[] as = eTictactoeImageIndex.values();
//        for( int i=0; i<as.length; i++)
//        {
//            if( as[i].Compare(value))
//            {
//                return as[i];
//            }
//        }
//
//        return eTictactoeImageIndex.EMPTY;
//    }
//}
public class Activity_clientStart extends Activity implements View.OnClickListener
{
    enum eCheckBoxPosColor
    {
        NON(-1),PINK(0), RED(1), YELLOW(2), GREEN(3), PHTHALOGREEN(4), BLUE(5), MAX(6);

        private int value;
        eCheckBoxPosColor(int i){value = i;}
        public int getValue(){return value;}

        public boolean Compare(int i){return value == i;}
        public static eCheckBoxPosColor getValue(int value)
        {
            eCheckBoxPosColor[] as = eCheckBoxPosColor.values();
            for( int i=0; i<as.length; i++)
            {
                if( as[i].Compare(value))
                {
                    return as[i];
                }
            }
            return eCheckBoxPosColor.NON;
         }
    }

    private EditText _ipText;
    private ImageButton _nextBtn;
    private EditText _user_name;

    private RadioButton[] _radio_button_colors = new RadioButton[6];
    private RadioButton[] _radio_button_posID = new RadioButton[6];

    private CheckBox[] _checkbox_pos_color = new CheckBox[eCheckBoxPosColor.MAX.getValue()];

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i("[J2Y]", "Activity_clientStart:onCreate");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialogue_start_client);

        _ipText = (EditText) findViewById(R.id.ClientIPText);
        _user_name = (EditText) findViewById(R.id.Text_userName);
        _nextBtn = (ImageButton) findViewById(R.id.ClientNextButton);
        _nextBtn.setOnClickListener(this);

        load_client_information();

        _radio_button_colors[0] = (RadioButton) findViewById(R.id.radioButton1);    // 빨강
        _radio_button_colors[1] = (RadioButton) findViewById(R.id.radioButton2);    // 녹색
        _radio_button_colors[2] = (RadioButton) findViewById(R.id.radioButton3);    // 노랑
        _radio_button_colors[3] = (RadioButton) findViewById(R.id.radioButton4);    // 파랑
        _radio_button_colors[4] = (RadioButton) findViewById(R.id.radioButton5);    // 핑크
        _radio_button_colors[5] = (RadioButton) findViewById(R.id.radioButton6);    // 
        for(int i = 0; i < _radio_button_colors.length; ++i)
            _radio_button_colors[i].setOnClickListener(optionOnClickListener);
        _radio_button_colors[FpcRoot.Instance._bubble_color_type].setChecked(true);


        _radio_button_posID[0] = (RadioButton) findViewById(R.id.radioButton_PosID_0);
        _radio_button_posID[1] = (RadioButton) findViewById(R.id.radioButton_PosID_1);
        _radio_button_posID[2] = (RadioButton) findViewById(R.id.radioButton_PosID_2);
        _radio_button_posID[3] = (RadioButton) findViewById(R.id.radioButton_PosID_3);
        _radio_button_posID[4] = (RadioButton) findViewById(R.id.radioButton_PosID_4);
        _radio_button_posID[5] = (RadioButton) findViewById(R.id.radioButton_PosID_5);

        for( int i=0; i<_radio_button_posID.length; ++i)
        {
            _radio_button_posID[i].setOnClickListener(posidOnClickListener);
        }
        _radio_button_posID[FpcRoot.Instance._user_posid].setChecked(true);


        _onceClick_connectToServer = false;

        // 컬러와 위치를 선택한다.
        _checkbox_pos_color[eCheckBoxPosColor.PINK.getValue()] = (CheckBox) findViewById(R.id.checkBox_pos_color_pink);
        _checkbox_pos_color[eCheckBoxPosColor.RED.getValue()] = (CheckBox) findViewById(R.id.checkBox_pos_color_red);
        _checkbox_pos_color[eCheckBoxPosColor.YELLOW.getValue()] = (CheckBox) findViewById(R.id.checkBox_pos_color_yellow);
        _checkbox_pos_color[eCheckBoxPosColor.GREEN.getValue()] = (CheckBox) findViewById(R.id.checkBox_pos_color_green);
        _checkbox_pos_color[eCheckBoxPosColor.PHTHALOGREEN.getValue()] = (CheckBox) findViewById(R.id.checkBox_pos_color_phthalogreen);
        _checkbox_pos_color[eCheckBoxPosColor.BLUE.getValue()] = (CheckBox) findViewById(R.id.checkBox_pos_color_blue);

        for(int i=0; i<eCheckBoxPosColor.MAX.getValue(); i++)
        {
            _checkbox_pos_color[i].setOnClickListener(this);
        }
        setUserPos(eCheckBoxPosColor.getValue(FpcRoot.Instance._bubble_color_type));
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------
    // pos_color
    public void Check_Pos_Color(eCheckBoxPosColor posColor)
    {
        for(int i=0; i<eCheckBoxPosColor.MAX.getValue(); i++)
        {
            _checkbox_pos_color[i].setChecked(false);
        }
        _checkbox_pos_color[posColor.getValue()].setChecked(true);
    }
    public void Check_Pos_Color(int index)
    {
        if( index <= eCheckBoxPosColor.MAX.getValue() ) return;

        for(int i=0; i<eCheckBoxPosColor.MAX.getValue(); i++)
        {
            _checkbox_pos_color[i].setChecked(false);
        }
        _checkbox_pos_color[index].setChecked(true);
    }
    public int getCheck_pos_Color()
    {
        for(int i=0; i<eCheckBoxPosColor.MAX.getValue(); i++)
        {
           if(  _checkbox_pos_color[i].isChecked() )
           {
               return i;
           }
        }
        return -1;
    }
    public void setUserPos(eCheckBoxPosColor posColor)
    {
        Check_Pos_Color(posColor);
        FpcRoot.Instance._bubble_color_type = posColor.getValue();
        FpcRoot.Instance._user_posid = posColor.getValue();
    }
    // end pos_color

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.ClientNextButton: connectToServer(); break;

            // 사용자색 선택
            case R.id.checkBox_pos_color_pink: setUserPos(eCheckBoxPosColor.PINK);   break;
            case R.id.checkBox_pos_color_red: setUserPos(eCheckBoxPosColor.RED); break;
            case R.id.checkBox_pos_color_yellow: setUserPos(eCheckBoxPosColor.YELLOW); break;
            case R.id.checkBox_pos_color_green: setUserPos(eCheckBoxPosColor.GREEN); break;
            case R.id.checkBox_pos_color_phthalogreen: setUserPos(eCheckBoxPosColor.PHTHALOGREEN); break;
            case R.id.checkBox_pos_color_blue: setUserPos(eCheckBoxPosColor.BLUE); break;
            // end 사용자색 선택
        }


    }

    boolean _onceClick_connectToServer;
    private void connectToServer()
    {
        save_client_information();

        if( MainActivity.Instance._virtualServer)
        {
            startActivity(new Intent(MainActivity.Instance, Activity_clientMain.class));
        }
        else
        {
            if( _onceClick_connectToServer == true) return;

            FpcRoot.Instance._user_name = _user_name.getText().toString();
            FpcRoot.Instance.ConnectToServer(_ipText.getText().toString());
            ChangeScenarioActivity();

            _onceClick_connectToServer = true;
        }

    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void ChangeScenarioActivity()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                while(true)
                {
                    if(FpNetFacade_client.Instance.IsConnected() && FpNetFacade_client.Instance._recv_connected_message)
                    {
                        //if( MainActivity.Instance._ready)
                        {
                            Log.i("[J2Y]", "userPosID" + FpcRoot.Instance._user_posid);
                            FpNetFacade_client.Instance.SendPacket_setUserInfo(_user_name.getText().toString(), FpcRoot.Instance._bubble_color_type, FpcRoot.Instance._user_posid);
                            startActivity(new Intent(MainActivity.Instance, Activity_clientMain.class));

//                            //server state 시나리오 선택
//                            startActivity(new Intent(MainActivity.Instance, Activity_clientMain.class));
//                            FpNetFacade_client.Instance.SendPacket_req_changeScenario(MainActivity.Instance._curServerScenario);

                            _onceClick_connectToServer = false;
                            //MainActivity.Instance._ready = false;
                            finish();
                            return;
                        }
                    }
                }

            }
        }.start();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 컬러 레디오 버튼
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



    RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {

        public void onClick(View v)
        {

            // 컬러
            for (int i = 0; i < _radio_button_colors.length; ++i)
            {

                if(_radio_button_colors[i].isChecked())
                {
                    FpcRoot.Instance._bubble_color_type = i;
                    FpcRoot.Instance._user_posid = i;
                }
            }


        }
    };
    RadioButton.OnClickListener posidOnClickListener = new RadioButton.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
//            // 포지션 아이디
//            for (int i = 0; i < _radio_button_posID.length; ++i)
//            {
//
//                if(_radio_button_posID[i].isChecked())
//                {
//                    FpcRoot.Instance._user_posid = i;
//                }
//            }
        }
    };

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 클라이언트 정보 저장/읽기
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    private  void save_client_information() {

        SharedPreferences prefs = getSharedPreferences("FamilypopClient", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ServerIP", _ipText.getText().toString());
        editor.putString("Username", _user_name.getText().toString());
        editor.putInt("bubble_color_type", FpcRoot.Instance._bubble_color_type);
        editor.putInt("user_posid", FpcRoot.Instance._user_posid);

        editor.commit();

    }
    private  void load_client_information() {

        SharedPreferences prefs = getSharedPreferences("FamilypopClient", MODE_PRIVATE);
        String text_ip = prefs.getString("ServerIP", "192.168.0.44");
        String text_username = prefs.getString("Username", "UserName");
        FpcRoot.Instance._bubble_color_type = prefs.getInt("bubble_color_type", 0);
        FpcRoot.Instance._user_posid = prefs.getInt("user_posid",0);

        _ipText.setText(text_ip);
        _user_name.setText(text_username);
    }
}