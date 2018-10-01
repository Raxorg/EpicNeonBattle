package com.badlogic.gdx.net;

import com.badlogic.gdx.utils.StreamUtils;

public class ServerSocketHints {
    public int acceptTimeout = 5000;
    public int backlog = 16;
    public int performancePrefBandwidth = 0;
    public int performancePrefConnectionTime = 0;
    public int performancePrefLatency = 1;
    public int receiveBufferSize = StreamUtils.DEFAULT_BUFFER_SIZE;
    public boolean reuseAddress = true;
}
