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
    public static String initSession(String user, String password)
    {
        if(currentSession != null)
            return "";

        String url = "jdbc:oracle:thin:@" + ServerData.SERVER_IP + ":" + ServerData.SERVER_PORT + ":XE";

        try
        {   //can access this shit
            Connection conn = DriverManager.getConnection(url, "18209_b_" + user, password);
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
        Statement st = null;
        try
        {
            st = currentSession.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
            try
            {
                if(st != null)
                    st.close();
            }catch (SQLException ignored){}
            return new Response(e.getMessage(), null, null);
        }
    }

    /*
     * Semantic call like <procedure_name>(args)
     * Function will made sql request {call <procedure_name>(args)} and execute
     */
    public static String callProcedure(String call, boolean commit)
    {
        if(currentSession == null)
            return "Session not initialized!";

        Statement st = null;
        try
        {
            st = currentSession.prepareCall("{call " + call + "}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            st.execute("{call " + call + "}");
            if(commit)
                commitTransaction();
            return "";
        }catch(Throwable e)
        {
            rollbackTransaction();
            try
            {
                if(st != null)
                    st.close();
            }catch (SQLException ignored){}
            return e.getMessage();
        }
    }

    /*
     * Send single request to server and get new value in IDName column
     */
    public static Response makeInsertWithResult(String request, String IDName)
    {
        if(currentSession == null)
            return new Response("Session not initialized!", null, null);
        Statement st = null;
        try
        {
            st = currentSession.prepareStatement(request, Statement.RETURN_GENERATED_KEYS);
            st.executeUpdate(request, new String[] {IDName});

            return new Response("", st.getGeneratedKeys(), st);
        }catch(Throwable e)
        {
            rollbackTransaction();
            try
            {
                if(st != null)
                    st.close();
            }catch (SQLException ignored){}
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
