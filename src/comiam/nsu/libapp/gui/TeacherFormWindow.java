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

public class TeacherFormWindow
{
    @FXML
    private ChoiceBox<String> grade;
    @FXML
    private ChoiceBox<String> post;
    @FXML
    private ChoiceBox<String> sub0;
    @FXML
    private ChoiceBox<String> sub1;
    @FXML
    private Button save;

    @Setter
    String humanID;
    @Setter
    Stage root;

    @FXML
    private void initialize()
    {
        val grades = DBActions.loadVariablesOfTable("TEACHER_GRADE");
        val posts = DBActions.loadVariablesOfTable("TEACHER_POST");
        val subjects = DBActions.loadVariablesOfTable("SUBJECTS");

        if(checkStrArgs(grades.getFirst(), posts.getFirst(), subjects.getFirst()))
            showError(root, grades.getFirst() + "\n" + posts.getFirst() + "\n" + subjects.getFirst());
        else
        {
            grade.setItems(GUIUtils.toList(grades.getSecond()));
            post.setItems(GUIUtils.toList(posts.getSecond()));
            sub0.setItems(GUIUtils.toList(subjects.getSecond()));
            sub1.setItems(GUIUtils.toList(subjects.getSecond()));
        }

        grade.setOnAction(e -> {
            if(grade.getItems().isEmpty())
            {
                val fac0 = DBActions.loadVariablesOfTable("TEACHER_GRADE");
                if(fac0.getFirst().isEmpty())
                    grade.setItems(FXCollections.observableArrayList(fac0.getSecond()));
            }
        });

        post.setOnAction(e -> {
            if(post.getItems().isEmpty())
            {
                val fac0 = DBActions.loadVariablesOfTable("TEACHER_POST");
                if(fac0.getFirst().isEmpty())
                    post.setItems(FXCollections.observableArrayList(fac0.getSecond()));
            }
        });

        sub0.setOnAction(e -> {
            if(sub0.getItems().isEmpty())
            {
                val fac0 = DBActions.loadVariablesOfTable("SUBJECTS");
                if(fac0.getFirst().isEmpty())
                    sub0.setItems(FXCollections.observableArrayList(fac0.getSecond()));
            }
        });

        sub1.setOnAction(e -> {
            if(sub1.getItems().isEmpty())
            {
                val fac0 = DBActions.loadVariablesOfTable("SUBJECTS");
                if(fac0.getFirst().isEmpty())
                    sub1.setItems(FXCollections.observableArrayList(fac0.getSecond()));
            }
        });

        save.setOnAction(e -> {
            val gr = grade.getSelectionModel().getSelectedItem();
            val p = post.getSelectionModel().getSelectedItem();
            val s0 = sub0.getSelectionModel().getSelectedItem();
            val s1 = sub1.getSelectionModel().getSelectedItem();

            if(!checkStrArgs(gr, p) && s0 == null && s1 == null)
            {
                showError(root, "Some fields are empty!");
                return;
            }

            if(!showError(root, DBActions.createNewTeacher(humanID, gr, p, s0, s1)))
            {
                Dialogs.showDefaultAlert(root, "Success!", "Teacher created successfully!", Alert.AlertType.INFORMATION);
                root.close();
            }
        });
    }
}
