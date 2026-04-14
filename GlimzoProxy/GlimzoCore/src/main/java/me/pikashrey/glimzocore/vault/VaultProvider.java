package me.pikashrey.glimzocore.vault;

import me.pikashrey.glimzocore.GlimzoCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;

public class VaultProvider {

    private final GlimzoCore plugin;

    public VaultProvider(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        // Register Vault Chat (rank prefix/suffix)
        new VaultChatProvider(plugin).register();

        // Register Vault Economy (coin bridge)
        new VaultEconomyProvider(plugin).register();
    }
}

