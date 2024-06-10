package me.itzjustsamu.playerskills.storage;

import me.itzjustsamu.playerskills.player.SPlayer;

import java.util.UUID;

public interface PlayerStorage {
    SPlayer load(UUID uuid);

    void save(SPlayer player);

    String getName();
}
