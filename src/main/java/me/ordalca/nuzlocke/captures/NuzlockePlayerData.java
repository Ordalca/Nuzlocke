package me.ordalca.nuzlocke.captures;

import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.storage.playerData.PlayerData;
import me.ordalca.nuzlocke.ModFile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class NuzlockePlayerData extends PlayerData {
    public static final String nbtKey = "blockedBiomes";

    private final PlayerData parent;
    public final HashMap<String, String> blockedBiomes  = new HashMap<>();
    public final HashMap<String, String> battleBiomes  = new HashMap<>();
    public NuzlockePlayerData(PlayerData data) {
        parent = data;
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerPartyStorage storage = StorageProxy.getParty(event.getPlayer().getUUID());
        NuzlockePlayerData nuzlockePlayerData = new NuzlockePlayerData(storage.playerData);
        storage.playerData = nuzlockePlayerData;

        CompoundNBT nbt = (CompoundNBT) event.getPlayer().getPersistentData().get(nbtKey);
        if (nbt != null) {
            nuzlockePlayerData.load(nbt);
        }
    }
    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerData data = StorageProxy.getParty(event.getPlayer().getUUID()).playerData;
        if (data instanceof NuzlockePlayerData) {
            NuzlockePlayerData ndata = (NuzlockePlayerData) data;
            event.getPlayer().getPersistentData().put(nbtKey, ndata.saveAsCompoundNBT());
        }
    }

    public void recordBattleBiomes(ArrayList<PixelmonEntity> opponents) {
        battleBiomes.clear();
        for (PixelmonEntity pokemon : opponents) {
            String biome = pokemon.getPersistentData().getString(BiomeBlocker.biomeKey);
            battleBiomes.put(pokemon.getUUID().toString(), biome);
        }
    }
    public boolean isBiomeBlocked(String pokemonUUID) {
        if (battleBiomes.containsKey(pokemonUUID)) {
            return isBiomeBlocked(pokemonUUID, battleBiomes.get(pokemonUUID));
        }
        ModFile.LOGGER.debug("Unknown UUID check for isBiomeBlocked(UUID): "+pokemonUUID+" "+battleBiomes);
        return true;
    }

    public boolean isBiomeBlocked(String pokemonUUID, String biome) {
        if (biome.equals("")) {
            ModFile.LOGGER.debug("Unknown biome for "+pokemonUUID);
            return true;
        } else {
            boolean hasBiome = blockedBiomes.containsKey(biome);
            if (hasBiome) {
                boolean blockedByThisPokemon = blockedBiomes.get(biome).equals(pokemonUUID);
                return !blockedByThisPokemon;
            } else {
                return false;
            }
        }
    }
    public void blockBiomeForPokemon(String pokemonUUID, String biome) {
        blockedBiomes.put(biome, pokemonUUID);
    }

    public CompoundNBT saveAsCompoundNBT() {
        CompoundNBT blocked = new CompoundNBT();
        for (String key : blockedBiomes.keySet()) {
            blocked.putString(key, blockedBiomes.get(key));
        }
        return blocked;
    }

    public void load(CompoundNBT blocked) {
        for (String key : blocked.getAllKeys()) {
            blockedBiomes.put(key, blocked.getString(key));
        }
    }


    public void writeToNBT(CompoundNBT var1) { parent.writeToNBT(var1); }
    public void readFromNBT(CompoundNBT var1) { parent.readFromNBT(var1); }
    public boolean getWasGifted() {
        return parent.getWasGifted();
    }
    public boolean getWasGifted(int year) {
        return parent.getWasGifted(year);
    }
    public void receivedGift(int year) { parent.receivedGift(year); }
}
