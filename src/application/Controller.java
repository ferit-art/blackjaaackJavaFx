package application;

import java.io.FileWriter;
import java.io.IOException;

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

// 	Current notes:

//	Seperate the section between the empty comment rows in Players.java as a new class with the name of 
//	Player.java and make the necessary adjustments in the rest of the code. => Do this in the java version
//	of the game too, although it is not the prioriety.

//	Seperation is done for the fx project.

// 	EventListeners to the double down and stand buttons,
// 	including the implamentation of the code from the previous version.
//	Thus, the splitAction instance variable is nessesary to determine which hand (called deck) the turn's action should affect.

// 	The implementation of bust and blackjack (21), as well as other outcomes like tie and double-downed win.

// 	It is alright to have much code but write new methods so that the event handler methods can be read in english.

//	If time is sufficient, animations would be nice...

public class Controller {
	public static Game game = new Game();

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
		game.setNumPlayers((int) sliderNumPlayers.getValue());
	}

	public void startBtn(ActionEvent e) throws IOException {
		sceneController.switchScene(e, "scene1.fxml");
	}

	public void notUnderstand(ActionEvent e) throws IOException {
		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
		game.setAllPlayers(new Player[0]);
	}

	public void understand(ActionEvent e) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("scene2.fxml")); // Same thing as switchScene, but
																					// delivers the controller too
		sceneController.sceneHistory.push("scene2.fxml");

		Parent root = loader.load();
		Controller ctrl = loader.getController();

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		game.setCurrentPlayerIndex(0);
		stage.show();

		game.setAllPlayers(new Player[game.getNumPlayers() + 1]); // +1 for the dealer

		for (int i = 0; i <= game.getNumPlayers(); i++) { // Initializing all players

			if (i == game.getNumPlayers()) {
				game.getAllPlayers()[i] = new Player();

				game.getAllPlayers()[i].nick = "Dealer";

			} else {
				game.getAllPlayers()[i] = new Player();

				game.getAllPlayers()[i].nick = "Player " + (i + 1);
			}
		}

		resources.setup();

		Game.loadBets(game.getAllPlayers(), ctrl);

		game.setCurrentPlayerIndex(0);

		while (game.getCurrentPlayerIndex() < game.getNumPlayers()
				&& game.getAllPlayers()[game.getCurrentPlayerIndex()].bet != 0) {
			game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);
		}

		if (game.getCurrentPlayerIndex() < game.getNumPlayers()) {

			ctrl.betLabel
					.setText("How much is your bet " + game.getAllPlayers()[game.getCurrentPlayerIndex()].nick + "?");
			ctrl.betInput.setVisible(true);
		} else {

			ctrl.betLabel.setText("All bets loaded. Ready to play!");
			ctrl.betInput.setVisible(false);
		}
	}

	public void setBet(ActionEvent e) {
		if (game.getCurrentPlayerIndex() == -1 || game.getCurrentPlayerIndex() >= game.getNumPlayers()) {
			nextButton.setVisible(true);
			setButton.setVisible(false);
			return; // No more players need to set bets
		}

		try {
			int betValue = Integer.parseInt(betInput.getText());

			if (betValue >= 1) {
				Player p = game.getAllPlayers()[game.getCurrentPlayerIndex()];
				p.originalBet = betValue;
				p.bet = p.originalBet;

				game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);

				// Skips to the next player with 0 as bet
				while (game.getCurrentPlayerIndex() < game.getNumPlayers()
						&& game.getAllPlayers()[game.getCurrentPlayerIndex()].bet != 0) {
					game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);
				}

				if (game.getCurrentPlayerIndex() < game.getNumPlayers()) {

					betLabel.setText(
							"How much is your bet " + game.getAllPlayers()[game.getCurrentPlayerIndex()].nick + "?");
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

			betLabel.setText("Invalid bet for " + game.getAllPlayers()[game.getCurrentPlayerIndex()].nick
					+ ": Enter a valid positive number.");
		}
	}

	public void toScene3(ActionEvent e) throws IOException {
		game.setCurrentPlayerIndex(0);
		sceneController.switchScene(e, "scene3.fxml");
	}

	// Player actions (In-game)

	public void hitAction(ActionEvent e) {

		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];

		if (!player.nick.equals("Dealer") && !player.splitTurn) {
			resources.hit(player, player.deck);

			String output = player.nick + " hit!\n";

			output += player.nick + "'s card this round: " + player.card + "\n";

			output += "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

			output += "The total value of " + player.nick + "'s hand: " + player.score + "\n";

			appendToConsole(output);

			Player lastHumanPlayer = game.getAllPlayers()[game.getNumPlayers() - 1]; // Incase of last player

			if (player.nick.equals(lastHumanPlayer.nick)) {
				Player dealer = game.getAllPlayers()[game.getNumPlayers()];
				resources.hit(dealer, dealer.deck);

				String dealerOutput = dealer.nick + " hit!\n" + dealer.nick + "'s card this round: " + dealer.card
						+ "\n" + "The amount of cards in " + dealer.nick + "'s deck: " + dealer.deck.size() + "\n"
						+ "The total value of " + dealer.nick + "'s hand: " + dealer.score + "\n";

				PauseTransition pause = new PauseTransition(Duration.seconds(0.2));
				pause.setOnFinished(event -> {
					appendToConsole(dealerOutput);
					game.setCurrentPlayerIndex(0);
				});
				pause.play();
			} else if (player.splitTurn) {

			} else {
				game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);
			}
		}
	}

	public void splitAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];

		if (!resources.splitAble(player)) {
			String output = "Every player can split hands only 1 time...\n"
					+ player.nick + " has to have 2 cards of the same value in his hand to be able to split..." + "\n";
			appendToConsole(output);
		} else if (resources.splitAble(player)) {
			resources.split(player);

			String output = player.nick + " split!";

			output += "\nThe amount of " + player.nick + "'s cards in the splitted deck is " + player.splitDeck.size();

			output += "\nThe total value of " + player.nick + "'s split hand: " + player.splitScore + "\n";

			appendToConsole(output);
		}
	}

	public void doubleDownAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		resources.doubleDown(player);

		String output = player.nick + " doubled down!";

		output += player.nick + "'s bet now is " + player.bet;
		resources.hit(player, player.deck);
	}

	public void standAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		resources.stand(player, player.score);

		if (player.nick.equals("Dealer")) {
			appendToConsole("\n" + player.nick + " stands with " + player.score + " as the total value");

		} else {
			if (player.splitTurn) {

			}
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
		for (int i = 0; i < game.getNumPlayers(); i++) {
			Player p = game.getAllPlayers()[i];

			if (game.getAllPlayers()[i] != null) {
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
				p.splitTurn = false;
				p.choice = false;
			}
		}

		game.setAllPlayers(new Player[0]);
		game.setNumPlayers(1); // Because the min amount of players is 1
		game.setCurrentPlayerIndex(-1); // The player amount is not given in the start / game hasn't started yet

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
