package me.ordalca.nuzlocke.captures;

import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.boss.BossTier;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

public class BiomeBlocker {
    public static final String biomeKey = "spawnBiome";
    private static BiomeBlocker handler = null;
    BiomeBlocker() { }
    public static BiomeBlocker getInstance() {
        if (handler == null) {
            handler = new BiomeBlocker();
        }
        return handler;
    }

    @SubscribeEvent
    public void entityAdded(SpawnEvent event) {
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

    @SubscribeEvent
    public void checkBlockedBiomes(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().getItem() instanceof CompassItem) {
            if (event.getPlayer().level.isClientSide()) {
                NuzlockePlayerData ndata = (NuzlockePlayerData) StorageProxy.getParty(event.getPlayer().getUUID()).playerData;
                if (ndata != null) {
                    if (ndata.blockedBiomes.size() > 0) {
                        ChatHandler.sendChat(event.getEntityLiving(), "Blocked biomes: " + ndata.blockedBiomes.keySet());
                    } else {
                        ChatHandler.sendChat(event.getEntityLiving(), "No Blocked biomes");
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void checkBlockedBiomesDebug(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getItemStack().getItem() instanceof CompassItem) {
            if (event.getPlayer().level.isClientSide()) {
                NuzlockePlayerData ndata = (NuzlockePlayerData) StorageProxy.getParty(event.getPlayer().getUUID()).playerData;
                if (ndata != null) {
                    if (ndata.blockedBiomes.size() > 0) {
                        ChatHandler.sendChat(event.getEntityLiving(), "Blocked biomes: " + ndata.blockedBiomes);
                    } else {
                        ChatHandler.sendChat(event.getEntityLiving(), "No Blocked biomes");
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void blockViaCaptureAttempt(CaptureEvent.StartCapture event) {
        if (!event.isCanceled()) {
            PixelmonEntity pokemon = event.getPokemon();
            String uuid = event.getPokemon().getUUID().toString();

            String currentBiome = pokemon.getPersistentData().getString(biomeKey);
            if (!currentBiome.equals("")) {
                PlayerPartyStorage storage = StorageProxy.getParty(event.getPlayer().getUUID());
                NuzlockePlayerData data = (NuzlockePlayerData) storage.playerData;
                data.blockBiomeForPokemon(uuid, currentBiome);
            } else {
                PixelmonCommandUtils.sendMessage(event.getPlayer(), "Unknown biome for caught pokemon");
            }
        }
    }

    @SubscribeEvent
    public void blockViaFirstEncounter(BattleStartedEvent event) {
        if (NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted()) {
            ArrayList<PlayerEntity> players = getPlayers(event.participant1);
            if (players.size() > 0) {
                ArrayList<PixelmonEntity> nonBossWildPokemon = BiomeBlocker.getPokemon(event.participant2);
                for (PlayerEntity player : players) {
                    for (PixelmonEntity pokemon : nonBossWildPokemon) {
                        PlayerPartyStorage storage = StorageProxy.getParty(player.getUUID());
                        NuzlockePlayerData data = (NuzlockePlayerData)storage.playerData;
                        if (data.isNuzlockeEnabled()) {
                            data.recordBattleBiomes(nonBossWildPokemon);
                            String pokemonUUID = pokemon.getUUID().toString();
                            String biome = pokemon.getPersistentData().getString(biomeKey);
                            if (!biome.equals("")) {
                                // if not already blocked
                                if (!data.isBiomeBlocked(pokemonUUID, biome)) {
                                    //then block it if dupes clause doesn't avoid it
                                    if (dupesClauseCheck(storage, pokemon)) {
                                        data.blockBiomeForPokemon(pokemonUUID, biome);
                                    }
                                }
                            } else {
                                ModFile.LOGGER.debug("Unknown biome for pokemon " + pokemon);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean dupesClauseCheck(PlayerPartyStorage party, PixelmonEntity pokemon) {
        if (NuzlockeConfigProxy.getNuzlocke().isDupesClauseActive()) {
            return !party.playerPokedex.hasCaught(pokemon.getSpecies());
        }
        return true;
    }

    public static ArrayList<PlayerEntity> getPlayers(BattleParticipant[] participants) {
        ArrayList<PlayerEntity> players = new ArrayList<>();
        for (BattleParticipant participant : participants) {
            if (participant.getEntity() instanceof PlayerEntity) {
                players.add((PlayerEntity) participant.getEntity());
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
