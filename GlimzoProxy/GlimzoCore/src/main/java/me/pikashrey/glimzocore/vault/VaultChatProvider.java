package me.pikashrey.glimzocore.vault;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.Rank;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.utilities.chat.CC;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

public class VaultChatProvider extends Chat {

    private final GlimzoCore plugin;

    public VaultChatProvider(GlimzoCore plugin) {
        super(null);
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getServicesManager()
                .register(Chat.class, this, plugin, ServicePriority.Highest);
        plugin.log("&a      Vault Chat provider registered.");
    }

    @Override
    public String getName() {
        return "GlimzoCore";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPlayerPrefix(String world, String playerName) {
        org.bukkit.entity.Player online = plugin.getServer().getPlayerExact(playerName);
        if (online == null) return "";
        return CC.translate(plugin.getRankManager().getChatPrefix(online.getUniqueId()));
    }

    @Override public void setPlayerPrefix(String world, String playerName, String prefix) {}
    @Override public String getPlayerSuffix(String world, String playerName) { return ""; }
    @Override public void setPlayerSuffix(String world, String playerName, String suffix) {}

    @Override
    public String getPlayerPrefix(String world, OfflinePlayer player) {
        org.bukkit.entity.Player online = plugin.getServer().getPlayer(player.getUniqueId());
        if (online == null) return CC.translate(plugin.getRankManager().getChatPrefix(player.getUniqueId()));
        return CC.translate(plugin.getRankManager().getChatPrefix(online.getUniqueId()));
    }

    @Override public void setPlayerPrefix(String world, OfflinePlayer player, String prefix) {}
    @Override public String getPlayerSuffix(String world, OfflinePlayer player) { return ""; }
    @Override public void setPlayerSuffix(String world, OfflinePlayer player, String suffix) {}

    @Override
    public String getGroupPrefix(String world, String group) {
        RankRef ref =
                RankRef.of(group);
        return ref.isValid() ? CC.translate(ref.getChatPrefix()) : "";
    }

    @Override public void setGroupPrefix(String world, String group, String prefix) {}
    @Override public String getGroupSuffix(String world, String group) { return ""; }
    @Override public void setGroupSuffix(String world, String group, String suffix) {}

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    // Removed @Override (not abstract in some Vault versions)
    public boolean hasGroupSupport() {
        return true;
    }

    // Removed @Override
    public String getPlayerPrimaryGroup(String world, String playerName) {
        org.bukkit.entity.Player online = plugin.getServer().getPlayerExact(playerName);
        if (online == null) return Rank.getDefault().getId();
        return plugin.getRankManager().getActiveRankRef(online).getId();
    }

    @Override
    public String[] getPlayerGroups(String world, String playerName) {
        return new String[]{ getPlayerPrimaryGroup(world, playerName) };
    }

    @Override
    public boolean playerInGroup(String world, String playerName, String group) {
        return getPlayerPrimaryGroup(world, playerName).equalsIgnoreCase(group);
    }

    public String getPlayerPrimaryGroup(String world, OfflinePlayer player) {
        org.bukkit.entity.Player online = plugin.getServer().getPlayer(player.getUniqueId());
        if (online == null) return Rank.getDefault().getId();
        return plugin.getRankManager().getActiveRankRef(online).getId();
    }

    @Override
    public String[] getPlayerGroups(String world, OfflinePlayer player) {
        return new String[]{ getPlayerPrimaryGroup(world, player) };
    }

    // Removed @Override
    public boolean playerInGroup(String world, OfflinePlayer player, String group) {
        return getPlayerPrimaryGroup(world, player).equalsIgnoreCase(group);
    }

    @Override public String  getGroupInfoString (String w, String g, String k, String d) { return d; }
    @Override public void    setGroupInfoString (String w, String g, String k, String v) {}
    @Override public int     getGroupInfoInteger(String w, String g, String k, int d) { return d; }
    @Override public void    setGroupInfoInteger(String w, String g, String k, int v) {}
    @Override public double  getGroupInfoDouble (String w, String g, String k, double d) { return d; }
    @Override public void    setGroupInfoDouble (String w, String g, String k, double v) {}
    @Override public boolean getGroupInfoBoolean(String w, String g, String k, boolean d) { return d; }
    @Override public void    setGroupInfoBoolean(String w, String g, String k, boolean v) {}

    @Override public String  getPlayerInfoString (String w, String p, String k, String d) { return d; }
    @Override public void    setPlayerInfoString (String w, String p, String k, String v) {}
    @Override public int     getPlayerInfoInteger(String w, String p, String k, int d) { return d; }
    @Override public void    setPlayerInfoInteger(String w, String p, String k, int v) {}
    @Override public double  getPlayerInfoDouble (String w, String p, String k, double d) { return d; }
    @Override public void    setPlayerInfoDouble (String w, String p, String k, double v) {}
    @Override public boolean getPlayerInfoBoolean(String w, String p, String k, boolean d) { return d; }
    @Override public void    setPlayerInfoBoolean(String w, String p, String k, boolean v) {}

    @Override public String  getPlayerInfoString (String w, OfflinePlayer p, String k, String d) { return d; }
    @Override public void    setPlayerInfoString (String w, OfflinePlayer p, String k, String v) {}
    @Override public int     getPlayerInfoInteger(String w, OfflinePlayer p, String k, int d) { return d; }
    @Override public void    setPlayerInfoInteger(String w, OfflinePlayer p, String k, int v) {}
    @Override public double  getPlayerInfoDouble (String w, OfflinePlayer p, String k, double d) { return d; }
    @Override public void setPlayerInfoDouble (String w, OfflinePlayer p, String k, double v) {}
    @Override public boolean getPlayerInfoBoolean(String w, OfflinePlayer p, String k, boolean d) { return d; }
    @Override public void    setPlayerInfoBoolean(String w, OfflinePlayer p, String k, boolean v) {}
}