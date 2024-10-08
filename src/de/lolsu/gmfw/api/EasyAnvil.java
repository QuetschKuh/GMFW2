package de.lolsu.gmfw.api;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EasyAnvil extends ContainerAnvil {

    private final EntityPlayer player;

    public EasyAnvil(EntityPlayer player) {
        super(player.inventory, player.world, new BlockPosition(0, 0, 0), player);
        this.player = player;
    }

    @Override
    public boolean a(EntityHuman entity) {
        return true;
    }

    public Inventory openInventory() {
        int containerId = player.nextContainerCounter();

        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage("Repairing"), 0);
        player.playerConnection.sendPacket(packet);

        player.activeContainer = this;
        player.activeContainer.windowId = containerId;
        player.activeContainer.addSlotListener(player);

        return this.getBukkitView().getTopInventory();
    }

}
