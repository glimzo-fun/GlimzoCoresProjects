package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.vpn.VpnChecker;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class BanCheckListener implements Listener {

    private final GlimzoCore plugin;

    public BanCheckListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID   uuid = event.getUniqueId();
        String ip   = event.getAddress().getHostAddress();

        // Log IP for alt detection (always, regardless of outcome)
        plugin.getMysqlManager().logIp(uuid, ip);

        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT banned, ban_expiry, ban_reason FROM glimzo_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean banned    = rs.getBoolean("banned");
                    long    banExpiry = rs.getLong("ban_expiry");
                    String  reason    = rs.getString("ban_reason");

                    if (banned) {
                        if (banExpiry == -1 || System.currentTimeMillis() < banExpiry) {
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                                    buildBanMessage(reason, banExpiry));
                            return; // banned - no further checks needed
                        } else {
                            liftExpiredBan(uuid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.log("&c[BanCheck] Ban check failed for " + uuid + ": " + e.getMessage());
        }

        if (!plugin.getConfig().getBoolean("vpn.enabled", true)) return;

        VpnChecker.Result vpnResult = plugin.getVpnChecker().check(ip);

        if (vpnResult == VpnChecker.Result.VPN_OR_PROXY) {
            // Check if player has baron+ rank in the database
            if (!hasBaronOrAbove(uuid)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        buildVpnKickMessage());
            }
            // If they have baron+, fall through and let them join normally
        }
        // API_ERROR or CLEAN → fall through and allow
    }


    private String buildBanMessage(String reason, long expiry) {
        me.pikashrey.glimzocore.managers.ConfigManager cfg = plugin.getConfigManager();
        StringBuilder sb = new StringBuilder();
        sb.append(CC.translate(cfg.getMessage("ban-screen.header",
                "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n&c&l✗  You are banned from this network.\n")));
        sb.append("\n");

        if (reason != null && !reason.isEmpty()) {
            sb.append(CC.translate(cfg.getMessage("ban-screen.reason", "&7Reason: &f{reason}")
                    .replace("{reason}", reason))).append("\n");
        }

        if (expiry == -1) {
            sb.append(CC.translate(cfg.getMessage("ban-screen.permanent", "&7Duration: &cPermanent")));
        } else {
            long remaining = expiry - System.currentTimeMillis();
            String formatted = remaining > 0
                    ? ChatListener.formatRemaining(remaining)
                    : "Expired";
            sb.append(CC.translate(cfg.getMessage("ban-screen.expires", "&7Expires in: &f{duration}")
                    .replace("{duration}", formatted)));
        }

        return sb.toString().trim();
    }


    private String buildVpnKickMessage() {
        String store = plugin.getConfig().getString("store-link", "store.glimzo.net");

        // Each line is exactly 50 chars wide inside the border for visual balance.
        // Colour scheme: dark border + gold accents + white body + green store link.
        return CC.translate(
                "\n"
              + "&8&m+--------------------------------------------------+\n"
              + "&8|                                                  &8|\n"
              + "&8|   &6&lVPN &8/ &6&lProxy &7Detected                          &8|\n"
              + "&8|                                                  &8|\n"
              + "&8|   &7Sorry, you are unable to connect to the       &8|\n"
              + "&8|   &7server because you are using a &c&lVPN &7or        &8|\n"
              + "&8|   &c&lProxy&7.                                         &8|\n"
              + "&8|                                                  &8|\n"
              + "&8|   &7Players &cwithout &eBARON&7+ rank are not allowed  &8|\n"
              + "&8|   &7to join through a VPN or proxy.               &8|\n"
              + "&8|                                                  &8|\n"
              + "&8|   &aGet &e&lBARON &aor above from our store and you   &8|\n"
              + "&8|   &awill be able to join with VPN &8/ &aproxy          &8|\n"
              + "&8|   &asupport unlocked.                             &8|\n"
              + "&8|                                                  &8|\n"
              + "&8|   &7Store &8» &b&n" + store + "                        &8|\n"
              + "&8|                                                  &8|\n"
              + "&8&m+--------------------------------------------------+"
              + "\n"
        );
    }


    /**
     * Queries the grants table directly since the player isn't loaded yet.
     * Returns true if the player has any active grant with a rank weight >= baron (100).
     *
     * This deliberately does NOT load ranks.yml weight at query time - instead
     * it checks rank IDs we know are baron or above.  If the rank system is purely
     * config-driven the weight comparison would need a separate DB column; instead
     * we rely on the known rank id list which is cheaper and safe.
     */
    private boolean hasBaronOrAbove(UUID uuid) {
        int bypassWeight = plugin.getConfig().getInt("vpn.bypass-weight", 100);
        if (bypassWeight <= 0) return false; // bypass disabled

        // Collect all rank ids with weight >= bypass-weight from the loaded rank definitions
        java.util.Set<String> qualifyingIds = new java.util.HashSet<>();
        try {
            me.pikashrey.glimzocore.features.rank.RankLoader loader = plugin.getRankLoader();
            if (loader != null) {
                for (me.pikashrey.glimzocore.features.rank.ConfiguredRank rank : loader.getAll()) {
                    if (rank.getWeight() >= bypassWeight) {
                        qualifyingIds.add(rank.getId().toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            // If rank loader isn't available, fail safe and use hardcoded fallback
            plugin.log("&e[VPN] Could not read rank loader, using fallback rank list.");
            qualifyingIds.addAll(java.util.Arrays.asList(
                    "baron", "warden", "mythic", "legendary", "ascendant", "cosmos",
                    "guide", "supportagents", "guardian", "pibble", "architect",
                    "admin", "chief", "creators"
            ));
        }

        if (qualifyingIds.isEmpty()) return false;

        // Build a parameterised IN clause
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < qualifyingIds.size(); i++) {
            inClause.append(i == 0 ? "?" : ",?");
        }

        String sql = "SELECT 1 FROM glimzo_grants " +
                     "WHERE target_uuid = ? AND active = 1 " +
                     "AND (expires_at = -1 OR expires_at > ?) " +
                     "AND LOWER(rank_id) IN (" + inClause + ") " +
                     "LIMIT 1";

        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, System.currentTimeMillis());
            int idx = 3;
            for (String id : qualifyingIds) {
                ps.setString(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // found at least one qualifying active grant
            }
        } catch (Exception e) {
            plugin.log("&c[VPN] Rank query failed for " + uuid + ": " + e.getMessage()
                    + " - failing open.");
            return true; // fail-open: can't check, allow through
        }
    }


    private void liftExpiredBan(UUID uuid) {
        try (Connection con = plugin.getMysqlManager().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE glimzo_players SET banned = 0, ban_expiry = -1, ban_reason = NULL WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.log("&c[BanCheck] Failed to lift expired ban for " + uuid + ": " + e.getMessage());
        }
    }
}
