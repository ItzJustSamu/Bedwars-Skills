package me.itzjustsamu.playerskills.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;

public interface Menu extends InventoryHolder {

    void onClick(int slot, ClickType event);

    default void onClose() {
        // EMPTY
    }

    default void open(Player player) {
        player.openInventory(getInventory());
    }
}