package com.codetaylor.mc.chunkt.events;

import com.codetaylor.mc.chunkt.ChunktMod;
import com.codetaylor.mc.chunkt.data.ChunkLoaderData;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ChunktMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

  @SubscribeEvent
  public static void on(TickEvent.WorldTickEvent event) {

    if (!(event.world instanceof ServerWorld)) {
      return;
    }

    if (event.phase == TickEvent.Phase.START) {
      ServerWorld world = (ServerWorld) event.world;
      ChunkLoaderData data = ChunkLoaderData.get(world);
      data.init(world);

    } else if (event.phase == TickEvent.Phase.END) {

      ServerWorld world = (ServerWorld) event.world;
      ChunkLoaderData data = ChunkLoaderData.get(world);
      LongSet chunks = data.getChunks();
      int tickSpeed = event.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);

      ChunkPos chunkPos = null;

      if (tickSpeed > 0 && !chunks.isEmpty()) {

        try {
          ServerChunkProvider chunkProvider = world.getChunkProvider();
          List<ServerPlayerEntity> players = world.getPlayers();

          for (long chunkL : chunks) {
            chunkPos = new ChunkPos(chunkL);

            if (chunkProvider.isChunkLoaded(chunkPos) && EventHandler.isOutsideSpawningRadius(chunkPos, players)) {
              EventHandler.tickChunk(world, chunkPos, tickSpeed);
            }
          }

        } catch (Throwable throwable) {
          CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception ticking world in chunk " + chunkPos);
          world.fillCrashReport(crashreport);
          throw new ReportedException(crashreport);
        }
      }
    }
  }

  private static boolean isOutsideSpawningRadius(ChunkPos chunkPos, List<ServerPlayerEntity> players) {

    return players.isEmpty()
        || players.stream().noneMatch(player -> !player.isSpectator() && EventHandler.getDistanceSquaredToChunk(chunkPos, player) < 16384);
  }

  private static double getDistanceSquaredToChunk(ChunkPos chunkPos, Entity entity) {

    double x = chunkPos.x * 16 + 8;
    double z = chunkPos.z * 16 + 8;
    double dx = x - entity.getPosX();
    double dz = z - entity.getPosZ();
    return dx * dx + dz * dz;
  }

  private static void tickChunk(ServerWorld world, ChunkPos chunkPos, int tickSpeed) {

    int xStart = chunkPos.getXStart();
    int zStart = chunkPos.getZStart();

    for (ChunkSection chunksection : world.getChunk(chunkPos.x, chunkPos.z).getSections()) {

      if (chunksection != Chunk.EMPTY_SECTION && chunksection.needsRandomTickAny()) {
        int yStart = chunksection.getYLocation();

        for (int i = 0; i < tickSpeed; i++) {
          BlockPos blockPos = world.getBlockRandomPos(xStart, yStart, zStart, 15);
          BlockState blockState = chunksection.getBlockState(blockPos.getX() - xStart, blockPos.getY() - yStart, blockPos.getZ() - zStart);

          if (blockState.ticksRandomly()) {
            blockState.randomTick(world, blockPos, world.rand);
          }

          IFluidState fluidState = blockState.getFluidState();

          if (fluidState.ticksRandomly()) {
            fluidState.randomTick(world, blockPos, world.rand);
          }
        }
      }
    }
  }
}
