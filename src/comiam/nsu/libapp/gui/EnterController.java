package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.db.core.DBCore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;
import lombok.var;

import static comiam.nsu.libapp.util.GUIUtils.showError;
import static comiam.nsu.libapp.util.StringChecker.checkStrArgs;
import static comiam.nsu.libapp.util.StringChecker.isInteger;

public class EnterController
{
    @FXML
    private TextField login;

    @FXML
    private PasswordField password;

    @Setter
    private Stage root;

    @FXML
    private void initialize()
    {
        login.setOnKeyReleased(e -> {
            if(e.getCode() == KeyCode.ENTER)
                login();
        });
        password.setOnKeyTyped(e -> {
            if(e.getCode() == KeyCode.ENTER)
                login();
        });
        login.requestFocus();
    }

    @FXML
    private void login()
    {
        val log = login.getText().trim();
        val passw = password.getText().trim();

        if(!checkStrArgs(log, passw))
            Dialogs.showDefaultAlert(root, "Ошибка", "Введите данные для входа!", Alert.AlertType.ERROR);
        else
        {
            val result = DBCore.initSession();

            if(result.isEmpty())
            {
                var res = DBActions.checkUserPassword(isInteger(log), log, passw);
                if(!res.isEmpty())
                    showError(root, res);
                else
                {
                    root.close();

                    var name = log;
                    if(isInteger(log))
                    {
                        var res2 = DBActions.getUserData(Integer.parseInt(log));
                        if(!res2.getFirst().isEmpty())
                        {
                            showError(root, res2.getFirst());
                            root.close();
                            DBCore.closeCurrentSession();
                            Platform.exit();
                            return;
                        }
                        else
                            name = res2.getSecond()[1] + " " + res2.getSecond()[0] + " " + res2.getSecond()[2];

                    }
                    Dialogs.showDefaultAlert(null, "Успех!", "Добро пожаловать, " + name + "!", Alert.AlertType.INFORMATION);
                    WindowLoader.loadHallSelectorWindow(name, isInteger(log) ? Integer.parseInt(log) : 0, isInteger(log));
                }
            }else
                Dialogs.showDefaultAlert(root, "Ошибка!", result, Alert.AlertType.ERROR);
        }
    }
}
