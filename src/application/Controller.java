package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// 	Current notes:

//	The view of the dealer + players, including the decks with cards and nicknames. When giving the player a new splitdeck 
//	in splitAction, the method should add another deck into the HBox.
//	Dealer's hbox is halvdone, card visuals are needed.

//	The question of if the saved bets will be used should be implemented before the showcase.

// 	It is alright to have much code but write new methods so that the event handler methods can be read in english.

public class Controller {
	public static Game game = new Game();

	// UI objects

	@FXML
	HBox playersContainer;
	@FXML
	ScrollPane consoleScrollPane;
	@FXML
	Label betQuestion;
	@FXML
	Label betLabel;
	@FXML
	Label consoleLog;
	@FXML
	TextField betInput;
	@FXML
	Button saveBets;
	@FXML
	Button noSavedBets;
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

	public void hideConsole(ActionEvent e) {

		if (game.isConsoleIsVisible()) {

			game.setConsoleIsVisible(false);
			consoleScrollPane.setVisible(false);
		} else {

			game.setConsoleIsVisible(true);
			consoleScrollPane.setVisible(true);
		}
	}

	// Player actions (In-game)

	public void hitAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		String output = "";

		if (game.getGameOver()) {

			return;

		} else if (!player.splitTurn && !player.hasStood) {

			Resources.hit(player, player.deck);

			output = player.nick + " hit!\n";

			output += player.nick + "'s drawn card this round: " + player.card + "\n";

			output += "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

			output += "The total value of " + player.nick + "'s hand: " + player.score + "\n";

			if (player.hasSplit) {

				output += "\n" + player.nick + "'s split turn:" + "\n";
				player.splitTurn = true;

			}
			updateCardsOnScreen();
			appendToConsole(output);
			continuationWith(player);

		} else if (player.splitTurn && !player.splitHasStood) {

			player.splitTurn = false;

			Resources.hit(player, player.splitDeck);

			output = player.nick + " hit!\n";

			output += player.nick + "'s drawn card this round: " + player.splitCard + "\n";

			output += "The amount of cards in " + player.nick + "'s splitted deck: " + player.splitDeck.size() + "\n";

			output += "The total value of " + player.nick + "'s splitted hand: " + player.splitScore + "\n";

			updateCardsOnScreen();
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

		if (!Resources.splitAble(player)) {

			if (game.getGameOver()) {

				return;

			} else {
				output = "\n" + "Every player can split hands only 1 time...\n" + player.nick
						+ " has to have 2 cards of the same value in his hand to be able to split..." + "\n" + "\n"
						+ player.nick + " apparently can not split..." + "\n";

				appendToConsole(output);
			}
		} else {
			Resources.split(player);

			output = "\n" + "Split action with two " + player.splitCard + "s is taken" + "\n";

			output += "\n" + player.nick + " split and hit 1 time for both of their decks!" + "\n";

			output += "\n" + "The amount of cards in " + player.nick + "'s splitted deck: " + player.splitDeck.size();
			output += "\n" + "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

			output += "\n" + "The total value of " + player.nick + "'s hand: " + player.score;
			output += "\n" + "The total value of " + player.nick + "'s split hand: " + player.splitScore + "\n";

			updateCardsOnScreen();
			appendToConsole(output);
			continuationWith(player);
		}
	}

	public void doubleDownAction(ActionEvent e) {
		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		String output = "";

		if (game.getGameOver()) {

			return;

		} else if (!player.splitTurn && !player.hasStood) {

			Resources.doubleDown(player);

			output = player.nick + " doubled down!\n";

			output += player.nick + "'s bet now is: " + player.bet + "\n";

			output += player.nick + "'s drawn card this round: " + player.card + "\n";

			output += "The amount of cards in " + player.nick + "'s deck: " + player.deck.size() + "\n";

			output += "The total value of " + player.nick + "'s hand: " + player.score + "\n";

			if (player.hasSplit) {

				output += "\n" + player.nick + "'s split turn:" + "\n";
				player.splitTurn = true;
			}

			updateCardsOnScreen();
			appendToConsole(output);
			continuationWith(player);

		} else if (player.splitTurn && !player.splitHasStood) {

			Resources.doubleDown(player);

			output = player.nick + " doubled down with their split deck!" + "\n";

			output += player.nick + "'s bet now is: " + player.bet + "\n";

			output += player.nick + "'s drawn card this round: " + player.splitCard + "\n";

			output += "The amount of cards in " + player.nick + "'s deck: " + player.splitDeck.size() + "\n";

			output += "The total value of " + player.nick + "'s hand: " + player.splitScore + "\n";

			updateCardsOnScreen();
			appendToConsole(output);
			continuationWith(player);

		} else {
			output += "\n" + "Error with turn automation, kindly contact the maker..." + "\n";
			appendToConsole(output);
		}
	}

