package me.ordalca.nuzlocke.server.battles;

import com.pixelmonmod.pixelmon.ai.ExecuteActionGoal;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.SetBattleAIEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.species.aggression.Aggression;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.battles.controller.ai.AdvancedAI;
import com.pixelmonmod.pixelmon.battles.controller.ai.AggressiveAI;
import com.pixelmonmod.pixelmon.battles.controller.ai.BattleAIBase;
import com.pixelmonmod.pixelmon.battles.controller.ai.TacticalAI;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.spawning.PlayerTrackingSpawner;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.TrainerSkill;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.PokemonAggression;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;

import me.ordalca.nuzlocke.server.NuzlockeServerPlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public class AIAdapter {
    AIAdapter() {}

    @SubscribeEvent
    public static void npcPokemonBooster(BattleStartedEvent event) {
        if (! (participantInNuzlocke(Arrays.asList(event.getTeamOne())))) {
            return;
        }


        if (NuzlockeConfigProxy.getNuzlocke().isEliteTrainerPokemon()) {
            for (BattleParticipant part : event.getTeamTwo()) {
                if (part instanceof TrainerParticipant) {
                    TrainerParticipant trainer = (TrainerParticipant) part;
                    for (PixelmonWrapper pokemon : trainer.allPokemon) {
                        pokemon.pokemon.getIVs().maximizeIVs();
                        pokemon.pokemon.getEVs().fillFromArray(252, 252, 252, 252, 252, 252);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void npcAIChooser(SetBattleAIEvent event) {
        if (!participantInNuzlocke(event.getBattleController().participants)) {
            return;
        }
        ModFile.LOGGER.debug("set AI");

        if (event.getParticipant() instanceof TrainerParticipant) {
            ModFile.LOGGER.debug("trainer ai");

            TrainerParticipant trainer = (TrainerParticipant) event.getParticipant();
            TrainerSkill skill = NuzlockeConfigProxy.getNuzlocke().isSmartTrainers();
            if (skill != TrainerSkill.STANDARD) {
                BattleAIBase trueAI = event.getAI();
                switch(skill) {
                    case ADVANCED:
                        trueAI = new AdvancedAI(trainer);
                        break;
                    case AGGRESSIVE:
                        trueAI = new AggressiveAI(trainer);
                        break;
                    case TACTICAL:
                        trueAI = new TacticalAI(trainer);
                        break;
                    case MIXED:
                        if (RandomHelper.getRandomChance(33)) { trueAI = new AggressiveAI(trainer); }
                        else if (RandomHelper.getRandomChance()) { trueAI = new AdvancedAI(trainer); }
                        else { trueAI = new TacticalAI(trainer); }
                        break;
                }
                event.setAI(trueAI);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void aggressionBooster(SpawnEvent event) {
        if (event.spawner instanceof PlayerTrackingSpawner) {
            if (event.action.getOrCreateEntity() instanceof PixelmonEntity) {
                PixelmonEntity pokemon = (PixelmonEntity) event.action.getOrCreateEntity();
                PokemonAggression aggression = NuzlockeConfigProxy.getNuzlocke().getPokemonAggression();
                PlayerTrackingSpawner playerTrackingSpawner = (PlayerTrackingSpawner)event.spawner;
                ServerPlayerEntity player = playerTrackingSpawner.getTrackedPlayer();

                if (pokemon.isBossPokemon()) {
                    return;
                }

                switch (aggression) {
                    case PASSIVE:
                        pokemon.setAggression(Aggression.PASSIVE);
                        break;
                    case TIMID:
                        pokemon.setAggression(Aggression.TIMID);
                        break;
                    case AGGRESSIVE:
                    case ENCOUNTER:
                        if (player != null && !NuzlockeServerPlayerData.isNuzlockeEnabled(player.getUUID())) {
                            return;
                        }

                        pokemon.setAggression(Aggression.AGGRESSIVE);
                        pokemon.setTarget(player);
                        pokemon.goalSelector.addGoal(-2, new ExecuteActionGoal(pokemon));
                        pokemon.goalSelector.addGoal(-1, new HuntGoal(pokemon, aggression));
                        break;
                    case STANDARD:
                        break;
                }
            }
        }
    }

    public static boolean participantInNuzlocke(List<BattleParticipant> participants) {
        for (BattleParticipant part : participants) {
            if (part instanceof PlayerParticipant) {
                if (NuzlockeServerPlayerData.isNuzlockeEnabled(((PlayerParticipant) part).player.getUUID())) {
                    return true;
                }
            }
        }
        return false;
    }
}
