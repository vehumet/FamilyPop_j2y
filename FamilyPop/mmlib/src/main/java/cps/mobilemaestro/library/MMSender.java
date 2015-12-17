package cps.mobilemaestro.library;

import android.os.Handler;

import org.json.JSONException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class MMSender extends Thread
{
	private final Handler ctrlHandler;
	private final MMDeviceInfo dest;
	private final MMCommands cmd;

	public MMSender(Handler ctrlHandler, MMDeviceInfo dest, MMCommands cmd)
	{
		this.ctrlHandler = ctrlHandler;
		this.dest = dest;
		this.cmd = cmd;
	}
	
	public void run() 
	{
		try 
		{
			InetAddress serverAddr = InetAddress.getByName(dest.getIpAddr());
			Socket socket = new Socket(serverAddr, MMCtrlParam.port);
			socket.setTcpNoDelay(true);

			MMUtils.sendOnce(socket, cmd);
		    
		    if(cmd.needAnswer)
		    {
		    	MMCommands ans = MMUtils.receiveOnce(socket);
				ans.socket = socket;
				MMUtils.sendMsg(ctrlHandler, 2, ans);
		    }
		}
		catch(SocketTimeoutException e)
		{
			MMUtils.sendMsg(ctrlHandler, 0, "A destination is not responsible");
		}
		catch (IOException e) 
		{
			MMUtils.sendMsg(ctrlHandler, 0, "A destination has been terminated");
		} catch (JSONException e) {
			MMUtils.sendMsg(ctrlHandler, 0, "JSON error");
		}
	}
}
