package org.masquerade;

import org.fusesource.jansi.AnsiConsole;
import org.masquerade.proxy.Connection;
import org.masquerade.utils.Logger;

public class Main {
    Logger logger = new Logger(Main.class);

    void main() {
        System.setProperty("jansi.force", "true");
        AnsiConsole.systemInstall();
        this.logger.log("Starting Masquerade...");

        // Startup tun interface
        this.logger.log("Starting the Proxy module...");

        Connection proxy = new Connection();

        try{
            proxy.connect();
        } catch (Exception e) {
            this.logger.error("Proxy failed to start properly.", e);
        }

        this.logger.info("Proxy started successfully.");
    }
}
