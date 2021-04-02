package comiam.nsu.libapp.db;

import comiam.nsu.libapp.util.Pair;
import lombok.val;

import java.sql.SQLException;
import java.util.ArrayList;

public class DBActions
{
    public static Pair<String, String[][]> getTableValues(String table)
    {
        val res = DBCore.makeRequest("select * from " + table.trim());

        if(!res.getFirst().isEmpty() || res.getSecond() == null)
            return new Pair<>("Failed request! Try again!", null);

        String[][] data = null;
        try
        {
            val rs = res.getSecond();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();

            int columns = rs.getMetaData().getColumnCount();

            data = new String[rows + 1][columns];

            val rsmd = rs.getMetaData();
            for (int i = 1; i <= columns; i++)
                data[i - 1][0] = rsmd.getColumnName(i);

            int rowI = 1;
            while(rs.next())
            {
                for(int i = 1; i <= columns; i++)
                    data[i - 1][rowI] = rs.getString(i);
                rowI++;
            }

        }catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        }

        return new Pair<>("", data);
    }

    public static Pair<String, String[]> getTableNames()
    {
        val res = DBCore.makeRequest("select table_name from user_tables");

        if(!res.getFirst().isEmpty() || res.getSecond() == null)
            return new Pair<>("Failed request! Try again!", null);

        String[] names = null;
        try
        {
            val rs = res.getSecond();

            int rowCount = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();

            names = new String[rowCount];

            String n;
            int i = 0;
            while(rs.next())
            {
                n = rs.getString(1);

                if(!n.equals("HTMLDB_PLAN_TABLE"))//ignore internal tables
                    names[i++] = n;
            }
        } catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        }

        return new Pair<>("", names);
    }
}
