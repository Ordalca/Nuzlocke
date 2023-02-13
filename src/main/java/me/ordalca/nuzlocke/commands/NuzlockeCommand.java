package me.ordalca.nuzlocke.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.TickHandler;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.storage.breeding.DayCareBox;
import com.pixelmonmod.pixelmon.api.storage.breeding.PlayerDayCare;
import com.pixelmonmod.pixelmon.command.PixelCommand;

import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.captures.NuzlockePlayerData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import com.google.common.collect.Lists;
import java.util.List;


public class NuzlockeCommand extends PixelCommand {
    public NuzlockeCommand(CommandDispatcher<CommandSource> dispatcher) {
        super(dispatcher, "nuzlocke", "/nuzlocke <command>", 2);
    }
    @Override
    public void execute(CommandSource sender, String[] args) throws CommandException {
        ServerPlayerEntity player = PixelmonCommandUtils.requireEntityPlayer(sender);
        if (player == null) {
            PixelmonCommandUtils.endCommand("argument.entity.notfound.player", args[0]);
        }

        String command = args[0];
        if (command.equalsIgnoreCase("begin")) {
            if (NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
                NuzlockePlayerData data = (NuzlockePlayerData)StorageProxy.getParty(player.getUUID()).playerData;
                data.nuzlockeEnabled = true;
            }
        } else if (command.equalsIgnoreCase("cancel")) {
            if (NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
                NuzlockePlayerData data = (NuzlockePlayerData)StorageProxy.getParty(player.getUUID()).playerData;
                data.nuzlockeEnabled = false;
            }
        }
        else if (command.equalsIgnoreCase("reset")) {
            String confirmCommand = "\"/nuzlocke reset confirm\"";
            if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                ModFile.LOGGER.debug("runReset");
                resetNuzlocke(player);
            } else {
                String confirmMessage = "This removes all pokemon from party and pc.  To verify, enter "+confirmCommand;
                PixelmonCommandUtils.sendMessage(player, confirmMessage);
                ModFile.LOGGER.debug("confirm"+confirmMessage);
            }
        } else if (command.equalsIgnoreCase("blocks")) {
            printBlocked(player);
        } else if (command.equalsIgnoreCase("clear")) {
            clearBlocks(player);
        } else if (command.equalsIgnoreCase("revive")) {
            clearDeath(player);
        }
    }
    public void resetNuzlocke(ServerPlayerEntity player) {
        // reset blocked biomes
        clearBlocks(player);

        // reset daycare
        PlayerPartyStorage storage = PixelmonCommandUtils.require(PixelmonCommandUtils.getPlayerStorage(player), "pixelmon.command.general.invalidplayer");
        PlayerDayCare daycare = storage.getDayCare();
        for (int idx = 0; idx < daycare.getAllowedBoxes(); idx++) {
            daycare.getBox(idx).ifPresent(DayCareBox::empty);
        }

        // remove party
        for (int idx = 0; idx < 6; idx++) {
            storage.set(idx, null);
        }

        // empty pc
        PCStorage pcStorage = StorageProxy.getPCForPlayer(player);
        for (int box = 0; box < pcStorage.getBoxCount(); box++) {
            for (int slot = 0; slot < 30; slot++) {
                pcStorage.set(box, slot,null);
            }
        }

        // Reset Pokedex
        storage.playerPokedex.wipe();

        // Rechoose starter
        storage.starterPicked = false;
        TickHandler.registerStarterList(player);
    }
    public void clearBlocks(ServerPlayerEntity player) {
        // reset blocked biomes
        PlayerPartyStorage party = StorageProxy.getParty(player.getUUID());
        NuzlockePlayerData ndata = (NuzlockePlayerData) party.playerData;
        ndata.blockedBiomes.clear();
        player.getPersistentData().put(NuzlockePlayerData.nbtKey, ndata.saveAsCompoundNBT());
    }

    public void printBlocked(ServerPlayerEntity player) {
        // reset blocked biomes
        PlayerPartyStorage party = StorageProxy.getParty(player.getUUID());
        NuzlockePlayerData ndata = (NuzlockePlayerData) party.playerData;
        if (ndata.blockedBiomes.size() > 0) {
            PixelmonCommandUtils.sendMessage(player, "Blocked biomes: "+ndata.blockedBiomes.keySet());
        } else {
            PixelmonCommandUtils.sendMessage(player, "No Blocked biomes");
        }
    }

    public void clearDeath(ServerPlayerEntity player) {
        // reset blocked biomes
        PlayerPartyStorage party = StorageProxy.getParty(player.getUUID());
        for (Pokemon pokemon : party.getTeam()) {
            if (pokemon.getPersistentData().getBoolean("dead")) {
                pokemon.getPersistentData().remove("dead");
            }
            pokemon.setHealthPercentage(100F);
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, CommandSource sender, String[] args, BlockPos pos) {
        List<String> list = Lists.newArrayList();
        if (args.length == 1) {
            if (NuzlockeConfigProxy.getNuzlocke().isPermissionRequired()) {
                list.add("begin");
                list.add("cancel");
            }
            list.add("blocks");
            list.add("reset");
        }
        return list;
    }
}
