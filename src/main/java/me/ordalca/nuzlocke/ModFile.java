package me.ordalca.nuzlocke;

import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBall;
import com.pixelmonmod.pixelmon.items.PokeBallPart;
import me.ordalca.nuzlocke.networking.NuzlockeNetwork;
import me.ordalca.nuzlocke.commands.NuzlockeCommand;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Optional;


@Mod(ModFile.MOD_ID)
@Mod.EventBusSubscriber(modid = ModFile.MOD_ID)

public class ModFile {
    public static boolean debug = false;
    public static final String raidBiome = "raid";
    public static final String MOD_ID = "nuzlocke";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ModFile() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Loaded Nuzlocke mod");
        NuzlockeNetwork.init();
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        LOGGER.info("Loaded commands");
        new NuzlockeCommand(event.getDispatcher());
    }

    public static CompoundNBT mapToNBT(HashMap<String, String> map) {
        CompoundNBT blocked = new CompoundNBT();
        for (String key : map.keySet()) {
            blocked.putString(key, map.get(key));
        }
        return blocked;
    }
    public static HashMap<String, String> nbtToMap(CompoundNBT nbt) {
        HashMap<String, String> map = new HashMap<>();
        for (String key : nbt.getAllKeys()) {
            map.put(key, nbt.getString(key));
        }
        return map;
    }
    public static boolean stackHasMasterBall(ItemStack stack) {
        Optional<PokeBall> pokeball = PokeBallPart.getPokeBall(stack);
        return (pokeball.isPresent() && pokeball.get().isGuaranteedCatch());
    }

}
