package me.pikashrey.glimzocore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.pikashrey.glimzocore.database.mysql.MySQLManager;
import me.pikashrey.glimzocore.discord.DiscordManager;
import me.pikashrey.glimzocore.features.achievements.AchievementManager;
import me.pikashrey.glimzocore.features.announcer.AnnouncerManager;
import me.pikashrey.glimzocore.features.chat.ChatManager;
import me.pikashrey.glimzocore.features.clan.ClanManager;
import me.pikashrey.glimzocore.features.clan.ClanLeaderboard;
import me.pikashrey.glimzocore.features.rank.RankManager;
import me.pikashrey.glimzocore.features.rank.RankLoader;
import me.pikashrey.glimzocore.features.rank.NetworkRankSync;
import me.pikashrey.glimzocore.features.season.SeasonLeaderboard;
import me.pikashrey.glimzocore.nametags.handler.NameTagHandler;
import me.pikashrey.glimzocore.features.cosmetics.CosmeticManager;
import me.pikashrey.glimzocore.features.cosmetics.ally.AllyItemManager;
import me.pikashrey.glimzocore.listeners.AllyItemListener;
import me.pikashrey.glimzocore.features.economy.coins.CoinManager;
import me.pikashrey.glimzocore.features.economy.gems.GemManager;
import me.pikashrey.glimzocore.features.friends.FriendManager;
import me.pikashrey.glimzocore.features.joinmessages.JoinMessageManager;
import me.pikashrey.glimzocore.features.leveling.LevelManager;
import me.pikashrey.glimzocore.features.leveling.PrestigeManager;
import me.pikashrey.glimzocore.features.lore.LoreManager;
import me.pikashrey.glimzocore.features.nick.NickManager;
import me.pikashrey.glimzocore.features.party.PartyManager;
import me.pikashrey.glimzocore.features.punishments.PunishmentManager;
import me.pikashrey.glimzocore.features.scoreboard.ScoreboardManager;
import me.pikashrey.glimzocore.features.season.SeasonManager;
import me.pikashrey.glimzocore.features.settings.SettingsManager;
import me.pikashrey.glimzocore.features.staff.StaffManager;
import me.pikashrey.glimzocore.features.tablist.TablistManager;
import me.pikashrey.glimzocore.listeners.*;
import me.pikashrey.glimzocore.managers.ConfigManager;
import me.pikashrey.glimzocore.managers.LobbyManager;
import me.pikashrey.glimzocore.network.BridgeListener;
import me.pikashrey.glimzocore.placeholder.GlimzoExpansion;
import me.pikashrey.glimzocore.tasks.AutoSaveTask;
import me.pikashrey.glimzocore.tasks.LeaderboardTask;
import me.pikashrey.glimzocore.tasks.SeasonTickTask;
import me.pikashrey.glimzocore.utilities.chat.CC;
import me.pikashrey.glimzocore.features.permission.PermissionCache;
import me.pikashrey.glimzocore.features.permission.PermissionManager;
import me.pikashrey.glimzocore.features.permission.PermissionResolver;
import me.pikashrey.glimzocore.features.rank.security.RankCooldownManager;
import me.pikashrey.glimzocore.features.rank.security.RankSecurityValidator;
import me.pikashrey.glimzocore.vault.VaultProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GlimzoCore extends JavaPlugin {

    private static GlimzoCore instance;

    private Gson gson;
    private ConfigManager configManager;
    private MySQLManager mysqlManager;

    private CoinManager coinManager;
    private GemManager gemManager;
    private LevelManager levelManager;
    private PrestigeManager prestigeManager;
    private SeasonManager seasonManager;
    private FriendManager friendManager;
    private PartyManager partyManager;
    private ClanManager clanManager;
    private RankManager rankManager;
    private PunishmentManager punishmentManager;
    private me.pikashrey.glimzocore.features.season.SeasonPassManager seasonPassManager;
    private me.pikashrey.glimzocore.features.clan.ClanLevelManager clanLevelManager;
    private NickManager nickManager;
    private ChatManager chatManager;
    private StaffManager staffManager;
    private me.pikashrey.glimzocore.features.staff.BuildModeManager buildModeManager;
    private me.pikashrey.glimzocore.features.sync.SyncManager syncManager;
    private me.pikashrey.glimzocore.features.vpn.VpnChecker vpnChecker;
    private me.pikashrey.glimzocore.listeners.InventoryClickListener inventoryClickListener;
    private me.pikashrey.glimzocore.listeners.ChatListener chatListenerInstance;
    private me.pikashrey.glimzocore.listeners.LevelingListener levelingListener;
    private AchievementManager achievementManager;
    private AnnouncerManager announcerManager;
    private JoinMessageManager joinMessageManager;
    private SettingsManager settingsManager;
    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private CosmeticManager cosmeticManager;
    private AllyItemManager allyItemManager;
    private me.pikashrey.glimzocore.features.hotbar.HotbarManager hotbarManager;
    private LoreManager loreManager;
    private DiscordManager discordManager;
    private LobbyManager lobbyManager;
    private NameTagHandler nameTagHandler;
    private ClanLeaderboard clanLeaderboard;
    private SeasonLeaderboard seasonLeaderboard;
    private PermissionCache permissionCache;
    private PermissionResolver permissionResolver;
    private PermissionManager permissionManager;
    private RankLoader rankLoader;
    private RankCooldownManager rankCooldownManager;
    private RankSecurityValidator rankSecurityValidator;
    private NetworkRankSync networkRankSync;
    private me.pikashrey.glimzocore.features.social.NetworkSocialSync socialSync;
    private me.pikashrey.glimzocore.managers.RestartManager restartManager;
    private me.pikashrey.glimzocore.features.mail.MailManager mailManager;
    private me.pikashrey.glimzocore.managers.DatabaseManager databaseManager;

    private boolean disabling = false;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();
        printHeader();

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .create();

        log("&7[1/8] &bLoading configuration...");
        configManager = new ConfigManager(this);
        configManager.load();

        log("&7[2/8] &bConnecting to MySQL...");
        mysqlManager = new MySQLManager(this);
        if (!mysqlManager.connect()) {
            log("&c[!] MySQL connection failed. Disabling GlimzoCore.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        log("&a      MySQL connected.");

        log("&7[3/8] &bInitialising managers...");
        initManagers();

        log("&7[5/8] &bRegistering listeners...");
        registerListeners();

        log("&7[5b] &bRegistering commands...");
        registerCommands();

        log("&7[5c] &bInitialising nametag system...");
        nameTagHandler = new NameTagHandler(this);

        log("&7[5d] &bStarting scoreboard & tablist...");
        scoreboardManager.enable();

        log("&7[6/8] &bRegistering BungeeCord channel...");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BridgeListener());

        log("&7[7/8] &bHooking soft-dependencies...");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GlimzoExpansion(this).register();
            log("&a      PlaceholderAPI registered.");
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            new VaultProvider(this).setup();
            log("&a      Vault registered.");
        }

        log("&7[8/8] &bStarting background tasks...");
        startTasks();

        printFooter(System.currentTimeMillis() - start);
    }

    @Override
    public void onDisable() {
        disabling = true;
        getServer().getScheduler().cancelTasks(this);

        if (announcerManager != null) announcerManager.stop();
        if (staffManager != null) {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (staffManager.isInStaffMode(p.getUniqueId())) {
                    staffManager.toggleStaffMode(p);
                }
            }
        }

        me.pikashrey.glimzocore.api.player.GlobalPlayer.saveAll();

        if (cosmeticManager != null) {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                cosmeticManager.savePlayer(p.getUniqueId());
            }
        }

        if (cosmeticManager != null)   cosmeticManager.disable();
        if (scoreboardManager != null) scoreboardManager.disable();
        if (tablistManager != null)    tablistManager.disable();
        if (discordManager != null)    discordManager.disable();

        if (databaseManager != null) databaseManager.stop();
        if (networkRankSync != null) networkRankSync.stop();
        if (socialSync != null) socialSync.stop();
        if (mysqlManager != null) {
            mysqlManager.disconnect();
            log("&aMySQL disconnected.");
        }

        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        log("&a[GlimzoCore] Disabled cleanly.");
    }

    private void initManagers() {
        coinManager     = new CoinManager(this);
        gemManager      = new GemManager(this);
        levelManager    = new LevelManager(this);
        prestigeManager = new PrestigeManager(this);
        seasonManager   = new SeasonManager(this);
        friendManager   = new FriendManager(this);
        partyManager    = new PartyManager(this);
        clanManager     = new ClanManager(this);

        permissionCache    = new PermissionCache();
        permissionResolver = new PermissionResolver(permissionCache);
        rankLoader         = new RankLoader(this, permissionCache, permissionResolver);
        rankLoader.load();
        permissionManager     = new PermissionManager(this, permissionCache);
        rankCooldownManager   = new RankCooldownManager();
        rankSecurityValidator = new RankSecurityValidator(this, rankCooldownManager);
        networkRankSync       = new NetworkRankSync(this);

        rankManager       = new RankManager(this, permissionCache);
        punishmentManager = new PunishmentManager(this);
        seasonPassManager = new me.pikashrey.glimzocore.features.season.SeasonPassManager(this);
        clanLevelManager  = new me.pikashrey.glimzocore.features.clan.ClanLevelManager(this);
        staffManager      = new StaffManager(this);
        buildModeManager  = new me.pikashrey.glimzocore.features.staff.BuildModeManager();
        syncManager       = new me.pikashrey.glimzocore.features.sync.SyncManager(this);
        syncManager.enable();
        vpnChecker        = new me.pikashrey.glimzocore.features.vpn.VpnChecker(this);
        nickManager       = new NickManager(this);
        chatManager       = new ChatManager(this);
        achievementManager = new AchievementManager(this);
        announcerManager  = new AnnouncerManager(this);
        announcerManager.start();
        joinMessageManager = new JoinMessageManager(this);
        settingsManager    = new SettingsManager(this);
        cosmeticManager    = new CosmeticManager(this);
        cosmeticManager.enable();
        allyItemManager = new AllyItemManager();
        cosmeticManager.getAllyPerkManager().setAllyItemManager(allyItemManager);
        hotbarManager = new me.pikashrey.glimzocore.features.hotbar.HotbarManager(this);
        Bukkit.getPluginManager().registerEvents(hotbarManager, this);
        loreManager       = new LoreManager(this);
        scoreboardManager = new ScoreboardManager(this);
        tablistManager    = new TablistManager(this);
        lobbyManager      = new LobbyManager(this);
        discordManager    = new DiscordManager(this);
        discordManager.enable();
        clanLeaderboard   = new ClanLeaderboard(this);
        seasonLeaderboard = new SeasonLeaderboard(this);

        restartManager  = new me.pikashrey.glimzocore.managers.RestartManager(this);
        mailManager     = new me.pikashrey.glimzocore.features.mail.MailManager(this);
        databaseManager = new me.pikashrey.glimzocore.managers.DatabaseManager(this);
        databaseManager.start();

        networkRankSync.start();
        socialSync = new me.pikashrey.glimzocore.features.social.NetworkSocialSync(this);
        socialSync.start();
    }

    private void registerListeners() {
        inventoryClickListener = new InventoryClickListener(this);
        chatListenerInstance   = new me.pikashrey.glimzocore.listeners.ChatListener(this);
        levelingListener       = new me.pikashrey.glimzocore.listeners.LevelingListener(this);
        register(
                new BanCheckListener(this),
                new PlayerJoinListener(this),
                new PlayerQuitListener(this),
                new PlayerInteractListener(this),
                new ChatListener(this),
                inventoryClickListener,
                new InventoryListener(this),
                new BlockListener(this),
                new EntityDamageListener(this),
                new FoodListener(this),
                new ItemDropListener(this),
                new PlayerMoveListener(this),
                new PlayerDeathListener(this),
                new WorldListener(this),
                new CommandBlockerListener(this),
                new DiscordLogListener(this),
                new InteractListener(this),
                new RankChangeListener(this),
                new AllyItemListener(this, allyItemManager),
                new me.pikashrey.glimzocore.menu.MenuManager(this),
                new me.pikashrey.glimzocore.listeners.AchievementListener(this),
                levelingListener,
                new me.pikashrey.glimzocore.listeners.NickBookListener(this)
        );
    }

    private void registerCommands() {
        reg("ban",          new me.pikashrey.glimzocore.commands.impl.punishments.BanCommand(this));
        reg("tempban",      new me.pikashrey.glimzocore.commands.impl.punishments.TempBanCommand(this));
        reg("unban",        new me.pikashrey.glimzocore.commands.impl.punishments.UnbanCommand(this));
        reg("mute",         new me.pikashrey.glimzocore.commands.impl.punishments.MuteCommand(this));
        reg("tempmute",     new me.pikashrey.glimzocore.commands.impl.punishments.TempMuteCommand(this));
        reg("unmute",       new me.pikashrey.glimzocore.commands.impl.punishments.UnmuteCommand(this));
        reg("kick",         new me.pikashrey.glimzocore.commands.impl.punishments.KickCommand(this));
        reg("warn",         new me.pikashrey.glimzocore.commands.impl.punishments.WarnCommand(this));
        reg("history",      new me.pikashrey.glimzocore.commands.impl.punishments.HistoryCommand(this));
        reg("warnings",     new me.pikashrey.glimzocore.commands.impl.punishments.WarningsCommand(this));
        reg("check",        new me.pikashrey.glimzocore.commands.impl.punishments.CheckCommand(this));
        reg("alts",         new me.pikashrey.glimzocore.commands.impl.punishments.AltsCommand(this));
        reg("checknick",    new me.pikashrey.glimzocore.commands.impl.punishments.CheckNickCommand(this));
        reg("coins",        new me.pikashrey.glimzocore.commands.impl.coins.CoinsCommand(this));
        reg("gems",         new me.pikashrey.glimzocore.commands.impl.gems.GemsCommand(this));
        reg("rank",         new me.pikashrey.glimzocore.commands.impl.rank.RankCommand(this));
        reg("giftrank",     new me.pikashrey.glimzocore.commands.impl.rank.GiftRankCommand(this));
        reg("setprefix",    new me.pikashrey.glimzocore.commands.impl.rank.SetPrefixCommand(this));
        reg("season",       new me.pikashrey.glimzocore.commands.impl.season.SeasonCommand(this));
        reg("seasonpass",   new me.pikashrey.glimzocore.commands.impl.season.SeasonPassCommand(this));
        reg("goat",         new me.pikashrey.glimzocore.commands.impl.stats.GoatCommand(this));
        reg("restart",      new me.pikashrey.glimzocore.commands.impl.admin.RestartCommand(this));
        reg("dbstats",      new me.pikashrey.glimzocore.commands.impl.admin.DbStatsCommand(this));
        reg("glimzo",       new me.pikashrey.glimzocore.commands.impl.admin.ReloadCommand(this));
        reg("cosmetics",    new me.pikashrey.glimzocore.commands.impl.cosmetics.CosmeticsCommand(this));
        reg("ally",         new me.pikashrey.glimzocore.commands.impl.cosmetics.AllyCommand(this,
                                cosmeticManager.getAllyPerkManager(),
                                cosmeticManager.getAllyLevelManager()));
        reg("sit",          new me.pikashrey.glimzocore.commands.impl.essential.SitCommand(this));
        reg("lie",          new me.pikashrey.glimzocore.commands.impl.essential.LieCommand(this));
        reg("lore",         new me.pikashrey.glimzocore.commands.impl.lore.LoreCommand(this));
        reg("quests",       new me.pikashrey.glimzocore.commands.impl.quests.QuestsCommand(this));
        reg("nick",         new me.pikashrey.glimzocore.commands.impl.essential.NickCommand(this));
        reg("unnick",       new me.pikashrey.glimzocore.commands.impl.essential.UnnickCommand(this));
        reg("ping",         new me.pikashrey.glimzocore.commands.impl.essential.PingCommand(this));
        reg("fly",          new me.pikashrey.glimzocore.commands.impl.essential.FlyCommand(this));
        reg("spawn",        new me.pikashrey.glimzocore.commands.impl.essential.SpawnCommand(this));
        reg("setspawn",     new me.pikashrey.glimzocore.commands.impl.essential.SetSpawnCommand(this));
        reg("visibility",   new me.pikashrey.glimzocore.commands.impl.essential.PlayerVisibilityCommand(this));
        reg("whereami",     new me.pikashrey.glimzocore.commands.impl.essential.WhereAmICommand(this));
        reg("discord",      new me.pikashrey.glimzocore.commands.impl.essential.DiscordCommand(this));
        reg("store",        new me.pikashrey.glimzocore.commands.impl.essential.StoreCommand(this));
        reg("forums",       new me.pikashrey.glimzocore.commands.impl.essential.ForumsCommand(this));
        reg("report",       new me.pikashrey.glimzocore.commands.impl.essential.ReportCommand(this));
        reg("emojis",       new me.pikashrey.glimzocore.commands.impl.essential.EmojisCommand(this));
        reg("help",         new me.pikashrey.glimzocore.commands.impl.essential.HelpCommand(this));
        reg("msg",          new me.pikashrey.glimzocore.commands.impl.essential.messages.MsgCommand(this));
        reg("message",      new me.pikashrey.glimzocore.commands.impl.essential.messages.MessageCommand(this));
        reg("reply",        new me.pikashrey.glimzocore.commands.impl.essential.messages.ReplyCommand(this));
        reg("ignore",       new me.pikashrey.glimzocore.commands.impl.essential.messages.IgnoreCommand(this));
        reg("socialspy",    new me.pikashrey.glimzocore.commands.impl.essential.messages.SocialSpyCommand(this));
        reg("vanish",       new me.pikashrey.glimzocore.commands.impl.essential.staff.VanishCommand(this));
        reg("staff",        new me.pikashrey.glimzocore.commands.impl.essential.staff.StaffCommand(this));
        reg("broadcast",    new me.pikashrey.glimzocore.commands.impl.essential.staff.BroadcastCommand(this));
        reg("alert",        new me.pikashrey.glimzocore.commands.impl.essential.staff.AlertCommand(this));
        reg("sc",           new me.pikashrey.glimzocore.commands.impl.essential.staff.StaffChatCommand(this));
        reg("freeze",       new me.pikashrey.glimzocore.commands.impl.essential.staff.FreezeCommand(this));
        reg("invsee",       new me.pikashrey.glimzocore.commands.impl.essential.staff.InvseeCommand(this));
        reg("clearchat",    new me.pikashrey.glimzocore.commands.impl.essential.staff.ClearChatCommand(this));
        reg("mail",         new me.pikashrey.glimzocore.commands.impl.essential.staff.MailCommand(this));
        reg("friend",       new me.pikashrey.glimzocore.commands.impl.friends.FriendCommand(this));
        reg("friendlist",   new me.pikashrey.glimzocore.commands.impl.friends.FriendListCommand(this));
        reg("party",        new me.pikashrey.glimzocore.commands.impl.party.PartyCommand(this));
        reg("clan",         new me.pikashrey.glimzocore.commands.impl.clan.ClanCommand(this));
        reg("level",        new me.pikashrey.glimzocore.commands.impl.leveling.LevelCommand(this));
        reg("prestige",     new me.pikashrey.glimzocore.commands.impl.leveling.PrestigeCommand(this));
        reg("stats",        new me.pikashrey.glimzocore.commands.impl.stats.StatsCommand(this));
        reg("profile",      new me.pikashrey.glimzocore.commands.impl.profile.ProfileCommand(this));
        reg("settings",     new me.pikashrey.glimzocore.commands.impl.settings.SettingsCommand(this));
        reg("achievements", new me.pikashrey.glimzocore.commands.impl.essential.AchievementsCommand(this));
        reg("gameselector",  new me.pikashrey.glimzocore.commands.impl.essential.GameSelectorCommand(this));
        reg("lobbyselector", new me.pikashrey.glimzocore.commands.impl.essential.LobbySelectorCommand(this));
        reg("gemsshop",      new me.pikashrey.glimzocore.commands.impl.essential.GemsShopCommand(this));
        reg("joinmessages",  new me.pikashrey.glimzocore.commands.impl.essential.JoinMessagesCommand(this));
        getCommand("permcheck").setExecutor(new me.pikashrey.glimzocore.commands.debug.PermCheckCommand(this));
    }

    private void reg(String name, me.pikashrey.glimzocore.commands.api.GlimzoCommand cmd) {
        me.pikashrey.glimzocore.commands.api.manager.CommandHandler.register(this, name, cmd);
    }

    private void register(Listener... listeners) {
        for (Listener l : listeners) getServer().getPluginManager().registerEvents(l, this);
    }

    private void startTasks() {
        long autosaveTicks    = configManager.getSettings().getLong("autosave.interval-ticks",    6000L);
        long leaderboardTicks = configManager.getSettings().getLong("leaderboard.interval-ticks", 12000L);
        long seasonTicks      = configManager.getSettings().getLong("season.tick-interval-ticks", 200L);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new AutoSaveTask(this),    600L, autosaveTicks);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new LeaderboardTask(this), 1200L, leaderboardTicks);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new SeasonTickTask(this),  200L, seasonTicks);
        Bukkit.getScheduler().runTaskTimer(this,
                new me.pikashrey.glimzocore.nametags.update.NameTagThread(this), 100L, 100L);
        // prune old social_events + rank_logs every 6 hours
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                new me.pikashrey.glimzocore.tasks.PruneTask(this), 72000L, 432000L);
    }

    private void printHeader() {
        log("&8&m--------------------------------------------");
        log("   &b&lGlimzoCore &7v" + getDescription().getVersion());
        log("   &7Author &8» &fPikashrey   &7MC &8» &f1.8.8");
        log("&8&m--------------------------------------------");
    }

    private void printFooter(long ms) {
        log("&8&m--------------------------------------------");
        log("   &aEnabled in &f" + ms + "ms");
        log("   &7MySQL      &8» &aONLINE");
        log("&8&m--------------------------------------------");
    }

    public void log(String msg) {
        getServer().getConsoleSender().sendMessage(CC.translate(msg));
    }

    public static GlimzoCore getInstance()    { return instance; }
    public Gson getGson()                      { return gson; }
    public ConfigManager getConfigManager()    { return configManager; }
    public MySQLManager getMysqlManager()      { return mysqlManager; }
    public CoinManager getCoinManager()        { return coinManager; }
    public GemManager getGemManager()          { return gemManager; }
    public LevelManager getLevelManager()      { return levelManager; }
    public PrestigeManager getPrestigeManager(){ return prestigeManager; }
    public SeasonManager getSeasonManager()    { return seasonManager; }
    public FriendManager getFriendManager()    { return friendManager; }
    public PartyManager getPartyManager()      { return partyManager; }
    public ClanManager getClanManager()        { return clanManager; }
    public RankManager getRankManager()        { return rankManager; }
    public PermissionCache getPermissionCache()    { return permissionCache; }
    public PermissionResolver getPermissionResolver() { return permissionResolver; }
    public PermissionManager getPermissionManager()   { return permissionManager; }
    public RankLoader getRankLoader()              { return rankLoader; }
    public RankCooldownManager getRankCooldownManager()     { return rankCooldownManager; }
    public RankSecurityValidator getRankSecurityValidator() { return rankSecurityValidator; }
    public NetworkRankSync getNetworkRankSync()             { return networkRankSync; }
    public me.pikashrey.glimzocore.features.social.NetworkSocialSync getSocialSync() { return socialSync; }
    public PunishmentManager getPunishmentManager()        { return punishmentManager; }
    public me.pikashrey.glimzocore.features.season.SeasonPassManager getSeasonPassManager()  { return seasonPassManager; }
    public me.pikashrey.glimzocore.features.clan.ClanLevelManager getClanLevelManager()      { return clanLevelManager; }
    public me.pikashrey.glimzocore.features.cosmetics.CosmeticUnlockManager getCosmeticUnlockManager() {
        return cosmeticManager != null ? cosmeticManager.getUnlockManager() : null;
    }
    public StaffManager getStaffManager()      { return staffManager; }
    public me.pikashrey.glimzocore.features.staff.BuildModeManager getBuildModeManager() { return buildModeManager; }
    public me.pikashrey.glimzocore.features.sync.SyncManager getSyncManager()           { return syncManager; }
    public me.pikashrey.glimzocore.features.vpn.VpnChecker getVpnChecker()             { return vpnChecker; }
    public me.pikashrey.glimzocore.listeners.InventoryClickListener getInventoryClickListener() { return inventoryClickListener; }
    public me.pikashrey.glimzocore.listeners.ChatListener getChatListenerInstance()             { return chatListenerInstance; }
    public me.pikashrey.glimzocore.listeners.LevelingListener getLevelingListener()             { return levelingListener; }
    public NickManager getNickManager()        { return nickManager; }
    public ChatManager getChatManager()        { return chatManager; }
    public DiscordManager getDiscordManager()  { return discordManager; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public AnnouncerManager getAnnouncerManager()     { return announcerManager; }
    public JoinMessageManager getJoinMessageManager() { return joinMessageManager; }
    public SettingsManager getSettingsManager()       { return settingsManager; }
    public CosmeticManager getCosmeticManager()       { return cosmeticManager; }
    public AllyItemManager getAllyItemManager()        { return allyItemManager; }
    public me.pikashrey.glimzocore.features.hotbar.HotbarManager getHotbarManager() { return hotbarManager; }
    public LoreManager getLoreManager()          { return loreManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public TablistManager getTablistManager()    { return tablistManager; }
    public LobbyManager getLobbyManager()        { return lobbyManager; }
    public NameTagHandler getNameTagHandler()    { return nameTagHandler; }
    public ClanLeaderboard getClanLeaderboard()  { return clanLeaderboard; }
    public SeasonLeaderboard getSeasonLeaderboard() { return seasonLeaderboard; }
    public me.pikashrey.glimzocore.managers.RestartManager getRestartManager() { return restartManager; }
    public me.pikashrey.glimzocore.features.mail.MailManager getMailManager()  { return mailManager; }
    public me.pikashrey.glimzocore.managers.DatabaseManager getDatabaseManager() { return databaseManager; }
    public boolean isDisabling() { return disabling; }
}
