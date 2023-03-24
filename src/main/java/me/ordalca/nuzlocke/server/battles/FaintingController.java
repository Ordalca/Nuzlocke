package me.ordalca.nuzlocke.server.battles;

import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.raids.EndRaidEvent;
import com.pixelmonmod.pixelmon.api.events.raids.StartRaidEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.raids.RaidData.RaidPlayer;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.*;

import me.ordalca.nuzlocke.server.NuzlockeServerPlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class FaintingController {

    @SubscribeEvent
    public static void sendOut(PokemonSendOutEvent.Pre event) {
        if (event.getPlayer() == null || ! NuzlockeServerPlayerData.isNuzlockeEnabled(event.getPlayer().getUUID())) {
            return;
        }

        Pokemon pokemon = event.getPokemon();
        if (pokemon.getPersistentData().getBoolean("dead")) {
            pokemon.setHealth(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void outOfBattleFaint(PixelmonFaintEvent.Post event) {
        if (event.getPlayer() == null || !NuzlockeServerPlayerData.isNuzlockeEnabled(event.getPlayer().getUUID())) {
            return;
        }
        PlayerPartyStorage storage = StorageProxy.getParty(event.getPlayer().getUUID());
        NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)storage.playerData;

        Pokemon pokemon = event.getPokemon();
        if (!data.inBattle) {
            pokemonFainted(storage,storage.getSlot(pokemon));
        }
    }

    @SubscribeEvent
    public static void onRaidStart(StartRaidEvent event) {
        for (RaidPlayer player : event.getRaid().getPlayers()) {
            ServerPlayerEntity spe = player.playerEntity;
            if (spe!=null) {
                PlayerPartyStorage storage = StorageProxy.getParty(player.player);
                NuzlockeServerPlayerData data = (NuzlockeServerPlayerData) storage.playerData;
                data.inRaid = true;
            }
        }
    }

    @SubscribeEvent
    public static void onRaidEnd(EndRaidEvent event) {
        for (RaidPlayer player : event.getRaid().getPlayers()) {
            ServerPlayerEntity spe = player.playerEntity;
            if (spe!=null) {
                PlayerPartyStorage storage = StorageProxy.getParty(player.player);
                NuzlockeServerPlayerData data = (NuzlockeServerPlayerData) storage.playerData;
                data.inRaid = false;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBattleStart(BattleStartedEvent event) {
        if (event.bc.containsParticipantType(PlayerParticipant.class)) {
            for (PlayerParticipant player : event.bc.getPlayers()) {
                PlayerPartyStorage storage = StorageProxy.getParty(player.player);
                NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)storage.playerData;
                data.inBattle = true;
            }
        }
    }

    @SubscribeEvent
    public static void onBattleEnded(BattleEndEvent event) {
        if (event.getBattleController().containsParticipantType(PlayerParticipant.class)) {
            for(ServerPlayerEntity player : event.getPlayers()) {
                PlayerPartyStorage storage = StorageProxy.getParty(player);
                NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)storage.playerData;
                if (data.isNuzlockeEnabled()) {
                    data.inBattle = false;
                    if (!data.inRaid) {
                        if (NuzlockeConfigProxy.getNuzlocke().isPVPDeathEnforced() || !(event.getBattleController().isPvP())) {
                            List<Pokemon> party = storage.getTeam();
                            for (Pokemon pokemon : party) {
                                if (pokemon.isFainted() && !pokemon.getPersistentData().getBoolean("dead")) {
                                    ModFile.LOGGER.debug("Fainting "+pokemon);
                                    pokemonFainted(storage, storage.getSlot(pokemon));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void pokemonFainted(PlayerPartyStorage storage, int slot) {
        switch (NuzlockeConfigProxy.getNuzlocke().pokemonFaintingPenalty()) {
            case FAINT: break;
            case DELETE:
            {
                storage.set(slot, null);
                break;
            }
            case DEAD:
            {
                Pokemon faintedPokemon = storage.get(slot);
                if (faintedPokemon != null) {
                    faintedPokemon.getPersistentData().putBoolean("dead", true);
                }
                break;
            }
        }

        if (storage.countAblePokemon() <= 0) {
            playerWiped(storage.getPlayer());
        }
    }
    private static void playerWiped(ServerPlayerEntity player) {
        switch (NuzlockeConfigProxy.getNuzlocke().trainerWipePenalty()) {
            case DEATH:
                player.getFoodData().setFoodLevel(0);
                player.setHealth(0);
                break;
            case TELEPORT:  {
                PlayerPartyStorage party = StorageProxy.getParty(player);
                party.teleportPos.teleport(player);
                break;
            }
        }
    }


    @SubscribeEvent
    public static void onPassiveHeal(PassiveHealEvent.Pre event) {
        if (! NuzlockeServerPlayerData.isNuzlockeEnabled(event.getPlayer().getUUID())) {
            return;
        }

        if (event.willRevive() && NuzlockeConfigProxy.getNuzlocke().pokemonFaintingPenalty() == FaintResult.DEAD) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHealerUsed(HealerEvent.Post event) {
        if (! NuzlockeServerPlayerData.isNuzlockeEnabled(event.player.getUUID())) {
            return;
        }
        PlayerPartyStorage party = StorageProxy.getParty(event.player.getUUID());
        healTeam(party);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bedOccurs(PlayerWakeUpEvent event) {
        if (PixelmonConfigProxy.getGeneral().getHealing().isBedsHealPokemon()) {
            PlayerPartyStorage party = StorageProxy.getParty(event.getPlayer().getUUID());
            healTeam(party);
        }
    }

    public static void healTeam(PlayerPartyStorage party) {
        if (party != null) {
            for (Pokemon pokemon : party.getTeam()) {
                if (pokemon.getPersistentData().getBoolean("dead")) {
                    pokemon.setHealth(0);
                } else {
                    pokemon.heal();
                }
            }
        }
    }
}
