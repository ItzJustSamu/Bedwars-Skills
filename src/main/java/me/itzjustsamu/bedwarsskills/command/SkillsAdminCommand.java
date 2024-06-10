package me.itzjustsamu.bedwarsskills.command;

import com.google.common.collect.ImmutableList;
import me.itzjustsamu.bedwarsskills.Permissions;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.skill.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SkillsAdminCommand extends Command implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("setskill", "givepoints", "view", "fullreset", "settings");
    private final BedWarsSkills plugin;

    public SkillsAdminCommand(BedWarsSkills plugin) {
        super("skillsadmin", "Admin control for PlayerSkills", "/skillsadmin", SUBCOMMANDS);
        setPermission(Permissions.ADMIN.getName());
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }

        if (args.length >= 4) {
            if (args[0].equalsIgnoreCase("setskill")) {
                setSkill(sender, args);
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("givepoints")) {
                givePoints(sender, args);
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("view")) {
                viewSkills(sender, args);
                return true;
            } else if (args[0].equalsIgnoreCase("fullreset")) {
                fullReset(sender, args);
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "/skillsadmin view <player>");
        sender.sendMessage(ChatColor.RED + "/skillsadmin givepoints <player> [-]<points>");
        sender.sendMessage(ChatColor.RED + "/skillsadmin setskill <player> <skill name> <level>");
        sender.sendMessage(ChatColor.RED + "/skillsadmin fullreset <player>");
        return true;
    }

    private Optional<SPlayer> getPlayer(CommandSender sender, String arg) {
        Player player = Bukkit.getPlayer(arg);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return Optional.empty();
        } else {
            SPlayer sPlayer = SPlayer.get(player.getUniqueId());
            if (sPlayer == null) {
                sender.sendMessage(ChatColor.RED + "ERROR: SPlayer could not be found.");
                return Optional.empty();
            }
            return Optional.of(sPlayer);
        }
    }

    private void setSkill(CommandSender sender, String[] args) {
        Optional<SPlayer> optionalSPlayer = getPlayer(sender, args[1]);
        if (!optionalSPlayer.isPresent()) return;
        SPlayer sPlayer = optionalSPlayer.get();
        String skillName = args[2];

        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Not a number.");
            return;
        }

        Skill Skills = plugin.getSkills().get(skillName);

        if (Skills == null) {
            sender.sendMessage(ChatColor.RED + "Skill could not be found. Skill names used in configurations and commands should contain no spaces and" +
                    " should be all lower case.");
            return;
        }

        sPlayer.setLevel(Skills.getSkillsConfigName(), level);
        sender.sendMessage(ChatColor.GREEN + "Skill level for " + Skills.getSkillsName() + " updated to " + Skills.getLevel(sPlayer) + ".");
    }

    private void givePoints(CommandSender sender, String[] args) {
        Optional<SPlayer> optionalSPlayer = getPlayer(sender, args[1]);
        if (!optionalSPlayer.isPresent()) return;
        SPlayer sPlayer = optionalSPlayer.get();

        int points;
        try {
            points = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Not a number.");
            return;
        }

        sPlayer.setPoints(sPlayer.getPoints() + points);

        sender.sendMessage(ChatColor.GREEN + "Points updated to " + sPlayer.getPoints() + ".");
    }

    private void viewSkills(CommandSender sender, String[] args) {
        Optional<SPlayer> optionalSPlayer = getPlayer(sender, args[1]);
        if (!optionalSPlayer.isPresent()) return;
        SPlayer sPlayer = optionalSPlayer.get();

        StringBuilder message = new StringBuilder();
        message.append(ChatColor.RED).append(ChatColor.BOLD).append("Skills").append("\n");
        for (Skill Skills : plugin.getSkills().values()) {
            message.append(ChatColor.RED).append(Skills.getSkillsName()).append(": ").append(ChatColor.GRAY).append(Skills.getLevel(sPlayer)).append("\n");
        }
        message.append(ChatColor.RED).append(ChatColor.BOLD).append("Points: ").append(ChatColor.GRAY).append(sPlayer.getPoints());

        sender.sendMessage(message.toString());
    }

    private void fullReset(CommandSender sender, String[] args) {
        Optional<SPlayer> optionalSPlayer = getPlayer(sender, args[1]);
        if (!optionalSPlayer.isPresent()) return;
        SPlayer sPlayer = optionalSPlayer.get();

        sPlayer.setPoints(0);
        Set<String> skills = sPlayer.getSkills().keySet();
        for (String skill : skills) {
            sPlayer.setLevel(skill, 0);
        }
        sender.sendMessage(ChatColor.GREEN + "Skill data for (" + ChatColor.GRAY + sPlayer.getPlayer() + ChatColor.GREEN + ") " +
                "has been reset.");
    }


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            // Tab complete subcommands
            String partialSubCommand = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String subCommand : SUBCOMMANDS) {
                if (subCommand.startsWith(partialSubCommand) && sender.hasPermission("playerskills.admin." + subCommand)) {
                    completions.add(subCommand);
                }
            }
            return completions;
        } else if (args.length == 2) {
            // Tab complete player names for specific subcommands
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("setskill") || subCommand.equals("givepoints") || subCommand.equals("view") || subCommand.equals("fullreset")) {
                String partialPlayerName = args[1].toLowerCase();
                List<String> playerNames = new ArrayList<>();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(partialPlayerName)) {
                        playerNames.add(onlinePlayer.getName());
                    }
                }
                return playerNames;
            }
        }

        return ImmutableList.of(); // Return an empty list if no matches
    }
}
