package me.itzjustsamu.bedwarsskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.impl.Paths;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;
import me.itzjustsamu.bedwarsskills.util.VersionControl;
import me.itzjustsamu.bedwarsskills.util.modifier.XMaterialModifier;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class KillRewardsSkill extends Skill implements Listener {
    private final ConfigPath<Long> Base_Cooldown_Time = Paths.longPath(new PathString("base-cooldown"), 60000L); // 1 min
    private final ConfigPath<Long> Cooldown_Reduction_Per_Level = Paths.longPath(new PathString("cooldown-reduction-per-level"), 5000L); // 5 seconds per level
    private final ConfigPath<String> Cooldown_Message = Paths.stringPath(new PathString("cooldown-message"), "&cKill Rewards cooldown: &e{remaining_time} seconds.");

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public KillRewardsSkill(BedWarsSkills plugin) {
        super(plugin, "Kill Rewards", "kill_rewards", 20, 6);
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        UUID uniqueId = killer.getUniqueId();
        SPlayer sPlayer = SPlayer.get(uniqueId);

        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Failed event. SPlayer for " + uniqueId + " is null.");
            }
            return;
        }

        int level = getLevel(sPlayer);

        if (isCooldownActive(killer)) {
            sendCooldownMessage(killer);
            return;
        }

        double chance = level * getUpgrade().getValue();

        if (new Random().nextDouble() * 100 < chance) {
            giveRandomResources(killer, level);
            applyPotionEffects(killer, level);
            setCooldown(killer);
        }
    }

    private boolean isCooldownActive(Player player) {
        UUID playerId = player.getUniqueId();
        if (cooldowns.containsKey(playerId)) {
            long cooldownEnd = cooldowns.get(playerId);
            if (System.currentTimeMillis() < cooldownEnd) {
                return true;
            } else {
                cooldowns.remove(playerId);
            }
        }
        return false;
    }

    private void sendCooldownMessage(Player player) {
        long remainingTime = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
        String message = ChatColor.translateAlternateColorCodes('&', Cooldown_Message.getValue())
                .replace("{remaining_time}", String.valueOf(remainingTime));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    private void setCooldown(Player player) {
        SPlayer sPlayer = SPlayer.get(player.getUniqueId());
        long cooldownTime = Base_Cooldown_Time.getValue() - (getLevel(sPlayer) * Cooldown_Reduction_Per_Level.getValue());
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);
        sendActionBar(player, cooldownTime / 1000);
    }

    private void giveRandomResources(Player player, int level) {
        Material[] resources = {
                Material.EMERALD,
                Material.GOLD_INGOT,
                Material.DIAMOND,
                Material.IRON_INGOT
        };

        for (Material resource : resources) {
            ItemStack itemStack = new ItemStack(resource, randomAmount(level));
            player.getInventory().addItem(itemStack);
        }
    }

    private int randomAmount(int level) {
        return new Random().nextInt(level) + 1;
    }

    private void applyPotionEffects(Player player, int level) {
        int duration = level * 20; // Duration in ticks (1 second per level)
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration, level - 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, level - 1, false, false, true));
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Arrays.asList(getUpgrade(), Base_Cooldown_Time, Cooldown_Reduction_Per_Level, Cooldown_Message);
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&eKill Rewards Skill Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.DIAMOND_SWORD))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill gives you a chance to receive resources and",
                        "&7temporary effects when you kill another player.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&eRewards Chance: ",
                        "   &e{prev}%&7 >>> &e{next}%"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double chance = getLevel(player) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(chance);
    }

    @Override
    public String getNextString(SPlayer player) {
        double chance = (getLevel(player) + 1) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(chance);
    }

    @Override
    public String getCoolDownPreviousString(SPlayer player) {
        return null;
    }

    @Override
    public String getCoolDownNextString(SPlayer player) {
        return null;
    }

    private void sendActionBar(Player player, long remainingTime) {
        if (VersionControl.isOldVersion()) {
            sendActionBarLegacy(player, remainingTime);
        } else {
            sendActionBarModern(player, remainingTime);
        }
    }

    private void sendActionBarLegacy(Player player, long remainingTime) {
        new BukkitRunnable() {
            long timeLeft = remainingTime;
            long ticks = 0;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    ticks++;
                    if (ticks % 100 == 0) { // 100 ticks = 5 seconds
                        String actionBarMessage = ChatColor.translateAlternateColorCodes('&', Cooldown_Message.getValue())
                                .replace("{remaining_time}", String.valueOf(timeLeft));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', actionBarMessage));
                        ticks = 0; // Reset ticks count
                    }
                    timeLeft--;
                } else {
                    player.sendMessage(""); // Clear the action bar
                    cancel(); // Stop the task when the cooldown ends
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 1L); // Update every tick
    }

    private void sendActionBarModern(Player player, long remainingTime) {
        new BukkitRunnable() {
            long timeLeft = remainingTime;
            long ticks = 0;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    ticks++;
                    if (ticks % 20 == 0) { // 20 ticks = 1 second
                        String actionBarMessage = ChatColor.translateAlternateColorCodes('&', Cooldown_Message.getValue())
                                .replace("{remaining_time}", String.valueOf(timeLeft));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMessage));
                        timeLeft--;
                    }
                } else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
                    cancel();
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 1L); // Update every tick
    }
}
