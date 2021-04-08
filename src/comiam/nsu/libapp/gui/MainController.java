package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.db.core.DBCore;
import comiam.nsu.libapp.db.objects.PersonCard;
import comiam.nsu.libapp.gui.custom.AutoCompleteTextField;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.collections.FXCollections;
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

import static comiam.nsu.libapp.util.GUIUtils.*;
import static comiam.nsu.libapp.util.StringChecker.checkStrArgs;

public class MainController
{
    @FXML
    private VBox givingBookBox;
    @FXML
    private VBox violationOpenBox;
    @FXML
    private VBox returningBookBox;
    @FXML
    private VBox violationClosingBox;
    @FXML
    private ChoiceBox<String> tables;
    @FXML
    private ChoiceBox<String> violationType;
    @FXML
    private ScrollPane tablePanel;
    @FXML
    private Button refreshTableData;
    @FXML
    private Button refreshTableCards;
    @FXML
    private Button editSelectedUser;
    @FXML
    private Button deleteSelectedUser;
    @FXML
    private Button addNewUser;
    @FXML
    private Button giveBook;
    @FXML
    private Button returnBook;
    @FXML
    private Button addViolation;
    @FXML
    private Button closeViolation;
    @FXML
    private TableColumn<PersonCard, String> IDColumn;
    @FXML
    private TableColumn<PersonCard, String> firstNameColumn;
    @FXML
    private TableColumn<PersonCard, String> lastNameColumn;
    @FXML
    private TableColumn<PersonCard, String> patronymicColumn;
    @FXML
    private TableColumn<PersonCard, String> regDateColumn;
    @FXML
    private TableColumn<PersonCard, String> rewriteDateColumn;
    @FXML
    private TableColumn<PersonCard, String> typeColumn;
    @FXML
    private TableColumn<PersonCard, String> canTakeAwayColumn;
    @FXML
    private Label markTF1;
    @FXML
    private Label markTF2;
    @FXML
    private Label markTF3;
    @FXML
    private Label markTF4;
    @FXML
    private Label markTF5;
    @FXML
    private Label markTF6;
    @FXML
    private Label markTF7;
    @FXML
    private TableView<PersonCard> cardTable;
    @FXML
    private DatePicker dateMustReturningOnGiving;
    @FXML
    private DatePicker violationDate;
    @FXML
    private DatePicker blockDate;
    @FXML
    private TextField monFine;
    @FXML
    private CheckBox bookReturned;

    private AutoCompleteTextField userFIOOnGiving;
    private AutoCompleteTextField bookNameOnGiving;
    private AutoCompleteTextField userFIOOnReturning;
    private AutoCompleteTextField bookNameOnReturning;
    private AutoCompleteTextField userFIOOnViolation;
    private AutoCompleteTextField bookNameOnViolation;
    private AutoCompleteTextField violationData;

    @Setter
    private Stage root;
    @Setter
    private int hallID;

    private String selectedTableName;


    @FXML
    private void initialize()
    {
        insertAutocompleteTextFiled();
        setupColumns();
        setupActions();
        fillChoiceList();
        editDatePickers();
        refresh();
    }

