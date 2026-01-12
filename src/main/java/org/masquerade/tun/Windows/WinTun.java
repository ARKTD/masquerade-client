package org.masquerade.tun.Windows;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * WinTun JNA interface for Windows.
 */
interface WinTun extends StdCallLibrary {
    WinTun INSTANCE = Native.load("wintun", WinTun.class);

    Pointer WintunCreateAdapter(WString name, WString tunnelType, Pointer guid);

    Pointer WintunOpenAdapter(WString name);
    void WintunCloseAdapter(Pointer adapter);

    Pointer WintunStartSession(Pointer adapter, int capacity);
    void WintunEndSession(Pointer session);

    Pointer WintunReceivePacket(Pointer session, IntByReference size);
    void WintunReleaseReceivePacket(Pointer session, Pointer packet);

    Pointer WintunAllocateSendPacket(Pointer session, Pointer packet);
    void WintunSendPacket(Pointer session, Pointer packet);
    int WintunGetLastError();
}
