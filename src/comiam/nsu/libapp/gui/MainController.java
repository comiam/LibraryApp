package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.DBActions;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;

public class MainController
{
    @FXML
    private ChoiceBox<String> tables;

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
            val table = tables.getValue();


        });
    }
}
