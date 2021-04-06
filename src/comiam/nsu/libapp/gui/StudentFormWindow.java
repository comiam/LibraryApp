package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.collections.FXCollections;
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

public class StudentFormWindow
{
    @FXML
    private ChoiceBox<String> faculty;
    @FXML
    private ChoiceBox<String> group;
    @FXML
    private TextField course;
    @FXML
    private Button save;

    @Setter
    String humanID;
    @Setter
    Stage root;

    @FXML
    private void initialize()
    {
        val fac = DBActions.loadVariablesOfTable("FACULTY");
        val groups = DBActions.loadVariablesOfTable("GROUPS");

        if(checkStrArgs(fac.getFirst(), groups.getFirst()))
            Dialogs.showDefaultAlert(null, "Error!", fac.getFirst() + "\n" + groups.getFirst(), Alert.AlertType.ERROR);
        else
        {
            faculty.setItems(GUIUtils.toList(fac.getSecond()));
            group.setItems(GUIUtils.toList(groups.getSecond()));
        }

        faculty.setOnAction(e -> {
            if(faculty.getItems().isEmpty())
            {
                val fac0 = DBActions.loadVariablesOfTable("FACULTY");
                if(fac0.getFirst().isEmpty())
                    faculty.setItems(FXCollections.observableArrayList(fac0.getSecond()));
            }
        });

        group.setOnAction(e -> {
            if(group.getItems().isEmpty())
            {
                val fac0 = DBActions.loadVariablesOfTable("GROUPS");
                if(fac0.getFirst().isEmpty())
                    group.setItems(FXCollections.observableArrayList(fac0.getSecond()));
            }
        });

        save.setOnAction(e -> {
            val fac0 = faculty.getSelectionModel().getSelectedItem();
            val gr = group.getSelectionModel().getSelectedItem();
            val course0 = course.getText().trim();

            if(!checkStrArgs(fac0, gr, course0))
            {
                showError(root, "Some fields are empty!");
                return;
            }

            if(!showError(root, DBActions.createNewStudent(humanID, fac0, gr, course0)))
            {
                Dialogs.showDefaultAlert(root, "Success!", "Student created successfully!", Alert.AlertType.INFORMATION);
                root.close();
            }
        });

    }
}
