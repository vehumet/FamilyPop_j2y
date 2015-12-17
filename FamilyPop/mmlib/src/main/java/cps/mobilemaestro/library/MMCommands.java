package cps.mobilemaestro.library;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

class MMCommands
{	
	public JSONObject jsonCmd = null;
	public boolean needAnswer = false;
	public Socket socket;
	
	MMCommands()
	{
		jsonCmd = new JSONObject();
	}
	
	MMCommands(JSONObject obj)
	{
		this.jsonCmd = obj;
	}
	
	String getCmdName() throws JSONException
	{
		return jsonCmd.getString("command");
	}
	
	void setCmdName(String cmdName) throws JSONException
	{
		jsonCmd.put("command", cmdName);
	}
		
	void beConnectCmd(String id, String ipAddr, boolean isLocator) throws JSONException
	{			
		needAnswer = true;
		setCmdName("connectCmd");
		jsonCmd.put("id", id);
		jsonCmd.put("ipAddr", ipAddr);
        jsonCmd.put("isLocator", isLocator);
	}
	
	void beConnectAns(boolean result) throws JSONException
	{				
		setCmdName("connectAns");
		jsonCmd.put("result", result);
	}
	
	void beDisconnectStoCCmd() throws JSONException
	{				
		setCmdName("disconnectStoCCmd");
	}
	
	void beDisconnectCtoSCmd(Boolean isLocator, String id) throws JSONException
	{	
		setCmdName("disconnectCtoSCmd");
        jsonCmd.put("isLocator", isLocator);
		jsonCmd.put("id", id);
	}
	
	void beStartNTPTimesyncCmd() throws JSONException
	{
		setCmdName("startNTPTimesyncCmd");
	}
	
	void beNTPTimesyncCmd(boolean done, long offset) throws JSONException
	{
		needAnswer = true;
		setCmdName("NTPTimesyncCmd");
		jsonCmd.put("Done", done);
		jsonCmd.put("offset", offset);
	}
	
	void beNTPTimesyncAns(Long requestTime, Long receiveTime) throws JSONException
	{
		setCmdName("NTPTimesyncAns");
		jsonCmd.put("requestTime", requestTime);
		jsonCmd.put("receiveTime", receiveTime);
	}

	void beRequestLocalizationCmd(String id) throws JSONException
	{
		needAnswer = true;
		setCmdName("requestLocalizationCmd");
		jsonCmd.put("id", id);
	}

	void beRequestLocalizationAns(boolean result) throws JSONException
	{
		setCmdName("requestLocalizationAns");
		jsonCmd.put("result", result);
	}
	
	void beMeasureCmd(long startTime, int playId) throws JSONException
	{
		setCmdName("measureCmd");
		jsonCmd.put("startTime", startTime);
		jsonCmd.put("playId", playId);
	}

	void beMeasureAns(boolean succeed, boolean isLocator, MMMeasureResult result) throws JSONException
	{
		setCmdName("measureAns");
		jsonCmd.put("succeed", succeed);
		jsonCmd.put("isLocator", isLocator);

		if(succeed) {
			for (int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++) {
				jsonCmd.accumulate("idx", result.getPeak(cnt).getIdx());
				jsonCmd.accumulate("value", result.getPeak(cnt).getValue());
			}
		}
	}
}

	