    private void editDatePickers()
    {
        dateMustReturningOnGiving.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });
        violationDate.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) > 0);
            }
        });
        blockDate.setDayCellFactory(dateMustReturningOnGiving.getDayCellFactory());
    }

    private void insertAutocompleteTextFiled()
    {
        val pane = givingBookBox.getChildren();
        pane.add(pane.indexOf(markTF1) + 1, (userFIOOnGiving = new AutoCompleteTextField()));
        pane.add(pane.indexOf(markTF2) + 1, (bookNameOnGiving = new AutoCompleteTextField()));

        val pane2 = returningBookBox.getChildren();
        pane2.add(pane2.indexOf(markTF3) + 1, (userFIOOnReturning = new AutoCompleteTextField()));
        pane2.add(pane2.indexOf(markTF4) + 1, (bookNameOnReturning = new AutoCompleteTextField()));

        val pane3 = violationOpenBox.getChildren();
        pane3.add(pane3.indexOf(markTF5) + 1, (userFIOOnViolation = new AutoCompleteTextField()));
        pane3.add(pane3.indexOf(markTF6) + 1, (bookNameOnViolation = new AutoCompleteTextField()));

        val pane4 = violationClosingBox.getChildren();
        pane4.add(pane4.indexOf(markTF7) + 1, (violationData = new AutoCompleteTextField()));

        var res = DBActions.getUsersListForBook();
        if(res.getFirst().isEmpty())
        {
            userFIOOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));
            userFIOOnReturning.getEntries().addAll(Arrays.asList(res.getSecond()));
            userFIOOnViolation.getEntries().addAll(Arrays.asList(res.getSecond()));
        }

        res = DBActions.getBooksList();
        if(res.getFirst().isEmpty())
        {
            bookNameOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));
            bookNameOnReturning.getEntries().addAll(Arrays.asList(res.getSecond()));
            bookNameOnViolation.getEntries().addAll(Arrays.asList(res.getSecond()));
        }
        res = DBActions.getViolationData();
        if(res.getFirst().isEmpty())
            violationData.getEntries().addAll(Arrays.asList(res.getSecond()));
        else
            violationData.getEntries().add("error");
    }

    private void fillChoiceList()
    {
        val names = getTableNames(root);

        if (names != null)
            tables.setItems(names);

        violationType.setItems(FXCollections.observableArrayList("lost", "corrupted"));
    }

    private void setupActions()
    {
        closeViolation.setOnAction(e -> {
            val violation = violationData.getText();

            if(violation.split(";").length != 7)
            {
                showError(root, "Invalid violation data!");
                return;
            }

            try
            {
                val arr = violation.split(";");
                val bookID = Integer.parseInt(arr[0].split(" ")[1]);
                val hallID = Integer.parseInt(arr[2].split(" ")[2]);
                val cardID = Integer.parseInt(arr[1].split(" ")[2]);
                var date = "TO_DATE('" + arr[3].split(" ")[2] + "', 'yyyy-mm-dd')";
                val bookRet = bookReturned.isSelected() ? 1 : 0;

                val res = DBCore.callProcedure("CLOSE_VIOLATION(" + bookID + ", " + hallID + ", " + cardID + ", " + date + ", " + bookRet + ")");
                if(res.isEmpty())
                    Dialogs.showDefaultAlert(root, "Success!", "Violation closed!", Alert.AlertType.INFORMATION);
                else
                    showError(root, res);
            }catch (Throwable ex) {
                ex.printStackTrace();
                showError(root, "Invalid violation data!");
            }
        });

        bookNameOnViolation.textProperty().addListener((observable, oldValue, newValue) -> {
            val book = bookNameOnViolation.getText();
            val cont = bookNameOnViolation.getEntries().stream().anyMatch(v -> v.equals(book));

            if(cont)
                try
                {
                    //Need for check validation of book ID
                    val res = DBActions.getBookCost(Integer.parseInt(book.split(":")[0].trim()) + "");

                    if(res.getFirst().isEmpty())
                    {
                        monFine.setText(Integer.parseInt(res.getSecond()) * 10 + " ");
                        monFine.setEditable(false);
                    }else
                        monFine.setEditable(true);

                }catch (Throwable ignored) {}
        });

        addViolation.setOnAction(e -> {
            val user = userFIOOnViolation.getText().trim();
            val book = bookNameOnViolation.getText().trim();
            val date = violationDate.getValue();
            val type = violationType.getValue().trim();
            val block = blockDate.getValue();
            val fine = monFine.getText().trim();

            if(!checkStrArgs(user, book, type, fine) || date == null)
            {
                showError(root, "Empty fields!");
                return;
            }

            val dateStr = date.toString();

            if(user.trim().split(":").length != 2)
            {
                showError(root, "Invalid user field!");
                return;
            }

            if(book.trim().split(":").length != 3)
            {
                showError(root, "Invalid book field!");
                return;
            }

            int cardID;
            int bookID;
            int fineInt;
            try
            {
                cardID = Integer.parseInt(user.trim().split(":")[1].trim());
                bookID = Integer.parseInt(book.trim().split(":")[0].trim());
                fineInt = Integer.parseInt(fine);
            }catch (Throwable ex) {
                showError(root, "Invalid values in user or book or fine fields!");
                return;
            }

            val res = DBCore.callProcedure("OPEN_VIOLATION(" + bookID + ", " + hallID + ", " + cardID + ", '" +
                    type + "', TO_DATE('" + dateStr + "', 'yyyy-mm-dd'), " + fineInt + ", " + (block == null ? "NULL" : "TO_DATE('" + block.toString() + "', 'yyyy-mm-dd')") + ")");
            if(res.isEmpty())
                Dialogs.showDefaultAlert(root, "Success!", "Violation added!", Alert.AlertType.INFORMATION);
            else
                showError(root, res);
        });

        giveBook.setOnAction(e -> {
            val user = userFIOOnGiving.getText();
            val book = bookNameOnGiving.getText();
            val date = dateMustReturningOnGiving.getValue();

            if(user.trim().isEmpty() || user.trim().split(":").length != 2)
            {
                showError(root, "Invalid user field!");
                return;
            }

            if(book.trim().isEmpty() || book.trim().split(":").length != 3)
            {
                showError(root, "Invalid book field!");
                return;
            }

            if(date == null)
            {
                showError(root, "Select date to return book!");
                return;
            }

            val dateStr = date.toString();
            int cardID;
            int bookID;
            try
            {
                cardID = Integer.parseInt(user.trim().split(":")[1].trim());
                bookID = Integer.parseInt(book.trim().split(":")[0].trim());
            }catch (Throwable ex) {
                showError(root, "Invalid values in user or book fields!");
                return;
            }
            val res = DBCore.callProcedure("GET_BOOK(" + bookID + ", " + hallID + ", " + cardID + ", TO_DATE('" + dateStr + "', 'yyyy-mm-dd'))");
            if(res.isEmpty())
                Dialogs.showDefaultAlert(root, "Success!", "The book can be handed in!", Alert.AlertType.INFORMATION);
            else
                showError(root, res);
        });

        returnBook.setOnAction(e -> {
            val user = userFIOOnReturning.getText();
            val book = bookNameOnReturning.getText();

            if(user.trim().isEmpty() || user.trim().split(":").length != 2)
            {
                showError(root, "Invalid user field!");
                return;
            }

            if(book.trim().isEmpty() || book.trim().split(":").length != 3)
            {
                showError(root, "Invalid book field!");
                return;
            }

            int cardID;
            int bookID;
            try
            {
                cardID = Integer.parseInt(user.trim().split(":")[1].trim());
                bookID = Integer.parseInt(book.trim().split(":")[0].trim());
            }catch (Throwable ex) {
                showError(root, "Invalid values in user or book fields!");
                return;
            }
            val res = DBCore.callProcedure("RETURN_BOOK(" + bookID + ", " + hallID + ", " + cardID + ")");
            if(res.isEmpty())
                Dialogs.showDefaultAlert(root, "Success!", "The book can be returned!", Alert.AlertType.INFORMATION);
            else
                showError(root, res);
        });

        userFIOOnGiving.setOnKeyReleased(e -> {
            if(userFIOOnGiving.getEntries().isEmpty())
            {
                val res = DBActions.getUsersListForBook();
                if(!res.getFirst().isEmpty())
                    return;

                userFIOOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));
            }
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

        violationData.setOnKeyReleased(e -> {
            if(violationData.getEntries().isEmpty())
            {
                val res = DBActions.getViolationData();
                if(!res.getFirst().isEmpty())
                    return;

                violationData.getEntries().addAll(Arrays.asList(res.getSecond()));
            }
        });

        userFIOOnReturning.setOnKeyReleased(userFIOOnGiving.getOnKeyReleased());
        bookNameOnReturning.setOnKeyReleased(bookNameOnGiving.getOnKeyReleased());
        userFIOOnViolation.setOnKeyReleased(userFIOOnGiving.getOnKeyReleased());
        bookNameOnViolation.setOnKeyReleased(bookNameOnGiving.getOnKeyReleased());

        refreshTableData.setOnAction(e -> {
            val names = getTableNames(root);

            if (names != null)
            {
                tables.setItems(names);

                if (selectedTableName != null)
                    tables.setValue(selectedTableName);
            }

            if (selectedTableName != null)
            {
                val table = getFreeSizeVariableTableFromRequest(root, selectedTableName);

                if (table != null)
                    tablePanel.setContent(table);
            }
        });

        tables.setOnAction(e -> {
            val tableName = tables.getValue();

            if (tableName == null)
                return;
            val table = getFreeSizeVariableTableFromRequest(root, tableName);

            if (table != null)
            {
                selectedTableName = tableName;
                tablePanel.setContent(table);
            }
        });

        refreshTableCards.setOnAction(e -> refresh());
        deleteSelectedUser.setOnAction(e -> {
            val user = cardTable.getSelectionModel().getSelectedItem();

            if (user == null)
            {
                showError(root, "There is nothing to delete!");
                return;
            }

            val result = DBActions.deleteSelectedUser(user);

            if (!showError(root, result))
                refresh();
        });

        addNewUser.setOnAction(e -> WindowLoader.loadEnterReaderOneWindow(this, false));

        editSelectedUser.setOnAction(e -> {
            val user = cardTable.getSelectionModel().getSelectedItem();

            if (user == null)
            {
                showError(root, "There is nothing to delete!");
                return;
            }
            WindowLoader.loadEnterReaderOneWindow(this, true, user.getHumanID(), user.getType());
        });
    }

    private void setupColumns()
    {
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        patronymicColumn.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("regDate"));
        rewriteDateColumn.setCellValueFactory(new PropertyValueFactory<>("rewriteDate"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        canTakeAwayColumn.setCellValueFactory(new PropertyValueFactory<>("canTakeAwayBook"));
    }

    protected void refresh()
    {
        val data = GUIUtils.getCardRows(root);

        if(data == null)
            return;

        cardTable.setItems(data);
    }
}
