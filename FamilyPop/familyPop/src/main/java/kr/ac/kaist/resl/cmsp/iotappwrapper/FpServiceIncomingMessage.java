package kr.ac.kaist.resl.cmsp.iotappwrapper;

import android.util.Base64;
import com.j2y.network.base.FpNetIncomingMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sehyeon on 2015-08-02.
 */
public class FpServiceIncomingMessage extends FpNetIncomingMessage {
    public int _dataIndex;
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void FpServiceIncomingMessage()
    {
        _dataIndex = 0;
    }
    public int _type;
    public JSONArray _dataArray;
    public String thingId;

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // Read Packet

    public void setDataArray(JSONObject object) {
        try {
            _type = Integer.parseInt(object.getString(FamilyPopService.KEY_TYPE));
            // Create new array to make independent copy
            _dataArray = new JSONArray(object.getJSONArray(_type + "").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setDataArray(String objectStr) {
        try {
            JSONObject object = new JSONObject(objectStr);
            _type = Integer.parseInt(object.getString(FamilyPopService.KEY_TYPE));
            // Create new array to make independent copy
            _dataArray = new JSONArray(object.getJSONArray(_type + "").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public int ReadInt()
    {
        int toReturn = -1;
        try {
            toReturn = _dataArray.getInt(_dataIndex++);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public float ReadFloat()
    {
        float toReturn = -1;
        try {
            toReturn = (float) _dataArray.getDouble(_dataIndex++);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public String ReadString()
    {
        String toReturn = "-1";
        try {
            toReturn = _dataArray.getString(_dataIndex++);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toReturn;
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public byte[] ReadByteArray(int length)
    {
        byte[] toReturn = {};
        try {
            String encodedData = _dataArray.getString(_dataIndex++);
            toReturn = Base64.decode(encodedData, Base64.DEFAULT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toReturn;
    }
}
