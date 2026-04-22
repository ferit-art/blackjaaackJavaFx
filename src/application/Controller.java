package application;

import java.io.IOException;
import java.util.HashMap;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// 	Current notes:

//	The view of the dealer + players, including the decks with cards and nicknames. When giving the player a new splitdeck 
//	in splitAction, the method should add another deck into the HBox.
//	Dealer's hbox is halvdone, card visuals are needed.

// 	NEXT => The implementation of bust and blackjack (21), as well as other outcomes like tie and double-downed win.
//	=>> Problem with the automation at the end of the continuationWith() method, when dealer hits or the index is 
//	changed, the index in allPlayers should not have stood... CONSIDER other possible outcomes too.

// 	It is alright to have much code but write new methods so that the event handler methods can be read in english.

//	Start a vscode (for home-pc) and a eclipse-branch for the blackjaaackJavaFx project when time is sufficient.

//	If time is sufficient, animations would be nice... (Although other exams and such have more priority)

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
	@FXML
	HBox dealerContainer;

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

		Controller ctrl = sceneController.switchSceneWithController(e, "scene2.fxml");
		game.setCurrentPlayerIndex(0);

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

		int lastHumanPlayerIndex = game.getNumPlayers() - 1;
		game.setLastHumanPlayer(game.getAllPlayers()[lastHumanPlayerIndex]);

		Resources.setup();

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
				p.bet = betValue;

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
		Controller scene3Controller = sceneController.switchSceneWithController(e, "scene3.fxml");
		scene3Controller.setupView();
	}

	// Player actions (In-game)

	public void hitAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		String output = "";

		if (!player.splitTurn && !player.hasStood) {

			Resources.hit(player, player.deck);

			output = player.nick + " hit!\n";

			output += player.nick + "'s drawn card this round: " + player.card + "\n";

			output += "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

			output += "The total value of " + player.nick + "'s hand: " + player.score + "\n";

			if (player.hasSplit) {

				output += "\n" + player.nick + "'s split turn:" + "\n";
				player.splitTurn = true;

			} else {
				appendToConsole(output);
				continuationWith(player);
			}
		} else if (player.splitTurn && !player.splitHasStood) {

			player.splitTurn = false;

			Resources.hit(player, player.splitDeck);

			output = player.nick + " hit!\n";

			output += player.nick + "'s drawn card this round: " + player.splitCard + "\n";

			output += "The amount of cards in " + player.nick + "'s splitted deck: " + player.splitDeck.size() + "\n";

			output += "The total value of " + player.nick + "'s splitted hand: " + player.splitScore + "\n";

			appendToConsole(output);
			continuationWith(player);

		} else {
			output += "\n" + "Error with turn automation, kindly contact the maker..." + "\n";
			appendToConsole(output);
		}
	}

	public void splitAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		String output = "";

		if (player.nick.equals("Dealer")) {

			return;
		} else if (!Resources.splitAble(player)) {

			output = "\n" + "Every player can split hands only 1 time...\n" + player.nick
					+ " has to have 2 cards of the same value in his hand to be able to split..." + "\n" + "\n"
					+ player.nick + " apparently can not split..." + "\n";

		} else {
			Resources.split(player);

			output = "\n" + "Split action with two " + player.splitCard + "s is taken" + "\n";

			output += "\n" + player.nick + " split and hit 1 time for both of their decks!" + "\n";

			output += "\n" + "The amount of cards in " + player.nick + "'s splitted deck: " + player.splitDeck.size();
			output += "\n" + "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

			output += "\n" + "The total value of " + player.nick + "'s hand: " + player.score;
			output += "\n" + "The total value of " + player.nick + "'s split hand: " + player.splitScore + "\n";

			continuationWith(player);
		}
		appendToConsole(output);
	}

	public void doubleDownAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		String output = "";

		if (!player.nick.equals("Dealer") && !player.splitTurn && !player.hasStood) {

			Resources.doubleDown(player);

			output = player.nick + " doubled down!\n";

			output += player.nick + "'s bet now is: " + player.bet + "\n";

			output += player.nick + "'s drawn card this round: " + player.card + "\n";

			output += "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

			output += "The total value of " + player.nick + "'s hand: " + player.score + "\n";

			if (player.hasSplit) {

				output += "\n" + player.nick + "'s split turn:" + "\n";
				player.splitTurn = true;

			} else {
				continuationWith(player);
			}

		} else if (player.splitTurn && !player.splitHasStood) {

			Resources.doubleDown(player);

			output = player.nick + " doubled down with their split deck!" + "\n";

			output += player.nick + "'s bet now is: " + player.bet + "\n";

			output += player.nick + "'s drawn card this round: " + player.splitCard + "\n";

			output += "The amount of cards in " + player.nick + "'s deck: " + player.splitDeck.size() + "\n";

			output += "The total value of " + player.nick + "'s hand: " + player.splitScore + "\n";

			continuationWith(player);

		} else {
			output += "\n" + "Error with turn automation, kindly contact the maker..." + "\n";

		}
		appendToConsole(output);
	}

	public void standAction(ActionEvent e) {

		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		String output = " ";

		if (!player.splitTurn && !player.hasStood) {

			Resources.stand(player, player.deck);
			output = "\n" + player.nick + " stands with " + player.score + " as their total score" + "\n";

			if (player.hasSplit) {

				output += "\n" + player.nick + "'s split turn:" + "\n";
				player.splitTurn = true;

			}
			appendToConsole(output);
			continuationWith(player);
		} else if (player.splitTurn && !player.splitHasStood) {

			Resources.stand(player, player.splitDeck);
			output = "\n" + player.nick + " stands with " + player.splitScore
					+ " as the total score for their splitted hand" + "\n";

			appendToConsole(output);
			continuationWith(player);
		} else {

			output = "\n" + "Automation error." + "\n";
			appendToConsole(output);
		}
	}

	// Navigation event listeners

	public void goBack(ActionEvent e) throws IOException {
		Resources.resetGame();
		sceneController.sceneHistory.pop();

		String fxmlFile = sceneController.sceneHistory.peek();
		sceneController.switchScene(e, fxmlFile);
	}

	public void goHome(ActionEvent e) throws IOException {
		Resources.resetGame();
		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
	}

	// Helper methods

	public void continuationWith(Player player) {

		if (!player.hasStood) {
			String status = Resources.scoreChecks(player, player.deck);
			if (status.equals("Bust") || status.equals("Blackjack")) {

				player.hasStood = true;
				appendToConsole("\n--> " + player.nick + " got a " + status + " on their primary hand!" + "\n");
			}
		}

		if (player.hasSplit && !player.splitHasStood) {
			String splitStatus = Resources.scoreChecks(player, player.splitDeck);
			if (splitStatus.equals("Bust") || splitStatus.equals("Blackjack")) {

				player.splitHasStood = true; // Lock the flag!
				appendToConsole("\n--> " + player.nick + " got a " + splitStatus + " on their split hand!");
			}
		}

		Player lastHumanPlayer = game.getLastHumanPlayer();
		Player nextPlayer = null;

		if (player.nick.equals(lastHumanPlayer.nick)) {

			Player dealer = game.getAllPlayers()[game.getNumPlayers()];

			if (!dealer.hasStood) {
				Resources.hit(dealer, dealer.deck);
			}

			String dealerStatus = Resources.scoreChecks(dealer, dealer.deck);

			if (dealerStatus.equals("Bust") || dealerStatus.equals("Blackjack")) {

				if (!dealer.hasStood) {

					dealer.hasStood = true;
					appendToConsole("\n--> " + dealer.nick + " got a " + dealerStatus + " on its hand!" + "\n");
				}
			} else if (dealerStatus.equals("17 or greater as score")) {

				if (!dealer.hasStood) {

					dealer.hasStood = true;
					appendToConsole("\n--> " + dealer.nick + " got " + dealerStatus + " on its hand!" + "\n" + "\n"
							+ "\n--> " + dealer.nick + " stands with " + dealer.score);
				}
			} else {
				String output = dealer.nick + " hit!\n" + dealer.nick + "'s card this round: " + dealer.card + "\n"
						+ "The amount of cards in " + dealer.nick + "'s deck: " + dealer.deck.size() + "\n"
						+ "The total value of " + dealer.nick + "'s hand: " + dealer.score + "\n";

				PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
				pause.setOnFinished(event -> {
					appendToConsole(output);
				});
				pause.play();
			}

			Player[] allPlayers = game.getAllPlayers();
			for (int i = 0; i < allPlayers.length; i++) {
				
				if (!allPlayers[i].hasStood) {
					game.setCurrentPlayerIndex(i);
					break;
				} 
			}
			
			game.setCurrentPlayerIndex(0);
			nextPlayer = game.getAllPlayers()[game.getCurrentPlayerIndex()];

		} else {
			game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);
			nextPlayer = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		}

		if (nextPlayer.hasSplit && nextPlayer.hasStood) {

			nextPlayer.splitTurn = true;
		}
	}

	public void setupView() {
		HashMap<String, HBox> playerDeckContainers = game.getPlayerDeckContainers();

		/*
		 * for (int i = 0; i < game.getNumPlayers(); i++) { Player p =
		 * game.getAllPlayers()[i];
		 * 
		 * VBox playerUiBox = new VBox(10);
		 * playerUiBox.setStyle("-fx-alignment: center;");
		 * 
		 * Label nameLabel = new Label(p.nick); Label betLabel = new Label("Bet: " +
		 * p.bet); Label scoreLabel = new Label("Score: " + p.score);
		 * 
		 * // The box that will actually hold their card images HBox cardsBox = new
		 * HBox(-20); // Negative spacing makes the cards overlap slightly!
		 * 
		 * // Save this cardsBox in our map so we can find it later
		 * playerCardBoxes.put(p.nick, cardsBox);
		 * 
		 * playerUiBox.getChildren().addAll(nameLabel, betLabel, scoreLabel, cardsBox);
		 * playersContainer.getChildren().add(playerUiBox); }
		 */

		// Dealer
		Player dealer = game.getAllPlayers()[game.getNumPlayers()];

		Label dealerNameLabel = new Label(dealer.nick);
		dealerNameLabel.setStyle("-fx-font-weight: bolder; -fx-font-size: 20px;");

		HBox dealerCardsBox = new HBox(-10);
		dealerCardsBox.setAlignment(Pos.CENTER);

		playerDeckContainers.put("Dealer", dealerCardsBox);
		game.setPlayerDeckContainers(playerDeckContainers);

		VBox dealerVBox = new VBox(10);
		dealerVBox.setAlignment(Pos.CENTER);
		dealerVBox.getChildren().addAll(dealerNameLabel, dealerCardsBox);

		dealerContainer.setAlignment(javafx.geometry.Pos.CENTER);
		dealerContainer.getChildren().clear(); // Clears out old duplicates if you revisit the scene
		dealerContainer.getChildren().add(dealerVBox);

		dealerContainer.getChildren().addAll(dealerNameLabel, dealerCardsBox);
	}

	private void appendToConsole(String message) {
		if (consoleLog.getText().isEmpty()) {
			consoleLog.setText(message);
		} else {
			consoleLog.setText(consoleLog.getText() + "\n" + message);
		}

		consoleScrollPane.layout();
		consoleScrollPane.setVvalue(1.0); // 1.0 = bottom
	}
}
