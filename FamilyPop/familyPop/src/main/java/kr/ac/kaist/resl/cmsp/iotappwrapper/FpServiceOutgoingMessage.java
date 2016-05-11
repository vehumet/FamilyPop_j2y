package kr.ac.kaist.resl.cmsp.iotappwrapper;

import android.util.Base64;
import com.j2y.network.base.FpNetOutgoingMessage;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Sehyeon on 2015-08-06.
 */
public class FpServiceOutgoingMessage extends FpNetOutgoingMessage {
    public JSONArray _dataArray;

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public FpServiceOutgoingMessage()
    {
        _dataArray = new JSONArray();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void Write(int value)
    {
        _dataArray.put(value);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void Write(float value)
    {
        try {
            _dataArray.put((double) value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void Write(String text)  {
        _dataArray.put(text);
    }



    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void Write(byte[] byteArray)
    {
        String encodedData = Base64.encodeToString(byteArray, Base64.DEFAULT);
        _dataArray.put(encodedData);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void AddPacketData(byte[] valueArray)
    {
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public int GetPacketSize()
    {
        return _dataArray.length();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public byte[] GetPacketToByte()
    {
        return null;
    }

}
