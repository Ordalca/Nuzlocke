package me.ordalca.nuzlocke.networking.messages.client;


import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.networking.ClientMessageHandler;
import me.ordalca.nuzlocke.networking.proxies.PokemonHolder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestNicknameMessage {
    public PokemonHolder pokemon;
    public RequestNicknameMessage(PokemonHolder pokemon) {
        this.pokemon = pokemon;
    }
    public static void encode(RequestNicknameMessage message, PacketBuffer buffer) {
        buffer.writeNbt(message.pokemon.store());
    }

    public static RequestNicknameMessage decode(PacketBuffer buffer) {
        CompoundNBT nbt = buffer.readNbt();
        if (nbt != null) {
            PokemonHolder holder = PokemonHolder.create(nbt);
            return new RequestNicknameMessage(holder);
        }
        return null;
    }
    public static void handle(RequestNicknameMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ModFile.LOGGER.debug("Unsafe RequestNickname");
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMessageHandler.handlePacket(msg, ctx));
                }
        );
        ctx.get().setPacketHandled(true);
    }
}
