package io.github.donut.proj;

import io.github.donut.proj.utils.PreparedStatementWrapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBManager extends DBSource {
    private DBManager() {
        // empty
    }

    private static class InstanceHolder {
        private static final DBManager INSTANCE = new DBManager();
    }

    public static DBManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public String getPlayerInfo(String userName) {
        String sql = "SELECT * FROM users WHERE username = ?;";
//        JSONObject jsonObj = new JSONObject();
        JSONArray array = new JSONArray();
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, userName) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                    }
                };
                ResultSet rs = stat.executeQuery();
        ) {
            while (rs.next()) {
                JSONObject record = new JSONObject();
                record.put("id", rs.getString("id"));
                record.put("username", rs.getString("username"));
                record.put("firstname", rs.getString("firstname"));
                record.put("lastname", rs.getString("lastname"));
                record.put("password", rs.getString("password"));
                array.put(record);
            }
//            jsonObj.put("users", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array.toString();
    }

    public boolean createAccount(String firstName, String lastName, String userName, String password) {
        String sql = "INSERT INTO users(username, firstname, lastname, password) VALUES(?, ?, ?, ?);";
        boolean temp = false;
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, firstName, lastName, userName, password) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                        stat.setString(2, (String) params[1]);
                        stat.setString(3, (String) params[2]);
                        stat.setString(4, (String) params[3]);
                    }
                };
        ) {
            if (stat.executeUpdate() != 0) temp = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * Called from the switch statement in {@code Main.java}, this method queries to the database and requests
     * to update the last name of the user with a matching {@code username}
     * @param userName Contains the username of the user who is updating their last name
     * @param lastName Contains the new last name for the user
     * @return Returns a boolean representing the success of the query
     * @author Joey Campbell
     */
    public boolean updateLastName(String userName, String lastName) {
        String sql = "UPDATE users SET lastname = ? WHERE username = ?;";
        boolean temp = false;
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, lastName, userName) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                        stat.setString(2, (String) params[1]);
                    }
                };
        ) {
            if (stat.executeUpdate() != 0) temp = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * Called from the switch statement in {@code Main.java}, this method queries to the database and requests
     * to update the user's username that that matches {@code oldUserName}. This method is unique because it must
     * handle a {@link PSQLException} to handle a non-unique {@code newUserName}.
     * @param oldUserName The old username of the calling user - need this to see which user to update in the database
     * @param newUserName The new username of the calling user - must be unique
     * @return Returns a boolean representing the success of the query
     * @author Joey Campbell
     */
    public boolean updateUserName(String oldUserName, String newUserName) {
        String sql = "UPDATE users SET username = ? WHERE username = ?;";
        boolean temp = false;
        try (
                Connection connection = getDataSource().getConnection();
                PreparedStatementWrapper stat = new PreparedStatementWrapper(connection, sql, newUserName, oldUserName) {
                    @Override
                    protected void prepareStatement(Object... params) throws SQLException {
                        stat.setString(1, (String) params[0]);
                        stat.setString(2, (String) params[1]);
                    }
                };
        ) {
            if (stat.executeUpdate() != 0) temp = true;
        } catch (PSQLException e) {
            // TODO May need to handle this more directly; for now, it just rejects the query and continues
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }
}