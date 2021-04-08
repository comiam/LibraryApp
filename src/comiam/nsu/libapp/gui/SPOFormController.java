package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;

import static comiam.nsu.libapp.util.GUIUtils.showError;
import static comiam.nsu.libapp.util.StringChecker.checkStrArgs;

public class SPOFormController
{
    @FXML
    private ChoiceBox<String> sub0;
    @FXML
    private Button save;

    @Setter
    String humanID;
    @Setter
    Stage root;

    @FXML
    private void initialize()
    {
        val subjects = DBActions.loadVariablesOfTable("SUBJECTS");

        if(checkStrArgs(subjects.getFirst()))
            showError(root,  subjects.getFirst());
        else
            sub0.setItems(GUIUtils.toList(subjects.getSecond()));

        sub0.setOnAction(e -> {
            if(sub0.getItems().isEmpty())
            {
                val fac0 = DBActions.loadVariablesOfTable("SUBJECTS");
                if(fac0.getFirst().isEmpty())
                    sub0.setItems(FXCollections.observableArrayList(fac0.getSecond()));
            }
        });
    }

    public void setIsEditingUser(boolean isEditingUser)
    {
        boolean isEmptyData = false;
        if(isEditingUser)
        {
            val res = DBActions.getRowInfoFrom(humanID, "HUMAN_ID", "SPO");

            isEmptyData = res.getFirst().equals("empty");
            if(!isEmptyData && showError(root, res.getFirst()))
                root.close();
            else
                sub0.setValue(res.getSecond()[1]);
        }

        boolean finalIsEmptyData = isEmptyData;
        save.setOnAction(e -> {
            val s0 = sub0.getSelectionModel().getSelectedItem();

            if(s0 == null)
            {
                showError(root, "Some fields are empty!");
                return;
            }

            if(!isEditingUser)
            {
                if(!showError(root, DBActions.createNewSPO(humanID, s0)))
                {
                    Dialogs.showDefaultAlert(root, "Success!", "SPO updated successfully!", Alert.AlertType.INFORMATION);
                    root.close();
                }
            }else
            {
                if(!showError(root, DBActions.updateSPO(humanID, s0, finalIsEmptyData)))
                {
                    Dialogs.showDefaultAlert(root, "Success!", "SPO updated successfully!", Alert.AlertType.INFORMATION);
                    root.close();
                }
            }
        });
    }
}
