package com.j2y.familypop.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import com.j2y.familypop.MainActivity;

/**
 * Created by gmpguru on 2015-12-07.
 */
// scene
public abstract class BaseActivity extends Activity
{
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //���� �Լ�
    @Override
    protected  void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        OnBase_Create();
    }
    @Override
    protected void onDestroy()
    {
        OnBase_Destroy();
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean ret = false;
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                OnBase_Back();
                break;
            default:
                break;
        }
        return ret;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startActivity(Intent intent)
    {
        super.startActivity(intent, null);
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // ��� �Լ�

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //
    protected  boolean OnBase_Back()
    {
        boolean ret = false;

        // ���ư���� �̵��ϸ� ���� ��Ƽ��Ƽ ������ ����
        finish();
        return ret;
    }
    protected void OnBase_Create()
    {
    }
    protected void OnBase_Destroy()
    {
    }
    protected void StartActivity(Activity close, Class<?> cls)
    {
        if( close != null) close.finish();

        startActivity(new Intent(MainActivity.Instance, cls));
    }
}
