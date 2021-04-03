package comiam.nsu.libapp.gui;

import comiam.nsu.libapp.db.core.DBActions;
import comiam.nsu.libapp.db.objects.PersonCard;
import comiam.nsu.libapp.util.GUIUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.val;

import static comiam.nsu.libapp.util.GUIUtils.*;

public class MainController
{
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
    private TableView<PersonCard> cardTable;

    @Setter
    private Stage root;

    private String selectedTableName;

    @FXML
    public void initialize()
    {
        refreshTableData.setOnAction(e -> {
            val names = getTableNames(root);

            if(names != null)
                tables.setItems(names);

            if(selectedTableName != null)
            {
                val table = getFreeSizeVariableTableFromRequest(root, selectedTableName);

                if(table != null)
                    tablePanel.setContent(table);
            }
        });

        tables.setOnAction(e -> {
            val tableName = tables.getValue();
            val table = getFreeSizeVariableTableFromRequest(root, tableName);

            if(table != null)
            {
                selectedTableName = tableName;
                tablePanel.setContent(table);
            }
        });

        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        patronymicColumn.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("regDate"));
        rewriteDateColumn.setCellValueFactory(new PropertyValueFactory<>("rewriteDate"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        canTakeAwayColumn.setCellValueFactory(new PropertyValueFactory<>("canTakeAwayBook"));

        refreshTableCards.setOnAction(e -> refresh());

        deleteSelectedUser.setOnAction(e -> {
            val user = cardTable.getSelectionModel().getSelectedItem();

            if(user == null)
            {
                Dialogs.showDefaultAlert(root, "Error", "There is nothing to delete!", Alert.AlertType.ERROR);
                return;
            }

            val res = DBActions.deleteUserCard(user);

            if(!res.isEmpty())
            {
                Dialogs.showDefaultAlert(root, "Error!", res, Alert.AlertType.ERROR);
                val data = GUIUtils.getCardRows(root);

                if(data == null)
                    return;

                cardTable.setItems(data);
            }
            refresh();
        });

        addNewUser.setOnAction(e -> {
            WindowLoader.loadEnterReaderOneWindow(this);
        });

        val names = getTableNames(root);

        if(names != null)
            tables.setItems(names);

        refresh();
    }

    public void refresh()
    {
        val data = GUIUtils.getCardRows(root);

        if(data == null)
            return;

        cardTable.setItems(data);
    }
}
