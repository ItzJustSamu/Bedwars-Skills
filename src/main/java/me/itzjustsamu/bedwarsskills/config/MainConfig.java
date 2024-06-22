package me.itzjustsamu.bedwarsskills.config;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.PathableConfig;
import me.hsgamer.hscore.config.path.AdvancedConfigPath;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.StickyConfigPath;
import me.hsgamer.hscore.config.path.impl.BooleanConfigPath;
import me.hsgamer.hscore.config.path.impl.IntegerConfigPath;
import me.hsgamer.hscore.config.path.impl.LongConfigPath;
import me.hsgamer.hscore.config.path.impl.StringConfigPath;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.fundingsource.FundingSource;
import me.itzjustsamu.bedwarsskills.fundingsource.XPFundingSource;
import me.itzjustsamu.bedwarsskills.storage.FlatFileStorage;
import me.itzjustsamu.bedwarsskills.storage.PlayerStorage;
import me.itzjustsamu.bedwarsskills.util.modifier.XMaterialModifier;
import me.itzjustsamu.bedwarsskills.util.path.ItemBuilderConfigPath;
import me.itzjustsamu.bedwarsskills.util.path.StringListConfigPath;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;


public class MainConfig extends PathableConfig {
    // Skill-related configurations

    public static final BooleanConfigPath OPTIONS_VERBOSE = new BooleanConfigPath(new PathString("options", "verbose"), false);
    public static final StringListConfigPath OPTIONS_MENU_isWorldRestricted = new StringListConfigPath(new PathString("options", "menu-world-restriction"), Collections.emptyList());
    static BedWarsSkills plugin;

    public static final ConfigPath<PlayerStorage> OPTIONS_PLAYER_STORAGE = new StickyConfigPath<>(
            new AdvancedConfigPath<String, PlayerStorage>(new PathString("options", "player-storage"), new FlatFileStorage(plugin)) {

                @Override
                public @Nullable String getFromConfig(@NotNull me.hsgamer.hscore.config.Config config) {
                    return config.getInstance(getPath(), String.class);
                }

                @Override
                public @Nullable PlayerStorage convert(@NotNull String rawValue) {
                    return BedWarsSkills.PLAYER_STORAGE_MAP.getOrDefault(rawValue, () -> new FlatFileStorage(plugin)).get();
                }

                @Override
                public @Nullable String convertToRaw(@NotNull PlayerStorage value) {
                    return value.getName();
                }
            }
    );
    public static final LongConfigPath OPTIONS_AUTO_SAVE_TICKS = new LongConfigPath(new PathString("options", "auto-save", "ticks"), 1000L);
    public static final BooleanConfigPath OPTIONS_AUTO_SAVE_ASYNC = new BooleanConfigPath(new PathString("options", "auto-save", "async"), true);

