package me.ordalca.nuzlocke.networking.messages.client;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.networking.ClientMessageHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BiomeBlockedMessage {
    public String blockedBiome;
    public String pokemonUUID;
    public BiomeBlockedMessage(String biome, String uuid) {
        this.blockedBiome = biome;
        this.pokemonUUID = uuid;
    }
    public static void encode(BiomeBlockedMessage message, PacketBuffer buffer) {
        buffer.writeUtf(message.blockedBiome);
        buffer.writeUtf(message.pokemonUUID);
    }

    public static BiomeBlockedMessage decode(PacketBuffer buffer) {
        String biome = buffer.readUtf();
        String uuid = buffer.readUtf();
        return new BiomeBlockedMessage(biome, uuid);
    }

    public static void handle(BiomeBlockedMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ModFile.LOGGER.debug("Unsafe Biome block");
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMessageHandler.handlePacket(msg, ctx));
                }
        );
        ctx.get().setPacketHandled(true);
    }
}
