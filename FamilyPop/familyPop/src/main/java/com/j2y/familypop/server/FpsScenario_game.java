package com.j2y.familypop.server;

import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.server.render.FpsGameBomb;
import com.j2y.familypop.server.render.FpsGameBubble;
import com.j2y.familypop.server.render.ProcessingImage_base;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.server.FpNetFacade_server;

import org.jbox2d.common.Vec2;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import shiffman.box2d.Box2DProcessing;
import processing.core.*;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsScenario_game(사용 X)
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsScenario_game extends FpsScenario_base
{
    private FpsGameBomb _bomb;
    private ArrayList<FpsGameBubble> _bubbles;
    private Lock _lock_bubbles = new ReentrantLock();


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

        _bubbles = new ArrayList<FpsGameBubble>();
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [메인 쓰레드]
    public void StartGame()
    {
        int clientCount = FpNetFacade_server.Instance._clients.size();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 폭탄 주위로 랜덤 버블들 생성
//    private void create_random_bubbles(int count) {
//
//        Vec2 bomb_pos = _bomb.GetScreenPosition();
//
//        for(int i = 0; i < count; ++i) {
//            FpsGameBubble bubble = new FpsGameBubble();
//
//            int x = getRandomMath(-150, 150) + (int)bomb_pos.x;
//            int y = getRandomMath(0, 300) + (int)bomb_pos.y;
//            int r = getRandomMath(50, 200);
//            int c = getRandomMath(0, 4);
//            bubble.CreateMover(_box2d, r, x, y,  c);
//
//            _bubbles.add(bubble);
//        }
//    }

    public int getRandomMath(int min, int max) {
        int range = max - min;
        int result = (int)(Math.random() * range) + min;
        return result;
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
        //create_random_bubbles(10);

        ChangeState(state_wait);
	}


	//------------------------------------------------------------------------------------------------------------------------------------------------------
    // [렌더링 쓰레드 또는 메인쓰레드]
    @Override
	public void OnDeactivated()
	{
        try {
            _lock_bubbles.lock();

            for(FpsGameBubble bubble : _bubbles)
                bubble.DestroyMover();
            if(_bomb != null) {
                _bomb.DestroyMover();
                _bomb = null;
            }
            _bubbles.clear();
        }
        finally {
            _lock_bubbles.unlock();
        }
	}


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 그리기
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void OnDraw() 
	{
		super.OnDraw();

        try {
            _lock_bubbles.lock();

            UpdateGameState();

            for(FpsGameBubble bubble : _bubbles) {

                if(null != bubble._attractor) {
                    Vec2 force = bubble._attractor.attract(bubble._body);
                    bubble.ApplyForce(force, bubble._attractor.GetAttractorPos(_box2d));
                }
                bubble.Display(_applet);
            }


            if(_bomb != null) {
                if(null != _bomb._attractor) {
                    Vec2 force = _bomb._attractor.attract(_bomb._body);
                    _bomb.ApplyForce(force, _bomb._attractor.GetAttractorPos(_box2d));
                }
                _bomb.Display(_applet);
            }


            for (FpsTalkUser user : Activity_serverMain.Instance._talk_users.values())
            {
                if(null == user._attractor)
                    continue;

                // 유저 그리기
                user._attractor.display(_applet, _box2d);
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
    public final static int state_fireBubbles = 1;
    public final static int state_fireBomb = 2;
    public final static int state_explodeBomb = 3;
    public final static int state_result = 4;

    private int _state = state_wait;
    private long _state_time;
    private long _wait_time;
    private long _explosion_time;

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public  void ChangeState(int state)
    {
        _state = state;
        _state_time = System.currentTimeMillis();

        if(_state == state_wait)
            OnDeactivated();
    }
    private long get_state_elapsedTime()
    {
        return System.currentTimeMillis() - _state_time;
    }

    private void WaitTime(long millTime)
    {
        _wait_time = System.currentTimeMillis() + millTime;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------

    public  void UpdateGameState()
    {
        if(_wait_time > System.currentTimeMillis())
            return;

        switch (_state) {

            case state_wait: {
                ChangeState(state_fireBubbles);
                WaitTime(100);

                // 160422 test
                test_fire_bubble();
            }
            break;

            case state_fireBubbles:
            {
// test back 160422
//                fire_bubble();
//                if(_bubbles.size() > 30) {
//                    ChangeState(state_fireBomb);
//                    WaitTime(1000); // 1초 대기
//                }
//                else {
//                    WaitTime(300); // 0.3초 대기
//                }


            }
            break;

            case state_fireBomb: {

                // 폭탄 발사
                if(null == _bomb) {
                    _bomb = new FpsGameBomb(_applet, _applet.width/2, _applet.height/2);
                    WaitTime(1000);
                }
                // 타겟으로 폭탄 이동
                else if(null == _bomb._attractor) {

                    _bomb.CreateMover(_box2d, 50f, _applet.width/2, _applet.height/2, 0xffffffff, 0.001f);
                    _bomb._attractor = Activity_serverMain.Instance.FindTalkUser_byId(0)._attractor;

                    // todo: 충돌시 폭발
                    WaitTime(3000);
                }
                // 폭발
                else  {

                    ExplodeBomb();
                    ChangeState(state_explodeBomb);
                }
            }
            break;

            case state_explodeBomb: {

                // todo: 일정 거리 이하이면

                if(System.currentTimeMillis() - _explosion_time > 1000) {

                    eraseExplodeBubbles();
                    ChangeState(state_result);
                }
            }
            break;

            case state_result: {

            }
            break;
        }
    }



    private void fire_bubble() {

        int userCount = Activity_serverMain.Instance.GetTalkUserCount();
        int targetUserId = getRandomMath(0, userCount);

        fire_bubble(targetUserId);
    }
    private void fire_bubble(int targetUserId)
    {

        FpsTalkUser user = Activity_serverMain.Instance.FindTalkUser_byId(targetUserId);
        if(null == user)
            return;

        FpsGameBubble bubble = new FpsGameBubble(_applet, user._attractor);

        int x = _applet.width/2;
        int y = _applet.height/2;
        int r = getRandomMath(30, 100);
        int c = getRandomMath(0, 4);
        int bubble_color = FpNetConstants.ColorArray[c];

        bubble.CreateMover(_box2d, r, x, y,  bubble_color);

        _bubbles.add(bubble);
    }
    private void test_fire_bubble()
    {
        int userCount = Activity_serverMain.Instance.GetTalkUserCount();
        int targetUserId = getRandomMath(0, userCount);

        FpsTalkUser user = Activity_serverMain.Instance.FindTalkUser_byId(targetUserId);
        if(null == user)
            return;

        int drawCount = 100;
        int addHeight = 20;
        int addWidth = 0;
        for(int i=0; i<drawCount; ++i)
        {
            FpsGameBubble bubble = new FpsGameBubble(_applet, user._attractor);

            if( addWidth > _applet.width)
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
                bubble.CreateMover(_box2d, 10, addWidth, addHeight,  bubble_color);
                // _applet.image(Activity_serverMain.Instance._image_server_righttop, addWidth, addHeight);
            }

            addWidth += 20f;
            addWidth += 1;

            _bubbles.add(bubble);
        }
    }
    private void ExplodeBomb() {

        _explosion_time = System.currentTimeMillis();

        for(FpsGameBubble bubble : _bubbles) {
            float dist = Distance(bubble.GetScreenPosition(), _bomb.GetScreenPosition());
            if(dist < 300)
                bubble.Explode(_applet, "explosive.png");
        }
        _bomb.DestroyMover();
        _bomb = null;
    }

    private void eraseExplodeBubbles() {

        ArrayList<FpsGameBubble> old_bubbles = (ArrayList<FpsGameBubble>)_bubbles.clone();

        _bubbles = new ArrayList<FpsGameBubble>();
        for(FpsGameBubble bubble : old_bubbles) {

            if(bubble._image_explosion != null)
                bubble.DestroyMover();
            else
                _bubbles.add(bubble);
        }
    }

    private float Distance(Vec2 v1, Vec2 v2)
    {
        float dx = v1.x -  v2.x;
        float dy = v1.y -  v2.y;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        return dist;
    }

}
