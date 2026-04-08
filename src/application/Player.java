package application;

import java.util.ArrayList;

public class Player {
	public ArrayList<String> deck = new ArrayList<String>();
	public ArrayList<String> splitDeck = new ArrayList<String>();
	
	public int bet;
	public int score;
	public int splitScore;
	public int originalBet = 0;

	public String nick;
	public String card;
	public String splitCard;

	public boolean splitChoice = false;
	public boolean choice = false;
	public boolean hasStood = false;
	public boolean splitHasStood = false;
	public boolean hasSplit = false;
	public boolean hasWon = false;
	public boolean hasDoubledDown = false;
}
