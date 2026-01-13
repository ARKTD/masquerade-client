package org.masquerade.tun.Windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import org.masquerade.tun.Tunnel;
import org.masquerade.utils.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class WinTunTunnel extends Tunnel {
    static Logger logger = new Logger(WinTunTunnel.class);
    private static boolean fetch_attempt = false;

    private Pointer adapter;
    private Pointer session;

    // WinTun settings
    private static final int RING_CAPACITY = 0x4000000;

    public WinTunTunnel(String adapterName) {
        super(adapterName);
        init();
    }

    @Override
    public void init(){
        fetchDLL();
        obtainAdapter();
    }

    public void obtainAdapter(){
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
                        new WString("VPN Connection"),
                        null);
            }

            if (adapter == null) {
                int error = Native.getLastError();
                logger.error("Failed to obtain adapter.", error);
                return;
            }

            // Start connection session
            session = WinTun.INSTANCE.WintunStartSession(adapter, RING_CAPACITY);
            if (session == null) {
                int error = Native.getLastError();
                logger.error("Failed to start a session.", error);
                WinTun.INSTANCE.WintunCloseAdapter(adapter);
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            logger.error("WinTun library was not found!", e);
        } catch (Exception e) {
            logger.error("An error was encountered while obtaining adapter", e);
        }
    }

    public boolean isRunning(){
        if(session != null && adapter != null){
            return true;
        }
        return false;
    }

    public void close(){
        if (session != null) {
            WinTun.INSTANCE.WintunEndSession(session);
        }
        if (adapter != null) {
            WinTun.INSTANCE.WintunCloseAdapter(adapter);
        }
    }

    public byte[] readPacket() {
        if (session == null) {
            return null;
        }

        IntByReference size = new IntByReference();
        Pointer packetPointer = WinTun.INSTANCE.WintunReceivePacket(session, size);
        
        if (packetPointer == null) {
            return null;
        }

        int packetSize = size.getValue();
        byte[] packet = packetPointer.getByteArray(0, packetSize);
        
        WinTun.INSTANCE.WintunReleaseReceivePacket(session, packetPointer);
        
        return packet;
    }

    public boolean writePacket(byte[] packet) {
        if (session == null || packet == null || packet.length == 0) {
            return false;
        }

        try {
            Pointer packetPointer = WinTun.INSTANCE.WintunAllocateSendPacket(session, packet.length);
            
            if (packetPointer == null) {
                int error = Native.getLastError();
                logger.error("Failed to allocate send packet", error);
                return false;
            }

            packetPointer.write(0, packet, 0, packet.length);
            WinTun.INSTANCE.WintunSendPacket(session, packetPointer);
            
            return true;
        } catch (Exception e) {
            logger.error("Error writing packet", e);
            return false;
        }
    }

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

        InputStream input = WinTunTunnel.class.getResourceAsStream(resourcePath);

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
