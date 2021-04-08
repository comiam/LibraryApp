package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.db.core.DBCore;
import comiam.nsu.libapp.db.objects.PersonCard;
import comiam.nsu.libapp.gui.custom.AutoCompleteTextField;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;
import lombok.var;

import java.util.Arrays;

import static comiam.nsu.libapp.util.GUIUtils.*;

public class MainController
{
    @FXML
    private VBox givingBookBox;
    @FXML
    private ChoiceBox<String> tables;
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
    private TableView<PersonCard> cardTable;
    @FXML
    private DatePicker dateMustReturningOnGiving;

    private AutoCompleteTextField userFIOOnGiving;
    private AutoCompleteTextField bookNameOnGiving;

    @Setter
    private Stage root;

    private String selectedTableName;

    //TODO add hall selector on starting program and write value there
    private int hallID = 1;

    @FXML
    public void initialize()
    {
        insertAutocompleteTextFiled();
        setupColumns();
        setupActions();
        fillDataTableList();
        refresh();
    }

    private void insertAutocompleteTextFiled()
    {
        val pane = givingBookBox.getChildren();
        pane.add(pane.indexOf(markTF1) + 1, (userFIOOnGiving = new AutoCompleteTextField()));
        pane.add(pane.indexOf(markTF2) + 1, (bookNameOnGiving = new AutoCompleteTextField()));

        var res = DBActions.getUsersListForBook();
        if(res.getFirst().isEmpty())
            userFIOOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));

        res = DBActions.getBooksList();
        if(res.getFirst().isEmpty())
            bookNameOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));
    }

    private void fillDataTableList()
    {
        val names = getTableNames(root);

        if (names != null)
            tables.setItems(names);
    }

    private void setupActions()
    {
        giveBook.setOnAction(e -> {
            val user = userFIOOnGiving.getText();
            val book = bookNameOnGiving.getText();
            val date = dateMustReturningOnGiving.getValue();

            if(user.trim().isEmpty() || user.trim().split(":").length < 2)
            {
                showError(root, "Invalid user field!");
                return;
            }

            if(book.trim().isEmpty() || book.trim().split(":").length < 3)
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
            int cardID = 0;
            int bookID = 0;
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

        userFIOOnGiving.setOnAction(e -> {
            if(userFIOOnGiving.getEntries().isEmpty())
            {
                val res = DBActions.getUsersListForBook();
                if(!res.getFirst().isEmpty())
                    return;

                userFIOOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));
            }
        });

        bookNameOnGiving.setOnAction(e -> {
            if(bookNameOnGiving.getEntries().isEmpty())
            {
                val res = DBActions.getBooksList();
                if(!res.getFirst().isEmpty())
                    return;

                bookNameOnGiving.getEntries().addAll(Arrays.asList(res.getSecond()));
            }
        });

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
