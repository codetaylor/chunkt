package com.codetaylor.mc.chunkt.util;

import com.codetaylor.mc.chunkt.ChunktMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class DebugUtil {

  public static void debug(String message, ServerWorld world) {

    if (ChunktMod.DEBUG) {
      ChunktMod.LOGGER.debug(message);

      for (PlayerEntity p : world.getServer().getPlayerList().getPlayers()) {
        p.sendMessage(new TranslationTextComponent(message));
      }
    }
  }
}
