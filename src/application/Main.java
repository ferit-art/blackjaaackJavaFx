package application;

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("startScene.fxml"));
			Scene scene = new Scene(root, 1200, 1000, Color.BEIGE);
			String css = this.getClass().getResource("application.css").toExternalForm();
			scene.getStylesheets().add(css);
			Image icon = new Image("Logo.png");

			primaryStage.setFullScreenExitHint("");
			primaryStage.setResizable(false);
			primaryStage.getIcons().add(icon);
			primaryStage.setTitle("Blackjaaack");
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		
		try {
			File saveFile = new File("player_bets.txt");
			if (saveFile.exists()) {
				saveFile.delete();
			}
		} catch (Exception e) {
			System.out.println("Could not delete save file on exit.");
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}