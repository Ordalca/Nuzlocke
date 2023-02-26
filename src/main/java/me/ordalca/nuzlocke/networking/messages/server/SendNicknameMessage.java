package me.ordalca.nuzlocke.networking.messages.server;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.*;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.comm.packetHandlers.RenamePokemonPacket;
import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.networking.proxies.PokemonHolder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendNicknameMessage {
    public PokemonHolder pokemon;

    public SendNicknameMessage(PokemonHolder pokemon) {
        this.pokemon = pokemon;
    }

    public static void encode(SendNicknameMessage message, PacketBuffer buffer) {
        buffer.writeNbt(message.pokemon.store());
    }

    public static SendNicknameMessage decode(PacketBuffer buffer) {
        CompoundNBT nbt = buffer.readNbt();
        if (nbt != null) {
            PokemonHolder holder = PokemonHolder.create(nbt);
            return new SendNicknameMessage(holder);
        }
        return null;
    }

    public static void handle(SendNicknameMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PlayerPartyStorage party = StorageProxy.getParty(context.getSender());
            Pokemon pokemon = party.get(message.pokemon.pokemonID);
            if (pokemon != null) {
                StoragePosition pos = party.getPosition(pokemon);
                pokemon.setNickname(new StringTextComponent(message.pokemon.name));
                NetworkHelper.sendToServer(new RenamePokemonPacket(pos, pokemon.getUUID(), message.pokemon.name));
                return;
            } else {
                PCStorage storage = StorageProxy.getPCForPlayer(context.getSender());
                for (int boxIdx = 0; boxIdx < storage.getBoxCount(); boxIdx++) {
                    PCBox box = storage.getBox(boxIdx);
                    for (int slot = 0; slot < 30; ++slot) {
                        pokemon = box.get(slot);
                        if (pokemon != null && pokemon.getUUID().equals(message.pokemon.pokemonID)) {
                            pokemon.setNickname(new StringTextComponent(message.pokemon.name));
                            StoragePosition pos = new StoragePosition(boxIdx, slot);
                            NetworkHelper.sendToServer(new RenamePokemonPacket(pos, pokemon.getUUID(), message.pokemon.name));
                            return;
                        }
                    }
                }
            }
            ModFile.LOGGER.debug("Cannot find pokemon for " + context.getSender() + ": " + message.pokemon.pokemonID);
        });
        context.setPacketHandled(true);
    }
}
