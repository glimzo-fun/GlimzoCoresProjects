package me.pikashrey.glimzocore.nametags.adapter;

import me.pikashrey.glimzocore.nametags.NameTag;
import org.bukkit.entity.Player;

public interface NameTagAdapter {

    NameTag getNameTag(Player player);
}

