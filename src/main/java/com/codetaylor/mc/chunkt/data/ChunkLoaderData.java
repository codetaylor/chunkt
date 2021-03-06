package com.codetaylor.mc.chunkt.data;

import com.codetaylor.mc.chunkt.ChunktMod;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;

public class ChunkLoaderData
    extends WorldSavedData {

  private static final String DATA_ID = ChunktMod.MODID + "_" + ChunkLoaderData.class.getSimpleName();

  private boolean initialized;
  private Long2IntMap loadersPerChunkCount;
  private LongSet loaderPositions;

  public ChunkLoaderData() {

    this(DATA_ID);
  }

  public ChunkLoaderData(String name) {

    super(name);
    this.loadersPerChunkCount = new Long2IntOpenHashMap();
    this.loadersPerChunkCount.defaultReturnValue(0);
    this.loaderPositions = new LongOpenHashSet();
    this.initialized = false;
  }

  public static ChunkLoaderData get(ServerWorld world) {

    return world.getSavedData().getOrCreate(ChunkLoaderData::new, DATA_ID);
  }

  public void init(ServerWorld world) {

    if (!this.initialized) {
      this.initialized = true;

      for (long chunkL : this.loadersPerChunkCount.keySet()) {
        world.forceChunk(ChunkPos.getX(chunkL), ChunkPos.getZ(chunkL), true);
      }

      ChunktMod.LOGGER.info("Loaded [{}] chunks in [{}]", this.loadersPerChunkCount.size(), world.dimension.getType().getRegistryName());
    }
  }

  public void add(ServerWorld world, BlockPos pos) {

    long posL = pos.toLong();

    if (!this.loaderPositions.contains(posL)) {
      int chunkX = pos.getX() >> 4;
      int chunkZ = pos.getZ() >> 4;
      long chunkL = ChunkPos.asLong(chunkX, chunkZ);
      int count = this.loadersPerChunkCount.get(chunkL);

      if (count == 0) {
        world.forceChunk(chunkX, chunkZ, true);
        ChunktMod.LOGGER.info("Loaded chunk @ [{},{}] in [{}]", chunkX, chunkZ, world.dimension.getType().getRegistryName());
      }

      this.loadersPerChunkCount.put(chunkL, ++count);
      this.loaderPositions.add(posL);
      this.markDirty();
    }
  }

  public void remove(ServerWorld world, BlockPos pos) {

    if (this.loaderPositions.remove(pos.toLong())) {
      int chunkX = pos.getX() >> 4;
      int chunkZ = pos.getZ() >> 4;
      long chunkL = ChunkPos.asLong(chunkX, chunkZ);
      int count = this.loadersPerChunkCount.get(chunkL);

      if (--count <= 0) {
        world.forceChunk(chunkX, chunkZ, false);
        ChunktMod.LOGGER.info("Unloaded chunk @ [{},{}] in [{}]", chunkX, chunkZ, world.dimension.getType().getRegistryName());
        this.loadersPerChunkCount.remove(chunkL);

      } else {
        this.loadersPerChunkCount.put(chunkL, count);
      }
      this.markDirty();
    }
  }

  public LongSet getChunks() {

    return this.loadersPerChunkCount.keySet();
  }

  @Override
  public void read(@Nonnull CompoundNBT tag) {

    long[] loaderPositions = tag.getLongArray("loaderPositions");

    for (long loaderPosition : loaderPositions) {
      this.loaderPositions.add(loaderPosition);
      BlockPos pos = BlockPos.fromLong(loaderPosition);
      long chunkL = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
      this.loadersPerChunkCount.put(chunkL, this.loadersPerChunkCount.get(chunkL) + 1);
    }
  }

  @Nonnull
  @Override
  public CompoundNBT write(@Nonnull CompoundNBT tag) {

    LongArrayNBT loaderPositions = new LongArrayNBT(this.loaderPositions.toArray(new long[0]));
    tag.put("loaderPositions", loaderPositions);
    return tag;
  }

}
