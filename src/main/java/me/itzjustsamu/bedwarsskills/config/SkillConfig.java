package me.itzjustsamu.bedwarsskills.config;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.itzjustsamu.bedwarsskills.skill.Skill;

import java.io.File;

public class SkillConfig extends BukkitConfig {
    public SkillConfig(Skill Skills) {
        super(new File(Skills.getPlugin().getDataFolder(), "skills" + File.separator + Skills.getSkillsConfigName() + ".yml"));

    }

}

