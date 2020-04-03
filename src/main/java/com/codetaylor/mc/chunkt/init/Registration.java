package com.codetaylor.mc.chunkt.init;

import com.codetaylor.mc.chunkt.ChunktMod;
import com.codetaylor.mc.chunkt.blocks.ChunkLoaderBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {

  private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, ChunktMod.MODID);
  private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, ChunktMod.MODID);

  public static void init() {

    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    BLOCKS.register(modEventBus);
    ITEMS.register(modEventBus);
  }

  public static final RegistryObject<ChunkLoaderBlock> CHUNK_LOADER_BLOCK = BLOCKS.register(ChunkLoaderBlock.NAME, ChunkLoaderBlock::new);
  public static final RegistryObject<Item> CHUNK_LOADER_ITEM = ITEMS.register(ChunkLoaderBlock.NAME, () -> new BlockItem(CHUNK_LOADER_BLOCK.get(), new Item.Properties().group(ItemGroup.MISC)));
}
