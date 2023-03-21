package me.ordalca.nuzlocke.server.captures;

import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.PokeBallImpactEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;

import com.pixelmonmod.pixelmon.entities.pokeballs.PokeBallEntity;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.OutOfBattleRestrictions;

import me.ordalca.nuzlocke.server.NuzlockeServerPlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;


import java.util.List;
import java.util.Locale;

public class OutOfBattleCatchControl {
    OutOfBattleCatchControl() {}

    public static boolean outOfBattleCatchingPermitted(PixelmonEntity pokemon, ServerPlayerEntity player) {
        NuzlockeServerPlayerData playerData = (NuzlockeServerPlayerData)StorageProxy.getParty(player).playerData;
        if (!playerData.isNuzlockeEnabled())
            return true;

        boolean blockCatch = false;
        String reason = "";
        if (pokemon.battleController == null) {
            String uuid = pokemon.getUUID().toString();
            String biome = pokemon.getPersistentData().getString(BiomeBlocker.biomeKey);

            OutOfBattleRestrictions restrictions = NuzlockeConfigProxy.getNuzlocke().getOutOfBattleRestrictions();

            if (NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted() && playerData.isBiomeBlocked(uuid, biome)) {
                blockCatch = true;
                reason = "First encounter";
            } else if (restrictions.preventCatchingLegendsOutOfBattle() && pokemon.getSpecies().isLegendary()) {
                blockCatch = true;
                reason = "No Legendary pokémon outside battle";
            } else if (restrictions.preventCatchingMythicalOutOfBattle() && pokemon.getSpecies().isMythical()) {
                blockCatch = true;
                reason = "No mythical pokémon outside battle";
            } else if (restrictions.preventCatchingUltraBeastsOutOfBattle() && pokemon.getSpecies().isUltraBeast()) {
                blockCatch = true;
                reason = "No ultra beasts outside battle";
            } else if (restrictions.preventCatchingStrongerPokemonOutOfBattle()) {
                int maxLevel = 0;
                List<Pokemon> team = StorageProxy.getParty(player).getTeam();
                for (Pokemon poke : team) {
                    if (poke.getPokemonLevel() > maxLevel) {
                        maxLevel = poke.getPokemonLevel();
                    }
                }
                int level = pokemon.getPokemon().getPokemonLevel();
                int cap = maxLevel + NuzlockeConfigProxy.getNuzlocke().permittedLevelDifferenceToCatch();
                if (level > cap) {
                    reason = "Target is too strong";
                    blockCatch = true;
                }
            }
        }

        if (blockCatch) {
            if (!shinyCausePermitsCatch(pokemon)) {
                PixelmonCommandUtils.sendMessage(player, "Catching attempt blocked by Nuzlocke rules: "+reason);
                return false;
            }
        }
        return true;
    }

    private static boolean shinyCausePermitsCatch(PixelmonEntity pokemon) {
        return (NuzlockeConfigProxy.getNuzlocke().isShinyClauseActive() &&
                pokemon.getPalette().getName().toLowerCase(Locale.ENGLISH).contains("shiny"));
    }
    @SubscribeEvent
    public static void ballHitsPokemon(PokeBallImpactEvent event) {
        if (event.isEmptyPokeBall()) {
            PokeBallEntity pokeball = event.getPokeBall();
            if (pokeball.getOwner() instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) pokeball.getOwner();
                event.getEntityHit().ifPresent(entity -> {
                    if (entity instanceof PixelmonEntity) {
                        PixelmonEntity pokemon = (PixelmonEntity) entity;
                        if (pokemon.battleController == null) {
                            boolean permitted = outOfBattleCatchingPermitted(pokemon, player);
                            event.setCanceled(!permitted);
                        }
                    }
                });
            }
        }
    }
}
