package org.btwr.decaying_spawners.util;

public interface DecayTracker {

    boolean isDecayed();
    void setDecayed(boolean decayed);

    int getMobCount();
    void setMobCount(int count);
    void incrementMobCount();

    int getTickCount();
    void setTickCount(int count);
    void incrementTickCount();

    void reset();

}