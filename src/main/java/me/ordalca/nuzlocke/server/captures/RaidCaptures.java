package me.ordalca.nuzlocke.server.captures;

import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.networking.NuzlockeNetwork;
import me.ordalca.nuzlocke.networking.messages.client.BiomeBlockedMessage;
import me.ordalca.nuzlocke.server.NuzlockeServerPlayerData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Locale;

public class RaidCaptures {
    @SubscribeEvent
    public static void raidCapture(CaptureEvent.StartRaidCapture event) {
        NuzlockeServerPlayerData data = (NuzlockeServerPlayerData) StorageProxy.getParty(event.getPlayer()).playerData;
        if (!data.isNuzlockeEnabled())
            return;

        String uuid = event.getRaidPokemon().getUUID().toString();

        boolean blockCatch = false;
        String reason = "";
        if (NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted() &&
                data.isBiomeBlocked(uuid, ModFile.raidBiome)) {
            blockCatch = true;
            reason = "Only the first attempted raid capture is permitted.";
        } else if (NuzlockeConfigProxy.getNuzlocke().preventMasterBallUseInRaids() &&
                event.getCaptureValues().getBallBonus() >= 255) {
            blockCatch = true;
            reason = "No using master balls in raids.";
        }

        if (blockCatch) {
            if (!shinyCausePermitsCatch(event.getRaidPokemon())) {
                ChatHandler.sendChat(event.getPlayer(), "Catching attempt blocked by Nuzlocke rules: "+reason);
                event.setCanceled(true);
            }
        }
    }

    private static boolean shinyCausePermitsCatch(Pokemon pokemon) {
        return (NuzlockeConfigProxy.getNuzlocke().isShinyClauseActive() &&
                pokemon.getPalette().getName().toLowerCase(Locale.ENGLISH).contains("shiny"));
    }
    @SubscribeEvent
    public static void caughtRaid(CaptureEvent.SuccessfulRaidCapture event) {
        NuzlockeServerPlayerData playerData = (NuzlockeServerPlayerData) StorageProxy.getParty(event.getPlayer()).playerData;

        if (!playerData.isNuzlockeEnabled())
            return;

        String raidID = event.getRaidPokemon().getUUID().toString();
        if (playerData.blockBiomeForPokemon(raidID, ModFile.raidBiome)) {
            BiomeBlockedMessage message = new BiomeBlockedMessage(ModFile.raidBiome, raidID);
            NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), message);
        }
    }
    @SubscribeEvent
    public static void failedRaid(CaptureEvent.FailedRaidCapture event) {
        NuzlockeServerPlayerData playerData = (NuzlockeServerPlayerData) StorageProxy.getParty(event.getPlayer()).playerData;

        if (!playerData.isNuzlockeEnabled())
            return;

        String raidID = event.getRaidPokemon().getUUID().toString();
        if (playerData.blockBiomeForPokemon(raidID, ModFile.raidBiome)) {
            BiomeBlockedMessage message = new BiomeBlockedMessage(ModFile.raidBiome, raidID);
            NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), message);
        }
    }
}
