package me.pikashrey.glimzocore.features.staff;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuildModeManager {

    // runtime-only, resets on restart intentionally
    private final Set<UUID> builders = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean toggle(UUID uuid) {
        if (builders.contains(uuid)) {
            builders.remove(uuid);
            return false;
        }
        builders.add(uuid);
        return true;
    }

    public boolean isBuilder(UUID uuid) { return builders.contains(uuid); }
    public void remove(UUID uuid)       { builders.remove(uuid); }
}
