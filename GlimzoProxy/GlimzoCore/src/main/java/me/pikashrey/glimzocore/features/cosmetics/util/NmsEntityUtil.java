package me.pikashrey.glimzocore.features.cosmetics.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Collection;

public final class NmsEntityUtil {

    private NmsEntityUtil() {}

    public static void suppressAI(Entity mob) {
        if (!(mob instanceof LivingEntity)) return;
        LivingEntity living = (LivingEntity) mob;
        living.setRemoveWhenFarAway(false);
        living.setCustomNameVisible(false);

        try {
            Object nmsEntity = mob.getClass().getMethod("getHandle").invoke(mob);

            // Set entity flags that disable physics/damage (1.8.8 NMS field names)
            setField(nmsEntity, "noclip",        true);
            setField(nmsEntity, "silent",        true);
            setField(nmsEntity, "invulnerable",  true);

            // Clear PathfinderGoalSelector goals so mob has no AI
            clearGoalSelector(nmsEntity, "goalSelector");
            clearGoalSelector(nmsEntity, "targetSelector");

        } catch (Exception ignored) {
            // Reflection failed - entity still spawns but may wander
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = findField(target.getClass(), fieldName);
            if (f != null) {
                f.setAccessible(true);
                f.set(target, value);
            }
        } catch (Exception ignored) {}
    }

    private static void clearGoalSelector(Object nmsEntity, String selectorField) {
        try {
            Field sf = findField(nmsEntity.getClass(), selectorField);
            if (sf == null) return;
            sf.setAccessible(true);
            Object selector = sf.get(nmsEntity);

            // PathfinderGoalSelector in 1.8.8 stores goals in fields named "b" and "c"
            for (String listField : new String[]{"b", "c"}) {
                try {
                    Field gf = findField(selector.getClass(), listField);
                    if (gf != null) {
                        gf.setAccessible(true);
                        Object goals = gf.get(selector);
                        if (goals instanceof Collection) {
                            ((Collection<?>) goals).clear();
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private static Field findField(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
