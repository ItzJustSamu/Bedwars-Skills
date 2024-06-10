package me.itzjustsamu.bedwarsskills;

import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import me.hsgamer.hscore.bukkit.scheduler.Scheduler;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;
import me.itzjustsamu.bedwarsskills.command.SkillsAdminCommand;
import me.itzjustsamu.bedwarsskills.command.SkillsCommand;
import me.itzjustsamu.bedwarsskills.config.MainConfig;
import me.itzjustsamu.bedwarsskills.config.MessageConfig;
import me.itzjustsamu.bedwarsskills.fundingsource.FundingSource;
import me.itzjustsamu.bedwarsskills.fundingsource.VaultFundingSource;
import me.itzjustsamu.bedwarsskills.fundingsource.XPFundingSource;
import me.itzjustsamu.bedwarsskills.listener.PlayerListener;
import me.itzjustsamu.bedwarsskills.menu.MenuController;
import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.skill.*;
import me.itzjustsamu.bedwarsskills.storage.FlatFileStorage;
import me.itzjustsamu.bedwarsskills.storage.PlayerStorage;
import me.itzjustsamu.bedwarsskills.util.CommonStringReplacer;
import me.itzjustsamu.bedwarsskills.util.Updater;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class BedWarsSkills extends BasePlugin {
    public static final Map<String, Supplier<FundingSource>> FUNDING_SOURCE_MAP = new CaseInsensitiveStringHashMap<>();
    public static final Map<String, Supplier<PlayerStorage>> PLAYER_STORAGE_MAP = new CaseInsensitiveStringHashMap<>();

    private final MessageConfig messageConfig = new MessageConfig(this);
    private final MainConfig mainConfig = new MainConfig(this);
    private final Map<String, Skill> skills = new ConcurrentHashMap<>();
    private final Logger logger = getLogger();
    private final FlatFileStorage storage = new FlatFileStorage(this);

    @Override
    public void preLoad() {
        FUNDING_SOURCE_MAP.put("XP", XPFundingSource::new);
        FUNDING_SOURCE_MAP.put("VAULT", VaultFundingSource::new);
        PLAYER_STORAGE_MAP.put("FLAT_FILE", () -> new FlatFileStorage(this));
    }

    @Override
    public void load() {
        MessageUtils.setPrefix(MessageConfig.PREFIX::getValue);
        messageConfig.setup();
        mainConfig.setup();
    }

    @Override
    public void enable() {
        registerSkill(new AcrobatSkill(this));
        registerSkill(new ArcherySkill(this));
        registerSkill(new CriticalsSkill(this));
        registerSkill(new DodgeSkill(this));
        registerSkill(new DoubleJumpSkill(this));
        registerSkill(new WoolReturnSkill(this));
        registerSkill(new HealthSkill(this));
        registerSkill(new KnockBackSkill(this));
        registerSkill(new LacerateSkill(this));
        registerSkill(new ResistanceSkill(this));
        registerSkill(new SpeedSkill(this));
        registerSkill(new StrengthSkill(this));
        registerSkill(new WoodReturnSkill(this));
        registerSkill(new WoolReturnSkill(this));
        registerSkill(new XPSkill(this));

        registerCommand(new SkillsCommand(this));
        registerCommand(new SkillsAdminCommand(this));
        CommonStringReplacer.setStorage(this.storage);

        registerListener(new MenuController());
        registerListener(new PlayerListener());

        Updater updater = new Updater(this, 113626);
        updater.checkForUpdates();
    }

    @Override
    public void postEnable() {
        startAutoSaveTask();
    }

    @Override
    protected List<Class<?>> getPermissionClasses() {
        return Collections.singletonList(Permissions.class);
    }

    private void startAutoSaveTask() {
        long ticks = MainConfig.OPTIONS_AUTO_SAVE_TICKS.getValue();
        if (ticks >= 0) {
            boolean async = MainConfig.OPTIONS_AUTO_SAVE_ASYNC.getValue();
            Runnable runnable = () -> {
                List<SPlayer> list = new ArrayList<>(SPlayer.getPlayers().values());
                for (SPlayer player : list) {
                    SPlayer.save(player);
                }
            };
            Scheduler.current().runner(async).runTaskTimer(runnable, ticks, ticks);
        }
    }

    @Override
    public void disable() {
        for (SPlayer player : SPlayer.getPlayers().values()) {
            SPlayer.save(player);
        }

        for (Skill skill : skills.values()) {
            skill.disable();
        }

        HandlerList.unregisterAll(this);

        skills.clear();
    }

    public Map<String, Skill> getSkills() {
        return skills;
    }

    public void registerSkill(Skill skill) {
        if (skill.isSkillDisabled()) {
            logger.info("Skipping registration of disabled skill: " + skill.getSkillsConfigName());
            return;
        }

        if (skills.containsKey(skill.getSkillsConfigName())) {
            logger.warning("Attempted to register duplicate skill: " + skill.getSkillsConfigName());
            return;
        }

        skills.put(skill.getSkillsConfigName(), skill);
        skill.setup();
        skill.enable();
        logger.info("Registered skill: " + skill.getSkillsConfigName());
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
