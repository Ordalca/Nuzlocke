package me.ordalca.nuzlocke.client;

import com.pixelmonmod.pixelmon.comm.ChatHandler;
import me.ordalca.nuzlocke.ModFile;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;

public class CompassInteractHandler {
    @SubscribeEvent
    public static void checkBlockedBiomes(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getItemStack().getItem() instanceof CompassItem) {

            HashMap<String, String> blocked = NuzlockeClientPlayerData.blockedBiomes;
            if (blocked.size() > 0) {
                ChatHandler.sendChat(event.getEntityLiving(), "Blocked biomes: " + blocked.keySet());
            } else {
                ChatHandler.sendChat(event.getEntityLiving(), "No Blocked biomes");
            }
        }
    }
    @SubscribeEvent
    public static void checkBlockedBiomesDebug(PlayerInteractEvent.RightClickItem event) {
        if (ModFile.debug) {
            if (event.getPlayer() instanceof ClientPlayerEntity) {
                if (event.getItemStack().getItem() instanceof CompassItem) {
                    HashMap<String, String> blocked = NuzlockeClientPlayerData.blockedBiomes;
                    if (blocked.size() > 0) {
                        ChatHandler.sendChat(event.getEntityLiving(), "Blocked biomes: " + blocked);
                    } else {
                        ChatHandler.sendChat(event.getEntityLiving(), "No Blocked biomes");
                    }
                }
            }
        }
    }
}
