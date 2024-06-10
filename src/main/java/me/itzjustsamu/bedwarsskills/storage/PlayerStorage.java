package me.itzjustsamu.bedwarsskills.storage;

import me.itzjustsamu.bedwarsskills.player.SPlayer;

import java.util.UUID;

public interface PlayerStorage {
    SPlayer load(UUID uuid);

    void save(SPlayer player);

    String getName();
}
