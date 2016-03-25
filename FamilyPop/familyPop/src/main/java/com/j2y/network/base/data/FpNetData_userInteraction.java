package com.j2y.network.base.data;

import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetOutgoingMessage;

/**
 * Created by gmpguru on 2016-02-02.
 */
public class FpNetData_userInteraction extends FpNetData_base
{
    public int _clientid;
    public int _send_client_id;

    //public int _colorid
    //----------------------------------------------------------------
    // �޽��� �Ľ�
    @Override
    public void Parse(FpNetIncomingMessage inMsg)
    {
        super.Parse(inMsg);

        _clientid = inMsg.ReadInt();
        _send_client_id = inMsg.ReadInt();
    }

    //----------------------------------------------------------------
    // �޽��� ��ŷ
    @Override
    public void Packing(FpNetOutgoingMessage outMsg)
    {
        super.Packing(outMsg);

        outMsg.Write(_clientid);
        outMsg.Write(_send_client_id);
    }
}
