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
		stage.show(); // Actually only for the start scene
	}

	public static Controller switchSceneWithController(ActionEvent e, String fxmlFile) throws IOException {

		FXMLLoader loader = new FXMLLoader(sceneController.class.getResource(fxmlFile));

		sceneHistory.push(fxmlFile); // Pushes the scene into history

		Parent root = loader.load();
		Controller scene3Controller = loader.getController(); // Grab the NEW Scene 3 Controller

		// 2. Switch the physical screen
		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		stage.setScene(new Scene(root));

		return scene3Controller;
	}
}
