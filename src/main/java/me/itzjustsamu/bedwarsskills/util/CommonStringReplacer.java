package me.itzjustsamu.bedwarsskills.util;

import me.hsgamer.hscore.bukkit.utils.ColorUtils;
import me.hsgamer.hscore.common.StringReplacer;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.skill.Skill;
import me.itzjustsamu.bedwarsskills.storage.FlatFileStorage;

public final class CommonStringReplacer {
    public static final StringReplacer COLORIZE = ColorUtils::colorize;
    private static Skill skill;
    private static FlatFileStorage storage; // Make sure to initialize this somewhere
    public static void setStorage(FlatFileStorage storage) {
        CommonStringReplacer.storage = storage;
    }
    public static final StringReplacer PLAYER_PROPERTIES = StringReplacer.of((original, uuid) -> {
        SPlayer sPlayer = SPlayer.get(uuid);
        int skillsUpgrade = (skill != null) ? skill.getUpgrade().getValue() : 0;
        int skillPrice = (skill != null) ? skill.getPrice().getValue() : 0;
        int price = sPlayer.getPointPrice();
        int skillUpgradeIncrement = (skill != null) ? skill.getIncrementedUpgrade().getValue() : 0;
        int skillMaxLevel = (skill != null) ? skill.getLimit() : 0;

        int resetSkillsPrice;
        if (MainConfig.RESET_INCREMENT_PRICE.getValue()  > 0) {
            resetSkillsPrice = MainConfig.RESET_INCREMENT_PRICE.getValue() * (sPlayer.getResetCount());
        } else {
            resetSkillsPrice = MainConfig.RESET_PRICE.getValue() + sPlayer.getResetCount() * (MainConfig.RESET_INCREMENT_PRICE.getValue());
        }
        Integer toggleLevel = skill != null ? storage.loadSkillLevel(sPlayer.getPlayer(), skill.getSkillsConfigName()) : null;
        int currentLevel = skill != null ? skill.getLevel(sPlayer) : 0;

        return original.replace("{player-price}", Integer.toString(price))
                .replace("{symbol}", MainConfig.POINTS_FUNDING_SOURCE.getValue().getSymbol(price))
                .replace("{player-points}", Integer.toString(sPlayer.getPoints()))
                .replace("{reset-price}", Integer.toString(MainConfig.RESET_PRICE.getValue()))
                .replace("{reset-increment-price}", Integer.toString(MainConfig.RESET_INCREMENT_PRICE.getValue()))
                .replace("{reset-skills-price}", Integer.toString(resetSkillsPrice))
                .replace("{refund-status}", Boolean.toString(MainConfig.REFUND_POINTS.getValue()))
                .replace("{points-price}", Integer.toString(MainConfig.POINTS_PRICE.getValue()))
                .replace("{skills-upgrade-price}", Integer.toString(skillsUpgrade))
                .replace("{skills-increment-point-price}", Integer.toString(skillUpgradeIncrement))
                .replace("{points-increment-price}", Integer.toString(MainConfig.POINTS_INCREMENT_PRICE.getValue()))
                .replace("{skill}", (skill != null) ? skill.getSkillsConfigName() : "")
                .replace("{skill-point-price}", Integer.toString(skillPrice))
                .replace("{skills-confirmation}", Boolean.toString(MainConfig.CONFIRMATION_PURCHASE_SKILLS.getValue()))
                .replace("{points-confirmation}", Boolean.toString(MainConfig.CONFIRMATION_PURCHASE_POINTS.getValue()))
                .replace("{reset-confirmation}", Boolean.toString(MainConfig.CONFIRMATION_RESET_SKILLS.getValue()))
                .replace("{skills-max-level}", Integer.toString(skillMaxLevel))
                .replace("{toggle-skill-level}", toggleLevel != null ? toggleLevel.toString() : "0")
                .replace("{current-skill-level}", Integer.toString(currentLevel));


    });

    private CommonStringReplacer() {
    }

    public static void setSkill(Skill skill) {
        CommonStringReplacer.skill = skill;
    }

    public static void resetSkill() {
        skill = null;
    }
}
