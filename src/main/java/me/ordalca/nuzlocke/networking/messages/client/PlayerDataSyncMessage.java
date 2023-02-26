package me.ordalca.nuzlocke.networking.messages.client;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.networking.ClientMessageHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public class PlayerDataSyncMessage {
    public HashMap<String, String> blockedBiomes;
    public boolean enabled;
    public PlayerDataSyncMessage(HashMap<String, String> data, boolean enabled) {
        this.blockedBiomes = data;
        this.enabled = enabled;
    }
    public static void encode(PlayerDataSyncMessage message, PacketBuffer buffer) {
        buffer.writeNbt(ModFile.mapToNBT(message.blockedBiomes));
        buffer.writeBoolean(message.enabled);
    }

    public static PlayerDataSyncMessage decode(PacketBuffer buffer) {
        CompoundNBT nbt = buffer.readNbt();
        HashMap<String, String> blockedBiomes = (nbt != null) ? ModFile.nbtToMap(nbt) : new HashMap<>();
        boolean enabled = buffer.readBoolean();
        return new PlayerDataSyncMessage(blockedBiomes, enabled);
    }

    public static void handle(PlayerDataSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                {
                    ModFile.LOGGER.debug("Unsafe Player Data sync");
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMessageHandler.handlePacket(msg, ctx));
                }
        );
        ctx.get().setPacketHandled(true);
    }
}
