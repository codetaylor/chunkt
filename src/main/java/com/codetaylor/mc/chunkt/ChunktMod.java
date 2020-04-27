package com.codetaylor.mc.chunkt;

import com.codetaylor.mc.chunkt.blocks.ChunkLoaderBlock;
import com.codetaylor.mc.chunkt.data.ChunkLoader;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(modid = ChunktMod.MODID)
public class ChunktMod {

  public static final String MODID = "chunkt";
  public static final Logger LOGGER = LogManager.getLogger();

  @Mod.Instance
  public static ChunktMod INSTANCE;

  @Mod.EventHandler
  public static void on(FMLPreInitializationEvent event) {

    ForgeChunkManager.setForcedChunkLoadingCallback(ChunktMod.INSTANCE, ChunkLoader.getInstance());
  }

  @SubscribeEvent
  public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {

    event.getRegistry().register(new ChunkLoaderBlock());
  }

  @SubscribeEvent
  public static void onRegisterItems(RegistryEvent.Register<Item> event) {

    event.getRegistry().register(new ItemBlock(Blocks.CHUNK_LOADER)
        .setRegistryName(Blocks.CHUNK_LOADER.getRegistryName())
        .setTranslationKey(Blocks.CHUNK_LOADER.getTranslationKey()));
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public static void onRegisterModels(ModelRegistryEvent event) {

    ModelResourceLocation modelResourceLocation = new ModelResourceLocation(new ResourceLocation(ChunktMod.MODID, ChunkLoaderBlock.NAME), "normal");
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Blocks.CHUNK_LOADER), 0, modelResourceLocation);
  }

  @GameRegistry.ObjectHolder(ChunktMod.MODID)
  public static class Blocks {

    @GameRegistry.ObjectHolder(ChunkLoaderBlock.NAME)
    public static final ChunkLoaderBlock CHUNK_LOADER;

    static {
      CHUNK_LOADER = null;
    }
  }
}
