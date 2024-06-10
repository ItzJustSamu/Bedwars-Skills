package me.itzjustsamu.playerskills.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Utils {
    public static final Logger logger = JavaPlugin.getProvidingPlugin(Utils.class).getLogger();
    private static final DecimalFormat percentageFormat = new DecimalFormat("#.#");

    static {
        percentageFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    private Utils() {
        // EMPTY
    }

    public static DecimalFormat getPercentageFormat() {
        return percentageFormat;
    }

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message) {
        logger.severe(message);
    }

    public static void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}
