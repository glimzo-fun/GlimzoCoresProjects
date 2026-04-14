package me.pikashrey.glimzocore.listeners;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.nick.NickManager;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

public class NickBookListener implements Listener {

    protected final GlimzoCore plugin;

    public NickBookListener(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookEdit(PlayerEditBookEvent event) {
        if (event.isSigning()) return; // only intercept edits, not signing

        Player player = event.getPlayer();
        BookMeta meta = event.getNewBookMeta();

        if (!meta.hasPages() || meta.getPageCount() == 0) return;

        // Check if this player has an active nick book session
        // (We mark it via a metadata tag on the player)
        if (!player.hasMetadata("nick_book")) return;
        player.removeMetadata("nick_book", plugin);

        // Extract first page text, strip formatting symbols
        String raw = meta.getPage(1).trim();
        if (raw.isEmpty()) {
            player.sendMessage(CC.translate("&cNickname cannot be empty."));
            return;
        }

        NickManager.Result result = plugin.getNickManager().setCustomNick(player, raw);
        switch (result) {
            case SUCCESS:
                player.sendMessage(CC.translate("&aNickname set to &f" + raw + "&a."));
                break;
            case TOO_LONG:
                player.sendMessage(CC.translate("&cToo long (max 16 characters)."));
                break;
            case TOO_SHORT:
                player.sendMessage(CC.translate("&cToo short (min 3 characters)."));
                break;
            case INVALID_CHARS:
                player.sendMessage(CC.translate("&cInvalid characters. Letters, digits, underscores only."));
                break;
            case NO_COLOR_PERMISSION:
                player.sendMessage(CC.translate("&cYou don't have permission to use colors."));
                break;
            case IMPERSONATION:
                player.sendMessage(CC.translate("&cThat name belongs to another player."));
                break;
            default:
                player.sendMessage(CC.translate("&cCould not set nickname."));
        }
    }
}
