package application;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Resources {

	// Lists

	public static HashMap<String, Integer> sCards = new HashMap<>();
	public static ArrayList<String> allCards = new ArrayList<String>();

	// Cards

	public static void setup() {
		sCards.put("lesser ace", 1);
		sCards.put("jack", 10);
		sCards.put("queen", 10);
		sCards.put("king", 10);
		sCards.put("greater ace", 11);

		allCards.add("lesser ace");
		allCards.add("2");
		allCards.add("3");
		allCards.add("4");
		allCards.add("5");
		allCards.add("6");
		allCards.add("7");
		allCards.add("8");
		allCards.add("9");
		allCards.add("10");
		allCards.add("jack");
		allCards.add("queen");
		allCards.add("king");
		allCards.add("greater ace");
	}

	// Actions

	public static void hit(Player player, ArrayList<String> deck) {

		Random random = new Random();
		String drawnCard = allCards.get(random.nextInt(allCards.size()));

		if (deck == player.splitDeck) {
			player.splitCard = drawnCard;
		} else {
			player.card = drawnCard;
		}
		addCard(player, deck, drawnCard);
	}

	public static void stand(Player player, ArrayList<String> deck) {

		if (deck != player.splitDeck) {

			player.hasStood = true;
			player.splitTurn = false;
		} else {
			player.splitHasStood = true;
		}
	}

	public static void doubleDown(Player player) {

		if (!player.splitTurn) {

			player.bet = player.bet * 2;
			player.hasDoubledDown = true;

			hit(player, player.deck);
			player.hasStood = true;

		} else {
			player.splitBet = player.bet * 2;
			player.splitHasDoubledDown = true;
			player.splitTurn = false;

			hit(player, player.splitDeck);
			player.splitHasStood = true;
		}
	}

	public static void split(Player player) {

		player.splitBet = player.bet;
		player.hasSplit = true;

		int splitCardValue;

		if (sCards.containsKey(player.splitCard) == true) {
			splitCardValue = sCards.get(player.splitCard);
		} else {
			splitCardValue = Integer.parseInt(player.splitCard);
		}

		player.score = player.score - splitCardValue;
		player.splitScore = player.splitScore + splitCardValue;
		player.deck.remove(player.card);
		player.splitDeck.add(player.splitCard);

		hit(player, player.deck);
		hit(player, player.splitDeck);
	}

	// Extra

	public static String scoreChecks(Player player, ArrayList<String> deck) {

		String bust = "Bust";
		String blackJ = "Blackjack";
		String error = "Problem with scoreCheck";

		if (deck == player.deck) { // For everyone with the primary deck sent

			if (player.nick == "Dealer") { // For the dealer that should stand at 17 or higher.

				if (player.score >= 17) {

					String dealerStand = "17 or greater as score";
					return dealerStand;
				}
			} else if (player.score > 21) {

				return bust;
			} else if (player.score == 21) {

				return blackJ;
			}
			return "Active";
			
		} else if (player.hasSplit && deck == player.splitDeck) { // For players with split hands and the split deck
																	// sent
			if (player.splitScore > 21) {

				return bust;
			} else if (player.splitScore == 21) {

				return blackJ;
			}
			return "Active";
		}
		return error;
	}

	public static void resetGame() {
		for (int i = 0; i < Controller.game.getNumPlayers(); i++) {
			Player p = Controller.game.getAllPlayers()[i];

			if (Controller.game.getAllPlayers()[i] != null) {
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
			}
		}

		Controller.game.setAllPlayers(new Player[0]);
		Controller.game.setNumPlayers(1); // Because the min amount of players is 1
		Controller.game.setCurrentPlayerIndex(-1); // The player amount is not given in the start => game hasn't started
													// yet

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

	public static boolean splitAble(Player player) {

		if (player.hasSplit || player.hasStood) {
			return false;
		}

		String firstCard = null;
		String secondCard = null;

		for (int i = 0; i < player.deck.size(); i++) {
			firstCard = player.deck.get(i);

			for (int j = i + 1; j < player.deck.size(); j++) {
				secondCard = player.deck.get(j);

				if (firstCard.equals(secondCard)) {
					player.splitCard = firstCard;
					player.card = player.splitCard;
					return true;
				}
			}
		}
		return false;
	}

	public static void addCard(Player player, ArrayList<String> deck, String drawnCard) {

		deck.add(drawnCard);

		int cardValue;
		if (Resources.sCards.containsKey(drawnCard)) {
			cardValue = Resources.sCards.get(drawnCard);
		} else {
			cardValue = Integer.parseInt(drawnCard);
		}

		if (deck == player.splitDeck) {
			player.splitScore = player.splitScore + cardValue;
		} else {
			player.score = player.score + cardValue;
		}
	}

	public static ArrayList<Player> theWinner(Player[] allPlayers) {
		ArrayList<Player> winners = new ArrayList<>();

		int bestScore = 0;

		// Find best score that's not bust
		for (Player p : allPlayers) {
			if (p.score <= 21 && p.score > bestScore) {
				bestScore = p.score;
			}
		}

		for (Player p : allPlayers) {
			if (p.score == bestScore && p.score <= 21) {
				winners.add(p);
			}
		}

		return winners; // Could be empty or 1 or multiple players
	}
}