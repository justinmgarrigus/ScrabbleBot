import java.awt.Color;
import java.awt.Font;

public class Runner 
{
	/**
	 * How many letters anyone can have in their 
	 * rack at one time. 
	 */
	public static final int MAX_LETTERS_IN_RACK = 7; 
	
	/**
	 * Each subscript value. To access subscript
	 * 1, call Runner.SUB[1]; 
	 */
	public static final String[] SUB = 
		{
			'\u2080' + "",
			'\u2081' + "",
			'\u2082' + "",
			'\u2083' + "",
			'\u2084' + "",
			'\u2085' + "",
			'\u2086' + "",
			'\u2087' + "",
			'\u2088' + "",
			'\u2089' + "",
			'\u2081' + "" + '\u2080' 
		};
	
	public static final Color 
		DOUBLE_LETTER_COLOR = new Color(209, 240, 255),
		TRIPLE_LETTER_COLOR = new Color(0, 174, 255), 
		DOUBLE_WORD_COLOR = new Color(255, 205, 201), 
		TRIPLE_WORD_COLOR = new Color(255, 107, 97), 
		PIECE_COLOR = new Color(230, 200, 100),
		START_COLOR = Color.YELLOW,
		DEFAULT_SPACE_COLOR = new Color(238, 238, 238);
	
	public static final int 
		DOUBLE_LETTER = 1, 
		TRIPLE_LETTER = 2, 
		DOUBLE_WORD = 3, 
		TRIPLE_WORD = 4, 
		START = 5; 
	
	public static final char[] ALPHABET = 
		{
			'A','B','C','D','E','F',
			'G','H','I','J','K','L',
			'M','N','O','P','Q','R',
			'S','T','U','V','W','X',
			'Y','Z'
		};
	
	public static final char EMPTY = '_';
	
	public static final Font 
		LETTER_FONT = new Font("Dialog", Font.PLAIN, 26); 
	
	public static final String BOARD_FILE_NAME
//			= "Default Board.txt";
//			= "Shrek Board.txt";
			= "Lightning Round Board.txt";
//			= "Words With Friends Board.txt"; 
	
	public static final String DICTIONARY_FILE_NAME 
			= "Collins Scrabble Words (2019).txt";
//			= "Google 20k Words.txt"; 
	
	public static final String LETTER_POINTS_FILE_NAME
//			= "Letter Points.txt"; 
			= "Words With Friends Letter Points.txt"; 
	
	public static void main(String[] args) 
	{
		new Frame(); 
	}
}
