package me.pikashrey.glimzocore.tasks;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.clan.ClanLeaderboard;
import me.pikashrey.glimzocore.features.season.SeasonLeaderboard;

public class LeaderboardTask implements Runnable {

    protected final GlimzoCore plugin;

    public LeaderboardTask(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ClanLeaderboard  clanLb   = plugin.getClanLeaderboard();
        SeasonLeaderboard seasonLb = plugin.getSeasonLeaderboard();

        if (clanLb   != null) clanLb.refresh();
        if (seasonLb != null) seasonLb.refresh();
    }
}

