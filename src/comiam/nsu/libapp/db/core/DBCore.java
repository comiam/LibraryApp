package comiam.nsu.libapp.db.core;

import java.sql.*;

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
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            currentSession = conn;
            currentSession.setAutoCommit(false);
            return "";
        }catch(Throwable e)
        {
            return e.getMessage();
        }
    }

    /*
     * Send single request to server
     */
    public static Response makeRequest(String request, boolean commit, boolean update)
    {
        if(currentSession == null)
            return new Response("Session not initialized!", null, null);
        try
        {
            Statement st = currentSession.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            if(!update)
                st.executeQuery(request);
            else
                st.executeUpdate(request);

            if(commit)
                commitTransaction();

            return new Response("", st.getResultSet(), st);
        }catch(Throwable e)
        {
            rollbackTransaction();
            return new Response(e.getMessage(), null, null);
        }
    }

    /*
     * Send single request to server and get new value in IDName column
     */
    public static Response makeInsertWithResult(String request, String IDName)
    {
        if(currentSession == null)
            return new Response("Session not initialized!", null, null);
        try
        {
            Statement st = currentSession.prepareStatement(request, Statement.RETURN_GENERATED_KEYS);
            st.executeUpdate(request, new String[] {IDName});

            return new Response("", st.getGeneratedKeys(), st);
        }catch(Throwable e)
        {
            rollbackTransaction();

            return new Response(e.getMessage(), null, null);
        }
    }

    public static void rollbackTransaction()
    {
        try
        {
            currentSession.rollback();
        }catch (SQLException ignored) {}
    }

    public static String commitTransaction()
    {
        try
        {
            currentSession.commit();
            return "";
        }catch (SQLException e) {
            return e.getMessage();
        }
    }

    public static boolean isBadResponse(Response response)
    {
        return !response.getErrorMsg().isEmpty();
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
