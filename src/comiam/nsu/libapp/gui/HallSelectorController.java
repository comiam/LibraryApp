package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBCore;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.var;

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
    @Setter
    private int userID;
    @Setter
    private boolean isReaderUser;

    @FXML
    private void initialize()
    {
        okBtn.setOnAction(e -> {
            var hall = halls.getValue();

            if(hall == null)
            {
                showError(root, "Выберите залл!");
                return;
            }

            root.close();

            int hallID = hall.equals("Дома") ? -1 : Integer.parseInt(hall.split(":")[0].trim());
            if(isReaderUser)
                WindowLoader.loadUserWindow(userName, hallID, userID);
            else
                WindowLoader.loadLibraryWindow(userName, hallID, hall.split(":")[1].trim().equals("intercol"));
        });
    }

    public void postInit()
    {
        var data = GUIUtils.getHalls(root);
        if(data == null)
        {
            root.close();
            DBCore.closeCurrentSession();
            Platform.exit();
            return;
        }
        halls.setItems(data);

        if(isReaderUser)
            data.add("Дома");
    }
}
