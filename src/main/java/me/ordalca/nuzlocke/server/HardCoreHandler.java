package me.ordalca.nuzlocke.server;

import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.HealerEvent;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.items.PokeBallPart;
import me.ordalca.nuzlocke.commands.NuzlockeConfig;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.networking.NuzlockeNetwork;
import me.ordalca.nuzlocke.networking.messages.client.PlayerDataSyncMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.UUID;

public class HardCoreHandler {
		@SubscribeEvent
		public static void playerTookPassiveDamage(LivingHurtEvent event) {
				if (NuzlockeConfigProxy.getNuzlocke().godModeEnabled()) {
						if (event.getEntity() instanceof PlayerEntity) {
								UUID hurtUUID = event.getEntity().getUUID();
								NuzlockeServerPlayerData playerData = (NuzlockeServerPlayerData) StorageProxy.getParty(hurtUUID).playerData;
								if (playerData.isNuzlockeEnabled()) {
										event.setCanceled(true);
								}
						}
				}
		}

		@SubscribeEvent
		public static void triggerStart(PlayerEvent.ItemCraftedEvent event) {
				if(NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
						if (NuzlockeConfigProxy.getNuzlocke().getPermissionMethod() == NuzlockeConfig.NuzlockeStartControl.BALLS) {
								if (event.getCrafting().getItem() instanceof PokeBallPart) {
										startNuzlocke((ServerPlayerEntity) event.getPlayer());
								}
						}
				}
		}
		@SubscribeEvent
		public static void triggerStart(PlayerEvent.ItemPickupEvent event) {
				if(NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
						if (NuzlockeConfigProxy.getNuzlocke().getPermissionMethod() == NuzlockeConfig.NuzlockeStartControl.BALLS) {
								if (event.getStack().getItem() instanceof PokeBallPart) {
										startNuzlocke((ServerPlayerEntity) event.getPlayer());
								}
						}
				}
		}
		@SubscribeEvent
		public static void triggerStart(HealerEvent event) {
				if(NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
						if (NuzlockeConfigProxy.getNuzlocke().getPermissionMethod() == NuzlockeConfig.NuzlockeStartControl.HEALER) {
								startNuzlocke((ServerPlayerEntity) event.player);
						}
				}
		}

		public static void startNuzlocke(ServerPlayerEntity player) {
				NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)StorageProxy.getParty(player.getUUID()).playerData;
				if (!data.nuzlockeEnabled) {
						PlayerDataSyncMessage message = new PlayerDataSyncMessage(data.blockedBiomes, data.nuzlockeEnabled = true);
						NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->player), message);
						PixelmonCommandUtils.sendMessage(player, "The Nuzlocke Challenge has begun!");
				}
		}
}
