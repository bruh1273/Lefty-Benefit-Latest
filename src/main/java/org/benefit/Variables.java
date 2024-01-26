package org.benefit;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.ScreenHandler;
import java.util.ArrayList;

/**
 * Variables used across the mod
 */
public class Variables {
    public static boolean delayUIPackets = false;
    public static boolean shouldEditSign = true;
    public static final ArrayList<Packet<?>> delayedPackets = new ArrayList<>();
    public static Screen storedScreen = null;
    public static ScreenHandler storedScreenHandler = null;
    public static String lastCommand = "";
}
