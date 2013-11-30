package de.minestar.xmas;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.minestar.minestarlibrary.utils.PlayerUtils;
import de.minestar.xmas.data.BlockVector;
import de.minestar.xmas.data.XMasDay;

public class AdminListener implements Listener {

    public static HashMap<String, Integer> adminMap = new HashMap<String, Integer>();

    public AdminListener() {
        adminMap = new HashMap<String, Integer>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack itemInHand = player.getItemInHand();

        if (itemInHand == null || !itemInHand.getType().equals(Material.BEACON)) {
            return;
        }

        int dayN = getDay(player);
        if (dayN < 1 || dayN > 24) {
            return;
        }

        if (block.getType().equals(Material.DISPENSER) || block.getType().equals(Material.DROPPER)) {
            XMasDay day = XMASCore.getDayByDate(dayN);

            day.setDispenserPos(new BlockVector(block.getLocation()));
            if (block.getType().equals(Material.DISPENSER)) {
                day.updateItems((Dispenser) block.getState());
                PlayerUtils.sendSuccess(player, XMASCore.NAME, "Dispenser for day " + dayN + " set!");
            } else {
                day.updateItems((Dropper) block.getState());
                PlayerUtils.sendSuccess(player, XMASCore.NAME, "Dropper for day " + dayN + " set!");
            }

            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            return;
        } else if (block.getType().equals(Material.STONE_BUTTON) || block.getType().equals(Material.WOOD_BUTTON)) {
            XMasDay day = XMASCore.getDayByDate(dayN);

            BlockVector buttonPos = new BlockVector(block.getLocation());
            day.addButtonPos(buttonPos);
            XMASCore.registerBlock(buttonPos, day);
            PlayerUtils.sendSuccess(player, XMASCore.NAME, "Buttonposition for day " + dayN + " set!");

            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            return;
        }
    }

    public static int getDay(Player player) {
        int day = 0;
        if (adminMap.containsKey(player.getName())) {
            day = adminMap.get(player.getName());
        }
        return day;
    }
}
