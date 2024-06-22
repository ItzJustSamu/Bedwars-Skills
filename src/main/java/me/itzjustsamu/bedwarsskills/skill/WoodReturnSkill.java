package me.itzjustsamu.bedwarsskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;
import me.itzjustsamu.bedwarsskills.util.VersionControl;
import me.itzjustsamu.bedwarsskills.util.modifier.XMaterialModifier;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WoodReturnSkill extends Skill {

    public WoodReturnSkill(BedWarsSkills plugin) {
        super(plugin, "Wood Return", "wood return", 20, 12);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        SPlayer sPlayer = SPlayer.get(uniqueId);

        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Failed event. SPlayer for " + uniqueId + " is null.");
            }
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Material blockType = event.getBlock().getType();
        if (isDiggable(blockType)) {
            Location blockLocation = event.getBlock().getLocation();

            // Schedule a task to check if the block is broken after 1 second
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Check if the block is still there (indicating region protection)
                    if (blockLocation.getBlock().getType() != blockType) {
                        int level = getLevel(sPlayer);
                        double chance = level * getUpgrade().getValue();

                        if (new Random().nextDouble() * 100 < chance) {
                            ItemStack treasure = getRandomTreasure();
                            Objects.requireNonNull(blockLocation.getWorld()).dropItemNaturally(blockLocation, treasure);
                        }
                    }
                    cancel();
                }
            }.runTaskLater(getPlugin(), 20L); // 20L = 1 second
        }
    }

    private boolean isDiggable(Material material) {
        return material == XMaterial.OAK_PLANKS.parseMaterial();
    }

    private ItemStack getRandomTreasure() {
        Material[] treasures;

        if (VersionControl.isOldVersion()) {
            treasures = new Material[]{
                    Material.GOLD_INGOT,
            };
        } else {
            treasures = new Material[]{
                    Material.GOLD_INGOT,
            };
        }

        return new ItemStack(treasures[new Random().nextInt(treasures.length)]);
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Collections.singletonList(getUpgrade());
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&eWood Return Skill Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.OAK_WOOD))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill gives you a chance to dig gold.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&eReturn Chance: ",
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
}
