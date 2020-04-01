package com.codetaylor.mc.chunkt.blocks;

import com.codetaylor.mc.chunkt.data.ChunkLoaderData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ChunkLoaderBlock
    extends Block {

  public static final String NAME = "chunk_loader";

  public ChunkLoaderBlock() {

    super(Properties.from(Blocks.IRON_BLOCK));
  }

  @Override
  public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {

    if (!world.isRemote) {
      ServerWorld serverWorld = (ServerWorld) world;
      ChunkLoaderData data = ChunkLoaderData.get(serverWorld);
      data.add(serverWorld, pos);
    }
  }

  @Override
  public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {

    if (!world.isRemote) {
      ServerWorld serverWorld = (ServerWorld) world;
      ChunkLoaderData data = ChunkLoaderData.get(serverWorld);
      data.remove(serverWorld, pos);
    }
  }
}
