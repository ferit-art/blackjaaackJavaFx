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

	public static void hit(players someone, ArrayList<String> deck) {

		if (!someone.hasStood) {

			Random random0 = new Random();
			someone.card = allCards.get(random0.nextInt(allCards.size()));
		}
		if (someone.hasSplit && !someone.splitHasStood) {
			Random random1 = new Random();
			someone.splitCard = allCards.get(random1.nextInt(allCards.size()));
		}
		addCard(someone, deck);
	}

	public static void stand(players someone, int score) {

		if (someone.nick.equals("Dealer")) {
			someone.hasStood = true;
			System.out.println("\n" + someone.nick + " stands with " + score + " as the total value");

		} else {
			if (someone.splitChoice) {
				someone.splitHasStood = true;
				System.out.println(
						"\n" + someone.nick + " stands with " + score + " as the total value for the split hand");
			} else {
				someone.hasStood = true;
				System.out.println("\n" + someone.nick + " stands with " + score + " as the total value");
			}
		}
	}

	public static boolean doubleDown(players someone) {
		if (someone.originalBet == 0) {
			someone.originalBet = someone.bet; // Fallback if originalBet wasn't set properly
		}
		someone.bet = someone.originalBet * 2;
		someone.hasDoubledDown = true;
		System.out.println(someone.nick + "'s bet now is " + someone.bet);
		hit(someone, someone.deck);
		someone.hasStood = true; // Set directly instead of calling stand
		return true;
	}

	public static void split(players someone) {
		if (splitAble(someone) == true) {

			someone.hasSplit = true;
			someone.score = someone.score - Integer.parseInt(someone.splitCard);
			someone.splitScore = someone.splitScore + Integer.parseInt(someone.splitCard);
			someone.deck.remove(someone.card);
			someone.splitDeck.add(someone.splitCard);

			hit(someone, someone.deck);
			hit(someone, someone.splitDeck);

		} else {
			System.out.println("Wrong choice buddy, choose again and wisely.");
		}
	}

	// Extra

	public static void choice(players someone, ArrayList<String> deck, boolean hasStood, int score, Scanner scanner) {
		System.out.println("\nWhat now " + someone.nick + " ?\n"
				+ "You can hit, stand, double down or split with your hand/split-hand");

		if (deck == someone.splitDeck) {
			someone.splitChoice = true;
		} else if (deck == someone.deck) {
			someone.choice = true;
		}

		String action = scanner.next();
		scanner.nextLine();
		switch (action.toLowerCase()) {
		case "hit":
			hit(someone, deck);
			break;
		case "stand":
			if (someone.choice) {
				stand(someone, score);
			} else if (someone.splitChoice) {
				stand(someone, score);
			}
			break;
		case "double":
			if (!someone.hasStood) {
				doubleDown(someone);
			}
			break;
		case "split":
			if (!someone.hasStood) {
				split(someone);
			}
			break;
		default:
			System.out.println("Invalid action!");
		}
	}

	public static boolean splitAble(players someone) {
		int count = 0;

		for (int i = 0; i < someone.deck.size(); i++) {
			if (someone.deck.get(i).equals(someone.card)) {
				count++;
			}
		}
		if (count >= 2) {
			someone.splitCard = someone.card;
			return true;
		} else {
			return false;
		}
	}

	public static void addCard(players someone, ArrayList<String> deck) {
		deck.add(someone.card);

		if (resources.sCards.containsKey(someone.card) == true) {
			someone.score = someone.score + resources.sCards.get(someone.card);
		} else {
			someone.score = someone.score + Integer.parseInt(someone.card);
		}

		if (someone.hasSplit) {
			deck.add(someone.splitCard);

			if (resources.sCards.containsKey(someone.card) == true) {
				someone.splitScore = someone.splitScore + resources.sCards.get(someone.splitCard);
			} else {
				someone.splitScore = someone.splitScore + Integer.parseInt(someone.splitCard);
			}
		}
		players.printInfo(someone);
	}

	public static ArrayList<players> theWinner(players[] allPlayers) {
		ArrayList<players> winners = new ArrayList<>();

		int bestScore = 0;

		// Find best score that's not bust
		for (players p : allPlayers) {
			if (p.score <= 21 && p.score > bestScore) {
				bestScore = p.score;
			}
		}

		for (players p : allPlayers) {
			if (p.score == bestScore && p.score <= 21) {
				winners.add(p);
			}
		}

		return winners; // Could be empty, 1 player, or multiple
	}
}