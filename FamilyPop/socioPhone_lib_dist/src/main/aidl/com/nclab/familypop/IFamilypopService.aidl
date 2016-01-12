// IFamilypopService.aidl
package com.nclab.familypop;

// Declare any non-default types here with import statements

interface IFamilypopService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void DataToFamilypopService(int a);

    /**	Server-side API	*/
    boolean startServer();
    boolean stopServer();
    String[] showClientsList();
    boolean initDevicesLocation(int x, int y);
    boolean getDevicesLocation(out int[] ids, out int[] x, out int[] y, out double[] angles);
    boolean startConversation();
    boolean stopConversation();

    /** Client-side API */
    boolean connectToServer(String ip);
    boolean disconnectFromServer();
}
