package me.ordalca.nuzlocke.server;

import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.storage.playerData.PlayerData;
import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.networking.NuzlockeNetwork;
import me.ordalca.nuzlocke.networking.messages.client.PlayerDataSyncMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class NuzlockeServerPlayerData extends PlayerData {
    public static final String nbtKey = "blockedBiomes";
    private static final String enabledKey = "nuzlockeEnabled";
    private final PlayerData parent;

    public boolean inBattle = false;
    public boolean inRaid = false;
    public ArrayList<UUID> faintedPokemon = new ArrayList<>();
    public HashMap<String, String> battleBiomes  = new HashMap<>();

    public boolean nuzlockeEnabled = false;
    public HashMap<String, String> blockedBiomes  = new HashMap<>();

    public NuzlockeServerPlayerData(PlayerData data) {
        parent = data;
    }

    public static boolean isNuzlockeEnabled(UUID player) {
        if (NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
            PlayerPartyStorage storage = StorageProxy.getParty(player);
            if (storage != null) {
                NuzlockeServerPlayerData data = (NuzlockeServerPlayerData) storage.playerData;
                return data.nuzlockeEnabled;
            }
        }
        return true;
    }
    public boolean isNuzlockeEnabled() {
        if (NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
            return this.nuzlockeEnabled;
        }
        return true;
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity)event.getPlayer();
        NuzlockeServerPlayerData data = loadPlayerData(player);
        PlayerDataSyncMessage message = new PlayerDataSyncMessage(data.blockedBiomes, data.isNuzlockeEnabled());
        NuzlockeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->player), message);
    }
    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerData data = StorageProxy.getParty(event.getPlayer().getUUID()).playerData;
        if (data instanceof NuzlockeServerPlayerData) {
            NuzlockeServerPlayerData ndata = (NuzlockeServerPlayerData) data;
            event.getPlayer().getPersistentData().put(nbtKey, ModFile.mapToNBT(ndata.blockedBiomes));
            event.getPlayer().getPersistentData().putBoolean(enabledKey, ndata.nuzlockeEnabled);
        }
    }

    public static NuzlockeServerPlayerData loadPlayerData(PlayerEntity player) {
        PlayerPartyStorage storage = StorageProxy.getParty(player.getUUID());
        NuzlockeServerPlayerData nuzlockePlayerData = new NuzlockeServerPlayerData(storage.playerData);
        storage.playerData = nuzlockePlayerData;

        CompoundNBT nbt = (CompoundNBT) player.getPersistentData().get(nbtKey);
        if (nbt != null) {
            nuzlockePlayerData.blockedBiomes = ModFile.nbtToMap(nbt);
        }

        if (NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
            nuzlockePlayerData.nuzlockeEnabled = player.getPersistentData().getBoolean(enabledKey);
        } else {
            nuzlockePlayerData.nuzlockeEnabled = true;
        }

        return nuzlockePlayerData;
    }
    public boolean isBiomeBlocked(String pokemonUUID, String biome) {
        if (!nuzlockeEnabled) { return false; }

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
    public boolean blockBiomeForPokemon(String pokemonUUID, String biome) {
        if (nuzlockeEnabled) {
            blockedBiomes.put(biome, pokemonUUID);
        }
        return nuzlockeEnabled;
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
