package me.ordalca.nuzlocke.captures;

import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBall;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.items.PokeBallPart;

import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.OutOfBattleRestrictions;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;


import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class OutOfBattleCatchControl {
    private static OutOfBattleCatchControl handler = null;
    OutOfBattleCatchControl() {}
    public static OutOfBattleCatchControl getInstance() {
        if (handler == null) {
            handler = new OutOfBattleCatchControl();
        }
        return handler;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void attemptingOutOfBattleCapture(CaptureEvent.StartCapture event) {
        ServerPlayerEntity player = event.getPlayer();
        if (!NuzlockePlayerData.isNuzlockeEnabled(player.getUUID()))
            return;

        PixelmonEntity pokemon = event.getPokemon();

        boolean blockCatch = false;
        if (pokemon.battleController == null) {
            String uuid = pokemon.getUUID().toString();
            String biome = pokemon.getPersistentData().getString(BiomeBlocker.biomeKey);
            NuzlockePlayerData playerData = (NuzlockePlayerData)StorageProxy.getParty(player.getUUID()).playerData;

            OutOfBattleRestrictions restrictions = NuzlockeConfigProxy.getNuzlocke().getOutOfBattleRestrictions();

            if (NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted() && playerData.isBiomeBlocked(uuid, biome)) {
                blockCatch = true;
            } else if (restrictions.preventCatchingLegendsOutOfBattle() && pokemon.getSpecies().isLegendary()) {
                blockCatch = true;
            } else if (restrictions.preventCatchingMythicalOutOfBattle() && pokemon.getSpecies().isMythical()) {
                blockCatch = true;
            } else if (restrictions.preventCatchingUltraBeastsOutOfBattle() && pokemon.getSpecies().isUltraBeast()) {
                blockCatch = true;
            } else if (restrictions.preventCatchingStrongerPokemonOutOfBattle()) {
                int maxLevel = 0;
                List<Pokemon> team = StorageProxy.getParty(player).getTeam();
                for (Pokemon poke : team) {
                    if (poke.getPokemonLevel() > maxLevel) {
                        maxLevel = poke.getPokemonLevel();
                    }
                }
                int level = pokemon.getPokemon().getPokemonLevel();
                int cap = maxLevel + NuzlockeConfigProxy.getNuzlocke().permittedLevelDifferenceToCatch();
                if (level > cap) {
                    blockCatch = true;
                }
            }
        }

        if (blockCatch) {
            if (!shinyCausePermitsCatch(pokemon)) {
                PixelmonCommandUtils.sendMessage(player, "Catching attempt blocked by Nuzlocke rules.");
                event.setCanceled(true);
            }
        }
    }

    private boolean shinyCausePermitsCatch(PixelmonEntity pokemon) {
        return (NuzlockeConfigProxy.getNuzlocke().isShinyClauseActive() &&
                pokemon.getPalette().getName().toLowerCase(Locale.ENGLISH).contains("shiny"));
    }

    @SubscribeEvent
    public void stopMasterBall(PlayerInteractEvent.RightClickItem itemEvent) {
        if (!NuzlockePlayerData.isNuzlockeEnabled(itemEvent.getPlayer().getUUID()))
            return;

        if(NuzlockeConfigProxy.getNuzlocke().preventMasterBallUse() && stackHasMasterBall(itemEvent.getItemStack())) {
            itemEvent.setCanceled(true);
        }
    }
    public static boolean stackHasMasterBall(ItemStack stack) {
        Optional<PokeBall> pokeball = PokeBallPart.getPokeBall(stack);
        return (pokeball.isPresent() && pokeball.get().isGuaranteedCatch());
    }
}
