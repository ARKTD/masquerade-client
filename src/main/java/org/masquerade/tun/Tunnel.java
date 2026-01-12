package org.masquerade.tun;

import org.masquerade.utils.Logger;

public abstract class Tunnel {
    static Logger logger = new Logger(Tunnel.class);

    public String adapterName;

    public Tunnel(String adapterName){
        this.adapterName = adapterName;
    }

    abstract public void init();
    abstract public void close();
    abstract public boolean isRunning();
}
