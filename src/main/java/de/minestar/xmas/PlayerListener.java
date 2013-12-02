package de.minestar.xmas;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.minestar.minestarlibrary.utils.PlayerUtils;
import de.minestar.xmas.data.BlockVector;
import de.minestar.xmas.data.XMasDay;

public class PlayerListener implements Listener {

    private BlockVector[] vectorList = new BlockVector[4];
    private BlockVector vector = new BlockVector("", 0, 0, 0);
    private DateFormat dayFormat = new SimpleDateFormat("dd");
    private DateFormat monthFormat = new SimpleDateFormat("MM");

    public PlayerListener() {
        this.vectorList[0] = new BlockVector("", 0, 0, 0);
        this.vectorList[1] = new BlockVector("", 0, 0, 0);
        this.vectorList[2] = new BlockVector("", 0, 0, 0);
        this.vectorList[3] = new BlockVector("", 0, 0, 0);
    }

    public void updateAdjacent(BlockVector vector) {
        this.vectorList[0].update(vector.getWorldName(), vector.getX(), vector.getY(), vector.getZ() + 1);
        this.vectorList[1].update(vector.getWorldName(), vector.getX(), vector.getY(), vector.getZ() - 1);
        this.vectorList[2].update(vector.getWorldName(), vector.getX() + 1, vector.getY(), vector.getZ());
        this.vectorList[3].update(vector.getWorldName(), vector.getX() - 1, vector.getY(), vector.getZ());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if ((!block.getType().equals(Material.STONE_BUTTON) && !block.getType().equals(Material.WOOD_BUTTON))) { // || block.getData() < 1 || block.getData() > 4) {
            return;
        }

        Date date = new Date();
        int nowDay = Integer.valueOf(dayFormat.format(date));
        int month = Integer.valueOf(monthFormat.format(date));

        if (player.isOp()) {
            month = 12;
            vector.update(block.getLocation());
            XMasDay override = XMASCore.getDayByButton(vector);
            if (override == null) {
                return;
            } else {
                override.dispenseItemsOP();
            }
            return;
        }

        if (month != 12) {
            return;
        }
        if (nowDay < 1 || nowDay > 24) {
            return;
        }

        vector.update(block.getLocation());
        XMasDay day = XMASCore.getDayByButton(vector);
        if (day == null) {
            return;
        }

        if (day.getDay() > nowDay || nowDay < day.getDay() - 3) {
            return;
        }

        // check if button != null ?
        if (player.isOp()) {
            day.dispenseItemsOP();
        } else if (day.hasPlayer(player.getName())) {
            this.punishPlayer(player);
        } else {
            if (day.addPlayer(player.getName())) {
                day.dispenseItems();
            }
        }
        return;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        vector.update(block.getLocation());
        if (block.getType().equals(Material.DISPENSER) || block.getType().equals(Material.DROPPER)) {
            for (XMasDay day : XMASCore.dayMapByDate.values()) {
                if (vector.equals(day.getDispenserPos())) {
                    if (player.isOp()) {
                        day.setDispenserPos(null);
                        PlayerUtils.sendInfo(player, XMASCore.NAME, "Dispenser/Dropper unregistered!");
                        return;
                    } else {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        } else if (block.getType().equals(Material.STONE_BUTTON) || block.getType().equals(Material.WOOD_BUTTON)) {
            // remove a registered button
            for (XMasDay day : XMASCore.dayMapByDate.values()) {
                if (day.hasButtonPos(vector)) {
                    if (player.isOp()) {
                        if (day.removeButtonPos(vector)) {
                            PlayerUtils.sendInfo(player, XMASCore.NAME, "Button unregistered!");
                        } else {
                            PlayerUtils.sendError(player, XMASCore.NAME, "Failed to remove button from database!");
                        }
                        return;
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else {
            this.updateAdjacent(vector);
            for (BlockVector other : this.vectorList) {
                for (XMasDay day : XMASCore.dayMapByDate.values()) {
                    if (day.hasButtonPos(other)) {
                        if (player.isOp()) {
                            day.removeButtonPos(other);
                            PlayerUtils.sendInfo(player, XMASCore.NAME, "Button unregistered!");
                            continue;
                        } else {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void punishPlayer(Player player) {
        // USER WAS ON LIST = PUNISH PLAYER
        Random rand = new Random();
        double x = rand.nextDouble();
        if (x <= 0.2) {
            player.setHealth(20.0);
            player.getWorld().strikeLightning(player.getLocation());
            player.setHealth(20.0);
        } else {
            if (x >= 0.9) {
                player.sendMessage(ChatColor.RED + "HODOR!");
            } else if (x >= 0.8) {
                player.sendMessage(ChatColor.RED + "Du nicht nehmen Kerze!");
            } else if (x >= 0.7) {
                player.sendMessage(ChatColor.RED + "Hit me baby, one more time!");
            } else if (x >= 0.6) {
                player.sendMessage(ChatColor.RED + "Push the button, don't push the button.");
            } else if (x >= 0.5) {
                player.sendMessage(ChatColor.RED + "Das sind keine Hupen! *möööööp*");
            } else if (x >= 0.4) {
                player.sendMessage(ChatColor.RED + "Du musst fester drücken!");
            } else if (x >= 0.3) {
                player.sendMessage(ChatColor.RED + "Drück mich härter!");
            } else if (x > 0.2) {
                player.sendMessage(ChatColor.RED + "Junge... so ein Feuerball.... BÄM!");
            }
            player.sendMessage(ChatColor.GRAY + "Netter Versuch. Komm morgen wieder...");
        }
    }

}
