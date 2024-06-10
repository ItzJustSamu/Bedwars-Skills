package me.itzjustsamu.bedwarsskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedwarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;
import me.itzjustsamu.bedwarsskills.util.VersionControl;
import me.itzjustsamu.bedwarsskills.util.modifier.XMaterialModifier;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;

public class SpeedSkill extends Skill implements Listener {

    public SpeedSkill(BedwarsSkills plugin) {
        super(plugin, "Speed", "speed", 10, 9);
        startSpeedUpdateTask();
    }

    private void startSpeedUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getPlugin().getServer().getOnlinePlayers()) {
                    applySpeedModifier(player);
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 20L); // Runs every second (20 ticks)
    }

    public void applySpeedModifier(Player player) {
        if (isWorldRestricted(player)) {
            return;
        }

        SPlayer sPlayer = SPlayer.get(player.getUniqueId());
        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Failed to apply speed modifier. SPlayer for " + player.getUniqueId() + " is null.");
            }
            return;
        }

        int level = getLevel(sPlayer);
        if (level > 0) {
            double speedIncrease = Math.min(1.0, level * getUpgrade().getValue() * 0.02); // Limit increase to 0.8
            try {
                updateSpeed(player, speedIncrease);
            } catch (Exception ex) {
                if (MainConfig.isVerboseLogging()) {
                    Utils.logError("Error applying speed modifier to " + player.getName(), ex);
                }
            }
        } else {
            clearSpeedModifier(player); // Reset to default speed if level is 0
        }
    }

    private void clearSpeedModifier(Player player) {
        try {
            updateSpeed(player, 0.2); // Reset to default speed
        } catch (Exception ex) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Error clearing speed modifier from " + player.getName(), ex);
            }
        }
    }

    private void updateSpeed(Player player, double speed) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double adjustedSpeed = Math.max(0.2, Math.min(1.0, speed));
        if (VersionControl.isNewVersion()) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (attribute != null) {
                attribute.setBaseValue(adjustedSpeed);
                if (MainConfig.isVerboseLogging()) {
                    Utils.logInfo("Updated speed for " + player.getUniqueId() + " to " + adjustedSpeed);
                }
            } else {
                if (MainConfig.isVerboseLogging()) {
                    Utils.logError("Attribute GENERIC_MOVEMENT_SPEED is null for player " + player.getUniqueId());
                }
            }
        } else {
            float newSpeed = (float) adjustedSpeed;
            player.getClass().getMethod("setWalkSpeed", float.class).invoke(player, newSpeed);
            if (MainConfig.isVerboseLogging()) {
                Utils.logInfo("Updated walk speed for " + player.getUniqueId() + " to " + newSpeed);
            }
        }
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&bSpeed Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.IRON_BOOTS))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill increases your movement speed.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&bSpeed Increase: ",
                        "   &e{prev}% &7 >>> &e{next}%"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double speed = getLevel(player) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(speed);
    }

    @Override
    public String getNextString(SPlayer player) {
        double speed = (getLevel(player) + 1) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(speed);
    }

}
