package com.nclab.sociophone;

/**
 * Created by gulee-lab on 15. 12. 15..
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nclab.partitioning.interfaces.IPartitioningInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ContextAPI{

    //=========================================================================
    // PUBLIC 메소드
    //=========================================================================
    private ReentrantLock symphonyLock = new ReentrantLock();
    private Condition symphonyConnected = symphonyLock.newCondition();
    private boolean SymphonyServiceConnected = false;
    public Context context;

    public static List<String> registeredContexts = new ArrayList<String>();
    public static List<Integer> maestro_QueryIds = new ArrayList<Integer>();
    public static List<Integer> dummy_QueryIds = new ArrayList<Integer>();
    public static List<Integer> QueryIds = new ArrayList<Integer>();
    public static List<Integer> record_QueryIds = new ArrayList<Integer>();
    public SymphonyService SService;

    public ContextAPI(Context context) {
        this.context = context;
        SService = new SymphonyService(context);
        bindSymphoneyService(context);
    }

    //=========================================================================
    // 서비스 연결
    //=========================================================================

    private class SymphonyConnection extends Thread {
        private String contextName = null;
        private String query;
        private long starttime;
        private Context context;

        public SymphonyConnection(Context context, String contextName, String query) {
            this.context = context;
            this.contextName = contextName;
            this.query = query;
            this.starttime = -1;
        }

        public SymphonyConnection(Context context, String contextName, String query, long starttime) {
            this.context = context;
            this.contextName = contextName;
            this.contextName = contextName;
            this.query = query;
            this.starttime = starttime;
        }

        public void run() {
            Log.d("ContextAPI", "Start symphony service connection.");
            bindSymphoneyService(this.context);

            //	wait until symphony connected
            symphonyLock.lock();
            try {
                while (!SymphonyServiceConnected) {
                    symphonyConnected.await();
                }
            } catch (InterruptedException ie) {
            } finally {
                symphonyLock.unlock();
            }

            Log.d("ContextAPI", "After the lock in SCthread");
            // register query to SymphoneyService
            final int queryId = SService.registerQuery(query);
            Log.d("ContextAPI", "RegisterQuery: " + query + " => " + queryId);

            // add queryId to the list
            QueryIds.add(queryId);
        }
    }

    private void bindSymphoneyService(Context context) {
        if (SService.isBinded()) {
            Log.d("ContextAPI", "SymphoneyService is already binded.");
            return;
        }

        // set connectivity to ContextAPI
        SService.setServiceConnection(new ServiceConnection(){
                                          @Override
                                          public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                                              if (SService.isBinded() == false)
                                                  return;

                                              // set task type
                                              SService.updateTaskType("MW");

                                              symphonyLock.lock();
                                              try { symphonyConnected.signal(); }
                                              finally { symphonyLock.unlock(); }

                                              SymphonyServiceConnected= true;
                                          }

                                          @Override
                                          public void onServiceDisconnected(ComponentName componentName) {
                                              Log.d("gulee", "SymphoneyService disconnected.");
                                          }
                                      }
            );
        // start Symphony Service
        SService.startService(context);
    }

    private void unbindSymphoneyService() {
        if (!SService.isBinded())
            return;

        //  de-register all queries
        deregisterQuery("DUMMY", dummy_QueryIds);
        deregisterQuery("DISTANCE", maestro_QueryIds);
        deregisterQuery("SPEAKER", QueryIds);
        deregisterQuery("REC", record_QueryIds);

        //ContextAPI.getInstance().stopService(this);
    }

    public boolean registerQuery(String query) {
        try {
            final String[] queryTokens = query.split(" ");
            // construct the query structure
            final String _query = "CONTEXT " + queryTokens[0]
                    + " INTERVAL " + queryTokens[1] + " " + queryTokens[2]
                    + " DELAY " + queryTokens[3];

            SymphonyConnection scThread = new SymphonyConnection(context, queryTokens[0], _query);
            scThread.start();
            return true;

        } catch (Exception e) {
            Log.d("SymphonyService", "SService-> registerQuery exception : " + e.toString());
            return false;
        }
    }


    public void deregisterQuery(String context, List<Integer> queryIdsList) {

        try {
            // for all registered queries
            for (int queryId : queryIdsList) {
                // deregister the query
                SService.deregisterQuery(queryId);
                Log.d("SymphonyService", "DeregisterQuery: " + queryId);
            }
            // clear the queryId list
            queryIdsList.clear();
            registeredContexts.remove(context);
        } catch (Exception e) {
            Log.d("SymphonyService", e.toString());
        }
    }

    class SymphonyService {
        // 인스턴스
        private ServiceConnection conn;
        // 서비스 액션
        private static final String SERVICE_ACTION = "com.nclab.partitioning.DEFAULT";
        // 인터페이스
        private IPartitioningInterface m_interface;
        // 서비스 연결 객체
        private ServiceConnection m_serviceConnection;

        SymphonyService(Context context){
           startService(context);
        }

        private class InnerServiceConnection implements ServiceConnection {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // 인터페이스를 얻는다.
                Log.d("ContextAPI", "get Interface");
                m_interface = IPartitioningInterface.Stub.asInterface(service);
                // 서비스 연결 객체가 존재하면 처리한다.
                if (m_serviceConnection != null) {
                    // 서비스 연결을 처리한다.
                    Log.d("ContextAPI", "m_serviceConnection is not null");
                    m_serviceConnection.onServiceConnected(name, service);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // 인터페이스를 초기화한다.
                m_interface = null;

                // 서비스 연결 객체가 존재하면 처리한다.
                if (m_serviceConnection != null) {
                    // 서비스 연결 해제를 처리한다.
                    m_serviceConnection.onServiceDisconnected(name);
                }
            }
        }

        public void startService(Context context) {
            // 서비스 인텐트를 생성한다.
            final Intent serviceIntent = new Intent(SERVICE_ACTION);

            // 서비스를 연결한다.
            conn = new InnerServiceConnection();
            context.getApplicationContext().bindService(serviceIntent, conn, Activity.BIND_AUTO_CREATE);
        }

        public void stopService(Context context) {
            //	(jungi) stopService --> unbindService
            if (conn != null) {
                context.unbindService(conn);
            }
        }

        public boolean isBinded() {
            // 인터페이스가 존재하는지 여부를 반환한다.
            return m_interface != null;
        }

        public void setServiceConnection(ServiceConnection serviceConnection) {
            // 서비스 연결 객체를 설정한다.
            m_serviceConnection = serviceConnection;
        }

        public void updateTaskType(String taskType) {
            // 인터페이스가 존재하면 처리한다.
            if (m_interface != null) {
                try {
                    // 기능을 처리한다.
                    m_interface.updateTaskType(taskType);
                } catch (RemoteException e) {
                    // 예외를 로그에 기록한다.
                    Log.d("SymphonyService", e.toString());
                }
            }
        }

        public int registerQuery(String string) {
            Log.e("SymphonyService", "m_interface : " + m_interface);
            // 인터페이스가 존재하면 처리한다.
            if (m_interface != null) {
                try {
                    // 기능을 처리한다.
                    int queryId = m_interface.registerQuery(string);
                    return queryId;
                } catch (RemoteException e) {
                    // 예외를 로그에 기록한다.
                    Log.d("SymphonyService", e.toString());
                }
            }
            return -1;
        }

        public int deregisterQuery(int queryId) {
            // 인터페이스가 존재하면 처리한다.
            Log.e("SymphonyService", "deregisterQuery : " + queryId);
            if (m_interface != null) {
                try {
                    // 기능을 처리한다.
                    return m_interface.deregisterQuery(queryId);
                } catch (RemoteException e) {
                    // 예외를 로그에 기록한다.
                    Log.d("SymphonyService", e.toString());
                }
            }
            // 빈 값을 반환한다.
            return -1;
        }

        public void startLogging(String string) {
            // 인터페이스가 존재하면 처리한다.
            if (m_interface != null) {
                try {
                    // 기능을 처리한다.
                    m_interface.startLogging(string);
                } catch (RemoteException e) {
                    // 예외를 로그에 기록한다.
                    Log.d("SymphonyService", e.toString());
                }
            }
        }

        public void stopLogging() {
            // 인터페이스가 존재하면 처리한다.
            if (m_interface != null) {
                try {
                    // 기능을 처리한다.
                    m_interface.stopLogging();
                } catch (RemoteException e) {
                    // 예외를 로그에 기록한다.
                    Log.d("SymphonyService", e.toString());
                }
            }
        }
    }
}
