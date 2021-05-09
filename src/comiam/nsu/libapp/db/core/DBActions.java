package comiam.nsu.libapp.db.core;

import comiam.nsu.libapp.db.objects.PersonCardRow;
import comiam.nsu.libapp.util.Pair;
import lombok.val;
import lombok.var;

import java.sql.SQLException;

import static comiam.nsu.libapp.db.core.DBCore.*;

public class DBActions
{
    public static String checkUserPassword(boolean isReaderUser, String login, String password)
    {
        var res = makeRequest("select * from " + (isReaderUser ? "PASSWORDS" : "ADMIN_PASSWORDS") +
                " where ID = '" + login + "' and PASSW = '" + password + "'", true, false);
        if(isBadResponse(res))
            return "Не получилось проверить данные пользователя: " + res.getErrorMsg();

        try
        {
            val rs = res.getSet();

            int rows = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();

            return rows > 0 ? "" : "В базе нет такого аккаунта!";
        }catch (SQLException e) {
            return e.getMessage();
        } finally
        {
            res.closeAll();
        }
    }

    public static String saveUserData(int userID, String firstname, String surname, String patronymic, String password)
    {
        var res = makeRequest("select HUMAN_ID from READER_CARD where ID = " + userID, false, false);
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

        res = makeRequest("update HUMAN set FIRST_NAME = '" + firstname + "', SURNAME = '"
                + surname + "', PATRONYMIC = '" + patronymic + "' where ID = " + humanID, false, true);
        if(isBadResponse(res))
            return "Не получилось обновить пользователя: " + res.getErrorMsg();

        res = makeRequest("update PASSWORDS set PASSW = '" + password + "' where ID = " + userID, true, true);
        if(isBadResponse(res))
            return "Не получилось обновить пользователя: " + res.getErrorMsg();

        return "";
    }

