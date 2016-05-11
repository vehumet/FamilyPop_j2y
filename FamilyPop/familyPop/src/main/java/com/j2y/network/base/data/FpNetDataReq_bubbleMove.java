package com.j2y.network.base.data;

import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetOutgoingMessage;
import com.j2y.network.server.FpNetServer_client;

/**
 * Created by gmpguru on 2016-01-27.
 */
public class FpNetDataReq_bubbleMove extends FpNetData_base
{
    public float _dirX;
    public float _dirY;
    public int _clientid;
    //----------------------------------------------------------------
    // 메시지 파싱
    @Override
    public void Parse(FpNetIncomingMessage inMsg)
    {
        super.Parse(inMsg);
        _dirX = inMsg.ReadFloat();
        _dirY = inMsg.ReadFloat();

        _clientid = ((FpNetServer_client)inMsg._obj )._clientID;
    }

    //----------------------------------------------------------------
    // 메시지 패킹
    @Override
    public void Packing(FpNetOutgoingMessage outMsg)
    {
        super.Packing(outMsg);
        outMsg.Write(_dirX);
        outMsg.Write(_dirY);
    }
}
