package comiam.nsu.libapp.db;

import comiam.nsu.libapp.util.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBCore
{
    private static Connection currentSession;

    /*
     * Initialization of JDBC driver
     */
    static
    {
        try
        {
            System.setProperty("oracle.jdbc.territoryMap", "ru=RUSSIA;RU=RUSSIA");
            System.setProperty("oracle.jdbc.languageMap", "ru=RUSSIAN;RU=RUSSIAN");
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * Connect to DB server and init session
     *
     * use 84.237.50.81:1521
     */
    public static String initSession(String username, String password)
    {
        if(currentSession != null)
            return "";

        String url = "jdbc:oracle:thin:@" + ServerData.SERVER_IP + ":" + ServerData.SERVER_PORT + ":XE";

        try
        {
            Connection conn = DriverManager.getConnection(url, username, password);
            currentSession = conn;
            currentSession.setAutoCommit(false);
            return "";
        }catch(Throwable e)
        {
            return e.getMessage();
        }
    }

    /*
     * Send request to server
     */
    public static Pair<String, ResultSet> makeRequest(String request)
    {
        if(currentSession == null)
            return new Pair<String, ResultSet>("Session not initialized!", null);
        try
        {
            PreparedStatement statement = currentSession.prepareStatement(request);

            statement.execute();
            currentSession.commit();

            return new Pair<String, ResultSet>("", statement.getResultSet());
        }catch(Throwable e)
        {
            try
            {
                currentSession.rollback();
            }catch(Throwable ignored) {}

            return new Pair<String, ResultSet>(e.getMessage(), null);
        }
    }

    /*
     * Close current session, if its exists
     */
    public static void closeCurrentSession()
    {
        try
        {
            if(currentSession != null)
                currentSession.close();
        }catch(Throwable ignored) {}
        currentSession = null;
    }
}
