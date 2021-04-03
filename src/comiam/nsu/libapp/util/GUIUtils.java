package comiam.nsu.libapp.util;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.db.objects.PersonCard;
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

    public static ObservableList<String> getTypeNames(Stage root)
    {
        val data = DBActions.getTypeNames();

        if(!data.getFirst().isEmpty() || data.getSecond() == null)
        {
            Dialogs.showDefaultAlert(root, "Error!", "Can't refresh tables! Error: " + data.getFirst(), Alert.AlertType.ERROR);
            return null;
        }
        return FXCollections.observableArrayList(data.getSecond());
    }

    public static ObservableList<String> getTableNames(Stage root)
    {
        val data = DBActions.getTableNames();

        if(!data.getFirst().isEmpty() || data.getSecond() == null)
        {
            Dialogs.showDefaultAlert(root, "Error!", "Can't refresh tables! Error: " + data.getFirst(), Alert.AlertType.ERROR);
            return null;
        }
        return FXCollections.observableArrayList(data.getSecond());
    }

    public static ObservableList<PersonCard> getCardRows(Stage root)
    {
        val res = DBActions.getTableOfCardData();

        if(!res.getFirst().isEmpty() || res.getSecond() == null)
        {
            Dialogs.showDefaultAlert(root, "Error!", "Can't load table of reader cards!\nError: " + res.getFirst(), Alert.AlertType.ERROR);
            return null;
        }

        val table = res.getSecond();
        val cards = new ArrayList<PersonCard>();

        for (val row : table)
            cards.add(new PersonCard(row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]));

        return FXCollections.observableArrayList(cards);
    }

    public static TableView<String[]> getFreeSizeVariableTableFromRequest(Stage root, String tableName)
    {
        val res = DBActions.getTableValues(tableName);

        if(!res.getFirst().isEmpty() || res.getSecond() == null)
        {
            Dialogs.showDefaultAlert(root, "Error!", "Can't load table + " + tableName + "!\nError: " + res.getFirst(), Alert.AlertType.ERROR);
            return null;
        }

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
