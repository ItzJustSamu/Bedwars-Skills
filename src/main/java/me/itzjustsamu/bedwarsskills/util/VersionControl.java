package me.itzjustsamu.bedwarsskills.util;

import org.bukkit.Bukkit;

public class VersionControl {

    private static final boolean NEW_VERSION;

    static {
        boolean newVersion;
        try {
            Class.forName("org.bukkit.attribute.Attribute");
            newVersion = true;
        } catch (ClassNotFoundException e) {
            newVersion = false;
        }
        NEW_VERSION = newVersion;
    }

    public static boolean isNewVersion() {
        return NEW_VERSION;
    }

    public static boolean isOldVersion() {
        String[] packageNameParts = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        return packageNameParts.length >= 4 && (packageNameParts[3].equals("v1_8_R3") || packageNameParts[3].startsWith("v1_8"));
    }

}
