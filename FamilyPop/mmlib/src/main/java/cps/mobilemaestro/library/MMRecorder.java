package cps.mobilemaestro.library;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MMRecorder extends Thread
{
	private int subLoop;
	private double loopBase = 10.8;
	private Handler topHandler;

	private BufferedWriter or = null;
	private long startTime;
	private int playId;

	public MMRecorder(Handler topHandler, long startTime, int playId)
	{ 
		this.topHandler = topHandler;
		this.startTime = startTime;
		this.playId = playId;
	}  
	 
    @SuppressLint("SdCardPath")
	@Override  
    public void run()
    {       	
    	int bufferSize = AudioRecord.getMinBufferSize(MMCtrlParam.recordRate, 1, AudioFormat.ENCODING_PCM_16BIT);
        int blockSize = bufferSize / 2;
    	int size, bufferReadResult, minBuf;
    	
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, MMCtrlParam.recordRate, 1, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        short[] readBuffer = new short[blockSize];        
        
        this.loopBase = 0.5f * (double)MMCtrlParam.recordRate / (double)blockSize;
        this.subLoop = (int)Math.ceil(this.loopBase * Math.ceil((double)(MMCtrlParam.recordLength)/500.f));
                
    	while(MMUtils.gettime() < startTime);
        
        try
        {	
			audioRecord.startRecording();
        	       	
        	short[] buffer = new short[subLoop * blockSize];
        	size = 0;        	
        	
        	System.out.println("Start recording.");
        			        	
	        for(int cnt = 0; cnt < subLoop; cnt++)
	        {        	
	        	bufferReadResult = audioRecord.read(readBuffer, 0, blockSize);
	        	minBuf = Math.min(bufferReadResult, blockSize); 
	        	
	        	for(int cnt2 = 0; cnt2 < minBuf; cnt2++)
	        		buffer[size + cnt2] = readBuffer[cnt2];
	        	size += minBuf;
	        }
           	audioRecord.stop();

			if(MMCtrlParam.writable) {
				this.or = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/sdcard/TTT/record_" + MMCtrlParam.order + "_" + playId + ".txt", false)));

				for (int cnt = 0; cnt < subLoop * blockSize; cnt++)
					or.write(buffer[cnt] + " \n");

				or.close();
			}

           	MMAnalyzer aThread = new MMAnalyzer(topHandler, buffer);
       		aThread.start();
           	
	        buffer = null;
           	
           	System.out.println("Finish recording.");
        }  
        catch(IllegalStateException e)
        {
        	Log.e("Recording failed", e.toString());
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
}
