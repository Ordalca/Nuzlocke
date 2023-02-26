package me.ordalca.nuzlocke.networking.proxies;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.client.ClientBagHandler;
import me.ordalca.nuzlocke.client.ClientNicknameHandler;
import me.ordalca.nuzlocke.client.CompassInteractHandler;
import me.ordalca.nuzlocke.client.NuzlockeClientBattleManager;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModFile.MOD_ID, value = Dist.CLIENT)
public class NuzlockeClientProxy {
    @SubscribeEvent
    public static void onPlayerJoining(ClientPlayerNetworkEvent.LoggedInEvent event) {
        NuzlockeConfigProxy.reload();

        MinecraftForge.EVENT_BUS.register(ClientBagHandler.class);
        MinecraftForge.EVENT_BUS.register(ClientNicknameHandler.class);
        MinecraftForge.EVENT_BUS.register(CompassInteractHandler.class);

        new NuzlockeClientBattleManager();
        ModFile.LOGGER.debug("Loaded Client Proxy");
    }
}
