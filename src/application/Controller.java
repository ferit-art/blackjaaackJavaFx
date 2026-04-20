package application;

import java.io.File;
import java.io.FileWriter;
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

// 	The implementation of bust and blackjack (21), as well as other outcomes like tie and double-downed win.

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
				continuationWith(player);
			}
		} else if (player.splitTurn && !player.splitHasStood) {

			player.splitTurn = false;

			Resources.hit(player, player.splitDeck);

			output = player.nick + " hit!\n";

			output += player.nick + "'s drawn card this round: " + player.splitCard + "\n";

			output += "The amount of cards in " + player.nick + "'s splitted deck: " + player.splitDeck.size() + "\n";

			output += "The total value of " + player.nick + "'s splitted hand: " + player.splitScore + "\n";

			continuationWith(player);

		} else {
			output += "\n" + "Error with turn automation, kindly contact the maker..." + "\n";
		}
		appendToConsole(output);
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
			continuationWith(player);
		} else if (player.splitTurn && !player.splitHasStood) {

			Resources.stand(player, player.splitDeck);
			output = "\n" + player.nick + " stands with " + player.splitScore
					+ " as the total score for their splitted hand" + "\n";

			continuationWith(player);
		} else {

			output = "\n" + "Automation error." + "\n";
		}
		appendToConsole(output);
	}

	// Navigation event listeners

	public void goBack(ActionEvent e) throws IOException {
		resetGame();
		sceneController.sceneHistory.pop();

		String fxmlFile = sceneController.sceneHistory.peek();
		sceneController.switchScene(e, fxmlFile);
	}

	public void goHome(ActionEvent e) throws IOException {
		resetGame();
		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
	}

	// Helper methods

	public void continuationWith(Player player) {
		Player lastHumanPlayer = game.getLastHumanPlayer();
		String output;
		Player nextPlayer = null;

		if (player.nick.equals(lastHumanPlayer.nick)) {

			Player dealer = game.getAllPlayers()[game.getNumPlayers()];
			Resources.hit(dealer, dealer.deck);

			output = dealer.nick + " hit!\n" + dealer.nick + "'s card this round: " + dealer.card + "\n"
					+ "The amount of cards in " + dealer.nick + "'s deck: " + dealer.deck.size() + "\n"
					+ "The total value of " + dealer.nick + "'s hand: " + dealer.score + "\n";

			game.setCurrentPlayerIndex(0);
			nextPlayer = game.getAllPlayers()[game.getCurrentPlayerIndex()];

			PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
			pause.setOnFinished(event -> {
				appendToConsole(output);
			});
			pause.play();

		} else {
			game.setCurrentPlayerIndex(game.getCurrentPlayerIndex() + 1);
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

	private void resetGame() {
		for (int i = 0; i < game.getNumPlayers(); i++) {
			Player p = game.getAllPlayers()[i];

			if (game.getAllPlayers()[i] != null) {
				p.deck.clear();
				p.splitDeck.clear();

				p.score = 0;
				p.splitScore = 0;
				p.bet = 0;
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
		game.setCurrentPlayerIndex(-1); // The player amount is not given in the start => game hasn't started yet

		try (FileWriter writer = new FileWriter("player_bets.txt", false)) {
			writer.write(""); // Empty file
			writer.flush();
		} catch (Exception ignored) {
		}

		try {
			new File("player_bets.txt").delete();
		} catch (Exception ignored) {
			System.out.println("crash");
		}
	}
}
