package com.j2y.familypop.server;

import processing.core.PApplet;
import shiffman.box2d.Box2DProcessing;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpsScenario_talk
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpsScenario_talk extends FpsScenario_base
{
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void OnActivated()
	{	
		
	}	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void OnDeactivated()
	{	
//		for(FpNetServer_client clinet : FpNetFacade_server.Instance._clients)
//		{
//			for(FpsBubble move : clinet._bubble)
//				move.DestroyMover();
//
//			clinet._bubble.clear();
//		}
	}
		
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void OnSetup(PApplet scPApplet, Box2DProcessing box2d) 
	{
		super.OnSetup(scPApplet, box2d);
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void OnDraw() 
	{
		
	}
}
