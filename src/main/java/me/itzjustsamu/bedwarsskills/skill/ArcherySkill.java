package me.itzjustsamu.bedwarsskills.skill;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;
import me.itzjustsamu.bedwarsskills.util.modifier.XMaterialModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import static me.itzjustsamu.bedwarsskills.util.Utils.getPercentageFormat;

public class ArcherySkill extends Skill {


    public ArcherySkill(BedWarsSkills plugin) {
        super(plugin, "Archery", "archery", 20, 1);
    }


    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) arrow.getShooter();

        if (isWorldRestricted(player)) {
            return;
        }

        SPlayer sPlayer = SPlayer.get(player.getUniqueId());

        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logError(String.format("Failed event. SPlayer for %s is null.", player.getUniqueId()));
            }
            return;
        }

        int playerLevel = getLevel(sPlayer);
        if (playerLevel > 0) {
            event.setDamage(event.getDamage() + (playerLevel * (event.getDamage() / 100) * getUpgrade().getValue()));
        }
    }

    @Override
    public ItemBuilder<ItemStack> getDefaultItem() {
        return new BukkitItemBuilder()
                .addItemModifier(new NameModifier().setName("&cArchery Overview"))
                .addItemModifier(new XMaterialModifier(XMaterial.BOW))
                .addItemModifier(new LoreModifier().setLore(
                        "&eLeft-Click &7to upgrade this skill using &e{price} &7point(s).",
                        "&7This skill increases damage dealt using bows.",
                        "&7Level: &e{level}&7/&e{limit}&7",
                        " ",
                        "&cBow Upgrade: ",
                        "   &e{prev}% &7 >>> &e{next}%"
                ));
    }

    @Override
    public String getPreviousString(SPlayer player) {
        double archery = getLevel(player) * getUpgrade().getValue();
        return getPercentageFormat().format(archery);
    }

    @Override
    public String getNextString(SPlayer player) {
        double archery = (getLevel(player) + 1) * getUpgrade().getValue();
        return getPercentageFormat().format(archery);
    }
}
