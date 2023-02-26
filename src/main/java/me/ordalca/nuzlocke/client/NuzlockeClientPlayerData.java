package me.ordalca.nuzlocke.client;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;

import java.util.HashMap;

public class NuzlockeClientPlayerData {
    public static boolean nuzlockeEnabled = false;
    public static boolean nicknaming = false;

    public static HashMap<String,String>  blockedBiomes = new HashMap<>();
    private static HashMap<String,String> battleBiomes = new HashMap<>();

    public static boolean isNuzlockeEnabled() {
        return nuzlockeEnabled || !(NuzlockeConfigProxy.getNuzlocke().isPermissionRequired());
    }

    public static void recordBattleBiomes(HashMap<String,String> opponents) {
        battleBiomes = opponents;
    }

    public static boolean isBiomeBlocked(String pokemonUUID) {
        if (!nuzlockeEnabled) {
            return false;
        }
        ModFile.LOGGER.debug("block:"+pokemonUUID+" "+battleBiomes);
        if (battleBiomes.containsKey(pokemonUUID)) {
            return isBiomeBlocked(pokemonUUID, battleBiomes.get(pokemonUUID));
        }
        ModFile.LOGGER.debug("Unknown UUID check for isBiomeBlocked(UUID): "+pokemonUUID+" "+battleBiomes);
        return true;
    }

    public static boolean isBiomeBlocked(String pokemonUUID, String biome) {
        if (!nuzlockeEnabled) {
            return false;
        }

        if (biome.equals("")) {
            return true;
        } else {
            boolean hasBiome = blockedBiomes.containsKey(biome);
            ModFile.LOGGER.debug("block:"+pokemonUUID+" "+hasBiome+" "+biome);

            if (hasBiome) {
                boolean blockedByThisPokemon = blockedBiomes.get(biome).equals(pokemonUUID);
                ModFile.LOGGER.debug("block: "+blockedByThisPokemon);

                return !blockedByThisPokemon;
            } else {

                return false;
            }
        }
    }
}
