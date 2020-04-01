package com.codetaylor.mc.chunkt.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class ChunkLoaderBlock
    extends Block {

  public static final String NAME = "chunk_loader";

  public ChunkLoaderBlock() {

    super(Properties.from(Blocks.IRON_BLOCK));
  }
}
