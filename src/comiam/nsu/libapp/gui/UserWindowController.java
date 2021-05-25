package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.db.core.DBCore;
import comiam.nsu.libapp.db.objects.OrderRow;
import comiam.nsu.libapp.db.objects.ViolationRow;
import comiam.nsu.libapp.gui.custom.AutoCompleteTextField;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;
import lombok.var;

import java.time.LocalDate;
import java.util.Arrays;

import static comiam.nsu.libapp.util.GUIUtils.showError;
import static comiam.nsu.libapp.util.StringChecker.checkStrArgs;

public class UserWindowController
{
    public static final int HOME_HALL_ID = -1;

    @FXML
    private Button giveBook;
    @FXML
    private Button returnBook;
    @FXML
    private Button orderBook;
    @FXML
    private Button refreshOrder;
    @FXML
    private Button refreshViolation;
    @FXML
    private Button saveUserData;

    @FXML
    private TextField cardIDF;
    @FXML
    private TextField username;
    @FXML
    private TextField surname;
    @FXML
    private TextField patronymic;
    @FXML
    private TextField password;

    @FXML
    private DatePicker orderReturnDate;

    @FXML
    private TableView<OrderRow> orderTable;
    @FXML
    private TableColumn<OrderRow, String> IDOrderColumn;
    @FXML
    private TableColumn<OrderRow, String> bookNameOrderColumn;
    @FXML
    private TableColumn<OrderRow, String> orderDateColumn;
    @FXML
    private TableColumn<OrderRow, String> latePassDateColumn;
    @FXML
    private TableColumn<OrderRow, String> takenDateColumn;
    @FXML
    private TableColumn<OrderRow, String> returnedOrderColumn;
    @FXML
    private TableColumn<OrderRow, String> acceptedOrderColumn;

    @FXML
    private TableView<ViolationRow> violationsTable;
    @FXML
    private TableColumn<ViolationRow, String> IDViolationColumn;
    @FXML
    private TableColumn<ViolationRow, String> bookNameViolationColumn;
    @FXML
    private TableColumn<ViolationRow, String> hallIDViolationColumn;
    @FXML
    private TableColumn<ViolationRow, String> violationDateColumn;
    @FXML
    private TableColumn<ViolationRow, String> violationTypeColumn;
    @FXML
    private TableColumn<ViolationRow, String> fineColumn;

    @FXML
    private Label markTF1;
    @FXML
    private Label markTF2;
    @FXML
    private Label markTF3;

    @FXML
    private VBox givingBookBox;
    @FXML
    private VBox returningBookBox;
    @FXML
    private VBox orderBookBox;

    @FXML
    private TabPane mainPane;
    @FXML
    private Tab passTakeBookTab;

    private AutoCompleteTextField bookNameOnGiving;
    private AutoCompleteTextField bookNameOnReturning;
    private AutoCompleteTextField bookNameOnOrder;

    @Setter
    private Stage root;
    @Setter
    private int userID;
    @Setter
    private int hallID;

    @FXML
    private void initialize()
    {
        insertAutocompleteTextFiled();
        setupColumns();
        setupActions();
        editDatePickers();
    }

    public void postInit()
    {
        if(hallID == HOME_HALL_ID)
            hideHallTabs();
        fillUserData();
        refreshOrderTable();
        refreshViolationTable();
    }

    private void fillUserData()
    {
        val res = DBActions.getUserData(userID);
        if(showError(root, res.getFirst()))
        {
            root.close();
            DBCore.closeCurrentSession();
            Platform.exit();
        }else
        {
            cardIDF.setEditable(false);
            cardIDF.setText(userID + "");

            username.setText(res.getSecond()[0]);
            surname.setText(res.getSecond()[1]);
            patronymic.setText(res.getSecond()[2]);
        }
    }

    private void hideHallTabs()
    {
        mainPane.getTabs().remove(passTakeBookTab);
    }

    private void refreshOrderTable()
    {
        val data = GUIUtils.getUserOrderRows(root, userID);

        if(data == null)
            return;

        orderTable.setItems(data);
    }

    private void refreshViolationTable()
    {
        val data = GUIUtils.getUserViolations(root, userID);

        if(data == null)
            return;

        violationsTable.setItems(data);
    }

