package org.masquerade.tun;

import org.masquerade.utils.Logger;

public class TunInterface {
    Logger logger = new Logger(TunInterface.class);
    public boolean connected = false;

    public void init() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            logger.log("Detected Windows, using WinTun...");
        } else if (os.contains("linux")) {
            logger.log("Detected Linux, using native Linux TUN...");
        } else {
            logger.error("Unsupported OS, no TUN interface can be started.", os);
        }

        this.connected = true;
    }

    public void close(){
        this.connected = false;
    }
}
