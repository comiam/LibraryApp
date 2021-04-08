package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBCore;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;

import static comiam.nsu.libapp.util.GUIUtils.showError;

public class HallSelectorController
{
    @FXML
    private ChoiceBox<String> halls;
    @FXML
    private Button okBtn;
    @Setter
    private Stage root;
    @Setter
    private String userName;

    @FXML
    private void initialize()
    {
        val data = GUIUtils.getHalls(root);
        if(data == null)
        {
            root.close();
            DBCore.closeCurrentSession();
            Platform.exit();
            return;
        }
        halls.setItems(data);

        okBtn.setOnAction(e -> {
            val hall = halls.getValue();

            if(hall == null)
            {
                showError(root, "Please select hall!");
                return;
            }

            root.close();
            WindowLoader.loadMainWindow(userName, Integer.parseInt(hall.split(":")[0].trim()));
        });
    }
}
