package com.example.orcapods;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(OrcaPods.MODID)
public class OrcaPods {
    public static final String MODID = "orcapods";

    public OrcaPods() {
        MinecraftForge.EVENT_BUS.register(new OrcaEvents());
    }
}
