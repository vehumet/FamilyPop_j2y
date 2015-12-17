package cps.mobilemaestro.library;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.json.JSONException;

import android.os.Handler;

public class MMListener extends Thread
{
	private final Handler ctrlHandler;
	private final ServerSocket serverSocket;
	private final int timeOut = 1000;
	
	public MMListener(Handler ctrlHandler) throws IOException
	{
		this.ctrlHandler = ctrlHandler;
		this.serverSocket = new ServerSocket(MMCtrlParam.port);
		
		this.serverSocket.setSoTimeout(timeOut);
		MMUtils.sendMsg(ctrlHandler, 1);
	}
	
	public void run() 
	{
		try 
		{	 
			while(!MMListener.interrupted())
			{
				try
				{
					Socket socket = serverSocket.accept();
					socket.setTcpNoDelay(true);
					MMCommands cmd = MMUtils.receiveOnce(socket);
					cmd.socket = socket;

					MMUtils.sendMsg(ctrlHandler, 2, cmd);
				}
				catch(SocketTimeoutException e)
				{
				} catch (JSONException e) {
				}
			}
			
			serverSocket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
