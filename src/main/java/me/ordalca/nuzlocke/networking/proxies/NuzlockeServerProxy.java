package me.ordalca.nuzlocke.networking.proxies;

import com.pixelmonmod.pixelmon.Pixelmon;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.server.NuzlockeServerPlayerData;
import me.ordalca.nuzlocke.server.battles.AIAdapter;
import me.ordalca.nuzlocke.server.battles.BagUsageHandler;
import me.ordalca.nuzlocke.server.battles.FaintingController;
import me.ordalca.nuzlocke.server.captures.BiomeBlocker;
import me.ordalca.nuzlocke.server.captures.OutOfBattleCatchControl;
import me.ordalca.nuzlocke.server.captures.RaidCaptures;

import me.ordalca.nuzlocke.server.nicknames.NicknameHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid = ModFile.MOD_ID)
public class NuzlockeServerProxy {
    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
        NuzlockeConfigProxy.reload();

        Pixelmon.EVENT_BUS.register(AIAdapter.class);
        Pixelmon.EVENT_BUS.register(BiomeBlocker.class);
        Pixelmon.EVENT_BUS.register(OutOfBattleCatchControl.class);
        Pixelmon.EVENT_BUS.register(RaidCaptures.class);

        Pixelmon.EVENT_BUS.register(FaintingController.class);
        MinecraftForge.EVENT_BUS.register(FaintingController.class);

        Pixelmon.EVENT_BUS.register(BagUsageHandler.class);
        Pixelmon.EVENT_BUS.register(NicknameHandler.class);

        MinecraftForge.EVENT_BUS.register(NuzlockeServerPlayerData.class);
        ModFile.LOGGER.debug("Loaded Server Proxy");
    }
}
