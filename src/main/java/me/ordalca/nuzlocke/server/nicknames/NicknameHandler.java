package me.ordalca.nuzlocke.server.nicknames;

import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import com.pixelmonmod.pixelmon.api.events.PokemonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.*;

import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.networking.NuzlockeNetwork;
import me.ordalca.nuzlocke.networking.messages.client.RequestNicknameMessage;
import me.ordalca.nuzlocke.networking.proxies.PokemonHolder;
import me.ordalca.nuzlocke.server.NuzlockeServerPlayerData;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class NicknameHandler {
    private static final boolean nicknamesRequired = PixelmonConfigProxy.getGeneral().isAllowPokemonNicknames() &&
            NuzlockeConfigProxy.getNuzlocke().areNicknamesRequired();


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void receivedPokemon(PokemonReceivedEvent event) {
        if (event.isCanceled()) return;

        NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)StorageProxy.getParty(event.getPlayer()).playerData;
        if (data.isNuzlockeEnabled() && nicknamesRequired) {
            PokemonHolder holder = new PokemonHolder(event.getPokemon());
            if (!holder.isNicknamed()) {
                requestNickname(event.getPlayer(), holder);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void receivedRaidPokemon(CaptureEvent.SuccessfulRaidCapture event) {
        if (event.isCanceled()) return;

        NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)StorageProxy.getParty(event.getPlayer()).playerData;
        if (data.isNuzlockeEnabled() && nicknamesRequired) {
            PokemonHolder holder = new PokemonHolder(event.getRaidPokemon());
            requestNickname(event.getPlayer(), holder);
        }
    }

    @SubscribeEvent
    public static void evolveCheck(EvolveEvent evolve) {
        NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)StorageProxy.getParty(evolve.getPlayer()).playerData;
        if (data.isNuzlockeEnabled() && nicknamesRequired) {
            PlayerPartyStorage party = StorageProxy.getParty(evolve.getPlayer());
            for (Pokemon poke : party.getTeam()) {
                if (poke.getSpecies().is(PixelmonSpecies.SHEDINJA)) {
                    PokemonHolder holder = new PokemonHolder(poke);
                    if (!holder.isNicknamed()) {
                        requestNickname(evolve.getPlayer(), holder);
                    }
                }
            }
        }
    }

    public static void requestNickname(ServerPlayerEntity player, PokemonHolder holder) {
        RequestNicknameMessage message = new RequestNicknameMessage(holder);
        NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->player), message);
    }
}
