package com.j2y.familypop.server;

import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.server.render.FpsBubble;
import com.j2y.familypop.server.render.FpsGameBomb;
import com.j2y.familypop.server.render.FpsGameBubble;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.server.FpNetFacade_server;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import shiffman.box2d.Box2DProcessing;

// 센서
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsFamilyBomb
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsFamilyBomb
{
    private FpsScenario_record _scenario_record;
    private FpsGameBomb _bomb;
    private int _bomb_target_index;

    private ArrayList<FpsGameBubble> _bubbles; // debug
    private Lock _lock_bubbles = new ReentrantLock();

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public int getRandomMath(int min, int max)
    {
        int range = max - min;
        int result = (int)(Math.random() * range) + min;
        return result;
    }

    public int getUserCount() {
        return Activity_serverMain.Instance.GetTalkUserCount();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 이벤트
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void Start(FpsScenario_record scenario_record)
	{
        _scenario_record = scenario_record;
        ChangeState(state_wait);

        _bubbles = new ArrayList<FpsGameBubble>();
	}
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void Destroy()
    {
        if(_bomb != null)
        {
            _bomb.DestroyMover();
            _bomb = null;
        }
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 그리기
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void Draw(PApplet applet, Box2DProcessing box2d)
	{
        if( FpsRoot.Instance._exitServer) return;

        UpdateGameState(applet, box2d);

        if(_bomb != null)
        {
            if(null != _bomb._attractor)
            {
                Vec2 force = _bomb._attractor.attract(_bomb._body);
                force.mulLocal(20f);
                _bomb.ApplyForce(force, _bomb._attractor.GetAttractorPos(box2d));
            }
            _bomb.Display(applet);
        }

        try {
            _lock_bubbles.lock();
            Activity_serverMain main = Activity_serverMain.Instance;

            for(FpsGameBubble bubble : _bubbles) {

                if(null != bubble._attractor) {
                    Vec2 force = bubble._attractor.attract(bubble._body);
                    bubble.ApplyForce(force, bubble._attractor.GetAttractorPos(main._box2d));
                }
                bubble.Display(main);
            }
        }
        finally {
            _lock_bubbles.unlock();
        }
	}



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 연출
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public final static int state_wait = 0;
    public final static int state_fireBomb = 2;
    public final static int state_explodeBomb = 3;
    public final static int state_result = 4;

    private int _state = state_wait;
    private long _wait_time;
    private long _bomb_move_time;
    private long _bomb_start_time;
    private long _bomb_duration;
    private long _explosion_start_time;
    private long _explosion_duration;

    // 센서.


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public  void ChangeState(int state)
    {
        _state = state;
    }

    private void WaitTime(long millTime)
    {
        _wait_time = System.currentTimeMillis() + millTime;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public  void UpdateGameState(PApplet applet, Box2DProcessing box2d)
    {
        if(_wait_time > System.currentTimeMillis())
            return;

        switch (_state)
        {

            case state_wait:
            {
                // 시작 메세지 출력
                Activity_serverMain.Instance.OnEvent_bomb_runningMsg(0);

                // 게임 시작을 알린다.
                FpNetFacade_server.Instance.Send_BombRunning();

                ChangeState(state_fireBomb);

                _bomb_duration = getRandomMath(12000, 25000);
                _bomb_start_time = System.currentTimeMillis();
                _bomb_target_index = getRandomMath(0, getUserCount());
                WaitTime(1000);

                // 160422 test
                test_fire_bubble();
            }
            break;

            case state_fireBomb:
            {

                // 폭탄 발사
                if(null == _bomb)
                {
                    _bomb = new FpsGameBomb(applet, applet.width/2, applet.height/2);
                    _bomb.CreateMover(box2d, 80f, applet.width/2, applet.height/2, 0xffffffff, 0.001f);
                    _bomb._body.setAngularVelocity(5);
                }

                // 폭발
                if(System.currentTimeMillis() > (_bomb_start_time + _bomb_duration))
                {
                    FpNetFacade_server.Instance.Send_endBomb();
                    ExplodeBomb();
                    ChangeState(state_explodeBomb);

                    // 클라에 결과 전송
                    FpNetFacade_server.Instance.Send_UserBang(Activity_serverMain.Instance.FindTalkUser_byId(_bomb_target_index));

                    //WaitTime(2000);
                }
                else
                {
                    long elapsedTime = System.currentTimeMillis() - _bomb_start_time;
                    int frame_bomb = (int)(((double)elapsedTime / (double) _bomb_duration) * 3);

                    _bomb.ChangeBombIndex(frame_bomb);


                    // 다음 이동 지점 찾기
                    _bomb_target_index = (_bomb_target_index + 1) % getUserCount();
                    _bomb._attractor = Activity_serverMain.Instance.FindTalkUser_byId(_bomb_target_index)._attractor;
                    WaitTime(5000);
                }
            }
            break;

            case state_explodeBomb:
            {
                // 폭발
                if(System.currentTimeMillis() > (_explosion_start_time + _explosion_duration))
                {
                    ChangeState(state_result);

                    _bomb.DestroyMover();
                    _bomb = null;
                }
                else
                {
                    long elapsedTime = System.currentTimeMillis() - _explosion_start_time;
                    int frame = (int) (((double) elapsedTime / (double) _explosion_duration) * 14);

                    _bomb.ChangeExplosionIndex(frame);
                    WaitTime(10);
                }
            }
            break;

            case state_result:
            {

                _scenario_record.End_familyBomb();
            }
            break;
        }
    }


    private void ExplodeBomb()
    {
        _explosion_start_time = System.currentTimeMillis();
        _explosion_duration = 3000;

        for (FpsTalkUser user : Activity_serverMain.Instance._talk_users.values())
        {
            if(null == user._attractor)
                continue;

            for (int i = user._bubble.size () - 1; i > -1; i--)
            {
                FpsBubble bubble = user._bubble.get(i);
                float dist = Distance(bubble.GetScreenPosition(), _bomb.GetScreenPosition());

                if((bubble._isMoving == true) && (dist < 300))
                {
                    Vec2 force = _bomb.FindBombForce(bubble.body, 100f);
                    bubble.applyForce(force);
                }
            }
        }

    }

    private float Distance(Vec2 v1, Vec2 v2)
    {
        float dx = v1.x -  v2.x;
        float dy = v1.y -  v2.y;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        return dist;
    }
    private void test_fire_bubble()
    {
        int userCount = Activity_serverMain.Instance.GetTalkUserCount();
        int targetUserId = getRandomMath(0, userCount);

        FpsTalkUser user = Activity_serverMain.Instance.FindTalkUser_byId(targetUserId);
        if(null == user)
            return;

        Activity_serverMain main = Activity_serverMain.Instance;

        int drawCount = 100;
        int addHeight = 20;
        int addWidth = 0;
        for(int i=0; i<drawCount; ++i)
        {
            FpsGameBubble bubble = new FpsGameBubble(main, user._attractor);

            if( addWidth > main.width)
            {
                addWidth = 0;

                addHeight+= 20;
                addHeight+=1;
            }
            //float _worldX = ((addWidth-(_applet.width/2))*12f)/(_applet.width/2);
            //float _worldY = ((addHeight-(_applet.height/2))*-20f)/(_applet.height/2);

            int c = getRandomMath(0, 4);
            int bubble_color = FpNetConstants.ColorArray[c];
            //if (eventCode == MotionEvent.ACTION_UP)
            {
                //_physicsWorld.addBox(_worldX, _worldY, .98f, .98f, 0f, false);
                bubble.CreateMover(main._box2d, 10, addWidth, addHeight,  bubble_color);
                // _applet.image(Activity_serverMain.Instance._image_server_righttop, addWidth, addHeight);
            }

            addWidth += 20f;
            addWidth += 1;

            _bubbles.add(bubble);
        }
    }

}
