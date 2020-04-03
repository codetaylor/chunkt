package com.codetaylor.mc.chunkt.events;

import com.codetaylor.mc.chunkt.ChunktMod;
import com.codetaylor.mc.chunkt.data.ChunkLoaderData;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChunktMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

  @SubscribeEvent
  public static void on(WorldEvent.Load event) {

    IWorld world = event.getWorld();

    if (!world.isRemote()) {
      ServerWorld serverWorld = (ServerWorld) world;
      ((ServerWorld) world).getServer().enqueue(new TickDelayedTask(1, () -> {
        ChunkLoaderData data = ChunkLoaderData.get(serverWorld);
        data.init(serverWorld);
      }));
    }
  }

  @SubscribeEvent
  public static void on(TickEvent.WorldTickEvent event) {

    if (event.phase != TickEvent.Phase.END) {
      return;
    }

    if (event.world instanceof ServerWorld) {
      ServerWorld world = (ServerWorld) event.world;
      ChunkLoaderData data = ChunkLoaderData.get(world);
      LongSet chunks = data.getChunks();
      int tickSpeed = event.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);

      if (tickSpeed > 0) {

        for (long chunkL : chunks) {
          EventHandler.tickChunk(world, new ChunkPos(chunkL), tickSpeed);
        }
      }
    }
  }

  private static void tickChunk(ServerWorld world, ChunkPos chunkPos, int tickSpeed) {

    if (world.getChunkProvider().chunkManager.isOutsideSpawningRadius(chunkPos)) {
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
}
