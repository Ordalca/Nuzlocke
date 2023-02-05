package me.ordalca.nuzlocke.commands;

import com.pixelmonmod.pixelmon.api.config.api.data.ConfigPath;
import com.pixelmonmod.pixelmon.api.config.api.yaml.AbstractYamlConfig;
import info.pixelmon.repack.org.spongepowered.objectmapping.ConfigSerializable;

@ConfigSerializable
@ConfigPath("config/pixelmon/nuzlocke.yml")
public class NuzlockeConfig extends AbstractYamlConfig{
    public enum FaintResult {
        FAINT,
        DELETE,
        DEAD
    }
    public enum WipeResult {
        NONE,
        TELEPORT,
        DEATH
    }
    public enum BagUse {
        UNRESTRICTED,
        NOHEALS,
        NOITEMS
    }
    public enum TrainerSkill {
        STANDARD,
        MIXED,
        TACTICAL,
        ADVANCED,
        AGGRESSIVE
    }
    public enum PokemonAggression {
        STANDARD,
        TIMID,
        PASSIVE,
        ENCOUNTER,
        AGGRESSIVE
    }

    private boolean firstEncounterRestriction = true;
    private boolean dupesClause = true;
    private boolean shinyClause = false;

    private boolean pvpDeathEnforced = false;
    private FaintResult faintResult = FaintResult.FAINT;
    private WipeResult wipeResult = WipeResult.NONE;
    private BagUse bagUse = BagUse.UNRESTRICTED;

    private boolean blockMasterBall = false;
    private boolean blockInBattleStronger = false;
    private int strongerThreshold = 5;
    private OutOfBattleRestrictions outOfBattle = new OutOfBattleRestrictions();

    private boolean eliteTrainerPokemon = false;
    private TrainerSkill trainerSkill = TrainerSkill.STANDARD;
    private PokemonAggression pokemonAggression = PokemonAggression.STANDARD;

    public boolean isFirstEncounterRestricted() { return this.firstEncounterRestriction; }
    public boolean isDupesClauseActive() { return this.dupesClause; }
    public boolean isShinyClauseActive() { return this.shinyClause; }

    public boolean isPVPDeathEnforced() { return this.pvpDeathEnforced; }
    public FaintResult pokemonFaintingPenalty() { return this.faintResult; }
    public WipeResult trainerWipePenalty() { return this.wipeResult; }
    public BagUse bagRestrictions() { return this.bagUse; }

    public boolean preventMasterBallUse() { return this.blockMasterBall; }
    public boolean preventCatchingStrongerPokemonInBattle() { return this.blockInBattleStronger; }
    public int permittedLevelDifferenceToCatch() { return this.strongerThreshold; }
    public OutOfBattleRestrictions getOutOfBattleRestrictions() { return this.outOfBattle; }

    public boolean isEliteTrainerPokemon() { return this.eliteTrainerPokemon; }
    public TrainerSkill isSmartTrainers() { return this.trainerSkill; }
    public PokemonAggression getPokemonAggression() { return this.pokemonAggression; }
    @ConfigSerializable
    public static class OutOfBattleRestrictions {
        private boolean blockOutOfBattleLegends = false;
        private boolean blockOutOfBattleMythical = false;
        private boolean blockOutOfBattleUltra = false;
        private boolean blockOutOfBattleStronger = false;

        public boolean preventCatchingLegendsOutOfBattle() { return this.blockOutOfBattleLegends; }
        public boolean preventCatchingMythicalOutOfBattle() { return this.blockOutOfBattleMythical; }
        public boolean preventCatchingUltraBeastsOutOfBattle() { return this.blockOutOfBattleUltra; }
        public boolean preventCatchingStrongerPokemonOutOfBattle() { return this.blockOutOfBattleStronger; }
    }
}