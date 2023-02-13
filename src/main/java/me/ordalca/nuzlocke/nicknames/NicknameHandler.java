package me.ordalca.nuzlocke.nicknames;

import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import com.pixelmonmod.pixelmon.api.events.PokemonReceivedEvent;
import com.pixelmonmod.pixelmon.api.events.pokemon.SetNicknameEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.*;
import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.captures.NuzlockePlayerData;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.UUID;

public class NicknameHandler {
    private static NicknameHandler handler = null;
    private boolean nicknamesRequired = false;
    private final ArrayList<Pokemon> toBeNicknamed = new ArrayList<>();
    NicknameHandler() {}
    public static NicknameHandler getInstance() {
        if (handler == null) {
            handler = new NicknameHandler();
            if (PixelmonConfigProxy.getGeneral().isAllowPokemonNicknames()) {
                handler.nicknamesRequired = NuzlockeConfigProxy.getNuzlocke().areNicknamesRequired();
            }
        }
        return handler;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void receivedPokemon(PokemonReceivedEvent event) {
        if (event.isCanceled()) return;

        NuzlockePlayerData data = (NuzlockePlayerData) StorageProxy.getParty(event.getPlayer()).playerData;
        if (data.nuzlockeEnabled && nicknamesRequired) {
            ModFile.LOGGER.debug("received: "+event.getPokemon().getDisplayName());
            if (!nicknamed(event.getPokemon())) {
                toBeNicknamed.add(event.getPokemon());
                checkGui();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void receivedRaidPokemon(CaptureEvent.SuccessfulRaidCapture event) {
        if (event.isCanceled()) return;

        NuzlockePlayerData data = (NuzlockePlayerData) StorageProxy.getParty(event.getPlayer()).playerData;
        if (data.nuzlockeEnabled && nicknamesRequired) {
            ModFile.LOGGER.debug("receivedRaid: "+event.getRaidPokemon().getDisplayName());
            toBeNicknamed.add(event.getRaidPokemon());
            checkGui();
        }
    }

    private void checkGui() {
        if (Minecraft.getInstance().screen == null) {
            Pokemon pokemon = toBeNicknamed.remove(0);
            NicknameRequiredScreen screen = new NicknameRequiredScreen(pokemon);
            Minecraft.getInstance().setScreen(screen);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void evolveCheck(EvolveEvent evolve) {
        checkForShedinja(evolve.getPlayer().getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void screenClear(GuiOpenEvent event) {
        if (event.getGui() == null) {
            filterNicknames();
            if (toBeNicknamed.size() > 0) {
                ClientPlayerEntity playerEntity = Minecraft.getInstance().player;
                if (playerEntity != null) {
                    UUID player = Minecraft.getInstance().player.getUUID();
                    NuzlockePlayerData playerData = (NuzlockePlayerData) StorageProxy.getParty(player).playerData;
                    playerData.nicknaming = true;

                    Pokemon pokemon = toBeNicknamed.get(0);
                    NicknameRequiredScreen screen = new NicknameRequiredScreen(pokemon);
                    event.setGui(screen);
                }
            }
        }
    }

    private void checkForShedinja(UUID playerID) {
        PlayerPartyStorage party = StorageProxy.getParty(playerID);
        for (Pokemon poke : party.getTeam()) {
            if (poke.getSpecies().is(PixelmonSpecies.SHEDINJA)) {
                if (!nicknamed(poke)) {
                    toBeNicknamed.add(poke);
                }
            }
        }
    }

    private void filterNicknames() {
        for (int idx = toBeNicknamed.size()-1; idx >= 0; idx--) {
            Pokemon poke = toBeNicknamed.get(idx);
            if (nicknamed(poke)) {
                toBeNicknamed.remove(idx);
                ModFile.LOGGER.debug("Removed previously nicknamed pokemon "+poke.getDisplayName());
            }
        }
    }

    private boolean nicknamed(Pokemon poke) {
        ModFile.LOGGER.debug("Checking "+poke.getDisplayName()+" the "+poke.getSpecies().getName());
        return !(poke.getDisplayName().equalsIgnoreCase(poke.getSpecies().getName()));
    }

    @SubscribeEvent
    public void makeInvulnerable(LivingHurtEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            NuzlockePlayerData playerData = (NuzlockePlayerData)StorageProxy.getParty(player.getUUID()).playerData;
            if (playerData.nicknaming) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void nicknameSet(SetNicknameEvent event) {
        if (NuzlockeConfigProxy.getNuzlocke().areNicknamesRequired() && !event.isCanceled()) {
            NuzlockePlayerData data = (NuzlockePlayerData) StorageProxy.getParty(event.player).playerData;
            if (data.nuzlockeEnabled && this.toBeNicknamed.size() == 0) {
                data.nicknaming = false;
            }
        }
    }
}
