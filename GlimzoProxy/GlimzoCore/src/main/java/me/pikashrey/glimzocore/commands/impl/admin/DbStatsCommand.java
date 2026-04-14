package me.pikashrey.glimzocore.commands.impl.admin;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.commands.api.CommandArgs;
import me.pikashrey.glimzocore.commands.api.GlimzoCommand;
import me.pikashrey.glimzocore.database.mysql.MySQLManager;

public class DbStatsCommand implements GlimzoCommand {

    private final GlimzoCore plugin;
    public DbStatsCommand(GlimzoCore p) { this.plugin = p; }

    @Override public String  getPermission() { return "glimzo.admin.dbstats"; }
    @Override public boolean isPlayerOnly()  { return false; }

    @Override
    public void execute(CommandArgs a) {
        MySQLManager db = plugin.getMysqlManager();
        if (!db.isConnected()) { a.send("&cMySQL is not connected."); return; }

        a.send("&8&m------------------------------");
        a.send("&b&lHikariCP Pool Stats");
        a.send(" &7Active connections:  &f" + db.getActiveConnections());
        a.send(" &7Idle connections:    &f" + db.getIdleConnections());
        a.send(" &7Total connections:   &f" + db.getTotalConnections());
        a.send(" &7Threads waiting:     &f" + db.getThreadsAwaitingConnection());
        a.send(" &7Online players:      &f" + org.bukkit.Bukkit.getOnlinePlayers().size());
        a.send("&8&m------------------------------");
    }
}
