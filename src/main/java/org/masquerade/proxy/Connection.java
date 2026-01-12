package org.masquerade.proxy;

import org.masquerade.tun.Tunnel;
import org.masquerade.tun.TunWrapper;

public class Connection {
    static TunWrapper TUNWrapper = new TunWrapper();
    static Tunnel tunnel = null;

    public void connect() throws Exception {
        if(tunnel != null){
            return;
        }

        tunnel = TUNWrapper.init();

        if(!tunnel.isRunning()){
            throw new Exception("Expected tunnel to be running after initialization.");
        }
    }

    public void close(){
        tunnel.close();
        tunnel = null;
    }
}
