package me.itzjustsamu.playerskills.menu;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.ColorUtils;
import me.itzjustsamu.playerskills.PlayerSkills;
import me.itzjustsamu.playerskills.config.MainConfig;
import me.itzjustsamu.playerskills.player.SPlayer;
import me.itzjustsamu.playerskills.skill.Skill;
import me.itzjustsamu.playerskills.storage.FlatFileStorage;
import me.itzjustsamu.playerskills.util.CommonStringReplacer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static me.itzjustsamu.playerskills.menu.Sounds.playExperienceOrbPickupSound;
import static me.itzjustsamu.playerskills.menu.Sounds.playItemBreakSound;

public class SkillsMenu implements Menu {
    private final PlayerSkills plugin;
    private final Player player;
    private final SPlayer sPlayer;
    private Skill clickedSkill;
    private BukkitConfig bukkitConfig;

    public SkillsMenu(PlayerSkills plugin, Player player, SPlayer sPlayer) {
        this.plugin = plugin;
        this.player = player;
        this.sPlayer = sPlayer;
    }

    public @NotNull Inventory getInventory() {
        CommonStringReplacer.resetSkill();
        String title = ColorUtils.colorize(MainConfig.SKILLS_MENU_TITLE.getValue());
        int size = MainConfig.SKILLS_MENU_SIZE.getValue();
        Inventory inventory = Bukkit.createInventory(this, size, title);
        if (MainConfig.SKILLS_BACKGROUND_ENABLED.getValue()) {
            ItemStack background = MainConfig.SKILLS_BACKGROUND_DISPLAY.getValue().build(player.getUniqueId());

            for (int i = 0; i < inventory.getSize(); ++i) {
                inventory.setItem(i, background);
            }
        }
        for (Skill skill : plugin.getSkills().values()) {
            if (!skill.isSkillDisabled()) {
                inventory.setItem(skill.getGuiSlot(), skill.getDisplayItem(player));
            }
        }
        return inventory;
    }

    public void onClick(int slot, ClickType clickType) {
        for (Skill skill : plugin.getSkills().values()) {
            if (clickType == ClickType.RIGHT && slot == skill.getGuiSlot()) {
                clickedSkill = skill;
                CommonStringReplacer.setSkill(clickedSkill);
                SettingsMenu SettingsMenu = new SettingsMenu(plugin, skill, player, sPlayer, bukkitConfig, new FlatFileStorage(plugin));
                SettingsMenu.open(player);
                return;
            }

            if (clickType == ClickType.LEFT && slot == skill.getGuiSlot()) {
                if (skill.getLevel(sPlayer) >= skill.getLimit()) {
                    playItemBreakSound(player);
                } else if (skill.getLevel(sPlayer) < skill.getLimit()) {
                    int price = skill.getPrice().getValue();
                    if (sPlayer.getPoints() >= price) {
                        Runnable callback = () -> {
                            sPlayer.setLevel(skill.getSkillsConfigName(), skill.getLevel(sPlayer) + 1);
                            sPlayer.setPoints(sPlayer.getPoints() - price);
                            playExperienceOrbPickupSound(player);
                            open(player);
                        };

                        if (MainConfig.CONFIRMATION_PURCHASE_SKILLS.getValue()) {
                            ConfirmationMenu confirmationMenu = new ConfirmationMenu(plugin, player, player.getOpenInventory().getTopInventory().getItem(slot), callback, this);
                            confirmationMenu.open(player);
                        } else {
                            callback.run();
                        }
                        return;
                    }
                    playItemBreakSound(player);
                }
            }
        }
    }
}