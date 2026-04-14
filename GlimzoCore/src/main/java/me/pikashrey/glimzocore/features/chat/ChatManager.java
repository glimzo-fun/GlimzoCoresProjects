package me.pikashrey.glimzocore.features.chat;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {

    protected final GlimzoCore plugin;

    private final Map<UUID, UUID>    lastPM      = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> ignoreLists = new ConcurrentHashMap<>();

    public ChatManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void loadIgnoreList(UUID uuid) {
        ignoreLists.put(uuid, fetchIgnoredFromDb(uuid));
    }

    public void loadIgnoreListFromData(UUID uuid, java.util.Set<String> uuidStrings) {
        Set<UUID> set = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (String s : uuidStrings) { try { set.add(UUID.fromString(s)); } catch (Exception ignored) {} }
        ignoreLists.put(uuid, set);
    }

    public void unloadIgnoreList(UUID uuid) {
        ignoreLists.remove(uuid);
        lastPM.remove(uuid);
    }

    public void ignore(UUID uuid, UUID target) {
        ignoreLists.computeIfAbsent(uuid,
                k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(target);
        me.pikashrey.glimzocore.utilities.general.Tasks.async(() -> saveIgnoreToDb(uuid, target));
    }

    public void unignore(UUID uuid, UUID target) {
        Set<UUID> list = ignoreLists.get(uuid);
        if (list != null) list.remove(target);
        me.pikashrey.glimzocore.utilities.general.Tasks.async(() -> removeIgnoreFromDb(uuid, target));
    }

    public boolean isIgnoring(UUID uuid, UUID target) {
        Set<UUID> list = ignoreLists.get(uuid);
        return list != null && list.contains(target);
    }

    public boolean sendPrivateMessage(Player from, Player to, String message) {
        PlayerData toData = GlobalPlayer.get(to);
        if (toData != null && !toData.getSettings().isPrivateMessagesEnabled()) {
            from.sendMessage(CC.translate("&c" + to.getName() + " has private messages disabled."));
            return false;
        }
        if (isIgnoring(to.getUniqueId(), from.getUniqueId())) {
            from.sendMessage(CC.translate("&c" + to.getName() + " is not accepting messages from you."));
            return false;
        }

        String fromPrefix = plugin.getRankManager().getChatPrefix(from.getUniqueId());
        String toPrefix   = plugin.getRankManager().getChatPrefix(to.getUniqueId());

        from.sendMessage(CC.translate("&7(To " + toPrefix + " &7" + to.getName() + "&7) &f" + message));
        to.sendMessage(CC.translate("&7(From " + fromPrefix + " &7" + from.getName() + "&7) &f" + message));

        lastPM.put(to.getUniqueId(), from.getUniqueId());
        broadcastSocialSpy(from, to, message);
        return true;
    }

    public UUID getLastMessageSender(UUID uuid) { return lastPM.get(uuid); }

    private void broadcastSocialSpy(Player from, Player to, String message) {
        String spy = CC.translate("&8[&eSpy&8] " + from.getName() + " &7-> " + to.getName() + "&8: &7" + message);
        for (PlayerData data : GlobalPlayer.getAll()) {
            if (data.isSocialSpyEnabled()) {
                Player p = Bukkit.getPlayer(data.getUuid());
                if (p != null && !p.equals(from) && !p.equals(to)) p.sendMessage(spy);
            }
        }
    }

    private Set<UUID> fetchIgnoredFromDb(UUID uuid) {
        Set<UUID> set = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
        String sql = "SELECT ignored_uuid FROM glimzo_ignore WHERE uuid = ?";
        try (java.sql.Connection con = plugin.getMysqlManager().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(UUID.fromString(rs.getString("ignored_uuid")));
            }
        } catch (java.sql.SQLException e) {
            plugin.log("&c[ChatManager] Failed to load ignore list for " + uuid);
        }
        return set;
    }

    private void saveIgnoreToDb(UUID uuid, UUID ignored) {
        String sql = "INSERT IGNORE INTO glimzo_ignore (uuid, ignored_uuid) VALUES (?,?)";
        try (java.sql.Connection con = plugin.getMysqlManager().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, ignored.toString());
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            plugin.log("&c[ChatManager] Failed to save ignore entry.");
        }
    }

    private void removeIgnoreFromDb(UUID uuid, UUID ignored) {
        String sql = "DELETE FROM glimzo_ignore WHERE uuid = ? AND ignored_uuid = ?";
        try (java.sql.Connection con = plugin.getMysqlManager().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, ignored.toString());
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            plugin.log("&c[ChatManager] Failed to remove ignore entry.");
        }
    }
}
