package com.j2y.network.base;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpNetConstants
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class FpNetConstants 
{
	//----------------------------------------------------------------------------
	// 연결
	public final static int Connected = 1;
	public final static int Exception = 2;
	public final static int ClientAccepted = 3;
	public final static int ClientDisconnected = 4;
	public final static int ServerDisconnected = 5;

	//----------------------------------------------------------------------------
	// Packet Protocol
    public final static int CSReq_setUserInfo = 500;
    public final static int SCNoti_roomUserInfo = 501;

	//----------------------------------------------------------------------------
	//public final static int SCNoti_getServerState = 502;

	//----------------------------------------------------------------------------
    public final static int CSReq_OnStartGame = 1000;
    public final static int SCNoti_startGame = 1001;
	public final static int SCNoti_OnStartScenario = 1101;
	public final static int CSReq_ChangeScenario = 1002;
	public final static int SCNoti_ChangeScenario = 1003;
    public final static int CSC_ShareImage = 1004;
    public final static int CSReq_TalkRecordInfo = 1005;
    public final static int SCRes_TalkRecordInfo = 1006;
    public final static int CSReq_smileEvent = 1007;
    public final static int SCNoti_smileEvent = 1008;

    public final static int CSReq_exitRoom = 2001;
    public final static int SCNoti_quitRoom = 2002;


	public final static int SCReq_userBang = 3000;
	public final static int SCNoti_bombRunning = 3001;
	public final static int SCNoti_endBomb = 3002;

    // Family Talk
    public final static int CSReq_familyTalk_voice = 3101;

    // regulation
	public final static int CSReq_regulation_Info = 3500;

	public final static int CSReq_clearBubble = 3501;

	// 틱텍토
	public final static int CSReq_OnStart_Tic_Tac_Toe = 4000;
	public final static int CSReq_OnEnd_Tic_Tac_Toe = 4001;

	public final static int SCNoti_Start_Tic_Tac_Toe = 4002;
	public final static int SCNoti_end_Tic_Tac_Toe = 4003;

	public final static int CSReq_selectIndex_Tic_Tac_Toe = 4004;
	public final static int SCReq_winUser_Tic_Tac_Toe = 4005;
	public final static int SCReq_loseUser_Tic_Tac_Toe = 4006;

	public final static int CSReq_throwback_Tic_Tac_Toe = 4007;

	// 사용자 메세지
	public final static int CSReq_userInput_bubbleMove = 5000;
	public final static int CSReq_userInteraction = 5001;

	// client update
	public final static int SCNoti_clientUpdate = 5002;			// 버블 정보를 클라이언트 들 에게 전송해 갱신 하라는 메세제


    //----------------------------------------------------------------------------
	// 시나리오
	public final static int SCENARIO_NONE = -1;
	public final static int SCENARIO_TALK = 0;
	public final static int SCENARIO_RECORD = 1;
	public final static int SCENARIO_GAME = 2;
	public final static int SCENARIO_TIC_TAC_TOE = 3;
	
	public final static int ColorSize = 6;
	public final static int[] ColorArray  = 
	{
		0xffE64496,     // pink
		0xffE44742,     // red
		0xffEBEC4B,     // yellow
		0xff45D18C,     // green
		0xff47D4CD,		// phthalogreen
		0xff4D82D6,     // blue
        0xff66ff66, 	// 임시
	};
}
