package me.pikashrey.glimzocore120;

import me.pikashrey.glimzocore120.chat.ChatManager120;
import me.pikashrey.glimzocore120.commands.CoinsCommand120;
import me.pikashrey.glimzocore120.commands.RankCommand120;
import me.pikashrey.glimzocore120.cosmetics.CosmeticManager120;
import me.pikashrey.glimzocore120.database.DatabaseManager120;
import me.pikashrey.glimzocore120.economy.CoinManager120;
import me.pikashrey.glimzocore120.listeners.PlayerListener120;
import me.pikashrey.glimzocore120.network.BridgeListener120;
import me.pikashrey.glimzocore120.player.PlayerDataManager120;
import me.pikashrey.glimzocore120.rank.RankManager120;
import me.pikashrey.glimzocore120.scoreboard.ScoreboardManager120;
import me.pikashrey.glimzocore120.tablist.TablistManager120;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GlimzoCore120 extends JavaPlugin {

    private static GlimzoCore120 instance;

    private DatabaseManager120   databaseManager;
    private PlayerDataManager120 playerDataManager;
    private RankManager120       rankManager;
    private CoinManager120       coinManager;
    private ChatManager120       chatManager;
    private TablistManager120    tablistManager;
    private ScoreboardManager120 scoreboardManager;
    private CosmeticManager120   cosmeticManager;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        getLogger().info("Starting GlimzoCore-120...");

        // Database first
        databaseManager = new DatabaseManager120(this);
        if (!databaseManager.connect()) {
            getLogger().severe("MySQL connection failed! Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        databaseManager.createTables();

        // Managers
        playerDataManager = new PlayerDataManager120(this);
        rankManager       = new RankManager120(this);
        coinManager       = new CoinManager120(this);
        chatManager       = new ChatManager120(this);
        tablistManager    = new TablistManager120(this);
        scoreboardManager = new ScoreboardManager120(this);
        cosmeticManager   = new CosmeticManager120(this);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener120(this), this);

        // BungeeCord channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BridgeListener120(this));

        // Commands
        getCommand("coins").setExecutor(new CoinsCommand120(this));
        getCommand("rank").setExecutor(new RankCommand120(this));

        // Start autosave task - every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> playerDataManager.saveAll(), 6000L, 6000L);

        getLogger().info("GlimzoCore-120 enabled in " +
                (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void onDisable() {
        // Save all online players synchronously before shutdown
        if (playerDataManager != null) playerDataManager.saveAll();
        if (databaseManager != null)   databaseManager.disconnect();
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getLogger().info("GlimzoCore-120 disabled cleanly.");
    }

    public static GlimzoCore120 getInstance()              { return instance; }
    public DatabaseManager120   getDatabaseManager()       { return databaseManager; }
    public PlayerDataManager120 getPlayerDataManager()     { return playerDataManager; }
    public RankManager120       getRankManager()           { return rankManager; }
    public CoinManager120       getCoinManager()           { return coinManager; }
    public ChatManager120       getChatManager()           { return chatManager; }
    public TablistManager120    getTablistManager()        { return tablistManager; }
    public ScoreboardManager120 getScoreboardManager()     { return scoreboardManager; }
    public CosmeticManager120   getCosmeticManager()       { return cosmeticManager; }
}
