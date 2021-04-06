package comiam.nsu.libapp.db.core;

import comiam.nsu.libapp.db.objects.PersonCard;
import comiam.nsu.libapp.util.Pair;
import lombok.val;
import lombok.var;

import java.sql.SQLException;

import static comiam.nsu.libapp.db.core.DBCore.*;

public class DBActions
{
    public static String createNewAssistant(String humanID, String sub0)
    {
        var res = makeRequest("insert into ASSISTANT values (" + humanID + ", '" + sub0 + "')", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Can't create new assistant: " + res.getErrorMsg() : "";
    }

    public static String createNewSPO(String humanID, String sub0)
    {
        var res = makeRequest("insert into SPO values (" + humanID + ", '" + sub0 + "')", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Can't create new spo: " + res.getErrorMsg() : "";
    }

    public static String createNewTeacher(String humanID, String grade, String post, String sub0, String sub1)
    {
        var res = makeRequest("insert into TEACHERS values (" + humanID + ", '" + grade + "', '" + post + "', " +
                  (sub0 == null ? "NULL" : "'" + sub0 + "'") + ", "
                + (sub1 == null ? "NULL" : "'" + sub1 + "'") + ")", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Can't create new teacher: " + res.getErrorMsg() : "";
    }

    public static String createNewStudent(String humanID, String fac, String group, String cource)
    {
        var res = makeRequest("insert into STUDENTS values (" + humanID + ", " + group + ", '" + fac + "', " + cource + ")", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Can't create new student: " + res.getErrorMsg() : "";
    }

    public static Pair<String, String> createNewReader(String surname, String firstName, String patronymic, String typeName)
    {
        var res = makeRequest("select ID from READER_TYPE where NAME = '" + typeName + "'", false, false);

        if(isBadResponse(res))
            return new Pair<>("Can't create new user: " + res.getErrorMsg(), "");

        int typeID = 0;
        try
        {
            while(res.getSet().next())
                typeID = Integer.parseInt(res.getSet().getString(1));
        }catch (SQLException e) {
            rollbackTransaction();
            return new Pair<>("Can't create new user: " + e.getMessage(), "");
        } finally
        {
            res.closeAll();
        }

        res = makeInsertWithResult("insert into HUMAN values (0, " + typeID + ", '" + surname + "', '"  + firstName + "', '" + patronymic + "')", "ID");
        if(isBadResponse(res))
            return new Pair<>("Can't create new user: " + res.getErrorMsg(), "");

        var ID = "";
        try
        {
            if(res.getSet().next())
                ID = res.getSet().getString(1);
        }catch (SQLException e) {
            rollbackTransaction();
            return new Pair<>("Can't create new user: " + e.getMessage(), "");
        } finally
        {
            res.closeAll();
        }

        res = makeInsertWithResult("insert into READER_CARD values (0, " + ID + ", CURRENT_DATE, CURRENT_DATE)", "ID");
        if(isBadResponse(res))
            return new Pair<>("Can't create new user: " + res.getErrorMsg(), "");

        val humanID = ID;
        ID = "";
        try
        {
            if(res.getSet().next())
                ID = res.getSet().getString(1);
        }catch (SQLException e) {
            rollbackTransaction();
            return new Pair<>("Can't create new user: " + e.getMessage(), "");
        } finally
        {
            res.closeAll();
        }

        res = makeRequest("insert into CARD_ACCOUNTING values (" + ID + ", CURRENT_DATE, 'create')", true, true);
        if(isBadResponse(res))
            return new Pair<>("Can't create new user: " + res.getErrorMsg(), "");

        res.closeAll();

        return new Pair<>("", humanID);
    }

    public static String deleteSelectedUser(PersonCard card)
    {
        var res = makeRequest("delete from READER_CARD where ID = " + card.getID() + " and HUMAN_ID = " + card.getHumanID(), false, true);
        if(isBadResponse(res))
            return "Can't delete user: " + res.getErrorMsg();

        res.closeAll();

        res = makeRequest("delete from HUMAN where ID = " + card.getHumanID(), false, true);
        if(isBadResponse(res))
            return "Can't delete user: " + res.getErrorMsg();

        res.closeAll();

        res = makeRequest("insert into CARD_ACCOUNTING values (" + card.getID() + ", CURRENT_DATE, 'delete')", false, true);
        if(isBadResponse(res))
            return "Can't delete user: " + res.getErrorMsg();

        res.closeAll();

        val finalRes = commitTransaction();

        return !finalRes.isEmpty() ? "Can't delete user: " + finalRes : "";
    }

    public static Pair<String, String[]> loadVariablesOfTable(String table)
    {
        var res = makeRequest("select ID_NAME from " + table, true, false);

        if(isBadResponse(res))
            return new Pair<>("Can't load data to form: " + res.getErrorMsg(), null);

        try
        {
            int rows = res.getSet().last() ? res.getSet().getRow() : 0;
            res.getSet().beforeFirst();

            val vars = new String[rows];
            int i = 0;
            while(res.getSet().next())
                vars[i++] = res.getSet().getString(1);

            return new Pair<>("", vars);
        }catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        } finally
        {
            res.closeAll();
        }
    }

    public static Pair<String, String[][]> getTableOfCardData()
    {
        val res = makeRequest("select READER_CARD.ID, HUMAN_ID, SURNAME, FIRST_NAME, PATRONYMIC, REG_DATE, REWRITE_DATE, RT.NAME, CAN_TAKE_FOR_TIME from READER_CARD " +
                " inner join HUMAN H on H.ID = READER_CARD.HUMAN_ID" +
                " inner join READER_TYPE RT on H.TYPE_ID = RT.ID", true, false);

        if(isBadResponse(res))
            return new Pair<>("Failed request! Try again!\nError: " + res.getErrorMsg(), null);

        String[][] data;
        try
        {
            val rs = res.getSet();

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
        } finally
        {
            res.closeAll();
        }

        return new Pair<>("", data);
    }

    public static Pair<String, String[][]> getTableValues(String table)
    {
        val res = makeRequest("select * from " + table.trim(), true, false);

        if(isBadResponse(res))
            return new Pair<>("Failed request! Try again!\nError: " + res.getErrorMsg(), null);

        String[][] data;
        try
        {
            val rs = res.getSet();

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
        } finally
        {
            res.closeAll();
        }

        return new Pair<>("", data);
    }

    public static Pair<String, String[]> getTypeNames()
    {
        val res = makeRequest("select NAME from READER_TYPE", true, false);

        if(isBadResponse(res))
            return new Pair<>("Failed request! Try again!\nError: " + res.getErrorMsg(), null);

        String[] names;
        try
        {
            val rs = res.getSet();

            int rowCount = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();

            names = new String[rowCount];

            int i = 0;
            while(rs.next())
                names[i++] = rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Pair<>(e.getMessage(), null);
        } finally
        {
            res.closeAll();
        }

        return new Pair<>("", names);
    }

    public static Pair<String, String[]> getTableNames()
    {
        val res = makeRequest("select table_name from user_tables", true, false);

        if(isBadResponse(res))
            return new Pair<>("Failed request! Try again!\nError: " + res.getErrorMsg(), null);

        String[] names;
        try
        {
            val rs = res.getSet();

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
        } finally
        {
            res.closeAll();
        }

        return new Pair<>("", names);
    }
}
