package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBCore;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;

public class EnterController
{
    @FXML
    private TextField login;

    @FXML
    private PasswordField password;

    @Setter
    private Stage root;

    @FXML
    private void login()
    {
        val log = login.getText();
        val passw = password.getText();

        if(log.trim().isEmpty() || passw.trim().isEmpty())
            Dialogs.showDefaultAlert(root, "Error", "Enter login data!", Alert.AlertType.ERROR);
        else
        {
            val result = DBCore.initSession(log.trim(), passw.trim());

            if(result.isEmpty())
            {
                root.close();

                Dialogs.showDefaultAlert(null, "Success!", "Welcome, " + log.trim() + "!", Alert.AlertType.INFORMATION);
                WindowLoader.loadMainWindow(log.trim());
            }else
                Dialogs.showDefaultAlert(root, "Error!", result, Alert.AlertType.ERROR);
        }
    }
}
