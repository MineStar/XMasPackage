package de.minestar.clashofkingdoms.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.utils.BlockUtils;

import de.minestar.clashofkingdoms.COKCore;
import de.minestar.clashofkingdoms.classes.EnumPlayerClass;
import de.minestar.clashofkingdoms.classes.PlayerClass;
import de.minestar.clashofkingdoms.data.COKGame;
import de.minestar.clashofkingdoms.enums.EnumTeam;
import de.minestar.clashofkingdoms.manager.GameManager;
import de.minestar.clashofkingdoms.utils.BlockVector;

public class GameListener implements Listener {

    private static final Set<Integer> nonPushableBlocks = new HashSet<Integer>(Arrays.asList(0, 6, 7, 8, 9, 10, 11, 23, 26, 30, 31, 32, 34, 37, 38, 39, 40, 50, 51, 52, 55, 59, 61, 62, 63, 64, 65, 68, 69, 70, 71, 72, 75, 76, 77, 81, 83, 84, 86, 90, 91, 92, 93, 94, 96, 103, 104, 105, 106, 111, 115, 116, 117, 119, 120, 122, 127, 130, 131, 132, Material.FLOWER_POT.getId()));

    private static final String BLOCK_PLACE = ChatColor.GOLD + "[COK] %s" + ChatColor.WHITE + " placed a block at the base of %s!";

    private GameManager gameManager;
    private BlockVector vector = new BlockVector("", 0, 0, 0);

    public GameListener() {
        this.gameManager = COKCore.gameManager;
    }

    // ///////////////////////////////////
    //
    // PLAYER RELATED
    //
    // ///////////////////////////////////

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        COKGame game = this.gameManager.getGameByPlayer(player.getName());

        // game must be running
        if (game.isStopped()) {
            return;
        }

