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

public class BattleBiomeSyncMessage {
        public HashMap<String, String> battleBiomes;
        public BattleBiomeSyncMessage(HashMap<String, String> data) {
            this.battleBiomes = data;
        }
        public static void encode(BattleBiomeSyncMessage message, PacketBuffer buffer) {
            buffer.writeNbt(ModFile.mapToNBT(message.battleBiomes));
        }

        public static BattleBiomeSyncMessage decode(PacketBuffer buffer) {
            CompoundNBT nbt = buffer.readNbt();
            HashMap<String, String> battleBiomes = (nbt != null) ? ModFile.nbtToMap(nbt) : new HashMap<>();
            return new BattleBiomeSyncMessage(battleBiomes);
        }

        public static void handle(BattleBiomeSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                    {
                        ModFile.LOGGER.debug("Unsafe Battle biome sync");
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMessageHandler.handlePacket(msg, ctx));
                    }
            );
            ctx.get().setPacketHandled(true);
        }
}
