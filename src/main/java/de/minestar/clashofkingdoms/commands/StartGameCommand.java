package de.minestar.clashofkingdoms.commands;

import org.bukkit.entity.Player;

import de.minestar.clashofkingdoms.COKCore;
import de.minestar.clashofkingdoms.data.COKGame;
import de.minestar.library.commandsystem.AbstractCommand;
import de.minestar.library.commandsystem.ArgumentList;
import de.minestar.library.commandsystem.annotations.Arguments;
import de.minestar.library.commandsystem.annotations.Description;
import de.minestar.library.commandsystem.annotations.Label;
import de.minestar.library.commandsystem.annotations.PermissionNode;
import de.minestar.minestarlibrary.utils.PlayerUtils;

@Label(label = "start")
@Arguments(arguments = "")
@PermissionNode(node = "cok.commands.game")
@Description(description = "Start the game")
public class StartGameCommand extends AbstractCommand {

    @Override
    public void execute(Player player, ArgumentList argumentList) {
        COKGame game = COKCore.gameManager.getGameByPlayer(player.getName());
        if (game == null) {
            PlayerUtils.sendError(player, COKCore.NAME, "You are currently not in a game!");
            return;
        }

        game.startGame();
    }
}