	public void standAction(ActionEvent e) {

		Player player = game.getAllPlayers()[game.getCurrentPlayerIndex()];
		String output = " ";

		if (game.getGameOver()) {

			return;

		} else if (!player.splitTurn && !player.hasStood) {

			Resources.stand(player, player.deck);
			output = "\n" + player.nick + " stands with " + player.score + " as their total score" + "\n";

			if (player.hasSplit) {

				output += "\n" + player.nick + "'s split turn:" + "\n";
				player.splitTurn = true;
			}

			updateCardsOnScreen();
			appendToConsole(output);
			continuationWith(player);

		} else if (player.splitTurn && !player.splitHasStood) {

			Resources.stand(player, player.splitDeck);
			output = "\n" + player.nick + " stands with " + player.splitScore
					+ " as the total score for their splitted hand" + "\n";

			updateCardsOnScreen();
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

	public void goHomeWithSavedBets(ActionEvent e) throws IOException {

		Player.saveBets();
		Resources.resetGame();

		sceneController.sceneHistory.clear();
		sceneController.switchScene(e, "startScene.fxml");
	}

	// Helper methods

	public void getBetResults() {
		Player[] allPlayers = game.getAllPlayers();
		Resources.getConsequences(allPlayers);
		String output = "";

		for (Player player : allPlayers) {

			if (!player.nick.equals("Dealer")) {

				output += "\n" + player.nick + "'s bet now is: " + player.bet + "\n";

				if (player.hasSplit) {
					output += "\n" + player.nick + "'s bet now is: " + player.bet + "\n";
				}
			}
		}
		output += "\n-->" + " Decide if you want to save the new bets " + "<--\n";
		output += "\n-->" + " THIS ROUND IS OVER " + "<--\n";
		appendToConsole(output);
	}

	public void roundlyPlayerCheck(Player player) {

		if (!player.hasStood || player.hasDoubledDown) {
			String status = Resources.scoreCheck(player, player.deck);

			if (status.equals("bust") || status.equals("blackjack")) {
				player.hasStood = true;
				appendToConsole("\n--> " + player.nick + " got a " + status + " on their primary hand!" + "\n");
			}
		}

		if (player.hasSplit && !player.splitHasStood) {
			String splitStatus = Resources.scoreCheck(player, player.splitDeck);

			if (splitStatus.equals("bust") || splitStatus.equals("blackjack")) {
				player.splitHasStood = true;
				appendToConsole("\n--> " + player.nick + " got a " + splitStatus + " on their split hand!");
			}
		}
	}

	public void dealerHitAction(Player dealer) {

		if (!dealer.hasStood && !game.getGameOver()) {
			Resources.hit(dealer, dealer.deck);

			String output = dealer.nick + " hit!\n" + dealer.nick + "'s card this round: " + dealer.card + "\n"
					+ "The amount of cards in " + dealer.nick + "'s deck: " + dealer.deck.size() + "\n"
					+ "The total value of " + dealer.nick + "'s hand: " + dealer.score + "\n";

			updateCardsOnScreen();
			appendToConsole(output);
		}
	}

	public void dealerCheck(Player dealer) {

		String dealerStatus = Resources.scoreCheck(dealer, dealer.deck);

		if (dealerStatus.equals("bust") || dealerStatus.equals("blackjack")) {

			if (!dealer.hasStood) {

				dealer.hasStood = true;
				appendToConsole("\n--> " + dealer.nick + " got a " + dealerStatus + " on its hand!" + "\n");
			}
		} else if (dealerStatus.equals("17 or greater as score")) {

			if (!dealer.hasStood) {

				dealer.hasStood = true;
				appendToConsole("\n--> " + dealer.nick + " got " + dealerStatus + " on its hand" + "\n--> "
						+ dealer.nick + " stands on " + dealer.score + "\n");
			}
		}
	}

	public Player findNextPlayer(int startIndex) {

		Player[] allPlayers = game.getAllPlayers();

		for (int i = 0; i < allPlayers.length; i++) {

			int checkIndex = (startIndex + i) % allPlayers.length; // Modulo trick will scan indexes 1, 2, 3 and if it
																	// didn't find anyone active, scans 0.
			Player suspect = allPlayers[checkIndex];

			if (!suspect.nick.equals("Dealer")) {
				if (!suspect.hasStood) {

					game.setCurrentPlayerIndex(checkIndex);
					return suspect;

				} else if (suspect.hasSplit && !suspect.splitHasStood) {

					suspect.splitTurn = true;
					game.setCurrentPlayerIndex(checkIndex);
					return suspect;
				}
			}
		}
		return null;
	}

	public void continuationWith(Player player) {

		roundlyPlayerCheck(player);

		Player lastHumanPlayer = game.getLastHumanPlayer();
		Player nextPlayer = null;

		if (player.nick.equals(lastHumanPlayer.nick)) {

			Player dealer = game.getAllPlayers()[game.getNumPlayers()];
			dealerHitAction(dealer);
			dealerCheck(dealer);

			nextPlayer = findNextPlayer(0);
		} else {

			int startIndex = game.getCurrentPlayerIndex();
			nextPlayer = findNextPlayer(startIndex + 1);
		}

		if (nextPlayer == null) {

			gameOver();
			getBetResults();
			betQuestion.setVisible(true);
			saveBets.setVisible(true);
			noSavedBets.setVisible(true);

			return;
		}

		if (nextPlayer.hasSplit && nextPlayer.hasStood) {

			nextPlayer.splitTurn = true;
		}
	}

	private void gameOver() {

		game.setGameOver(true);

		String output = "\n" + "Results:" + "\n";
		Player[] allPlayers = game.getAllPlayers();
		ArrayList<Player> winners = Resources.getWinners(allPlayers, true);
		ArrayList<Player> splitWinners = Resources.getWinners(allPlayers, false);

		if (winners.isEmpty() && splitWinners.isEmpty()) {
			output = "\n" + "There are no winners... :(" + "\n";
			output += "\n" + "Better luck next time!" + "\n" + "\n";

		} else {
			for (Player winner : winners) {
				output += "\n--> " + winner.nick + "'s primary hand has won with " + winner.score + " as score" + "\n";
			}

			for (Player winner : splitWinners) {
				output += "\n--> " + winner.nick + "'s split hand has won with " + winner.splitScore + " as score"
						+ "\n";
			}
		}
		appendToConsole(output);
	}

	public void setupView() {
		HashMap<String, HBox> playerDeckContainers = game.getPlayerDeckContainers();

		if (playersContainer != null) {
			playersContainer.getChildren().clear();
		}

		// 1. Build the Human Players
		for (int i = 0; i < game.getNumPlayers(); i++) {
			Player player = game.getAllPlayers()[i];

			VBox playerBox = new VBox(10);
			playerBox.setAlignment(Pos.BOTTOM_CENTER);
			playerBox.setPadding(new Insets(0, 50, 0, 50));

			Label nameLabel = new Label(player.nick);
			nameLabel.setStyle("-fx-font-weight: bolder; -fx-font-size: 20px;");

			HBox primaryDeckBox = new HBox(-50);
			primaryDeckBox.setAlignment(Pos.CENTER);
			playerDeckContainers.put(player.nick, primaryDeckBox);

			VBox allDecksBox = new VBox(10);
			allDecksBox.setAlignment(Pos.CENTER);
			allDecksBox.getChildren().add(primaryDeckBox);

			if (player.hasSplit) {
				HBox splitDeckBox = new HBox(-40);
				splitDeckBox.setAlignment(Pos.CENTER);
				playerDeckContainers.put(player.nick + "_split", splitDeckBox);
				allDecksBox.getChildren().add(splitDeckBox);
			}

			// Assemble Player UI
			playerBox.getChildren().addAll(allDecksBox, nameLabel);
			playersContainer.getChildren().add(playerBox);
		}

		// 2. Build the Dealer
		Player dealer = game.getAllPlayers()[game.getNumPlayers()];

		Label dealerNameLabel = new Label(dealer.nick);
		dealerNameLabel.setStyle("-fx-font-weight: bolder; -fx-font-size: 20px;");

		HBox dealerDeckBox = new HBox(-50);
		dealerDeckBox.setAlignment(Pos.CENTER);
		playerDeckContainers.put("Dealer", dealerDeckBox);

		VBox dealerVBox = new VBox(10);
		dealerVBox.setAlignment(Pos.TOP_CENTER);

		// Assemble Dealer UI
		dealerVBox.getChildren().addAll(dealerNameLabel, dealerDeckBox);

		if (dealerContainer != null) {
			dealerContainer.setAlignment(Pos.TOP_CENTER);
			dealerContainer.getChildren().clear();
			dealerContainer.getChildren().add(dealerVBox);
		}

		game.setPlayerDeckContainers(playerDeckContainers);
		updateCardsOnScreen();
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

	public void updateCardsOnScreen() {
		HashMap<String, HBox> containers = game.getPlayerDeckContainers();

		for (Player player : game.getAllPlayers()) {

			// 1. Update Primary Deck
			HBox primaryBox = containers.get(player.nick);

			if (primaryBox != null) {
				renderDeckToHBox(player.deck, primaryBox);
			}

			// 2. Update Split Deck
			if (player.hasSplit) {
				HBox splitBox = containers.get(player.nick + "_split");

				if (splitBox != null) {
					renderDeckToHBox(player.splitDeck, splitBox);
				}
			}
		}
	}

	private void renderDeckToHBox(ArrayList<String> deck, HBox container) {
		container.getChildren().clear();

		for (String cardName : deck) {

			String cleanName = cardName;

			if (cleanName.contains("ace")) {
				cleanName = "Ace";
			} else {

				Boolean nameContainsNumber = false;

				try {
					Integer.parseInt(cleanName);
					nameContainsNumber = true;

				} catch (Exception e) {
				}

				if (!nameContainsNumber) {
					cleanName = cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1);
				}
			}
			String imagePath = "/All Cards v.1/" + cleanName + ".png";

			try {
				Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
				ImageView imageView = new ImageView(cardImage);

				imageView.setFitHeight(160.0);
				imageView.setPreserveRatio(true);
				container.getChildren().add(imageView);

			} catch (Exception e) {
				System.out.println("Could not load: " + imagePath); // Debugging safety net
			}
		}
	}
}
