package me.pikashrey.glimzocore.api.clan;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ClanLevel {

    /** The full level ladder, indexed 1-10. Index 0 is unused (level starts at 1). */
    public static final ClanLevel[] LEVELS = buildLevels();

    private final int                  level;
    private final long                 xpRequired;   // total clan XP to reach this level
    private final Map<ClanPerk, Double> perkValues;   // perk -> value at this level

    public ClanLevel(int level, long xpRequired, Map<ClanPerk, Double> perkValues) {
        this.level       = level;
        this.xpRequired  = xpRequired;
        this.perkValues  = Collections.unmodifiableMap(new EnumMap<>(perkValues));
    }

    /** Returns the bonus value for a perk at this level. 0.0 if perk not active. */
    public double getPerkValue(ClanPerk perk) {
        return perkValues.getOrDefault(perk, 0.0);
    }

    /** Whether a perk is unlocked at this level. */
    public boolean hasPerk(ClanPerk perk) {
        return perkValues.containsKey(perk);
    }

    public int                   getLevel()      { return level; }
    public long                  getXpRequired() { return xpRequired; }
    public Map<ClanPerk, Double> getPerkValues() { return perkValues; }

    /** Returns max clan members at this level: base 10 + capacity bonuses. */
    public int getMaxMembers() {
        int base = 10;
        Double bonus = perkValues.getOrDefault(ClanPerk.INCREASED_CAPACITY, 0.0);
        return base + bonus.intValue();
    }

    /** Returns the ClanLevel object for a given level number (1-10). */
    public static ClanLevel forLevel(int level) {
        if (level < 1) return LEVELS[1];
        if (level >= LEVELS.length) return LEVELS[LEVELS.length - 1];
        return LEVELS[level];
    }

    /** Returns the max level available. */
    public static int maxLevel() {
        return LEVELS.length - 1;
    }

    private static ClanLevel[] buildLevels() {
        // Index 0 is a placeholder; actual levels are 1-10
        ClanLevel[] levels = new ClanLevel[11];

        levels[0]  = null;

        // Level 1 - base, no perks yet
        levels[1]  = build(1,       0, new double[0], new ClanPerk[0]);

        // Level 2 - custom tag + small XP boost
        levels[2]  = build(2,    5000,
                new double[]{0.0},
                new ClanPerk[]{ClanPerk.CUSTOM_TAG});

        // Level 3 - XP boost 5%
        levels[3]  = build(3,   15000,
                new double[]{0.05},
                new ClanPerk[]{ClanPerk.XP_BOOST});

        // Level 4 - clan banner + coin boost 5%
        levels[4]  = build(4,   35000,
                new double[]{0.0, 0.05},
                new ClanPerk[]{ClanPerk.CLAN_BANNER, ClanPerk.COIN_BOOST});

        // Level 5 - XP boost 10%
        levels[5]  = build(5,   70000,
                new double[]{0.10, 0.05},
                new ClanPerk[]{ClanPerk.XP_BOOST, ClanPerk.COIN_BOOST});

        // Level 6 - gem boost 5%
        levels[6]  = build(6,  120000,
                new double[]{0.10, 0.05, 0.05},
                new ClanPerk[]{ClanPerk.XP_BOOST, ClanPerk.COIN_BOOST, ClanPerk.GEM_BOOST});

        // Level 7 - extended MOTD
        levels[7]  = build(7,  200000,
                new double[]{0.10, 0.05, 0.05, 0.0},
                new ClanPerk[]{ClanPerk.XP_BOOST, ClanPerk.COIN_BOOST, ClanPerk.GEM_BOOST,
                                ClanPerk.EXPANDED_MOTD});

        // Level 8 - XP boost 15%, coin boost 10%
        levels[8]  = build(8,  320000,
                new double[]{0.15, 0.10, 0.05, 0.0},
                new ClanPerk[]{ClanPerk.XP_BOOST, ClanPerk.COIN_BOOST, ClanPerk.GEM_BOOST,
                                ClanPerk.EXPANDED_MOTD});

        // Level 9 - increased capacity +5 slots
        levels[9]  = build(9,  500000,
                new double[]{0.15, 0.10, 0.10, 0.0, 5.0},
                new ClanPerk[]{ClanPerk.XP_BOOST, ClanPerk.COIN_BOOST, ClanPerk.GEM_BOOST,
                                ClanPerk.EXPANDED_MOTD, ClanPerk.INCREASED_CAPACITY});

        // Level 10 - max: XP 20%, coin 15%, gem 15%, capacity +10
        levels[10] = build(10, 800000,
                new double[]{0.20, 0.15, 0.15, 0.0, 10.0},
                new ClanPerk[]{ClanPerk.XP_BOOST, ClanPerk.COIN_BOOST, ClanPerk.GEM_BOOST,
                                ClanPerk.EXPANDED_MOTD, ClanPerk.INCREASED_CAPACITY});

        return levels;
    }

    private static ClanLevel build(int level, long xp, double[] values, ClanPerk[] perks) {
        Map<ClanPerk, Double> map = new EnumMap<>(ClanPerk.class);
        for (int i = 0; i < perks.length; i++) {
            map.put(perks[i], values[i]);
        }
        return new ClanLevel(level, xp, map);
    }
}

