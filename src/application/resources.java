package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class resources {

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

	public static void stand(Player player, int score) {

		if (player.nick.equals("Dealer")) {
			player.hasStood = true;

		} else {
			if (player.splitTurn) {
				player.splitHasStood = true;
				System.out.println(
						"\n" + player.nick + " stands with " + score + " as the total value for the split hand");
			} else {
				player.hasStood = true;
				System.out.println("\n" + player.nick + " stands with " + score + " as the total value");
			}
		}
	}

	public static void doubleDown(Player player) {
		
		player.bet = player.bet* 2;
		player.hasDoubledDown = true;
		
		hit(player, player.deck);
		player.hasStood = true;
	}

	public static void split(Player player) {
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

	// choice method is now implemented inside of the controller file
	// depending on the action taken.

	public static void choice(Player player, ArrayList<String> deck, boolean hasStood, int score, Scanner scanner) {
		System.out.println("\nWhat now " + player.nick + " ?\n"
				+ "You can hit, stand, double down or split with your hand/split-hand");

		if (deck == player.splitDeck) {
			player.splitTurn = true;
		} else if (deck == player.deck) {
			player.choice = true;
		}

		String action = scanner.next();
		scanner.nextLine();
		switch (action.toLowerCase()) {
		case "hit":
			hit(player, deck);
			break;
		case "stand":
			if (player.choice) {
				stand(player, score);
			} else if (player.splitTurn) {
				stand(player, score);
			}
			break;
		case "double":
			if (!player.hasStood) {
				doubleDown(player);
			}
			break;
		case "split":
			if (!player.hasStood && !player.hasSplit) {
				split(player);
			}
			break;
		default:
			System.out.println("Invalid action!");
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
		if (resources.sCards.containsKey(drawnCard)) {
			cardValue = resources.sCards.get(drawnCard);
		} else {
			cardValue = Integer.parseInt(drawnCard);
		}

		if (deck == player.splitDeck) {
			player.splitScore = player.splitScore + cardValue;
		} else {
			player.score = player.score + cardValue;
		}

		/*
		 * deck.add(player.card);
		 * 
		 * if (resources.sCards.containsKey(player.card) == true) { player.score =
		 * player.score + resources.sCards.get(player.card); } else { player.score =
		 * player.score + Integer.parseInt(player.card); }
		 * 
		 * if (player.hasSplit) { deck.add(player.splitCard);
		 * 
		 * if (resources.sCards.containsKey(player.card) == true) { player.splitScore =
		 * player.splitScore + resources.sCards.get(player.splitCard); } else {
		 * player.splitScore = player.splitScore + Integer.parseInt(player.splitCard); }
		 * }
		 */
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