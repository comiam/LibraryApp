package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBCore;
import comiam.nsu.libapp.util.GUIUtils;
import comiam.nsu.libapp.util.Pair;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.val;

import java.util.HashMap;

import static comiam.nsu.libapp.gui.Dialogs.*;

public class WindowLoader
{
    public static void loadEnterWindow()
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EnterController.class.getResource("EnterWindow.fxml"));
            Parent root = loader.load();
            EnterController controller = loader.getController();
            controller.setRoot(newWindow);

            newWindow.setTitle("Hello");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 203, 257));
            newWindow.centerOnScreen();
            newWindow.show();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadStudentFormWindow(String humanID)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("StudentFormWindow.fxml"));
            Parent root = loader.load();
            StudentFormWindow controller = loader.getController();
            controller.setHumanID(humanID);

            newWindow.setTitle("Add new student");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 303, 225));
            newWindow.centerOnScreen();
            newWindow.show();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadEnterReaderOneWindow(MainController parent)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("EnterReaderStepOne.fxml"));
            Parent root = loader.load();
            EnterReaderOneController controller = loader.getController();
            controller.setRoot(newWindow);
            controller.setController(parent);

            newWindow.setTitle("Add new user");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 303, 225));
            newWindow.centerOnScreen();
            newWindow.show();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadMainWindow(String name)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("MainWindow.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setRoot(newWindow);

            newWindow.setTitle(name);
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 640, 501));
            newWindow.centerOnScreen();

            newWindow.setOnCloseRequest(e -> {
                DBCore.closeCurrentSession();
                Platform.exit();
            });

            newWindow.show();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }
}
