package me.pikashrey.glimzocore.nametags.adapter.impl;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.api.rank.RankRef;
import me.pikashrey.glimzocore.nametags.NameTag;
import me.pikashrey.glimzocore.nametags.adapter.NameTagAdapter;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.entity.Player;

public class NameTagAdapterImpl implements NameTagAdapter {

    private final GlimzoCore plugin;

    public NameTagAdapterImpl(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public NameTag getNameTag(Player player) {
        RankRef rank = plugin.getRankManager().getActiveRankRef(player);

        // Translate the full rank prefix e.g. "&4&l[CHIEF] " → "§4§l[CHIEF] "
        // "§4§l[CHIEF] " = 12 chars, fits fine.
        // "§6[BARON] " = 9 chars, fits fine.
        String prefix = CC.translate(rank.getChatPrefix());
        if (prefix.length() > 16) prefix = prefix.substring(0, 16);

        return new NameTag(prefix, "");
    }
}