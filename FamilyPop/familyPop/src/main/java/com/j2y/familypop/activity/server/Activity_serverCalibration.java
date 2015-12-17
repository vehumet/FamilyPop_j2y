package com.j2y.familypop.activity.server;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.backup.Dialog_MessageBox_ok_cancel;
import com.j2y.familypop.client.FpcLocalization_Client;
import com.nclab.familypop.R;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// Activity_serverCalibration
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class Activity_serverCalibration extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener
{
    EditText _editText_height_length;
    EditText _editText_width_length;

    EditText _editText_height_length2;
    EditText _editText_width_length2;

    SeekBar _seekbar_height_length;
    SeekBar _seekbar_width_length;

    ImageButton _button_next;
    int _nextButtonCount;

    RelativeLayout _layout_calibration_0;
    RelativeLayout _layout_pointline;



    private int _min_seekbar_width_length = 84;
    private int _min_seekbar_height_length = 56;

    private int _min_seekbar_width_length_half = 42;
    private int _min_seekbar_height_length_half = 28;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //
        setContentView(R.layout.activity_calibration);

        _editText_height_length = (EditText) findViewById(R.id.editText_calibration_height_length);
        _editText_width_length = (EditText) findViewById(R.id.edittext_calibration_width_length);

        _editText_height_length2 = (EditText) findViewById(R.id.editText_calibration_height_length_2);
        _editText_width_length2 = (EditText) findViewById(R.id.edittext_calibration_width_length_2);

        _editText_height_length.setText(Integer.toString(_min_seekbar_height_length));
        _editText_width_length.setText(Integer.toString(_min_seekbar_width_length));
        _editText_height_length2.setText(Integer.toString(_min_seekbar_height_length_half));
        _editText_width_length2.setText(Integer.toString(_min_seekbar_width_length_half));

        _seekbar_height_length = (SeekBar) findViewById(R.id.seekBar_calibration_height_length);
        _seekbar_width_length = (SeekBar) findViewById(R.id.seekBar_calibration_width_length);

        //_seekbar_height_length.setOnClickListener(this);
        //_seekbar_width_length.setOnClickListener(this);
        _seekbar_height_length.setOnSeekBarChangeListener(this);
        _seekbar_width_length.setOnSeekBarChangeListener(this);

        _button_next = (ImageButton) findViewById(R.id.button_calibration_next);
        _button_next.setOnClickListener(this);

        MainActivity main = MainActivity.Instance;

        _layout_calibration_0 = (RelativeLayout) findViewById(R.id.layout_calibration_0);
        _layout_pointline = (RelativeLayout) findViewById(R.id.layout_calibration_pointline);

        int height = MainActivity.Instance._familypopSetting.getInt("_calibration_height_length", _min_seekbar_height_length);
        int width = MainActivity.Instance._familypopSetting.getInt("_calibration_width_length", _min_seekbar_width_length);
        int height2 = MainActivity.Instance._familypopSetting.getInt("_calibration_height_length2", _min_seekbar_height_length_half);
        int width2 = MainActivity.Instance._familypopSetting.getInt("_calibration_width_length2", _min_seekbar_width_length_half);

        _editText_height_length.setText(Integer.toString(height));
        _editText_width_length.setText(Integer.toString(width));
        _editText_height_length2.setText(Integer.toString(height2));
        _editText_width_length2.setText(Integer.toString(width2));

        _seekbar_height_length.setProgress(height - _min_seekbar_height_length);
        _seekbar_width_length.setProgress(width - _min_seekbar_width_length);

        _layout_pointline.setVisibility(View.VISIBLE);
        _layout_calibration_0.setVisibility(View.GONE);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.button_calibration_next:

//                if( _nextButtonCount == 0 ) {
//                    MainActivity.Instance._calibration_width_length = Integer.getInteger(_editText_width_length.getText().toString());
//                    MainActivity.Instance._calibration_height_length = Integer.getInteger(_editText_height_length.getText().toString());
//                }

                onClick_nextButton();
                break;
        }
    }

    private void onClick_nextButton()
    {

        // 0 pointline, 1 messageBox
        switch (_nextButtonCount)
        {
            case 0:
                    _layout_calibration_0.setVisibility(View.VISIBLE);
                    _layout_pointline.setVisibility(View.GONE);
                break;

            case 1:
            //-------------------------------------------------------------------------------------------------------------------

                    Dialog_MessageBox_ok_cancel msgBox = new Dialog_MessageBox_ok_cancel(this) {
                        @Override
                        protected void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);

                            _ok.setText("NEXT");
                            _cancel.setText("RENEW");
                            _content.setText("Calibration was completed." +
                                    "         Please check the location of your cellphon.");

                            _editText.setVisibility(View.GONE);
                        }

                        @Override
                        public void onClick(View v) {
                            super.onClick(v);

                            MainActivity main = MainActivity.Instance;


                            switch (v.getId()) {
                                case R.id.button_custom_dialog_ok:
                                    startActivity(new Intent(MainActivity.Instance, Activity_serverMain.class));

                                    main.SetFamilypopSettingValue_int("_calibration_height_length", Integer.parseInt(_editText_height_length.getText().toString()));
                                    main.SetFamilypopSettingValue_int("_calibration_width_length", Integer.parseInt(_editText_width_length.getText().toString()));

                                    main.SetFamilypopSettingValue_int("_calibration_height_length2", Integer.parseInt(_editText_height_length2.getText().toString()));
                                    main.SetFamilypopSettingValue_int("_calibration_width_length2", Integer.parseInt(_editText_width_length2.getText().toString()));

//                                    main.familypopSettingValue("_calibration_height_length", Integer.parseInt(_editText_height_length.getText().toString()));
//                                    main.familypopSettingValue("_calibration_width_length", Integer.parseInt(_editText_width_length.getText().toString()));
                                    cancel();
                                    break;
                                case R.id.button_custom_dialog_cancel:
                                    _layout_calibration_0.setVisibility(View.GONE);
                                    _layout_pointline.setVisibility(View.VISIBLE);
                                    cancel();
                                    break;
                            }
                        }
                    };

                    msgBox.show();
                //------------------------------------------------------------------
                break;
        }

        _nextButtonCount++;
        _nextButtonCount =_nextButtonCount % 2;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // seekbar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if( seekBar.getId() == _seekbar_height_length.getId())
        {
            _editText_height_length.setText(Integer.toString(  _min_seekbar_height_length + _seekbar_height_length.getProgress() ));
        }
        if( seekBar.getId() == _seekbar_width_length.getId() )
        {
            _editText_width_length.setText(Integer.toString( _min_seekbar_width_length + _seekbar_width_length.getProgress()));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {

    }
}
