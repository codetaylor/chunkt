package com.codetaylor.mc.chunkt.data;

import com.codetaylor.mc.chunkt.ChunktMod;
import com.codetaylor.mc.chunkt.blocks.ChunkLoaderBlock;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ChunkLoader
    implements ForgeChunkManager.PlayerOrderedLoadingCallback {

  private static MethodHandle nbtTagLongArray$dataGetter;

  static {

    try {

      nbtTagLongArray$dataGetter = MethodHandles.lookup().unreflectGetter(
          /*
          MC 1.12: net/minecraft/nbt/NBTTagLongArray.data
          Name: b => field_193587_b => data
          Comment: None
          Side: BOTH
          AT: public net.minecraft.nbt.NBTTagLongArray field_193587_b # data
           */
          ObfuscationReflectionHelper.findField(NBTTagLongArray.class, "field_193587_b")
      );

    } catch (IllegalAccessException e) {
      ChunktMod.LOGGER.error("", e);
    }
  }

  private static ChunkLoader INSTANCE;

  public static ChunkLoader getInstance() {

    if (INSTANCE == null) {
      INSTANCE = new ChunkLoader();
    }

    return INSTANCE;
  }

  private Int2ObjectMap<Long2IntMap> loadersPerChunkCount;
  private Int2ObjectMap<LongSet> loaderPositions;
  private Int2ObjectMap<Long2ObjectMap<ForgeChunkManager.Ticket>> chunkTickets;

  private ChunkLoader() {

    this.loadersPerChunkCount = new Int2ObjectOpenHashMap<>();
    this.loadersPerChunkCount.defaultReturnValue(null);

    this.loaderPositions = new Int2ObjectOpenHashMap<>();
    this.loaderPositions.defaultReturnValue(null);

    this.chunkTickets = new Int2ObjectOpenHashMap<>();
    this.chunkTickets.defaultReturnValue(null);
  }

  private Long2IntMap getLoadersPerChunkCount(int dimensionId) {

    Long2IntMap map = this.loadersPerChunkCount.get(dimensionId);

    if (map == null) {
      map = new Long2IntOpenHashMap();
      map.defaultReturnValue(0);
      this.loadersPerChunkCount.put(dimensionId, map);
    }

    return map;
  }

  private LongSet getLoaderPositions(int dimensionId) {

    LongSet set = this.loaderPositions.get(dimensionId);

    if (set == null) {
      set = new LongOpenHashSet();
      this.loaderPositions.put(dimensionId, set);
    }

    return set;
  }

  private Long2ObjectMap<ForgeChunkManager.Ticket> getChunkTickets(int dimensionId) {

    Long2ObjectMap<ForgeChunkManager.Ticket> map = this.chunkTickets.get(dimensionId);

    if (map == null) {
      map = new Long2ObjectOpenHashMap<>();
      map.defaultReturnValue(null);
      this.chunkTickets.put(dimensionId, map);
    }

    return map;
  }

  /**
   * @return false if the chunk can't be loaded due to Forge limit
   */
  public boolean add(WorldServer world, BlockPos pos, EntityPlayer player) {

    long posL = pos.toLong();
    int dimensionId = world.provider.getDimensionType().getId();
    LongSet loaderPositions = this.getLoaderPositions(dimensionId);
    Long2IntMap loadersPerChunkCount = this.getLoadersPerChunkCount(dimensionId);
    Long2ObjectMap<ForgeChunkManager.Ticket> ticketsByChunk = this.getChunkTickets(dimensionId);

    if (!loaderPositions.contains(posL)) {

      int chunkX = pos.getX() >> 4;
      int chunkZ = pos.getZ() >> 4;
      long chunkL = ChunkPos.asLong(chunkX, chunkZ);
      int count = loadersPerChunkCount.get(chunkL);

      if (count == 0) {
        ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestPlayerTicket(ChunktMod.INSTANCE, player.getName(), world, ForgeChunkManager.Type.NORMAL);

        if (ticket == null) {
          return false;
        }

        ForgeChunkManager.forceChunk(ticket, new ChunkPos(chunkX, chunkZ));
        ticketsByChunk.put(chunkL, ticket);
        ChunktMod.LOGGER.info("Loaded chunk @ [{},{}] in dimension [{}]", chunkX, chunkZ, dimensionId);
      }

      loadersPerChunkCount.put(chunkL, ++count);
      loaderPositions.add(pos.toLong());

      this.updateTicketBlockPositions(ticketsByChunk.get(chunkL), loaderPositions);
    }

    return true;
  }

  public void remove(WorldServer world, BlockPos pos) {

    int dimensionId = world.provider.getDimensionType().getId();
    LongSet loaderPositions = this.getLoaderPositions(dimensionId);
    Long2IntMap loadersPerChunkCount = this.getLoadersPerChunkCount(dimensionId);
    Long2ObjectMap<ForgeChunkManager.Ticket> ticketsByChunk = this.getChunkTickets(dimensionId);

    if (loaderPositions.remove(pos.toLong())) {
      int chunkX = pos.getX() >> 4;
      int chunkZ = pos.getZ() >> 4;
      long chunkL = ChunkPos.asLong(chunkX, chunkZ);
      int count = loadersPerChunkCount.get(chunkL);

      if (--count <= 0) {
        ForgeChunkManager.Ticket ticket = ticketsByChunk.remove(chunkL);

        if (ticket != null) {
          ForgeChunkManager.unforceChunk(ticket, new ChunkPos(chunkX, chunkZ));
          loadersPerChunkCount.remove(chunkL);
          ChunktMod.LOGGER.info("Unloaded chunk @ [{},{}] in [{}]", chunkX, chunkZ, dimensionId);
        }

      } else {
        loadersPerChunkCount.put(chunkL, count);
        this.updateTicketBlockPositions(ticketsByChunk.get(chunkL), loaderPositions);
      }
    }
  }

  private void updateTicketBlockPositions(ForgeChunkManager.Ticket ticket, LongSet loaderPositions) {

    if (ticket != null) {
      int index = 0;
      long[] arr = new long[loaderPositions.size()];

      for (LongIterator it = loaderPositions.iterator(); it.hasNext(); ) {
        arr[index] = it.nextLong();
        index += 1;
      }

      NBTTagCompound modData = ticket.getModData();
      modData.setTag("blockPos", new NBTTagLongArray(arr));
    }
  }

  public LongSet getChunks(WorldServer world) {

    int dimensionId = world.provider.getDimensionType().getId();
    return this.getLoadersPerChunkCount(dimensionId).keySet();
  }

  @Override
  public ListMultimap<String, ForgeChunkManager.Ticket> playerTicketsLoaded(ListMultimap<String, ForgeChunkManager.Ticket> tickets, World world) {

    ListMultimap<String, ForgeChunkManager.Ticket> result = ArrayListMultimap.create();

    for (final Map.Entry<String, Collection<ForgeChunkManager.Ticket>> entry : tickets.asMap().entrySet()) {

      for (ForgeChunkManager.Ticket ticket : entry.getValue()) {
        NBTTagCompound modDataTag = ticket.getModData();

        if (modDataTag.hasKey("blockPos")) {
          NBTTagLongArray blockPositions = (NBTTagLongArray) modDataTag.getTag("blockPos");

          try {
            long[] arr = (long[]) nbtTagLongArray$dataGetter.invokeExact(blockPositions);

            for (long posL : arr) {
              BlockPos blockPos = BlockPos.fromLong(posL);
              IBlockState blockState = world.getBlockState(blockPos);

              if (blockState.getBlock() instanceof ChunkLoaderBlock) {
                result.put(entry.getKey(), ticket);
                break;
              }
            }

          } catch (Throwable throwable) {
            ChunktMod.LOGGER.error("", throwable);
          }
        }
      }
    }

    return result;
  }

  @Override
  public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {

    this.chunkTickets.clear();
    this.loaderPositions.clear();
    this.loadersPerChunkCount.clear();

    if (nbtTagLongArray$dataGetter == null) {
      return;
    }

    int dimensionId = world.provider.getDimensionType().getId();
    LongSet loaderPositions = this.getLoaderPositions(dimensionId);
    Long2IntMap loadersPerChunkCount = this.getLoadersPerChunkCount(dimensionId);
    Long2ObjectMap<ForgeChunkManager.Ticket> ticketsByChunk = this.getChunkTickets(dimensionId);

    for (ForgeChunkManager.Ticket ticket : tickets) {

      NBTTagCompound modDataTag = ticket.getModData();

      if (modDataTag.hasKey("blockPos")) {
        NBTTagLongArray blockPositions = (NBTTagLongArray) modDataTag.getTag("blockPos");

        try {
          long[] arr = (long[]) nbtTagLongArray$dataGetter.invokeExact(blockPositions);

          for (long posL : arr) {
            BlockPos pos = BlockPos.fromLong(posL);
            IBlockState blockState = world.getBlockState(pos);

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            long chunkL = ChunkPos.asLong(chunkX, chunkZ);
            int count = loadersPerChunkCount.get(chunkL);

            if (blockState.getBlock() instanceof ChunkLoaderBlock) {
              loadersPerChunkCount.put(chunkL, ++count);
              loaderPositions.add(posL);
              ticketsByChunk.put(chunkL, ticket);
              ForgeChunkManager.forceChunk(ticket, new ChunkPos(chunkX, chunkZ));
            }
          }

        } catch (Throwable throwable) {
          ChunktMod.LOGGER.error("", throwable);
        }
      }
    }

    ChunktMod.LOGGER.info("Loaded [{}] chunks in dimension [{}]", loadersPerChunkCount.size(), dimensionId);
  }

}
