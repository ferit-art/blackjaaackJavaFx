package application;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

import javafx.scene.layout.HBox;

public class Game {

	private HashMap<String, HBox> playerDeckContainers = new HashMap<>(); // Image containers for the players
	private Player[] allPlayers;
	private int currentPlayerIndex = 1; // The actual game has not started yet
	private int numPlayers = 1; // Min amount of players is 1
	private Player lastHumanPlayer;
	private boolean gameOver = false;
	private boolean consoleIsVisible = true;

	public int getNumPlayers() {
		return numPlayers;
	}

	public void setNumPlayers(int numPlayers) {
		this.numPlayers = numPlayers;
	}

	public Player[] getAllPlayers() {
		return allPlayers;
	}

	public void setAllPlayers(Player[] allPlayers) {
		this.allPlayers = allPlayers;
	}

	public int getCurrentPlayerIndex() {
		return currentPlayerIndex;
	}

	public void setCurrentPlayerIndex(int currentPlayerIndex) {
		this.currentPlayerIndex = currentPlayerIndex;
	}

	public static void saveBets(Player[] allPlayers) {

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

	public static void loadBets(Player[] allPlayers, Controller ctrl) {

		try {
			Scanner fileScanner = new Scanner(new File("player_bets.txt"));

			while (fileScanner.hasNextLine()) {

				String line = fileScanner.nextLine();
				String[] parts = line.split(":");
				String playerName = parts[0];

				double bet = Double.parseDouble(parts[1]);

				for (Player p : allPlayers) {
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

	public Player getLastHumanPlayer() {
		return lastHumanPlayer;
	}

	public void setLastHumanPlayer(Player lastHumanPlayer) {
		this.lastHumanPlayer = lastHumanPlayer;
	}

	public HashMap<String, HBox> getPlayerDeckContainers() {
		return playerDeckContainers;
	}

	public void setPlayerDeckContainers(HashMap<String, HBox> playerDeckContainers) {
		this.playerDeckContainers = playerDeckContainers;
	}

	public Boolean getGameOver() {
		return gameOver;
	}

	public void setGameOver(Boolean gameOver) {
		this.gameOver = gameOver;
	}

	public boolean isConsoleIsVisible() {
		return consoleIsVisible;
	}

	public void setConsoleIsVisible(boolean consoleIsVisible) {
		this.consoleIsVisible = consoleIsVisible;
	}
}