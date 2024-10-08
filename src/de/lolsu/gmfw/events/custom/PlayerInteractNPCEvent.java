package de.lolsu.gmfw.events.custom;

import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerInteractNPCEvent extends Event implements Cancellable {

    private final Player player;
    private final int entityId;
    private final PacketPlayInUseEntity.EnumEntityUseAction action;

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled = false;

    public PlayerInteractNPCEvent(Player player, int id, PacketPlayInUseEntity.EnumEntityUseAction action) {
        this.player = player;
        entityId = id;
        this.action = action;
    }

    public Player getPlayer() { return player; }
    public int getEntityId() { return entityId; }
    public PacketPlayInUseEntity.EnumEntityUseAction getAction() { return action; }


    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static HandlerList getHandlerList() { return HANDLER_LIST; }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
