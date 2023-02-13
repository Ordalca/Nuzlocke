package me.ordalca.nuzlocke.captures;

import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.raids.StartRaidEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.raids.RaidData;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;

public class RaidCaptures {
    private static final String raidBiome = "raid";

    private static RaidCaptures handler = null;
    RaidCaptures() {}
    public static RaidCaptures getInstance() {
        if (handler == null) {
            handler = new RaidCaptures();
        }
        return handler;
    }
    @SubscribeEvent
    public void raidCapture(CaptureEvent.StartRaidCapture event) {
        if (!NuzlockePlayerData.isNuzlockeEnabled(event.getPlayer().getUUID()))
            return;

        NuzlockePlayerData playerData = (NuzlockePlayerData) StorageProxy.getParty(event.getPlayer().getUUID()).playerData;
        String uuid = event.getRaidPokemon().getUUID().toString();

        boolean blockCatch = false;
        if (NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted() && playerData.isBiomeBlocked(uuid, raidBiome)) {
            blockCatch = true;
        } else if (NuzlockeConfigProxy.getNuzlocke().preventMasterBallUseInRaids() && event.getCaptureValues().getBallBonus() >= 255) {
            blockCatch = true;
        }

        if (blockCatch) {
            if (!shinyCausePermitsCatch(event.getRaidPokemon())) {
                PixelmonCommandUtils.sendMessage(event.getPlayer(), "Catching attempt blocked by Nuzlocke rules.");
                event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public void caughtRaid(CaptureEvent.SuccessfulRaidCapture event) {
        if (!NuzlockePlayerData.isNuzlockeEnabled(event.getPlayer().getUUID()))
            return;
        NuzlockePlayerData playerData = (NuzlockePlayerData) StorageProxy.getParty(event.getPlayer().getUUID()).playerData;
        playerData.blockBiomeForPokemon(event.getRaidPokemon().getUUID().toString(), raidBiome);
    }
    @SubscribeEvent
    public void failedRaid(CaptureEvent.FailedRaidCapture event) {
        if (!NuzlockePlayerData.isNuzlockeEnabled(event.getPlayer().getUUID()))
            return;
        NuzlockePlayerData playerData = (NuzlockePlayerData) StorageProxy.getParty(event.getPlayer().getUUID()).playerData;
        playerData.blockBiomeForPokemon(event.getRaidPokemon().getUUID().toString(), raidBiome);
    }

    private boolean shinyCausePermitsCatch(Pokemon pokemon) {
        return (NuzlockeConfigProxy.getNuzlocke().isShinyClauseActive() &&
                pokemon.getPalette().getName().toLowerCase(Locale.ENGLISH).contains("shiny"));
    }

}
