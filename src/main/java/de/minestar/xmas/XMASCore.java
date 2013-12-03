package de.minestar.xmas;

import java.io.File;
import java.util.HashMap;

import org.bukkit.plugin.PluginManager;

import de.minestar.minestarlibrary.AbstractCore;
import de.minestar.minestarlibrary.commands.CommandList;
import de.minestar.xmas.commands.XMASCommand;
import de.minestar.xmas.data.BlockVector;
import de.minestar.xmas.data.XMasDay;

public class XMASCore extends AbstractCore {

    public static XMASCore INSTANCE;

    public static final String NAME = "XMAS";
    public static final String FULLNAME = "[" + NAME + "]";

    public static HashMap<Integer, XMasDay> dayMapByDate = new HashMap<Integer, XMasDay>();
    public static HashMap<BlockVector, XMasDay> dayMapByVector = new HashMap<BlockVector, XMasDay>();

    public static AdminListener adminListener;
    public static PlayerListener playerListener;

    public static SQLite database;

    public XMASCore() {
        super(NAME);
        XMASCore.INSTANCE = this;
    }

    @Override
    protected boolean createManager() {
        // load days
        dayMapByDate = new HashMap<Integer, XMasDay>();
        for (int day = 1; day <= 24; day++) {
            XMasDay currentDay = new XMasDay(day);
            dayMapByDate.put(day, currentDay);
            for (BlockVector vector : currentDay.getButtons()) {
                if (vector != null) {
                    XMASCore.registerBlock(vector, currentDay);
                }
            }
        }

        database = new SQLite(XMASCore.NAME, new File(getDataFolder(), "sqlconfig.yml"));
        database.init();
        for (int day = 1; day <= 24; day++) {
            XMasDay currentDay = dayMapByDate.get(day);
            currentDay.loadButtonsFromDB();
        }

        return super.createManager();
    }

    public static XMasDay getDayByButton(BlockVector vector) {
        return dayMapByVector.get(vector);
    }

    public static void registerBlock(BlockVector button, XMasDay day) {
        dayMapByVector.put(button, day);
    }

    @Override
    protected boolean createListener() {
        adminListener = new AdminListener();
        playerListener = new PlayerListener();
        return super.createListener();
    }

    public static XMasDay getDayByDate(int day) {
        return dayMapByDate.get(day);
    }

    @Override
    protected boolean createCommands() {
        //@formatter:off
        this.cmdList = new CommandList(NAME,
                new XMASCommand           ("/xmas",   "<DAY>",     "xmas.set")
        );
        //@formatter:on
        return super.createCommands();
    }

    @Override
    protected boolean registerEvents(PluginManager pm) {
        pm.registerEvents(adminListener, this);
        pm.registerEvents(playerListener, this);
        return super.registerEvents(pm);
    }
}
