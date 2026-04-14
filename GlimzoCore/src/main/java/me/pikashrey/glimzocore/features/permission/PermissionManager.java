package me.pikashrey.glimzocore.features.permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionManager {

    private final PermissionCache cache;
    private final Plugin plugin;
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    public PermissionManager(Plugin plugin, PermissionCache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    public void applyPermissions(Player player, Set<String> permissions) {
        UUID uuid = player.getUniqueId();

        PermissionAttachment attachment = attachments.computeIfAbsent(
                uuid, u -> player.addAttachment(plugin));

        new HashSet<>(attachment.getPermissions().keySet()).forEach(attachment::unsetPermission);

        for (String perm : permissions) {
            if (perm != null && !perm.isEmpty()) attachment.setPermission(perm, true);
        }

        cache.cachePlayer(uuid, new HashSet<>(permissions));
    }

    public void invalidate(Player player) {
        cache.invalidatePlayer(player.getUniqueId());
    }

    public void remove(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = attachments.remove(uuid);
        if (attachment != null) {
            try { player.removeAttachment(attachment); }
            catch (IllegalArgumentException ignored) {}
        }
        cache.invalidatePlayer(uuid);
    }
}
