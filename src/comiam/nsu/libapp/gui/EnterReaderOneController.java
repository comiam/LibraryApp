package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.util.GUIUtils;
import comiam.nsu.libapp.util.Pair;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;

import static comiam.nsu.libapp.util.GUIUtils.showError;
import static comiam.nsu.libapp.util.StringChecker.checkStrArgs;

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
    @Setter
    MainController controller;
    @Setter
    String humanID;
    @Setter
    String type;

    @FXML
    private void initialize()
    {
        val types = GUIUtils.getTypeNames(root);

        if(types != null)
            typesBox.setItems(types);
        else
            root.close();
    }

    public void setIsEditingUser(boolean isEditingUser)
    {
        if(isEditingUser)
        {
            val res0 = DBActions.getRowInfoFrom(humanID, "ID", "HUMAN");
            if(!showError(root, res0.getFirst()))
            {
                surname.setText(res0.getSecond()[2]);
                firstName.setText(res0.getSecond()[3]);
                patronymic.setText(res0.getSecond()[4]);
                typesBox.setValue(type);
            }else
                root.close();
        }

        continueBtn.setOnAction(e -> {
            val surn = surname.getText().trim();
            val firstn = firstName.getText().trim();
            val patr = patronymic.getText().trim();
            val selected = typesBox.getSelectionModel().getSelectedItem();

            if(!checkStrArgs(surn, firstn, patr, selected))
            {
                showError(root, "One of fields is empty!");
                return;
            }

            Pair<String, String> res = new Pair<>(null, null);

            if(!isEditingUser)
                res = DBActions.createNewReader(surn, firstn, patr, selected);
            else
            {
                res.setFirst(DBActions.updateHuman(humanID, surn, firstn, patr, selected));
                res.setSecond(humanID);
            }

            if(!showError(root, res.getFirst()))
                controller.refreshUsersCardTable();

            root.close();

            switch (selected)
            {
                case "student":
                    WindowLoader.loadStudentFormWindow(res.getSecond(), isEditingUser);
                    break;
                case "teacher":
                    WindowLoader.loadTeacherFormWindow(res.getSecond(), isEditingUser);
                    break;
                case "spo":
                    WindowLoader.loadSPOFormWindow(res.getSecond(), isEditingUser);
                    break;
                case "assistant":
                    WindowLoader.loadAssistantFormWindow(res.getSecond(), isEditingUser);
                default:
                    Dialogs.showDefaultAlert(root, "Success!", "Abiturient updated successfully!", Alert.AlertType.INFORMATION);
                    break;
            }
        });
    }
}
