package me.pikashrey.glimzocore.features.joinmessages;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.features.cosmetics.joineffect.JoinMessage;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class JoinMessageManager {

    protected final GlimzoCore plugin;
    private final Random random = new Random();

    public JoinMessageManager(GlimzoCore plugin) {
        this.plugin = plugin;
        syncJoinMessages();
    }

    public void syncJoinMessages() {
        List<String> configured = plugin.getConfigManager().getMessages().getStringList("join-quit.join-messages");
        if (configured != null && !configured.isEmpty()) JoinMessage.updateFromConfig(configured);
    }

    public void broadcastJoin(Player player) {
        RankRef rank = plugin.getRankManager().getActiveRankRef(player);

        me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState state = plugin.getCosmeticManager().getState(player);
        String pinnedId = state != null ? state.getActiveJoinMessageId() : null;
        List<String> msgs = JoinMessage.getAll();
        String msg = pinnedId != null ? findMessage(pinnedId, msgs) : (msgs.isEmpty() ? "joined." : msgs.get(random.nextInt(msgs.size())));

        String format = plugin.getConfigManager().getMessage("join-quit.join-format", "{rank_color}[{rank}] &f{player} &7{message}");
        String formatted = CC.translate(format
                .replace("{rank_color}", rank.getColorCode())
                .replace("{rank}",       rank.getDisplayName())
                .replace("{player}",     player.getName())
                .replace("{message}",    msg));

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (plugin.getSettingsManager().canSeeJoinMessages(online.getUniqueId())) online.sendMessage(formatted);
        }
    }

    public void broadcastQuit(Player player) {
        RankRef rank = plugin.getRankManager().getActiveRankRef(player);
        String format = plugin.getConfigManager().getMessage("join-quit.quit-format", "{rank_color}[{rank}] &f{player} &7left the server");
        String formatted = CC.translate(format
                .replace("{rank_color}", rank.getColorCode())
                .replace("{rank}",       rank.getDisplayName())
                .replace("{player}",     player.getName()));

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && plugin.getSettingsManager().canSeeJoinMessages(online.getUniqueId())) {
                online.sendMessage(formatted);
            }
        }
    }

    private String findMessage(String id, List<String> msgs) {
        try {
            int idx = Integer.parseInt(id);
            if (idx >= 0 && idx < msgs.size()) return msgs.get(idx);
        } catch (NumberFormatException ignored) {}
        return msgs.isEmpty() ? "joined." : msgs.get(random.nextInt(msgs.size()));
    }
}
