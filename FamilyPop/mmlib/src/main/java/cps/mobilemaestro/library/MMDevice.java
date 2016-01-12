package cps.mobilemaestro.library;

import java.io.IOException;

import android.os.Handler;

public interface MMDevice
{
	void start();
	void stop();
	boolean doMeasure(long startTime, int playId);
}

