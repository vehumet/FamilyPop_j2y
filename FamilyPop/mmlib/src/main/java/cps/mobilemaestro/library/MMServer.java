package cps.mobilemaestro.library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;

import org.json.JSONArray;
import org.json.JSONException;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class MMServer implements MMDevice
{
    private MMDeviceInfo selfInfo = null;
    private MMDeviceInfo locator = null;
    // CPS: layout argument 추가
    private MMDeviceLayout layout = null;
    private ArrayList<MMDeviceInfo> playerList = new ArrayList<MMDeviceInfo>();
    private Context context = null;

    private boolean started = false;
    private boolean synched = false;
    private int syncCnt;
    private int measureCnt;
    private boolean measureSucceed = false;

    private static String localizationRequestId;
    private static MMMeasureResult[] measureResult;
    private static double[][] distanceResult;
    private static MMLocationResult[] locationResult;

    private MMListener lThread = null;

    protected Handler topHandler = null;
    protected Handler ctrlHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // Error from anywhere
                    String errorMsg = msg.obj.toString();
                    stop();

                    //Send error message to an activity
                    MMUtils.sendMsg(topHandler, 0, errorMsg);
                    break;

                case 1: // Listener start message
                    started = true;

                    //Send start message to an activity
                    MMUtils.sendMsg(topHandler, 1);
                    break;

                case 2: // Listened command handling
                    MMCommands cmd = (MMCommands) msg.obj;

                    try {
                        String cmdName = cmd.getCmdName();
                        System.out.println("MMServer: get " + cmdName);

                        if(cmdName.equalsIgnoreCase("connectCmd"))
                        {
                            synched = false;
                            if(cmd.jsonCmd.getBoolean("isLocator"))
                                setLocator(new MMDeviceInfo( cmd.jsonCmd.getString("id"), cmd.jsonCmd.getString("ipAddr")));
                            else
                                addPlayer(new MMDeviceInfo( cmd.jsonCmd.getString("id"), cmd.jsonCmd.getString("ipAddr")));

                            MMCommands connAns = new MMCommands();
                            connAns.beConnectAns(true);
                            MMUtils.sendOnce(cmd.socket, connAns);

                            //Send player update message to an activity
                            MMUtils.sendMsg(topHandler, 2);
                        }
                        else if(cmdName.equalsIgnoreCase("disconnectCtoSCmd"))
                        {
                            if(cmd.jsonCmd.getBoolean("isLocator"))
                                delLocator();
                            else
                                delPlayer(cmd.jsonCmd.getString("id"));

                            MMUtils.sendMsg(topHandler, 2);
                        }
                        else if (cmdName.equalsIgnoreCase("NTPTimesyncCmd"))
                        {
                            MMCommands NTPTimesyncAns = new MMCommands();
                            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(cmd.socket.getOutputStream())), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(cmd.socket.getInputStream()));

                            while (true) {
                                NTPTimesyncAns.beNTPTimesyncAns(cmd.jsonCmd.getLong("sendTimestamp"), cmd.jsonCmd.getLong("receiveTimestamp"));
                                MMUtils.send(out, NTPTimesyncAns);

                                if (!cmd.jsonCmd.getBoolean("Done")) {
                                    cmd.jsonCmd = MMUtils.receive(in);
                                }
                                else {
                                    System.out.println("Sync Offset: " + cmd.jsonCmd.getLong("offset"));
                                    break;
                                }
                            }

                            if(++syncCnt < playerList.size())
                                doNTPTimeSync();
                            else
                            {
                                synched = true;
                                MMUtils.sendMsg(topHandler, 3);
                            }
                        }
                        else if (cmdName.equalsIgnoreCase("requestLocalizationCmd"))
                        {
                            MMCommands requestLocalizationAns = new MMCommands();
                            boolean isLocalizable = locator != null && synched && playerList.size() > 0;
                            requestLocalizationAns.beRequestLocalizationAns(isLocalizable);
                            MMUtils.sendOnce(cmd.socket, requestLocalizationAns);

                            if(isLocalizable) {
                                measureSucceed = true;
                                localizationRequestId = cmd.jsonCmd.getString("id");
                                doLocalization(cmd.jsonCmd.getString("id"));
                            }
                        }
                        else if (cmdName.equalsIgnoreCase("measureAns"))
                        {
                            if(measureSucceed)
                            {
                                int idx = cmd.jsonCmd.getBoolean("isLocator") ? 1 : 2;

                                if(!cmd.jsonCmd.getBoolean("succeed")) {
                                    measureSucceed = false;
                                    System.out.println("Measurement is failed due to the network delay on " + ((idx == 1)? "locator" : "client"));
                                }
                                else
                                {
                                    JSONArray idxArray = cmd.jsonCmd.getJSONArray("idx");
                                    JSONArray valueArray = cmd.jsonCmd.getJSONArray("value");

                                    for (int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++)
                                        measureResult[idx].setPeak(cnt, idxArray.getInt(cnt), valueArray.getInt(cnt));
                                }
                            }

                            measureCnt++;
                        }

                        if(!cmd.needAnswer)
                            cmd.socket.close();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

                case 3: // measure result
                    measureResult[0] = (MMMeasureResult)msg.obj;
                    measureCnt++;
                    break;
            }

            if(measureCnt == MMCtrlParam.numDevice && measureSucceed)
            {
                measureCnt = 0;
                System.out.println("-----Arrival time measure result-----");
                for(int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++)
                {
                    for (int cnt2 = 0; cnt2 < MMCtrlParam.numDevice; cnt2++)
                    {
                        if( measureResult == null ) continue; //?
                        if( measureResult[cnt] == null ) continue; //?

                        System.out.println("(" + cnt + ", " + cnt2 + "): " + measureResult[cnt].getPeak(cnt2).getIdx() + ", " + measureResult[cnt].getPeak(cnt2).getValue());
                    }
                }

                calcDistance();
                calcLocation();
                MMUtils.sendMsg(topHandler, 4, locationResult[2]);
            }
            else if(measureCnt == MMCtrlParam.numDevice && !measureSucceed)
            {
                boolean isLocalizable = locator != null && synched && playerList.size() > 0;

                if(isLocalizable) {
                    measureSucceed = true;
                    doLocalization(localizationRequestId);
                }
            }
            else
                MMUtils.sendMsg(topHandler, 5);
        }
    };

    // CPS: layout argument 추가
    public MMServer(Context context, Handler topHandler, MMDeviceLayout layout)
	{
        this.context = context;
        this.topHandler = topHandler;
        this.layout = layout;
        MMCtrlParam.serverLocatorDist = layout.getWidth();
        this.selfInfo = new MMDeviceInfo("server", MMUtils.getLocalIpAddr());
	}

    public void start()
    {
        // Start a listener
        if(!started) {
            StrictMode.enableDefaults();
            try {
                lThread = new MMListener(ctrlHandler);
                lThread.start();
            } catch (IOException e) {
                MMUtils.sendMsg(ctrlHandler, 0, "Failed to start a listener");
            }
        }
    }

    public void stop()
    {
        if(started)
        {
            MMCommands disconnectCmd = new MMCommands();

            try {
                disconnectCmd.beDisconnectStoCCmd();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(locator != null)
            {
                MMSender sender = new MMSender(ctrlHandler, locator, disconnectCmd);
                sender.start();
            }

            for(int cnt = 0; cnt < playerList.size(); cnt++)
            {
                MMSender sender = new MMSender(ctrlHandler, playerList.get(cnt), disconnectCmd);
                sender.start();
            }

            lThread.interrupt();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            lThread = null;
            delLocator();
            delPlayerList();
            started = false;
            synched = false;
        }
    }

    public void startSync()
    {
        synched = false;

        if(locator != null)
            syncCnt = -1;
        else
            syncCnt = 0;

        doNTPTimeSync();
    }

    public void doNTPTimeSync()
    {
        MMCommands startNTPTimesyncCmd = new MMCommands();
        MMDeviceInfo curClient;

        if(syncCnt == -1)
            curClient = locator;
        else
            curClient = playerList.get(syncCnt);

        try
        {
            startNTPTimesyncCmd.beStartNTPTimesyncCmd();
            MMSender sender = new MMSender(ctrlHandler, curClient, startNTPTimesyncCmd);
            sender.start();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void doLocalization(String id)
    {
        measureResult = new MMMeasureResult[MMCtrlParam.numDevice];
        for(int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++)
            measureResult[cnt] = new MMMeasureResult();

        measureCnt = 0;
        long startTime = MMUtils.gettime() + MMCtrlParam.recordDelay;

        try
        {
            for (int cnt = 0; cnt < 2; cnt++)
            {
                MMDeviceInfo curDevice;
                if (cnt == 0)
                    curDevice = locator;
                else
                    curDevice = playerList.get(findPlayerIdxWithId(id));

                MMCommands measureCmd = new MMCommands();
                measureCmd.beMeasureCmd(startTime, cnt + 1);

                MMSender sender = new MMSender(ctrlHandler, curDevice, measureCmd);
                sender.start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(measureSucceed)
            measureSucceed = doMeasure(startTime, 0);
    }

    public boolean doMeasure(long startTime, int playId)
    {
        if(startTime < MMUtils.gettime())
            return false;

        MMRecorder rThread = new MMRecorder(ctrlHandler, startTime, playId);
        rThread.start();

        MMAudioPlayer apThread = new MMAudioPlayer(context, startTime, playId);
        apThread.start();

        return true;
    }

    public void calcDistance()
    {
        distanceResult = new double[MMCtrlParam.numDevice][MMCtrlParam.numDevice];

        MMMeasureResult res_i, res_j;
        double tmp_i_j, tmp_j_i;

        for(int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++)
        {
            res_i = measureResult[cnt];
            for(int cnt2 = 0; cnt2 < MMCtrlParam.numDevice; cnt2++)
            {
                res_j = measureResult[cnt2];

                if(cnt == cnt2)
                    distanceResult[cnt][cnt2] = 0;
                else if(cnt + cnt2 == 1)
                    distanceResult[cnt][cnt2] = layout.getHeight();
                else
                {
                    tmp_i_j = (double) (res_i.getPeak(cnt2).getIdx() - res_i.getPeak(cnt).getIdx()) / (double) MMCtrlParam.recordRate * 1000.f;
                    tmp_j_i = (double) (res_j.getPeak(cnt2).getIdx() - res_j.getPeak(cnt).getIdx()) / (double) MMCtrlParam.recordRate * 1000.f;

                    distanceResult[cnt][cnt2] = (tmp_i_j - tmp_j_i) / 2.f * MMCtrlParam.sound_cm_to_ms + MMCtrlParam.spk_mic_cm;
                }
            }
        }

        System.out.println("-----Distance calculation result-----");
        for(int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++)
            for(int cnt2 = cnt + 1; cnt2 < MMCtrlParam.numDevice; cnt2++)
                System.out.println("(" + cnt + ", " + cnt2 + "): " + distanceResult[cnt][cnt2] + "\n");
    }

    public void calcLocation()
    {
        Matrix R = Matrix.random(MMCtrlParam.numDevice - 1, MMCtrlParam.numDevice - 1);
        double[] Z = new double[MMCtrlParam.numDevice - 1];
        Matrix XXte = Matrix.random(MMCtrlParam.numDevice - 1, MMCtrlParam.numDevice - 1);
        Matrix X = Matrix.random(2, 2);

    	/*R.set(0, 0, 0);
    	R.set(0, 1, 0.873260563025899);
    	R.set(0, 2, 0.662842835889241);
    	R.set(1, 0, 0.873260563025899);
    	R.set(1, 1, 0);
    	R.set(1, 2, 0.093797507386184);
    	R.set(2, 0, 0.662842835889241);
    	R.set(2, 1, 0.093797507386184);
    	R.set(2, 2, 0);

    	Z[0] = 0.000881087196662;
    	Z[1] = 0.838027554466191;
    	Z[2] = 0.645591369854234;*/

        for(int cnt = 1; cnt < MMCtrlParam.numDevice; cnt++)
        {
            for(int cnt2 = 1; cnt2 < MMCtrlParam.numDevice; cnt2++)
                R.set(cnt - 1, cnt2 - 1, Math.pow(distanceResult[cnt][cnt2], 2));

            Z[cnt - 1] = Math.pow(distanceResult[0][cnt], 2);
        }

        X.set(0, 0, 0);
        X.set(0, 1, 0);
        X.set(1, 0, Math.sqrt(Z[0]));
        X.set(1, 1, 0);

        for(int cnt = 0; cnt < MMCtrlParam.numDevice - 1; cnt++)
        {
            for(int cnt2 = 0; cnt2 < MMCtrlParam.numDevice - 1; cnt2++)
                XXte.set(cnt, cnt2, (Z[cnt] + Z[cnt2] - R.get(cnt, cnt2)) / 2.f);
        }

        SingularValueDecomposition svd = new SingularValueDecomposition(XXte);
        Matrix Xe = Matrix.random(MMCtrlParam.numDevice, 2);
        Matrix U = Matrix.random(MMCtrlParam.numDevice - 1, 2);
        U.setMatrix(0, MMCtrlParam.numDevice - 2, 0, 1, svd.getU());

        Matrix D = Matrix.random(2, 2);
        D.setMatrix(0, 1, 0, 1, svd.getS());

        for(int cnt = 0; cnt < 2; cnt++)
            for(int cnt2 = 0; cnt2 < 2; cnt2++)
                D.set(cnt, cnt2, Math.sqrt(D.get(cnt, cnt2)));

        Matrix mult = U.times(D);

        Xe.set(0, 0, 0);
        Xe.set(0, 1, 0);

        //Estimation
        for(int cnt = 0; cnt < MMCtrlParam.numDevice - 1; cnt++)
        {
            Xe.set(cnt + 1, 0, mult.get(cnt, 0));
            Xe.set(cnt + 1, 1, mult.get(cnt, 1));
        }

        //Rotation
        Matrix H = Xe.getMatrix(0, 1, 0, 1).transpose().times(X.getMatrix(0, 1, 0, 1));
        SingularValueDecomposition svdR = new SingularValueDecomposition(H);

        Matrix rot = svdR.getV().times(svdR.getU().transpose());
        Matrix Xer = (rot.times(Xe.transpose())).transpose();

        locationResult = new MMLocationResult[MMCtrlParam.numDevice];
        locationResult[0] = new MMLocationResult("Server", Xer.get(0, 0), Xer.get(0, 1));
        locationResult[1] = new MMLocationResult(getLocator().getId(), Xer.get(1, 0), Math.abs(Xer.get(1, 1)));
        //locationResult[2] = new MMLocationResult(localizationRequestId, Math.abs(Xer.get(2, 0)), Math.abs(Xer.get(2, 1)) + MMCtrlParam.phoneCompensation);
        locationResult[2] = new MMLocationResult(localizationRequestId, Math.abs(Xer.get(2, 1)) + MMCtrlParam.phoneCompensation, Math.abs(Xer.get(2, 0)));

        System.out.println("-----Location calculation result-----");
        for (int cnt = 0; cnt < MMCtrlParam.numDevice; cnt++) {
            if (cnt == 2) {
                System.out.println(locationResult[cnt].getLocX() + ", " + locationResult[cnt].getLocY() + ", (" + layout.getZeroX() + ", " + layout.getZeroY() + "), (" + layout.getWidth() + ", " + layout.getHeight() + ")");
                locationResult[cnt].setLocX((locationResult[cnt].getLocX() - layout.getZeroX()) / layout.getWidth());
                locationResult[cnt].setLocY((locationResult[cnt].getLocY() - layout.getZeroY()) / layout.getHeight());
            }
            System.out.println("(" + locationResult[cnt].getId() + "): " + locationResult[cnt].getLocX() + ", " + locationResult[cnt].getLocY());
        }
    }

    public MMDeviceInfo getSelfInfo() {
        return selfInfo;
    }

    public void setSelfInfo(MMDeviceInfo selfInfo) {
        this.selfInfo = selfInfo;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// Manage locator and player list //////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public MMDeviceInfo getLocator()
    {
        return locator;
    }

    public void setLocator(MMDeviceInfo locator)
    {
        this.locator = locator;
    }

    public void delLocator()
    {
        setLocator(null);
    }

    public ArrayList<MMDeviceInfo> getPlayerList()
    {
        return playerList;
    }

    public void setPlayerList(ArrayList<MMDeviceInfo> playerList)
    {
        this.playerList = playerList;
    }

    public void delPlayerList()
    {
        this.playerList.clear();
    }

    public void addPlayer(MMDeviceInfo player)
    {
        this.playerList.add(player);
    }

    public void delPlayer(String id)
    {
        int idx = findPlayerIdxWithId(id);
        if(idx != -1)
            playerList.remove(idx);
    }

    public int findPlayerIdxWithId(String id)
    {
        for(int cnt = 0; cnt < playerList.size(); cnt++)
            if(id.equalsIgnoreCase(playerList.get(cnt).getId()))
                return cnt;

        return -1;
    }
}


