package org.masquerade.tun.Windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import org.masquerade.tun.TunAdapter;
import org.masquerade.utils.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class WinTunAdapter extends TunAdapter {
    static Logger logger = new Logger(WinTunAdapter.class);
    private static boolean fetch_attempt = false;

    private Pointer adapter;
    private Pointer session;

    // WinTun settings
    private static final int RING_CAPACITY = 0x4000000;

    public WinTunAdapter(String adapterName) {
        super(adapterName);
        init();
    }

    @Override
    public void init(){
        fetchDLL();
        obtainAdapter();
    }

    public boolean obtainAdapter(){
        try {
            if(!fetch_attempt) {
                fetchDLL();
            }

            // Try to open existing adapter
            adapter = WinTun.INSTANCE.WintunOpenAdapter(new WString(adapterName));

            if (adapter == null) {
                logger.log("Attempting to create new WinTun adapter...");

                adapter = WinTun.INSTANCE.WintunCreateAdapter(
                        new WString(adapterName),
                        new WString("Wintun"),
                        null);
            }

            if (adapter == null) {
                int error = Native.getLastError();
                logger.error("Failed to create/open adapter.", error);
                return false;
            }

            // Start session
            session = WinTun.INSTANCE.WintunStartSession(adapter, RING_CAPACITY);
            if (session == null) {
                int error = Native.getLastError();
                log.error("Failed to start session. Error code: {}", error);
                WinTun.INSTANCE.WintunCloseAdapter(adapter);
                return false;
            }

            open = true;
            int version = WinTun.INSTANCE.WintunGetRunningDriverVersion();
            log.info("WinTun opened (driver version: 0x{})", Integer.toHexString(version));

            // Configure IP and routing
            configureAdapter();

            return true;

        } catch (UnsatisfiedLinkError e) {
            log.error("WinTun library not found! Download wintun.dll from https://www.wintun.net/");
            return false;
        } catch (Exception e) {
            log.error("Failed to open WinTun", e);
            return false;
        }
    }

    public void close(){
        // Nothing to close here for now
    }

    /*

        @Override
        public byte[] readPacket() {
            if (!open) return null;

            try {
                IntByReference size = new IntByReference();
                Pointer packet = WinTun.INSTANCE.WintunReceivePacket(session, size);

                if (packet == null) {
                    return null;
                }

                int len = size.getValue();
                byte[] data = packet.getByteArray(0, len);
                WinTun.INSTANCE.WintunReleaseReceivePacket(session, packet);

                return data;

            } catch (Exception e) {
                if (open) {
                    log.error("Error reading packet", e);
                }
                return null;
            }
        }

        @Override
        public void close() {
            if (!open) return;
            open = false;

            if (session != null) {
                WinTun.INSTANCE.WintunEndSession(session);
            }
            if (adapter != null) {
                WinTun.INSTANCE.WintunCloseAdapter(adapter);
            }

            log.info("WinTun closed");
        }

        private void configureAdapter() {
            try {
                log.info("Configuring adapter IP and routing...");

                // Set IP address on the adapter (10.8.0.1/24)
                ProcessBuilder pb1 = new ProcessBuilder(
                        "netsh", "interface", "ip", "set", "address",
                        "name=\"" + adapterName + "\"",
                        "source=static",
                        "addr=10.8.0.1",
                        "mask=255.255.255.0"
                );
                Process p1 = pb1.start();
                int exit1 = p1.waitFor();

                if (exit1 == 0) {
                    log.info("✓ IP address configured: 10.8.0.1/24");
                } else {
                    log.warn("Failed to set IP address (exit code: {})", exit1);
                }

                // Add route for all traffic (0.0.0.0/0) through TUN adapter
                // This makes all internet traffic go through our VPN
                ProcessBuilder pb2 = new ProcessBuilder(
                        "netsh", "interface", "ip", "add", "route",
                        "0.0.0.0/0", "\"" + adapterName + "\"", "10.8.0.1", "metric=1"
                );
                Process p2 = pb2.start();
                int exit2 = p2.waitFor();

                ProcessBuilder pb3 = new ProcessBuilder(
                        "netsh", "interface", "ip", "add", "route",
                        "128.0.0.0/1", "\"" + adapterName + "\"", "10.8.0.1", "metric=1"
                );
                Process p3 = pb3.start();
                int exit3 = p3.waitFor();

                if (exit2 == 0 && exit3 == 0) {
                    log.info("✓ Routes configured (0.0.0.0/1 + 128.0.0.0/1)");
                    log.info("All internet traffic will now flow through TUN adapter");
                } else {
                    log.warn("Failed to add routes (exit codes: {}, {})", exit2, exit3);
                }

            } catch (IOException | InterruptedException e) {
                log.error("Failed to configure adapter", e);
            }
        }
    }*/

    public static synchronized void fetchDLL() {
        if(fetch_attempt){
            return;
        }

        // If we failed while loading we just pray it exists in system.
        // No need to replay an already-failed fetch.
        fetch_attempt = true;

        String arch = System.getProperty("os.arch").toLowerCase();
        String archFolder;

        if (arch.contains("amd64") || arch.contains("x86_64")) {
            archFolder = "amd64";
        } else if (arch.contains("x86")) {
            archFolder = "x86";
        } else if (arch.contains("aarch64")) {
            archFolder = "arm64";
        } else if (arch.contains("arm")) {
            archFolder = "arm";
        } else {
            logger.error("Unsupported architecture for WinTun", arch);
            return;
        }

        String resourcePath = "/wintun/bin/" + archFolder + "/wintun.dll";

        InputStream input = WinTunAdapter.class.getResourceAsStream(resourcePath);

        if (input == null) {
            logger.error("The WinTun DLL failed to load from resources, we pray it already exists somewhere");
            return;
        }

        String libFolderPath = System.getProperty("user.home") + "/Masquerade";
        File libFolder = new File(libFolderPath, "lib");
        libFolder.mkdirs();
        File dllFile = new File(libFolder, "wintun.dll");

        System.setProperty("jna.library.path", libFolder.getAbsolutePath());

        try{
            Files.copy(input, dllFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            input.close();
        }
        catch(Exception e){
            logger.error("Failed to extract WinTun DLL", e);
        }

        logger.info("Successfully extracted WinTun DLL");
    }
}
