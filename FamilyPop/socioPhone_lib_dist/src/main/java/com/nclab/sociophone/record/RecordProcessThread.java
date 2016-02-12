package com.nclab.sociophone.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.nclab.sociophone.SocioPhone;
import com.nclab.sociophone.SocioPhoneConstants;
import com.nclab.sociophone.processors.VolumeWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_WORLD_READABLE;

public class RecordProcessThread extends Thread
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
    long window_size = SocioPhoneConstants.windowSize;
    long nextCheckPoint = Long.MAX_VALUE;
    // Volume
    boolean volume;
    String mFilename;
    double _sound_amplitude;


    //FileOutputStream os = null;
    public RecordProcessThread(Handler handler, boolean volume, String filename)
    {
        mHandler = handler;
        mFilename = filename;

        //_bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)*3;
        _bufferSize = 100 * 1024;
//        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, MMCtrlParam.recordRate, 1, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 100 * 1024); // 100KB Buffer

        this.volume = volume;
    }

    public void setCheckPoint(long time) {
        nextCheckPoint = time;
    }

    public void run()
    {
        super.run();
        Log.i("MYTAG", "Recorder Thread is running");
        while (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mHandler.obtainMessage(999, e).sendToTarget();
            }
        }

        audioRecord.startRecording();

        long size = 0;
        int received = 0;
        long sum = 0;
        int buffer_size = 2000;
        short[] buffer = new short[buffer_size];
        //ArrayList<Short> wav_buffer = new ArrayList<Short>(); // back 151222 memleak
        boolean flag = false;
        boolean warm = false;

        //=====
        // recording
        byte data[] = new byte[_bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(filename);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        int read = 0;



        while (_recording)
        {
            // If it is recording state, read from buffer, and write to file
//			if(os == null && warm) {
//        		try {
//        			Log.i("MYTAG", "init file!");
//        			os = new FileOutputStream(getRawFilename());
//        		}
//        		catch(Exception e){
//        			e.printStackTrace();
//        			mHandler.obtainMessage(SocioPhoneConstants.DISPLAY_LOG, "Error on file initialization");
//        		}
//        	}

            // back 151222
            //received = audioRecord.read(buffer, 0, buffer_size);
//            for (int i = 0; i < received; ++i)
//                wav_buffer.add(buffer[i]);
            //writeAudioDataToFile();
            // ========================================================================
            received = audioRecord.read(buffer, 0, buffer_size);
            if( read>0)
            {
            }
            if( AudioRecord.ERROR_INVALID_OPERATION != read )
            {
                try
                {
                    for( int i=0; i<buffer.length; i++)
                    {
                        os.write(shortToByte(buffer[i]));
                    }
                    //os.write(shortToByte(buffer[0]));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }


            //========================================================================
            int averageSoundLevel = 0;
            for (int i = 0; i < received; i++)
            {
                //averageSoundLevel += Math.abs(buffer[i]);
                averageSoundLevel += Math.abs(buffer[i]);
            }
            if( averageSoundLevel == 0 ||
                received == 0 )
            {
                float error = 0;
                error++;
            }
            averageSoundLevel /= received;
            _sound_amplitude = (averageSoundLevel / 1);
            //int amplitude = (buffer[0] & 0xff) << 8 | buffer[1];
            //_sound_amplitude = 20 * Math.log10((double)Math.abs(amplitude) / 32768);


//			try {
//				if(os != null && warm) {
//                    byte[] buffer_bytes = shortToBytes(buffer, received);
//                    os.write(buffer_bytes, 0, buffer_bytes.length);
//                }
//			} catch (IOException e) {
//				e.printStackTrace();
//				mHandler.obtainMessage(SocioPhoneConstants.DISPLAY_LOG, "Error on file writting");
//			}
            long temp = System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset;
            if (temp > nextCheckPoint)
            {
                flag = true;
                do {
                    nextCheckPoint += window_size;
                }
                while (temp > nextCheckPoint);
            }
            size += received;

            //window process
            if (!volume)
            {
                continue;
            }
            else if (warm)
            {
                for (int i = 0; i < received; i++)
                {
                    int intensity = buffer[i];
                    sum += intensity * intensity;
                }
            }
            if (flag)
            {
                //double decibel = (10 * Math.log10(sum/(double)size));
                double decibel = sum / (double) size;
                if (warm)
                {
                    if (filter)
                    {
                        //decibel = vFilter.getAmplitude(datab, datac) / (double) size;
                    }
                    mHandler.obtainMessage(SocioPhoneConstants.SIGNAL_VOLUME_WINDOW, new VolumeWindow(nextCheckPoint - window_size, decibel)).sendToTarget();
                }
                size = 0;
                sum = 0;
                warm = true;
                flag = false;
                datac = 0;
                //Log.i("MYTAG", "Recorder Thread is warmed up");
            }
        }

        try
        {
            os.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
//		if(os != null) {
//			try {
//				os.flush();
//				os.close();
//
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;



        // back 151222 memleak
        // wav 파일 저장
//        short[] wav_buffer_array = new short[wav_buffer.size()];
//
//        for (int i = 0; i < wav_buffer_array.length; ++i)
//        {
//            wav_buffer_array[i] = wav_buffer.get(i);
//        }
//
//        Wave waveFile = new Wave(sampleRate, (short) 1, wav_buffer_array, 0, wav_buffer.size() - 1);
//
//        waveFile.wroteToFile(GetFilePath(), GetWavFileName());

        stopRecording();
    }

    public byte[] shortToBytes(short[] shortsA, int sizeInShorts)
    {
        byte[] bytes2 = new byte[sizeInShorts * 2];
        ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortsA);
        return bytes2;
    }


    public void stopRecord() {
        _recording = false;
    }

    public double GetSoundAmplitue() {
        return _sound_amplitude;
    }


    public String GetFilePath()
    {

        String filepath = Environment.getExternalStorageDirectory().getPath() + "/SocioPhone";

        return filepath;
    }

    private String _wav_fileName = "";
    private String makeWavFileName()
    {

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, "SocioPhone");

        if (!file.exists())
        {
            file.mkdirs();
        }

        _wav_fileName = file.getAbsolutePath() + "/" + mFilename + "_" + convertToHRF(System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset) + "_" + (SocioPhone.isServer ? "B" : "A") + ".wav";
        return _wav_fileName;

        //back
//        _wav_fileName = mFilename + "_" + convertToHRF(System.currentTimeMillis() + SocioPhoneConstants.deviceTimeOffset) + "_" + (SocioPhone.isServer ? "B" : "A") + ".wav";
//        return _wav_fileName;
    }
    public String GetWavFileName()
    {
        if("" == _wav_fileName)
            makeWavFileName();
        return _wav_fileName;
    }

    public String getRawFilename()
    {
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


    private class Wave {
        private final int LONGINT = 4;
        private final int SMALLINT = 2;
        private final int INTEGER = 4;
        private final int ID_STRING_SIZE = 4;
        private final int WAV_RIFF_SIZE = LONGINT + ID_STRING_SIZE;
        private final int WAV_FMT_SIZE = (4 * SMALLINT) + (INTEGER * 2) + LONGINT + ID_STRING_SIZE;
        private final int WAV_DATA_SIZE = ID_STRING_SIZE + LONGINT;
        private final int WAV_HDR_SIZE = WAV_RIFF_SIZE + ID_STRING_SIZE + WAV_FMT_SIZE + WAV_DATA_SIZE;
        private final short PCM = 1;
        private final int SAMPLE_SIZE = 2;
        int cursor, nSamples;
        byte[] output;

        public Wave(int sampleRate, short nChannels, short[] data, int start, int end) {
            nSamples = end - start + 1;
            cursor = 0;
            output = new byte[nSamples * SMALLINT + WAV_HDR_SIZE];
            buildHeader(sampleRate, nChannels);
            writeData(data, start, end);
        }

        // ------------------------------------------------------------
        private void buildHeader(int sampleRate, short nChannels) {
            write("RIFF");
            write(output.length);
            write("WAVE");
            writeFormat(sampleRate, nChannels);
        }

        // ------------------------------------------------------------
        public void writeFormat(int sampleRate, short nChannels) {
            write("fmt ");
            write(WAV_FMT_SIZE - WAV_DATA_SIZE);
            write(PCM);
            write(nChannels);
            write(sampleRate);
            write(nChannels * sampleRate * SAMPLE_SIZE);
            write((short) (nChannels * SAMPLE_SIZE));
            write((short) 16);
        }

        // ------------------------------------------------------------
        public void writeData(short[] data, int start, int end) {
            write("data");
            write(nSamples * SMALLINT);
            for (int i = start; i <= end; write(data[i++])) ;
        }

        // ------------------------------------------------------------
        private void write(byte b) {
            output[cursor++] = b;
        }

        // ------------------------------------------------------------
        private void write(String id) {
            if (id.length() != ID_STRING_SIZE)
                Log.i("[J2Y]", "String " + id + " must have four characters.");
            else {
                for (int i = 0; i < ID_STRING_SIZE; ++i) write((byte) id.charAt(i));
            }
        }

        // ------------------------------------------------------------
        private void write(int i) {
            write((byte) (i & 0xFF));
            i >>= 8;
            write((byte) (i & 0xFF));
            i >>= 8;
            write((byte) (i & 0xFF));
            i >>= 8;
            write((byte) (i & 0xFF));
        }

        // ------------------------------------------------------------
        private void write(short i) {
            write((byte) (i & 0xFF));
            i >>= 8;
            write((byte) (i & 0xFF));
        }

        // ------------------------------------------------------------
        public boolean wroteToFile(String str_path, String filename) {
            boolean ok = false;

            try {

                //File path = new File(str_path, filename);
                File dir = makeDirectory(str_path);
                File file = makeFile(dir, str_path+"/"+filename);

                FileOutputStream outFile = new FileOutputStream(file);

                outFile.write(output);
                outFile.close();


                ok = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ok = false;
            } catch (IOException e) {
                ok = false;
                e.printStackTrace();
            }
            return ok;
        }
        //파일 관리
        // 디렉토리 생성
        private File makeDirectory(String dirPath)
        {
            File dir = new File(dirPath);
            if( !dir.exists())
            {
                dir.mkdirs();
                //dir.createNewFile();
            }

            return dir;
        }
        //
        private File makeFile(File dir, String filePath)
        {
            File file = null;

            if( dir.isDirectory())
            {
                file = new File(filePath);
                if(file!=null&&!file.exists())
                {

                    try
                    {
                        file.createNewFile();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {

                    }
                }
                else
                {

                }
            }
            return file;

        }
    }
    //=====================================================================================================================================================================================
    // record
    private void writeAudioDataToFile()
    {
        byte data[] = new byte[_bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try
        {
            os = new FileOutputStream(filename);
            //String path = getFilesDir()+"/"+AUDIO_RECORDER_TEMP_FILE;
            //os = new FileOutputStream(path);
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;
        if (null != os)
        {
            while(_recording)
            {
                read = audioRecord.read(data, 0, _bufferSize);
                if (read > 0)
                {
                }

                if (AudioRecord.ERROR_INVALID_OPERATION != read)
                {
                    try
                    {
                        os.write(data);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            try
            {
                os.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
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
        copyWaveFile(getTempFilename(), GetWavFileName());
        deleteTempFile();
    }
    private void copyWaveFile(String inFilename,String outFilename)
    {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = 44100;
        int channels = 2;
        long byteRate = 16 * 44100 * channels/8;

        byte[] data = new byte[_bufferSize];

        try
        {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            //Toast.makeText(getApplicationContext(), "File size: " + totalDataLen, Toast.LENGTH_SHORT).show();
            //AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1)
            {
                out.write(data);
            }

            in.close();
            out.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException
    {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
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
    public static final short byteToShort(byte[] buffer){
        return byteToShort(buffer, 0);
    }
    public static final short byteToShort(byte[] buffer, int offset){
        return (short) ( (buffer[offset+1]&0xff)<<8 | (buffer[offset]&0xff) );
    }
    public static final int byte1ToInt(byte b){
        return (int)(b&0xff);
    }
    public static final int byte1ToInt(byte[] b, int offset){
        return (int)(b[offset]&0xff);
    }
    public static final int byte2ToInt(byte[] buffer, int offset){
        return (buffer[offset+1]&0xff)<<8 | (buffer[offset]&0xff);
    }
    public static final int byteToInt(byte[] buffer){
        return byteToInt(buffer, 0);
    }
    public static final int byteToInt(byte[] buffer, int offset){
        return (buffer[offset+3]&0xff)<<24 | (buffer[offset+2]&0xff)<<16 | (buffer[offset+1]&0xff)<<8 | (buffer[offset]&0xff);
    }
    public static final byte[] shortToByte( short s )
    {
        byte[] dest = new byte[2];
        dest[1] = (byte)(s & 0xff);
        dest[0] = (byte)((s>>8) & 0xff);
        return dest;
    }
//=====================================================================================================================================================================================
}


