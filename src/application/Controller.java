package application;

import java.io.FileWriter;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Controller {

	// UI subjects

	@FXML
	ScrollPane consoleScrollPane;
	@FXML
	Label consoleLabel;
	@FXML
	Label consoleLog;
	@FXML
	TextField betInput;
	@FXML
	Button backButton;
	@FXML
	Button backButton2;
	@FXML
	Button backButton3;
	@FXML
	Button nextButton;
	@FXML
	Button setButton;
	@FXML
	Button hitButton;
	@FXML
	Button standButton;
	@FXML
	Button doubleButton;
	@FXML
	Button splitButton;

	// Event handlers
	public void sliderNumPlayers(MouseEvent e) {
		Slider sliderNumPlayers = (Slider) e.getSource();
		players.numPlayers = (int) sliderNumPlayers.getValue();
	}

	public void startBtn(ActionEvent e) throws IOException {
		sceneController.switchScene(e, "scene1.fxml");
	}

	public void notUnderstand(ActionEvent e) throws IOException {
		resetGame();
		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
		players.allPlayers = new players[0];
	}

	private int currentPlayerIndex = 0;

	public void understand(ActionEvent e) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("scene2.fxml")); // Same thing as switchScene, but
																					// delivers the controller too
		sceneController.sceneHistory.push("scene2.fxml");

		Parent root = loader.load();
		Controller ctrl = loader.getController();

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();

		players.allPlayers = new players[players.numPlayers + 1]; // +1 for the dealer

		for (int i = 0; i < players.numPlayers; i++) {
			players.allPlayers[i] = new players();
			players.allPlayers[i].nick = "Player " + (i + 1);
		}

		players.allPlayers[players.numPlayers] = new players();
		players.allPlayers[players.numPlayers].nick = "Dealer";

		resources.setup();

		players.loadBets(players.allPlayers, ctrl);

		while (ctrl.currentPlayerIndex < players.numPlayers && players.allPlayers[ctrl.currentPlayerIndex].bet != 0) {

			ctrl.currentPlayerIndex++;
		}

		if (ctrl.currentPlayerIndex < players.numPlayers) {

			ctrl.consoleLabel.setText("How much is your bet " + players.allPlayers[ctrl.currentPlayerIndex].nick + "?");
			ctrl.betInput.setVisible(true);
		} else {

			ctrl.consoleLabel.setText("All bets loaded. Ready to play!");
			ctrl.betInput.setVisible(false);
		}
	}

	public void setBet(ActionEvent e) {
		if (currentPlayerIndex == -1 || currentPlayerIndex >= players.numPlayers) {
			nextButton.setVisible(true);
			setButton.setVisible(false);
			return; // No more players need bets
		}

		try {
			int betValue = Integer.parseInt(betInput.getText());

			if (betValue >= 1) {
				players p = players.allPlayers[currentPlayerIndex];
				p.originalBet = betValue;
				p.bet = p.originalBet;

				currentPlayerIndex++;

				while (currentPlayerIndex < players.numPlayers && players.allPlayers[currentPlayerIndex].bet != 0) {
					currentPlayerIndex++;
				}

				if (currentPlayerIndex < players.numPlayers) {

					consoleLabel.setText("How much is your bet " + players.allPlayers[currentPlayerIndex].nick + "?");
					betInput.clear();
				} else {

					nextButton.setVisible(true);
					setButton.setVisible(false);
					betInput.setVisible(false);
					consoleLabel.setText("All bets set. Ready to play!");
					currentPlayerIndex = 0;
				}
			} else {
				consoleLabel.setText("The bet must be at least 1.");
			}
		} catch (NumberFormatException ex) {

			consoleLabel.setText("Invalid bet for " + players.allPlayers[currentPlayerIndex].nick
					+ ": Enter a valid positive number.");
		}
	}

	public void toScene3(ActionEvent e) throws IOException {
		sceneController.switchScene(e, "scene3.fxml");
	}

	// Player actions (In-game)

	public void hitAction(ActionEvent e) {
		players player = players.allPlayers[players.currentPlayer];

		resources.hit(player, player.deck);

		String output = player.nick + " hit!\n";

		output += player.nick + "'s card this round: " + player.card + "\n";

		output += "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

		output += "The total value of " + player.nick + "'s hand: " + player.score + "\n";

		if (player.hasSplit) {
			output += player.nick + "'s split-card this round: " + player.splitCard + "\n";

			output += "The amount of " + player.nick + "'s cards in the splitted deck is " + player.splitDeck.size()
					+ "\n";

			output += "The total value of " + player.nick + "'s split hand: " + player.splitScore + "\n";

		}

		if (player.nick != "Dealer") {

		}

		appendToConsole(output);

		if (player.nick == "Dealer") {
			players.currentPlayer = 0;
		} else {
			players.currentPlayer++;
		}
	}

	public void splitAction(ActionEvent e) {
		players player = players.allPlayers[players.currentPlayer];

		resources.split(player);

		String output = player.nick + " split!";

		output += "\nThe amount of " + player.nick + "'s cards in the splitted deck is " + player.splitDeck.size();

		output += "\nThe total value of " + player.nick + "'s split hand: " + player.splitScore + "\n";

		appendToConsole(output);

		if (player.nick == "Dealer") {
			players.currentPlayer = 0;
		} else {
			players.currentPlayer++;
		}
	}

	public void goBack(ActionEvent e) throws IOException {
		resetGame();
		sceneController.sceneHistory.pop(); // Erase the current scene

		String fxmlFile = sceneController.sceneHistory.peek();
		sceneController.switchScene(e, fxmlFile);
	}

	public void goHome(ActionEvent e) throws IOException {
		resetGame();
		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
	}

	// Helper methods

	private void appendToConsole(String message) {
		if (consoleLog.getText().isEmpty()) {
			consoleLog.setText(message);
		} else {
			consoleLog.setText(consoleLog.getText() + "\n" + message);
		}

		consoleScrollPane.layout();
		consoleScrollPane.setVvalue(1.0); // 1.0 = bottom
	}

	private void resetGame() {
		for (int i = 0; i < players.numPlayers; i++) {
			players p = players.allPlayers[i];

			if (players.allPlayers[i] != null) {
				p.deck.clear();
				p.splitDeck.clear();

				p.score = 0;
				p.splitScore = 0;
				p.bet = 0;
				p.originalBet = 0;
				p.card = null;
				p.splitCard = null;

				p.hasStood = false;
				p.splitHasStood = false;
				p.hasSplit = false;
				p.hasDoubledDown = false;
				p.hasWon = false;
				p.splitChoice = false;
				p.choice = false;
			}
		}

		players.allPlayers = new players[0];

		players.currentPlayer = 0;
		players.numPlayers = 0;
		currentPlayerIndex = -1;

		try (FileWriter writer = new FileWriter("player_bets.txt", false)) {
	        writer.write("");           // ← empty file
	        writer.flush();
	    } catch (Exception ignored) {
	    }
		
		try {
			new java.io.File("player_bets.txt").delete(); 
		} catch (Exception ignored) {
			System.out.println("crash");
		}
	}
}
