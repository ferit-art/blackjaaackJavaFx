package application;

import java.io.IOException;
import java.util.Stack;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class sceneController {

	public static final Stack<String> sceneHistory = new Stack<String>();
	
	public static void switchScene(ActionEvent event, String fxmlFile) throws IOException {
		
		sceneHistory.push(fxmlFile); // Pushes the scene into history
		
		Parent root = FXMLLoader.load(sceneController.class.getResource(fxmlFile));
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
}
