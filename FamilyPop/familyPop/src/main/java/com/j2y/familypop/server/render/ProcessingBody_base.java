package com.j2y.familypop.server.render;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import processing.core.PApplet;
import shiffman.box2d.Box2DProcessing;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsBubble
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class ProcessingBody_base
{
    protected Box2DProcessing _parent_box2d;
    public Body _body;
    protected BodyDef _bodyDef;
    protected int _color;
    protected Fixture _fixture;
    public float _rad;

    public boolean _isMoving;   // 어트랙터로 움직일지 여부
    public int _start_time;
    public int _end_time;



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 속성
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public Vec2 GetScreenPosition()
    {
        return _parent_box2d.getBodyPixelCoord(_body);
    }
    public Vec2 GetWorldPosition()
    {
        return _body.getWorldCenter();
    }

    public void SetPosition(Vec2 pos)
    {
        _bodyDef.position.set(pos);
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public boolean CreateMover(Box2DProcessing box2d, float rad, float x, float y, int color, float density)
    {
        try
        {
            _parent_box2d = box2d;
            _color = color;
            _rad = rad;

            // Define a _body
            _bodyDef = new BodyDef();
            _bodyDef.type = BodyType.DYNAMIC;

            // Set its position
            _bodyDef.position = _parent_box2d.coordPixelsToWorld(x, y);
            _body = _parent_box2d.world.createBody(_bodyDef);

            // Make the _body's shape a circle
            CircleShape cs_ = new CircleShape();
            cs_.m_radius = _parent_box2d.scalarPixelsToWorld(this._rad);
            //cs_.computeMass();
            // Define a fixture
            FixtureDef fd_ = new FixtureDef();
            fd_.shape = cs_;

            fd_.density = density;
            fd_.friction = 1;
            fd_.restitution = 0.0000001f;

            _fixture = _body.createFixture(fd_);
            _body.setLinearVelocity(new Vec2(0, 0));
            _isMoving = false;
            _body.setActive(true);
        }
        catch (NullPointerException e)
        {
            return false;
        }
        return true;
    }
    public boolean CreateMover(Box2DProcessing box2d, float rad, float x, float y, int color)
    {
        return CreateMover(box2d, rad, x, y, color, 0.0001f);
    }

    public void DestroyMover()
    {
        _body.destroyFixture(_fixture);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 렌더링
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    public void StartMover(int record_end_time)
    {
        _isMoving = true;
        _body.setActive(true);
        _end_time = record_end_time;
    }
	
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void PlusMoverRadius(float rad)
	{
        if(null == _parent_box2d || null == _body)
            return;

        if(_rad > 150f)
            return;
        _rad += rad;
        Fixture ft = _body.getFixtureList();
        if (ft.getShape() != null)
            ft.getShape().m_radius += _parent_box2d.scalarPixelsToWorld(rad);
	}
	  
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void ApplyForce(Vec2 _v, Vec2 _attPos)
	{
		_body.applyForce(_v, _body.getWorldCenter());
	}


	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void Display(PApplet _pApplet)
	{
        if(null == _body)
            return;
	    // We look at each _body and get its screen position
		Vec2 pos = _parent_box2d.getBodyPixelCoord(_body);
	    
		// Get its angle of rotation
	    float angle_ = _body.getAngle();
	    _pApplet.pushMatrix();
	    _pApplet.translate(pos.x, pos.y);
	    _pApplet.rotate(angle_);
	    // _pApplet.fill(150);
	    _pApplet.noFill();
	    _pApplet.stroke(_color);
        _pApplet.strokeWeight(3);
        _pApplet.ellipse(0, 0, _rad * 2, _rad * 2);
	    
	    // Let's add a line so we can see the rotation
	    // _pApplet.line(0, 0, _rad, 0);
	    _pApplet.popMatrix();
	  }
	}
