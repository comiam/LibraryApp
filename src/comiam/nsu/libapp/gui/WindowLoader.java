package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBCore;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static comiam.nsu.libapp.gui.Dialogs.showExceptionDialog;

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

    public static void loadAssistantFormWindow(String humanID, boolean updateUser)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("AssistantFormWindow.fxml"));
            Parent root = loader.load();
            AssistantFormWindow controller = loader.getController();

            controller.setHumanID(humanID);
            controller.setRoot(newWindow);

            newWindow.setTitle("Add new assistant");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 303, 87));
            newWindow.centerOnScreen();
            newWindow.show();

            controller.setIsEditingUser(updateUser);
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadSPOFormWindow(String humanID, boolean updateUser)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("SPOFormWindow.fxml"));
            Parent root = loader.load();
            SPOFormWindow controller = loader.getController();

            controller.setHumanID(humanID);
            controller.setRoot(newWindow);

            newWindow.setTitle("Add new spo");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 303, 87));
            newWindow.centerOnScreen();
            newWindow.show();

            controller.setIsEditingUser(updateUser);
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadStudentFormWindow(String humanID, boolean updateUser)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("StudentFormWindow.fxml"));
            Parent root = loader.load();
            StudentFormWindow controller = loader.getController();

            controller.setHumanID(humanID);
            controller.setRoot(newWindow);

            newWindow.setTitle("Add new student");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 303, 225));
            newWindow.centerOnScreen();
            newWindow.show();

            controller.setIsEditingUser(updateUser);
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadTeacherFormWindow(String humanID, boolean updateUser)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainController.class.getResource("TeacherFormWindow.fxml"));
            Parent root = loader.load();
            TeacherFormWindow controller = loader.getController();

            controller.setHumanID(humanID);
            controller.setRoot(newWindow);

            newWindow.setTitle("Add new teacher");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 303, 239));
            newWindow.centerOnScreen();
            newWindow.show();

            controller.setIsEditingUser(updateUser);
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadEnterReaderOneWindow(MainController parent, boolean toEdit, String... data)
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

            if(toEdit)
            {
                controller.setHumanID(data[0]);
                controller.setType(data[1]);
            }
            controller.setIsEditingUser(toEdit);
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
