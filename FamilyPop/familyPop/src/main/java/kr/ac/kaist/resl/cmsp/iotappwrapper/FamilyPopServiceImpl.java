package kr.ac.kaist.resl.cmsp.iotappwrapper;

import android.content.Context;
import android.util.Log;
import com.j2y.familypop.client.FpcRoot;
import com.j2y.familypop.server.FpsRoot;
import com.j2y.network.base.FpNetFacade_base;
import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.server.FpNetFacade_server;
import kr.ac.kaist.resl.cmsp.iotapp.library.IoTAppService;
import kr.ac.kaist.resl.cmsp.iotapp.library.ThingServiceEndpoint;
import kr.ac.kaist.resl.cmsp.iotapp.library.impl.AndroidIoTAppService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shheo on 15. 4. 20.
 */
public class FamilyPopServiceImpl implements FamilyPopService {
    private static final String TAG = FamilyPopServiceImpl.class.getSimpleName();
    protected static final String ID_HEADER_SERVER = "FamilyPop_Server_";
    protected static final String ID_HEADER_CLIENT= "FamilyPop_Client_";
    private String thingId;
    private String thingName;
    protected String serverThingId;
    protected static IoTAppService iotAppService = null;
    private FpNetFacade_base fpNetFacade_CallbackObj;
    public List<FamilyPopService> _peers = new ArrayList<FamilyPopService>();
    public Map<String, FamilyPopService> _peers_map = new HashMap<String, FamilyPopService>();

    public FamilyPopServiceImpl(Context context, String thingId, String thingName, FpNetFacade_base callbackObj) {
        iotAppService = new AndroidIoTAppService(context);
        iotAppService.connectPlatform(null);
        this.thingId = thingId;
        this.thingName = thingName;
        this.fpNetFacade_CallbackObj = callbackObj;
    }

    @Override
    public void processStringNoReturn(String str) {
        try {
            Log.d("IoTApp", "processStringNoReturn: " + str);
            final JSONObject jsonMsg = new JSONObject(str);
            int type = Integer.parseInt(jsonMsg.getString(KEY_TYPE));
            String senderThingId = jsonMsg.getString(FamilyPopService.THING_ID);
            FpServiceIncomingMessage msg = new FpServiceIncomingMessage();
            msg.setDataArray(jsonMsg);
            if (senderThingId.startsWith(ID_HEADER_CLIENT)) {
                FpNetFacade_server server = (FpNetFacade_server) fpNetFacade_CallbackObj;
                if (server._thingId_clients_map.containsKey(senderThingId)) {
                    Log.d("IoTApp", "processStringNoReturn: Setting obj of message as client " + senderThingId);
                    msg._obj = server._thingId_clients_map.get(senderThingId);
                } else {
                    Log.e("IoTApp", "wtf? " + senderThingId);
                    for (String id : server._thingId_clients_map.keySet()) {
                        Log.e("IoTApp", "wtf? " + id);
                    }
                }
            }
            fpNetFacade_CallbackObj._messageHandler.obtainMessage(type, msg).sendToTarget();
        } catch (JSONException e) {
            Log.e(TAG, "String " + str + " is not in json format");
            e.printStackTrace();
        }
    }

    @Override
    public String processStringWithReturn(String str) {
        String toReturn = "";
        try {
            Log.d("IoTApp", "processStringWithReturn: param: " + str);
            final JSONObject command = new JSONObject(str);
            int type = Integer.parseInt(command.getString(KEY_TYPE));
            Log.d("IoTApp", "processStringWithReturn: return type: " + type);
            toReturn = "Returned from " + thingId;
        } catch (JSONException e) {
            Log.e(TAG, "String " + str + " is not in json format");
            e.printStackTrace();
        }

        return toReturn;
    }

    @Override
    public String getThingId() {
        return thingId;
    }

    @Override
    public String getThingName() {
        return thingName;
    }

    @Override
    public void setEndpoint(ThingServiceEndpoint thingServiceEndpoint) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public Boolean isConnected() {
        return null;
    }
}
