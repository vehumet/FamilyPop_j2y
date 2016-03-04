package com.nclab.sociophone.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jsim on 16. 1. 20.
 */
public class SoundRecThread extends Thread {

    public static final int HANDLER_RAW_BUFFER_DATA = 0;
    public static final int HANDLER_REC_FINISHED = 1;

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 8000;

    private int mSystemBufferSize = 0;

    public String waveFileName;
    private String rawFilePath;

    /**
     *
     * @param rawFilePath Raw file path.
     */
    public SoundRecThread(String rawFilePath, String waveFilePath){
        this.rawFilePath = rawFilePath;
        this.waveFileName = waveFilePath;
    }

    @Override
    public void run(){
        convertToWaveFile(rawFilePath ,waveFileName);
    }

    public void convertToWaveFile (String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen ;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 1;
        long byteRate = (RECORDER_BPP * RECORDER_SAMPLERATE * channels)/8;

        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        mSystemBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, audioEncoding);

        byte[] data = new byte[mSystemBufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 44;

            //AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/*
	public String getTempFilename(){
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath,AUDIO_RECORDER_FOLDER);

		if(!file.exists()){
			file.mkdirs();
		}

		File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

		if(tempFile.exists())
			tempFile.delete();

		Log.e("FILE",file.getAbsolutePath() + "/"  + AUDIO_RECORDER_TEMP_FILE);
		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);

	}*/

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        Log.e("FILE", "totalAudioLen = " + totalAudioLen);
        Log.e("FILE","totalDataLen = " + totalDataLen);
        Log.e("FILE","longSampleRate = " + longSampleRate);
        Log.e("FILE","byteRate = " + byteRate);

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
        header[32] = (byte) (RECORDER_BPP * channels / 8);
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
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


}
