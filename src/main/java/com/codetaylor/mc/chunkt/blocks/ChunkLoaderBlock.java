package com.codetaylor.mc.chunkt.blocks;

import com.codetaylor.mc.chunkt.ChunktMod;
import com.codetaylor.mc.chunkt.data.ChunkLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;

public class ChunkLoaderBlock
    extends Block {

  public static final String NAME = "chunk_loader";

  public ChunkLoaderBlock() {

    super(Material.IRON);
    this.setCreativeTab(CreativeTabs.MISC);
    this.setRegistryName(new ResourceLocation(ChunktMod.MODID, ChunkLoaderBlock.NAME));
    this.setTranslationKey(ChunktMod.MODID + "." + NAME);
  }

  @Override
  public int getHarvestLevel(@Nonnull IBlockState state) {

    return 0;
  }

  @Nonnull
  @Override
  public CreativeTabs getCreativeTab() {

    return CreativeTabs.MISC;
  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

    if (!world.isRemote && placer instanceof EntityPlayer) {
      WorldServer worldServer = (WorldServer) world;
      ChunkLoader.getInstance().add(worldServer, pos, (EntityPlayer) placer);
    }
  }

  @Override
  public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {

    if (!world.isRemote) {
      WorldServer worldServer = (WorldServer) world;
      ChunkLoader.getInstance().remove(worldServer, pos);
    }
  }
}