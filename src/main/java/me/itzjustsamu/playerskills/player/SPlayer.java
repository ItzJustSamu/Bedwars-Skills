package me.itzjustsamu.playerskills.player;

import me.itzjustsamu.playerskills.PlayerSkills;
import me.itzjustsamu.playerskills.config.MainConfig;
import me.itzjustsamu.playerskills.skill.SpeedSkill;
import me.itzjustsamu.playerskills.util.Utils;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SPlayer {
    private static final HashMap<UUID, SPlayer> players = new HashMap<>();

    private final UUID player;
    private final Map<String, Integer> skills;
    private int points;
    private int resetCount;

    public SPlayer(UUID player) {
        this.player = player;
        this.skills = new HashMap<>();
        this.resetCount = 0;
    }

    public static void load(UUID uuid) {
        SPlayer sPlayer = MainConfig.OPTIONS_PLAYER_STORAGE.getValue().load(uuid);
        players.put(uuid, sPlayer);
    }

    public static SPlayer get(UUID uuid) {
        return players.get(uuid);
    }

    public static void unload(UUID uuid) {
        players.remove(uuid);
    }

    public static void save(SPlayer sPlayer) {
        if (sPlayer == null) {
            if (MainConfig.isVerboseLogging()) {
                Utils.logInfo("Plugin tried to save an SPlayer for a null player.");
            }
            return;
        }
        MainConfig.OPTIONS_PLAYER_STORAGE.getValue().save(sPlayer);
    }

    public static HashMap<UUID, SPlayer> getPlayers() {
        return players;
    }

    public UUID getPlayer() {
        return player;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }

    public int Level(String skill) {
        return skills.getOrDefault(skill, 0);
    }

    public void setLevel(String skill, int level) {
        skills.put(skill, level);
    }

    public void onJoin(PlayerLoginEvent event) {
        load(event.getPlayer().getUniqueId());
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPointPrice() {
        int base = MainConfig.POINTS_PRICE.getValue();
        int playerPoints = getPoints();
        for (int i : getSkills().values()) {
            playerPoints += i;
        }
        return base + (playerPoints * MainConfig.POINTS_INCREMENT_PRICE.getValue());
    }

    public int getResetCount() {
        return resetCount;
    }

    public void incrementResetCount() {
        resetCount++;
    }
}
