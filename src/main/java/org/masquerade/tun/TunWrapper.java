package org.masquerade.tun;

import org.masquerade.tun.Windows.WinTunTunnel;
import org.masquerade.utils.Logger;

public class TunWrapper {
    Logger logger = new Logger(TunWrapper.class);

    public Tunnel init() {
        String os = System.getProperty("os.name").toLowerCase();

        Tunnel tunnel = null;

        if (os.contains("win")) {
            logger.log("Detected Windows, using WinTun...");
            tunnel = new WinTunTunnel("Masquerade Tunnel");
        } else if (os.contains("linux")) {
            logger.log("Detected Linux, using native Linux TUN...");
        } else {
            logger.error("Unsupported OS, no TUN interface can be started.", os);
        }

        return tunnel;
    }
}
