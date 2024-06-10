package me.itzjustsamu.bedwarsskills.menu;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.ColorUtils;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.skill.Skill;
import me.itzjustsamu.bedwarsskills.storage.FlatFileStorage;
import me.itzjustsamu.bedwarsskills.util.CommonStringReplacer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static me.itzjustsamu.bedwarsskills.Permissions.ADMIN;
import static me.itzjustsamu.bedwarsskills.menu.Sounds.*;

public class SettingsMenu implements Menu {

    private final BedWarsSkills plugin;
    private final Skill skill;

    private final Player player;
    private final SPlayer sPlayer;
    private final BukkitConfig config;
    private final Map<String, Integer> toggleSkill;
    private final FlatFileStorage storage;

    public SettingsMenu(BedWarsSkills plugin, Skill skill, Player player, SPlayer sPlayer, BukkitConfig config, FlatFileStorage storage) {
        this.plugin = plugin;
        this.skill = skill;
        this.player = player;
        this.sPlayer = sPlayer;
        this.config = new BukkitConfig(new File(plugin.getDataFolder(), "skills" + File.separator + skill.getSkillsConfigName() + ".yml"));
        this.toggleSkill = new HashMap<>();
        this.storage = storage;
    }

    @Override
    public @NotNull Inventory getInventory() {
        String title = ColorUtils.colorize(MainConfig.SETTINGS_MENU_TITLE.getValue());
        int size = MainConfig.SETTINGS_MENU_SIZE.getValue();
        Inventory inventory = Bukkit.createInventory(this, size, title);

        if (MainConfig.SETTINGS_BACKGROUND_ENABLED.getValue()) {
            ItemStack background = MainConfig.SETTINGS_BACKGROUND_DISPLAY.getValue().build(sPlayer.getPlayer());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, background);
            }
        }

        if (skill != null) {
            inventory.setItem(3, skill.getDisplayItem(player));
            inventory.setItem(MainConfig.PURCHASE_POINT_SLOT.getValue(), MainConfig.PURCHASE_POINT_DISPLAY.getValue().build(sPlayer.getPlayer()));
            inventory.setItem(MainConfig.RESET_SKILLS_SLOT.getValue(), MainConfig.RESET_SKILLS_DISPLAY.getValue().build(sPlayer.getPlayer()));
            inventory.setItem(MainConfig.ADMIN_SLOT.getValue(), MainConfig.ADMIN_DISPLAY.getValue().build(sPlayer.getPlayer()));
            inventory.setItem(MainConfig.TOGGLE_SKILL_SLOT.getValue(), MainConfig.TOGGLE_SKILL_DISPLAY.getValue().build(sPlayer.getPlayer()));
            inventory.setItem(MainConfig.BACK_SLOT.getValue(), MainConfig.BACK_DISPLAY.getValue().build(sPlayer.getPlayer()));
            inventory.setItem(MainConfig.TOGGLE_SKILL_LEVEL_SLOT.getValue(), MainConfig.TOGGLE_SKILL_LEVEL_DISPLAY.getValue().build(sPlayer.getPlayer()));
        }

        return inventory;
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == 3) {
            handleSkillClick(clickType);
        } else if (slot == MainConfig.PURCHASE_POINT_SLOT.getValue()) {
            Runnable callback = getRunnable();
            if (MainConfig.CONFIRMATION_PURCHASE_POINTS.getValue()) {
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(plugin, player, player.getOpenInventory().getTopInventory().getItem(slot), callback, this);
                confirmationMenu.open(player);
            } else {
                callback.run();
            }
        } else if (slot == MainConfig.RESET_SKILLS_SLOT.getValue()) {
            Runnable callback = getRunnable(clickType);
            if (MainConfig.CONFIRMATION_RESET_SKILLS.getValue()) {
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(plugin, player, player.getOpenInventory().getTopInventory().getItem(slot), callback, this);
                confirmationMenu.open(player);
            } else {
                callback.run();
            }
        } else if (slot == MainConfig.ADMIN_SLOT.getValue() && player.hasPermission(ADMIN)) {
            AdminMenu adminMenu = new AdminMenu(player, skill, sPlayer, storage);
            adminMenu.open(player);
        } else if (slot == MainConfig.TOGGLE_SKILL_SLOT.getValue()) {
            handleToggleSkill(clickType);
        } else if (slot == MainConfig.BACK_SLOT.getValue()) {
            SkillsMenu skillsMenu = new SkillsMenu(plugin, player, sPlayer);
            skillsMenu.open(player);
            playUIButtonClickSound(player);
            CommonStringReplacer.resetSkill();
        } else if (slot == MainConfig.TOGGLE_SKILL_LEVEL_SLOT.getValue()) {
            handleToggleSkillLevel(clickType);
        }
    }

    private void handleSkillClick(ClickType event) {
        if (skill.getLevel(sPlayer) >= skill.getLimit()) {
            playItemBreakSound(player);
        } else if ((event == ClickType.LEFT || event == ClickType.RIGHT) && skill.getLevel(sPlayer) < skill.getLimit()) {
            int price = skill.getPrice().getValue();
            if (sPlayer.getPoints() >= price) {
                Runnable callback = () -> {
                    sPlayer.setLevel(skill.getSkillsConfigName(), skill.getLevel(sPlayer) + 1);
                    sPlayer.setPoints(sPlayer.getPoints() - price);
                    playExperienceOrbPickupSound(player);
                    open(player);
                };

                if (MainConfig.CONFIRMATION_PURCHASE_SKILLS.getValue()) {
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(plugin, player, skill.getDisplayItem(player), callback, this);
                    confirmationMenu.open(player);
                } else {
                    callback.run();
                }
            } else {
                playItemBreakSound(player);
                open(player);
            }
        }
    }

    private void handleToggleSkill(ClickType clickType) {
        String skillName = skill.getSkillsConfigName();
        int currentLevel = skill.getLevel(sPlayer);

        if (currentLevel == 0) {
            // Restore the original skill level
            Integer previousLevel = storage.loadPreviousSkillLevel(sPlayer.getPlayer(), skillName);
            if (previousLevel != null) {
                sPlayer.setLevel(skillName, previousLevel);
            }
        } else {
            // Save the current skill level
            storage.savePreviousSkillLevel(sPlayer.getPlayer(), skillName, currentLevel);
            // Set the skill level to 0
            sPlayer.setLevel(skillName, 0);
        }

        playUIButtonClickSound(player);
        open(player);
    }

    private void handleToggleSkillLevel(ClickType clickType) {
        String skillName = skill.getSkillsConfigName();
        int currentLevel = skill.getLevel(sPlayer);
        Integer storedLevel = storage.loadSkillLevel(sPlayer.getPlayer(), skillName);

        if (storedLevel == null) {
            storedLevel = currentLevel;
            storage.saveSkillLevel(sPlayer.getPlayer(), skillName, currentLevel);
        }

        if (clickType == ClickType.RIGHT) {
            // Increase the skill level by 1 if it's not at the stored level
            if (currentLevel < storedLevel) {
                sPlayer.setLevel(skillName, currentLevel + 1);
            } else {
                playItemBreakSound(player);
            }
        } else if (clickType == ClickType.LEFT) {
            // Decrease the skill level by 1 if it's above 0
            if (currentLevel > 0) {
                sPlayer.setLevel(skillName, currentLevel - 1);
            } else {
                playItemBreakSound(player);
            }
        }

        // Save the updated skill level
        storage.saveSkillLevel(sPlayer.getPlayer(), skillName, skill.getLevel(sPlayer));

        playUIButtonClickSound(player);
        open(player);
    }


    @NotNull
    private Runnable getRunnable(ClickType event) {
        if ((event == ClickType.LEFT || event == ClickType.RIGHT) && MainConfig.CONFIRMATION_RESET_SKILLS.getValue()) {
            return getResetSkillsCallback();
        } else {
            return getCallbackWithoutConfirmation();
        }
    }

    @NotNull
    private Runnable getResetSkillsCallback() {
        return () -> {
            int resetSkillsPrice = 0;
            if (MainConfig.RESET_PRICE.getValue() > 0) {
                resetSkillsPrice = MainConfig.RESET_PRICE.getValue();
            } else if (MainConfig.RESET_INCREMENT_PRICE.getValue() > 0) {
                resetSkillsPrice = MainConfig.RESET_PRICE.getValue() + (MainConfig.RESET_INCREMENT_PRICE.getValue() * sPlayer.getResetCount());
            }

            if (sPlayer.getPoints() >= resetSkillsPrice) {
                sPlayer.setPoints(sPlayer.getPoints() - resetSkillsPrice);
                if (MainConfig.REFUND_POINTS.getValue()) {
                    refundPointsForReset();
                }
                sPlayer.getSkills().clear();
                playGenericExplodeSound(player);
                sPlayer.incrementResetCount();
                open(player);
            } else {
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(plugin, player, MainConfig.RESET_SKILLS_DISPLAY.getValue().build(sPlayer.getPlayer()), null, this);
                confirmationMenu.open(player);
            }
        };
    }

    private void refundPointsForReset() {
        for (String s : sPlayer.getSkills().keySet()) {
            Skill skill = plugin.getSkills().get(s);
            if (skill != null) {
                for (int i = 1; i <= sPlayer.Level(s); ++i) {
                    sPlayer.setPoints(sPlayer.getPoints() + skill.getPrice().getValue());
                }
            } else {
                plugin.getLogger().severe("Skill not found for" + s);
            }
        }
    }

    @NotNull
    private Runnable getCallbackWithoutConfirmation() {
        return () -> {
            int resetPoint;
            if (MainConfig.RESET_INCREMENT_PRICE.getValue() > 0) {
                resetPoint = sPlayer.getResetCount() * (MainConfig.RESET_PRICE.getValue() * MainConfig.RESET_INCREMENT_PRICE.getValue());
            } else {
                resetPoint = MainConfig.RESET_PRICE.getValue() * (sPlayer.getResetCount() + 1);
            }
            if (sPlayer.getPoints() >= resetPoint) {
                sPlayer.setPoints(sPlayer.getPoints() - resetPoint);
                if (MainConfig.REFUND_POINTS.getValue()) {
                    refundPointsForReset();
                }
                sPlayer.getSkills().clear();
                sPlayer.incrementResetCount();
                playGenericExplodeSound(player);
                open(player);
            } else {
                playItemBreakSound(player);
                open(player);
            }
        };
    }

    @NotNull
    private Runnable getRunnable() {
        int price = sPlayer.getPointPrice();
        return () -> {
            if (MainConfig.POINTS_FUNDING_SOURCE.getValue().doTransaction(sPlayer, price, (player))) {
                sPlayer.setPoints(sPlayer.getPoints() + 1);
                playUIButtonClickSound(player);
                open(player);
            } else {
                playItemBreakSound(player);
            }
        };
    }
}
