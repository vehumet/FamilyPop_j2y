package cps.mobilemaestro.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;

public class MMAudioPlayer extends Thread
{
	private SoundPool soundPool;
	private int soundId;
	private Context context;
	private long startTime;
	private int playId;

	public MMAudioPlayer(Context context, long startTime, int playId)
	{
		this.context = context;
		this.playId = playId;
		this.startTime = startTime + MMCtrlParam.playDelay + MMCtrlParam.playInterval * playId;
	}

    @SuppressLint("SdCardPath")
	@Override  
    public void run()
    {
		soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		try {			
			//String filename = "kasami_" + MMCtrlParam.order + "_" + playId + ".wav";
			String filename = "mls_" + MMCtrlParam.order + ".wav";
			soundId = soundPool.load(context.getAssets().openFd(filename), 1);

			while(MMUtils.gettime() < startTime);

			soundPool.play(soundId, 1.f, 1.f, 1, 0, 1.f);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
