package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.DBActions;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainController
{
    @FXML
    private ChoiceBox<String> tables;

    @FXML
    private ScrollPane tablePanel;

    @FXML
    private Button refresh;

    @Setter
    private Stage root;

    @FXML
    public void initialize()
    {
        refresh.setOnAction(e -> {
            val data = DBActions.getTableNames();

            if(!data.getFirst().isEmpty() || data.getSecond() == null)
            {
                Dialogs.showDefaultAlert(root, "Error!", "Can't refresh tables! Error: " + data.getFirst(), Alert.AlertType.ERROR);
                return;
            }
            val coll = FXCollections.observableArrayList(data.getSecond());
            tables.setItems(coll);
        });

        tables.setOnAction(e -> {
            val tableName = tables.getValue();
            val res = DBActions.getTableValues(tableName);

            if(!res.getFirst().isEmpty() || res.getSecond() == null)
            {
                Dialogs.showDefaultAlert(root, "Error!", "Can't load table + " + tableName + " +! Error: " + res.getFirst(), Alert.AlertType.ERROR);
                return;
            }

            val table = res.getSecond();
            val tableView = new TableView<String[]>();
            val columns = new ArrayList<TableColumn<String[], String>>();
            for(int i = 0;i < table[0].length;i++)
            {
                final int I = i;
                val col = new TableColumn<String[],String>(table[0][i]);
                col.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue()[I]));

                columns.add(col);
            }

            var list = new ArrayList<String[]>(table);
            list.remove(0);
            ObservableList<String[]> data = FXCollections.observableArrayList(list);

            tableView.getColumns().addAll(columns);
            tableView.setItems(data);

            tablePanel.setContent(tableView);
        });
    }
}
