package me.itzjustsamu.bedwarsskills.fundingsource;

import me.itzjustsamu.bedwarsskills.player.SPlayer;
import org.bukkit.entity.Player;

public interface FundingSource {

    String getSymbol(int price);

    boolean doTransaction(SPlayer sPlayer, int price, Player player);

    String getName();
}
