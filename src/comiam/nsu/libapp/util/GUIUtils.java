package comiam.nsu.libapp.util;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.db.objects.*;
import comiam.nsu.libapp.gui.Dialogs;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;

public class GUIUtils
{
    public static void autoResizeColumns(TableView<?> table)
    {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().forEach(column ->
        {
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++)
            {
                if (column.getCellData(i) != null)
                {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    if (calcwidth > max)
                        max = calcwidth;
                }
            }
            column.setPrefWidth(max + 10.0d);
        });
    }

    public static boolean showError(Stage root, String message)
    {
        if(message != null && !message.isEmpty())
        {
            Dialogs.showDefaultAlert(root, "Error!", message, Alert.AlertType.ERROR);
            return true;
        }else
            return false;
    }

    public static ObservableList<String> getHalls(Stage root)
    {
        val data = DBActions.getHallNames();

        if(showError(root, data.getFirst()))
            return null;

        return FXCollections.observableArrayList(data.getSecond());
    }

    public static ObservableList<String> getTypeNames(Stage root)
    {
        val data = DBActions.getTypeNames();

        if(showError(root, data.getFirst()))
            return null;

        return FXCollections.observableArrayList(data.getSecond());
    }

    public static ObservableList<String> getTableNames(Stage root)
    {
        val data = DBActions.getTableNames();

        if(showError(root, data.getFirst()))
            return null;

        return FXCollections.observableArrayList(data.getSecond());
    }

    public static ObservableList<ViolationRow> getUserViolations(Stage root, int userID)
    {
        val res = DBActions.getUserViolations(userID);

        if(showError(root, res.getFirst()))
            return null;

        val table = res.getSecond();
        val rows = new ArrayList<ViolationRow>();

        for (val row : table)
            rows.add(new ViolationRow(row[0], row[1], row[2], row[3], row[4], row[5]));

        return FXCollections.observableArrayList(rows);
    }

    public static ObservableList<OrderRow> getUserOrderRows(Stage root, int userID)
    {
        val res = DBActions.getUserOrders(userID);

        if(showError(root, res.getFirst()))
            return null;

        val table = res.getSecond();
        val rows = new ArrayList<OrderRow>();

        for (val row : table)
            rows.add(new OrderRow(row[0], row[1], row[2], row[3], row[4], row[5]));

        return FXCollections.observableArrayList(rows);
    }

    public static ObservableList<BookOperationCardRow> getBookStorageAccountingRows(Stage root, int hallID)
    {
        val res = DBActions.getTableBookStorageAccounting(hallID);

        if(showError(root, res.getFirst()))
            return null;

        val table = res.getSecond();
        val rows = new ArrayList<BookOperationCardRow>();

        for (val row : table)
            rows.add(new BookOperationCardRow(row[0], row[1], row[2], row[3], row[4], row[5]));

        return FXCollections.observableArrayList(rows);
    }

    public static ObservableList<BookStorageRow> getBookStorageRows(Stage root, int hallID)
    {
        val res = DBActions.getTableBookStorage(hallID);

        if(showError(root, res.getFirst()))
            return null;

        val table = res.getSecond();
        val rows = new ArrayList<BookStorageRow>();

        for (val row : table)
            rows.add(new BookStorageRow(row[0], row[1], row[2], row[3], row[4]));

        return FXCollections.observableArrayList(rows);
    }

    public static ObservableList<PersonCardRow> getCardRows(Stage root)
    {
        val res = DBActions.getTableOfCardData();

        if(showError(root, res.getFirst()))
            return null;

        val table = res.getSecond();
        val cards = new ArrayList<PersonCardRow>();

        for (val row : table)
            cards.add(new PersonCardRow(row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]));

        return FXCollections.observableArrayList(cards);
    }

    public static ObservableList<String> toList(String[] vals)
    {
        return FXCollections.observableArrayList(vals);
    }

    public static TableView<String[]> getFreeSizeVariableTableFromRequest(Stage root, String tableName)
    {
        val res = DBActions.getTableValues(tableName);

        if(showError(root, res.getFirst()))
            return null;

        val table = res.getSecond();
        val tableView = new TableView<String[]>();
        val columns = new ArrayList<TableColumn<String[], String>>();
        for(int i = 0;i < table[0].length;i++)
        {
            final int I = i;
            val col = new TableColumn<String[],String>(table[0][i]);
            col.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()[I]));

            columns.add(col);
        }

        val values = new ArrayList<>(Arrays.asList(table).subList(1, table.length));
        val data = FXCollections.observableArrayList(values);

        tableView.getColumns().addAll(columns);
        tableView.setItems(data);
        autoResizeColumns(tableView);

        return tableView;
    }
}
