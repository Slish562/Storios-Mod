package com.io.storiosmod.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public class ThirstCompat {
    public static final String MOD_ID = "thirst";
    private static boolean initialized = false;
    private static Method drinkMethod;

    private static void init() {
        if (initialized)
            return;
        initialized = true;

        if (ModList.get().isLoaded(MOD_ID)) {
            try {

                Class<?> thirstHelperClass = null;
                try {
                    thirstHelperClass = Class.forName("com.lovetropics.thirst.common.utils.ThirstHelper");
                } catch (ClassNotFoundException e) {
                    try {
                        thirstHelperClass = Class.forName("com.lovetropics.thirst.utils.ThirstHelper");
                    } catch (ClassNotFoundException ignored) {
                    }
                }

                if (thirstHelperClass != null) {

                    for (Method m : thirstHelperClass.getMethods()) {
                        if (m.getName().equals("drink") && m.getParameterCount() == 3) {

                            drinkMethod = m;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean isThirstLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void drink(Player player, int purity, int amount, float hydration) {
        if (!isThirstLoaded() || player.level().isClientSide)
            return;

        init();

        if (drinkMethod != null) {
            try {

                drinkMethod.invoke(null, player, amount, hydration);
            } catch (Exception e) {
            }
        }
    }
}
