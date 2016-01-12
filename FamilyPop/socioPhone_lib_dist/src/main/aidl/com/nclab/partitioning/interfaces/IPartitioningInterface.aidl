// IPartitioningInterface.aidl
package com.nclab.partitioning.interfaces;

// Declare any non-default types here with import statements

interface IPartitioningInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    int registerQuery(String query);

	int deregisterQuery(int queryId);

	int updateTaskType(String taskType);

	void startLogging(String filename);
	void stopLogging();
}

