package de.lolsu.gmfw.interfaces;

import net.minecraft.server.v1_8_R3.Packet;

public interface IPacketReader {

    void inject();
    void uninject();
    void readPacket(Packet<?> packet);
    Object getValue(Object obj, String name);

}
