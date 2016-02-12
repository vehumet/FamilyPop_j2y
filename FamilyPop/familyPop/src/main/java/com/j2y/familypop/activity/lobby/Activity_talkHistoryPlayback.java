package com.j2y.familypop.activity.lobby;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.BaseActivity;
import com.j2y.familypop.backup.Dialog_MessageBox_ok_cancel;
import com.j2y.familypop.backup.test_CustomSeekBar;
import com.j2y.familypop.client.FpcRoot;
import com.j2y.familypop.client.FpcTalkRecord;
import com.j2y.network.base.FpNetConstants;
import com.nclab.familypop.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by gmpguru on 2015-05-21.
 */
public class Activity_talkHistoryPlayback extends BaseActivity implements View.OnClickListener ,SeekBar.OnSeekBarChangeListener
{

    FrameLayout _layout_bubbles;
    ImageButton _button_home;
    ImageButton _imageButton_play;
    ImageView _imageview_center_bubble;


    SeekBar _seekbar_playState;

    ArrayList<BubbleButton> _bubbleButtons;

    public FpcTalkRecord _fpcTalkRecord;


    //test

    private ArrayList<ProgressItem> progressItemList;
    private ProgressItem mProgressItem;
    test_CustomSeekBar _testseekbar;
    //end test


    private MediaPlayer _media_player;
    TextView _textview_maxPlayTime;
    TextView _textview_seekbar;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i("[J2Y]", "Activity_talkHistoryPlayback:onCreate");


        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialouge_playback);

        //ui
        _bubbleButtons = new ArrayList<BubbleButton>();

        _button_home = (ImageButton) findViewById(R.id.button_playback_home);
        _button_home.setOnClickListener(this);
        ((ImageButton) findViewById(R.id.button_play_record)).setOnClickListener(this);




        // buttons test (bubbles)
        _layout_bubbles = (FrameLayout) findViewById(R.id.frame_talkhistory_playback_bubbles);

        FpcTalkRecord talk_record = FpcRoot.Instance._selected_talk_record;
        if(talk_record != null)
        {
            ((TextView) findViewById(R.id.textView_time)).setText(Activity_talkHistory.CalculatePlayTime(talk_record));
            ((TextView) findViewById(R.id.textView_name)).setText(talk_record._name);
            ((TextView) findViewById(R.id.textView_day)).setText(Activity_talkHistory.CalculateDate(talk_record));


            for (int i = 0; i < talk_record._bubbles.size(); i++)
            {
                FpcTalkRecord.Bubble item = talk_record._bubbles.get(i);
                //item._radius = 100; // 임시
                //int bubble_size = (int)(item._radius * 1.8f);
                int bubble_size = (int)(item._radius * 1.8f);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) bubble_size, (int) bubble_size);

                Button bbt = bubbleButtons_Create(item);

                FrameLayout lay = (FrameLayout) findViewById(R.id.frame_talkhistory_playback_bubbles);

                //params.setMargins(500 - (int) item._y, 500 - (int) item._x, 0, 0);
                //params.setMargins(410 + (int) item._x, 320 + (int) item._y, 0, 0);
                params.setMargins(500 + (int) item._x, 360 + (int) item._y, 0, 0);
                //params.setMargins((int)lay.getWidth()/2 + (int) item._x, (int)lay.getWidth()/2 + (int) item._y, 0, 0);

                // todo: 버블 컬러 변경
                switch (item._color)
                {
//                    case R.color.red:  bbt.setBackgroundResource (R.drawable.image_bubble_red); break;
//                    case R.color.yellow: bbt.setBackgroundResource (R.drawable.image_bubble_yellow); break;
//                    case R.color.purple: bbt.setBackgroundResource(R.drawable.image_bubble_purple); break;
//                    case R.color.dncolor: bbt.setBackgroundResource(R.drawable.image_bubble_donotcolor); break;

//                    case 0:  bbt.setBackgroundResource (R.drawable.image_bubble_pink); break;
//                    case 1: bbt.setBackgroundResource(R.drawable.image_bubble_red); break; // 녹색으로
//                    case 2: bbt.setBackgroundResource (R.drawable.image_bubble_yellow); break;
//                    case 3: bbt.setBackgroundResource(R.drawable.image_bubble_green); break;
//                    case 4: bbt.setBackgroundResource(R.drawable.image_bubble_phthalogreen); break;
//                    case 5: bbt.setBackgroundResource(R.drawable.image_bubble_blue); break;

                    case 0:  bbt.setBackgroundResource (R.drawable.image_bead_4); break;
                    case 1: bbt.setBackgroundResource(R.drawable.image_bead_0); break; // 녹색으로
                    case 2: bbt.setBackgroundResource (R.drawable.image_bead_2); break;
                    case 3: bbt.setBackgroundResource(R.drawable.image_bead_1); break;
                    case 4: bbt.setBackgroundResource(R.drawable.image_bead_5); break;
                    case 5: bbt.setBackgroundResource(R.drawable.image_bead_3); break;




                    //case 100: bbt.setBackgroundResource(R.drawable.image_bubble_red_smile); break; // smile
                    case 100: bbt.setBackgroundResource(R.drawable.image_bubble_orange_smile);
                        //params.setMargins(455 + (int) item._x, 315 + (int) item._y, 0, 0);
                        params.setMargins(400 + (int) item._x, 260 + (int) item._y, 0, 0);
                        break; // smile
                }

                Log.i("[J2Y]", String.format("[Playback][Bubble]:%f,%f", item._x, item._y));

