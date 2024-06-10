package me.itzjustsamu.playerskills.menu;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.ColorUtils;
import me.hsgamer.hscore.config.path.impl.IntegerConfigPath;
import me.itzjustsamu.playerskills.config.MainConfig;
import me.itzjustsamu.playerskills.fundingsource.FundingSource;
import me.itzjustsamu.playerskills.fundingsource.VaultFundingSource;
import me.itzjustsamu.playerskills.fundingsource.XPFundingSource;
import me.itzjustsamu.playerskills.player.SPlayer;
import me.itzjustsamu.playerskills.skill.Skill;
import me.itzjustsamu.playerskills.storage.FlatFileStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static me.itzjustsamu.playerskills.Permissions.ADMIN;
import static me.itzjustsamu.playerskills.menu.Sounds.*;

public class AdminMenu implements Menu {

    private final Player player;
    private final Skill skill;
    private final SPlayer sPlayer;

    private final FlatFileStorage storage;


    public AdminMenu(Player player, Skill skill, SPlayer sPlayer, FlatFileStorage storage) {
        this.player = player;
        this.skill = skill;
        this.sPlayer = sPlayer;
        this.storage = storage;
        new BukkitConfig(new File(skill.getPlugin().getDataFolder(), "skills" + File.separator + skill.getSkillsConfigName() + ".yml"));
        skill.getConfig().setup();
    }

