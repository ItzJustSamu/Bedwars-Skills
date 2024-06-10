package me.itzjustsamu.bedwarsskills.skill;

import me.hsgamer.hscore.common.StringReplacer;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.StickyConfigPath;
import me.hsgamer.hscore.config.path.impl.BooleanConfigPath;
import me.hsgamer.hscore.config.path.impl.IntegerConfigPath;
import me.hsgamer.hscore.config.path.impl.Paths;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.config.MessageConfig;
import me.itzjustsamu.bedwarsskills.config.SkillConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.path.ItemBuilderConfigPath;
import me.itzjustsamu.bedwarsskills.util.path.StringListConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public abstract class Skill implements Listener {

    private final SkillConfig CONFIG;
    private final BedWarsSkills PLUGIN;

    private final String NAME;
    private final String SKILL;
    private final ConfigPath<List<String>> isWorldRestrictedS = new StickyConfigPath<>(new StringListConfigPath(new PathString("only-in-worlds"), Collections.emptyList()));

    private final int LIMIT;
    private final int GUI_SLOT;
    private int INCREMENT;
    private int PRICE;
    private int INCREMENTED_UPGRADE;
    private int INCREMENT_LIMIT;
    private ItemBuilderConfigPath ITEM_CONFIG;
    private IntegerConfigPath GET_INCREMENT;
    private IntegerConfigPath GET_INCREMENTED_UPGRADE;
    private IntegerConfigPath GET_INCREMENT_LIMIT;

    private IntegerConfigPath GET_PRICE;
    private final BooleanConfigPath GET_DISABLED = new BooleanConfigPath(new PathString("disable"), false);
    private ItemBuilder<ItemStack> DISPLAY_ITEM;
    private IntegerConfigPath GET_LIMIT;
    private IntegerConfigPath GET_GUI_SLOT;

    public Skill(BedWarsSkills BedwarsSkills, String SKILL_CONFIG_NAME, String SKILL, int SET_LIMIT, int SET_GUI_SLOT) {
        this.PLUGIN = BedwarsSkills;
        this.NAME = SKILL_CONFIG_NAME;
        this.SKILL = SKILL;
        this.LIMIT = SET_LIMIT;
        this.GUI_SLOT = SET_GUI_SLOT;
        this.CONFIG = new SkillConfig(this);
    }

    public final void setup() {
        CONFIG.setup();
        GET_LIMIT = Paths.integerPath(new PathString("limit"), LIMIT);
        GET_LIMIT.setConfig(CONFIG);

        GET_GUI_SLOT = Paths.integerPath(new PathString("gui-slot"), GUI_SLOT);
        GET_INCREMENT = Paths.integerPath(new PathString("increment"), INCREMENT);
        GET_INCREMENT.setConfig(CONFIG);
        GET_INCREMENTED_UPGRADE = Paths.integerPath(new PathString("incremented-upgrade"), INCREMENTED_UPGRADE);
        GET_INCREMENTED_UPGRADE.setConfig(CONFIG);
        GET_PRICE = Paths.integerPath(new PathString("price"), PRICE);
        GET_PRICE.setConfig(CONFIG);
        GET_DISABLED.setConfig(CONFIG);
        GET_GUI_SLOT.setConfig(CONFIG);
        isWorldRestrictedS.setConfig(CONFIG);
        getAdditionalConfigPaths().forEach(configPath -> configPath.setConfig(CONFIG));
        ItemBuilderConfigPath itemBuilderConfigPath = new ItemBuilderConfigPath(new PathString("display"), getDefaultItem());
        itemBuilderConfigPath.setConfig(CONFIG);
        CONFIG.save();

        DISPLAY_ITEM = itemBuilderConfigPath.getValue();
        DISPLAY_ITEM.addStringReplacer(StringReplacer.of((original, uuid) -> {
            SPlayer sPlayer = SPlayer.get(uuid);

            if (getLevel(sPlayer) >= getLimit()) {
                original = original.replace("{next}", MainConfig.PLACEHOLDERS_NEXT_MAX.getValue())
                        .replace("{price}", MainConfig.PLACEHOLDERS_SKILL_PRICE_MAX.getValue());
            } else {
                original = original.replace("{next}",  getNextString(sPlayer))
                        .replace("{price}", Integer.toString(getPrice().getValue()));
            }
            original = original
                    .replace("{prev}", getPreviousString(sPlayer))
                    .replace("{level}", Integer.toString(getLevel(sPlayer)))
                    .replace("{limit}", Integer.toString(getLimit())
                    .replace("{upgrade}", Integer.toString(getUpgrade().getValue())
            .replace("{incremented-upgrade}", Integer.toString(getIncrementedUpgrade().getValue()))));
            return original;
        }));

        List<ConfigPath<?>> messageConfigPaths = getMessageConfigPaths();
        if (!messageConfigPaths.isEmpty()) {
            MessageConfig messageConfig = PLUGIN.getMessageConfig();
            messageConfigPaths.forEach(configPath -> configPath.setConfig(messageConfig));
            messageConfig.save();
        }
        Bukkit.getPluginManager().registerEvents(this, PLUGIN);
    }

    public int getLimit() {
        return GET_LIMIT.getValue();
    }
    public void setLimit(int Level) {
        GET_LIMIT.setValue(Level, getConfig());
    }

    public int getGuiSlot() {
        return GET_GUI_SLOT.getValue();
    }

    public List<ConfigPath<?>> getAdditionalConfigPaths() {
        return Collections.emptyList();
    }

    public List<ConfigPath<?>> getMessageConfigPaths() {
        return Collections.emptyList();
    }

    public ItemBuilder<ItemStack> getDefaultItem() {
        if (ITEM_CONFIG == null) {
            ITEM_CONFIG = new ItemBuilderConfigPath(new PathString("display"), null);
            ITEM_CONFIG.setConfig(CONFIG);
        }
        return ITEM_CONFIG.getValue();
    }

    public final String getSkillsConfigName() {
        return NAME;
    }

    public final String getSkillsName() {
        return SKILL;
    }

    public final BedWarsSkills getPlugin() {
        return PLUGIN;
    }

    public final SkillConfig getConfig() {
        return CONFIG;
    }
    public ItemStack getDisplayItem(Player player) {
        return DISPLAY_ITEM.build(player.getUniqueId());
    }

    public abstract String getPreviousString(SPlayer player);

    public abstract String getNextString(SPlayer player);

    public void enable() {
        // EMPTY
    }

    public void disable() {
        // EMPTY
    }

    public int getLevel(SPlayer player) {
        return player.Level(getSkillsConfigName());
    }

    public IntegerConfigPath getPrice() {
        return GET_PRICE;
    }

    public IntegerConfigPath getUpgrade() {
        return GET_INCREMENT;
    }

    public IntegerConfigPath getIncrementedUpgrade() {
        return GET_INCREMENTED_UPGRADE;
    }

    public void setIncrement(int increment) {
        GET_INCREMENT.setValue(increment, getConfig());
    }

    public void setPrice(int price) {
        GET_PRICE.setValue(price, getConfig());
    }

    public boolean isWorldRestricted(Player player) {
        List<String> list = isWorldRestrictedS.getValue();
        if (list.isEmpty()) {
            return false;
        }
        World world = player.getLocation().getWorld();
        if (world == null) {
            return true;
        }
        return !list.contains(world.getName());
    }

    public boolean isWorldRestricted(World world) {
        List<String> list = isWorldRestrictedS.getValue();
        if (list.isEmpty()) {
            return false;
        }
        if (world == null) {
            return true;
        }
        return !list.contains(world.getName());
    }


    public boolean isSkillDisabled() {
        return GET_DISABLED.getValue();
    }
}
