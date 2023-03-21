package me.ordalca.nuzlocke.client;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfig;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientBagHandler {
    @SubscribeEvent
    public void blockBannedItems(PlayerInteractEvent.RightClickItem itemEvent) {
        PlayerEntity player = itemEvent.getPlayer();
        if (player == null || !NuzlockeClientPlayerData.isNuzlockeEnabled()) {
            return;
        }

        if (NuzlockeConfigProxy.getNuzlocke().pokemonFaintingPenalty() == NuzlockeConfig.FaintResult.DEAD) {
            Item item = itemEvent.getItemStack().getItem();
            if (item.equals(PixelmonItems.revive) || item.equals(PixelmonItems.max_revive) || item.equals(PixelmonItems.revival_herb) || item.equals(PixelmonItems.sacredash)) {
                itemEvent.setCanceled(true);
            }
        }

        if (NuzlockeConfigProxy.getNuzlocke().preventMasterBallUse() && ModFile.stackHasMasterBall(itemEvent.getItemStack())) {
            itemEvent.setCanceled(true);
        }
    }
}
