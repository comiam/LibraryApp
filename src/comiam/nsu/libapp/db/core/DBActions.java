package comiam.nsu.libapp.db.core;

import comiam.nsu.libapp.db.objects.PersonCard;
import comiam.nsu.libapp.util.Pair;
import lombok.val;
import lombok.var;

import java.sql.SQLException;

public class DBActions
{
    public static String insertNewUser(String surname, String firstName, String patronymic, String type, String... data)
    {
        var res = DBCore.makeRequest("insert into HUMAN values (0, '" + type + "', '" + surname + "', '"  + firstName + "', '" + patronymic + "')");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return "Failed request!\nError: " + res.getFirst();

        val human = getLastRecordFromTable("HUMAN", "ID");

        if(human == null)
            return "Fatal error during execution the request to library server! Try again!\n";

        res = DBCore.makeRequest("insert into READER_CARD values (0, " + human[0] + ", CURRENT_DATE, CURRENT_DATE)");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return "Failed request!\nError: " + res.getFirst();

        val card = getLastRecordFromTable("READER_CARD", "ID");

        if(card == null)
            return "Fatal error during execution the request to library server! Try again!\n";


        res = DBCore.makeRequest("insert into CARD_ACCOUNTING values (" + card[0] + ", CURRENT_DATE, 'create')");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return "Failed request!\nError: " + res.getFirst();

        /*if(!type.equals("abitura"))
        {
            //Some stuff
        }*/

        return "";
    }

    public static String[] getLastRecordFromTable(String name, String orderBy)
    {
        val res = DBCore.makeRequest("SELECT * FROM (\n" +
                "SELECT * FROM " + name + " ORDER BY " + orderBy + " DESC\n" +
                ") WHERE ROWNUM = 1");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return null;

        try
        {
            val rs = res.getSecond();
            int columns = rs.getMetaData().getColumnCount();

            val row = new String[columns];
            while(rs.next())
                for(int i = 1; i <= columns; i++)
                    row[i - 1] = rs.getString(i);

            return row;
        }catch (SQLException e) {
            return null;
        }
    }

    public static String deleteUserCard(PersonCard card)
    {
        var res = DBCore.makeRequest("delete from READER_CARD where ID = " + card.getID() + " and HUMAN_ID = " + card.getHumanID());

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return "Failed request!\nError: " + res.getFirst();

        res = DBCore.makeRequest("delete from HUMAN where ID = " + card.getHumanID());

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return "Failed request!\nError: " + res.getFirst();

        res = DBCore.makeRequest("insert into CARD_ACCOUNTING values (" + card.getID() + ", CURRENT_DATE, 'delete')");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return "Failed request!\nError: " + res.getFirst();

        return "";
    }

    public static Pair<String, String[][]> getTableOfCardData()
    {
        val res = DBCore.makeRequest("select READER_CARD.ID, HUMAN_ID, SURNAME, FIRST_NAME, PATRONYMIC, REG_DATE, REWRITE_DATE, TYPE_ID, CAN_TAKE_FOR_TIME from READER_CARD " +
                " inner join HUMAN H on H.ID = READER_CARD.HUMAN_ID" +
                " inner join READER_TYPE RT on H.TYPE_ID = RT.ID");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return new Pair<>("Failed request! Try again!\nError: " + res.getFirst(), null);

        String[][] data;
        try
        {
            val rs = res.getSecond();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            int columns = rs.getMetaData().getColumnCount();

            data = new String[rows][columns];

            int rowI = 0;
            while(rs.next())
            {
                for(int i = 1; i <= columns; i++)
                    data[rowI][i - 1] = rs.getString(i);
                rowI++;
            }
        }catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        }

        return new Pair<>("", data);
    }

    public static Pair<String, String[][]> getTableValues(String table)
    {
        val res = DBCore.makeRequest("select * from " + table.trim());

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return new Pair<>("Failed request! Try again!\nError: " + res.getFirst(), null);

        String[][] data;
        try
        {
            val rs = res.getSecond();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();

            int columns = rs.getMetaData().getColumnCount();

            data = new String[rows + 1][columns];

            val rsmd = rs.getMetaData();
            for (int i = 1; i <= columns; i++)
                data[0][i - 1] = rsmd.getColumnName(i);

            int rowI = 1;
            while(rs.next())
            {
                for(int i = 1; i <= columns; i++)
                    data[rowI][i - 1] = rs.getString(i);
                rowI++;
            }

        }catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        }

        return new Pair<>("", data);
    }

    public static Pair<String, String[]> getTypeNames()
    {
        val res = DBCore.makeRequest("select ID, NAME from READER_TYPE");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return new Pair<>("Failed request! Try again!\nError: " + res.getFirst(), null);

        String[] names;
        try
        {
            val rs = res.getSecond();

            int rowCount = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();

            names = new String[rowCount];

            String n;
            int i = 0;
            while(rs.next())
                names[i++] = rs.getString(1) + ": " + rs.getString(2);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Pair<>(e.getMessage(), null);
        }

        return new Pair<>("", names);
    }

    public static Pair<String, String[]> getTableNames()
    {
        val res = DBCore.makeRequest("select table_name from user_tables");

        if(!res.getFirst().isEmpty() && res.getSecond() == null)
            return new Pair<>("Failed request! Try again!\nError: " + res.getFirst(), null);

        String[] names;
        try
        {
            val rs = res.getSecond();

            int rowCount = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();

            names = new String[rowCount - 1];//ignore internal tables, so decrement row size

            String n;
            int i = 0;
            while(rs.next())
            {
                n = rs.getString(1);

                if(!n.equals("HTMLDB_PLAN_TABLE") && !n.isEmpty())//ignore internal tables
                    names[i++] = n;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Pair<>(e.getMessage(), null);
        }

        return new Pair<>("", names);
    }
}
