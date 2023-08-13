package me.ordalca.nuzlocke.server.captures;

import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.boss.BossTier;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.networking.NuzlockeNetwork;
import me.ordalca.nuzlocke.networking.messages.client.BattleBiomeSyncMessage;
import me.ordalca.nuzlocke.networking.messages.client.BiomeBlockedMessage;
import me.ordalca.nuzlocke.server.NuzlockeServerPlayerData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BiomeBlocker {
    public static final String biomeKey = "spawnBiome";
    BiomeBlocker() {}

    @SubscribeEvent
    public static void entityAdded(SpawnEvent event) {
        Entity spawnedEntity = event.action.getOrCreateEntity();
        if (spawnedEntity instanceof PixelmonEntity) {
            PixelmonEntity pokemon = (PixelmonEntity) spawnedEntity;
            ResourceLocation location = event.action.spawnLocation.biome.getRegistryName();

            if (location != null && !pokemon.getPersistentData().contains(biomeKey)) {
                String path = location.getPath();
                pokemon.getPersistentData().putString(biomeKey, path);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blockViaCaptureAttempt(CaptureEvent.StartCapture event) {
        if (!event.isCanceled()) {
            PixelmonEntity pokemon = event.getPokemon();
            String uuid = event.getPokemon().getUUID().toString();

            String currentBiome = pokemon.getPersistentData().getString(biomeKey);
            if (!currentBiome.equals("")) {
                PlayerPartyStorage storage = StorageProxy.getParty(event.getPlayer().getUUID());
                NuzlockeServerPlayerData data = (NuzlockeServerPlayerData) storage.playerData;
                if (data.blockBiomeForPokemon(uuid, currentBiome)) {
                    BiomeBlockedMessage message = new BiomeBlockedMessage(currentBiome,uuid);
                    NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), message);
                }
            } else {
                PixelmonCommandUtils.sendMessage(event.getPlayer(), "Unknown biome for caught pokemon");
            }
        }
    }

    @SubscribeEvent
    public static void blockBattlePokemon(BattleEndEvent event) {
        if (NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted()) {
            ArrayList<ServerPlayerEntity> players = getPlayers(event.getBattleController().participants);
            for (ServerPlayerEntity player : players) {
                PlayerPartyStorage storage = StorageProxy.getParty(player.getUUID());
                NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)storage.playerData;
                if (data.isNuzlockeEnabled()) {
                    for (String pokemonUUID : data.battleBiomes.keySet()) {
                        String biome = data.battleBiomes.get(pokemonUUID);
                        if (!biome.equals("")) {
                            // if not already blocked
                            if (!data.isBiomeBlocked(pokemonUUID, biome)) {
                                //then block it if dupes clause doesn't avoid it
                                if(data.blockBiomeForPokemon(pokemonUUID, biome)) {
                                    BiomeBlockedMessage message = new BiomeBlockedMessage(biome,pokemonUUID);
                                    NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->player), message);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockViaFirstEncounter(BattleStartedEvent event) {
        if (NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted()) {
            ArrayList<ServerPlayerEntity> players = getPlayers(event.getTeamOne());
            if (players.size() > 0) {
                ArrayList<PixelmonEntity> nonBossWildPokemon = BiomeBlocker.getPokemon(event.getTeamTwo());
                for (ServerPlayerEntity player : players) {
                    PlayerPartyStorage storage = StorageProxy.getParty(player);
                    NuzlockeServerPlayerData data = (NuzlockeServerPlayerData)storage.playerData;
                    if (data.isNuzlockeEnabled()) {
                        HashMap<String, String> battleRecord = new HashMap<>();
                        HashMap<String, String> fullBattleRecord = new HashMap<>();

                        for (PixelmonEntity pokemon : nonBossWildPokemon) {
                            String pokemonUUID = pokemon.getUUID().toString();
                            String biome = pokemon.getPersistentData().getString(biomeKey);
                            if (!biome.equals("")) {
                                if (dupesClauseCheck(storage, pokemon))
                                    battleRecord.put(pokemonUUID, biome);
                                fullBattleRecord.put(pokemonUUID, biome);
                            } else {
                                ModFile.LOGGER.debug("Unknown biome for pokemon " + pokemon);
                            }
                        }

                        data.battleBiomes = battleRecord;
                        BattleBiomeSyncMessage message = new BattleBiomeSyncMessage(fullBattleRecord);
                        NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->player), message);
                    }
                }
            }
        }
    }

    private static boolean dupesClauseCheck(PlayerPartyStorage party, PixelmonEntity pokemon) {
        if (NuzlockeConfigProxy.getNuzlocke().isDupesClauseActive()) {
            return !party.playerPokedex.hasCaught(pokemon.getSpecies());
        }
        return true;
    }

    public static ArrayList<ServerPlayerEntity> getPlayers(List<BattleParticipant> participants) {
        ArrayList<ServerPlayerEntity> players = new ArrayList<>();
        for (BattleParticipant participant : participants) {
            if (participant.getEntity() instanceof ServerPlayerEntity) {
                players.add((ServerPlayerEntity) participant.getEntity());
            }
        }
        return players;
    }
    public static ArrayList<ServerPlayerEntity> getPlayers(BattleParticipant[] participants) {
        ArrayList<ServerPlayerEntity> players = new ArrayList<>();
        for (BattleParticipant participant : participants) {
            if (participant.getEntity() instanceof ServerPlayerEntity) {
                players.add((ServerPlayerEntity) participant.getEntity());
            }
        }
        return players;
    }

    public static ArrayList<PixelmonEntity> getPokemon(BattleParticipant[] participants) {
        ArrayList<PixelmonEntity> pokemonList = new ArrayList<>();
        for (BattleParticipant participant : participants) {
            if (participant instanceof WildPixelmonParticipant) {
                WildPixelmonParticipant part = (WildPixelmonParticipant) participant;
                for (PixelmonWrapper poke : part.controlledPokemon) {
                    BossTier bossTier = poke.entity.getBossTier();
                    if (! bossTier.isBoss()) {
                        pokemonList.add(poke.entity);
                    }
                }
            }
        }
        return pokemonList;
    }
}
