package com.badlogic.gdx.net;

import com.badlogic.gdx.utils.StreamUtils;

public class SocketHints {
    public int connectTimeout = 5000;
    public boolean keepAlive = true;
    public boolean linger = false;
    public int lingerDuration = 0;
    public int performancePrefBandwidth = 0;
    public int performancePrefConnectionTime = 0;
    public int performancePrefLatency = 1;
    public int receiveBufferSize = StreamUtils.DEFAULT_BUFFER_SIZE;
    public int sendBufferSize = StreamUtils.DEFAULT_BUFFER_SIZE;
    public int socketTimeout = 0;
    public boolean tcpNoDelay = true;
    public int trafficClass = 20;
}
