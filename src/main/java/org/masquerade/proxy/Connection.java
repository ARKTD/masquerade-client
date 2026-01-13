package org.masquerade.proxy;

import org.masquerade.tun.Tunnel;
import org.masquerade.tun.TunWrapper;
import org.masquerade.utils.Logger;

public class Connection {
    static Logger logger = new Logger(Connection.class);
    static TunWrapper TUNWrapper = new TunWrapper();
    static Tunnel tunnel = null;

    public static ProxyMiddleware middleware = new ProxyMiddleware();

    // First part of proxy - obtain TUN interface, no handler.
    // aka VPN is started, but no "route" is selected, therefore the
    // adapter is prepared, but is idle.
    public void connect() throws Exception {
        // Logic is simple here: first try to init tunnel if not running yet
        // Tunnel might exist, but middleware might be stopped
        if (tunnel.isRunning()) {
            return;
        }
        tunnel = TUNWrapper.init();

        if (!tunnel.isRunning()) {
            throw new Exception("Expected tunnel to be running after initialization.");
        }
    }

    // Disconnect is only meant for working with adapter.
    public void disconnect() {
        middleware.stop();

        if (tunnel != null) {
            tunnel.close();
            tunnel = null;
        }

        logger.info("Connection closed.");
    }

    // Second part of proxy - engage the middleware for processing.
    // aka VPN uses the already-prepared tunnel, routes packets.
    public void engage() throws Exception{
        if(!tunnel.isRunning()){
            this.connect();
        }

        if(middleware.isActive()){
            return;
        }

        middleware.start(tunnel);
    }

    // Only stops the middleware, leaves the tunnel running.
    public void disengage(){
        middleware.stop();
        // We do not touch the tunnel here, we just paused VPN adapter.
    }
}
