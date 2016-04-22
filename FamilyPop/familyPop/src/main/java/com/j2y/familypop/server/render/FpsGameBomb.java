package com.j2y.familypop.server.render;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import processing.core.PApplet;
import processing.core.PImage;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsGameBomb
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsGameBomb extends ProcessingBody_base
{
    public FpsAttractor _attractor;

    // We need to keep track of a Body and a radius
    private PApplet _applet;
//    private PImage _explosionImage = null;
//    private PImage _disPlayImage = null;

    private ProcessingImage_base[] _image_buffer = new ProcessingImage_base[3 + 14]; // Bomb, Exp
    private ProcessingImage_base _image_bomb;
    private boolean _explosion;
    private long _start_time;



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public FpsGameBomb(PApplet applet, int initX, int initY)
    {
        _applet  = applet;
        _explosion = false;
        _start_time = System.currentTimeMillis();

        int index = 0;
        for(int i = 0; i < 3; ++i) {
            _image_buffer[index] = new ProcessingImage_base(applet, String.format("bomb_%d.png", i));
            _image_buffer[index].SetPosition(new Vec2(initX, initY));
            ++index;
        }

        for(int i = 0; i < 14; ++i)
        {
            _image_buffer[index] = new ProcessingImage_base(applet, String.format("explo_%d.png", i));
            _image_buffer[index].SetPosition(new Vec2(initX, initY));
            ++index;
        }


        _image_bomb = _image_buffer[0];


//        // 폭탄
//        if(_bombImage == null)
//            _bombImage = _scPApplet.loadImage("bomb.png");
//
//        // 폭발
//        if(_explosionImage == null)
//            _explosionImage = _scPApplet.loadImage("explosive.png");
//
//        // 대포
//        if(_cannonImage == null)
//            _cannonImage = _scPApplet.loadImage("cannon.png");


    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void ApplyForce(Vec2 _v, Vec2 _attPos)
    {
        super.ApplyForce(_v, _attPos);

        Vec2 vTaget = _attPos;
        Vec2 vCurBody = _parent_box2d.getBodyPixelCoord(_body);

        Vec2 vLength = new Vec2(vTaget.x -  vCurBody.x, vTaget.y -  vCurBody.y);

        if(Math.sqrt(vLength.x * vLength.x + vLength.y * vLength.y) < 90)
        {
            //_disPlayImage = _explosionImage;
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // Formula for gravitational attraction
    // We are computing this in "world" coordinates
    // No need to convert to pixels and back
    public Vec2 FindBombForce(Body bubbleBody, float force)
    {

        float g_ = 1000; // Strength of force
        // clone() makes us a copy
        Vec2 pos_ = _body.getWorldCenter();
        Vec2 pos_mover_ = bubbleBody.getWorldCenter();
        // Vector pointing from mover to attractor
        Vec2 force_ = pos_.sub(pos_mover_);
        float dist_ = force_.length();

        // Keep force within bounds
        dist_ = PApplet.constrain(dist_, 1, 2);
        force_.normalize();
        // Note the attractor's mass is 0 because it's fixed so can't use that
        float strength_ = (g_ * 1 * bubbleBody.m_mass) / (dist_ * dist_); // Calculate
        strength_ = strength_ * -force; //
        // gravitional
        // force
        // magnitude
        force_.mulLocal(strength_); // Get force vector --> magnitude *
        // direction
        return force_;
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 렌더링
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void Display(PApplet applet)
    {
        //super.Display(applet);
        long duration = System.currentTimeMillis() - _start_time;

        if(_body != null)
        {
            Vec2 pos = GetScreenPosition();

            if(_explosion) {
                int size = (int)(_rad * 4);
                _image_bomb.Display(applet, (int)pos.x, (int)pos.y, size, size);
            }
            else {


                int size = (int)(_rad * 3);
                pos.x -= (size / 2);
                pos.y -= (size / 2);

//                float angle_ = _body.getAngle();
//                applet.pushMatrix();
//                applet.translate(pos.x, pos.y);
//                applet.rotate(angle_);
//
//                float pivotX = -(float)size * 0.7f;
//                float pivotY = -(float)size * 0.7f;
//                _image_bomb.Display(applet, (int)pivotX, (int)pivotY, size, size);
//                applet.popMatrix();

                _image_bomb.Display(applet, (int)pos.x, (int)pos.y, size, size);
            }
        }

//        // We look at each _body and get its screen position
//        Vec2 pos = _parent_box2d.getBodyPixelCoord(_body);
//
//        // Get its angle of rotation
//        float angle_ = _body.getAngle();
//        _pApplet.pushMatrix();
//        _pApplet.translate(pos.x, pos.y);
//        _pApplet.rotate(angle_);
//        // _pApplet.fill(150);
//        _pApplet.noFill();
//        _pApplet.stroke(_color);
//        _pApplet.strokeWeight(3);
//
//        if(_disPlayImage != null)
//            _pApplet.image(_disPlayImage, -_bombImage.width / 2, -_disPlayImage.height / 2);
//        else
//            _pApplet.ellipse(0, 0, _rad * 2, _rad * 2);
//
//        // Let's add a line so we can see the rotation
//        // _pApplet.line(0, 0, _rad, 0);
//        _pApplet.popMatrix();
    }

    public void ChangeBombIndex(int index)
    {
        _image_bomb = _image_buffer[index];
    }

    public void ChangeExplosionIndex(int base_index)
    {
        _body.setAngularVelocity(0f);
        _explosion = true;
        //int index = 3 + (int)(weight * 14);
        _image_bomb = _image_buffer[3 + base_index];
    }




}
