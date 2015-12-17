package cps.mobilemaestro.library;

import java.io.IOException;

import android.os.Handler;

public interface MMDevice
{
	public void start();
	public void stop();
	public boolean doMeasure(long startTime, int playId);
}

