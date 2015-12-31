package com.j2y.familypop.server.render;

import com.nclab.familypop.R;

import processing.core.PImage;
import shiffman.box2d.*;

import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

import processing.core.PApplet;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsAttractor
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsAttractor
{
	// We need to keep track of a Body and a radius
	public Body body;
	float rad;
	public int _color;

    Box2DProcessing _box2d;

	private PImage _userImage = null;
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public FpsAttractor(Box2DProcessing box2d, float _rad, float _x, float _y, int tempC)
    {
        _box2d = box2d;
		_color = tempC;
		rad = _rad;

		//body.setType(BodyType.STATIC);
		// Define a _body
		BodyDef bd_ = new BodyDef();
		bd_.type = BodyType.STATIC;
		// Set its position
		bd_.position = _box2d.coordPixelsToWorld(_x, _y);
		body = _box2d.world.createBody(bd_);
    
		// Make the _body's shape a circle
		CircleShape cs_ = new CircleShape();
		cs_.m_radius = _box2d.scalarPixelsToWorld(rad);
   
		body.createFixture(cs_, 1);
	}

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public Vec2 GetPosition()
    {
        return _box2d.getBodyPixelCoord(body);
    }


	//------------------------------------------------------------------------------------------------------------------------------------------------------
	// Formula for gravitational attraction
	// We are computing this in "world" coordinates
	// No need to convert to pixels and back
	public Vec2 attract(Body bubbleBody)
	{
    
		float g_ = 1000; // Strength of force
		// clone() makes us a copy
		Vec2 pos_ = body.getWorldCenter();
		Vec2 pos_mover_ = bubbleBody.getWorldCenter();
		// Vector pointing from mover to attractor
		Vec2 force_ = pos_.sub(pos_mover_);
		float dist_ = force_.length();
    
		// Keep force within bounds
		dist_ = PApplet.constrain(dist_, 1, 2);
		force_.normalize();
		// Note the attractor's mass is 0 because it's fixed so can't use that
		float strength_ = (g_ * 1 * bubbleBody.m_mass) / (dist_ * dist_); // Calculate
                                      // gravitional
                                      // force
                                      // magnitude
		force_.mulLocal(strength_); // Get force vector --> magnitude *
		// direction
		return force_;
	}

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public Vec2 GetAttractorPos(Box2DProcessing _box2d)
    {
        return _box2d.getBodyPixelCoord(body);
    }

	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void display(PApplet _pApplet, Box2DProcessing _box2d) 
	{
		// We look at each _body and get its screen position
		Vec2 pos = _box2d.getBodyPixelCoord(body);
		// Get its angle of rotation
		float angle_ = body.getAngle();
		_pApplet.pushMatrix();

		_pApplet.translate(pos.x, pos.y);

		_pApplet.rotate(angle_);
		_pApplet.fill(_color);
		//_pApplet.stroke(0);
		_pApplet.noStroke();
		_pApplet.strokeWeight(1);
		_pApplet.ellipse(0, 0, rad * 2, rad * 2);
		_pApplet.popMatrix();

		//----------------------------------------------------------------------------------------------------------------------------------------------------
//
//		int color = getColor(_color);
//		if( color != R.color.black)
//		{
//			switch (color)
//			{
//
//			}
//			_userImage =
//
//		}
//

	}

//	private int getColor(int index)
//	{
//        /*
//        0xffff6666,     // 빨강
//		0xff66ff66,     // 녹색
//		0xffffff66,     // 노랑
//		0xff6666ff,     // 파랑
//		0xffffffff,     // 보라
//        0xff66ff66, // 임시
//        */
//
//
//		switch(index)
//		{
//			case 0: return R.color.red;
//			case 1: return R.color.green;
//			case 2: return R.color.yellow;
//			case 3: return R.color.blue;
//			case 4: return R.color.pink;
//			case 5: return R.color.grey;   //임시?
//		}
//
//		return R.color.black; // 없는 컬러
//	}
}
