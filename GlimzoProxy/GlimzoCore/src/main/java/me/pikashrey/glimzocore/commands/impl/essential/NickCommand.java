package me.pikashrey.glimzocore.commands.impl.essential;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.features.nick.NickManager;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;

public class NickCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public NickCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return null; } // rank check done inside
    @Override public boolean isPlayerOnly()  { return true; }

    @Override
    public void execute(CommandArgs a) {
        Player player = a.getPlayer();
        if (player == null) return;

        NickManager nm = plugin.getNickManager();

        if (!nm.hasNickAccess(player)) {
            player.sendMessage(CC.translate("&c&l✗ &cYou need &1[Warden]&c or above to use nicknames."));
            return;
        }

        if (nm.isRandomNickTier(player)) {
            if (a.has(0)) {
                player.sendMessage(CC.translate(
                        "&e&l⚠ &1[Warden] &7players receive a random nickname.\n" +
                        "&7Run &e/nick &7with no arguments to get one assigned."));
                return;
            }
            NickManager.Result result = nm.assignRandomNick(player);
            if (result == NickManager.Result.SUCCESS) {
                player.sendMessage(CC.translate(
                        "&aYou have been given the nickname: &f" + nm.getNick(player.getUniqueId()) + "&a."));
            } else if (result == NickManager.Result.NO_POOL) {
                player.sendMessage(CC.translate("&c&l✗ &cNo nicknames available right now. Try again later."));
            } else {
                player.sendMessage(CC.translate("&cCould not assign a nickname."));
            }
            return;
        }

        if (nm.isRankNickTier(player)) {
            if (!a.has(0)) {
                showCosmosHelp(player, nm);
                return;
            }
            String nickArg = a.get(0);
            String rankArg = a.has(1) ? a.get(1) : null;
            NickManager.Result result = nm.setCustomNick(player, nickArg, rankArg);
            handleResult(player, result, nickArg);
            return;
        }

        if (!a.has(0)) {
            showLegendaryHelp(player, nm);
            return;
        }
        NickManager.Result result = nm.setCustomNick(player, a.get(0));
        handleResult(player, result, a.get(0));
    }

    private void handleResult(Player player, NickManager.Result result, String nick) {
        switch (result) {
            case SUCCESS:
                player.sendMessage(CC.translate("&aNickname set to &f" + nick + "&a."));
                break;
            case TOO_LONG:
                player.sendMessage(CC.translate("&cNickname too long (max 16 characters)."));
                break;
            case TOO_SHORT:
                player.sendMessage(CC.translate("&cNickname too short (min 3 characters)."));
                break;
            case INVALID_CHARS:
                player.sendMessage(CC.translate("&cInvalid characters. Letters, digits, and underscores only."));
                break;
            case NO_COLOR_PERMISSION:
                player.sendMessage(CC.translate("&cYou don't have permission to use colour codes in your nick."));
                break;
            case IMPERSONATION:
                player.sendMessage(CC.translate("&cThat name belongs to another player."));
                break;
            case INVALID_RANK:
                player.sendMessage(CC.translate("&cThat rank doesn't exist."));
                break;
            case RANK_TOO_HIGH:
                player.sendMessage(CC.translate("&cYou can only spoof ranks below your own."));
                break;
            default:
                player.sendMessage(CC.translate("&cCould not set nickname."));
        }
    }

    private void showLegendaryHelp(Player player, NickManager nm) {
        player.sendMessage(CC.translate("&e&l» &7Custom nick: &a/nick &2<nickname>"));
        if (nm.hasNick(player.getUniqueId())) {
            player.sendMessage(CC.translate("&7Current &8» &7" + nm.getNick(player.getUniqueId())));
            player.sendMessage(CC.translate("&7Remove it &8» &a/unnick"));
        }
    }

    private void showCosmosHelp(Player player, NickManager nm) {
        player.sendMessage(CC.translate("&5&l» &7Custom nick: &a/nick &2<nickname>"));
        player.sendMessage(CC.translate("&5&l» &7With rank spoof: &a/nick &2<nickname> <rank>"));
        player.sendMessage(CC.translate("&7  (rank must be below your own rank)"));
        if (nm.hasNick(player.getUniqueId())) {
            String rankSuffix = nm.getNickRank(player.getUniqueId()) != null
                    ? " &8[rank: &7" + nm.getNickRank(player.getUniqueId()) + "&8]" : "";
            player.sendMessage(CC.translate("&7Current &8» &7" + nm.getNick(player.getUniqueId()) + rankSuffix));
            player.sendMessage(CC.translate("&7Remove it &8» &a/unnick"));
        }
    }
}
