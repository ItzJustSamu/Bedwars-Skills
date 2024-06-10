package me.itzjustsamu.bedwarsskills.fundingsource;

import me.itzjustsamu.bedwarsskills.player.SPlayer;
import me.itzjustsamu.bedwarsskills.util.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultFundingSource implements FundingSource {
    private final Economy economy;
    private static final RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

    public VaultFundingSource() {
        if (rsp == null) {
            Utils.logError("Failed to initialize VaultFundingSource: Economy service provider is null.");
            throw new IllegalStateException("Economy service provider is null");
        }
        economy = rsp.getProvider();
    }

    @Override
    public String getSymbol(int price) {
        return price <= 1 ? economy.currencyNameSingular() : economy.currencyNamePlural();
    }

    @Override
    public boolean doTransaction(SPlayer sPlayer, int price, Player player) {
        double balance = economy.getBalance(player);
        if (balance >= price) {
            economy.withdrawPlayer(player, price);
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "VAULT";
    }
}
