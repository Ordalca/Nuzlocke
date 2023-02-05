package me.ordalca.nuzlocke;

import com.pixelmonmod.pixelmon.Pixelmon;

import me.ordalca.nuzlocke.battles.BagUsageHandler;
import me.ordalca.nuzlocke.battles.AIAdapter;
import me.ordalca.nuzlocke.battles.NuzlockeClientBattleManager;
import me.ordalca.nuzlocke.captures.BiomeBlocker;
import me.ordalca.nuzlocke.captures.NuzlockePlayerData;
import me.ordalca.nuzlocke.captures.OutOfBattleCatchControl;
import me.ordalca.nuzlocke.commands.NuzlockeCommand;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.healing.FaintingController;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(ModFile.MOD_ID)
@Mod.EventBusSubscriber(modid = ModFile.MOD_ID)
public class ModFile {

    public static final String MOD_ID = "nuzlocke";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ModFile() {}

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
        NuzlockeConfigProxy.reload();

        MinecraftForge.EVENT_BUS.register(BiomeBlocker.getInstance());
        Pixelmon.EVENT_BUS.register(BiomeBlocker.getInstance());

        MinecraftForge.EVENT_BUS.register(OutOfBattleCatchControl.getInstance());
        Pixelmon.EVENT_BUS.register(OutOfBattleCatchControl.getInstance());

        MinecraftForge.EVENT_BUS.register(FaintingController.getInstance());
        Pixelmon.EVENT_BUS.register(FaintingController.getInstance());

        Pixelmon.EVENT_BUS.register(BagUsageHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(NuzlockePlayerData.class);

        Pixelmon.EVENT_BUS.register(AIAdapter.getInstance());
        new NuzlockeClientBattleManager();
    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
        // Logic for once the server has started here
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        new NuzlockeCommand(event.getDispatcher());
        //Register command logic here
        // Commands don't have to be registered here
        // However, not registering them here can lead to some hybrids/server software not recognising the commands
    }
}
