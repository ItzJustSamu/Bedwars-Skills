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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CriticalsSkill extends Skill {
    private final ConfigPath<Double> CRITICAL_MULTIPLIER = Paths.doublePath(new PathString("critical-multiplier"), 1.5D);
    private final ConfigPath<String> CRITICAL_MESSAGE = Paths.stringPath(new PathString("critical-message"), "&a*** CRITICAL HIT ***");

    public CriticalsSkill(PlayerSkills plugin) {
        super(plugin, "Criticals", "criticals", 20, 3);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

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

        int playerLevel = getLevel(sPlayer);

        double criticalMultiplier = CRITICAL_MULTIPLIER.getValue();
        double chance = playerLevel * getUpgrade().getValue();
        String criticalMessage = CRITICAL_MESSAGE.getValue();

        if (ThreadLocalRandom.current().nextDouble() < chance / 100.0) {
            if (!criticalMessage.isEmpty()) {
                MessageUtils.sendMessage(player, criticalMessage, "");
            }
            event.setDamage(event.getDamage() * criticalMultiplier);
        }

    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Arrays.asList(getUpgrade(), CRITICAL_MULTIPLIER);
    }

    @Override
    public List<ConfigPath<?>> getMessageConfigPaths() {
        return List.of(CRITICAL_MESSAGE);
    }
    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cCriticals Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.BLAZE_POWDER))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill gives a chance to deal a critical (150%) shot.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cCritical chance: ",
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
        double damage = getLevel(player) + getUpgrade().getValue();
        return Utils.getPercentageFormat().format(damage);
    }
}
