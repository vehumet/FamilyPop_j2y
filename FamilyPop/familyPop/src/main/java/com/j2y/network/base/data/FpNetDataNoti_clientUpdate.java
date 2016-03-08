package com.j2y.network.base.data;

import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetOutgoingMessage;

import java.util.ArrayList;

/**
 * Created by gmpguru on 2016-03-05.
 */
public class FpNetDataNoti_clientUpdate extends FpNetData_base
{
    public ArrayList< clientInfo > _clientInfos = null;
//    public float _dirX;
//    public float _dirY;
//    public int _clientid;
    //----------------------------------------------------------------
    // 메시지 파싱
    @Override
    public void Parse(FpNetIncomingMessage inMsg)
    {
        super.Parse(inMsg);

        if( _clientInfos == null ) _clientInfos = new ArrayList<clientInfo>();

        int count = inMsg.ReadInt();

        for( int i=0; i<count; i++)
        {
            clientInfo cInfo = new clientInfo();
            cInfo.Parse(inMsg);
            _clientInfos.add(cInfo);
        }
    }
    //----------------------------------------------------------------
    // 메시지 패킹
    @Override
    public void Packing(FpNetOutgoingMessage outMsg)
    {
        super.Packing(outMsg);

        int count = _clientInfos.size();
        outMsg.Write(count);

        for( int i=0; i<count; i++)
        {
            clientInfo cinfo = _clientInfos.get(i);
            cinfo.Packing(outMsg);
        }
    }

    //----------------------------------------------------------------
    // 데이터 추가
    public void AddClientData(float posx, float posy, int color, int clientID)
    {
        if( _clientInfos == null ) _clientInfos = new ArrayList<clientInfo>();

        clientInfo cinfo = new clientInfo();

        cinfo._posX = posx;
        cinfo._posY = posy;
        cinfo._color = color;
        cinfo._clientId = clientID;

        _clientInfos.add(cinfo);
    }

    public class clientInfo
    {
        public float _posX;
        public float _posY;
        public int _color;
        public int _clientId;

        //----------------------------------------------------------------
        // 메시지 파싱
        public void Parse(FpNetIncomingMessage inMsg)
        {
            _posX = inMsg.ReadFloat();
            _posY = inMsg.ReadFloat();
            _color = inMsg.ReadInt();
            _clientId = inMsg.ReadInt();
        }
        //----------------------------------------------------------------
        // 메시지 패킹
        public void Packing(FpNetOutgoingMessage outMsg)
        {
            outMsg.Write(_posX);
            outMsg.Write(_posY);
            outMsg.Write(_color);
            outMsg.Write(_clientId);
        }
    }
}
