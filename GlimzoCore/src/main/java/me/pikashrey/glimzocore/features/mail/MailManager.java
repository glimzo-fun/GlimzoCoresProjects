package me.pikashrey.glimzocore.features.mail;

import me.pikashrey.glimzocore.GlimzoCore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MailManager {

    protected final GlimzoCore plugin;

    public MailManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void send(UUID senderUuid, String senderName, UUID recipientUuid, String body) {
        String sql = "INSERT INTO glimzo_mail (sender_uuid, sender_name, recipient_uuid, subject, body, sent_at) " +
                     "VALUES (?, ?, ?, '', ?, ?)";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, senderUuid.toString());
            ps.setString(2, senderName);
            ps.setString(3, recipientUuid.toString());
            ps.setString(4, body);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[Mail] send failed: " + e.getMessage());
        }
    }

    public List<MailEntry> getInbox(UUID uuid) {
        List<MailEntry> list = new ArrayList<>();
        String sql = "SELECT id, sender_name, body, sent_at, read_at FROM glimzo_mail " +
                     "WHERE recipient_uuid = ? ORDER BY sent_at DESC LIMIT 50";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MailEntry(
                            rs.getInt("id"),
                            rs.getString("sender_name"),
                            rs.getString("body"),
                            rs.getLong("sent_at"),
                            rs.getLong("read_at")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.log("&c[Mail] inbox query failed: " + e.getMessage());
        }
        return list;
    }

    /** Fetch a single mail entry by ID, verifying ownership. Returns null if not found. */
    public MailEntry getEntry(java.util.UUID recipientUuid, int id) {
        String sql = "SELECT id, sender_name, body, sent_at, read_at FROM glimzo_mail "
                   + "WHERE id = ? AND recipient_uuid = ? LIMIT 1";
        try (java.sql.Connection con = plugin.getMysqlManager().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, recipientUuid.toString());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MailEntry(
                            rs.getInt("id"),
                            rs.getString("sender_name"),
                            rs.getString("body"),
                            rs.getLong("sent_at"),
                            rs.getLong("read_at")
                    );
                }
            }
        } catch (java.sql.SQLException e) {
            plugin.log("&c[Mail] getEntry failed: " + e.getMessage());
        }
        return null;
    }

    public void markRead(int id) {
        String sql = "UPDATE glimzo_mail SET read_at = ? WHERE id = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[Mail] markRead failed: " + e.getMessage());
        }
    }

    public void delete(UUID owner, int id) {
        String sql = "DELETE FROM glimzo_mail WHERE id = ? AND recipient_uuid = ?";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, owner.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.log("&c[Mail] delete failed: " + e.getMessage());
        }
    }

    public int countUnread(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM glimzo_mail WHERE recipient_uuid = ? AND read_at = -1";
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.log("&c[Mail] countUnread failed: " + e.getMessage());
        }
        return 0;
    }

    public static class MailEntry {
        public final int    id;
        public final String senderName;
        public final String body;
        public final long   sentAt;
        public final long   readAt;

        public MailEntry(int id, String senderName, String body, long sentAt, long readAt) {
            this.id         = id;
            this.senderName = senderName;
            this.body       = body;
            this.sentAt     = sentAt;
            this.readAt     = readAt;
        }

        public boolean isRead() { return readAt != -1; }
    }
}