//

                //params.setMargins(0, 0, 0, 0);
                bbt.setLayoutParams(params);
                bbt.requestLayout();
            }

            // 가운데 이미지 결정.
            _imageview_center_bubble = (ImageView) findViewById(R.id.imageView_center_bubble);
            switch (talk_record._bubble_color_type)
            {
                case 0: _imageview_center_bubble.setBackgroundResource(R.drawable.image_bead_4);   break;      // pink
                case 1: _imageview_center_bubble.setBackgroundResource(R.drawable.image_bead_0);   break;      // red
                case 2: _imageview_center_bubble.setBackgroundResource(R.drawable.image_bead_2);   break;      // yellow
                case 3: _imageview_center_bubble.setBackgroundResource(R.drawable.image_bead_1);   break;      // green
                case 4: _imageview_center_bubble.setBackgroundResource(R.drawable.image_bead_5);   break;      // phthalogreen
                case 5: _imageview_center_bubble.setBackgroundResource(R.drawable.image_bead_3);   break;      // blue
                case 6: _imageview_center_bubble.setBackgroundResource(R.drawable.image_bead_6);   break;
            }

        }
        //end buttons test (bubbles)


        // 사운드 플레이

//        try {
//            MediaPlayer mediaPlayer = new MediaPlayer();
//            FileInputStream fis = new FileInputStream(pathinlocal+FileName);
//            FileDescriptor fd = fis.getFD();
//            mediaPlayer.setDataSource(fd);
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//        } catch (IOException e) {
//            // handle exception
//            //((EditText) ((Activity) mMain).findViewById(R.id.txtStatus)).setText(e.getMessage());
//        }


        _media_player = new MediaPlayer();

        try {

            String filepath = Environment.getExternalStorageDirectory().getPath() + "/SocioPhone";

            //String wav_fullname = filepath + "/" + talk_record._filename;
            String wav_fullname = talk_record._filename;
            //Uri muri = Uri.parse(wav_fullname);

            //File files = new File(wav_fullname);
            Log.i("[J2Y]", "[play_talk_record]" + wav_fullname);

            File file = new File(wav_fullname);
            //FileInputStream fis = new FileInputStream(wav_fullname);
            FileInputStream fis = new FileInputStream(file);
            //_media_player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            _media_player.reset();
            FileDescriptor fd = fis.getFD();
            _media_player.setDataSource(fd, 0, file.length());
            //_media_player.setDataSource(MainActivity.Instance, muri);
            _media_player.prepare();
            //_media_player.getDuration();        //음악 파일의 전체 길이


        }
        catch (Exception e)
        {
            //Toast.makeText(this, "error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        create_progessBar();

        _textview_maxPlayTime = (TextView) findViewById(R.id.textView_max_playtime);
        _textview_seekbar = (TextView) findViewById(R.id.textView_seekbar);

        _textview_maxPlayTime.setText(Activity_talkHistory.Instance.CalculatePlayTime(talk_record));
        _textview_seekbar.setText("0:00:00");


        _imageButton_play = (ImageButton) findViewById(R.id.button_play_record);

        // 스마일 추가
        //addSmileEffect(0, R.drawable.scroll_smilepoint1);
        //addSmileEffect(200, R.drawable.scroll_smilepoint0);
        //addSmileEffect(400, R.drawable.scroll_smilepoint0);


    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onStop() {
        stop_talk_record();

        _seekbar_handler.cancel(true);

        super.onStop();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 이벤트
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_playback_home:
                //startActivity(new Intent(MainActivity.Instance, Activity_talkHistory.class));
                finish();
                break;

            case R.id.button_play_record:
                if(_media_player.isPlaying())
                {
                    _media_player.pause();
                    _imageButton_play.setBackgroundResource(R.drawable.button_play_over);
                }
                else
                {
                    play_talk_record();
                    _imageButton_play.setBackgroundResource(R.drawable.button_pause_over);
                }
                break;


//            case R.id.button_playback_home:
//                //startActivity(new Intent(MainActivity.Instance, Activity_talkHistory.class));
//                finish();
//                break;
        }



        for( int i=0; i<_bubbleButtons.size(); i++)
        {
            if( _bubbleButtons.get(i)._id == v.getId() )
            {
                _bubbleButtons.get(i).onClick(v);
            }
        }


    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [이벤트] 백버튼 막기
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK: {
                open_quit_dialog();
            }
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void open_quit_dialog()
    {
        Dialog_MessageBox_ok_cancel msgBox = new Dialog_MessageBox_ok_cancel(this)
        {
            @Override
            protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);

                _ok.setText("OK");
                _cancel.setText("CANCEL");
                _content.setText("Do you want to quit this dialogue?");
                _editText.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onClick(View v)
            {
                super.onClick(v);

                switch (v.getId())
                {
                    case R.id.button_custom_dialog_ok:
                        finish();
                        break;
                    case R.id.button_custom_dialog_cancel:
                        cancel();
                        break;
                }
            }
        };

        msgBox.show();
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    private void play_talk_record()
    {
        FpcTalkRecord talk_record = FpcRoot.Instance._selected_talk_record;
        if(talk_record == null)
            return;

        _media_player.start();
    }

    private void stop_talk_record() {
        if(_media_player != null)
            _media_player.stop();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //seek bar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        //mplayer.seekTo(progress);

        // 음악 파일의 전체 길이
        int time = _media_player.getDuration() * progress / 100;


        long diffSec = 0;
        long diffMin = 0;
        //
        diffSec = (time / 1000) % 60;// 초
        diffMin = (time) / 60000; // 분

        _textview_seekbar.setText(String.format("0:%02d:%02d", diffMin, diffSec));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {

        int time = _media_player.getDuration() * seekBar.getProgress() / 100;
        _media_player.seekTo(time);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 타임라인
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



    private int getColor(int clientIndex, int index)
    {
        /*
        0xffE64496,     // pink
                0xffE44742,     // red
                0xffEBEC4B,     // yellow
                0xff45D18C,     // green
                0xff47D4CD,		// phthalogreen
                0xff4D82D6,     // blue
                0xff66ff66, // 임시
        */
        if(index < 0 || index > 5)
            index = clientIndex;

        switch(index)
        {
            case 0: return R.color.pink;
            case 1: return R.color.red;
            case 2: return R.color.yellow;
            case 3: return R.color.green;
            case 4: return R.color.phthalogreen;
            case 5: return R.color.blue;
        }

        return R.color.black; // 없는 컬러
    }
    private void create_progessBar()
    {
        // test seekbar
        //_seekbar_playState = (SeekBar) findViewById(R.id.seekBar_history_voice_playstate);
        _testseekbar = (test_CustomSeekBar) findViewById(R.id.seekBar_history_voice_playstate);

        progressItemList = new ArrayList<ProgressItem>();
        ArrayList<FpcTalkRecord.Bubble> talkrecord= FpcRoot.Instance._selected_talk_record._bubbles;
        float total = FpcRoot.Instance._selected_talk_record._endTime - FpcRoot.Instance._selected_talk_record._startTime;

        float backstartTime=0;

        for( int i=0; i<talkrecord.size(); i++)
        {
            backstartTime -= talkrecord.get(i)._startTime;
            backstartTime *=-1;

            mProgressItem = new ProgressItem();
            mProgressItem.progressItemPercentage = ((backstartTime / total) * 100);

            mProgressItem.color =  getColor(i, talkrecord.get(i)._color);

            progressItemList.add(mProgressItem);

            backstartTime = talkrecord.get(i)._startTime;
        }

        _testseekbar.initData(progressItemList);
        _testseekbar.invalidate();
        _testseekbar.setOnSeekBarChangeListener(this);

        _seekbar_handler = new SeekBarHandler();

        //_seekbar_handler.execute();
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)
        {
            _seekbar_handler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            _seekbar_handler.execute();
        }
    }


    private SeekBarHandler _seekbar_handler;

    public class SeekBarHandler extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPostExecute(Void result) {
            //Log.d("##########Seek Bar Handler ################","###################Destroyed##################");
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {

            if(_media_player == null || _media_player.getDuration() == 0) return;

            int currentPosition = _media_player.getCurrentPosition();
            int duration = _media_player.getDuration();

            int progress = currentPosition * 100 / duration;

            _testseekbar.setProgress(progress);

            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            while(true) {
                try {
                    if(_media_player != null) {
                        //_media_player.isPlaying();
                    }
                    if (isCancelled())
                        break;
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //onProgressUpdate();
                publishProgress();
            }
            return null;
        }

    }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 버블 버튼
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    private Button bubbleButtons_Create(FpcTalkRecord.Bubble bubble)
    {
        BubbleButton bb = new BubbleButton(generateViewId(), _layout_bubbles,this);
        bb._bubble = bubble;
        _bubbleButtons.add(bb);
        return bb._button;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public static int generateViewId()
    {
        for(;;)
        {
            final int result = sNextGeneratedId.get();

            int newValue = result+1;
            if(newValue>0x00FFFFFF) newValue = 1;
            if(sNextGeneratedId.compareAndSet(result,newValue))
            {
                return result;
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public class  BubbleButton
    {
        Button _button;
        int _id;

        Activity _parents;
        public FpcTalkRecord.Bubble _bubble;

        public BubbleButton(int id, ViewGroup layout, Activity_talkHistoryPlayback parents )
        {
            // init
            _id = id;
            _parents = parents;

            // create button
            _button = new Button(parents);
            _button.setOnClickListener(parents);
            _button.setId(_id);
            layout.addView(_button);
        }

        public void onClick(View v)
        {
            if(_media_player == null            )
            return;

            _media_player.seekTo((int)_bubble._startTime);

            int progress = _media_player.getCurrentPosition() * 100 / _media_player.getDuration();
            _testseekbar.setProgress(progress);

            //_button.setText("hit!! :" + _id);
        }

    }

    public class ProgressItem
    {

        public int color;
        public double progressItemPercentage;
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 스마일 효과
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------


    //pos, R.drawable.image
    private void addSmileEffect(int posx , int image)
    {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(_testseekbar.getPaddingLeft() + posx, 0, 0, 0);
        //params.addRule(RelativeLayout.BELOW, R.id.layout_talkhistory_playback_bubbles);
        //params.addRule(RelativeLayout.ALIGN_LEFT, R.id.seekBar_history_voice_playstate);

        //이미지 얻어오기
        Drawable drb  = getResources().getDrawable(image);

        ImageView imgview = new ImageView(this);
        //imgview.setImageResource(image);
        imgview.setLayoutParams(params);
        imgview.setImageDrawable(drb);

        // 레이아웃 얻어오기
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_talkhistory_playback_voicestate);

        // 레이아웃에 추가
        layout.addView(imgview);
        // 갱신.
        imgview.requestLayout();
    }
}
