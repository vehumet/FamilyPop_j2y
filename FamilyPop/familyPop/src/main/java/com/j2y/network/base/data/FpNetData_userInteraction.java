package com.j2y.network.base.data;

import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetOutgoingMessage;

/**
 * Created by gmpguru on 2016-02-02.
 */
public class FpNetData_userInteraction extends FpNetData_base
{
    public int _clientid;
    //----------------------------------------------------------------
    // 메시지 파싱
    @Override
    public void Parse(FpNetIncomingMessage inMsg)
    {
        super.Parse(inMsg);

        _clientid = inMsg.ReadInt();
    }

    //----------------------------------------------------------------
    // 메시지 패킹
    @Override
    public void Packing(FpNetOutgoingMessage outMsg)
    {
        super.Packing(outMsg);

        outMsg.Write(_clientid);
    }
}