    private void setupActions()
    {
        refreshOrder.setOnAction(e -> refreshOrderTable());
        refreshViolation.setOnAction(e -> refreshViolationTable());

        orderBook.setOnAction(e -> {
            val book = bookNameOnOrder.getText().trim();
            val date = orderReturnDate.getValue();

            if(!checkStrArgs(book) || date == null)
            {
                showError(root, "Одно или несколько полей пустые!");
                return;
            }

            val dateStr = date.toString();

            int bookID;
            try
            {
                bookID = Integer.parseInt(book.trim().split(":")[0].trim());
            }catch (Throwable ex) {
                showError(root, "Некорректные значения в полях!");
                return;
            }

            val res = DBCore.callProcedure("\"18209_BOLSHIM\".ORDER_BOOK(" + bookID + ", " + userID + ", TO_DATE('" + dateStr + "', 'yyyy-mm-dd'), 0)", true);
            if(res.isEmpty())
                Dialogs.showDefaultAlert(root, "Успех!", "Книга заказана!", Alert.AlertType.INFORMATION);
            else
                showError(root, res);
        });

        giveBook.setOnAction(e -> {
            val book = bookNameOnGiving.getText();

            if(book.trim().isEmpty() || book.trim().split(":").length != 3)
            {
                showError(root, "Некорректное значение поля книги!");
                return;
            }

            int bookID;
            try
            {
                bookID = Integer.parseInt(book.trim().split(":")[0].trim());
            }catch (Throwable ex) {
                showError(root, "Некорретные значения в полях!");
                return;
            }
            val res = DBCore.callProcedure("\"18209_BOLSHIM\".GET_BOOK(" + bookID + ", " + hallID + ", " + userID + ", CURRENT_DATE+1)", true);
            if(res.isEmpty())
                Dialogs.showDefaultAlert(root, "Успех!", "Книга сдана!", Alert.AlertType.INFORMATION);
            else
                showError(root, res);
        });

        returnBook.setOnAction(e -> {
            val book = bookNameOnReturning.getText();

            if(book.trim().isEmpty() || book.trim().split(":").length != 3)
            {
                showError(root, "Некорректное значение поля книги!");
                return;
            }

            int bookID;
            try
            {
                bookID = Integer.parseInt(book.trim().split(":")[0].trim());
            }catch (Throwable ex) {
                showError(root, "Некорретные значения в полях!");
                return;
            }
            val res = DBCore.callProcedure("\"18209_BOLSHIM\".RETURN_BOOK(" + bookID + ", " + hallID + ", " + userID + ")", true);
            if(res.isEmpty())
                Dialogs.showDefaultAlert(root, "Успех!", "Книга сдана!", Alert.AlertType.INFORMATION);
            else
                showError(root, res);
        });

        bookNameOnGiving.setOnKeyReleased(e -> {
            if(bookNameOnGiving.getEntries().isEmpty())
            {
                val res = DBActions.getBooksList();
                if(!res.getFirst().isEmpty())
                    return;

                bookNameOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));
            }
        });

        bookNameOnReturning.setOnKeyReleased(bookNameOnGiving.getOnKeyReleased());
        bookNameOnOrder.setOnKeyReleased(bookNameOnGiving.getOnKeyReleased());

        saveUserData.setOnAction(e -> {
            val name = username.getText().trim();
            val surn = surname.getText().trim();
            val patr = patronymic.getText().trim();
            val passw = password.getText().trim();

            if(!checkStrArgs(name, surn, patr, passw))
            {
                showError(root, "Одно или несколько полей пустые!");
                return;
            }

            if(name.length() > 20 || surn.length() > 20 || patr.length() > 20 || passw.length() > 32)
            {
                showError(root, "Некорректные длины полей!");
                return;
            }

            val res = DBActions.saveUserData(userID, name, surn, patr, passw);
            if(!res.isEmpty())
                showError(root, res);
            else
                Dialogs.showDefaultAlert(root, "Успех!", "Данные пользователя сохранены!", Alert.AlertType.INFORMATION);
        });
    }

    private void editDatePickers()
    {
        orderReturnDate.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });
    }

    private void setupColumns()
    {
        IDOrderColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        bookNameOrderColumn.setCellValueFactory(new PropertyValueFactory<>("bookName"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        latePassDateColumn.setCellValueFactory(new PropertyValueFactory<>("latePassDate"));
        takenDateColumn.setCellValueFactory(new PropertyValueFactory<>("takenDate"));
        returnedOrderColumn.setCellValueFactory(new PropertyValueFactory<>("returnedOrder"));
        acceptedOrderColumn.setCellValueFactory(new PropertyValueFactory<>("accepted"));

        IDViolationColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        bookNameViolationColumn.setCellValueFactory(new PropertyValueFactory<>("bookName"));
        hallIDViolationColumn.setCellValueFactory(new PropertyValueFactory<>("hallID"));
        violationDateColumn.setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        violationTypeColumn.setCellValueFactory(new PropertyValueFactory<>("violationType"));
        fineColumn.setCellValueFactory(new PropertyValueFactory<>("fine"));
    }

    private void insertAutocompleteTextFiled()
    {
        val pane = givingBookBox.getChildren();
        pane.add(pane.indexOf(markTF1) + 1, (bookNameOnGiving = new AutoCompleteTextField()));

        val pane2 = returningBookBox.getChildren();
        pane2.add(pane2.indexOf(markTF2) + 1, (bookNameOnReturning = new AutoCompleteTextField()));

        val pane7 = orderBookBox.getChildren();
        pane7.add(pane7.indexOf(markTF3) + 1, (bookNameOnOrder = new AutoCompleteTextField()));

        var res = DBActions.getBooksList();
        if(res.getFirst().isEmpty())
        {
            val arr = Arrays.asList(res.getSecond());
            bookNameOnGiving.getEntries().addAll(arr);
            bookNameOnReturning.getEntries().addAll(arr);
            bookNameOnOrder.getEntries().addAll(arr);
        }
    }
}
