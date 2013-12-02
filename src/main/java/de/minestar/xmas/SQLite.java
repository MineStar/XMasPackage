package de.minestar.xmas;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import de.minestar.minestarlibrary.database.AbstractSQLiteHandler;
import de.minestar.xmas.data.BlockVector;
import de.minestar.xmas.data.XMasDay;

public class SQLite extends AbstractSQLiteHandler {

    private Connection connection;

    public SQLite(String pluginName, File SQLConfigFile) {
        super(pluginName, SQLConfigFile);
    }

    @Override
    protected void createStatements(String arg0, Connection con) throws Exception {
    }

    @Override
    protected void createStructure(String arg0, Connection con) throws Exception {
        this.connection = con;
    }

    public void init() {
        for (int day = 1; day <= 24; day++) {
            // players
            try {
                String data = "CREATE TABLE IF NOT EXISTS day_" + day + " (player VARCHAR(64) NOT NULL);";
                Statement statement = this.connection.createStatement();
                statement.execute(data);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // buttons
            try {
                String data = "CREATE TABLE IF NOT EXISTS buttons_" + day + " (world VARCHAR(64) NOT NULL, x INT(11) NOT NULL, y INT(11) NOT NULL, z INT(11) NOT NULL);";
                Statement statement = this.connection.createStatement();
                statement.execute(data);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // copy old buttons
            XMasDay xmasDay = XMASCore.getDayByDate(day);
            for (BlockVector vector : xmasDay.getButtons()) {
                this.addButton(day, vector);
            }

            // rename old files
            File file = new File(XMASCore.INSTANCE.getDataFolder(), "buttons_" + day + ".txt");
            if (file.exists()) {
                file.renameTo(new File(XMASCore.INSTANCE.getDataFolder(), "old_buttons_" + day + ".txt"));
            }
        }
    }
    public boolean removeButton(int day, BlockVector vector) {
        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate("DELETE FROM buttons_" + day + " WHERE world='" + vector.getWorldName() + "' AND x=" + vector.getX() + " AND y=" + vector.getY() + " AND z=" + vector.getZ() + ";");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addButton(int day, BlockVector vector) {
        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate("INSERT INTO buttons_" + day + " (world, x, y, z) VALUES ('" + vector.getWorldName() + "', " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ() + ");");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<BlockVector> getButtonsForDay(int day) {
        ArrayList<BlockVector> list = new ArrayList<BlockVector>();
        try {
            Statement statement = this.connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM buttons_" + day);
            while (results.next()) {
                String worldName = results.getString("world");
                int x = results.getInt("x");
                int y = results.getInt("y");
                int z = results.getInt("z");
                if (Bukkit.getWorld(worldName) != null) {
                    BlockVector vector = new BlockVector(worldName, x, y, z);
                    list.add(vector);
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            list.clear();
        }
        return list;
    }

    public boolean insertPlayer(int day, String player) {
        try {
            if (this.hasPlayer(day, player)) {
                return false;
            }
            Statement statement = this.connection.createStatement();
            statement.executeUpdate("INSERT INTO day_" + day + " (player) VALUES ('" + player + "');");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasPlayer(int day, String player) {
        try {
            Statement statement = this.connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM day_" + day + " WHERE player='" + player + "'");
            while (results.next()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
