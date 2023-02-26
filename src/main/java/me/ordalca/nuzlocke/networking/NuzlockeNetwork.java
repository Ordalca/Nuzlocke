package me.ordalca.nuzlocke.networking;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.networking.messages.client.BattleBiomeSyncMessage;
import me.ordalca.nuzlocke.networking.messages.client.BiomeBlockedMessage;
import me.ordalca.nuzlocke.networking.messages.client.PlayerDataSyncMessage;
import me.ordalca.nuzlocke.networking.messages.client.RequestNicknameMessage;
import me.ordalca.nuzlocke.networking.messages.server.SendNicknameMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NuzlockeNetwork {
    public static final String VERSION = "0.1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(ModFile.MOD_ID, "nuzlockenetwork"), ()->VERSION, VERSION::equals, VERSION::equals);

    public static void init() {
        ModFile.LOGGER.debug("Registered Messages");

        CHANNEL.registerMessage(0, PlayerDataSyncMessage.class, PlayerDataSyncMessage::encode, PlayerDataSyncMessage::decode, PlayerDataSyncMessage::handle);
        CHANNEL.registerMessage(1, BiomeBlockedMessage.class, BiomeBlockedMessage::encode, BiomeBlockedMessage::decode, BiomeBlockedMessage::handle);
        CHANNEL.registerMessage(2, BattleBiomeSyncMessage.class, BattleBiomeSyncMessage::encode, BattleBiomeSyncMessage::decode, BattleBiomeSyncMessage::handle);
        CHANNEL.registerMessage(3, RequestNicknameMessage.class, RequestNicknameMessage::encode, RequestNicknameMessage::decode, RequestNicknameMessage::handle);
        CHANNEL.registerMessage(4, SendNicknameMessage.class, SendNicknameMessage::encode, SendNicknameMessage::decode, SendNicknameMessage::handle);


    }
}
