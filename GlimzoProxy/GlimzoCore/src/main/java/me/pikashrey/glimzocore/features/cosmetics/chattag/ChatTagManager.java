package me.pikashrey.glimzocore.features.cosmetics.chattag;

import me.pikashrey.glimzocore.GlimzoCore;
import me.pikashrey.glimzocore.features.cosmetics.PlayerCosmeticState;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class ChatTagManager {

    private final GlimzoCore          plugin;
    private final Map<String, ChatTag> registry = new LinkedHashMap<>();

    public ChatTagManager(GlimzoCore plugin) {
        this.plugin = plugin;
        loadTags();
    }

    private void loadTags() {
        registry.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("chat-tags");
        if (section == null) {
            loadDefaults();
            return;
        }

        List<ChatTag> tagList = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection tagSection = section.getConfigurationSection(key);
            if (tagSection == null) continue;

            int    order       = tagSection.getInt("order", 0);
            String tag         = tagSection.getString("tag", key);
            String description = tagSection.getString("description", "");
            String permission  = tagSection.getString("permission", null);

            tagList.add(new ChatTag(key, order, tag, description, permission));
        }

        tagList.sort(Comparator.comparingInt(ChatTag::getOrder));
        for (ChatTag t : tagList) {
            registry.put(t.getId(), t);
        }

        plugin.log("&7[ChatTags] Loaded &b" + registry.size() + " &7chat tags.");
    }

    private void loadDefaults() {
        // Fallback hardcoded tags if no config section present
        String[][] defaults = {
                {"savage",   "1",  "&c[Savage]",      "The savage tag",    null},
                {"chill",    "2",  "&b[Chill]",        "Chill vibes only",  null},
                {"goat",     "3",  "&6[GOAT]",         "Greatest of all",   "glimzo.chattag.goat"},
                {"vip",      "4",  "&e[VIP]",          "VIP member",        "glimzo.rank.vip"},
                {"legend",   "5",  "&5[Legend]",       "Legendary player",  "glimzo.rank.legend"},
                {"chief",    "6",  "&4[Chief]",        "The Chief",         "glimzo.rank.chief"},
                {"og",       "7",  "&d[OG]",           "Original Gangster", "glimzo.chattag.og"},
                {"galaxy",   "8",  "&3[Galaxy]",       "Out of this world", null},
                {"pro",      "9",  "&a[Pro]",          "A true pro",        null},
                {"grinder",  "10", "&2[Grinder]",      "Never stops",       null},
        };
        int i = 0;
        for (String[] d : defaults) {
            registry.put(d[0], new ChatTag(d[0], Integer.parseInt(d[1]), d[2], d[3], d[4]));
            i++;
        }
        plugin.log("&7[ChatTags] Loaded " + i + " default chat tags.");
    }

    public boolean selectTag(Player player, PlayerCosmeticState state, String tagId) {
        if (tagId == null) {
            state.setActiveChatTagId(null);
            return true;
        }
        ChatTag tag = registry.get(tagId);
        if (tag == null) return false;
        if (!tag.canUse(player)) return false;
        state.setActiveChatTagId(tagId);
        return true;
    }

    /**
     * Deselect / clear the player's active tag.
     */
    public void clearTag(Player player, PlayerCosmeticState state) {
        state.setActiveChatTagId(null);
    }

    public String getFormattedTag(PlayerCosmeticState state) {
        String id = state.getActiveChatTagId();
        if (id == null) return null;
        ChatTag tag = registry.get(id);
        if (tag == null) return null;
        return CC.translate(tag.getTag());
    }

    public String buildChatPrefix(String rankPrefix, String playerName, PlayerCosmeticState state) {
        // Escape % so String.format() in AsyncPlayerChatEvent doesn't throw
        playerName = playerName.replace("%", "%%");
        String tag = (state != null) ? getFormattedTag(state) : null;

        // Translate the prefix first so we work with § codes (e.g. "§4§l[CHIEF] ")
        String translatedPrefix = CC.translate(rankPrefix != null ? rankPrefix : "&7");

        // Extract the colour/format codes at the start of the translated prefix.
        // Walk past any § + code pairs (e.g. §4, §l, §c) to get the full leading format.
        // We then re-apply those same codes before the player name.
        StringBuilder leadingCodes = new StringBuilder();
        int i = 0;
        while (i + 1 < translatedPrefix.length() && translatedPrefix.charAt(i) == '§') {
            leadingCodes.append(translatedPrefix, i, i + 2);
            i += 2;
        }
        String rankColor = leadingCodes.length() > 0 ? leadingCodes.toString() : "§7";

        // Format: "<translatedPrefix><rankColor><playerName> §8» §f"
        // e.g. "§4§l[CHIEF] §4§lBabluOnTheBeach §8» §f"
        String result = translatedPrefix + rankColor + playerName + " §8» §f";

        if (tag != null && !tag.isEmpty()) {
            result = result + tag + " ";
        }
        return result; // already translated
    }

    /**
     * Build the full chat line including the message. Convenience method.
     */
    public String buildChatLine(String rankPrefix, String playerName, PlayerCosmeticState state, String message) {
        return buildChatPrefix(rankPrefix, playerName, state) + message;
    }

    public ChatTag getTag(String id)                  { return registry.get(id); }
    public Map<String, ChatTag> getRegistry()         { return new LinkedHashMap<>(registry); }

    public List<ChatTag> getAvailableTagsFor(Player player) {
        List<ChatTag> available = new ArrayList<>();
        for (ChatTag tag : registry.values()) {
            if (tag.canUse(player)) available.add(tag);
        }
        return available;
    }
}