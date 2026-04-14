package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.PlayerData;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.features.clan.ClanManager;
import me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState;
import me.pikashrey.glimzocore.features.cosmetics.chattag.ChatTagManager;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {

    protected final GlimzoCore plugin;

    // Chat cooldown: UUID -> timestamp when they're next allowed to chat (ms)
    private final Map<UUID, Long>   chatCooldown  = new ConcurrentHashMap<>();
    // Rank prefix cache - avoids a DB/rank lookup on every message.
    // Invalidated on rank change (via RankChangeListener) and on quit.
    private final Map<UUID, String> prefixCache   = new ConcurrentHashMap<>();

    // Cooldown duration in ms - 1 second between messages
    private static final long CHAT_COOLDOWN_MS = 1_000L;

    public ChatListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID   uuid   = player.getUniqueId();

        PlayerData data = GlobalPlayer.get(uuid);
        if (data != null && data.isMuted()) {
            event.setCancelled(true);
            player.sendMessage(CC.translate(buildMuteMessage(data)));
            return;
        }

        if (plugin.getPartyManager().hasPartyChatEnabled(uuid)) {
            event.setCancelled(true);
            if (plugin.getPartyManager().isInParty(uuid)) {
                plugin.getPartyManager().sendPartyChat(uuid, event.getMessage());
            } else {
                // Party disbanded while chat was toggled - clean up silently
                player.sendMessage(CC.translate("&cYou are not in a party."));
            }
            return;
        }

        ClanManager cm = plugin.getClanManager();
        if (cm != null && cm.hasClanChatEnabled(uuid)) {
            event.setCancelled(true);
            me.pikashrey.glimzocore.api.clan.ClanData clan = cm.getClanByPlayer(uuid);
            if (clan != null) {
                String rankPrefix = plugin.getRankManager().getChatPrefix(uuid);
                cm.sendClanChat(clan, rankPrefix, player.getName(), event.getMessage());
            }
            return;
        }

        long now  = System.currentTimeMillis();
        Long next = chatCooldown.get(uuid);
        if (next != null && now < next) {
            event.setCancelled(true);
            long remainMs = next - now;
            player.sendMessage(CC.translate(
                    "&cYou're sending messages too fast. Wait &f"
                    + String.format("%.1f", remainMs / 1000.0)
                    + "s&c."));
            return;
        }
        chatCooldown.put(uuid, now + CHAT_COOLDOWN_MS);

        String message = event.getMessage();
        message = replaceEmojis(message);
        event.setMessage(message);
        

        ChatTagManager ctm = plugin.getCosmeticManager() != null
                ? plugin.getCosmeticManager().getChatTagManager()
                : null;
        if (ctm == null) return;

        PlayerCosmeticState state = plugin.getCosmeticManager().getState(player);
        String rankPrefix = prefixCache.computeIfAbsent(uuid, k ->
                plugin.getRankManager() != null
                        ? plugin.getRankManager().getChatPrefix(k)
                        : "&7[Baron]");
        
        // Use nick if player has one, otherwise use real name
        String displayName = player.getName();
        if (data != null && data.hasNick()) {
            String nick = data.getNick();
            if (nick != null) {
                displayName = CC.translate(nick);
            }
        }

        event.setFormat(ctm.buildChatPrefix(rankPrefix, displayName, state) + "%2$s");
    }

    private String buildMuteMessage(PlayerData data) {
        long expiry = data.getMuteExpiry();
        String reason = data.getMuteReason() != null ? data.getMuteReason() : "No reason given";

        if (expiry == -1) {
            return "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                   "&c&l✗ &cYou are permanently muted.\n" +
                   "&7Reason &8» &f" + reason + "\n" +
                   "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        }

        long remaining = expiry - System.currentTimeMillis();
        String formatted = remaining > 0 ? formatRemaining(remaining) : "Expired";

        return "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
               "&c&l✗ &cYou are muted.\n" +
               "&7Reason &8» &f" + reason + "\n" +
               "&7Expires &8» &e" + formatted + "\n" +
               "&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    }

    /** Format remaining milliseconds as "2d 3h", "4h 12m", "5m 30s", or "45s". */
    public static String formatRemaining(long ms) {
        long secs  = ms / 1000;
        long days  = secs / 86400;
        long hours = (secs % 86400) / 3600;
        long mins  = (secs % 3600) / 60;
        long sec   = secs % 60;
        if (days  > 0) return days  + "d " + hours + "h";
        if (hours > 0) return hours + "h " + mins  + "m";
        if (mins  > 0) return mins  + "m " + sec   + "s";
        return sec + "s";
    }

    /** Called from PlayerQuitListener to clean up the cooldown entry for this player. */
    public void onQuit(java.util.UUID uuid) {
        chatCooldown.remove(uuid);
        prefixCache.remove(uuid);
    }

    private String replaceEmojis(String message) {
        me.pikashrey.glimzocore.utilities.chat.Symbols sym = null;
        message = message.replace(":star:", me.pikashrey.glimzocore.utilities.chat.Symbols.STAR);
        message = message.replace(":heart:", me.pikashrey.glimzocore.utilities.chat.Symbols.HEART);
        message = message.replace(":diamond:", me.pikashrey.glimzocore.utilities.chat.Symbols.DIAMOND);
        message = message.replace(":check:", me.pikashrey.glimzocore.utilities.chat.Symbols.CHECKMARK);
        message = message.replace(":cross:", me.pikashrey.glimzocore.utilities.chat.Symbols.CROSS);
        message = message.replace(":lightning:", me.pikashrey.glimzocore.utilities.chat.Symbols.LIGHTNING);
        message = message.replace(":crown:", me.pikashrey.glimzocore.utilities.chat.Symbols.CROWN);
        message = message.replace(":sword:", me.pikashrey.glimzocore.utilities.chat.Symbols.SWORD);
        message = message.replace(":bullet:", me.pikashrey.glimzocore.utilities.chat.Symbols.BULLET);
        message = message.replace(":arrow:", me.pikashrey.glimzocore.utilities.chat.Symbols.ARROW_RIGHT);
        return message;
    }

    /** Called by RankChangeListener and PlayerQuitListener to clear stale prefix cache. */
    public void invalidatePrefix(java.util.UUID uuid) {
        prefixCache.remove(uuid);
    }
}
