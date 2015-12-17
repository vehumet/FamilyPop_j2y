package com.j2y.network.base.data;

import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetOutgoingMessage;

/**
 * Created by gmpguru on 2015-08-24.
 */
public class FpNetDataReq_TicTacToe_Start extends FpNetData_base
{
    public int _style; // 1(o), 2(x)
    //----------------------------------------------------------------
    // 메시지 파싱
    @Override
    public void Parse(FpNetIncomingMessage inMsg)
    {
        super.Parse(inMsg);
        _style = inMsg.ReadInt();
    }

    //----------------------------------------------------------------
    // 메시지 패킹
    @Override
    public void Packing(FpNetOutgoingMessage outMsg)
    {
        super.Packing(outMsg);
        outMsg.Write(_style);
    }
}