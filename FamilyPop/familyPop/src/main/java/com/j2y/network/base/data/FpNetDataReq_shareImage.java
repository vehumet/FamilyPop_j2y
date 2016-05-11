package com.j2y.network.base.data;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// FpNetDataReq_shareImage
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.j2y.network.base.FpNetIncomingMessage;
import com.j2y.network.base.FpNetOutgoingMessage;

import java.io.ByteArrayOutputStream;

public class FpNetDataReq_shareImage extends FpNetData_base
{
    public byte[] _bitMapByteArray;

    //----------------------------------------------------------------
    // 메시지 파싱
    @Override
    public void Parse(FpNetIncomingMessage inMsg)
    {
        super.Parse(inMsg);

        int length = inMsg.ReadInt();
        if(length > 0) {
            _bitMapByteArray = inMsg.ReadByteArray(length);
        }
    }

    //----------------------------------------------------------------
    // 메시지 패킹
    @Override
    public void Packing(FpNetOutgoingMessage outMsg)
    {
        super.Packing(outMsg);

        if(null == _bitMapByteArray)
            outMsg.Write((int)0);
        else {
            // compress because of binder size limit (approx. 1MB)
            Bitmap bmp = BitmapFactory.decodeByteArray(_bitMapByteArray, 0, _bitMapByteArray.length);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream);
            _bitMapByteArray = stream.toByteArray();
            outMsg.Write(_bitMapByteArray.length);
            if (_bitMapByteArray.length > 0)
                outMsg.Write(_bitMapByteArray);
        }
    }
}