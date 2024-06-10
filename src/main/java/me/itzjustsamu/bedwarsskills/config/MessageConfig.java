package me.itzjustsamu.bedwarsskills.config;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.PathableConfig;
import me.hsgamer.hscore.config.path.impl.StringConfigPath;
import org.bukkit.plugin.Plugin;

public class MessageConfig extends PathableConfig {
    public static final StringConfigPath PREFIX = new StringConfigPath(new PathString("prefix"), "&7[&cPlayerSkills&7]&r ");
    public static final StringConfigPath MENU_isWorldRestricteds = new StringConfigPath(new PathString("menu-world-restriction"), "&cYou cannot modify your skills in your current location.");
    public static final StringConfigPath XP_SYMBOL = new StringConfigPath(new PathString("xp-symbol"), "XP");

    public MessageConfig(Plugin plugin) {
        super(new BukkitConfig(plugin, "messages.yml"));
    }
}
