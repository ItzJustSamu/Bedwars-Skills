package me.itzjustsamu.bedwarsskills.skill;

import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;
import me.itzjustsamu.bedwarsskills.util.modifier.XMaterialModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AcrobatSkill extends Skill {

    public AcrobatSkill(BedWarsSkills plugin) {
        super(plugin, "Acrobat", "acrobat", 20, 0);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        UUID uniqueId = player.getUniqueId();
        SPlayer sPlayer = SPlayer.get(uniqueId);

        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError("Failed event. SPlayer for " + uniqueId + " is null.");
            }
            return;
        }

        // Check if the damage cause is fall
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        // Reduce fall damage based on skill level
        double reduction = (double) (getLevel(sPlayer) * getUpgrade().getValue()) / 100; // Reduce damage by a percentage
        double damage = event.getDamage() * (1 - reduction);
        event.setDamage(Math.max(damage, 0)); // Ensure damage doesn't go negative
    }

    @Override
    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Collections.singletonList(getUpgrade());
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&eAcrobat Skill Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.FEATHER)) // Display a feather for visual representation
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill reduces fall damage.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&eFall Damage Reduction: ",
                        "   &e{prev}%&7 >>> &e{next}%"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double reduction = getLevel(player) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(reduction);
    }

    @Override
    public String getNextString(SPlayer player) {
        double reduction = (getLevel(player) + 1) * getUpgrade().getValue();
        return Utils.getPercentageFormat().format(reduction);
    }
}
