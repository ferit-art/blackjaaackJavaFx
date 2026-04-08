package application;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Players {
	public static Player[] allPlayers = new Player[0]; // +1 for the dealer
	
	public static int numPlayers = 1; 
	public static int currentPlayer = 0;

	public int getCurrentPlayer() {
		return currentPlayer;
	}
	
	public void setCurrentPlayer(int currentPlayer) {
		this.currentPlayer = currentPlayer;
	}
	
	public int getNumPlayers() {
		return numPlayers;
	}

	public void setNumPlayers(int numPlayers) {
		this.numPlayers = numPlayers;
	}

	public static void saveBets(Players[] allPlayers) {
		
		try {
			new File("player_bets.txt").createNewFile();
			FileWriter writer = new FileWriter("player_bets.txt");
			for (Player p : allPlayers) {
				if (!p.nick.equals("Dealer")) {
					writer.write(p.nick + ":" + p.bet + "\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("\nCould not save bets.");
		}
	}

	public static void loadBets(Players[] allPlayers, Controller ctrl) {

		try {
			Scanner fileScanner = new Scanner(new File("player_bets.txt"));

			while (fileScanner.hasNextLine()) {

				String line = fileScanner.nextLine();
				String[] parts = line.split(":");
				String playerName = parts[0];

				int bet = Integer.parseInt(parts[1]);

				for (Players p : allPlayers) {
					if (p.nick.equals(playerName)) {
						p.bet = bet;
						break;
					}
				}
			}
			ctrl.betLabel.setText("Previous bets loaded.");
			fileScanner.close();

		} catch (FileNotFoundException e) {
		}
	}

	public static void Game() {
		Scanner scanner = new Scanner(System.in);

		System.out.println("\033[3mWelcome to the card game called 'Blackjaaack' \033[0m\n\n"
				+ "Note: The actual Ace is divided into two seperate cards,\n" + "lesser ace and greater ace.\n"
				+ "It equals 1 and 11.");

		System.out.print("\nHow many players? ");
		int numPlayers = scanner.nextInt();

		Players[] allPlayers = new Players[numPlayers + 1]; // +1 for the dealer

		for (int i = 0; i < numPlayers; i++) {
			allPlayers[i] = new Players();
			allPlayers[i].nick = "Player " + (i + 1);
		}

		allPlayers[numPlayers] = new Players();
		allPlayers[numPlayers].nick = "Dealer";

		resources.setup();

		for (Players p : allPlayers) {

			if (!p.nick.equals("Dealer")) {

				if (p.bet != 0) {

					System.out.println("Previous bet loaded for " + p.nick);
					p.originalBet = p.bet;
				} else {

					System.out.println("How much is your bet " + p.nick + "?");
					p.originalBet = scanner.nextInt();
					p.bet = p.originalBet;
				}
			}
		}

		for (int i = 0; i < allPlayers.length; i++) {
			resources.hit(allPlayers[i], allPlayers[i].deck);
		}

		boolean gameActive = true;

		while (gameActive) { // The game loop
			gameActive = false;

			for (int i = 0; i < allPlayers.length; i++) { // Reset flags
				allPlayers[i].choice = false;
				allPlayers[i].splitChoice = false;
			}

			for (int i = 0; i < allPlayers.length; i++) { // The core

				if (allPlayers[i].score >= 21 && !allPlayers[i].hasStood) { // Guaranteed win (21)

					allPlayers[i].choice = true; // The original deck
					resources.stand(allPlayers[i], allPlayers[i].score);
					if (allPlayers[i].score > 21) { // Bust

						System.out.println(allPlayers[i].nick + "'s busted.");
					}

				} else {

					if (allPlayers[i].nick.equals("Dealer")) { // Dealer actions

						if (allPlayers[i].hasStood == false && allPlayers[i].score >= 17) { // Dealer stands on 17 or
																							// higher, up to 21
							allPlayers[i].choice = true; // The original deck
							resources.stand(allPlayers[i], allPlayers[i].score);

						} else if (allPlayers[i].hasStood == false) { // Dealer otherwise continues to hit

							resources.hit(allPlayers[i], allPlayers[i].deck);
						}
					} else if (allPlayers[i].hasStood == false) { // If not Dealer

						gameActive = true;
						if (allPlayers[i].hasSplit == true && !allPlayers[i].splitHasStood) { // Incase of a split deck
							resources.choice(allPlayers[i], allPlayers[i].splitDeck, allPlayers[i].splitHasStood,
									allPlayers[i].splitScore, scanner);
						} else if (!allPlayers[i].hasStood) {
							resources.choice(allPlayers[i], allPlayers[i].deck, allPlayers[i].hasStood,
									allPlayers[i].score, scanner);
						}
					}
				}
			} // core

			if (!gameActive) { // The results

				ArrayList<Players> winners = resources.theWinner(allPlayers);

				if (winners.isEmpty()) {

					System.out.println("\nNo winners - everyone busted!");

				} else if (winners.size() == 1) {

					System.out.println("\nThe winner is: " + winners.get(0).nick);
					winners.get(0).hasWon = true;

				} else {

					System.out.println("\nIt is tied between:");
					for (Players player : winners) {

						player.hasWon = true;
						System.out.println("- " + player.nick);
					}
				}
			}
		} // game

		for (Players p : allPlayers) { // Consequences
			if (!p.nick.equals("Dealer")) {
				if (p.hasWon) {
					if (p.hasDoubledDown) {
						System.out.println(p.nick + " won with doubled down bet!");
						p.bet = p.originalBet * 4;
					} else {
						p.bet = p.bet * 2; // Double the winnings for regular wins
					}
				} else {
					if (p.hasDoubledDown) {
						System.out.println(p.nick + " lost their doubled down bet of " + p.bet);
						p.bet = 1;
					} else {
						System.out.println(p.nick + " lost their bet of " + p.originalBet);
						p.bet = 1;
					}
				}
			}
		}

		for (Players p : allPlayers) { // The change in bet
			if (!p.nick.equals("Dealer")) {
				System.out.println("\n" + p.nick + "'s bet now is: " + p.bet);
			}
		}

		System.out.print("Keep the bets? (y/n): "); // If the players want to save their bets for future rounds
		String answer = scanner.next();

		if (answer.equalsIgnoreCase("y")) {

			saveBets(allPlayers);
		} else {

			for (Players p : allPlayers) {

				p.bet = 0;
			}
			saveBets(allPlayers);
		}
		scanner.close();
	}
}
