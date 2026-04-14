package me.pikashrey.glimzocore.vault;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

public class VaultEconomyProvider implements Economy {

    private final GlimzoCore plugin;

    public VaultEconomyProvider(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getServicesManager()
              .register(Economy.class, this, plugin, ServicePriority.Highest);
        plugin.log("&a      Vault Economy provider registered.");
    }

    @Override public boolean isEnabled()       { return true; }
    @Override public String  getName()          { return "GlimzoCore"; }
    @Override public boolean hasBankSupport()   { return false; }
    @Override public int     fractionalDigits() { return 0; }
    @Override public String  format(double amount) { return (long) amount + " coins"; }
    @Override public String  currencyNamePlural()  { return "coins"; }
    @Override public String  currencyNameSingular(){ return "coin"; }

    @Override
    public boolean hasAccount(String player)           { return getPlayerData(player) != null; }
    @Override
    public boolean hasAccount(OfflinePlayer player)    { return hasAccount(player.getName()); }
    @Override
    public boolean hasAccount(String player, String w) { return hasAccount(player); }
    @Override
    public boolean hasAccount(OfflinePlayer p, String w){ return hasAccount(p); }

    @Override
    public boolean createPlayerAccount(String player)           { return true; }
    @Override
    public boolean createPlayerAccount(OfflinePlayer player)    { return true; }
    @Override
    public boolean createPlayerAccount(String p, String w)      { return true; }
    @Override
    public boolean createPlayerAccount(OfflinePlayer p, String w){ return true; }

    @Override
    public double getBalance(String player) {
        PlayerData data = getPlayerData(player);
        return data != null ? (double) data.getCoins() : 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getName());
    }

    @Override
    public double getBalance(String player, String world) { return getBalance(player); }
    @Override
    public double getBalance(OfflinePlayer p, String w)   { return getBalance(p); }

    @Override
    public boolean has(String player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) { return has(player.getName(), amount); }
    @Override
    public boolean has(String p, String w, double a)        { return has(p, a); }
    @Override
    public boolean has(OfflinePlayer p, String w, double a) { return has(p, a); }

    @Override
    public EconomyResponse withdrawPlayer(String player, double amount) {
        long coins = (long) amount;
        long removed = plugin.getCoinManager().removeCoins(
                getUuidOrNull(player), coins, "Vault withdraw");
        if (removed > 0) {
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
        }
        return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient coins");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer p, double a)       { return withdrawPlayer(p.getName(), a); }
    @Override
    public EconomyResponse withdrawPlayer(String p, String w, double a)    { return withdrawPlayer(p, a); }
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer p, String w, double a){ return withdrawPlayer(p, a); }

    @Override
    public EconomyResponse depositPlayer(String player, double amount) {
        long coins = (long) amount;
        long added = plugin.getCoinManager().addCoins(
                getUuidOrNull(player), coins, "Vault deposit");
        return new EconomyResponse(added, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer p, double a)        { return depositPlayer(p.getName(), a); }
    @Override
    public EconomyResponse depositPlayer(String p, String w, double a)     { return depositPlayer(p, a); }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer p, String w, double a){ return depositPlayer(p, a); }

    // Bank stubs - not supported
    @Override public EconomyResponse createBank(String n, String p)           { return notSupported(); }
    @Override public EconomyResponse createBank(String n, OfflinePlayer p)    { return notSupported(); }
    @Override public EconomyResponse deleteBank(String n)                     { return notSupported(); }
    @Override public EconomyResponse bankBalance(String n)                    { return notSupported(); }
    @Override public EconomyResponse bankHas(String n, double a)              { return notSupported(); }
    @Override public EconomyResponse bankWithdraw(String n, double a)         { return notSupported(); }
    @Override public EconomyResponse bankDeposit(String n, double a)          { return notSupported(); }
    @Override public EconomyResponse isBankOwner(String n, String p)          { return notSupported(); }
    @Override public EconomyResponse isBankOwner(String n, OfflinePlayer p)   { return notSupported(); }
    @Override public EconomyResponse isBankMember(String n, String p)         { return notSupported(); }
    @Override public EconomyResponse isBankMember(String n, OfflinePlayer p)  { return notSupported(); }
    @Override public java.util.List<String> getBanks()                        { return java.util.Collections.emptyList(); }

    private EconomyResponse notSupported() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    private PlayerData getPlayerData(String name) {
        return name != null ? GlobalPlayer.get(name) : null;
    }

    private java.util.UUID getUuidOrNull(String name) {
        PlayerData data = getPlayerData(name);
        return data != null ? data.getUuid() : null;
    }
}
