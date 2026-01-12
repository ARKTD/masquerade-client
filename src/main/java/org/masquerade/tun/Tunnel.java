package org.masquerade.tun;

import org.masquerade.utils.Logger;

public abstract class TunConnection {
    static Logger logger = new Logger(TunConnection.class);

    public String adapterName;

    public TunConnection(String adapterName){
        this.adapterName = adapterName;
    }

    public void init(){

    }

    public void close(){

    }
}
