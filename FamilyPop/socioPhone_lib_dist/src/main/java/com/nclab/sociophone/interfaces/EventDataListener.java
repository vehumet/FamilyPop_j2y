package com.nclab.sociophone.interfaces;

public interface EventDataListener {
    void onInteractionEventOccured(int speakerID, int eventType, long timestamp);
}
