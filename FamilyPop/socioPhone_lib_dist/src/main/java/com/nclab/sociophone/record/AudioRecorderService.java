package com.nclab.sociophone.record;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.nclab.sociophone.ContextAPI;
import com.nclab.sociophone.SocioPhone;
import com.nclab.sociophone.SocioPhoneConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by jsim on 16. 1. 20.
 */
public class AudioRecorderService extends Service {
    // JSIM's code START

    private ContextAPI CAPI;
    String mFilename;

    // JSIM's code END

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";

    //	private boolean isActive = false;
    private static boolean isRECToggleOn = false;
    public static String finalPath = "/sdcard/AudioRecorder/AudioREC_test";
    private boolean isActive = false;

//	private final Handler mHandler = null;

    public static boolean isRECToggleOn() {
        return isRECToggleOn;
    }
    @Override
    public int  onStartCommand(Intent intent, int flags, int startId) {
        Log.i("JSIM", "AudioRecorderService start");
        CAPI = new ContextAPI(this);

        if (intent != null && intent.getExtras() != null) {
            String sender = intent.getStringExtra("sociophone request");
            if (sender.equals("startService")) {
                mFilename = intent.getStringExtra("filename");
                CAPI.registerQuery("REC 1000 1000 0");
            }
        }
        /*
        if (intent != null && intent.getExtras() != null){
            String sender = intent.getStringExtra("sender");
            if (sender.equals("AudioRecorder")) {
                boolean isChecked = intent.getBooleanExtra("isChecked", false);

                if (isChecked) {
                    isRECToggleOn = true;
                    isActive = true;
                    CAPI.registerQuery("REC 1000 1000 0");
                }
                else{
                    isRECToggleOn = false;
                    isActive = false;
                    //			finalPath = requestPath();
                    String targetWaveFilePath = getWaveFilePath();
                    convertToWAVFile(finalPath, targetWaveFilePath);

                    sendPathToActivity(targetWaveFilePath);
                    CAPI.deregisterQuery("REC");
                    //deregisterQuery(m_strDouebleQueryIds);
                    //registeredContexts.clear();

                    unbindPartitionService();
                    this.stopService(intent);
                }
            }
            else if (sender.equals("PartitionReceiver") && isActive){

            }
        }*/
        return START_STICKY;
    }

    /**
     * Convert raw file to WAV file.
     * @param rawFilePath
     * @param waveFilePath
     */
    private void convertToWAVFile(String rawFilePath, String waveFilePath) {
        SoundRecThread recThread = new SoundRecThread(rawFilePath, waveFilePath);
        recThread.start();
    }

    /**
     * Get WAV file path.
     * @return WAV file path.
     */
    public String getWaveFilePath(){
        /*String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        Log.e("FILE", file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
        return file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV;*/
        Log.i("JSIM", mFilename + "_" + convertToHRF(System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset) + "_" + (SocioPhone.isServer ? "B" : "A") + ".wav");
        return mFilename + "_" + convertToHRF(System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset) + "_" + (SocioPhone.isServer ? "B" : "A") + ".wav";
    }

    private String convertToHRF(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        String res = (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE) + "-" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);

