package me.itzjustsamu.bedwarsskills;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class Permissions {
    public static final Permission COMMAND = new Permission("playerskills.command", "Skills permission", PermissionDefault.TRUE);
    public static final Permission ADMIN = new Permission("playerskills.admin", "Skills admin permission", PermissionDefault.OP);


    private Permissions() {
        // EMPTY
    }
}
