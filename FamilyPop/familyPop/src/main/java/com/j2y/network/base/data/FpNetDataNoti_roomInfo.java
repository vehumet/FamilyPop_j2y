package com.j2y.network.base.data;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpNetData_changeScenario
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetOutgoingMessage;

import java.util.ArrayList;

public class FpNetDataNoti_roomInfo extends FpNetData_base
{
    public String _userNames;
    public String _bubblesInfo = "";
    public String _clientsInfo = "";

    //----------------------------------------------------------------
    // 메시지 파싱
    @Override
    public void Parse(FpNetIncomingMessage inMsg)
    {
        super.Parse(inMsg);

        _userNames = inMsg.ReadString();
        _bubblesInfo = inMsg.ReadString();
        _clientsInfo = inMsg.ReadString();
    }

    //----------------------------------------------------------------
    // 메시지 패킹
    @Override
    public void Packing(FpNetOutgoingMessage outMsg)
    {
        super.Packing(outMsg);

        outMsg.Write(_userNames);
        outMsg.Write(_bubblesInfo);
        outMsg.Write(_clientsInfo);
    }
}
