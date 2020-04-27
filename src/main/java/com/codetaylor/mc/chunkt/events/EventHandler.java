package com.codetaylor.mc.chunkt.events;

import com.codetaylor.mc.chunkt.ChunktMod;
import com.codetaylor.mc.chunkt.data.ChunkLoader;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Mod.EventBusSubscriber(modid = ChunktMod.MODID)
public class EventHandler {

  private static MethodHandle world$updateLCGGetter;
  private static MethodHandle world$updateLCGSetter;

  static {

    try {

      world$updateLCGGetter = MethodHandles.lookup().unreflectGetter(
          /*
          MC 1.12: net/minecraft/world/World.updateLCG
          Name: l => field_73005_l => updateLCG
          Comment: "Contains the current Linear Congruential Generator seed for block updates. Used with an A value of 3 and a C value of 0x3c6ef35f, producing a highly planar series of values ill-suited for choosing random blocks in a 16x128x16 field."
          Side: BOTH
          AT: public net.minecraft.world.World field_73005_l # updateLCG
           */
          ObfuscationReflectionHelper.findField(World.class, "field_73005_l")
      );

      world$updateLCGSetter = MethodHandles.lookup().unreflectSetter(
          /*
          MC 1.12: net/minecraft/world/World.updateLCG
          Name: l => field_73005_l => updateLCG
          Comment: "Contains the current Linear Congruential Generator seed for block updates. Used with an A value of 3 and a C value of 0x3c6ef35f, producing a highly planar series of values ill-suited for choosing random blocks in a 16x128x16 field."
          Side: BOTH
          AT: public net.minecraft.world.World field_73005_l # updateLCG
           */
          ObfuscationReflectionHelper.findField(World.class, "field_73005_l")
      );

    } catch (IllegalAccessException e) {
      ChunktMod.LOGGER.error("", e);
    }
  }

  @SubscribeEvent
  public static void on(TickEvent.WorldTickEvent event) {

    if (!(event.world instanceof WorldServer)) {
      return;
    }

    if (world$updateLCGGetter == null || world$updateLCGSetter == null) {
      return;
    }

    if (event.phase == TickEvent.Phase.END) {

      WorldServer world = (WorldServer) event.world;
      LongSet chunks = ChunkLoader.getInstance().getChunks(world);
      int tickSpeed = event.world.getGameRules().getInt("randomTickSpeed");

      ChunkPos chunkPos = null;

      if (tickSpeed > 0 && !chunks.isEmpty()) {

        try {
          ChunkProviderServer chunkProvider = world.getChunkProvider();
          List<EntityPlayer> players = world.playerEntities;

          for (long chunkL : chunks) {
            chunkPos = new ChunkPos((int) chunkL, (int) (chunkL >> 32));
            Chunk chunk = chunkProvider.loadedChunks.get(chunkL);

            if (chunk != null && EventHandler.isOutsideSpawningRadius(chunkPos, players)) {
              EventHandler.tickChunk(world, chunkPos, chunk, tickSpeed);
            }
          }

        } catch (Throwable throwable) {
          CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception ticking world in chunk: " + chunkPos);
          world.addWorldInfoToCrashReport(crashreport);
          throw new ReportedException(crashreport);
        }
      }
    }
  }

  private static boolean isOutsideSpawningRadius(ChunkPos chunkPos, List<EntityPlayer> players) {

    return players.isEmpty()
        || players.stream().noneMatch(player -> !player.isSpectator() && EventHandler.getDistanceSquaredToChunk(chunkPos, player) < 16384);
  }

  private static double getDistanceSquaredToChunk(ChunkPos chunkPos, Entity entity) {

    double x = chunkPos.x * 16 + 8;
    double z = chunkPos.z * 16 + 8;
    double dx = x - entity.posX;
    double dz = z - entity.posZ;
    return dx * dx + dz * dz;
  }

  private static void tickChunk(WorldServer world, ChunkPos chunkPos, Chunk chunk, int tickSpeed) throws Throwable {

    int xStart = chunkPos.getXStart();
    int zStart = chunkPos.getZStart();

    for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray()) {

      if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.needsRandomTick()) {

        for (int i = 0; i < tickSpeed; ++i) {
          int lcg = (int) world$updateLCGGetter.invokeExact((World) world) * 3 + 1013904223;
          world$updateLCGSetter.invokeExact((World) world, lcg);
          int r = lcg >> 2;
          int x = r & 15;
          int z = r >> 8 & 15;
          int y = r >> 16 & 15;
          IBlockState iblockstate = extendedblockstorage.get(x, y, z);
          Block block = iblockstate.getBlock();

          if (block.getTickRandomly()) {
            block.randomTick(world, new BlockPos(x + xStart, y + extendedblockstorage.getYLocation(), z + zStart), iblockstate, world.rand);
          }
        }
      }
    }
  }
}
