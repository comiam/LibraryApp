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
            loader.setLocation(LibraryWindowController.class.getResource("AssistantFormWindow.fxml"));
            Parent root = loader.load();
            AssistantFormController controller = loader.getController();

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
            loader.setLocation(LibraryWindowController.class.getResource("SPOFormWindow.fxml"));
            Parent root = loader.load();
            SPOFormController controller = loader.getController();

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
            loader.setLocation(LibraryWindowController.class.getResource("StudentFormWindow.fxml"));
            Parent root = loader.load();
            StudentFormController controller = loader.getController();

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
            loader.setLocation(LibraryWindowController.class.getResource("TeacherFormWindow.fxml"));
            Parent root = loader.load();
            TeacherFormController controller = loader.getController();

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

    public static void loadEnterReaderOneWindow(LibraryWindowController parent, boolean toEdit, String... data)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LibraryWindowController.class.getResource("EnterReaderStepOne.fxml"));
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

    public static void loadHallSelectorWindow(String name, int userID, boolean isReaderUser)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LibraryWindowController.class.getResource("HallSelectorWindow.fxml"));
            Parent root = loader.load();
            HallSelectorController controller = loader.getController();
            controller.setRoot(newWindow);
            controller.setUserName(name);
            controller.setReaderUser(isReaderUser);
            controller.setUserID(userID);

            newWindow.setTitle(name);
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 305, 86));
            newWindow.centerOnScreen();

            newWindow.setOnCloseRequest(e -> {
                DBCore.closeCurrentSession();
                Platform.exit();
            });

            newWindow.show();
            controller.postInit();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadUserWindow(String name, int hallID, int userID)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LibraryWindowController.class.getResource("UserWindow.fxml"));
            Parent root = loader.load();
            UserWindowController controller = loader.getController();
            controller.setRoot(newWindow);
            controller.setHallID(hallID);
            controller.setUserID(userID);

            newWindow.setTitle(name);
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 640, 501));
            newWindow.centerOnScreen();

            newWindow.setOnCloseRequest(e -> {
                DBCore.closeCurrentSession();
                Platform.exit();
            });

            newWindow.show();
            controller.postInit();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void loadLibraryWindow(String name, int hallID, boolean isMAHall)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LibraryWindowController.class.getResource("LibraryWindow.fxml"));
            Parent root = loader.load();
            LibraryWindowController controller = loader.getController();
            controller.setRoot(newWindow);
            controller.setHallID(hallID);
            controller.setMA(isMAHall);

            newWindow.setTitle(name);
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 640, 501));
            newWindow.centerOnScreen();

            newWindow.setOnCloseRequest(e -> {
                DBCore.closeCurrentSession();
                Platform.exit();
            });

            newWindow.show();
            controller.postInit();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }
    }
}
