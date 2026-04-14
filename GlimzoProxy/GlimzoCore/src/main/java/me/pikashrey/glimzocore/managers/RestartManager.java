package me.pikashrey.glimzocore.managers;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class RestartManager {

    protected final GlimzoCore plugin;

    private BukkitTask countdownTask;
    private int secondsRemaining = -1;

    private static final int[] BROADCAST_AT = {300, 180, 120, 60, 30, 10, 5, 4, 3, 2, 1};

    public RestartManager(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void scheduleRestart(int delaySeconds) {
        cancelRestart();
        secondsRemaining = Math.max(5, delaySeconds);

        countdownTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (secondsRemaining <= 0) {
                broadcastSync(plugin.getConfigManager().getMessage("restart.now", "&c&lServer restarting now!"));
                Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);
                cancelRestart();
                return;
            }

            for (int milestone : BROADCAST_AT) {
                if (secondsRemaining == milestone) {
                    String fmt = plugin.getConfigManager().getMessage("restart.countdown", "&eServer restart in &c{time}&e.");
                    broadcastSync(fmt.replace("{time}", formatSeconds(secondsRemaining)));
                    break;
                }
            }

            secondsRemaining--;
        }, 0L, 20L);
    }

    public void cancelRestart() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        secondsRemaining = -1;
    }

    public int     getSecondsRemaining()  { return secondsRemaining; }
    public boolean isRestartScheduled()   { return secondsRemaining > 0; }

    private void broadcastSync(String message) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(CC.translate(message)));
    }

    private static String formatSeconds(int s) {
        if (s >= 60) {
            int mins = s / 60;
            int secs = s % 60;
            return mins + " minute" + (mins != 1 ? "s" : "") + (secs > 0 ? " " + secs + "s" : "");
        }
        return s + " second" + (s != 1 ? "s" : "");
    }
}
