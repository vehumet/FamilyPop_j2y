package com.j2y.familypop.server.render;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PImage;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsGameBomb
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsGameBubble extends ProcessingBody_base
{
    public FpsAttractor _attractor;

    // We need to keep track of a Body and a radius
    public ProcessingImage_base _image_explosion;
    //private PImage _disPlayImage = null;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public FpsGameBubble(PApplet applet, FpsAttractor attractor) {
        _attractor = attractor;
    }

    public void Explode(PApplet applet, String imageName)
    {
        _image_explosion = new ProcessingImage_base(applet, imageName);
        _image_explosion.SetPosition(GetScreenPosition());
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 렌더링
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void Display(PApplet applet)
    {
        if(_image_explosion != null)
            _image_explosion.Display(applet);
        else
            super.Display(applet);
    }
}
