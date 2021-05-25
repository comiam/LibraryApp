package comiam.nsu.libapp.db.core;

import comiam.nsu.libapp.db.objects.AcceptOrderRow;
import comiam.nsu.libapp.db.objects.PersonCardRow;
import comiam.nsu.libapp.util.Pair;
import lombok.val;
import lombok.var;

import java.sql.SQLException;

import static comiam.nsu.libapp.db.core.DBCore.*;

public class DBActions
{
    public static Pair<String, String[]> getStatistics()
    {
        var res = makeRequest("select count(*) from \"18209_BOLSHIM\".READER_CARD", true, false);
        if(isBadResponse(res))
            return new Pair<>("Не получилось собрать статистику: " + res.getErrorMsg(), null);
        String[] data = new String[5];
        String error;
        if(!(error = readSingleValueFromResponse(res, data, 0)).isEmpty())
            return new Pair<>(error, null);

        res = makeRequest("select count(*) from \"18209_BOLSHIM\".MA_ORDER", true, false);
        if(isBadResponse(res))
            return new Pair<>("Не получилось собрать статистику: " + res.getErrorMsg(), null);
        if(!(error = readSingleValueFromResponse(res, data, 1)).isEmpty())
            return new Pair<>(error, null);
        res.closeAll();

        res = makeRequest("select count(*) from \"18209_BOLSHIM\".VIOLATIONS where VIOLATIONS.IS_CLOSED = 0", true, false);
        if(isBadResponse(res))
            return new Pair<>("Не получилось собрать статистику: " + res.getErrorMsg(), null);
        if(!(error = readSingleValueFromResponse(res, data, 2)).isEmpty())
            return new Pair<>(error, null);
        res.closeAll();

        res = makeRequest("select NAME from \"18209_BOLSHIM\".ACCEPTING_RETURNING_BOOKS\n" +
                "    inner join \"18209_BOLSHIM\".BOOKS B on B.ID = ACCEPTING_RETURNING_BOOKS.BOOK_ID\n" +
                "    where ROWNUM = 1\n" +
                "    group by BOOK_ID, NAME\n" +
                "    order by COUNT(BOOK_ID) desc", true, false);
        if(isBadResponse(res))
            return new Pair<>("Не получилось собрать статистику: " + res.getErrorMsg(), null);
        if(!(error = readSingleValueFromResponse(res, data, 3)).isEmpty())
            return new Pair<>(error, null);
        res.closeAll();

        res = makeRequest("select CARD_ID || ' ' || SURNAME || ' ' || FIRST_NAME || ' ' || PATRONYMIC from \"18209_BOLSHIM\".ACCEPTING_RETURNING_BOOKS\n" +
                "    inner join \"18209_BOLSHIM\".READER_CARD RC on ACCEPTING_RETURNING_BOOKS.CARD_ID = RC.ID\n" +
                "    inner join \"18209_BOLSHIM\".HUMAN H on H.ID = RC.HUMAN_ID\n" +
                "    where ROWNUM = 1\n" +
                "    group by CARD_ID, SURNAME, FIRST_NAME, PATRONYMIC\n" +
                "    order by COUNT(CARD_ID)", true, false);
        if(isBadResponse(res))
            return new Pair<>("Не получилось собрать статистику: " + res.getErrorMsg(), null);
        if(!(error = readSingleValueFromResponse(res, data, 4)).isEmpty())
            return new Pair<>(error, null);
        res.closeAll();

        return new Pair<>("", data);
    }

    private static String readSingleValueFromResponse(Response res, String[] data, int index)
    {
        try
        {
            val rs = res.getSet();

            if(rs.next())
                data[index] = rs.getString(1);
        }catch (SQLException e) {
            return e.getMessage();
        } finally
        {
            res.closeAll();
        }
        return "";
    }

    public static String acceptOrder(AcceptOrderRow order)
    {
        var res = makeRequest("update \"18209_BOLSHIM\".MA_ORDER set ACCEPTED_BY_ADMIN = 1 where BOOK_ID = " + order.getBookID() +
                " and CARD_ID = " + order.getCardID() + " and to_char(ORDER_DATE, 'yyyy-mm-dd') = '" + order.getOrderDate().split(" ")[0] + "' and " +
                " to_char(RETURN_DATE, 'yyyy-mm-dd') = '" + order.getReturnedOrder().split(" ")[0] + "' and ACCEPTED_BY_ADMIN = 0 and rownum = 1", true, true);
        if(isBadResponse(res))
            return "Не получилось одобрить заказ: " + res.getErrorMsg();

        res.closeAll();

        return "";
    }

