package cps.mobilemaestro.library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

public class MMUtils
{	
	//For network transmission
	public static void send(PrintWriter out, MMCommands cmd) throws JSONException, IOException
	{
		cmd.jsonCmd.put("sendTimestamp", gettime());
		out.println(cmd.jsonCmd.toString()); 
	}
	
	public static void sendOnce(Socket socket, MMCommands cmd) throws JSONException, IOException
	{
		PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
		send(out, cmd);
	}
	
	public static JSONObject receive(BufferedReader in) throws JSONException, IOException, SocketTimeoutException
	{ 
    	JSONObject jsonCmd = new JSONObject(in.readLine());    
		jsonCmd.put("receiveTimestamp", gettime());
	 
		return jsonCmd;
	}
	
	public static MMCommands receiveOnce(Socket socket) throws JSONException, IOException, SocketTimeoutException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		return new MMCommands(receive(in));
	}
	
	public static String getLocalIpAddr()
	{
		try 
		{ 
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
			{
				NetworkInterface intf = ( NetworkInterface ) en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
				{
					InetAddress inetAddress = ( InetAddress ) enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) 
					{
							if (inetAddress instanceof Inet4Address)
							{
								return inetAddress.getHostAddress().toString();
							}
					}
				}
			}
		} 
		catch (SocketException ex) 
		{
		}
		
		return null;
	}
		 
	//For control message
	public static void sendMsg(Handler ctrlHandler, int what, String contents)
	{
		Message msg = ctrlHandler.obtainMessage();
		msg.what = what;
		msg.obj = contents;
		ctrlHandler.sendMessage(msg);
	}
	
	public static void sendMsg(Handler ctrlHandler, int what, MMCommands cmd)
	{
		Message msg = ctrlHandler.obtainMessage();
		msg.what = what;
		msg.obj = cmd;
		ctrlHandler.sendMessage(msg);
	}
	
	public static void sendMsg(Handler ctrlHandler, int what, MMDeviceInfo netInfo)
	{
		Message msg = ctrlHandler.obtainMessage();
		msg.what = what;
		msg.obj = netInfo;
		ctrlHandler.sendMessage(msg);
	}
	
	public static void sendMsg(Handler ctrlHandler, int what)
	{
		Message msg = ctrlHandler.obtainMessage();
		msg.what = what;
		msg.obj = null;
		ctrlHandler.sendMessage(msg);
	}
	
	public static void sendMsg(Handler ctrlHandler, int what, MMMeasureResult result)
	{
		Message msg = ctrlHandler.obtainMessage();
		msg.what = what;
		msg.obj = result;
		ctrlHandler.sendMessage(msg);
	}

	public static void sendMsg(Handler ctrlHandler, int what, MMLocationResult result)
	{
		Message msg = ctrlHandler.obtainMessage();
		msg.what = what;
		msg.obj = result;
		ctrlHandler.sendMessage(msg);
	}

	public static long gettime()
	{
		return System.currentTimeMillis();
	}
	//public native static long gettime();	
}
