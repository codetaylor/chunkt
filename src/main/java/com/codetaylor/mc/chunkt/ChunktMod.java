package com.codetaylor.mc.chunkt;

import com.codetaylor.mc.chunkt.init.Registration;
import net.minecraftforge.fml.common.Mod;

@Mod(ChunktMod.MODID)
public class ChunktMod {

  public static final String MODID = "chunkt";

  public ChunktMod() {

    Registration.init();
  }
}
