package org.masquerade.proxy;

import org.masquerade.tun.Tunnel;
import org.masquerade.tun.TunWrapper;

public class Connection {
    static TunWrapper TUNWrapper = new TunWrapper();
    static Tunnel tunnel = null;

    public void connect(){
        if(tunnel != null){
            return;
        }

        tunnel = TUNWrapper.init();
    }

    public void close(){
        tunnel.close();
        tunnel = null;
    }
}
