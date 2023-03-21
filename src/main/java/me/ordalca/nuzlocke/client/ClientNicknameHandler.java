package me.ordalca.nuzlocke.client;

import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.networking.NuzlockeNetwork;
import me.ordalca.nuzlocke.networking.messages.server.SendNicknameMessage;
import me.ordalca.nuzlocke.networking.proxies.PokemonHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.UUID;

public class ClientNicknameHandler {
    public static ArrayList<PokemonHolder> toBeNicknamed = new ArrayList<PokemonHolder>();
    @SubscribeEvent
    public static void screenClear(GuiOpenEvent event) {
        if (event.getGui() == null) {

            if (NuzlockeConfigProxy.getNuzlocke().areNicknamesRequired()) {
                if (toBeNicknamed.size() > 0) {
                    PokemonHolder pokemon = toBeNicknamed.get(0);
                    NicknameRequiredScreen screen = new NicknameRequiredScreen(pokemon);
                    event.setGui(screen);
                } else {
                    NuzlockeClientPlayerData.nicknaming = false;
                }
            }
        }
    }

    public static void reportNickname(PokemonHolder holder) {
        toBeNicknamed.remove(holder);
        NuzlockeNetwork.CHANNEL.sendToServer(new SendNicknameMessage(holder));
    }

    @SubscribeEvent
    public static void makeInvulnerable(LivingHurtEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            UUID hurtUUID = event.getEntity().getUUID();
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null && hurtUUID.equals(Minecraft.getInstance().player.getUUID())) {
                if (NuzlockeClientPlayerData.nicknaming) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public static void checkGui() {
        if (Minecraft.getInstance().screen == null) {
            PokemonHolder pokemon = toBeNicknamed.remove(0);
            NicknameRequiredScreen screen = new NicknameRequiredScreen(pokemon);
            Minecraft.getInstance().setScreen(screen);
        }
    }
}