    public static String denyOrder(AcceptOrderRow order)
    {
        var res = makeRequest("delete from \"18209_BOLSHIM\".MA_ORDER where BOOK_ID = " + order.getBookID() +
                " and CARD_ID = " + order.getCardID() + " and to_char(ORDER_DATE, 'yyyy-mm-dd') = '" + order.getOrderDate().split(" ")[0] + "' and " +
                " to_char(RETURN_DATE, 'yyyy-mm-dd') = '" + order.getReturnedOrder().split(" ")[0] + "' and ACCEPTED_BY_ADMIN = 0 and rownum = 1", true, true);
        if(isBadResponse(res))
            return "Не получилось отклонить заказ: " + res.getErrorMsg();

        res.closeAll();

        return "";
    }

    public static Pair<String, String[][]> getNotAcceptedOrders()
    {
        var res = makeRequest("select BOOK_ID, NAME, CARD_ID, ORDER_DATE, RETURN_DATE from \"18209_BOLSHIM\".MA_ORDER " +
                        "inner join \"18209_BOLSHIM\".BOOKS B on B.ID = MA_ORDER.BOOK_ID \n" +
                        "where ACCEPTED_BY_ADMIN = 0 and TAKEN IS NULL",
                true, false);
        if(isBadResponse(res))
            return new Pair<>("Не получилось получить неодобренные заказы: " + res.getErrorMsg(), null);

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

    public static String createUserDB(String name, String password)
    {
        var res = makeRequest("create user \"18209_B_" + name.toUpperCase() + "\" identified by \"" + password + "\"", false, true);
        if(isBadResponse(res))
            return res.getErrorMsg();
        res.closeAll();
        res = makeRequest("grant create session to \"18209_B_" + name.toUpperCase() + "\" with admin option", false, true);
        if(isBadResponse(res))
            return res.getErrorMsg();
        res.closeAll();
        res = makeRequest("grant READER to \"18209_B_" + name.toUpperCase() + "\" with admin option", true, true);
        if(isBadResponse(res))
            return res.getErrorMsg();
        else
        {
            res.closeAll();
            return "";
        }
    }

    public static Pair<String, Boolean> userIsReader()
    {
        var res = DBActions.getUserPrivileges();
        if(!res.getFirst().isEmpty())
            return new Pair<>(res.getFirst(), null);
        else
            for(var role : res.getSecond())
                if(role.equals("READER"))
                    return new Pair<>("", true);

        return new Pair<>("", false);
    }

    private static Pair<String, String[]> getUserPrivileges()
    {
        var res = makeRequest("SELECT \"USER_ROLE_PRIVS\".\"GRANTED_ROLE\" FROM \"USER_ROLE_PRIVS\"", true, false);
        if(isBadResponse(res))
            return new Pair<>("Не получилось получить права пользователя: " + res.getErrorMsg(), null);
        String[] data;
        try
        {
            val rs = res.getSet();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            data = new String[rows];

            int i = 0;
            while(rs.next())
                data[i++] = rs.getString("GRANTED_ROLE");
        }catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        } finally
        {
            res.closeAll();
        }

        return new Pair<>("", data);
    }

    public static String saveUserData(int userID, String firstname, String surname, String patronymic, String password)
    {
        var res = makeRequest("select HUMAN_ID from \"18209_BOLSHIM\".READER_CARD where ID = " + userID, false, false);
        if(isBadResponse(res))
            return "Не получилось обновить пользователя: " + res.getErrorMsg();

        int humanID = 0;
        try
        {
            while(res.getSet().next())
                humanID = Integer.parseInt(res.getSet().getString(1));
        }catch (SQLException e) {
            rollbackTransaction();
            return "Не получилось обновить пользователя: " + e.getMessage();
        } finally
        {
            res.closeAll();
        }

        res = makeRequest("update \"18209_BOLSHIM\".HUMAN set FIRST_NAME = '" + firstname + "', SURNAME = '"
                + surname + "', PATRONYMIC = '" + patronymic + "' where ID = " + humanID, false, true);
        if(isBadResponse(res))
            return "Не получилось обновить пользователя: " + res.getErrorMsg();

        res = makeRequest("ALTER USER \"18209_B_" + userID + "\" IDENTIFIED BY \"" + password + "\"", true, true);
        if(isBadResponse(res))
            return "Не получилось обновить пользователя: " + res.getErrorMsg();

        return "";
    }

