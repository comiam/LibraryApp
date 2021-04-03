package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;

public class EnterReaderOneController
{
    @FXML
    private TextField surname;
    @FXML
    private TextField firstName;
    @FXML
    private TextField patronymic;
    @FXML
    private ChoiceBox<String> typesBox;
    @FXML
    private Button continueBtn;

    @Setter
    private Stage root;

    @Setter MainController controller;

    @FXML
    public void initialize()
    {


        continueBtn.setOnAction(e -> {
            val surn = surname.getText().trim();
            val firstn = firstName.getText().trim();
            val patr = patronymic.getText().trim();
            val selected = typesBox.getSelectionModel().getSelectedItem();

            if(selected == null || surn.isEmpty() || firstn.isEmpty() || patr.isEmpty())
            {
                Dialogs.showDefaultAlert(root, "Error!", "One of fields is empty!", Alert.AlertType.ERROR);
                return;
            }

            if(!selected.split(":")[1].trim().equals("abitura"))
            {
                Dialogs.showDefaultAlert(root, "Error!", "Sorry, but this type not supported!", Alert.AlertType.ERROR);
                return;
            }

            val ans = DBActions.insertNewUser(surn, firstn, patr, selected.split(":")[0].trim());
            if(!ans.isEmpty())
                Dialogs.showDefaultAlert(root, "Error!", ans, Alert.AlertType.ERROR);
            else
            {
                Dialogs.showDefaultAlert(root, "Success!", "User " + surn + " " + firstn + " " + patr + " is created!", Alert.AlertType.INFORMATION);
                controller.refresh();
                root.close();
            }
        });

        val types = GUIUtils.getTypeNames(root);

        if(types != null)
            typesBox.setItems(types);
    }
}
