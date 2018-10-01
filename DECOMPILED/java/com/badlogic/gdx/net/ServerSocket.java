package com.badlogic.gdx.net;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.utils.Disposable;

public interface ServerSocket extends Disposable {
    Socket accept(SocketHints socketHints);

    Protocol getProtocol();
}
