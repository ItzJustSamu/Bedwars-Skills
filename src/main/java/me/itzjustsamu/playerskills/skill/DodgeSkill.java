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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// ... (other imports and package declaration)

public class DodgeSkill extends Skill {
    private final ConfigPath<String> dodgeMessage = Paths.stringPath(new PathString("dodge-message"), "&a*** ATTACK DODGED ***");
    private final ConfigPath<Long> COOLDOWN_DURATION = Paths.longPath(new PathString("cooldown-duration"), 5000L); // 5000 milliseconds (5 seconds)
    private final ConfigPath<String> COOLDOWN_MESSAGE = Paths.stringPath(new PathString("cooldown-message"), "&cDodge cooldown: &e{remaining_time} seconds.");

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public DodgeSkill(PlayerSkills plugin) {
        super(plugin, "Dodge", "dodge", 20, 4);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
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
                // Send cooldown message
                String cooldownMessage = COOLDOWN_MESSAGE.getValue()
                        .replace("{remaining_time}", String.valueOf((cooldownEndTime - System.currentTimeMillis()) / 1000L));
                MessageUtils.sendMessage(player, cooldownMessage, "");
                return;
            }
        }

        int dodgeLevel = getLevel(sPlayer);

        double chance = dodgeLevel * getUpgrade().getValue();
        String message = dodgeMessage.getValue();
        if (ThreadLocalRandom.current().nextDouble(100) < chance) {
            if (!message.isEmpty()) {
                MessageUtils.sendMessage(player, message, "");
            }
            event.setCancelled(true);

            // Set cooldown
            cooldowns.computeIfAbsent(player.getUniqueId(), key -> System.currentTimeMillis() + COOLDOWN_DURATION.getValue());
        }
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Arrays.asList(getUpgrade(), COOLDOWN_DURATION, COOLDOWN_MESSAGE);
    }

    @Override
    public List<ConfigPath<?>> getMessageConfigPaths() {
        return Collections.singletonList(dodgeMessage);
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cDodge Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.SUGAR))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill gives a chance to completely dodge attacks.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cDodge chance: ",
                        "   &e{prev}% &7 >>> &e{next}%"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double damage = getLevel(player) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(damage);
    }

    @Override
    public String getNextString(SPlayer player) {
        double damage = (getLevel(player) + 1) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(damage);
    }
}