        return res;

    }

    /**
     * Send path info to the MainActivity.
     * @param finalPath
     */
    private final void sendPathToActivity(String finalPath) {
        Intent sendPath = new Intent("kr.ac.kaist.calab.audiorecorder.MainActivity");
        //	sendPath.setComponent(new ComponentName(
        //			"kr.ac.kaist.calab.audiorecorder",
        //			"kr.ac.kaist.calab.audiorecorder.MainActivity"));
        sendPath.setAction( "kr.ac.kaist.calab.audiorecorder.PATH_BROADCAST" );
        sendPath.putExtra("path", finalPath);
        this.sendBroadcast(sendPath);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
/*  // FIXME We don't need registerQuery, deregisterQuery
    private void registerQuery(String[] queries, List<Integer> queryIdsList)
    {
        try
        {
            // ?? ??? ???? ????.
            for (String query : queries)
            {
                // ??? ??? ???.
                final String[] queryTokens = query.split(" ");

                // ?? ??? ????.
                final String _query = "CONTEXT " + queryTokens[0] + " INTERVAL " + queryTokens[1] + " " + queryTokens[2] + " DELAY " + queryTokens[3];

                // ??? ????.
                //final int queryId = SymphonyService.getInstance().registerQuery(_query);
                final boolean isSucceeded = CAPI.registerQuery(_query);

                // ??? ????.
                //Log.d("MainActivity", "registerQuery:"+_query+" => "+ queryId);
                Log.d("MainActivity", "registerQuery:"+_query+" => "+ isSucceeded);

                // ?? ?? ?? ???? ????.
                //queryIdsList.add(queryId);
            }
        }
        catch (Exception e)
        {
            // ??? ????.
            Log.d("MainActivity", e.toString());
        }
    }

    private void deregisterQuery(List<Integer> queryIdsList)
    {
        try
        {
            // ?? ?? ?? ???? ???? ????.
            for (int queryId : queryIdsList)
            {
                // ??? ????.
                SymphonyService.getInstance().deregisterQuery(queryId);
                //CAPI.deregisterQuery("REC");

                // ??? ????.
                Log.d("MainActivity", "deregisterQuery:"+queryId);

            }

            // ?? ?? ?? ???? ?????.
            queryIdsList.clear();
        }
        catch (Exception e)
        {
            // ??? ????.
            Log.d("MainActivity", e.toString());
        }
    }
    */

    /*	private String requestPath(){
        final String path = SymphonyService.getInstance().requestPath();
        Log.e("JYSY", "final path is:"+path);
        Toast.makeText(this, "path:"+path, Toast.LENGTH_SHORT).show();

        return path;

    }
     */

/*
    private void unbindPartitionService() {
        SymphonyService.getInstance().stopLogging();

        // ??? ?? ???? ?? ??? ?????.
        SymphonyService.getInstance().setServiceConnection(null);

        // ?????? ????? ??????.
        SymphonyService.getInstance().stopService(this);

    }

    private void bindPartitionService() {
        // ?????? ?????? ???? ????? ???????.
        SymphonyService.getInstance().setServiceConnection(this);
        //	Log.e("SY", "bindPartitionService");
        // ??? ???? ????.
        SymphonyService.getInstance().startService(this);
    }
*/

    /* // FIXME We don't need below part
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        if (SymphonyService.getInstance().isBinded() == false) return;

        Log.d("MainActivity", "Services initialization completed");

//		final String[] queries = { "REC 1000 1000 0", "GetVolume 10000 16000 6000" };
        final String[] queries = { "REC 1000 1000 0" };
        //final String[] queries_getVolume = { "GetVolume 10000 16000 6000"};
        //final String[] queries1 = { "STOREDOUBLE 2000 2000 0" };
        registerQuery(queries, m_recQueryIds);
        //registeredContexts.add("REC");
        //registerQuery(queries1, m_strDouebleQueryIds);
        //registeredContexts.add("STOREDOUBLE");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // TODO Auto-generated method stub
        Log.w("JAESEONG", "CDonServiceDisconnected");
    }
*/
    @Override
    public void onDestroy() {
        String targetWaveFilePath = getWaveFilePath();
        convertToWAVFile(finalPath, targetWaveFilePath);

        sendPathToActivity(targetWaveFilePath);
        CAPI.deregisterQuery("REC");
        Intent toSend = new Intent("kr.ac.kaist.calab.audiorecorder.DESTROY");
        sendBroadcast(toSend);
        Log.i("JSIM", "AudioRecorderService destroy");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.w("JAESEONG", "CDonUnbind");
        return true;
    }

    // ???? ??? ??? ????? (Indoor Location Monitoring)

    public static List<Integer> m_recQueryIds = new ArrayList<Integer>();
    //public static List<Integer> m_strDouebleQueryIds = new ArrayList<Integer>();
    //public static List<String> registeredContexts = new ArrayList<String>();
}