    public static Pair<String, String[]> getUserData(int userID)
    {
        var res = makeRequest("select FIRST_NAME, SURNAME, PATRONYMIC from \"18209_BOLSHIM\".READER_CARD " +
                        "inner join \"18209_BOLSHIM\".HUMAN H on H.ID = \"18209_BOLSHIM\".READER_CARD.HUMAN_ID " +
                        "where \"18209_BOLSHIM\".READER_CARD.ID = " + userID,
                true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении данных пользователя: " + res.getErrorMsg(), null);

        String[] data;
        try
        {
            val rs = res.getSet();

            int columns = rs.getMetaData().getColumnCount();
            data = new String[columns];

            int colI = 0;
            if(rs.next())
                for(int i = 1; i <= columns; i++)
                    data[colI++] = rs.getString(i);
        }catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        } finally
        {
            res.closeAll();
        }

        return new Pair<>("", data);
    }

    public static Pair<String, String[][]> getUserViolations(int userID)
    {
        var res = makeRequest("select BOOK_ID, NAME, HALL_ID, VIOLATION_DATE, VIOLATION_TYPE, MONETARY_FINE from \"18209_BOLSHIM\".VIOLATIONS " +
                        "inner join \"18209_BOLSHIM\".BOOKS B on B.ID = VIOLATIONS.BOOK_ID where CARD_ID = " + userID,
                true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении текущих задолжностей: " + res.getErrorMsg(), null);

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

    public static Pair<String, String[][]> getUserOrders(int userID)
    {
        var res = makeRequest("select ID, NAME, ORDER_DATE, RETURN_DATE, TAKEN, " +
                "RETURN_STATE, ACCEPTED_BY_ADMIN from \"18209_BOLSHIM\".MA_ORDER inner join \"18209_BOLSHIM\".BOOKS B on B.ID = MA_ORDER.BOOK_ID where CARD_ID = " + userID + " and TAKEN IS NULL and RETURN_STATE = 0",
                true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении заказов: " + res.getErrorMsg(), null);

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

    public static Pair<String, String[]> getViolationData()
    {
        var res = makeRequest("select V.BOOK_ID, V.CARD_ID, V.HALL_ID, V.VIOLATION_DATE, V.VIOLATION_TYPE, H.SURNAME, H.FIRST_NAME, H.PATRONYMIC, B.NAME from \"18209_BOLSHIM\".VIOLATIONS V\n" +
                " inner join \"18209_BOLSHIM\".READER_CARD RC on V.CARD_ID = RC.ID\n" +
                " inner join \"18209_BOLSHIM\".HUMAN H on H.ID = RC.HUMAN_ID\n" +
                " inner join \"18209_BOLSHIM\".BOOKS B on B.ID = V.BOOK_ID\n" +
                " where V.IS_CLOSED = 0", true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении задолжностей: " + res.getErrorMsg(), null);

        String[] data;
        try
        {
            val rs = res.getSet();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            int columns = rs.getMetaData().getColumnCount();
            data = new String[rows];

            int rowI = 0;
            val row = new String[columns];
            while(rs.next())
            {
                for(int i = 1; i <= columns; i++)
                    row[i - 1] = i == 4 ? rs.getString(i).split(" ")[0] : rs.getString(i);

                data[rowI] = String.format("book %s; card %s; hall %s; date %s; type %s; FIO %s; book name %s",
                        row[0], row[1], row[2], row[3], row[4], row[5] + " " + row[6] + (row[7] == null ? "" : " " + row[7]), row[8]);
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

    public static Pair<String, String> getBookCost(String bookID)
    {
        var res = makeRequest("select COST from \"18209_BOLSHIM\".BOOKS where ID = " + bookID, true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении стоимости книги: " + res.getErrorMsg(), "");

        var cost = "";
        try
        {
            if(res.getSet().next())
                cost = res.getSet().getString(1);
        }catch (SQLException e) {
            rollbackTransaction();
            return new Pair<>("Ошибка при получении стоимости книги: " + e.getMessage(), "");
        } finally
        {
            res.closeAll();
        }
        return new Pair<>("", cost);
    }

    public static Pair<String, String[]> getHallNames()
    {
        var res = makeRequest("select LH.ID, HT.ID_NAME from \"18209_BOLSHIM\".LIBRARY_HALLS LH inner join \"18209_BOLSHIM\".HALL_TYPE HT on HT.ID_NAME = LH.HALL_TYPE_ID", true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении названий залов: " + res.getErrorMsg(), null);

        String[] data;
        try
        {
            val rs = res.getSet();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            int columns = rs.getMetaData().getColumnCount();
            data = new String[rows];

            int rowI = 0;
            while(rs.next())
            {
                data[rowI] = "";
                for(int i = 1; i <= columns; i++)
                    data[rowI] += (i == 1 ? "" : ": ") + rs.getString(i);
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

    public static Pair<String, String[]> getBooksList()
    {
        var res = makeRequest("select ID, NAME, YEAR_OF_PUBL from \"18209_BOLSHIM\".BOOKS", true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении названий книг: " + res.getErrorMsg(), null);

        String[] data;
        try
        {
            val rs = res.getSet();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            int columns = rs.getMetaData().getColumnCount();
            data = new String[rows];

            int rowI = 0;
            while(rs.next())
            {
                data[rowI] = "";
                for(int i = 1; i <= columns; i++)
                    data[rowI] += (i == 1 ? "" : ": ") + (i == columns ? rs.getString(i).split(" ")[0] : rs.getString(i));
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

    public static Pair<String, String[]> getUsersListForBook()
    {
        var res = makeRequest("select H.SURNAME, H.FIRST_NAME, H.PATRONYMIC, READER_CARD.ID from \"18209_BOLSHIM\".READER_CARD inner join \"18209_BOLSHIM\".HUMAN H on H.ID = READER_CARD.HUMAN_ID", true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении пользователей: " + res.getErrorMsg(), null);

        String[] data;
        try
        {
            val rs = res.getSet();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            int columns = rs.getMetaData().getColumnCount();
            data = new String[rows];

            int rowI = 0;
            while(rs.next())
            {
                data[rowI] = "";
                for(int i = 1; i <= columns; i++)
                    data[rowI] += (i == columns ? ": " : i == 1 ? "" : " ") + rs.getString(i);
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

    public static Pair<String, String[]> getRowInfoFrom(String comparableValue, String compareColumn, String table)
    {
        var res = makeRequest("select * from \"18209_BOLSHIM\"." + table + " where " + compareColumn + " = " + comparableValue, true, false);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при получении строк таблицы: " + res.getErrorMsg(), null);

        try
        {
            val rs = res.getSet();
            val data = new String[rs.getMetaData().getColumnCount()];

            boolean empty = true;
            while(rs.next())
            {
                empty = false;
                for(int i = 1; i <= data.length; i++)
                    data[i - 1] = rs.getString(i);
            }

            return new Pair<>(empty ? "empty" : "", data);
        }catch (SQLException e) {
            return new Pair<>(e.getMessage(), null);
        } finally
        {
            res.closeAll();
        }
    }

    public static String createNewAssistant(String humanID, String sub0)
    {
        var res = makeRequest("insert into \"18209_BOLSHIM\".ASSISTANT values (" + humanID + ", '" + sub0 + "')", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при создании ассистента: " + res.getErrorMsg() : "";
    }

    public static String updateAssistant(String humanID, String sub0, boolean isNewData)
    {
        if(isNewData)
            return createNewAssistant(humanID, sub0);
        else
        {
            var res = makeRequest("update \"18209_BOLSHIM\".ASSISTANT set SUBJECT_ID = '" + sub0 + "' where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении ассистента: " + res.getErrorMsg() : "";
        }
    }

    public static String createNewSPO(String humanID, String sub0)
    {
        var res = makeRequest("insert into \"18209_BOLSHIM\".SPO values (" + humanID + ", '" + sub0 + "')", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при создании СПО: " + res.getErrorMsg() : "";
    }

    public static String updateSPO(String humanID, String sub0, boolean isNewData)
    {
        if(isNewData)
            return createNewSPO(humanID, sub0);
        else
        {
            var res = makeRequest("update \"18209_BOLSHIM\".SPO set SUBJECT_ID = '" + sub0 + "' where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении СПО: " + res.getErrorMsg() : "";
        }
    }

    public static String createNewTeacher(String humanID, String grade, String post, String sub0, String sub1)
    {
        var res = makeRequest("insert into \"18209_BOLSHIM\".TEACHERS values (" + humanID + ", '" + grade + "', '" + post + "', " +
                  (sub0 == null ? "NULL" : "'" + sub0 + "'") + ", "
                + (sub1 == null ? "NULL" : "'" + sub1 + "'") + ")", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при создании учителя: " + res.getErrorMsg() : "";
    }

    public static String updateTeacher(String humanID, String grade, String post, String sub0, String sub1, boolean isNewData)
    {
        if(isNewData)
            return createNewTeacher(humanID, grade, post, sub0, sub1);
        else
        {
            var res = makeRequest("update \"18209_BOLSHIM\".TEACHERS set GRADE_ID = '" + grade + "', POST_ID = '" + post +
                    "', SUBJECT_ID = '" + sub0 + "', SUBJECT2_ID = '" + sub1 + "' where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении учителя: " + res.getErrorMsg() : "";
        }
    }

    public static String createNewStudent(String humanID, String fac, String group, String cource)
    {
        var res = makeRequest("insert into \"18209_BOLSHIM\".STUDENTS values (" + humanID + ", " + group + ", '" + fac + "', " + cource + ")", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при создании студента: " + res.getErrorMsg() : "";
    }

    public static String updateStudent(String humanID, String fac, String group, String cource, boolean isNewData)
    {
        if(isNewData)
            return createNewStudent(humanID, fac, group, cource);
        else
        {
            var res = makeRequest("update \"18209_BOLSHIM\".STUDENTS set GROUP_ID = " + group + ", FACULTY_ID = '" + fac +
                    "', COURCE = " + cource + " where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении студента: " + res.getErrorMsg() : "";
        }
    }

    public static String updateHuman(String ID, String surname, String firstName, String patronymic, String typeName)
    {
        var res = makeRequest("select ID from \"18209_BOLSHIM\".READER_TYPE where NAME = '" + typeName + "'", false, false);

        if(isBadResponse(res))
            return "Ошибка при создании человека в БД: " + res.getErrorMsg();

        int typeID = 0;
        try
        {
            while(res.getSet().next())
                typeID = Integer.parseInt(res.getSet().getString(1));
        }catch (SQLException e) {
            rollbackTransaction();
            return "Ошибка при создании человека в БД: " + e.getMessage();
        } finally
        {
            res.closeAll();
        }

        res = makeRequest("update \"18209_BOLSHIM\".HUMAN set TYPE_ID = " + typeID + ", SURNAME = '" + surname + "', FIRST_NAME = '" + firstName +
                "', PATRONYMIC = '" + patronymic + "' where ID = " + ID, true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при обновлении человека в БД: " + res.getErrorMsg() : "";
    }

    public static Pair<String, String> createNewReader(String surname, String firstName, String patronymic, String typeName)
    {
        var res = makeRequest("select ID from \"18209_BOLSHIM\".READER_TYPE where NAME = '" + typeName + "'", false, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при создании читателя: " + res.getErrorMsg(), "");

        int typeID = 0;
        try
        {
            while(res.getSet().next())
                typeID = Integer.parseInt(res.getSet().getString(1));
        }catch (SQLException e) {
            rollbackTransaction();
            return new Pair<>("Ошибка при создании читателя: " + e.getMessage(), "");
        } finally
        {
            res.closeAll();
        }

        res = makeInsertWithResult("insert into \"18209_BOLSHIM\".HUMAN values (0, " + typeID + ", '" + surname + "', '"  + firstName + "', '" + patronymic + "')", "ID");
        if(isBadResponse(res))
            return new Pair<>("Ошибка при создании читателя: " + res.getErrorMsg(), "");

        var ID = "";
        try
        {
            if(res.getSet().next())
                ID = res.getSet().getString(1);
        }catch (SQLException e) {
            rollbackTransaction();
            return new Pair<>("Ошибка при создании читателя: " + e.getMessage(), "");
        } finally
        {
            res.closeAll();
        }

        res = makeInsertWithResult("insert into \"18209_BOLSHIM\".READER_CARD values (0, " + ID + ", CURRENT_DATE, CURRENT_DATE)", "ID");
        if(isBadResponse(res))
            return new Pair<>("Ошибка при создании читателя: " + res.getErrorMsg(), "");

        val humanID = ID;
        ID = "";
        try
        {
            if(res.getSet().next())
                ID = res.getSet().getString(1);
        }catch (SQLException e) {
            rollbackTransaction();
            return new Pair<>("Ошибка при создании читателя: " + e.getMessage(), "");
        } finally
        {
            res.closeAll();
        }

        var res2 = createUserDB(ID, "1111");
        if(!res2.isEmpty())
            return new Pair<>("Ошибка при создании читателя: " + res2, "");

        res = makeRequest("insert into \"18209_BOLSHIM\".CARD_ACCOUNTING values (" + ID + ", CURRENT_DATE, 'create')", true, true);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при создании читателя: " + res.getErrorMsg(), "");

        res.closeAll();

        return new Pair<>("", humanID);
    }

    public static String deleteSelectedUser(PersonCardRow card)
    {
        var res = makeRequest("delete from \"18209_BOLSHIM\".READER_CARD where ID = " + card.getID() + " and HUMAN_ID = " + card.getHumanID(), false, true);
        if(isBadResponse(res))
            return "Ошибка при удалении читателя: " + res.getErrorMsg();

        res.closeAll();

        res = makeRequest("delete from \"18209_BOLSHIM\".HUMAN where ID = " + card.getHumanID(), false, true);
        if(isBadResponse(res))
            return "Ошибка при удалении читателя: " + res.getErrorMsg();

        res.closeAll();

        res = makeRequest("insert into \"18209_BOLSHIM\".CARD_ACCOUNTING values (" + card.getID() + ", CURRENT_DATE, 'delete')", false, true);
        if(isBadResponse(res))
            return "Ошибка при удалении читателя: " + res.getErrorMsg();

        res = makeRequest("DROP USER \"18209_B_" + card.getID() + "\"", false, true);

        res.closeAll();

        val finalRes = commitTransaction();

        return !finalRes.isEmpty() ? "Ошибка при удалении читателя: " + finalRes : "";
    }

    public static Pair<String, String[]> loadVariablesOfTable(String table)
    {
        var res = makeRequest("select ID_NAME from \"18209_BOLSHIM\"." + table, true, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при загрузке названий: " + res.getErrorMsg(), null);

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

    public static Pair<String, String[][]> getTableBookStorageAccounting(int hallID)
    {
        val res = makeRequest("select B.ID, B.NAME, B.YEAR_OF_PUBL, BA.TIME, BA.OPERATION, BA.\"COUNT\"" +
                " from \"18209_BOLSHIM\".BOOK_ACCOUNTING BA inner join \"18209_BOLSHIM\".BOOKS B on B.ID = BA.BOOK_ID" +
                " where HALL_ID = " + hallID, true, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при загрузке учета склада книг: " + res.getErrorMsg(), null);

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

    public static Pair<String, String[][]> getTableBookStorage(int hallID)
    {
        val res = makeRequest("select B.ID, B.NAME, B.YEAR_OF_PUBL, B.AUTHOR, HS.COUNT from \"18209_BOLSHIM\".BOOKS B\n" +
                " inner join \"18209_BOLSHIM\".HALL_STORAGE HS on B.ID = HS.BOOK_ID\n" +
                " where HS.HALL_ID = " + hallID, true, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при загрузке списка склада книг: " + res.getErrorMsg(), null);

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

    public static Pair<String, String[][]> getTableOfCardData()
    {
        val res = makeRequest("select READER_CARD.ID, HUMAN_ID, SURNAME, FIRST_NAME, PATRONYMIC, REG_DATE, REWRITE_DATE, RT.NAME, CAN_TAKE_FOR_TIME from \"18209_BOLSHIM\".READER_CARD " +
                " inner join \"18209_BOLSHIM\".HUMAN H on H.ID = READER_CARD.HUMAN_ID" +
                " inner join \"18209_BOLSHIM\".READER_TYPE RT on H.TYPE_ID = RT.ID", true, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при загрузке списка читателей: " + res.getErrorMsg(), null);

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
        val res = makeRequest("select * from \"18209_BOLSHIM\"." + table.trim(), true, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при загрузке строк выбранной таблицы: " + res.getErrorMsg(), null);

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
        val res = makeRequest("select NAME from \"18209_BOLSHIM\".READER_TYPE", true, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при загрузке типов читателей: " + res.getErrorMsg(), null);

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
        return new Pair<>("", new String[] {"SPO", "ASSISTANT", "HALL_TYPE", "LIBRARY_HALLS", "BOOKS", "HALL_STORAGE",
                                                "BOOK_ACCOUNTING", "DEPARTMENT", "VIOLATIONS", "ACCEPTING_RETURNING_BOOKS",
                                                "MA_ORDER", "VIOLATION_TYPES", "CARD_ACCOUNTING", "READER_TYPE", "HUMAN",
                                                "READER_CARD", "FACULTY", "GROUPS", "TEACHER_GRADE", "TEACHER_POST",
                                                "SUBJECTS", "STUDENTS", "TEACHERS"});
    }
}
