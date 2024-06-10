package me.itzjustsamu.playerskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.itzjustsamu.playerskills.PlayerSkills;
import me.itzjustsamu.playerskills.config.MainConfig;
import me.itzjustsamu.playerskills.player.SPlayer;
import me.itzjustsamu.playerskills.util.Utils;
import me.itzjustsamu.playerskills.util.VersionControl;
import me.itzjustsamu.playerskills.util.modifier.XMaterialModifier;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.bukkit.scheduler.Scheduler;
import me.hsgamer.hscore.bukkit.scheduler.Task;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.impl.Paths;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class HealthSkill extends Skill {
    private final ConfigPath<Boolean> compatibilityMode = Paths.booleanPath(new PathString("compatibility-mode"), false);
    private final Map<UUID, Integer> knownMaxHealth = new IdentityHashMap<>();
    private Task task;

    public HealthSkill(PlayerSkills plugin) {
        super(plugin, "Health", "health", 20, 5);
    }

    @Override
    public void enable() {
        Runnable runnable = () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                SPlayer sPlayer = SPlayer.get(uuid);
                if (sPlayer == null) {
                    if (MainConfig.isVerboseLogging()) {
                        Utils.logError("Failed event. SPlayer for " + uuid + " is null.");
                    }
                    continue;
                }
                if (isWorldRestricted(player)) {
                    clearPlayer(player);
                    return;
                }
                int hpNeeded = getLevel(sPlayer) * (getUpgrade().getValue() * 2);
                if (hpNeeded != knownMaxHealth.getOrDefault(uuid, 0)) {
                    knownMaxHealth.put(player.getUniqueId(), hpNeeded);
                    clearModifier(player);
                    try {
                        updateHealth(player, hpNeeded);
                    } catch (final Exception ignored) {
                    }
                }
            }
        };
        long tick = compatibilityMode.getValue() ? 1L : 20L;
        task = Scheduler.plugin(getPlugin()).sync().runTaskTimer(runnable, tick, tick);
    }

    @Override
    public void disable() {
        super.disable();
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
                // IGNORED
            }
        }
    }

    private void updateHealth(Player player, int amount) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (VersionControl.isNewVersion()) {
            try {
                Object attribute = player.getClass().getMethod("getAttribute", Attribute.class).invoke(player, Attribute.GENERIC_MAX_HEALTH);
                if (attribute != null) {
                    double currentBaseValue = (double) attribute.getClass().getMethod("getBaseValue").invoke(attribute);
                    double newBaseValue = Math.max(20, currentBaseValue + amount);
                    attribute.getClass().getMethod("setBaseValue", double.class).invoke(attribute, newBaseValue);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Handle the exception or log it
            }
        } else {
            double newMaxHealth = Math.max(20, player.getMaxHealth() + amount);
            player.getClass().getMethod("setMaxHealth", double.class).invoke(player, newMaxHealth);
        }
    }


    private void clearPlayer(Player player) {
        knownMaxHealth.remove(player.getUniqueId());
        clearModifier(player);
    }

    private void clearModifier(Player player) {
        if (VersionControl.isNewVersion()) {
            try {
                Object attribute = player.getClass().getMethod("getAttribute", Attribute.class).invoke(player, Attribute.GENERIC_MAX_HEALTH);
                if (attribute != null) {
                    Iterable<?> modifiers = (Iterable<?>) attribute.getClass().getMethod("getModifiers").invoke(attribute);
                    for (Object modifier : modifiers) {
                        String name = (String) modifier.getClass().getMethod("getName").invoke(modifier);
                        if ("PlayerSkillsHealth".equals(name)) {
                            attribute.getClass().getMethod("removeModifier", modifier.getClass()).invoke(attribute, modifier);
                        }
                    }
                    attribute.getClass().getMethod("setBaseValue", double.class).invoke(attribute, 20.0); // Reset to the default max health value (10 hearts)
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Handle the exception or log it
            }
        } else {
            try {
                player.getClass().getMethod("setMaxHealth", double.class).invoke(player, 20.0); // Reset to the default max health value (10 hearts)
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Handle the exception or log it
            }
        }
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearPlayer(event.getPlayer());
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Arrays.asList(getUpgrade(), compatibilityMode);
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cHealth Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.APPLE))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill increases the amount of health you have.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cExtra hearts: ",
                        "   &e{prev}❤ &7 >>> &e{next}❤"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        int hp = getLevel(player) * getUpgrade().getValue();
        return Integer.toString(hp);
    }

    @Override
    public String getNextString(SPlayer player) {
        int hp = (getLevel(player) + 1) * getUpgrade().getValue();
        return Integer.toString(hp);
    }
}