    public static Pair<String, String[]> getUserData(int userID)
    {
        var res = makeRequest("select FIRST_NAME, SURNAME, PATRONYMIC, PASSW from READER_CARD " +
                        "inner join HUMAN H on H.ID = READER_CARD.HUMAN_ID " +
                        "inner join PASSWORDS P on READER_CARD.ID = P.ID where READER_CARD.ID = " + userID,
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
            val row = new String[columns];
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
        var res = makeRequest("select BOOK_ID, NAME, HALL_ID, VIOLATION_DATE, VIOLATION_TYPE, MONETARY_FINE from VIOLATIONS " +
                        "inner join BOOKS B on B.ID = VIOLATIONS.BOOK_ID where CARD_ID = " + userID,
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
                "RETURN_STATE from MA_ORDER inner join BOOKS B on B.ID = MA_ORDER.BOOK_ID where CARD_ID = " + userID,
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
        var res = makeRequest("select V.BOOK_ID, V.CARD_ID, V.HALL_ID, V.VIOLATION_DATE, V.VIOLATION_TYPE, H.SURNAME, H.FIRST_NAME, H.PATRONYMIC, B.NAME from VIOLATIONS V\n" +
                " inner join READER_CARD RC on V.CARD_ID = RC.ID\n" +
                " inner join HUMAN H on H.ID = RC.HUMAN_ID\n" +
                " inner join BOOKS B on B.ID = V.BOOK_ID\n" +
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
        var res = makeRequest("select COST from BOOKS where ID = " + bookID, true, false);
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
        var res = makeRequest("select LH.ID, HT.ID_NAME from LIBRARY_HALLS LH inner join HALL_TYPE HT on HT.ID_NAME = LH.HALL_TYPE_ID", true, false);
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
        var res = makeRequest("select ID, NAME, YEAR_OF_PUBL from BOOKS", true, false);
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
        var res = makeRequest("select H.SURNAME, H.FIRST_NAME, H.PATRONYMIC, READER_CARD.ID from READER_CARD inner join HUMAN H on H.ID = READER_CARD.HUMAN_ID", true, false);
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
        var res = makeRequest("select * from " + table + " where " + compareColumn + " = " + comparableValue, true, false);
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
        var res = makeRequest("insert into ASSISTANT values (" + humanID + ", '" + sub0 + "')", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при создании ассистента: " + res.getErrorMsg() : "";
    }

    public static String updateAssistant(String humanID, String sub0, boolean isNewData)
    {
        if(isNewData)
            return createNewAssistant(humanID, sub0);
        else
        {
            var res = makeRequest("update ASSISTANT set SUBJECT_ID = '" + sub0 + "' where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении ассистента: " + res.getErrorMsg() : "";
        }
    }

    public static String createNewSPO(String humanID, String sub0)
    {
        var res = makeRequest("insert into SPO values (" + humanID + ", '" + sub0 + "')", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при создании СПО: " + res.getErrorMsg() : "";
    }

    public static String updateSPO(String humanID, String sub0, boolean isNewData)
    {
        if(isNewData)
            return createNewSPO(humanID, sub0);
        else
        {
            var res = makeRequest("update SPO set SUBJECT_ID = '" + sub0 + "' where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении СПО: " + res.getErrorMsg() : "";
        }
    }

    public static String createNewTeacher(String humanID, String grade, String post, String sub0, String sub1)
    {
        var res = makeRequest("insert into TEACHERS values (" + humanID + ", '" + grade + "', '" + post + "', " +
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
            var res = makeRequest("update TEACHERS set GRADE_ID = '" + grade + "', POST_ID = '" + post +
                    "', SUBJECT_ID = '" + sub0 + "', SUBJECT2_ID = '" + sub1 + "' where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении учителя: " + res.getErrorMsg() : "";
        }
    }

    public static String createNewStudent(String humanID, String fac, String group, String cource)
    {
        var res = makeRequest("insert into STUDENTS values (" + humanID + ", " + group + ", '" + fac + "', " + cource + ")", true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при создании студента: " + res.getErrorMsg() : "";
    }

    public static String updateStudent(String humanID, String fac, String group, String cource, boolean isNewData)
    {
        if(isNewData)
            return createNewStudent(humanID, fac, group, cource);
        else
        {
            var res = makeRequest("update STUDENTS set GROUP_ID = " + group + ", FACULTY_ID = '" + fac +
                    "', COURCE = " + cource + " where HUMAN_ID = " + humanID, true, true);
            res.closeAll();
            return isBadResponse(res) ? "Ошибка при обновлении студента: " + res.getErrorMsg() : "";
        }
    }

    public static String updateHuman(String ID, String surname, String firstName, String patronymic, String typeName)
    {
        var res = makeRequest("select ID from READER_TYPE where NAME = '" + typeName + "'", false, false);

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

        res = makeRequest("update HUMAN set TYPE_ID = " + typeID + ", SURNAME = '" + surname + "', FIRST_NAME = '" + firstName +
                "', PATRONYMIC = '" + patronymic + "' where ID = " + ID, true, true);
        res.closeAll();
        return isBadResponse(res) ? "Ошибка при обновлении человека в БД: " + res.getErrorMsg() : "";
    }

    public static Pair<String, String> createNewReader(String surname, String firstName, String patronymic, String typeName)
    {
        var res = makeRequest("select ID from READER_TYPE where NAME = '" + typeName + "'", false, false);

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

        res = makeInsertWithResult("insert into HUMAN values (0, " + typeID + ", '" + surname + "', '"  + firstName + "', '" + patronymic + "')", "ID");
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

        res = makeInsertWithResult("insert into READER_CARD values (0, " + ID + ", CURRENT_DATE, CURRENT_DATE)", "ID");
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

        res = makeRequest("insert into PASSWORDS values(" + humanID + ", '1111')", false, true);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при создании читателя: " + res.getErrorMsg(), "");

        res = makeRequest("insert into CARD_ACCOUNTING values (" + ID + ", CURRENT_DATE, 'create')", true, true);
        if(isBadResponse(res))
            return new Pair<>("Ошибка при создании читателя: " + res.getErrorMsg(), "");

        res.closeAll();

        return new Pair<>("", humanID);
    }

    public static String deleteSelectedUser(PersonCardRow card)
    {
        var res = makeRequest("delete from READER_CARD where ID = " + card.getID() + " and HUMAN_ID = " + card.getHumanID(), false, true);
        if(isBadResponse(res))
            return "Ошибка при удалении читателя: " + res.getErrorMsg();

        res.closeAll();

        res = makeRequest("delete from HUMAN where ID = " + card.getHumanID(), false, true);
        if(isBadResponse(res))
            return "Ошибка при удалении читателя: " + res.getErrorMsg();

        res.closeAll();

        res = makeRequest("insert into CARD_ACCOUNTING values (" + card.getID() + ", CURRENT_DATE, 'delete')", false, true);
        if(isBadResponse(res))
            return "Ошибка при удалении читателя: " + res.getErrorMsg();

        res.closeAll();

        val finalRes = commitTransaction();

        return !finalRes.isEmpty() ? "Ошибка при удалении читателя: " + finalRes : "";
    }

    public static Pair<String, String[]> loadVariablesOfTable(String table)
    {
        var res = makeRequest("select ID_NAME from " + table, true, false);

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
                " from BOOK_ACCOUNTING BA inner join BOOKS B on B.ID = BA.BOOK_ID" +
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
        val res = makeRequest("select B.ID, B.NAME, B.YEAR_OF_PUBL, B.AUTHOR, HS.COUNT from BOOKS B\n" +
                " inner join HALL_STORAGE HS on B.ID = HS.BOOK_ID\n" +
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
        val res = makeRequest("select READER_CARD.ID, HUMAN_ID, SURNAME, FIRST_NAME, PATRONYMIC, REG_DATE, REWRITE_DATE, RT.NAME, CAN_TAKE_FOR_TIME from READER_CARD " +
                " inner join HUMAN H on H.ID = READER_CARD.HUMAN_ID" +
                " inner join READER_TYPE RT on H.TYPE_ID = RT.ID", true, false);

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
        val res = makeRequest("select * from " + table.trim(), true, false);

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
        val res = makeRequest("select NAME from READER_TYPE", true, false);

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
        val res = makeRequest("select table_name from user_tables", true, false);

        if(isBadResponse(res))
            return new Pair<>("Ошибка при загрузке списков таблиц: " + res.getErrorMsg(), null);

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

                if(!n.equals("ADMIN_PASSWORDS") && !n.equals("PASSWORDS") && !n.equals("HTMLDB_PLAN_TABLE") && !n.isEmpty())//ignore internal tables
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
