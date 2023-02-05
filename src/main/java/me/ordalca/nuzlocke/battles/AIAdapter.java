package me.ordalca.nuzlocke.battles;

import com.pixelmonmod.pixelmon.ai.ExecuteActionGoal;
import com.pixelmonmod.pixelmon.ai.MoveTowardsTargetGoal;
import com.pixelmonmod.pixelmon.ai.TargetGoal;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.SetBattleAIEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.species.aggression.Aggression;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.battles.controller.ai.AdvancedAI;
import com.pixelmonmod.pixelmon.battles.controller.ai.AggressiveAI;
import com.pixelmonmod.pixelmon.battles.controller.ai.BattleAIBase;
import com.pixelmonmod.pixelmon.battles.controller.ai.TacticalAI;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.spawning.PlayerTrackingSpawner;

import me.ordalca.nuzlocke.captures.BiomeBlocker;
import me.ordalca.nuzlocke.captures.NuzlockePlayerData;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.TrainerSkill;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.PokemonAggression;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AIAdapter {
    private static AIAdapter handler = null;
    AIAdapter() {}
    public static AIAdapter getInstance() {
        if (handler == null) {
            handler = new AIAdapter();
        }
        return handler;
    }

    @SubscribeEvent
    public void npcPokemonBooster(BattleStartedEvent event) {
        if (NuzlockeConfigProxy.getNuzlocke().isEliteTrainerPokemon()) {
            for (BattleParticipant part : event.participant2) {
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
    public void npcAIChooser(SetBattleAIEvent event) {
        if (event.participant instanceof TrainerParticipant) {
            TrainerParticipant trainer = (TrainerParticipant) event.participant;
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
    public void aggressionBooster(SpawnEvent event) {
        if (event.spawner instanceof PlayerTrackingSpawner) {
            if (event.action.getOrCreateEntity() instanceof PixelmonEntity) {
                PixelmonEntity pokemon = (PixelmonEntity) event.action.getOrCreateEntity();
                PokemonAggression aggression = NuzlockeConfigProxy.getNuzlocke().getPokemonAggression();
                ServerPlayerEntity player = ((PlayerTrackingSpawner) event.spawner).getTrackedPlayer();

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
}