        this.vector.update(event.getBlock().getLocation());
        if (game.isBaseBlock(EnumTeam.RED, vector) || game.isBaseBlock(EnumTeam.BLU, vector)) {
            // referees can always break on bases
            if (!game.getPlayer(player.getName()).isInTeam(EnumTeam.REF)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        COKGame game = this.gameManager.getGameByPlayer(player.getName());

        // game must be running or be paused
        if (game.isStopped()) {
            return;
        }

        // some vars...
        this.vector.update(event.getBlock().getLocation());
        final EnumTeam team = game.getPlayer(player.getName()).getTeam();

        // handle the blockplace
        switch (team) {
            case RED : {
                // RED cannot place on RED-Base
                if (game.isPaused() || game.isBaseBlock(EnumTeam.RED, vector)) {
                    event.setBuild(false);
                    event.setCancelled(true);
                    return;
                }

                // handle blockplace on BLU BASE
                if (game.isRunning() && game.isBaseBlock(EnumTeam.BLU, vector)) {
                    if (event.getBlock().getTypeId() == game.getSettings().getBaseTypeID() && event.getBlock().getData() == game.getSettings().getBaseSubID()) {
                        game.sendMessageToAll(String.format(BLOCK_PLACE, (ChatColor.RED + player.getName()), EnumTeam.BLU.getFullTeamName(ChatColor.WHITE)));
                        game.checkForWinner(EnumTeam.BLU);
                    } else {
                        this.gameManager.getPlayer(player.getName()).sendMessage(ChatColor.RED + "Wrong blocktype!");
                        event.setBuild(false);
                        event.setCancelled(true);
                    }
                    return;
                }
            }
            case BLU : {
                // BLU cannot place on BLU-Base
                if (game.isPaused() || game.isBaseBlock(EnumTeam.BLU, vector)) {
                    event.setBuild(false);
                    event.setCancelled(true);
                    return;
                }
                // handle blockplace on RED BASE
                if (game.isRunning() && game.isBaseBlock(EnumTeam.RED, vector)) {
                    if (event.getBlock().getTypeId() == game.getSettings().getBaseTypeID() && event.getBlock().getData() == game.getSettings().getBaseSubID()) {
                        game.sendMessageToAll(String.format(BLOCK_PLACE, (ChatColor.BLUE + player.getName()), EnumTeam.RED.getFullTeamName(ChatColor.WHITE)));
                        game.checkForWinner(EnumTeam.RED);
                    } else {
                        this.gameManager.getPlayer(player.getName()).sendMessage(ChatColor.RED + "Wrong blocktype!");
                        event.setBuild(false);
                        event.setCancelled(true);
                    }
                    return;
                }
            }
            case REF : {
                // referees can always place

                // handle blockplace on bases
                if (game.isBaseBlock(EnumTeam.RED, vector)) {
                    if (event.getBlock().getTypeId() == game.getSettings().getBaseTypeID() && event.getBlock().getData() == game.getSettings().getBaseSubID()) {
                        game.sendMessageToAll(String.format(BLOCK_PLACE, (ChatColor.DARK_GRAY + player.getName()), EnumTeam.RED.getFullTeamName(ChatColor.WHITE)));
                        game.checkForWinner(EnumTeam.RED);
                    } else {
                        this.gameManager.getPlayer(player.getName()).sendMessage(ChatColor.RED + "Wrong blocktype!");
                        event.setBuild(false);
                        event.setCancelled(true);
                    }
                } else if (game.isBaseBlock(EnumTeam.BLU, vector)) {
                    if (event.getBlock().getTypeId() == game.getSettings().getBaseTypeID() && event.getBlock().getData() == game.getSettings().getBaseSubID()) {
                        game.sendMessageToAll(String.format(BLOCK_PLACE, (ChatColor.DARK_GRAY + player.getName()), EnumTeam.BLU.getFullTeamName(ChatColor.WHITE)));
                        game.checkForWinner(EnumTeam.BLU);
                    } else {
                        this.gameManager.getPlayer(player.getName()).sendMessage(ChatColor.RED + "Wrong blocktype!");
                        event.setBuild(false);
                        event.setCancelled(true);
                    }
                }
                return;
            }
            default : {
                event.setBuild(false);
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // only blockchanges
        if (BlockUtils.LocationEquals(event.getFrom(), event.getTo())) {
            return;
        }

        // is the player in a game?
        Player player = event.getPlayer();
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        // cancel movement, if the game is paused
        COKGame game = this.gameManager.getGameByPlayer(player.getName());

        if (game.isRunning() || game.isStopped()) {
            return;
        }

        if (game.isPaused() && !game.getPlayer(player.getName()).isInTeam(EnumTeam.REF)) {
            event.setTo(event.getFrom().clone());
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        COKGame game = this.gameManager.getGameByPlayer(player.getName());
        game.onPlayerDisconnect(player.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        // cancel itemdropping of armor
        COKGame game = this.gameManager.getGameByPlayer(player.getName());
        if (game.getPlayer(player.getName()).getPlayerClass() != null) {
            String name = "";
            ItemStack stack = null;
            for (int index = event.getDrops().size() - 1; index >= 0; index--) {
                stack = event.getDrops().get(index);
                name = stack.getType().name().toLowerCase();
                if (name.contains("helmet") || name.contains("chestplate") || name.contains("leggings") || name.contains("boots")) {
                    event.getDrops().remove(index);
                }
            }
        }
        game.onPlayerDeath(player.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getEntity();

            // is the player in a game?
            if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
                return;
            }

            // cancel damage, if not a player
            if (this.gameManager.getPlayer(player.getName()).isInTeam(EnumTeam.SPEC) || this.gameManager.getPlayer(player.getName()).isInTeam(EnumTeam.REF)) {
                event.setDamage(0);
                event.setCancelled(true);
                return;
            }
        } else if (event.getDamager().getType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getDamager();

            // is the player in a game?
            if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
                return;
            }

            // cancel damage, if not a player
            if (this.gameManager.getPlayer(player.getName()).isInTeam(EnumTeam.SPEC) || this.gameManager.getPlayer(player.getName()).isInTeam(EnumTeam.REF)) {
                event.setDamage(0);
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        // teleport to teamspawn
        COKGame game = this.gameManager.getGameByPlayer(player.getName());
        EnumTeam team = game.getPlayer(player.getName()).getTeam();
        if (game.isStopped()) {
            team = EnumTeam.SPEC;
        } else {
            if (team.equals(EnumTeam.RED) || team.equals(EnumTeam.BLU)) {
                // get new classes
                for (PlayerClass playerClass : game.getSettings().getPlayerClassList()) {

                    if (playerClass.getClassName().equalsIgnoreCase(EnumPlayerClass.REFEREE.getClassName())) {
                        continue;
                    }

                    if (!playerClass.isEnabled()) {
                        continue;
                    }

                    if (game.getTeamData(team).getCurrentClass(EnumPlayerClass.byType(playerClass.getClassName())) != null) {
                        continue;
                    }

                    game.getRandomizedPlayerClass(team, playerClass);
                }

            }
        }

        if (game.getTeamData(team).getSpawn() != null) {
            game.getPlayer(player.getName()).getBukkitPlayer().teleport(game.getTeamData(team).getSpawn());
        } else {
            if (game.getTeamData(EnumTeam.SPEC).getSpawn() != null) {
                game.getPlayer(player.getName()).getBukkitPlayer().teleport(game.getTeamData(EnumTeam.SPEC).getSpawn());
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        // disallow itempickups for spectators
        COKGame game = this.gameManager.getGameByPlayer(player.getName());
        if (game.getPlayer(player.getName()).isInTeam(EnumTeam.SPEC)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        // teleport to teamspawn
        COKGame game = this.gameManager.getGameByPlayer(player.getName());
        if (game.getPlayer(player.getName()).isInTeam(EnumTeam.SPEC)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // is the player in a game?
        if (!this.gameManager.isPlayerInAnyGame(player.getName())) {
            return;
        }

        // disallow itemdrops for spectators
        COKGame game = this.gameManager.getGameByPlayer(player.getName());
        if (game.getPlayer(player.getName()).isInTeam(EnumTeam.SPEC)) {
            event.setCancelled(true);
            return;
        }
    }

    // ///////////////////////////////////
    //
    // COMMON RELATED
    //
    // ///////////////////////////////////

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (this.cancelBlockEvent(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        IgniteCause cause = event.getCause();
        if (cause.equals(IgniteCause.LAVA) || cause.equals(IgniteCause.SPREAD) || cause.equals(IgniteCause.LIGHTNING) || cause.equals(IgniteCause.FIREBALL)) {
            if (this.cancelBlockEvent(event.getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (!event.getSource().getType().equals(Material.RED_MUSHROOM) && !event.getSource().getType().equals(Material.BROWN_MUSHROOM)) {
            if (this.cancelBlockEvent(event.getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (this.cancelBlockEvent(block)) {
                event.setCancelled(true);
                event.setYield(0f);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (this.cancelBlockEvent(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        ArrayList<Block> changedBlocks = this.getPistonChangeBlocks(event.getBlock(), event.getDirection());
        for (Block block : changedBlocks) {
            if (this.cancelBlockEvent(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (this.cancelBlockEvent(event.getRetractLocation().getBlock())) {
            event.setCancelled(true);
            return;
        }
    }

    private ArrayList<Block> getPistonChangeBlocks(Block pistonBlock, BlockFace direction) {
        ArrayList<Block> list = new ArrayList<Block>();
        Block temp = pistonBlock;
        for (int count = 0; count < 13; count++) {
            temp = temp.getRelative(direction);
            list.add(temp);
            if (nonPushableBlocks.contains(temp.getTypeId())) {
                return list;
            }
        }
        return list;
    }

    private boolean cancelBlockEvent(Block block) {
        // update the BlockVector & the ProtectionInfo
        this.vector.update(block.getLocation());
        for (COKGame game : this.gameManager.getAllGames()) {
            if (game.isBaseBlock(EnumTeam.RED, vector) || game.isBaseBlock(EnumTeam.BLU, vector)) {
                return true;
            }
        }
        return false;
    }
}
