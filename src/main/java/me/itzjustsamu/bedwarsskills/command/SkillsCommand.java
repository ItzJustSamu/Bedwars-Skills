package me.itzjustsamu.bedwarsskills.command;

import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.itzjustsamu.bedwarsskills.Permissions;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.config.MessageConfig;
import me.itzjustsamu.bedwarsskills.menu.SkillsMenu;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SkillsCommand extends Command {

    private final BedWarsSkills plugin;
    public SkillsCommand(BedWarsSkills plugin) {
        super("skills", "Open skills menu", "/skills", Arrays.asList("s", "skills", "skill"));
        setPermission(Permissions.COMMAND.getName());
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Please use /skillsadmin instead.");
            return false;
        }
        Player player = (Player) sender;
        SPlayer sPlayer = SPlayer.get(player.getUniqueId());

        List<String> listOfWorlds = MainConfig.OPTIONS_MENU_isWorldRestricted.getValue();
        if (!listOfWorlds.isEmpty() && !listOfWorlds.contains(player.getWorld().getName())) {
            MessageUtils.sendMessage(player, MessageConfig.MENU_isWorldRestricted.getValue());
            return true;
        }

        new SkillsMenu(plugin, player, sPlayer).open(player);
        return true;
    }
}