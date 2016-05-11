package com.j2y.network.server;

import java.net.Socket;

import android.os.Handler;
import android.util.Log;

import com.j2y.familypop.server.FpsRoot;
import com.j2y.familypop.server.FpsScenario_base;
import com.j2y.network.base.FpNetFacade_base;
import com.j2y.network.base.FpNetOutgoingMessage;
import com.j2y.network.base.FpPacketData;
import com.j2y.network.base.FpPacketHeader;
import com.j2y.network.base.data.FpNetData_base;
import kr.ac.kaist.resl.cmsp.iotappwrapper.FamilyPopService;
import kr.ac.kaist.resl.cmsp.iotappwrapper.FpServiceOutgoingMessage;
import org.json.JSONException;
import org.json.JSONObject;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpNetServer_client
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpNetServer_client extends FpNetFacade_base
{

	public static int _index;
	public int _clientID;
	public String _user_name;
    public int _bubble_color_type;
	public int _user_posid;
	public String _thingId;

	// Network
	//private FpNetIOStream _ioStream;
	
	// Scenario
	public FpsScenario_base _curScenario;
	
	// Game(FamilyBomb)
//	public ArrayList<FpsBubble> _bubble;
//    public FpsAttractor _attractor;
	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public FpNetServer_client(String thingId, Handler handle, FpsScenario_base curScenario)
	{
		_clientID = _index++;
		_thingId = thingId;
		
		_curScenario = curScenario;
        _user_name = "user1";
        _bubble_color_type = 0;
		_user_posid = 0;

		_messageHandler = handle;

		//_ioStream = new FpNetIOStream(this, true, handle);
		//_ioStream.start();
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
	public void Disconnect()
	{
        Log.i("[J2Y]", "[Server] client disconnected");
        super.Disconnect();
	}
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 패킷 전송
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
	/*
	private void sendPacket(FpPacketData packetData)
	{
		if(_ioStream != null)
			_ioStream.SendPacket(packetData);
	}
	*/

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void SendPacket(final int msgID, final FpNetData_base outPacket)
    {
		/*
        FpNetOutgoingMessage outMsg = new FpNetOutgoingMessage();
        outPacket.Packing(outMsg);

        FpPacketData packetData = new FpPacketData();
        packetData._header = new FpPacketHeader();
        packetData._header._size = outMsg.GetPacketSize();
        packetData._header._type = msgID;
        packetData._data = outMsg.GetPacketToByte();

        sendPacket(packetData);
        */

		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FpServiceOutgoingMessage outMsg = new FpServiceOutgoingMessage();
					outPacket.Packing(outMsg);
					JSONObject jsonObj = new JSONObject();
					jsonObj.put(FamilyPopService.THING_ID, FpsRoot._myThingId);
					jsonObj.put(FamilyPopService.KEY_TYPE, msgID);
					jsonObj.put(msgID + "", outMsg._dataArray);

					FamilyPopService serverObject = FpsRoot._iotAppService.getServiceObject(FamilyPopService.class, _thingId);
					if (serverObject != null) {
						serverObject.processStringNoReturn(jsonObj.toString());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		})).start();
    }
}
