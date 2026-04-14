package me.pikashrey.glimzocore.commands.impl.essential.staff;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.mail.MailManager;
import me.pikashrey.glimzocore.menus.MailInboxMenu;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class MailCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public MailCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; }
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        // /mail or /mail inbox → open GUI
        if (!a.has(0) || a.get(0).equalsIgnoreCase("inbox")) {
            openInbox(player);
            return;
        }

        switch (a.get(0).toLowerCase()) {

            case "send": {
                if (!a.has(2)) { a.usage("/mail send <player> <message>"); return; }
                String targetName = a.get(1);
                String body       = a.join(2);
                handleSend(player, targetName, body);
                break;
            }

            case "delete": {
                if (!a.has(1)) { a.usage("/mail delete <id>"); return; }
                int id;
                try { id = Integer.parseInt(a.get(1)); }
                catch (NumberFormatException e) { a.sendError("Invalid ID."); return; }
                final int fid = id;
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getMailManager().delete(player.getUniqueId(), fid);
                    player.sendMessage(CC.translate("&a&l✔ &aMail &8#" + fid + " &adeleted."));
                });
                break;
            }

            default:
                showHelp(player);
        }
    }


    private void openInbox(Player player) {
        player.sendMessage(CC.translate("&7Loading your inbox..."));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MailManager.MailEntry> inbox =
                    plugin.getMailManager().getInbox(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                if (inbox.isEmpty()) {
                    player.sendMessage(CC.translate("&7Your inbox is empty."));
                    return;
                }
                new MailInboxMenu(plugin, player, inbox, 0).open();
            });
        });
    }


    private void handleSend(Player sender, String targetName, String body) {
        if (body.trim().isEmpty()) { sender.sendMessage(CC.translate("&cMessage cannot be empty.")); return; }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID targetUuid = resolveUuid(targetName);
            if (targetUuid == null) {
                sender.sendMessage(CC.translate("&c&l✗ &cPlayer &f" + targetName + " &cnot found."));
                return;
            }
            plugin.getMailManager().send(sender.getUniqueId(), sender.getName(), targetUuid, body);
            final UUID fuid = targetUuid;
            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage(CC.translate("&a&l✔ &aMail sent to &f" + targetName + "&a."));
                Player online = Bukkit.getPlayer(fuid);
                if (online != null) {
                    online.sendMessage(CC.translate(
                            "&2[&a&lMail&2] &fNew mail from &d" + sender.getName()
                            + "&f. Run &a/mail &fto read it."));
                }
            });
        });
    }


    private void showHelp(Player player) {
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("  &a&lMail Commands"));
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("  &a/mail               &8» &7Open your inbox"));
        player.sendMessage(CC.translate("  &a/mail send &f<player> <msg> &8» &7Send a mail"));
        player.sendMessage(CC.translate("  &a/mail delete &f<id>  &8» &7Delete a mail"));
        player.sendMessage(CC.translate("&2&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    @SuppressWarnings("deprecation")
    private UUID resolveUuid(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) return online.getUniqueId();
        OfflinePlayer off = Bukkit.getOfflinePlayer(name);
        return (off != null && off.hasPlayedBefore()) ? off.getUniqueId() : null;
    }
}
