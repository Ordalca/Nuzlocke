package me.ordalca.nuzlocke.healing;

import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.ParticipantType;

import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class FaintingController {
    private final HashSet<UUID> playersInBattle;

    private static FaintingController handler = null;
    public FaintingController() {
        playersInBattle = new HashSet<>();
    }
    public static FaintingController getInstance() {
        if (handler == null) {
            handler = new FaintingController();
        }
        return handler;
    }

    @SubscribeEvent
    public void outOfBattleFaint(PixelmonFaintEvent.Post event) {
        PlayerEntity player = event.getPlayer();

        if (player != null && !playerInBattle(player.getUUID())) {
            Pokemon pokemon = event.getPokemon();
            PlayerPartyStorage storage = StorageProxy.getParty(player.getUUID());
            onPokemonFainting(player, storage.getSlot(pokemon));
        }
    }

    private boolean playerInBattle(UUID playerID) {
        return playersInBattle.contains(playerID);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public synchronized void onBattleStart(BattleStartedEvent event) {
        for (BattleParticipant participant : event.participant1) {
            if (participant.getType() == ParticipantType.Player) {
                playersInBattle.add(participant.getEntity().getUUID());
            }
        }
    }

    @SubscribeEvent
    public void onBattleEnded(BattleEndEvent event) {
        Map<BattleParticipant, BattleResults> resultsMap = event.getResults();
        for (BattleParticipant participant : resultsMap.keySet()) {
            if (participant.getEntity() instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) participant.getEntity();
                playersInBattle.remove(player.getUUID());
                if (NuzlockeConfigProxy.getNuzlocke().isPVPDeathEnforced() || !(event.getBattleController().isPvP())) {
                    Pokemon[] party = StorageProxy.getParty(player).getOriginalParty();
                    int count = 0;
                    for (int slot = 0; slot < party.length; slot++) {
                        Pokemon poke = party[slot];
                        if (poke != null) {
                            if (poke.isFainted()) {
                                onPokemonFainting(player, slot);
                            } else {
                                count++;
                            }
                        }
                    }
                    if (count == 0) {
                        playerWiped(player);
                    }
                }
            }
        }
    }

    private void playerWiped(ServerPlayerEntity player) {
        PixelmonCommandUtils.sendMessage(player, "Your team has died, the nuzlocke is lost.");
        switch (NuzlockeConfigProxy.getNuzlocke().trainerWipePenalty()) {
            case DEATH:
                player.kill();
                break;
            case TELEPORT:  {
                PlayerPartyStorage party = StorageProxy.getParty(player);
                party.teleportPos.teleport(player);
                break;
            }
        }
    }

    public void onPokemonFainting(PlayerEntity player, int slot) {
        PlayerPartyStorage storage = StorageProxy.getParty(player.getUUID());
        switch (NuzlockeConfigProxy.getNuzlocke().pokemonFaintingPenalty()) {
            case FAINT: break;
            case DELETE:
            {
                storage.set(slot, null);
                break;
            }
            case DEAD:
            {
                Pokemon faintedPokemon = storage.get(slot);
                if (faintedPokemon != null) {
                    faintedPokemon.getPersistentData().putBoolean("dead", true);
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onPassiveHeal(PassiveHealEvent.Pre event) {
        if (event.willRevive() && NuzlockeConfigProxy.getNuzlocke().pokemonFaintingPenalty() == FaintResult.DEAD) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHealerUsed(HealerEvent.Post event) {
        PlayerEntity player = event.player;
        PlayerPartyStorage party = StorageProxy.getParty(player.getUUID());
        for (Pokemon pokemon : party.getTeam()) {
            if (pokemon.getPersistentData().getBoolean("dead")) {
                pokemon.setHealth(0);
            }
        }
    }

    @SubscribeEvent
    public void blockMainHandRevives(PlayerInteractEvent.RightClickItem itemEvent) {
        PlayerEntity player = itemEvent.getPlayer();
        if (player != null && NuzlockeConfigProxy.getNuzlocke().pokemonFaintingPenalty() == FaintResult.DEAD) {
            Item item = itemEvent.getItemStack().getItem();
            if (item.equals(PixelmonItems.revive) || item.equals(PixelmonItems.max_revive) || item.equals(PixelmonItems.revival_herb)) {
                itemEvent.setCanceled(true);
            } else if(item.equals(PixelmonItems.sacredash)) {
                itemEvent.setCanceled(true);
                ItemStack ash = player.getMainHandItem();
                ash.shrink(1);
                fakeSacredAsh(player);
            }
        }
    }

    private void fakeSacredAsh(PlayerEntity player) {
        PlayerPartyStorage party = StorageProxy.getParty(player.getUUID());
        if (party != null) {
            for (Pokemon pokemon : party.getTeam()) {
                if (pokemon.isFainted()) continue;
                pokemon.heal();
            }
        }
    }

    @SubscribeEvent
    public void sendOut(PokemonSendOutEvent.Pre event) {
        Pokemon pokemon = event.getPokemon();
        if (pokemon.getPersistentData().getBoolean("dead")) {
            pokemon.setHealth(0);
            event.setCanceled(true);
        }
    }
}
