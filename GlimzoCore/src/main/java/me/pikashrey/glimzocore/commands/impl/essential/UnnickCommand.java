package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.player.GlobalPlayer;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UnnickCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public UnnickCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; } // open to anyone with a nick
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        // Staff can unnick others: /unnick <player>
        if (a.has(0) && player.hasPermission("glimzo.staff.nick")) {
            Player target = Bukkit.getPlayer(a.get(0));
            if (target == null) {
                player.sendMessage(CC.translate("&c&l✗ &cPlayer not online."));
                return;
            }
            if (!plugin.getNickManager().hasNick(target.getUniqueId())) {
                player.sendMessage(CC.translate("&f" + target.getName() + " &7doesn't have a nickname."));
                return;
            }
            plugin.getNickManager().clearNick(target);
            target.sendMessage(CC.translate("&7Your nickname has been removed by a staff member."));
            player.sendMessage(CC.translate("&a&l✔ &aRemoved nickname for &f" + target.getName() + "&a."));

            // Broadcast removal to other servers via social sync
            broadcastUnnick(target);
            return;
        }

        // Self-unnick
        if (!plugin.getNickManager().hasNick(player.getUniqueId())) {
            player.sendMessage(CC.translate("&7You don't have a nickname to remove."));
            return;
        }

        plugin.getNickManager().clearNick(player);
        player.sendMessage(CC.translate("&a&l✔ &aYour nickname has been removed."));

        // Broadcast to other servers so their tablist/chat picks it up
        broadcastUnnick(player);
    }

    /**
     * Tells other servers to clear this player's nick.
     * Reuses the social sync NICK_CLEAR event type - receiving servers call
     * clearNick() on the player if they're online there too.
     */
    private void broadcastUnnick(Player player) {
        if (plugin.getSocialSync() == null) return;
        // Publish as a friend-remove style one-off - the payload is just the UUID.
        // NetworkSocialSync.handleEvent() will be extended to handle NICK_CLEAR.
        plugin.getSocialSync().publishNickClear(player.getUniqueId());
    }
}
