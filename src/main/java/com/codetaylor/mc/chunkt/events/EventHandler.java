package com.codetaylor.mc.chunkt.events;

import com.codetaylor.mc.chunkt.ChunktMod;
import com.codetaylor.mc.chunkt.data.ChunkLoaderData;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
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
}
