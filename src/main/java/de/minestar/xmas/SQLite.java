package de.minestar.xmas;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.minestar.minestarlibrary.database.AbstractSQLiteHandler;

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
            try {
                String data = "CREATE TABLE IF NOT EXISTS day_" + day + " (player VARCHAR(64) NOT NULL);";
                Statement statement = this.connection.createStatement();
                statement.execute(data);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
