package com.nclab.sociophone.record;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioRecord;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.nclab.sociophone.SocioPhone;
import com.nclab.sociophone.SocioPhoneConstants;
import com.nclab.sociophone.processors.VolumeWindow;

import java.io.File;
import java.util.Calendar;

public class RecordProcessThread
{
    private AudioRecord audioRecord;
    private int _bufferSize = 0;
    boolean _recording = true;


    boolean processing = false;
    double datab[];
    int datac;
    public boolean filter = false;
    Handler mHandler;
    int sampleRate = 8000;
    long window_size = SocioPhoneConstants.windowSize; // 300
    long nextCheckPoint = Long.MAX_VALUE;
    boolean flag;
    String mFilename;
    double _sound_amplitude;
    private BroadcastReceiver symphonyReceiver;
    Context mContext;

    //FileOutputStream os = null;
    public RecordProcessThread(Handler handler, boolean volume, String filename, Context mContext)
    {
        mHandler = handler;
        mFilename = filename;
        _bufferSize = 100 * 1024;
        this.mContext = mContext;
    }

    public void setCheckPoint(long time) {
        nextCheckPoint = time;
    }

    public void start_record()
    {
        Log.i("gulee", "Recorder Thread is running");
        BroadcastReceiver symphonyReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // back 16_02_24
                // TODO Auto-generated method stub
                String ctxName = intent.getStringExtra("context");
                String ctxVal = intent.getStringExtra("result");


                Log.d("gulee", "StartRecord context: " + ctxName + ", result: " + ctxVal);

                if (ctxName == null)
                    return;

                if (ctxName.equals("GetVolume"))
                {
                    long temp = System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset;
                    if(temp > nextCheckPoint)
                    {
                        flag = true;
                        do {
                            nextCheckPoint += window_size;
                        } while(temp > nextCheckPoint);
                    }
                    _sound_amplitude = Double.parseDouble(ctxVal);
                    if(flag)
                    {
                        Log.d("gulee", "StartRecord context: " + ctxName + ", result: " + ctxVal);
                        mHandler.obtainMessage(SocioPhoneConstants.SIGNAL_VOLUME_WINDOW, new VolumeWindow(temp, Double.parseDouble(ctxVal))).sendToTarget();
                        flag = false;
                    }
                }
            }
        };

        mContext.registerReceiver(symphonyReceiver, new IntentFilter("com.nclab.partitioning.DEFAULT"));
    }

    public void stopRecord() {
        _recording = false;
    }

    public double GetSoundAmplitue() {
        return _sound_amplitude;
    }


    //
    public String GetFilePath()
    {
        // /AudioRecorder
        String filepath = Environment.getExternalStorageDirectory().getPath() + "/SocioPhone";
        return filepath;
    }

    private String _wav_fileName = "";
    private String makeWavFileName()
    {
        _wav_fileName = mFilename + "_" + convertToHRF(System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset) + "_" + (SocioPhone.isServer ? "B" : "A") + ".wav";
        return _wav_fileName;
    }
    public String GetWavFileName()
    {
        if("" == _wav_fileName)
            makeWavFileName();
        return _wav_fileName;
    }

    public String getRawFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, "SocioPhone");

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + mFilename + "_" + convertToHRF(System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset) + "_" + (SocioPhone.isServer ? "B" : "A") + ".raw");
    }

    private String convertToHRF(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        String res = (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE) + "-" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);

        return res;

    }


    //=====================================================================================================================================================================================
    // record
    private void stopRecording()
    {
        if (null != audioRecord)
        {
            _recording = false;

            int i =  audioRecord.getState();
            if (i==1)
                audioRecord.stop();
            audioRecord.release();

            audioRecord = null;
            //recordingThread = null;
        }
//      //GetFilePath(), GetWavFileName()
        //copyWaveFile(getTempFilename(), getFilename());
        deleteTempFile();
    }

    // file
    private String getTempFilename()
    {
        //String filepath = Environment.getExternalStorageDirectory().getPath();
        String filepath = Environment.getExternalStorageDirectory().getPath() + "/SocioPhone";
        //String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //String filepath =   getFilesDir().getAbsolutePath();

        File file = new File(filepath,"AudioRecorder");

        if (!file.exists())
        {
            file.mkdirs();
        }

        File tempFile = new File(filepath,"record_temp.raw");

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + "record_temp.raw");
    }
    private String getFilename()
    {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,"AudioRecorder");

        if (!file.exists()) {
            file.mkdirs();
        }

        //return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav");
        return (file.getAbsolutePath() + "/" + GetWavFileName() + ".wav");
    }
    private void deleteTempFile()
    {
        File file = new File(getTempFilename());
        file.delete();
    }
//=====================================================================================================================================================================================
}


