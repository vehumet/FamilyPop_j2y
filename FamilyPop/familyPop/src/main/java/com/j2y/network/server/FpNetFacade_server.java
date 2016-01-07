package com.j2y.network.server;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.j2y.familypop.activity.Activity_clientMain;
import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.server.FpsRoot;
import com.j2y.familypop.server.FpsScenarioDirector;
import com.j2y.familypop.server.FpsTalkUser;
import com.j2y.familypop.server.render.FpsBubble;
import com.j2y.familypop.server.render.FpsGameBomb;
import com.j2y.network.base.FpNetConstants;
import com.j2y.network.base.FpNetFacade_base;
import com.j2y.network.base.data.*;
import com.j2y.network.client.FpNetFacade_client;

import org.jbox2d.common.Vec2;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpNetFacade_server
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpNetFacade_server extends FpNetFacade_base
{
	public static FpNetFacade_server Instance;
	
	private FpTCPAccepter _accepter;
	private ServerSocket _serverSocket;
    private FpNetServer_packetHandler _packet_handler;
    //


	public ArrayList<FpNetServer_client> _clients = new ArrayList<FpNetServer_client>();

    //private HashMap<int, FpNetServer_client> _clientsTest;

	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public FpNetFacade_server()
	{
        Instance = this;
        _packet_handler = new FpNetServer_packetHandler(this);
	}
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 서버 네트워크
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
	public boolean IsConnected() 
	{
		if(_serverSocket == null)
			return false;
		
		return _serverSocket.isBound();
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	public void StartServer(int port) 
	{
		try {
			_serverSocket = new ServerSocket(port);
			_serverSocket.setReuseAddress(true);
			
			_accepter = new FpTCPAccepter(_serverSocket, _messageHandler);
			_accepter.start();
			
		} catch (IOException e) {
			e.printStackTrace();

			try {
				_serverSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
		}
	}

	//------------------------------------------------------------------------------------------------------------------------------------------------------
    public void CloseServer()
    {
        Log.i("[J2Y]", "FpNetFacade_server:CloseServer");
		try{
            send_quitRoom();

			_serverSocket.close();
			_accepter.destroy();

            for(FpNetServer_client client : _clients)
                client.Disconnect();

            _clients.clear();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    private void send_quitRoom()
    {
        Log.i("[J2Y]", "FpNetFacade_server:send_quitRoom");
        BroadcastPacket(FpNetConstants.SCNoti_quitRoom);
        SystemClock.sleep(50); // 기다려야 하나??

        //if(Activity_serverMain.Instance != null)
        //     Activity_serverMain.Instance.CloseRoom();
        FpNetServer_client._index = 0; // 임시
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 게임 상태
    public void Send_UserBang(FpsTalkUser user)
    {
        Log.i("[J2Y]", "FpNetFacade_server:Send_UserBang");
        user._net_client.SendPacket(FpNetConstants.SCReq_userBang, new FpNetData_base());
    }
    public void Send_BombRunning()
    {
        Log.i("[J2Y]", "FpNetFacade_server:Send_BombRunning");
        BroadcastPacket(FpNetConstants.SCNoti_bombRunning);
    }
    public void Send_endBomb()
    {
        Log.i("[J2Y]", "FpNetFacade_server:Send_endBomb");
        BroadcastPacket(FpNetConstants.SCNoti_endBomb);
    }
    //틱텍토
    public void Send_UserWin_TicTacToe(FpsTalkUser user)
    {
        Log.i("[J2Y]", "FpNetFacade_server:Send_UserWin_TicTacToe");
        user._net_client.SendPacket(FpNetConstants.SCReq_winUser_Tic_Tac_Toe, new FpNetData_base());

        for( int i=0; i<_clients.size()-1; i++)
        {
            if( user._net_client._clientID != _clients.get(i)._clientID)
            {
                _clients.get(i).SendPacket(FpNetConstants.SCReq_loseUser_Tic_Tac_Toe, new FpNetData_base());
            }
        }

    }
    public void Send_Start_TicTacToe()
    {
        for(int i=0; i<_clients.size(); i++ )//FpNetServer_client client : _clients)
        {
            if( i == _clients.size()-1)
            {
                FpNetDataReq_TicTacToe_Start outMsg = new FpNetDataReq_TicTacToe_Start();
                outMsg._style = 0;
                _clients.get(i).SendPacket(FpNetConstants.SCNoti_Start_Tic_Tac_Toe, outMsg);
            }
            else
            {
                FpNetDataReq_TicTacToe_Start outMsg = new FpNetDataReq_TicTacToe_Start();
                outMsg._style = (i % 2) + 1;
                _clients.get(i).SendPacket(FpNetConstants.SCNoti_Start_Tic_Tac_Toe, outMsg);
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void Send_talk_record_info()
    {

        Log.i("[J2Y]", "FpNetFacade_server:send_talk_record_info");

        //if(FpsScenarioDirector.Instance.GetActiveScenarioType() == FpNetConstants.SCENARIO_RECORD)
        {
            for(FpNetServer_client client : _clients)
            {
                FpNetDataRes_recordInfoList outMsg = new FpNetDataRes_recordInfoList();

                // todo: 메인쓰레드, 렌더링쓰레드 충돌남
                FpsTalkUser talk_user = Activity_serverMain.Instance.GetTalkUser(client);
                if(null == talk_user)
                    continue;

                outMsg._attractor._x = talk_user._attractor.GetPosition().x;
                outMsg._attractor._y = talk_user._attractor.GetPosition().y;
                outMsg._attractor._color = client._clientID;

                for(FpsBubble bubble : talk_user._bubble)
                {
                    Vec2 pos = bubble.GetPosition();
                    //Log.i("[J2Y]", String.format("[NetServer]:%f,%f", pos.x, pos.y));
                    outMsg.AddRecordData(bubble._start_time, bubble._end_time, pos.x, pos.y, bubble._rad, bubble._colorId);
                }

                outMsg._smile_events.addAll(talk_user._smile_events);

                client.SendPacket(FpNetConstants.SCRes_TalkRecordInfo, outMsg);
            }
        }
        SystemClock.sleep(50); // 기다려야 하나??
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 모든 클라 패킷 보내기
    public void BroadcastPacket(int msgID, FpNetData_base outPacket)
    {
//        FpNetOutgoingMessage outMsg = new FpNetOutgoingMessage();
//        //outMsg.Write(msgID);
//        outPacket.Packing(outMsg);
//
//        FpPacketData packetData = new FpPacketData();
//        packetData._header = new FpPacketHeader();
//        packetData._header._size = outMsg.GetPacketSize();
//        packetData._header._type = msgID;
//        packetData._data = outMsg.GetPacketToByte();
//
//        for(FpNetServer_client clinet : _clients)
//        {
//            clinet.SendPacket(packetData);
//        }

        for(FpNetServer_client client : _clients)
        {
            client.SendPacket(msgID, outPacket);
        }
    }


    public void BroadcastPacket(int msgID)
    {
        BroadcastPacket(msgID, new FpNetData_base());
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 클라이언트 관리
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public void AddClient(Socket socket)
    {
        FpNetServer_client client = new FpNetServer_client(socket, _messageHandler, FpsRoot.Instance._scenarioDirector.GetActiveScenario());
        _clients.add(client);

        if(Activity_serverMain.Instance != null)
            Activity_serverMain.Instance.AddTalkUser(client);
    }
    public void RemoveClient(FpNetServer_client client)
    {
        // todo: 클라이언트 한명만 나가기
        if(Activity_serverMain.Instance != null)
            FpNetFacade_server.Instance.Send_talk_record_info();

        // 현재 버전은 서버 종료하기
        FpsRoot.Instance.CloseServer();

        if(Activity_serverMain.Instance != null)
            Activity_serverMain.Instance.CloseServer();
    }

    public FpNetServer_client GetClientByIndex(int index)
    {
        if((index < 0) || (index >= _clients.size()))
            return null;
        return _clients.get(index);
    }

    // 서버의 정보를 클라이언트에게 전송한다.
    public void Send_ServerState( FpNetServer_client client )
    {
//        // test
//        FpNetDataNoti_serverInfo outMsg = new FpNetDataNoti_serverInfo();
//        outMsg._curScenario = FpsScenarioDirector.Instance.GetActiveScenarioType();
//        client.SendPacket(FpNetConstants.SCNoti_getServerState, outMsg);

    }
}
