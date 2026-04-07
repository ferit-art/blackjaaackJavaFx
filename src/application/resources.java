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

	public static void hit(Players player, ArrayList<String> deck) {

		if (!player.hasStood) {

			Random random0 = new Random();
			player.card = allCards.get(random0.nextInt(allCards.size()));
		}
		if (player.hasSplit && !player.splitHasStood) {
			Random random1 = new Random();
			player.splitCard = allCards.get(random1.nextInt(allCards.size()));
		}
		addCard(player, deck);
	}

	public static void stand(Players player, int score) {

		if (player.nick.equals("Dealer")) {
			player.hasStood = true;
			System.out.println("\n" + player.nick + " stands with " + score + " as the total value");

		} else {
			if (player.splitChoice) {
				player.splitHasStood = true;
				System.out.println(
						"\n" + player.nick + " stands with " + score + " as the total value for the split hand");
			} else {
				player.hasStood = true;
				System.out.println("\n" + player.nick + " stands with " + score + " as the total value");
			}
		}
	}

	public static boolean doubleDown(Players player) {
		if (player.originalBet == 0) {
			player.originalBet = player.bet; // Fallback if originalBet wasn't set properly
		}
		player.bet = player.originalBet * 2;
		player.hasDoubledDown = true;
		System.out.println(player.nick + "'s bet now is " + player.bet);
		hit(player, player.deck);
		player.hasStood = true; // Set directly instead of calling stand
		return true;
	}

	public static void split(Players player) {
		if (splitAble(player) == true) {

			player.hasSplit = true;
			player.score = player.score - Integer.parseInt(player.splitCard);
			player.splitScore = player.splitScore + Integer.parseInt(player.splitCard);
			player.deck.remove(player.card);
			player.splitDeck.add(player.splitCard);

			hit(player, player.deck);
			hit(player, player.splitDeck);

		} else {
			System.out.println("Wrong choice buddy, choose again and wisely.");
		}
	}

	// Extra

	public static void choice(Players player, ArrayList<String> deck, boolean hasStood, int score, Scanner scanner) {
		System.out.println("\nWhat now " + player.nick + " ?\n"
				+ "You can hit, stand, double down or split with your hand/split-hand");

		if (deck == player.splitDeck) {
			player.splitChoice = true;
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
			} else if (player.splitChoice) {
				stand(player, score);
			}
			break;
		case "double":
			if (!player.hasStood) {
				doubleDown(player);
			}
			break;
		case "split":
			if (!player.hasStood) {
				split(player);
			}
			break;
		default:
			System.out.println("Invalid action!");
		}
	}

	public static boolean splitAble(Players player) {
		int count = 0;

		for (int i = 0; i < player.deck.size(); i++) {
			if (player.deck.get(i).equals(player.card)) {
				count++;
			}
		}
		if (count >= 2) {
			player.splitCard = player.card;
			return true;
		} else {
			return false;
		}
	}

	public static void addCard(Players player, ArrayList<String> deck) {
		deck.add(player.card);

		if (resources.sCards.containsKey(player.card) == true) {
			player.score = player.score + resources.sCards.get(player.card);
		} else {
			player.score = player.score + Integer.parseInt(player.card);
		}

		if (player.hasSplit) {
			deck.add(player.splitCard);

			if (resources.sCards.containsKey(player.card) == true) {
				player.splitScore = player.splitScore + resources.sCards.get(player.splitCard);
			} else {
				player.splitScore = player.splitScore + Integer.parseInt(player.splitCard);
			}
		}
	}

	public static ArrayList<Players> theWinner(Players[] allPlayers) {
		ArrayList<Players> winners = new ArrayList<>();

		int bestScore = 0;

		// Find best score that's not bust
		for (Players p : allPlayers) {
			if (p.score <= 21 && p.score > bestScore) {
				bestScore = p.score;
			}
		}

		for (Players p : allPlayers) {
			if (p.score == bestScore && p.score <= 21) {
				winners.add(p);
			}
		}

		return winners; // Could be empty, 1 player, or multiple
	}
}