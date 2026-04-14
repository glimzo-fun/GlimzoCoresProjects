package me.pikashrey.glimzocore.features.cosmetics.morph;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.morph.impl.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MorphManager implements Listener {

    private final GlimzoCore            plugin;
    private final Map<String, BaseMorph> registry = new LinkedHashMap<>();
    private final Map<UUID, BaseMorph>   active   = new ConcurrentHashMap<>();
    // Fake NMS entity per morphed player (packet-only, never added to world)
    private final Map<UUID, EntityLiving> fakeEntities = new ConcurrentHashMap<>();
    private BukkitTask task;

    public MorphManager(GlimzoCore plugin) {
        this.plugin = plugin;
        register(new BlazeMorph());
        register(new SlimeMorph());
        register(new CreeperMorph());
        register(new ChickenMorph());
        register(new BatMorph());
        register(new EndermanMorph());
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void register(BaseMorph morph) { registry.put(morph.getId(), morph); }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<UUID> toRemove = new ArrayList<>();
            for (Map.Entry<UUID, BaseMorph> entry : active.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                // Move fake entity to player position for all viewers
                EntityLiving fake = fakeEntities.get(entry.getKey());
                if (fake != null) {
                    Location loc = player.getLocation();
                    fake.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    PacketPlayOutEntityTeleport tpPacket = new PacketPlayOutEntityTeleport(fake);
                    for (Player viewer : player.getWorld().getPlayers()) {
                        if (!viewer.equals(player)) sendPacket(viewer, tpPacket);
                    }
                }
                entry.getValue().onTick(player);
            }
            for (UUID uuid : toRemove) {
                cleanupFake(uuid);
                active.remove(uuid);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) showPlayerToAll(p);
            }
        }, 0L, 2L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
        for (UUID uuid : new ArrayList<>(active.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            cleanupFake(uuid);
            if (p != null) showPlayerToAll(p);
        }
        active.clear();
    }

    public boolean morph(Player player, String morphId) {
        BaseMorph morph = registry.get(morphId);
        if (morph == null) return false;

        unmorph(player);

        // Create fake NMS entity (packet-only, never spawned in world)
        EntityLiving fake = createFakeEntity(morph.getType(), player.getLocation());
        if (fake == null) return false;

        fakeEntities.put(player.getUniqueId(), fake);
        active.put(player.getUniqueId(), morph);
        morph.onEquip(player);

        // Hide real player, show fake mob to all others
        hidePlayerFromAll(player);
        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(fake);
        for (Player other : player.getWorld().getPlayers()) {
            if (!other.equals(player)) sendPacket(other, spawnPacket);
        }

        return true;
    }

    public void unmorph(Player player) {
        BaseMorph morph = active.remove(player.getUniqueId());
        if (morph == null) return;
        morph.onUnequip(player);
        cleanupFake(player.getUniqueId());
        showPlayerToAll(player);
    }

    public boolean isMorphed(Player player) { return active.containsKey(player.getUniqueId()); }
    public BaseMorph getActiveMorph(Player player) { return active.get(player.getUniqueId()); }
    public Map<String, BaseMorph> getRegistry() { return new HashMap<>(registry); }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (isMorphed(event.getPlayer())) unmorph(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        // Show existing morphs to new joiner
        for (Map.Entry<UUID, EntityLiving> entry : fakeEntities.entrySet()) {
            Player morphed = Bukkit.getPlayer(entry.getKey());
            if (morphed == null || morphed.equals(joiner)) continue;
            if (!morphed.getWorld().equals(joiner.getWorld())) continue;
            sendPacket(joiner, new PacketPlayOutSpawnEntityLiving(entry.getValue()));
            joiner.hidePlayer(morphed);
        }
    }

    public void onPlayerJoin(Player joiner) {
        // Called from CosmeticManager - delegates to event-driven version above
    }

    private void cleanupFake(UUID uuid) {
        EntityLiving fake = fakeEntities.remove(uuid);
        if (fake == null) return;
        int[] ids = { fake.getId() };
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(ids);
        for (Player other : Bukkit.getOnlinePlayers()) sendPacket(other, destroyPacket);
    }

    private EntityLiving createFakeEntity(MorphType type, Location loc) {
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
        EntityLiving entity;
        switch (type) {
            case BLAZE:    entity = new EntityBlaze(world);    break;
            case SLIME:    entity = new EntitySlime(world);    break;
            case CREEPER:  entity = new EntityCreeper(world);  break;
            case CHICKEN:  entity = new EntityChicken(world);  break;
            case BAT:      entity = new EntityBat(world);      break;
            case ENDERMAN: entity = new EntityEnderman(world); break;
            default: return null;
        }
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        // Suppress AI flags via DataWatcher so it appears still
        // No AI needed - entity is never added to world, only used for packets
        return entity;
    }

    private void hidePlayerFromAll(Player target) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(target)) other.hidePlayer(target);
        }
    }

    private void showPlayerToAll(Player target) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(target)) other.showPlayer(target);
        }
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
