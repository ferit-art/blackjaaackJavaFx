package application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

// Current notes:

//	Seperate the section between the empty comment rows in Players.java as a new class with the name of 
//	Player.java and make the necessary adjustments in the rest of the code. => Do this in the java version
//	of the game too.

// 	EventListeners to the double down and stand buttons,
// 	including the implamentation of the code from the previous version.

// 	The implementation of bust and blackjack (21), as well as other outcomes like 

public class Controller {

	// UI objects

	@FXML
	ScrollPane consoleScrollPane;
	@FXML
	Label betLabel;
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
		Players.numPlayers = (int) sliderNumPlayers.getValue();
	}

	public void startBtn(ActionEvent e) throws IOException {
		sceneController.switchScene(e, "scene1.fxml");
	}

	public void notUnderstand(ActionEvent e) throws IOException {
		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
		Players.allPlayers = new Players[0];
	}

	private int currentPlayerIndex = -1;

	public void understand(ActionEvent e) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("scene2.fxml")); // Same thing as switchScene, but
																					// delivers the controller too
		sceneController.sceneHistory.push("scene2.fxml");

		Parent root = loader.load();
		Controller ctrl = loader.getController();

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		ctrl.currentPlayerIndex = 0;
		stage.show();

		Players.allPlayers = new Players[Players.numPlayers + 1]; // +1 for the dealer

		for (int i = 0; i <= Players.numPlayers; i++) {

			if (i == Players.numPlayers) {
				Players.allPlayers[i] = new Players();
				Players.allPlayers[i].nick = "Dealer";

			} else {
				Players.allPlayers[i] = new Players();
				Players.allPlayers[i].nick = "Player " + (i + 1);
			}
		}

		resources.setup();

		Players.loadBets(Players.allPlayers, ctrl);

		ctrl.currentPlayerIndex = 0;

		while (ctrl.currentPlayerIndex < Players.numPlayers && Players.allPlayers[ctrl.currentPlayerIndex].bet != 0) {
			ctrl.currentPlayerIndex++;
		}

		if (ctrl.currentPlayerIndex < Players.numPlayers) {

			ctrl.betLabel.setText("How much is your bet " + Players.allPlayers[ctrl.currentPlayerIndex].nick + "?");
			ctrl.betInput.setVisible(true);
		} else {

			ctrl.betLabel.setText("All bets loaded. Ready to play!");
			ctrl.betInput.setVisible(false);
		}
	}

	public void setBet(ActionEvent e) {
		if (currentPlayerIndex == -1 || currentPlayerIndex >= Players.numPlayers) {
			nextButton.setVisible(true);
			setButton.setVisible(false);
			return; // No more players need to set bets
		}

		try {
			int betValue = Integer.parseInt(betInput.getText());

			if (betValue >= 1) {
				Players p = Players.allPlayers[currentPlayerIndex];
				p.originalBet = betValue;
				p.bet = p.originalBet;

				currentPlayerIndex++;

				while (currentPlayerIndex < Players.numPlayers && Players.allPlayers[currentPlayerIndex].bet != 0) {
					currentPlayerIndex++;
				}

				if (currentPlayerIndex < Players.numPlayers) {

					betLabel.setText("How much is your bet " + Players.allPlayers[currentPlayerIndex].nick + "?");
					betInput.clear();
				} else {

					nextButton.setVisible(true);
					setButton.setVisible(false);
					betInput.setVisible(false);
					betLabel.setText("All bets set. Ready to play!");
				}
			} else {
				betLabel.setText("The bet must be at least 1.");
			}
		} catch (NumberFormatException ex) {

			betLabel.setText("Invalid bet for " + Players.allPlayers[currentPlayerIndex].nick
					+ ": Enter a valid positive number.");
		}
	}

	public void toScene3(ActionEvent e) throws IOException {
		Players.currentPlayer = 0;
		sceneController.switchScene(e, "scene3.fxml");
	}

	// Player actions (In-game)

	public void hitAction(ActionEvent e) {

		Players player = Players.allPlayers[Players.currentPlayer];

		if (!player.nick.equals("Dealer")) {
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
			appendToConsole(output);

			Players lastHumanPlayer = Players.allPlayers[Players.numPlayers - 1]; // Incase of last player

			if (player.nick.equals(lastHumanPlayer.nick)) {
				Players dealer = Players.allPlayers[Players.numPlayers];
				resources.hit(dealer, dealer.deck);

				String dealerOutput = dealer.nick + " hit!\n" + dealer.nick + "'s card this round: " + dealer.card + "\n" + "The amount of cards in " + dealer.nick + "'s deck: " + dealer.deck.size() + "\n" + "The total value of " + dealer.nick + "'s hand: " + dealer.score + "\n";
				
				PauseTransition pause = new PauseTransition(Duration.seconds(0.2));
				pause.setOnFinished(event -> {
					appendToConsole(dealerOutput);
					Players.currentPlayer = 0;
				});
				pause.play();
				
			} else {
				Players.currentPlayer++;
			}
		}
	}

	public void splitAction(ActionEvent e) {
		Players player = Players.allPlayers[Players.currentPlayer];

		resources.split(player);

		String output = player.nick + " split!";

		output += "\nThe amount of " + player.nick + "'s cards in the splitted deck is " + player.splitDeck.size();

		output += "\nThe total value of " + player.nick + "'s split hand: " + player.splitScore + "\n";

		appendToConsole(output);

		if (player.nick == "Dealer") {
			Players.currentPlayer = 0;
		} else {
			Players.currentPlayer++;
		}
	}

	public void doubleAction(ActionEvent e) {
		Players player = Players.allPlayers[Players.currentPlayer];
		resources.doubleDown(player);

		String output = player.nick + " doubled down!";

		output += player.nick + "'s bet now is " + player.bet;
		resources.hit(player, player.deck);
	}

	public void standAction(ActionEvent e) {
		Players player = Players.allPlayers[Players.currentPlayer];
		resources.stand(player, player.score);
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
		for (int i = 0; i < Players.numPlayers; i++) {
			Players p = Players.allPlayers[i];

			if (Players.allPlayers[i] != null) {
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

		Players.allPlayers = new Players[0];
		Players.currentPlayer = 0;
		Players.numPlayers = 1; // Because the min amount of players is 1
		currentPlayerIndex = -1; // The player amount is not given in the start / game hasn't started yet

		try (FileWriter writer = new FileWriter("player_bets.txt", false)) {
			writer.write(""); // ← empty file
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
