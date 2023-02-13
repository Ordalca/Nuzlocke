package me.ordalca.nuzlocke.battles;

import com.pixelmonmod.pixelmon.api.battles.BagSection;
import com.pixelmonmod.pixelmon.api.events.battles.BagItemEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.items.ItemData;
import me.ordalca.nuzlocke.captures.NuzlockePlayerData;
import me.ordalca.nuzlocke.commands.NuzlockeConfigProxy;
import me.ordalca.nuzlocke.commands.NuzlockeConfig.*;
import me.ordalca.nuzlocke.captures.OutOfBattleCatchControl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class BagUsageHandler {
    private static BagUsageHandler handler = null;
    BagUsageHandler() {}
    public static BagUsageHandler getInstance() {
        if (handler == null) {
            handler = new BagUsageHandler();
        }
        return handler;
    }
    @SubscribeEvent
    public void onBagUse(BagItemEvent.CollectItems.Pre event) {
        if (!NuzlockePlayerData.isNuzlockeEnabled(event.getPlayer().getUUID()))
            return;

        List<ItemData> items = event.getItems();
        if (NuzlockeConfigProxy.getNuzlocke().preventMasterBallUse() ||
                NuzlockeConfigProxy.getNuzlocke().bagRestrictions() != BagUse.UNRESTRICTED) {
            for (int idx = items.size() - 1; idx >= 0; idx--) {
                ItemStack stack = items.get(idx).getItemStack();
                if (NuzlockeConfigProxy.getNuzlocke().preventMasterBallUse() &&
                        OutOfBattleCatchControl.stackHasMasterBall(stack)) {
                    items.remove(idx);
                } else if (NuzlockeConfigProxy.getNuzlocke().bagRestrictions() == BagUse.NOITEMS && violatesNoItems(stack)) {
                    items.remove(idx);
                } else if (NuzlockeConfigProxy.getNuzlocke().bagRestrictions() == BagUse.NOHEALS && violatesNoHeals(stack)) {
                    items.remove(idx);
                }
            }
        }
        for (int idx = items.size() - 1; idx >= 0; idx--) {
            Item item = items.get(idx).getItemStack().getItem();
            if (item.equals(PixelmonItems.revive) ||
                    item.equals(PixelmonItems.max_revive) ||
                    item.equals(PixelmonItems.revival_herb) ||
                    item.equals(PixelmonItems.sacredash)) {
                items.remove(idx);
            }
        }
        event.setItems(items);
    }
    private boolean violatesNoItems(ItemStack stack) {
        return ! (BagSection.POKEBALLS.isItem(stack));
    }
    private boolean violatesNoHeals(ItemStack stack) {
        boolean valid = (BagSection.POKEBALLS.isItem(stack) || BagSection.BATTLE_ITEMS.isItem(stack));
        return !valid;
    }
}
