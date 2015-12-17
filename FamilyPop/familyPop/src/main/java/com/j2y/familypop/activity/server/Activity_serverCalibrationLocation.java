package com.j2y.familypop.activity.server;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.j2y.familypop.MainActivity;
import com.nclab.familypop.R;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// Activity_serverCalibrationLocation
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class Activity_serverCalibrationLocation extends Activity implements View.OnClickListener
{
    public enum eLocationButtons
    {
        NON(-1),LEFT(0), TOP(1), RIGHT(2), BOTTOM(3),MAX(4);

        private int value;
        private eLocationButtons(int i){value = i;}

        public int getValue(){return value;}
        public String getValuetoString(int value)
        {
            return getValue(value).toString();
        }
        public boolean isEmpty(){return equals(eLocationButtons.NON);}
        public boolean Compare(int i){return value == i;}
        public static eLocationButtons getValue(int value)
        {
            eLocationButtons[] as = eLocationButtons.values();
            for( int i=0; i<as.length; i++)
            {
                if( as[i].Compare(value))
                {
                    return as[i];
                }
            }

            return eLocationButtons.NON;
        }

    }


    Button[] _button_location;
    ImageButton _button_next;

    eLocationButtons _selectLocationButton;
    boolean _selectServer;

    int _click_count; // LocationButton

    Location[] _location_info;

    //Location _location_server;
    //Location _location_locator;
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //

        setContentView(R.layout.activity_calibration_location);

        //ui
        _button_next = (ImageButton) findViewById(R.id.button_calibrationlocation_next);
        _button_next.setOnClickListener(this);

        _button_location = new Button[eLocationButtons.MAX.getValue()];
        _button_location[eLocationButtons.LEFT.getValue()] = (Button) findViewById(R.id.button_calibration_location_left);
        _button_location[eLocationButtons.TOP.getValue()] = (Button) findViewById(R.id.button_calibration_location_top);
        _button_location[eLocationButtons.BOTTOM.getValue()] = (Button) findViewById(R.id.button_calibration_location_bottom);
        _button_location[eLocationButtons.RIGHT.getValue()] = (Button) findViewById(R.id.button_calibration_location_right);

        _button_location[eLocationButtons.LEFT.getValue()].setOnClickListener(this);
        _button_location[eLocationButtons.TOP.getValue()].setOnClickListener(this);
        _button_location[eLocationButtons.BOTTOM.getValue()].setOnClickListener(this);
        _button_location[eLocationButtons.RIGHT.getValue()].setOnClickListener(this);



        _selectLocationButton = eLocationButtons.NON;
        _selectServer = false;

        _click_count = 0;

        //
        _location_info = new Location[eLocationButtons.MAX.getValue()];

        _location_info[eLocationButtons.LEFT.getValue()] = new Location(eLocationButtons.LEFT);
        _location_info[eLocationButtons.TOP.getValue()] = new Location(eLocationButtons.TOP);
        _location_info[eLocationButtons.RIGHT.getValue()] = new Location(eLocationButtons.RIGHT);
        _location_info[eLocationButtons.BOTTOM.getValue()] = new Location(eLocationButtons.BOTTOM);


        locationInfo_init();
        locationInfo_load();
        update_location();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v)
    {
//        if( v.getId() == R.id.button_calibrationlocation_next)
//        {
//            startActivity( new Intent(MainActivity.Instance, Activity_serverCalibration.class));
//        }

        switch (v.getId())
        {
            case R.id.button_calibrationlocation_next:
                locationInfo_save();
                startActivity( new Intent(MainActivity.Instance, Activity_serverCalibration.class));
                break;

            case R.id.button_calibration_location_left:
                setLocationButtonState(eLocationButtons.LEFT, 1);
                break;
            case R.id.button_calibration_location_top:
                setLocationButtonState(eLocationButtons.TOP, 1);
                break;
            case R.id.button_calibration_location_bottom:
                setLocationButtonState(eLocationButtons.BOTTOM, 1);
                break;
            case R.id.button_calibration_location_right:
                setLocationButtonState(eLocationButtons.RIGHT, 1);
                break;
        }

        update_location();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 0 idle, 1 select, 2 active
    private void setLocationButtonState(eLocationButtons ebutton, int state)
    {
        if( _click_count >= 2 )
        {
            _click_count = 0;
            _selectLocationButton = eLocationButtons.NON;
            _selectServer = false;
            locationInfo_init();
            return;
        }

        String str;
        if( !_selectServer)
        {
            _selectServer = true;
            str = "SERVER";
        }
        else
        {
            str = "LOCATOR";
        }

        _location_info[ebutton.getValue()]._state = state;
        _location_info[ebutton.getValue()]._title = str;


        if(_selectLocationButton != eLocationButtons.NON)
        {
            _location_info[_selectLocationButton.getValue()]._state = 2;
        }


        _selectLocationButton = ebutton;
        _click_count++;
    }


    private void update_location()
    {
        for( int i=0; i<eLocationButtons.MAX.getValue(); i++)
        {
           // if(_selectLocationButton != eLocationButtons.NON)  _button_location[_selectLocationButton.getValue()].setBackgroundResource(R.drawable.button_box_active);

            switch (_location_info[i]._state)
            {
                case 0: //idel
                    _button_location[_location_info[i]._pos.getValue()].setText(_location_info[i]._title);
                    _button_location[_location_info[i]._pos.getValue()].setBackgroundResource(R.drawable.button_box_none);
                    break;
                case 1: // select
                    _button_location[_location_info[i]._pos.getValue()].setText(_location_info[i]._title);
                    _button_location[_location_info[i]._pos.getValue()].setBackgroundResource(R.drawable.button_box_select);
                    break;
                case 2: // active
                    _button_location[_location_info[i]._pos.getValue()].setText(_location_info[i]._title);
                    _button_location[_location_info[i]._pos.getValue()].setBackgroundResource(R.drawable.button_box_active);
                    break;
            }
        }
    }
    private void locationInfo_save()
    {
        MainActivity main = MainActivity.Instance;

        SharedPreferences.Editor editor  = main._familypopSetting.edit();
        editor.putBoolean("location_info_save", true);
        editor.commit();
        //main.familypopSettingValue("location_info_save",)

        for( int i=0; i<eLocationButtons.MAX.getValue(); i++)
        {
            //_location_info[i]
            String suffix = _location_info[i]._pos.getValuetoString(_location_info[i]._pos.getValue());

//            main.familypopSettingValue("location_info_pos"+ suffix, _location_info[i]._pos.getValue());
//            main.familypopSettingValue("location_info_state"+suffix, _location_info[i]._state);
//            main.familypopSettingValue("location_info_title"+suffix, _location_info[i]._title);

            main.SetFamilypopSettingValue_int("location_info_pos"+ suffix, _location_info[i]._pos.getValue());
            main.SetFamilypopSettingValue_int("location_info_state"+suffix, _location_info[i]._state);
            main.familypopSettingValue("location_info_title" + suffix, _location_info[i]._title);

            //main.familypopSettingValue("location_info_title", _location_info[i]._title);
        }
    }
    private void locationInfo_load()
    {
        MainActivity main = MainActivity.Instance;

        Boolean save = main._familypopSetting.getBoolean("location_info_save",false);

        if( save )
        {


            for (int i = 0; i < eLocationButtons.MAX.getValue(); i++) {
                String suffix = _location_info[i]._pos.getValuetoString(_location_info[i]._pos.getValue());
                eLocationButtons index = eLocationButtons.getValue(i);

//                _location_info[index.getValue()]._pos = eLocationButtons.getValue(main.familypopSettingValue("location_info_pos" + suffix, 0));
//                _location_info[index.getValue()]._state = main.familypopSettingValue("location_info_state" + suffix, 0);

                _location_info[index.getValue()]._pos = eLocationButtons.getValue(main.GetFamilypopSettingValue_int("location_info_pos" + suffix));
                _location_info[index.getValue()]._state = main.GetFamilypopSettingValue_int( "location_info_state" + suffix);

                _location_info[index.getValue()]._title = main.familypopSettingValue("location_info_title" + suffix, null);
            }
        }

        _click_count = 2;
    }
    private void locationInfo_init()
    {
        for( int i=0; i<eLocationButtons.MAX.getValue(); i++)
        {
            _location_info[i]._state = 0;
            _location_info[i]._title = "";
        }

        _click_count = 0;
        _selectLocationButton = eLocationButtons.NON;
        _selectServer = false;
    }

    class Location
    {
        // 0 idle, 1 select, 2 active
        public int _state;
        public String _title;

        //
        public eLocationButtons _pos;

        public Location(eLocationButtons pos)
        {
            _state = 0;
            _pos = pos;
            _title = "";
        }
    }

}