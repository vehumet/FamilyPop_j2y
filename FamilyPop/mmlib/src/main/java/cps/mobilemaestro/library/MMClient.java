package cps.mobilemaestro.library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Vector;

import org.json.JSONException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;

public class MMClient implements MMDevice
{
	private MMDeviceInfo selfInfo = null;
	private MMDeviceInfo serverInfo;
	private boolean isLocator;
	private Context context;
	private long clockOffset;

	protected boolean started = false;
	public boolean connected = false;

	protected MMListener lThread = null;

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

					//Send start meassage to an activity
					MMUtils.sendMsg(topHandler, 1);
					break;

				case 2: // Listened command handling
					MMCommands cmd = (MMCommands) msg.obj;

					try {
						String cmdName = cmd.getCmdName();

						if (cmdName.equalsIgnoreCase("connectAns"))
						{
							connected = true;
							MMUtils.sendMsg(topHandler, 2);
						}
						else if(cmdName.equalsIgnoreCase("disconnectStoCCmd"))
						{
							connected = false;
							stop();

							MMUtils.sendMsg(topHandler, 0, "Disconnected");
						}
						else if(cmdName.equalsIgnoreCase("startNTPTimesyncCmd"))
						{
							MMCommands NTPTimesyncCmd = new MMCommands();
							NTPTimesyncCmd.beNTPTimesyncCmd(false, 0);

							MMSender sender = new MMSender(ctrlHandler, serverInfo, NTPTimesyncCmd);
							sender.start();
						}
						else if(cmdName.equals("NTPTimesyncAns"))
						{
							Vector<Long> offset = new Vector<Long>();
							Long requestTime;
							Long receiveTime;
							Long transmitTime;
							Long responseTime;

							Long avg_offset = (long)0;

							PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(cmd.socket.getOutputStream())),true);
							BufferedReader in = new BufferedReader(new InputStreamReader(cmd.socket.getInputStream()));

							MMCommands NTPTimesyncCmd = new MMCommands();
							NTPTimesyncCmd.beNTPTimesyncCmd(false, 0);

							for(int cnt = 0; cnt < 20; cnt++)
							{
								MMUtils.send(out, NTPTimesyncCmd);
								cmd.jsonCmd = MMUtils.receive(in);

								requestTime = cmd.jsonCmd.getLong("requestTime");
								receiveTime = cmd.jsonCmd.getLong("receiveTime");
								transmitTime = cmd.jsonCmd.getLong("sendTimestamp");
								responseTime = cmd.jsonCmd.getLong("receiveTimestamp");

								offset.add(((receiveTime - requestTime) + (transmitTime - responseTime)) / 2);
							}

							Collections.sort(offset);

							for(int cnt = 2; cnt < offset.size() - 2; cnt++)
								avg_offset += offset.get(cnt);

							avg_offset /= (offset.size() - 4);
							offset.clear();

							clockOffset = avg_offset;
							NTPTimesyncCmd.beNTPTimesyncCmd(true, avg_offset);
							MMUtils.send(out, NTPTimesyncCmd);
						}
						else if(cmdName.equals("requestLocalizationAns"))
						{
							if(cmd.jsonCmd.getBoolean("result"))
								MMUtils.sendMsg(topHandler, 3, "Start the localization");
							else
								MMUtils.sendMsg(topHandler, 3, "Cannot do the localization");
						}
						else if(cmdName.equals("measureCmd"))
						{
							if(!doMeasure(cmd.jsonCmd.getLong("startTime") - clockOffset, cmd.jsonCmd.getInt("playId"))) {
								MMCommands measureAns = new MMCommands();
								measureAns.beMeasureAns(false, isLocator, null);

								MMSender sender = new MMSender(ctrlHandler, serverInfo, measureAns);
								sender.start();
							}
						}

						cmd.socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;

				case 3:
					MMMeasureResult result = (MMMeasureResult)msg.obj;

					try {
						MMCommands measureAns = new MMCommands();
						measureAns.beMeasureAns(true, isLocator, result);

						MMSender sender = new MMSender(ctrlHandler, serverInfo, measureAns);
						sender.start();
					} catch (JSONException e) {
						e.printStackTrace();
					}

					break;
			}
		}
	};

	public MMClient(Context context, Handler topHandler, String id, boolean isLocator, String serverIpAddr)
	{
		this.context = context;
		this.topHandler = topHandler;
		this.isLocator = isLocator;
		this.serverInfo = new MMDeviceInfo("server", serverIpAddr);
		this.selfInfo = new MMDeviceInfo(id, MMUtils.getLocalIpAddr());
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
			disconnect();

			lThread.interrupt();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			lThread = null;
			started = false;
		}
	}

	public void connect()
	{
		if(started)
		{
			try {
				MMCommands connectCmd = new MMCommands();
				connectCmd.beConnectCmd(selfInfo.getId(), selfInfo.getIpAddr(), this.isLocator);

				MMSender sender = new MMSender(ctrlHandler, serverInfo, connectCmd);
				sender.start();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void disconnect()
	{
		if(connected)
		{
			try {
				MMCommands disconnectCmd = new MMCommands();
				disconnectCmd.beDisconnectCtoSCmd(isLocator, selfInfo.getId());

				MMSender sender = new MMSender(ctrlHandler, serverInfo, disconnectCmd);
				sender.start();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			connected = false;
		}
	}

	public void requestLocalization()
	{
		if(connected)
		{
			try {
				MMCommands requestLocalizationCmd = new MMCommands();
				requestLocalizationCmd.beRequestLocalizationCmd(selfInfo.getId());

				MMSender sender = new MMSender(ctrlHandler, serverInfo, requestLocalizationCmd);
				sender.start();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
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

	public MMDeviceInfo getSelfInfo() {
		return selfInfo;
	}

	public void setSelfInfo(MMDeviceInfo selfInfo) {
		this.selfInfo = selfInfo;
	}

	public MMDeviceInfo getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(MMDeviceInfo serverInfo) {
		this.serverInfo = serverInfo;
	}
}

