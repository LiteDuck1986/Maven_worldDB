package main;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import db.DatabaseConnection;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import operations.SelectOperation;
import operations.ViewManager;

public class WorldDbApp extends Application{
	private Connection con;
	private TableView<ObservableList<String>> table =
			new TableView<>();
	
	private ComboBox<String> tableSelector = new ComboBox<>();
	
	private List<String> currentColumns = new ArrayList<>();
	private List<String> databaseView = new ArrayList<>();
	
	private Button btnSelect = new Button("Atlasīt (SELECT)");
	private Button btnInsert = new Button("Pievienot (INSERT)");
	private Button btnUpdate = new Button("Atjaunināt (UPDATE)");
	private Button btnDelete = new Button("Dzēst (DELETE)");
	
	@Override
	public void start(Stage stage) {
		Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet()); // Var arī izmantot PrimerDark(), NordDark()
		
		try {
			con = DatabaseConnection.getConnection();
			databaseView = ViewManager.getDatabaseViews(con);
			
		} catch (Exception e) {
			showAlert("Savienojuma kļūda: ", e.getMessage());
			return;
		}
		
		tableSelector.getItems().add("-- Tabulas ---");
		tableSelector.getItems().addAll("city"); // paši pieliksiet pārējās divas tabulas
		
		if(databaseView.isEmpty()) {
			tableSelector.getItems().add("--- NAV ATRASTS NEVIENS SKATS ---");
			showAlert("Informācija", "Datu bāzē pagaidām nav neviens skats");
		} else {
			tableSelector.getItems().add("--- SKATI ---");
			tableSelector.getItems().addAll(databaseView);
		}
		
		btnSelect.setOnAction(e -> loadData());
		btnInsert.setOnAction(e -> showInsertUpdateDialog(false));
		btnUpdate.setOnAction(e -> showInsertUpdateDialog(true));
//		btnDelete.setOnAction(e -> performDelete());
		
		tableSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			boolean isSeperator = (newVal == null || newVal.startsWith("---"));
			btnSelect.setDisable(isSeperator);
			btnInsert.setDisable(isSeperator);
			btnUpdate.setDisable(isSeperator);
			btnDelete.setDisable(isSeperator);
		});
		
		tableSelector.getSelectionModel().select(1);
		HBox topBox = new HBox(10, new Label("Tabula/Skats:"),
				tableSelector, btnSelect);
		
		topBox.setPadding(new Insets(10));
		
		HBox bottomBox = new HBox(10, btnInsert, btnUpdate, btnDelete);
		
		VBox root = new VBox(10, topBox, table, bottomBox); // Jāpapildina ar 
		stage.setScene(new Scene(root, 900, 600));
		stage.setTitle("World DB APP");
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		stage.show();
		loadData();
	}
	
	private void loadData() {
		String selectedTable = tableSelector.getValue();
		if(selectedTable == null || selectedTable.startsWith("---"))
			return;
		
		try {
			currentColumns = SelectOperation.populateTable(con, selectedTable, table);
		}catch(Exception e) {
			showAlert("Kļūda atlasot datus", e.getMessage());
		}
	}
	
	private void showAlert(String title, String msg) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}
	
	private void showInsertUpdateDialog(boolean isUpdate) {
		String tableName = tableSelector.getValue();
		if(tableName == null || tableName.startsWith("---"))
			return;
		
		if(databaseView.contains(tableName)) {
			showAlert("Uzmanību!", "Izvēlētais objekts ir skats! Datus tajā mainīt un ievietot nevar!");
			return;
		}
		
		loadData();
		Alert dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setTitle(isUpdate ? "UPDATE " + tableName : "INSERT " +tableName);
		
		dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		
		if(isUpdate) {
			dialog.setHeaderText("Aizpildi laukus, lai atjauninātu:");
			
		} else {
			dialog.setHeaderText("Aizpildi laukus, lai pievienotu:");
		}
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		
		//.........
	}

	public static void main(String[] args) {
		launch(args);

	}

}
