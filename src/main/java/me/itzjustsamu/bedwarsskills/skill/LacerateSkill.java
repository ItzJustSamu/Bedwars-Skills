package me.itzjustsamu.bedwarsskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.bukkit.scheduler.Scheduler;
import me.hsgamer.hscore.bukkit.scheduler.Task;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.impl.Paths;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;
import me.itzjustsamu.bedwarsskills.util.modifier.XMaterialModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

public class LacerateSkill extends Skill {
    private final ConfigPath<Integer> bleedCycles = Paths.integerPath(new PathString("bleed-cycles"), 8);
    private final ConfigPath<Long> bleedInterval = Paths.longPath(new PathString("bleed-interval"), 50L);
    private final ConfigPath<Integer> bleedDamage = Paths.integerPath(new PathString("bleed-damage"), 2);
    private final ConfigPath<Boolean> applyToNonPlayers = Paths.booleanPath(new PathString("apply-to-non-players"), false);
    private final ConfigPath<String> bleedingEnemy = Paths.stringPath(new PathString("bleeding-enemy-message"), "&a*** ENEMY BLEEDING ***");
    private final ConfigPath<String> bleedingSelf = Paths.stringPath(new PathString("bleeding-self-message"), "&c*** YOU ARE BLEEDING ***");

    private final Map<LivingEntity, Task> cutEntities = new ConcurrentHashMap<>();

    public LacerateSkill(BedWarsSkills plugin) {
        super(plugin, "Lacerate", "lacerate", 20, 8);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || (!(event.getEntity() instanceof Player) && (!applyToNonPlayers.getValue()))) {
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

        int lacerateLevel = getLevel(sPlayer);

        double chance = lacerateLevel * getUpgrade().getValue();

        double randomValue = ThreadLocalRandom.current().nextDouble();
        if (randomValue < chance / 100.0) {
            LivingEntity victim = (LivingEntity) event.getEntity();

            bleed(victim);

            String message = bleedingEnemy.getValue();
            if (!message.isEmpty()) {
                MessageUtils.sendMessage(player, message, "");
            }
            String victimMesssage = bleedingSelf.getValue();
            if (!victimMesssage.isEmpty()) {
                MessageUtils.sendMessage(victim, victimMesssage, "");
            }
        }
    }

    private void bleed(LivingEntity player) {
        if (cutEntities.containsKey(player)) {
            return;
        }
        BooleanSupplier runnable = new BooleanSupplier() {
            int times = 0;

            @Override
            public boolean getAsBoolean() {
                player.damage(bleedDamage.getValue(), null);
                times++;
                if (times >= bleedCycles.getValue()) {
                    cutEntities.remove(player);
                    return false;
                }
                return true;
            }
        };
        Task task = Scheduler.plugin(getPlugin()).sync().runEntityTaskTimer(player, runnable, bleedInterval.getValue(), bleedInterval.getValue());
        cutEntities.put(player, task);
    }

    private void cancelTask(LivingEntity livingEntity) {
        Optional.ofNullable(cutEntities.get(livingEntity)).ifPresent(task -> {
            try {
                task.cancel();
            } catch (Exception ignored) {
                // IGNORED
            }
        });
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        cancelTask(event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelTask(event.getPlayer());
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Arrays.asList(getUpgrade(), bleedCycles, bleedInterval, bleedDamage, applyToNonPlayers);
    }

    @Override
    public List<ConfigPath<?>> getMessageConfigPaths() {
        return Arrays.asList(bleedingEnemy, bleedingSelf);
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cLacerate Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.REDSTONE))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill increases the chance of making an enemy bleed.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cCut chance: ",
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

    @Override
    public String getCoolDownPreviousString(SPlayer player) {
        return null;
    }

    @Override
    public String getCoolDownNextString(SPlayer player) {
        return null;
    }
}
