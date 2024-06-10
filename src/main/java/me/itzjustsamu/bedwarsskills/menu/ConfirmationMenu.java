package me.itzjustsamu.bedwarsskills.menu;

import me.hsgamer.hscore.bukkit.scheduler.Scheduler;
import me.hsgamer.hscore.bukkit.utils.ColorUtils;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConfirmationMenu implements Menu {

    private final BedWarsSkills plugin;
    private final Player player;
    private final ItemStack display;
    private final Runnable callback;
    private final Menu superMenu;

    public ConfirmationMenu(BedWarsSkills plugin, Player player, ItemStack display, Runnable callback, Menu superMenu) {
        this.plugin = plugin;
        this.player = player;
        this.display = display;
        this.callback = callback;
        this.superMenu = superMenu;
    }

    @Override
    public @NotNull Inventory getInventory() {
        String title = ColorUtils.colorize(MainConfig.CONFIRMATION_MENU_TITLE.getValue());
        int size = MainConfig.CONFIRMATION_MENU_SIZE.getValue();
        Inventory inventory = Bukkit.createInventory(this, size, title);

        if (MainConfig.CONFIRMATION_MENU_BACKGROUND.getValue()) {
            ItemStack background = MainConfig.CONFIRMATION_MENU_BACKGROUND_DISPLAY.getValue().build(player.getUniqueId());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, background);
            }
        }

        ItemStack yes = MainConfig.CONFIRMATION_ACCEPT.getValue().build(player.getUniqueId());
        ItemStack no = MainConfig.CONFIRMATION_DENY.getValue().build(player.getUniqueId());

        inventory.setItem(10, no);
        inventory.setItem(11, no);
        inventory.setItem(12, no);
        inventory.setItem(13, display);
        inventory.setItem(14, yes);
        inventory.setItem(15, yes);
        inventory.setItem(16, yes);

        return inventory;
    }

    @Override
    public void onClick(int slot, ClickType event) {
        if (slot == 10 || slot == 11 || slot == 12) {
            if (superMenu != null) {
                superMenu.open(player);
            } else {
                player.closeInventory();
            }
        } else if (slot == 14 || slot == 15 || slot == 16) {
            callback.run();
            if (superMenu != null) {
                superMenu.open(player);
            }
        }
    }

    @Override
    public void onClose() {
        if (superMenu != null) {
            Scheduler.plugin(plugin).sync().runEntityTask(player, () -> superMenu.open(player));
        }
    }

}