package org.benefit;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.ScreenHandler;
import java.util.ArrayList;
public class Variables {
//variables used across the mod
    public static boolean sendUIPackets = true;
    public static boolean delayUIPackets = false;
    public static boolean shouldEditSign = true;
    public static ArrayList<Packet<?>> delayedPackets = new ArrayList<>();
    public static Screen storedScreen = null;
    public static ScreenHandler storedScreenHandler = null;
    public static String lastCommand;

    static {
        Variables.lastCommand = "";}
    }

