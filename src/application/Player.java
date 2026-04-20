package application;

import java.util.ArrayList;

public class Player {
	public ArrayList<String> deck = new ArrayList<String>();
	public ArrayList<String> splitDeck = new ArrayList<String>();

	//	Normal variables per player
	public int bet;
	public int score;

	public String nick;
	public String card;
	public String splitCard;

	public boolean splitTurn = false; //	Determines if the next player action is for the split deck or the primary.
	public boolean hasStood = false;
	public boolean splitHasStood = false;
	public boolean hasSplit = false;
	public boolean hasWon = false;
	public boolean hasDoubledDown = false;
	public boolean splitHasDoubledDown = false;

	//	Split-variables per player
	public int splitBet;
	public int splitScore;
}
