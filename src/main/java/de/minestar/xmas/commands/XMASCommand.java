package de.minestar.xmas.commands;

import org.bukkit.entity.Player;

import de.minestar.minestarlibrary.commands.AbstractCommand;
import de.minestar.minestarlibrary.utils.PlayerUtils;
import de.minestar.xmas.AdminListener;
import de.minestar.xmas.XMASCore;

public class XMASCommand extends AbstractCommand {

    public XMASCommand(String syntax, String arguments, String node) {
        super(syntax, arguments, node);
        this.description = "XMAS!";
    }

    @Override
    public void execute(String[] args, Player player) {
        if (!player.isOp()) {
            PlayerUtils.sendError(player, XMASCore.NAME, "You are not allowed to use this command!");
            return;
        }

        try {
            int day = Integer.valueOf(args[0]);
            if (day > 0 && day < 25) {
                AdminListener.adminMap.put(player.getName(), day);
                PlayerUtils.sendSuccess(player, XMASCore.NAME, "Day set!");
            } else {
                AdminListener.adminMap.remove(player.getName());
                PlayerUtils.sendSuccess(player, XMASCore.NAME, "Day unset!");
            }
            return;
        } catch (Exception e) {
            PlayerUtils.sendError(player, XMASCore.NAME, "Wrong syntax!");
            PlayerUtils.sendInfo(player, "Use: /xmas <1..24>");
            return;
        }
    }
}
