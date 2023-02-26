package me.ordalca.nuzlocke.networking;

import me.ordalca.nuzlocke.client.ClientNicknameHandler;
import me.ordalca.nuzlocke.client.NuzlockeClientPlayerData;
import me.ordalca.nuzlocke.networking.messages.client.BattleBiomeSyncMessage;
import me.ordalca.nuzlocke.networking.messages.client.BiomeBlockedMessage;
import me.ordalca.nuzlocke.networking.messages.client.PlayerDataSyncMessage;
import me.ordalca.nuzlocke.networking.messages.client.RequestNicknameMessage;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientMessageHandler {
    public static void handlePacket(PlayerDataSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NuzlockeClientPlayerData.blockedBiomes = msg.blockedBiomes;
        NuzlockeClientPlayerData.nuzlockeEnabled = msg.enabled;
    }
    public static void handlePacket(BiomeBlockedMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NuzlockeClientPlayerData.blockedBiomes.put(msg.blockedBiome, msg.pokemonUUID);
    }
    public static void handlePacket(BattleBiomeSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NuzlockeClientPlayerData.recordBattleBiomes(msg.battleBiomes);
    }
    public static void handlePacket(RequestNicknameMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ClientNicknameHandler.toBeNicknamed.add(msg.pokemon);
        ClientNicknameHandler.checkGui();
    }
}
