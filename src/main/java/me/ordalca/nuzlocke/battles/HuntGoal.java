package me.ordalca.nuzlocke.battles;

import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.storage.playerData.PlayerData;
import me.ordalca.nuzlocke.captures.BiomeBlocker;
import me.ordalca.nuzlocke.captures.NuzlockePlayerData;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.PokemonAggression;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.pathfinding.Path;

import java.util.EnumSet;

public class HuntGoal extends Goal {
    private final PixelmonEntity theEntity;
    private final PokemonAggression aggression;
    private NuzlockePlayerData data;
    private String biome;
    private Path path;

    public HuntGoal(PixelmonEntity par1EntityCreature, PokemonAggression aggression) {
        this.theEntity = par1EntityCreature;
        this.aggression = aggression;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.TARGET));

        if (this.biome == null) {
            if (this.theEntity.getPersistentData().contains(BiomeBlocker.biomeKey)) {
                this.biome = this.theEntity.getPersistentData().getString(BiomeBlocker.biomeKey);
            }
        }
    }

    public boolean canUse() {
        if (this.theEntity.getTarget() == null) {
            return false;
        } else {
            if (this.theEntity.getAggressionTimer() > 0) {
                return false;
            }

            if (this.theEntity.battleController != null) {
                return false;
            }

            if (this.theEntity.getOwner() != null && BattleRegistry.getBattle((ServerPlayerEntity) this.theEntity.getOwner()) != null) {
                this.theEntity.setTarget(null);
                return false;
            }
        }

        LivingEntity targetEntity = this.theEntity.getTarget();

        if (!(targetEntity instanceof PlayerEntity)) {
            return false;
        }

        if (this.data == null) {
            PlayerData playerData = StorageProxy.getParty(targetEntity.getUUID()).playerData;
            if (playerData instanceof NuzlockePlayerData) {
                this.data = (NuzlockePlayerData) playerData;
            } else {
                return false;
            }
        }

        if (!this.data.nuzlockeEnabled) {
            return false;
        }

        if (aggression == PokemonAggression.ENCOUNTER) {
            if (this.data.isBiomeBlocked(this.theEntity.getStringUUID(), this.biome)) {
                this.theEntity.setTarget(null);
                return false;
            }
        }

        this.path = this.theEntity.getNavigation().createPath(targetEntity, 0);
        return (this.path != null);
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = this.theEntity.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else {
            return !this.theEntity.getNavigation().isDone();
        }
    }

    public void stop() {
        this.path = null;
    }

    public void start() {
        this.theEntity.getNavigation().moveTo(this.path, (double)((float)this.theEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));
        this.theEntity.setAggressive(true);
    }
}
