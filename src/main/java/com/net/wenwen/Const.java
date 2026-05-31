package com.net.wenwen;

import com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Project: CageBox
 * @Author: cnlimiter
 * @CreateTime: 2025/7/6 00:51
 * @Note:
 */
public class Const {
    public static final String MOD_ID = "wenwen";

    public static final Logger LOGGER = LogManager.getLogger();
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
