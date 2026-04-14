package me.pikashrey.glimzocore.discord;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.utilities.discord.DiscordWebhook;

public class DiscordManager {

    protected final GlimzoCore plugin;

    private boolean enabled;
    private String  logWebhook;
    private String  staffWebhook;
    private String  banWebhook;

    public DiscordManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        enabled      = plugin.getConfig().getBoolean("discord.enabled", false);
        logWebhook   = plugin.getConfig().getString("discord.log-webhook", "");
        staffWebhook = plugin.getConfig().getString("discord.staff-webhook", "");
        banWebhook   = plugin.getConfig().getString("discord.ban-webhook", "");

        if (enabled) {
            plugin.log("&7Discord webhooks &aenabled&7.");
        }
    }

    public void disable() {
        // No persistent connection to close - webhook sends are fire-and-forget
    }

    // Public API

    /**
     * Send a generic log message to the log webhook (async).
     */
    public void sendLog(String title, String description, int colour) {
        if (!enabled || logWebhook.isEmpty()) return;
        sendAsync(logWebhook, title, description, colour);
    }

    /**
     * Send a staff alert to the staff webhook (async).
     */
    public void sendStaffAlert(String title, String description) {
        if (!enabled || staffWebhook.isEmpty()) return;
        sendAsync(staffWebhook, title, description, 0xE74C3C);
    }

    /**
     * Send a ban notification to the ban webhook (async).
     */
    public void sendBanNotification(String bannedPlayer, String reason, String bannedBy) {
        if (!enabled || banWebhook.isEmpty()) return;
        sendAsync(banWebhook,
                "Player Banned",
                "**" + bannedPlayer + "** was banned by **" + bannedBy + "**\nReason: " + reason,
                0xE74C3C);
    }

    /**
     * Send a join notification to the log webhook (async).
     */
    public void sendJoinLog(String playerName, String rank) {
        if (!enabled || logWebhook.isEmpty()) return;
        sendAsync(logWebhook,
                "Player Joined",
                "**" + playerName + "** [" + rank + "] joined the server.",
                0x2ECC71);
    }

    private void sendAsync(String webhookUrl, String title, String description, int colour) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean ok = new DiscordWebhook(webhookUrl)
                    .setUsername("GlimzoCore")
                    .setEmbed(title, description, colour)
                    .send();
            if (!ok) {
                plugin.log("&c[Discord] Webhook send failed for: " + title);
            }
        });
    }

    public boolean isEnabled() { return enabled; }
}

