package me.itzjustsamu.playerskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.impl.Paths;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.playerskills.PlayerSkills;
import me.itzjustsamu.playerskills.config.MainConfig;
import me.itzjustsamu.playerskills.player.SPlayer;
import me.itzjustsamu.playerskills.util.Utils;
import me.itzjustsamu.playerskills.util.VersionControl;
import me.itzjustsamu.playerskills.util.modifier.XMaterialModifier;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DoubleJumpSkill extends Skill implements Listener {
    private final ConfigPath<Long> Base_Cooldown_Time = Paths.longPath(new PathString("base-cooldown"), 300000L); // 5 mins
    private final ConfigPath<Long> Cooldown_Reduction_Per_Level = Paths.longPath(new PathString("cooldown-reduction-per-level"), 5000L); // 2 seconds per level
    private final ConfigPath<String> Cooldown_Message = Paths.stringPath(new PathString("cooldown-message"), "&cDoubleJump cooldown: &e{remaining_time} seconds.");

    private final HashMap<Player, Boolean> Cooldown = new HashMap<>();
    private final HashMap<Player, Long> Cooldown_Map = new HashMap<>();
    private final HashMap<Player, Boolean> Jumped = new HashMap<>();

    public DoubleJumpSkill(PlayerSkills plugin) {
        super(plugin, "DoubleJump", "doublejump", 5, 5);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && Jumped.getOrDefault(player, false)) {
            event.setCancelled(true);
            Jumped.put(player, false);
        }
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        SPlayer sPlayer = SPlayer.get(player.getUniqueId());
        if (sPlayer == null || getLevel(sPlayer) == 0) {
            player.setAllowFlight(false);
            return;
        }

        player.setAllowFlight(!Cooldown_Map.containsKey(player) || System.currentTimeMillis() >= Cooldown_Map.get(player));
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || isWorldRestricted(player) || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Check if the player is on cooldown for double jump
        if (Cooldown_Map.containsKey(player) && System.currentTimeMillis() < Cooldown_Map.get(player)) {
            // If the player hasn't double jumped, cancel the event and disable flight mode
            if (!Jumped.getOrDefault(player, false)) {
                event.setCancelled(true);
                player.setAllowFlight(false);
            }
        } else {
            doubleJump(player);
        }
    }

    private void doubleJump(Player player) {
        SPlayer sPlayer = SPlayer.get(player.getUniqueId());

        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Failed event. SPlayer for " + player.getUniqueId() + " is null.");
            }
            return;
        }

        // Check if the player has a jump level greater than 0
        if (getLevel(sPlayer) > 0) {
            // Calculate the jump height based on the player's jump level
            double jumpHeight = getLevel(sPlayer) * getUpgrade().getValue();

            // Calculate the cooldown time based on the player's level
            long cooldownTime = Base_Cooldown_Time.getValue() - (getLevel(sPlayer) * Cooldown_Reduction_Per_Level.getValue());

            // Set cooldown for the double jump
            Cooldown_Map.put(player, System.currentTimeMillis() + cooldownTime);

            // Calculate the direction for the double jump
            Vector direction = player.getLocation().getDirection().normalize();

            // Multiply the normalized direction vector by the jump height to get the final velocity
            Vector velocity = direction.multiply(jumpHeight);

            player.setAllowFlight(true);

            // Set the player's velocity for the double jump
            player.setVelocity(velocity);

            // Schedule task to disable flight after a short delay
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> player.setAllowFlight(false), 10L); // Delay of 0.5 seconds (10 ticks)
            // Display remaining cooldown time as an action bar message
            long remainingTime = (Cooldown_Map.get(player) - System.currentTimeMillis()) / 1000L;
            sendActionBar(player, remainingTime);

            // Set HasDoubleJumped to true only if the player is in the air
            if (player.getLocation().getY() % 1 != 0) {
                Jumped.put(player, true);
            }
        } else {
            // Reset HasDoubleJumped to false if the player doesn't have a jump level
            Jumped.put(player, false);
        }
    }

    @EventHandler
    public void onSneak(final PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (player.getLocation().getY() % 1 == 0 && Cooldown.get(player) != null && !Cooldown.get(player)) {
            Cooldown.put(player, true);
            player.setVelocity(new Vector());
        }
    }

    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Arrays.asList(getUpgrade(), Base_Cooldown_Time, Cooldown_Reduction_Per_Level, Cooldown_Message);
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cDouble Jump Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.LEATHER_BOOTS))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill allows you to perform a double jump with cooldown based on skill level.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cVelocity increase: ",
                        "   &e{prev} &7 >>> &e{next}"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double jumpHeight = getLevel(player) * getUpgrade().getValue();
        return String.valueOf(jumpHeight);
    }

    @Override
    public String getNextString(SPlayer player) {
        double jumpHeight = (getLevel(player) + 1) * getUpgrade().getValue();
        return String.valueOf(jumpHeight);
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
