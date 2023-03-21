package me.ordalca.nuzlocke.server;

import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class DamageHandler {
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
}
