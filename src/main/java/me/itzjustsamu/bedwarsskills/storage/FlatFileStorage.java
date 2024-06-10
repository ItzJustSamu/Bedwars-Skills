package me.itzjustsamu.bedwarsskills.storage;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.config.Config;
import me.hsgamer.hscore.config.PathString;
import me.itzjustsamu.bedwarsskills.BedWarsSkills;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;

import java.io.File;
import java.util.UUID;

public class FlatFileStorage implements PlayerStorage {
    private static final PathString SKILLS_PATH = new PathString("skills");
    private static final PathString POINTS_PATH = new PathString("points");
    private static final PathString RESTORED_SKILL_PATH = new PathString("restored-skills");
    private static final PathString LEVEL_PATH = new PathString("levels");
    private final BedWarsSkills plugin;

    public FlatFileStorage(BedWarsSkills plugin) {
        this.plugin = plugin;
    }

    private Config setupFile(UUID uuid) {
        File dataFolder = plugin.getDataFolder();
        File userFile = new File(dataFolder, "users" + File.separator + uuid.toString() + ".yml");
        BukkitConfig config = new BukkitConfig(userFile);
        config.setup();
        return config;
    }

    @Override
    public SPlayer load(UUID uuid) {
        Config config = setupFile(uuid);
        SPlayer sPlayer = new SPlayer(uuid);
        config.getNormalizedValues(SKILLS_PATH, false).forEach((k, v) -> {
            try {
                sPlayer.getSkills().put(PathString.toPath(k), Integer.parseInt(String.valueOf(v)));
            } catch (Exception exception) {
                if (MainConfig.OPTIONS_VERBOSE.getValue()) {
                    Utils.logError("Error while loading player " + uuid, exception);
                }
            }
        });
        int points = config.getInstance(POINTS_PATH, 0, Number.class).intValue();
        sPlayer.setPoints(points);
        return sPlayer;
    }

    @Override
    public void save(SPlayer player) {
        Config config = setupFile(player.getPlayer());
        config.remove(SKILLS_PATH);
        config.set(SKILLS_PATH, player.getSkills());
        config.set(POINTS_PATH, player.getPoints());
        config.save();
    }

    @Override
    public String getName() {
        return "FLAT_FILE";
    }

    public Integer loadPreviousSkillLevel(UUID uuid, String skillName) {
        Config config = setupFile(uuid);
        return config.getInstance(RESTORED_SKILL_PATH.append(skillName), Integer.class);
    }

    public void savePreviousSkillLevel(UUID uuid, String skillName, int level) {
        Config config = setupFile(uuid);
        config.set(RESTORED_SKILL_PATH.append(skillName), level);
        config.save();
    }

    public Integer loadSkillLevel(UUID uuid, String skillName) {
        Config config = setupFile(uuid);
        return config.getInstance(LEVEL_PATH.append(skillName), Integer.class);
    }

    public void saveSkillLevel(UUID uuid, String skillName, int level) {
        Config config = setupFile(uuid);
        config.set(LEVEL_PATH.append(skillName), level);
        config.save();
    }
}
