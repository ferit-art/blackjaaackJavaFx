package application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

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

// Current notes:

// Work in progress as of late: EventListeners to the double down and stand buttons,
// including the implamentation of the code from the previous version.

// The automation of hitAction for the Dealer hasn't been corrected, thus DOES NOT work as of current state. 

public class Controller {

	// UI subjects

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
		players.numPlayers = (int) sliderNumPlayers.getValue();
	}

	public void startBtn(ActionEvent e) throws IOException {
		sceneController.switchScene(e, "scene1.fxml");
	}

	public void notUnderstand(ActionEvent e) throws IOException {
		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
		players.allPlayers = new players[0];
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

		players.allPlayers = new players[players.numPlayers + 1]; // +1 for the dealer

		for (int i = 0; i <= players.numPlayers; i++) {

			if (i == players.numPlayers) {
				players.allPlayers[i] = new players();
				players.allPlayers[i].nick = "Dealer";

			} else {
				players.allPlayers[i] = new players();
				players.allPlayers[i].nick = "Player " + (i + 1);
			}
		}

		resources.setup();

		players.loadBets(players.allPlayers, ctrl);

		ctrl.currentPlayerIndex = 0;

		while (ctrl.currentPlayerIndex < players.numPlayers && players.allPlayers[ctrl.currentPlayerIndex].bet != 0) {
			ctrl.currentPlayerIndex++;
		}

		if (ctrl.currentPlayerIndex < players.numPlayers) {

			ctrl.betLabel.setText("How much is your bet " + players.allPlayers[ctrl.currentPlayerIndex].nick + "?");
			ctrl.betInput.setVisible(true);
		} else {

			ctrl.betLabel.setText("All bets loaded. Ready to play!");
			ctrl.betInput.setVisible(false);
		}
	}

	public void setBet(ActionEvent e) {
		if (currentPlayerIndex == -1 || currentPlayerIndex >= players.numPlayers) {
			nextButton.setVisible(true);
			setButton.setVisible(false);
			return; // No more players need to set bets
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

					betLabel.setText("How much is your bet " + players.allPlayers[currentPlayerIndex].nick + "?");
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

			betLabel.setText("Invalid bet for " + players.allPlayers[currentPlayerIndex].nick
					+ ": Enter a valid positive number.");
		}
	}

	public void toScene3(ActionEvent e) throws IOException {
		players.currentPlayer = 0;
		sceneController.switchScene(e, "scene3.fxml");
	}

	// Player actions (In-game)

	public void hitAction(ActionEvent e) {

		players player = players.allPlayers[players.currentPlayer];

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

			players lastHumanPlayer = players.allPlayers[players.numPlayers - 1]; // Incase of last player

			if (player.nick.equals(lastHumanPlayer.nick)) {
				players dealer = players.allPlayers[players.numPlayers];
				resources.hit(dealer, dealer.deck);

				String dealerOutput = dealer.nick + " hit!\n";

				dealerOutput += dealer.nick + "'s card this round: " + dealer.card + "\n";

				dealerOutput += "The amount of cards in " + dealer.nick + "'s deck: " + dealer.deck.size() + "\n";

				dealerOutput += "The total value of " + dealer.nick + "'s hand: " + dealer.score + "\n";
				
				// Thread.sleep / pause for 2 secs here
				
				appendToConsole(dealerOutput);
				players.currentPlayer = 0;
			} else {
				players.currentPlayer++;
			}
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

	public void doubleAction(ActionEvent e) {
		players player = players.allPlayers[players.currentPlayer];
		resources.doubleDown(player);

		String output = player.nick + " doubled down!";

		output += player.nick + "'s bet now is " + player.bet;
		resources.hit(player, player.deck);
	}

	public void standAction(ActionEvent e) {
		players player = players.allPlayers[players.currentPlayer];
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
		players.numPlayers = 1; // Because the min amount is 1
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
