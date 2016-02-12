package com.j2y.familypop.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Vec2;

import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.server.render.FpsBubble;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.base.FpNetUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;

import processing.core.PApplet;
import processing.core.PImage;
import shiffman.box2d.Box2DProcessing;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsScenario_record
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsScenario_record extends FpsScenario_base
{
    // 대화 버블
    private ArrayList<Integer> _turnDataSpeakers = new ArrayList<Integer>();
    private ArrayList<Integer> _log_turnData = new ArrayList<Integer>();
    public FpsBubble _current_bubble;
    private Lock _lock_turn_data = new ReentrantLock();
    private int _current_bubble_color_type;

    // 이미지 공유
    private PImage _shareImage = null;

    // 페밀리밤
    public FpsFamilyBomb _family_bomb;

    //			case 0: return R.color.red;
//			case 1: return R.color.green;
//			case 2: return R.color.yellow;
//			case 3: return R.color.blue;
//			case 4: return R.color.pink;
//			case 5: return R.color.grey;   //임시?

    // 유저 이미지
    public PImage _Image_red;
    public PImage _Image_green;
    public PImage _Image_yellow;
    public PImage _Image_blue;
    public PImage _Image_ping;


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [렌더링 쓰레드]
    @Override
    public void OnSetup(PApplet scPApplet, Box2DProcessing box2d)
    {
        super.OnSetup(scPApplet, box2d);
    }

    public void OnQuit()
    {


    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 게임(페밀리밤)
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void Start_familyBomb()
    {
        if( _family_bomb == null)
        {
            _family_bomb = new FpsFamilyBomb();
            _family_bomb.Start(this);
        }
    }

    public void End_familyBomb()
    {
        if(_family_bomb != null) {

            _family_bomb.Destroy();
            _family_bomb = null;
        }
    }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 이벤트
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [렌더링 쓰레드??]
	@Override
	public void OnActivated()
	{
        Log.i("[J2Y]", "[FpsScenario_record] OnActivated ");

        _turnDataSpeakers.clear();
        init_turnData_count();

        _current_bubble_color_type = -1;
        FpsRoot.Instance._socioPhone.startRecord(0, "temp");
    }

	//------------------------------------------------------------------------------------------------------------------------------------------------------
    // [렌더링 쓰레드]
    @Override
	public void OnDeactivated()
	{
        End_familyBomb();


        //FpsRoot.Instance._socioPhone.stopRecord();
        // 말풍선 모두 제거
//        for (FpsTalkUser user : Activity_serverMain.Instance._talk_users.values())
//            user.ResetMovers();
//
//        _current_bubble = null;

        ClaerBubble();

        Log.i("[J2Y]", "[FpsScenario_record] OnDeactivated ");
    }
    public void ClaerBubble()
    {
        for (FpsTalkUser user : Activity_serverMain.Instance._talk_users.values())
            user.ResetMovers();

        _current_bubble = null;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [메인쓰레드] [이벤트] 대화 턴 데이터

    private static Integer getMaxCountValue(List<Integer> myList){

        HashMap<Integer, Integer> frequencymap = new HashMap<Integer, Integer>();
        Integer maximum = 0;

        //Loop through list
        for(Integer integerObj : myList) {
            //If the hashmap contains the value then increase count
            if(frequencymap.containsKey(integerObj.intValue())) {
                frequencymap.put(integerObj.intValue(), frequencymap.get(integerObj.intValue())+1);
            }else{
                //add value with count of 1
                frequencymap.put(integerObj.intValue(), 1);
            }
        }

        //Loop through hashmap to find the maximum count
        for (int value : frequencymap.values())
        {
            if(value > maximum)
            {
                maximum = value;
            }
        }

        //Loop through hashmap to find the maximum count
        Integer keyOfMaxValue = null;
        for (int key : frequencymap.keySet())
        {
            Integer value = frequencymap.get(key);
            if(value == maximum)
            {
                keyOfMaxValue = key;
            }
        }

        return keyOfMaxValue;
        //return maximum;
    }

    public ArrayList<Integer> speakerBuffer = new ArrayList<Integer>();
    int currentSpeakerId = 0;
    //final static int SPAKER_BUF_SIZE = 7;

    @Override
    public void OnTurnDataReceived(int speakerID)
    {
        //Log.i("[J2Y]", "OnTurnDataReceived: " + speakerID[0]);
        //Log.i("[J2Y]", "ThreadID:[OnTurn]" + (int) Thread.currentThread().getId());

        synchronized (speakerBuffer)
        {
            speakerBuffer.add(0, speakerID);

            int window_size = Activity_serverMain.Instance._regulation_seekBar_2;
            if (speakerBuffer.size() > window_size)
            {
                speakerBuffer.remove(window_size);

                currentSpeakerId = getMaxCountValue(speakerBuffer);
/*
                ArrayList<Integer> testList = new ArrayList<Integer>();
                testList.add(1);
                testList.add(1);
                testList.add(0);
                testList.add(3);
                testList.add(3);
                testList.add(3);
                testList.add(3);

                Log.i("KAIST", "Test: " + (getMaxCountValue(testList) == 3));
                */

            }
        }
    }


    int previousSpeakerId = 0;
    private void process_turn_data_average(int avg_count)
    {
        int IGNORED_VALUE = avg_count;

        //Log.i("KAIST", "CurrentSpeakerId: " + currentSpeakerId);

        boolean isBubbleStarting = (previousSpeakerId == 0 || previousSpeakerId == 1 || (previousSpeakerId  != currentSpeakerId)) && (currentSpeakerId >= 2);
        boolean isBubbleGrowing = (previousSpeakerId >= 2) && (previousSpeakerId == currentSpeakerId);
        boolean isBubbleEnding = (previousSpeakerId >= 2) && (previousSpeakerId == 0 && previousSpeakerId == 1); // Condition #1
        isBubbleEnding |= (previousSpeakerId >= 2) && (previousSpeakerId != currentSpeakerId); // Condition #2
        isBubbleEnding |= (previousSpeakerId >= 2) && (_bubbleSize >= Activity_serverMain.Instance._regulation_seekBar_3); // Condition #3

        if (isBubbleStarting)
        {
            //Log.i("KAIST", "-----Bubble starts: " + currentSpeakerId + "/" + previousSpeakerId);

            // 3.1. 이전 버블 발사
            if (_current_bubble != null)
            {
                int end_time = (int) FpsRoot.Instance._socioPhone.GetRecordTime();
                _current_bubble.StartMover(end_time);
                _current_bubble = null;
            }

            FpsTalkUser user = Activity_serverMain.Instance.FindTalkUser_byId(currentSpeakerId - 2);
            if (user != null)
            {
                // 3.2. 새로운 버블 생성
                FpsBubble bubble = create_new_bubble(currentSpeakerId);
                if (bubble != null)
                {
                    user._bubble.add(bubble);
                    _current_bubble = bubble;
                }

                _current_bubble_color_type = user._bubble_color_type;
            }

        }
        else if (isBubbleGrowing)
        {
            //Log.i("KAIST", "Bubble grows: " + currentSpeakerId + "/" + previousSpeakerId);

            if (_current_bubble != null)
                _current_bubble.PlusMoverRadius(Activity_serverMain.Instance._plusMoverRadius * 0.1f);

        }
        else if (isBubbleEnding)
        {
           // Log.i("KAIST", "-----Bubble ends: " + currentSpeakerId + "/" + previousSpeakerId);

            // 3.1. 이전 버블 발사
            if (_current_bubble != null)
            {
                int end_time = (int) FpsRoot.Instance._socioPhone.GetRecordTime();
                _current_bubble.StartMover(end_time);
                _current_bubble = null;
            }
        }
        previousSpeakerId = currentSpeakerId;
    }
     /*
    @Override
    public void OnTurnDataReceived(int speakerID) {

        try {
            _lock_turn_data.lock();

            if(_family_bomb != null)
                return;

            int current_speaker = speakerID; // [??] 0은 대화 없음, 1은 서버
            // 대화 데이터 추가
            //if(current_speaker == 1) // 1은 서버
            //    return;

            _log_turnData.add(current_speaker);
            if(_log_turnData.size() > 30)
                _log_turnData.remove(0);


            if(current_speaker < s_max_user_count)
                ++_turnData_count[current_speaker];

            _turnDataSpeakers.add(current_speaker);
            _event_turnData = true;
        }
        finally {
            _lock_turn_data.unlock();
        }

    }
    */

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 대화 버블 생성
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [렌더링쓰레드] [이벤트] 대화 턴 데이터

    private void process_turn_data_realtime()
    {
        try {
            _lock_turn_data.lock();
            if (_turnDataSpeakers.size() <= 1) // 첫 대화는 무시
                return;

            int prev_speaker = _turnDataSpeakers.get(0);
            int current_speaker = _turnDataSpeakers.get(_turnDataSpeakers.size() - 1);

            if (prev_speaker != current_speaker) {

                // 1. 이전 버블 발사
                if (_current_bubble != null) {
                    int end_time = (int) FpsRoot.Instance._socioPhone.GetRecordTime();
                    _current_bubble.StartMover(end_time);
                    _current_bubble = null;
                }

                FpsTalkUser user = Activity_serverMain.Instance.FindTalkUser_byId(current_speaker - 2);
                if (user != null) {

                    // 2. 새로운 버블 생성
                    FpsBubble bubble = create_new_bubble(current_speaker);
                    if (bubble != null) {
                        user._bubble.add(bubble);
                        _current_bubble = bubble;
                    }
                }
                _turnDataSpeakers.clear();
                _turnDataSpeakers.add(current_speaker); // 마지막 정보는 추가
            } else {
                    /* todo: 버블 크기 변경 */
                if (_current_bubble != null) {
                    _current_bubble.PlusMoverRadius(0.2f);
                }

                _turnDataSpeakers.add(current_speaker);
            }
        }
        finally {
            _lock_turn_data.unlock();
        }
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [렌더링쓰레드] [이벤트] 대화 턴 데이터
    private final static int s_max_user_count = 4 + 2; // 최대 유저는 4 + 2
    private int[] _turnData_count = new int[10];
    private int _prev_speaker;
    private boolean _event_turnData = false;
    private void init_turnData_count()
    {
        //_prev_speaker = 0;
        for(int i = 0; i < _turnData_count.length; ++i)
            _turnData_count[i] = 0;
    }

    float _bubbleSize = 150.0f;
    /*
    private void process_turn_data_average(int avg_count)
    {
        try {
            _lock_turn_data.lock();

            // 버블 크기 변경
            if (_current_bubble != null)
                _current_bubble.PlusMoverRadius(0.1f);

            if(false == _event_turnData)
                return;
            _event_turnData = false;

            // 대화 정보 누적
            if (_turnDataSpeakers.size() <= avg_count)
            {

                // 버블 크기 변경
                if (_current_bubble != null)
                    _current_bubble.PlusMoverRadius(0.1f);
            }
            else {

                // 1. 가장 말을 많이 한 화자 찾기
                int current_speaker = 0;
                int max_count = 0;
                for (int i = 0; i < _turnData_count.length; ++i) {
                    if (max_count <= _turnData_count[i]) {
                        max_count = _turnData_count[i];
                        current_speaker = i;
                    }
                }

                if( _current_bubble != null)
                {
                    _bubbleSize = _current_bubble._rad;
                }
                // 2. 한사람이 계속 얘기 중
                if (_prev_speaker == current_speaker && _bubbleSize <= Activity_serverMain.Instance._regulation_seekBar_3)
                //if (_prev_speaker == current_speaker)
                {
                    // 버블 크기 변경
                    //if (_current_bubble != null)
                    //    _current_bubble.PlusMoverRadius(0.1f);

                }   // 3. 화자가 변경됨
                else
                {
                    // 3.1. 이전 버블 발사
                    if (_current_bubble != null) {
                        int end_time = (int) FpsRoot.Instance._socioPhone.GetRecordTime();
                        _current_bubble.StartMover(end_time);
                        _current_bubble = null;
                    }

                    if(current_speaker > 1) {
                        FpsTalkUser user = Activity_serverMain.Instance.FindTalkUser_byId(current_speaker - 2);
                        if (user != null) {

                            // 3.2. 새로운 버블 생성
                            FpsBubble bubble = create_new_bubble(current_speaker);
                            if (bubble != null) {
                                user._bubble.add(bubble);
                                _current_bubble = bubble;
                            }

                            _current_bubble_color_type = user._bubble_color_type;
                        }
                    }

                    _prev_speaker = current_speaker;

                }

                //init_turnData_count();
                //_turnDataSpeakers.clear();

//                int removeCount = 2;
//                for(int i=0; i<removeCount; ++i)
//                {
//                    _turnDataSpeakers.remove(0);
//                }

                int old_speaker = _turnDataSpeakers.get(0);
                if(_turnData_count[old_speaker] > 0)
                    _turnData_count[old_speaker]--;
                _turnDataSpeakers.remove(0);

            }
        }
        finally {
            _lock_turn_data.unlock();
        }
    }
    */
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 새로운 버블 생성
    public FpsBubble create_new_bubble(int current_speaker)
    {

//        if(prev_speaker <= 1) {
//            prev_speaker = current_speaker;
//            if(prev_speaker <= 1)
//                prev_speaker = 2;
//        }

        //FpsTalkUser prev_user = Activity_serverMain.Instance.FindTalkUser_byId(prev_speaker - 2);
        //int bubble_color = FpNetConstants.ColorArray[current_speaker - 2];
        if(_current_bubble_color_type == -1)
        {
            _current_bubble_color_type = Activity_serverMain.Instance.FindTalkUser_byId(current_speaker - 2)._bubble_color_type;
        }

        int bubble_color = FpNetConstants.ColorArray[_current_bubble_color_type];
        FpsBubble bubble = new FpsBubble();
        boolean res = bubble.CreateMover(_applet, _box2d, 20.0f, _applet.width/2, _applet.height/2, bubble_color, _current_bubble_color_type, FpsBubble.Type_Normal);

        _box2d.world.setAllowSleep(true);
//        _box2d.world.setSubStepping(true);
//        _box2d.world.setSleepingAllowed(true);

        if(res)
        {
            bubble._start_time = (int)FpsRoot.Instance._socioPhone.GetRecordTime();
            return bubble;
        }

        return null;
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 스마일 버블 생성
    @Override
    public void  Create_smile_bubble()
    {

        for (FpsTalkUser user : Activity_serverMain.Instance._talk_users.values())
        {
            if (user == null)
                continue;

            Create_smile_bubble(user);
//            FpsBubble bubble = new FpsBubble();
//            Vec2 bpos = new Vec2(_applet.width / 2f, _applet.height / 2f);
//            Vec2 userPos = user._attractor.GetPosition();
//            Vec2 dir = new Vec2((userPos.x - bpos.x) * 0.1f, (userPos.y - bpos.y) * 0.1f);
//            bpos.x += dir.x;
//            bpos.y += dir.y;
//
//            boolean res = bubble.CreateMover(_applet, _box2d, 80.0f, bpos.x, bpos.y, 0, 100, FpsBubble.Type_Smile);
//
//            if (res)
//            {
//                bubble._start_time = (int) FpsRoot.Instance._socioPhone.GetRecordTime();
//                bubble.StartMover(0);
//                user._bubble.add(bubble);
//            }
        }
    }
    public void Create_smile_bubble(FpsTalkUser user)
    {
        FpsBubble bubble = new FpsBubble();
        Vec2 bpos = new Vec2(_applet.width / 2f, _applet.height / 2f);
        Vec2 userPos = user._attractor.GetPosition();
        Vec2 dir = new Vec2((userPos.x - bpos.x) * 0.1f, (userPos.y - bpos.y) * 0.1f);
        bpos.x += dir.x;
        bpos.y += dir.y;

        boolean res = bubble.CreateMover(_applet, _box2d, 80.0f, bpos.x, bpos.y, 0, 100, FpsBubble.Type_Smile);

        if (res)
        {
            bubble._start_time = (int) FpsRoot.Instance._socioPhone.GetRecordTime();
            bubble.StartMover(0);
            user._bubble.add(bubble);
        }
    }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 그리기
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	//------------------------------------------------------------------------------------------------------------------------------------------------------
    // [렌더링 쓰레드]
    @Override
	public void OnDraw()
	{
		super.OnDraw();

        if( FpsRoot.Instance._exitServer) return;

        //process_turn_data_realtime();
        process_turn_data_average(Activity_serverMain.Instance._regulation_seekBar_2);

        _applet.text("printing some text to the message window!", 50, 0);

        try {
            _lock_turn_data.lock();
            int y = 0;
            for(int log : _log_turnData)
            {
                _applet.text("" + log, 150, y += 50);
            }
        }
        finally
        {
            _lock_turn_data.unlock();
        }

        if(_shareImage != null)
        {
            float aspect_ratio = (float)_shareImage.height / (float)_shareImage.width;
            float width = 600;
            float height = width * aspect_ratio;

            _applet.image(_shareImage, (_applet.width - width) / 2, (_applet.height - height) / 2, width, height);


        }

        for (FpsTalkUser user : Activity_serverMain.Instance._talk_users.values())
        {
            if(null == user._attractor)
                continue;

            // 유저 그리기
            user._attractor._color = FpNetConstants.ColorArray[user._bubble_color_type];
            user._attractor.display(_applet, _box2d);

            //Log.i("[BUBBLE]", "------------------------------ bubble ---------------------------------------");
            // 말풍선 그리기 ??
            if( user == null ) continue;            //?
            if( user._bubble == null) continue;     //?
            if( user._bubble.size() == 0 ) continue; //?

            for (int i = user._bubble.size () - 1; i > -1; i--)
            {
                if( user._bubble.size() == 0 ) break; //?
                //Log.i("[BUBBLE]", user._bubble.get(i).toString());

                // 움직여도 되는지?
                if (user._bubble.get(i)._isMoving == true)
                {
                    Vec2 force = user._attractor.attract(user._bubble.get(i).body);
                    user._bubble.get(i).applyForce(force);
                }
                user._bubble.get(i).display(_applet);
            }
            //Log.i("[BUBBLE]", "------------------------------ exit bubble ---------------------------------------");
        }

        if(_family_bomb != null) {
            _family_bomb.Draw(_applet, _box2d);
        }

	}
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 이미지 공유
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [메인쓰레드]
    public void SetShareImage(byte[] bitmapByteArray)
    {


        if(bitmapByteArray != null)
        {
            //_shareImage = null;

            Bitmap shareBitmap = FpNetUtil.ByteArrayToBitmap(bitmapByteArray);
            int nHeight = shareBitmap.getHeight();
            int nWidth = shareBitmap.getWidth();

            PImage shareImage = _applet.createImage(nWidth, nHeight, _applet.ARGB);

//            _applet.

//            for (int y = 0; y < nHeight; y++)
//            {
//                for (int x = 0; x < nWidth; x++)
//                {
//                    shareImage.pixels[y * nWidth + x] = shareBitmap.getPixel(x, y);
//                }
//            }

            int imagebuffer[] = new int[nWidth * nHeight];
            shareBitmap.getPixels(imagebuffer, 0, nWidth, 0, 0, nWidth, nHeight );
            System.arraycopy(imagebuffer, 0, shareImage.pixels, 0, nWidth * nHeight);
            shareImage.updatePixels();

            _shareImage = shareImage;
        }
        else
        {
            _shareImage = null;
        }
    }
}
