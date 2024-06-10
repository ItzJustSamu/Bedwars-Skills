package me.itzjustsamu.playerskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.impl.Paths;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.playerskills.PlayerSkills;
import me.itzjustsamu.playerskills.config.MainConfig;
import me.itzjustsamu.playerskills.player.SPlayer;
import me.itzjustsamu.playerskills.util.Utils;
import me.itzjustsamu.playerskills.util.modifier.XMaterialModifier;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static me.itzjustsamu.playerskills.util.Utils.getPercentageFormat;

public class ShadowStepSkill extends Skill {
    private final ConfigPath<String> TELEPORT_MESSAGE = Paths.stringPath(new PathString("teleport-message"), "&a*** SNEAK ATTACK ***");
    private final ConfigPath<Long> COOLDOWN_DURATION = Paths.longPath(new PathString("cooldown-duration"), 5000L); // 5000 milliseconds (5 seconds)
    private final ConfigPath<String> COOLDOWN_MESSAGE = Paths.stringPath(new PathString("cooldown-message"), "&cShadowStep cooldown: {remaining_time} seconds.");

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Random random = new Random();

    public ShadowStepSkill(PlayerSkills plugin) {
        super(plugin, "ShadowStep", "shadowstep", 20, 10);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getEntity();
        Player player = (Player) event.getDamager();

        if (isWorldRestricted(player)) {
            return;
        }

        SPlayer sPlayer = SPlayer.get(player.getUniqueId());

        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Failed event. SPlayer for " + player.getUniqueId() + " is null.");
            }
            return;
        }

        // Check cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            Long cooldownEndTime = cooldowns.get(player.getUniqueId());
            if (cooldownEndTime != null && System.currentTimeMillis() < cooldownEndTime) {
                // Check if the player's level is above 1 before sending the cooldown message
                if (getLevel(sPlayer) > 1) {
                    sendActionBar(player, cooldownEndTime);
                }
                return;
            }
        }

        if (getLevel(sPlayer) > 0) {
            // Check if teleportation should occur
            if (shouldTeleport(sPlayer)) {
                // Teleport behind the target
                teleportBehindTarget(player, target);

                // Send the teleport message
                String message = TELEPORT_MESSAGE.getValue();
                if (!message.isEmpty()) {
                    MessageUtils.sendMessage(player, message, "");
                }
                MessageUtils.sendMessage(player, message, "");
                // Set cooldown
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_DURATION.getValue());
            }
        }
    }

    private boolean shouldTeleport(SPlayer sPlayer) {
        int playerLevel = Math.max(0, getLevel(sPlayer));  // Ensure playerLevel is at least 0
        double chance = playerLevel * getUpgrade().getValue();
        return random.nextDouble() * 70 < chance;
    }

    private void teleportBehindTarget(Player player, Player target) {
        Location targetLocation = target.getLocation().add(target.getLocation().getDirection().multiply(-1));
        Location finalLocation = findSafeLocation(targetLocation);

        // Implement teleportation logic here
        player.teleport(finalLocation);
    }

    private Location findSafeLocation(Location location) {
        Block block = location.getBlock();
        Block feet = block.getRelative(0, 1, 0);

        if (!feet.getType().isSolid()) {
            return location;
        }

        Location newLocation = location.clone();
        newLocation.setY(location.getY() + 2);

        return newLocation;
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return List.of(getUpgrade(), TELEPORT_MESSAGE, COOLDOWN_DURATION, COOLDOWN_MESSAGE);
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cShadow Step"))
                .addItemModifier(new XMaterialModifier(XMaterial.NETHER_STAR))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill increases the chance to teleport behind the target in combat.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cTeleport Chance: ",
                        "   &e{prev}% &7 >>> &e{next}%"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double chance = getLevel(player) * getUpgrade().getValue();
        return getPercentageFormat().format(chance);
    }

    @Override
    public String getNextString(SPlayer player) {
        double chance = (getLevel(player) + 1) * getUpgrade().getValue();
        return getPercentageFormat().format(chance);
    }

    private void sendActionBar(Player player, long cooldownEndTime) {
        long remainingTime = (cooldownEndTime - System.currentTimeMillis()) / 1000L;

        new BukkitRunnable() {
            long timeLeft = remainingTime;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    String actionBarMessage = ChatColor.translateAlternateColorCodes('&', COOLDOWN_MESSAGE.getValue())
                            .replace("{remaining_time}", String.valueOf(timeLeft));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                    timeLeft--;
                } else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    cancel(); // Stop the task when the cooldown ends
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 20L); // Update every second
    }
}
