package me.itzjustsamu.playerskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.playerskills.PlayerSkills;
import me.itzjustsamu.playerskills.config.MainConfig;
import me.itzjustsamu.playerskills.player.SPlayer;
import me.itzjustsamu.playerskills.util.Utils;
import me.itzjustsamu.playerskills.util.modifier.XMaterialModifier;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class XPSkill extends Skill {

    public XPSkill(PlayerSkills plugin) {
        super(plugin, "XP", "xp", 20, 13);
    }

    // Event handler for hit event
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return; // The entity wasn't killed by a player
        }

        Player killer = event.getEntity().getKiller();

        if (isWorldRestricted(killer)) {
            return;
        }

        SPlayer sPlayer = SPlayer.get(killer.getUniqueId());

        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Failed event. SPlayer for " + killer.getUniqueId() + " is null.");
            }
            return;
        }

        double reductionFactor = 0.02;
        int baseXp = getLevel(sPlayer) + killer.getTotalExperience() * getUpgrade().getValue();
        int reducedXp = (int) (baseXp * reductionFactor);

        // Set the new experience points
        killer.setTotalExperience(reducedXp);

        // Update the player's visible XP bar
        killer.setLevel(killer.getLevel() + reducedXp);
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

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


        // Check if the mined block should yield experience
        if (blockDropsExperience(block.getType())) {
            // Calculate the additional XP from mining
            double XP = getLevel(sPlayer) + player.getTotalExperience() * getUpgrade().getValue();

            // Set the new experience points
            player.setTotalExperience((int) XP);
        }
    }

    // Helper method to check if a block drops experience
    private boolean blockDropsExperience(Material blockType) {
        // You can define which block types drop experience here
        return blockType == Material.COAL_ORE || blockType == Material.IRON_ORE
                || blockType == Material.GOLD_ORE || blockType == Material.DIAMOND_ORE;
        // Add more block types as needed
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Collections.singletonList(getUpgrade());
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cXPSkill Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.EXPERIENCE_BOTTLE))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill increases the amount of XP gained per hit.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cXP Increase: ",
                        "   &e{prev}% &7 >>> &e{next}%"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double xp = getLevel(player) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(xp);
    }

    @Override
    public String getNextString(SPlayer player) {
        double xp = (getLevel(player) + 1) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(xp);
    }
}
