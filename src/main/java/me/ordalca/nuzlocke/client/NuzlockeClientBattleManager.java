package me.ordalca.nuzlocke.client;

import com.pixelmonmod.pixelmon.client.ClientProxy;
import com.pixelmonmod.pixelmon.client.gui.battles.ClientBattleManager;
import com.pixelmonmod.pixelmon.client.gui.battles.PixelmonClientData;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;

import java.util.*;

public class NuzlockeClientBattleManager extends ClientBattleManager {
    public NuzlockeClientBattleManager() {
        ClientProxy.battleManager = this;
    }

    @Override
    public boolean canCatchOpponent() {
        if (super.canCatchOpponent()) {
            if (NuzlockeClientPlayerData.isNuzlockeEnabled()) {
                if (notBlockedByBiome() && notBlockedByLevel()) {
                    return true;
                } else return permittedByShinyClause();
            } else {
                ModFile.LOGGER.debug("nuzlocke not enabled on client");
                return true;
            }
        }
        return false;
    }

    public boolean notBlockedByBiome() {
        if (!NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted()) return true;
        else {
            for (PixelmonClientData clientData : this.displayedEnemyPokemon) {
                if (!(NuzlockeClientPlayerData.isBiomeBlocked(clientData.pokemonUUID.toString()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean notBlockedByLevel() {
        return !(NuzlockeConfigProxy.getNuzlocke().preventCatchingStrongerPokemonInBattle() && opponentIsHigherLevel());
    }
    private boolean opponentIsHigherLevel() {
        int maxLevel = 0;
        for (PixelmonClientData playerPoke : this.displayedOurPokemon) {
            if (playerPoke.level > maxLevel)
                maxLevel = playerPoke.level;
        }
        for (PixelmonClientData wild : this.displayedEnemyPokemon) {
            if (wild.level <= maxLevel + NuzlockeConfigProxy.getNuzlocke().permittedLevelDifferenceToCatch()) {
                return false;
            }
        }
        return true;
    }

    // if not first catch, but is shiny, then catching is fine
    public boolean permittedByShinyClause() {
        return (NuzlockeConfigProxy.getNuzlocke().isShinyClauseActive() && opponentIsShiny());
    }
    private boolean opponentIsShiny() {
        for(int i = 0; i < this.battleSetup[1].length; ++i) {
            if(this.displayedEnemyPokemon[i].palette.toLowerCase(Locale.ENGLISH).contains("shiny")) {
                return true;
            }
        }
        return false;
    }
}