    @Override
    public @NotNull Inventory getInventory() {
        String title = ColorUtils.colorize(MainConfig.ADMIN_MENU_TITLE.getValue());
        int size = MainConfig.ADMIN_MENU_SIZE.getValue();
        Inventory inventory = Bukkit.createInventory(this, size, title);

        if (MainConfig.ADMIN_BACKGROUND_ENABLED.getValue()) {
            ItemStack background = MainConfig.ADMIN_BACKGROUND_DISPLAY.getValue().build(player.getUniqueId());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, background);
            }
        }

        if (skill != null) {
            inventory.setItem(3, skill.getDisplayItem(player));
            inventory.setItem(MainConfig.PURCHASE_POINT_SLOT.getValue(), MainConfig.PURCHASE_POINT_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.RESET_SKILLS_SLOT.getValue(), MainConfig.RESET_SKILLS_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.ADMIN_SKILLS_UPGRADE_SLOT.getValue(), MainConfig.ADMIN_SKILLS_UPGRADE_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.ADMIN_SKILLS_POINT_PRICE_SLOT.getValue(), MainConfig.ADMIN_SKILLS_POINT_PRICE_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.ADMIN_SKILLS_INCREMENT_POINT_PRICE_SLOT.getValue(), MainConfig.ADMIN_SKILLS_INCREMENT_POINT_PRICE_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.ADMIN_SKILLS_MAX_LEVEL_SLOT.getValue(), MainConfig.ADMIN_SKILLS_MAX_LEVEL_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.ADMIN_SKILLS_CONFIRMATION_TOGGLE_SLOT.getValue(), MainConfig.ADMIN_SKILLS_CONFIRMATION_TOGGLE_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.POINTS_FUNDING_SLOT.getValue(), MainConfig.POINTS_FUNDING_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.POINTS_SLOT.getValue(), MainConfig.POINTS_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.POINTS_INCREMENT_SLOT.getValue(), MainConfig.POINTS_INCREMENT_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.POINTS_CONFIRMATION_TOGGLE_SLOT.getValue(), MainConfig.POINTS_CONFIRMATION_TOGGLE_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.RESET_REFUND_SLOT.getValue(), MainConfig.RESET_REFUND_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.RESET_SLOT.getValue(), MainConfig.RESET_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.RESET_INCREMENT_SLOT.getValue(), MainConfig.RESET_INCREMENT_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.RESET_CONFIRMATION_TOGGLE_SLOT.getValue(), MainConfig.RESET_CONFIRMATION_TOGGLE_DISPLAY.getValue().build(player.getUniqueId()));
            inventory.setItem(MainConfig.ADMIN_BACK_SLOT.getValue(), MainConfig.ADMIN_BACK_DISPLAY.getValue().build(player.getUniqueId()));
        }

        return inventory;
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == MainConfig.PURCHASE_POINT_SLOT.getValue()) {
            Runnable callback = getRunnable();
            if (slot == MainConfig.PURCHASE_POINT_SLOT.getValue() && MainConfig.CONFIRMATION_PURCHASE_POINTS.getValue() && player.hasPermission(ADMIN)) {
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(skill.getPlugin(), player, player.getOpenInventory().getTopInventory().getItem(slot), callback, this);
                confirmationMenu.open(player);
            } else {
                callback.run();
            }
        } else if (slot == MainConfig.RESET_SKILLS_SLOT.getValue()) {
            Runnable callback = getRunnable(clickType);
            if (slot == MainConfig.RESET_SKILLS_SLOT.getValue() && MainConfig.CONFIRMATION_RESET_SKILLS.getValue()) {
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(skill.getPlugin(), player, player.getOpenInventory().getTopInventory().getItem(slot), callback, this);
                confirmationMenu.open(player);
            } else {
                callback.run();
            }
        } else if (slot == 3) {
            handleSkillClick(clickType);
        } else if (slot == MainConfig.ADMIN_SKILLS_UPGRADE_SLOT.getValue()) {
            handleUpgradeClick(clickType);
        } else if (slot == MainConfig.ADMIN_SKILLS_POINT_PRICE_SLOT.getValue()) {
            handlePriceClick(clickType);
        } else if (slot == MainConfig.ADMIN_SKILLS_INCREMENT_POINT_PRICE_SLOT.getValue()) {
            handleIncrementClick(clickType);
        } else if (slot == MainConfig.ADMIN_SKILLS_MAX_LEVEL_SLOT.getValue()) {
            handleSkillsMaxLevelClick(clickType);
        } else if (slot == MainConfig.ADMIN_SKILLS_CONFIRMATION_TOGGLE_SLOT.getValue()) {
            toggleConfirmation(slot);
            playUIButtonClickSound(player);
        } else if (slot == MainConfig.POINTS_FUNDING_SLOT.getValue()) {
            toggleFundingSource();
            playUIButtonClickSound(player);
        } else if (slot == MainConfig.POINTS_SLOT.getValue()) {
            if (clickType == ClickType.LEFT) {
                decreasePoints();
                playUIButtonClickSound(player);
            } else if (clickType == ClickType.RIGHT) {
                increasePoints();
                playUIButtonClickSound(player);
            }
        } else if (slot == MainConfig.POINTS_INCREMENT_SLOT.getValue()) {
            if (clickType == ClickType.LEFT) {
                decreaseIncrementedPoints();
                playUIButtonClickSound(player);
            } else if (clickType == ClickType.RIGHT) {
                increaseIncrementedPoints();
                playUIButtonClickSound(player);
            }
        } else if (slot == MainConfig.ADMIN_BACK_SLOT.getValue()) {
            SettingsMenu SettingsMenu = new SettingsMenu(skill.getPlugin(), skill, player, sPlayer, skill.getConfig(), storage);
            SettingsMenu.open(player);
            playUIButtonClickSound(player);
        } else if (slot == MainConfig.POINTS_CONFIRMATION_TOGGLE_SLOT.getValue()) {
            togglePointsConfirmation(slot);
            playUIButtonClickSound(player);
        } else if (slot == MainConfig.RESET_SLOT.getValue()) {
            if (clickType == ClickType.LEFT) {
                decreaseResetPrice();
                playUIButtonClickSound(player);
            } else if (clickType == ClickType.RIGHT) {
                increaseResetPrice();
                playUIButtonClickSound(player);
            }
        } else if (slot == MainConfig.RESET_INCREMENT_SLOT.getValue()) {
            if (clickType == ClickType.LEFT) {
                decreaseIncrementResetPrice();
                playUIButtonClickSound(player);
            } else if (clickType == ClickType.RIGHT) {
                increaseIncrementResetPrice();
                playUIButtonClickSound(player);
            }
        } else if (slot == MainConfig.RESET_REFUND_SLOT.getValue()) {
            toggleRefundPoints();
            playUIButtonClickSound(player);
        } else if (slot == MainConfig.RESET_CONFIRMATION_TOGGLE_SLOT.getValue()) {
            toggleResetConfirmation(slot);
            playUIButtonClickSound(player);
        } else if (slot == MainConfig.ADMIN_BACK_SLOT.getValue()) {
            SettingsMenu SettingsMenu = new SettingsMenu(skill.getPlugin(), skill, (player), sPlayer, skill.getConfig(), storage);
            SettingsMenu.open(player);
            playUIButtonClickSound(player);
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
                    ConfirmationMenu confirmationMenu = new ConfirmationMenu(skill.getPlugin(), player, skill.getDisplayItem(player), callback, this);
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

    private void handleUpgradeClick(ClickType event) {
        if (event == ClickType.RIGHT && player.hasPermission(ADMIN)) {
            increaseSkillsUpgrade();
            playUIButtonClickSound(player);
        } else if (event == ClickType.LEFT && player.hasPermission(ADMIN)) {
            decreaseSkillsUpgrade();
            playUIButtonClickSound(player);
        }
    }

    private void handleIncrementClick(ClickType event) {
        if (event == ClickType.RIGHT && player.hasPermission(ADMIN)) {
            increaseSkillsUpgradeIncrementPrice();
            playUIButtonClickSound(player);
        } else if (event == ClickType.LEFT && player.hasPermission(ADMIN)) {
            decreaseSkillsUpgradeIncrementPrice();
            playUIButtonClickSound(player);
        }
    }

    private void handlePriceClick(ClickType event) {
        if (event == ClickType.RIGHT && player.hasPermission(ADMIN)) {
            increaseSkillsPrice();
            playUIButtonClickSound(player);
        } else if (event == ClickType.LEFT && player.hasPermission(ADMIN)) {
            decreaseSkillsPrice();
            playUIButtonClickSound(player);
        }
    }

    private void handleSkillsMaxLevelClick(ClickType event) {
        if (event == ClickType.RIGHT && player.hasPermission(ADMIN)) {
            increaseSkillsMaxLevel();
            playUIButtonClickSound(player);
        } else if (event == ClickType.LEFT && player.hasPermission(ADMIN)) {
            decreaseSkillsMaxLevel();
            playUIButtonClickSound(player);
        }
    }

    @NotNull
    private Runnable getRunnable() {
        int price = sPlayer.getPointPrice();
        return () -> {
            if (MainConfig.POINTS_FUNDING_SOURCE.getValue().doTransaction(this.sPlayer, price, this.player)) {
                sPlayer.setPoints(sPlayer.getPoints() + 1);
                playUIButtonClickSound(player);
                open(player);
            } else {
                playItemBreakSound(player);
            }
        };
    }

    @NotNull
    private Runnable getRunnable(ClickType event) {
        if ((event == ClickType.LEFT) && MainConfig.CONFIRMATION_RESET_SKILLS.getValue()) {
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
                sPlayer.incrementResetCount();
                sPlayer.getSkills().clear();
                playGenericExplodeSound(player);
                this.open(player);
            } else {
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(skill.getPlugin(), player, MainConfig.RESET_SKILLS_DISPLAY.getValue().build(player.getUniqueId()), null, this);
                confirmationMenu.open(player);
            }
        };
    }

    private void refundPointsForReset() {
        for (String s : sPlayer.getSkills().keySet()) {
            for (int i = 1; i <= sPlayer.Level(s); ++i) {
                sPlayer.setPoints(sPlayer.getPoints() + skill.getPlugin().getSkills().get(s).getPrice().getValue());
            }
        }
    }

    @NotNull
    private Runnable getCallbackWithoutConfirmation() {
        return () -> {
            int resetSkillsPrice;
            if (MainConfig.RESET_INCREMENT_PRICE.getValue()  > 0) {
                resetSkillsPrice = MainConfig.RESET_INCREMENT_PRICE.getValue() * (sPlayer.getResetCount());
            } else {
                resetSkillsPrice = MainConfig.RESET_PRICE.getValue() + sPlayer.getResetCount() * (MainConfig.RESET_INCREMENT_PRICE.getValue());
            }
            if (sPlayer.getPoints() >= resetSkillsPrice) {
                sPlayer.setPoints(sPlayer.getPoints() - resetSkillsPrice);
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

    public void increaseSkillsUpgrade() {
        if (skill != null) {
            IntegerConfigPath incrementedSkill = skill.getUpgrade();
            int currentIncrement = incrementedSkill.getValue();
            int newIncrement = currentIncrement + 1;
            skill.setIncrement(newIncrement);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }
    }

    public void decreaseSkillsUpgrade() {
        if (skill != null) {
            IntegerConfigPath incrementedSkill = skill.getUpgrade();
            int currentIncrement = incrementedSkill.getValue();
            int newIncrement = Math.max(0, currentIncrement - 1);
            skill.setIncrement(newIncrement);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }
    }

    public void decreaseSkillsPrice() {
        if (skill != null) {
            int currentPrice = skill.getPrice().getValue();
            int newPrice = Math.max(0, currentPrice - 1);
            skill.setPrice(newPrice);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }
    }

    public void increaseSkillsPrice() {
        if (skill != null) {
            int currentPrice = skill.getPrice().getValue();
            int newPrice = currentPrice + 1;
            skill.setPrice(newPrice);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }
    }

    public void decreaseSkillsUpgradeIncrementPrice() {
        if (skill != null) {
            int currentPrice = skill.getPrice().getValue();
            int newPrice = Math.max(0, currentPrice - 1);
            skill.setPrice(newPrice);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }
    }

    public void increaseSkillsUpgradeIncrementPrice() {
        if (skill != null) {
            int currentPrice = skill.getPrice().getValue();
            int newPrice = currentPrice + 1;
            skill.setPrice(newPrice);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }
    }

    public void increaseSkillsMaxLevel() {
        if (skill != null) {
            int currentMaxLevel = skill.getLimit();
            int newMaxLevel = currentMaxLevel + 1;
            skill.setLimit(newMaxLevel);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }

    }

    public void decreaseSkillsMaxLevel() {
        if (skill != null) {
            int currentMaxLevel = skill.getLimit();
            int newMaxLevel = Math.max(0, currentMaxLevel - 1);
            skill.setLimit(newMaxLevel);
            skill.getConfig().save();
            player.openInventory(getInventory());
        }
    }

    private void toggleConfirmation(int slot) {
        if (slot == MainConfig.ADMIN_SKILLS_CONFIRMATION_TOGGLE_SLOT.getValue()) {
            boolean confirmationStatus = !MainConfig.CONFIRMATION_PURCHASE_SKILLS.getValue();
            MainConfig.CONFIRMATION_PURCHASE_SKILLS.setValue(confirmationStatus);
            skill.getConfig().set(MainConfig.CONFIRMATION_PURCHASE_SKILLS.getPath(), MainConfig.CONFIRMATION_PURCHASE_SKILLS.getValue());
            skill.getConfig().save();
        }
        player.openInventory(getInventory());
    }

    private void increasePoints() {
        int newPoints = MainConfig.POINTS_PRICE.getValue() + 1;
        MainConfig.POINTS_PRICE.setValue(newPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.POINTS_PRICE.getPath(), newPoints);
        skill.getConfig().save();
    }

    private void decreasePoints() {
        int newPoints = Math.max(0, MainConfig.POINTS_PRICE.getValue() - 1);
        MainConfig.POINTS_PRICE.setValue(newPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.POINTS_PRICE.getPath(), newPoints);
        skill.getConfig().save();
    }

    private void toggleFundingSource() {
        FundingSource currentFundingSource = MainConfig.POINTS_FUNDING_SOURCE.getValue();
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException e) {
            player.sendMessage(ChatColor.RED + "Vault or Economy is not installed. Please install Vault to use economy features.");
            return;
        }
        FundingSource newFundingSource = (currentFundingSource instanceof VaultFundingSource)
        ? new XPFundingSource()
        : new VaultFundingSource();

        String newFundingSourceIdentifier = (newFundingSource instanceof VaultFundingSource) ? "VAULT" : "XP";

        MainConfig.POINTS_FUNDING_SOURCE.setValue(newFundingSource);
        player.openInventory(getInventory());

        skill.getConfig().set(MainConfig.POINTS_FUNDING_SOURCE.getPath(), newFundingSourceIdentifier);
        skill.getConfig().save();
    }

    private void increaseIncrementedPoints() {
        int newPoints = MainConfig.POINTS_INCREMENT_PRICE.getValue() + 1;
        MainConfig.POINTS_INCREMENT_PRICE.setValue(newPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.POINTS_INCREMENT_PRICE.getPath(), newPoints);
        skill.getConfig().save();
    }

    private void decreaseIncrementedPoints() {
        int newPoints = Math.max(0, MainConfig.POINTS_INCREMENT_PRICE.getValue() - 1);
        MainConfig.POINTS_INCREMENT_PRICE.setValue(newPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.POINTS_INCREMENT_PRICE.getPath(), newPoints);
        skill.getConfig().save();
    }

    private void togglePointsConfirmation(int slot) {
        if (slot == MainConfig.POINTS_CONFIRMATION_TOGGLE_SLOT.getValue()) {
        boolean confirmationStatus = !MainConfig.CONFIRMATION_PURCHASE_POINTS.getValue();
        MainConfig.CONFIRMATION_PURCHASE_POINTS.setValue(confirmationStatus);
        skill.getConfig().set(MainConfig.CONFIRMATION_PURCHASE_POINTS.getPath(),
                MainConfig.CONFIRMATION_PURCHASE_POINTS.getValue());
        skill.getConfig().save();
        }
        player.openInventory(getInventory());
    }

    private void increaseResetPrice() {
        int newResetPoints = MainConfig.RESET_PRICE.getValue() + 1;
        MainConfig.RESET_PRICE.setValue(newResetPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.RESET_PRICE.getPath(), newResetPoints);
        skill.getConfig().save();
    }

    private void decreaseResetPrice() {
        int newResetPoints = Math.max(0, MainConfig.RESET_PRICE.getValue() - 1);
        MainConfig.RESET_PRICE.setValue(newResetPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.RESET_PRICE.getPath(), newResetPoints);
        skill.getConfig().save();
    }

    private void increaseIncrementResetPrice() {
        int newIncrementResetPoints = MainConfig.RESET_INCREMENT_PRICE.getValue() + 1;
        MainConfig.RESET_INCREMENT_PRICE.setValue(newIncrementResetPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.RESET_INCREMENT_PRICE.getPath(), newIncrementResetPoints);
        skill.getConfig().save();
    }

    private void decreaseIncrementResetPrice() {
        int newIncrementResetPoints = Math.max(0, MainConfig.RESET_INCREMENT_PRICE.getValue() - 1);
        MainConfig.RESET_INCREMENT_PRICE.setValue(newIncrementResetPoints);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.RESET_INCREMENT_PRICE.getPath(), newIncrementResetPoints);
        skill.getConfig().save();
    }

    private void toggleRefundPoints() {
        boolean newRefundStatus = !MainConfig.REFUND_POINTS.getValue();
        MainConfig.REFUND_POINTS.setValue(newRefundStatus);
        player.openInventory(getInventory());
        skill.getConfig().set(MainConfig.REFUND_POINTS.getPath(), newRefundStatus);
        skill.getConfig().save();
    }

    private void toggleResetConfirmation(int slot) {
        if (slot == MainConfig.RESET_CONFIRMATION_TOGGLE_SLOT.getValue()) {
            boolean confirmationStatus = !MainConfig.CONFIRMATION_RESET_SKILLS.getValue();
            MainConfig.CONFIRMATION_RESET_SKILLS.setValue(confirmationStatus);
            skill.getConfig().set(MainConfig.CONFIRMATION_RESET_SKILLS.getPath(), MainConfig.CONFIRMATION_RESET_SKILLS.getValue());
            skill.getConfig().save();
        }
        player.openInventory(getInventory());
    }
}
