package com.codetaylor.mc.chunkt;

import com.codetaylor.mc.chunkt.init.Registration;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChunktMod.MODID)
public class ChunktMod {

  public static final String MODID = "chunkt";
  public static final boolean DEBUG = true;
  public static final Logger LOGGER = LogManager.getLogger();

  public ChunktMod() {

    Registration.init();
  }
}
