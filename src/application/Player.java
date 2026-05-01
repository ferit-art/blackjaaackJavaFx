package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Player {
	public ArrayList<String> deck = new ArrayList<String>();
	public ArrayList<String> splitDeck = new ArrayList<String>();

	// Normal variables per player
	public double bet;
	public int score;

	public String nick;
	public String card;

	public boolean hasStood = false;
	public boolean hasDoubledDown = false;
	public boolean splitHasDoubledDown = false;

	// Split-variables per player
	public double splitBet;
	public int splitScore;
	public String splitCard;
	public boolean hasSplit = false;
	public boolean splitHasStood = false;
	public boolean splitTurn = false; // Determines if the next player action is for the split deck or the primary.
	public static boolean betsSaved = false;
	
	public static void saveBets() {
		
		betsSaved = true;
		Player[] allPlayers = Controller.game.getAllPlayers();
		
		try {
			FileWriter writer = new FileWriter("player_bets.txt");
			
			for (Player player : allPlayers) {
				
				if (!player.nick.equals("Dealer")) {
					writer.write(player.nick + ":" + player.bet + "\n");
				}
			}
			writer.close();
			
		} catch (IOException e) {
			System.out.println("\n" + "Could not save bets.");
		}
	}

	public static void loadBets() {

		Player[] allPlayers = Controller.game.getAllPlayers();

		try {
			Scanner fileScanner = new Scanner(new File("player_bets.txt"));

			while (fileScanner.hasNextLine()) {

				String line = fileScanner.nextLine();
				String[] parts = line.split(":");
				String playerName = parts[0];

				int bet = Integer.parseInt(parts[1]);

				for (Player player : allPlayers) {
					if (player.nick.equals(playerName)) {
						player.bet = bet;
						break;
					}
				}
			}
			fileScanner.close();

		} catch (FileNotFoundException e) {
			System.out.println("\n" + "Could not load bets.");
		}
	}

}
