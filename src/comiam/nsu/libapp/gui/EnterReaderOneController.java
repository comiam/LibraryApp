package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.fxml.FXML;
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

    @Setter MainController controller;

    @FXML
    public void initialize()
    {
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

            val res = DBActions.createNewReader(surn, firstn, patr, selected);
            if(!showError(root, res.getFirst()))
                controller.refresh();

            root.close();

            switch (selected)
            {
                case "student":
                    WindowLoader.loadStudentFormWindow(res.getSecond());
                    break;
                case "teacher":
                    WindowLoader.loadTeacherFormWindow(res.getSecond());
                    break;
                case "spo":
                    WindowLoader.loadSPOFormWindow(res.getSecond());
                    break;
                case "assistant":
                    WindowLoader.loadAssistantFormWindow(res.getSecond());
                default:
                    break;
            }
        });

        val types = GUIUtils.getTypeNames(root);

        if(types != null)
            typesBox.setItems(types);
    }
}