    public static final IntegerConfigPath PURCHASE_POINT_SLOT = new IntegerConfigPath(new PathString("purchase", "point", "slot"), 4);
    public static final ConfigPath<ItemBuilder<ItemStack>> PURCHASE_POINT_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("purchase", "point", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cPurchase Points"))
                    .addItemModifier(new XMaterialModifier(XMaterial.BOOK))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7You have &e{player-points} &7points.",
                            "&eLeft-Click &7to purchase a skill point for &e{player-price} {symbol}&7."
                    ))
    ));

    public static final IntegerConfigPath RESET_SKILLS_SLOT = new IntegerConfigPath(new PathString("reset", "skills", "slot"), 5);

    public static final ConfigPath<ItemBuilder<ItemStack>> RESET_SKILLS_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("reset", "skills", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cReset"))
                    .addItemModifier(new XMaterialModifier(XMaterial.TNT))
                    .addItemModifier(new LoreModifier().setLore(
                            "&eLeft-Click &7to completely reset your skills.",
                            "&7This costs &e{reset-skills-price} &7skill point.",
                            "",
                            "&7Refund status: &e{refund-status}."
                    ))
    ));

    public static final IntegerConfigPath BACK_SLOT = new IntegerConfigPath(new PathString("back", "slot"), 45);
    public static final ConfigPath<ItemBuilder<ItemStack>> BACK_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("back", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&aBACK"))
                    .addItemModifier(new XMaterialModifier(XMaterial.ARROW))
    ));


    // Admin Configurations

    public static final IntegerConfigPath ADMIN_SLOT = new IntegerConfigPath(new PathString("admin", "slot"), 8);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSettings"))
                    .addItemModifier(new XMaterialModifier(XMaterial.NETHER_STAR))
    ));

    // Skills Configurations


    public static final StringConfigPath SKILLS_MENU_TITLE = new StringConfigPath(new PathString("skills", "menu", "title"), "&c&lSkills");
    public static final IntegerConfigPath SKILLS_MENU_SIZE = new IntegerConfigPath(new PathString("skills", "menu", "size"), 54);

    public static final BooleanConfigPath SKILLS_BACKGROUND_ENABLED = new BooleanConfigPath(new PathString("skills", "background", "enabled"), true);
    public static final ConfigPath<ItemBuilder<ItemStack>> SKILLS_BACKGROUND_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("skills", "background", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&r"))
                    .addItemModifier(new XMaterialModifier(XMaterial.GRAY_STAINED_GLASS_PANE))
    ));


    // Settings Configurations


    public static final StringConfigPath SETTINGS_MENU_TITLE = new StringConfigPath(new PathString("settings" ,"menu", "title"), "&c&lSkills Settings");
    public static final IntegerConfigPath SETTINGS_MENU_SIZE = new IntegerConfigPath(new PathString("settings" ,"menu", "size"), 54);

    public static final BooleanConfigPath SETTINGS_BACKGROUND_ENABLED = new BooleanConfigPath(new PathString("settings", "background", "enabled"), true);
    public static final ConfigPath<ItemBuilder<ItemStack>> SETTINGS_BACKGROUND_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("settings", "background", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&r"))
                    .addItemModifier(new XMaterialModifier(XMaterial.GRAY_STAINED_GLASS_PANE))
    ));

    public static final IntegerConfigPath TOGGLE_SKILL_SLOT = new IntegerConfigPath(new PathString("toggle-skill", "slot"), 9);
    public static final BooleanConfigPath TOGGLE_SKILL = new BooleanConfigPath(new PathString("toggle-skill"), false);

    public static final ConfigPath<ItemBuilder<ItemStack>> TOGGLE_SKILL_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("toggle-skill", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cToggle {skill}"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
    ));

    public static final IntegerConfigPath TOGGLE_SKILL_LEVEL_SLOT = new IntegerConfigPath(new PathString("toggle", "skill-level", "slot"), 10);

    public static final ConfigPath<ItemBuilder<ItemStack>> TOGGLE_SKILL_LEVEL_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("toggle", "skill-level", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cToggle {skill} Levels"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "",
                            "&7Level: &e{toggle-skill-level}&7&l/&e{current-skill-level}"
                    ))
    ));
    // Admin configurations


    public static final StringConfigPath ADMIN_MENU_TITLE = new StringConfigPath(new PathString("admin", "title"), "&c&lSkills Admin");
    public static final IntegerConfigPath ADMIN_MENU_SIZE = new IntegerConfigPath(new PathString("admin", "size"), 54);

    public static final BooleanConfigPath ADMIN_BACKGROUND_ENABLED = new BooleanConfigPath(new PathString("admin", "background", "enabled"), true);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_BACKGROUND_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "background", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&r"))
                    .addItemModifier(new XMaterialModifier(XMaterial.GRAY_STAINED_GLASS_PANE))
    ));


    public static final IntegerConfigPath ADMIN_SKILLS_UPGRADE_SLOT = new IntegerConfigPath(new PathString("admin", "skills-upgrade", "slot"), 9);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_SKILLS_UPGRADE_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "skills-upgrade", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet {skill} Upgrade"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Points: &e{skills-upgrade-price}",
                            "&eLeft-Click &7to decrease points.",
                            "&eRight-Click &7to increase points."
                    ))
    ));

    public static final IntegerConfigPath ADMIN_SKILLS_POINT_PRICE_SLOT = new IntegerConfigPath(new PathString("admin", "skills-point-price", "slot"), 10);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_SKILLS_POINT_PRICE_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "skills-point-price", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet {skill} Point's price"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Points: &e{skill-point-price}",
                            "&eLeft-Click &7to decrease points.",
                            "&eRight-Click &7to increase points."
                    ))
    ));

    public static final IntegerConfigPath ADMIN_SKILLS_INCREMENT_POINT_PRICE_SLOT = new IntegerConfigPath(new PathString("admin", "skills-increment", "point-price", "slot"), 11);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_SKILLS_INCREMENT_POINT_PRICE_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "skills-increment","point-price", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet {skill} point price increment"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Points: &e{skills-increment-point-price}",
                            "&eLeft-Click &7to decrease points.",
                            "&eRight-Click &7to increase points."
                    ))
    ));

    public static final IntegerConfigPath ADMIN_SKILLS_MAX_LEVEL_SLOT = new IntegerConfigPath(new PathString("admin", "skills", "max-level", "slot"), 12);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_SKILLS_MAX_LEVEL_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "skills","max-level", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet {skill} Maximum Level"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Level: &e{skills-max-level}",
                            "&eLeft-Click &7to decrease a level.",
                            "&eRight-Click &7to increase a level."
                    ))
    ));

    public static final IntegerConfigPath ADMIN_SKILLS_CONFIRMATION_TOGGLE_SLOT = new IntegerConfigPath(new PathString("admin", "skills-confirmation", "toggle", "slot"), 13);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_SKILLS_CONFIRMATION_TOGGLE_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "skills-confirmation","toggle", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet Skills Confirmation Menu"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Menu: &e{skills-confirmation} "
                    ))
    ));

    public static final IntegerConfigPath POINTS_FUNDING_SLOT = new IntegerConfigPath(new PathString("points", "funding", "slot"), 14);
    public static final ConfigPath<FundingSource> POINTS_FUNDING_SOURCE = new StickyConfigPath<>(
            new AdvancedConfigPath<String, FundingSource>(new PathString("points", "funding-source"), new XPFundingSource()) {
                @Override
                public @Nullable String getFromConfig(@NotNull me.hsgamer.hscore.config.Config config) {
                    return config.getInstance(getPath(), String.class);
                }

                @Override
                public @Nullable FundingSource convert(@NotNull String rawValue) {
                    return BedWarsSkills.FUNDING_SOURCE_MAP.getOrDefault(rawValue, XPFundingSource::new).get();
                }

                @Override
                public @Nullable String convertToRaw(@NotNull FundingSource value) {
                    return value.getName();
                }
            }
    );
    public static final ConfigPath<ItemBuilder<ItemStack>> POINTS_FUNDING_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("points", "funding", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cPoints Funding"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Funding Source: &e{symbol}."
                    ))
    ));

    public static final IntegerConfigPath POINTS_SLOT = new IntegerConfigPath(new PathString("points", "slot"), 15);
    public static final IntegerConfigPath POINTS_PRICE = new IntegerConfigPath(new PathString("points", "price"), 1);
    public static final ConfigPath<ItemBuilder<ItemStack>> POINTS_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("points", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet Points"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Points Price: &e{points-price} {symbol}",
                            "&eLeft-Click &7to decrease price.",
                            "&eRight-Click &7 to increase price."
                    ))
    ));

    public static final IntegerConfigPath POINTS_INCREMENT_SLOT = new IntegerConfigPath(new PathString("points", "increment", "slot"), 16);
    public static final IntegerConfigPath POINTS_INCREMENT_PRICE = new IntegerConfigPath(new PathString("points", "increment", "price"), 0);

    public static final ConfigPath<ItemBuilder<ItemStack>> POINTS_INCREMENT_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("points", "increment", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet Increment Points"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Incrementing Price Points: &e{points-increment-price}."
                    ))
    ));

    public static final IntegerConfigPath POINTS_CONFIRMATION_TOGGLE_SLOT = new IntegerConfigPath(new PathString("points","confirmation", "toggle", "slot"), 17);
    public static final ConfigPath<ItemBuilder<ItemStack>> POINTS_CONFIRMATION_TOGGLE_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("points", "confirmation","toggle", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet Points Confirmation Menu"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Menu: &e{points-confirmation} "
                    ))
    ));

    public static final BooleanConfigPath CONFIRMATION_PURCHASE_SKILLS = new BooleanConfigPath(new PathString("confirmation", "purchase-skills"), false);
    public static final BooleanConfigPath CONFIRMATION_PURCHASE_POINTS = new BooleanConfigPath(new PathString("confirmation", "purchase-points"), false);
    public static final BooleanConfigPath CONFIRMATION_RESET_SKILLS = new BooleanConfigPath(new PathString("confirmation", "reset-skills"), false);

    public static final IntegerConfigPath RESET_SLOT = new IntegerConfigPath(new PathString("reset", "slot"), 18);

    public static final IntegerConfigPath RESET_PRICE = new IntegerConfigPath(new PathString("reset", "price"), 1);
    public static final ConfigPath<ItemBuilder<ItemStack>> RESET_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("reset", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet Reset Points"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Reset Cost: &e{reset-price}",
                            "&eLeft-Click &7to decrease price.",
                            "&eRight-Click &7 to increase price."
                    ))
    ));
    public static final IntegerConfigPath RESET_INCREMENT_SLOT = new IntegerConfigPath(new PathString("reset", "increment" , "slot"), 19);

    public static final IntegerConfigPath RESET_INCREMENT_PRICE = new IntegerConfigPath(new PathString("reset", "increment" , "price"), 1);
    public static final ConfigPath<ItemBuilder<ItemStack>> RESET_INCREMENT_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("reset", "increment" , "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet Increment Reset Points"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Incremented Reset Cost: &e{reset-increment-price}",
                            "&eLeft-Click &7to decrease price.",
                            "&eRight-Click &7 to increase price."
                    ))
    ));

    public static final ConfigPath<ItemBuilder<ItemStack>> RESET_REFUND_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("reset", "refund", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cRefund Skill Points"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Refund Reset Status &e{refund-status}."
                    ))
    ));
    public static final IntegerConfigPath RESET_REFUND_SLOT = new IntegerConfigPath(new PathString("reset", "refund", "slot"), 20);
    public static final BooleanConfigPath REFUND_POINTS = new BooleanConfigPath(new PathString("reset", "refund-points"), true);

    public static final IntegerConfigPath RESET_CONFIRMATION_TOGGLE_SLOT = new IntegerConfigPath(new PathString("reset","confirmation", "toggle", "slot"), 21);
    public static final ConfigPath<ItemBuilder<ItemStack>> RESET_CONFIRMATION_TOGGLE_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("reset", "confirmation","toggle", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&cSet Reset Confirmation Menu"))
                    .addItemModifier(new XMaterialModifier(XMaterial.PAPER))
                    .addItemModifier(new LoreModifier().setLore(
                            "&7Menu: &e{reset-confirmation} "
                    ))
    ));
    public static final IntegerConfigPath ADMIN_BACK_SLOT = new IntegerConfigPath(new PathString("admin", "back", "slot"), 45);
    public static final ConfigPath<ItemBuilder<ItemStack>> ADMIN_BACK_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("admin", "back", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&aBACK"))
                    .addItemModifier(new XMaterialModifier(XMaterial.ARROW))
    ));


    // Background configurations

    public static final StringConfigPath PLACEHOLDERS_SKILL_PRICE_MAX = new StringConfigPath(new PathString("placeholders", "skill-price-max"), "--");
    public static final StringConfigPath PLACEHOLDERS_NEXT_MAX = new StringConfigPath(new PathString("placeholders", "next-max"), "--");


    // Confirmation Configurations


    public static final StringConfigPath CONFIRMATION_MENU_TITLE = new StringConfigPath(new PathString("confirmation", "menu", "title"), "&a&lConfirmation");

    public static final IntegerConfigPath CONFIRMATION_MENU_SIZE = new IntegerConfigPath(new PathString("confirmation", "menu", "size"), 27);
    public static final BooleanConfigPath CONFIRMATION_MENU_BACKGROUND = new BooleanConfigPath(new PathString("confirmation", "menu", "background"), true);
    public static final ConfigPath<ItemBuilder<ItemStack>> CONFIRMATION_MENU_BACKGROUND_DISPLAY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("confirmation", "menu-background", "display"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&r"))
                    .addItemModifier(new XMaterialModifier(XMaterial.GRAY_STAINED_GLASS_PANE))
    ));

    public static final ConfigPath<ItemBuilder<ItemStack>> CONFIRMATION_ACCEPT = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("confirmation", "accept"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&a&lConfirm"))
                    .addItemModifier(new XMaterialModifier(XMaterial.LIME_STAINED_GLASS_PANE))
                    .addItemModifier(new LoreModifier().setLore("&7Confirm action."))
    ));
    public static final ConfigPath<ItemBuilder<ItemStack>> CONFIRMATION_DENY = new StickyConfigPath<>(new ItemBuilderConfigPath(new PathString("confirmation", "deny"),
            new BukkitItemBuilder()
                    .addItemModifier(new NameModifier().setName("&c&lDecline"))
                    .addItemModifier(new XMaterialModifier(XMaterial.RED_STAINED_GLASS_PANE))
                    .addItemModifier(new LoreModifier().setLore("&7Decline and return to the previous menu."))
    ));

    public MainConfig(Plugin plugin) {
        super(new BukkitConfig(plugin, "config.yml"));
    }

    public static boolean isVerboseLogging() {
        return OPTIONS_VERBOSE.getValue();
    }
}