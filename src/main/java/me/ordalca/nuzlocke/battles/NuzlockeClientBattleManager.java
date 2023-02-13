package me.ordalca.nuzlocke.battles;

import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.client.ClientProxy;
import com.pixelmonmod.pixelmon.client.gui.battles.ClientBattleManager;
import com.pixelmonmod.pixelmon.client.gui.battles.PixelmonClientData;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.captures.NuzlockePlayerData;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;

import java.util.*;

public class NuzlockeClientBattleManager extends ClientBattleManager {
    public NuzlockeClientBattleManager() {
        ClientProxy.battleManager = this;
    }

    @Override
    public boolean canCatchOpponent() {
        if (super.canCatchOpponent()) {
            NuzlockePlayerData playerData = (NuzlockePlayerData) StorageProxy.getParty(this.getViewPlayer().getUUID()).playerData;
            if (playerData.nuzlockeEnabled) {
                if (notBlockedByBiome() && notBlockedByLevel()) {
                    return true;
                } else return permittedByShinyClause();
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean notBlockedByBiome() {
        if (!NuzlockeConfigProxy.getNuzlocke().isFirstEncounterRestricted()) return true;
        else {
            NuzlockePlayerData playerData = (NuzlockePlayerData) StorageProxy.getParty(this.getViewPlayer().getUUID()).playerData;
            for (PixelmonClientData clientData : this.displayedEnemyPokemon) {
                if (!(playerData.isBiomeBlocked(clientData.pokemonUUID.toString()))) {
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

    public void updateOurPokemon(List<PixelmonWrapper> displayed, List<PixelmonWrapper> fullTeam) {
        UUID[] clientDisplay = new UUID[displayed.size()];
        for (int idx = 0; idx < displayed.size(); idx++) {
            PixelmonWrapper wrapper = displayed.get(idx);
            clientDisplay[idx] = wrapper.getPokemonUUID();
        }
        PixelmonClientData[] clientTeam = new PixelmonClientData[fullTeam.size()];
        for (int idx = 0; idx < fullTeam.size(); idx++) {
            PixelmonWrapper wrapper = fullTeam.get(idx);
            clientTeam[idx] = new PixelmonClientData(wrapper);
        }
        this.setFullTeamData(clientTeam);
        this.setTeamPokemon(clientDisplay);
    }
}