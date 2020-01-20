import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * The area the letters on the board are kept. 
 */
public class Board extends JPanel implements FocusListener
{
	private static final Border BLANK_BORDER = 
			new LineBorder(Color.BLACK, 0); 
	
	/**
	 * The Frame that's trying to use this panel. 
	 */
	private Frame source;
	
	/**
	 * Map between each letter and how many points they
	 * are worth. 
	 */
	private HashMap<String, Integer> letterPoint; 
	
	private Human human; 
	
	/**
	 * How many turns have passed. *One turn is not a 
	 * rotation between two players, but rather a single
	 * player finishing their turn*. Two players putting
	 * down one word each would equal two turns. 
	 */
	private int turnNumber; 
	
	/**
	 * All of the letters to display. 
	 */
	private JTextField[][] spaces; 
	
	/**
	 * The data contained in the spaces JTextField[][] array.
	 */
	private char[][] letters; 
	
	/**
	 * Coordinates with the spaces JTextField[][]. 
	 * spaces[0][0] coordinates with points[0][0]
	 * for how much points that space is worth. 
	 */
	private int[][] scoreArray; 
	
	/**
	 * Array keeping track of the modifiers currently
	 * available on the board. spaces[0][0] coordinates
	 * with modifierArray[0][0] for *modifiers that 
	 * the player can still receive.*
	 */
	private int[][] modifierArray; 
	
	/**
	 * A way we can input a JTextField and get the field's
	 * x,y location as a result. 
	 */
	private HashMap<JTextField, Point> spaceLocations; 
	
	/**
	 * A list of spaces the human character typed in. 
	 * Once they submit their turn, these spaces will
	 * be reformatted. 
	 */
	private HashSet<JTextField> interactedSpaces; 
	
	/**
	 * These are spaces that a player has interacted
	 * with and submitted to the game, and can't be
	 * changed later on. 
	 */
	private HashSet<JTextField> submittedSpaces;
	
	/**
	 * Anchor spaces are spaces adjacent to submitted 
	 * spaces. The robot will place their first letter
	 * here.  
	 */
	private HashSet<Point> anchorSpaces; 
	
	/**
	 * Spaces the player/robot has put letters on, that
	 * can't be changed later on. 
	 */
	private HashSet<Point> submittedLocations; 

	/**
	 * The location of the last JTextField the user 
	 * edited. 
	 */
	private Point selectedTextFieldLocation; 
	
	/**
	 * If the robot places a letter somewhere where there's 
	 * already a letter on it, then we keep track of what
	 * that letter is. 
	 */
	private HashMap<Point, Character> robotTempStorage; 
	
	private Point midPoint; 
	
	private Board(Frame source, Human human)
	{
		setBorder(BorderFactory.createRaisedBevelBorder());
		
		this.source = source;
		this.human = human; 
		
		letterPoint = readLetterPointsFile(); 
		turnNumber = 0; 
		submittedSpaces = new HashSet<>(); 
		spaceLocations = new HashMap<>(); 
		submittedLocations = new HashSet<>(); 
		anchorSpaces = new HashSet<>(); 
		robotTempStorage = new HashMap<>(); 
		interactedSpaces = new HashSet<>(); 
	}
	
	/**
	 * Creates a new board given the size it will be. 
	 * @param source The Frame that's trying to use this
	 * panel. 
	 * @param width How many letters wide. 
	 * @param height How many letters tall. 
	 */
	public Board(Frame source, Human human, int width, int height)
	{
		this(source, human); 
		
		// We are going to have each row be its own
		// JPanel. We will add each of those JPanels
		// to this central Board JPanel. 
		spaces = new JTextField[width][height]; 
		letters = new char[width][height];
		scoreArray = new int[width][height]; 
		modifierArray = new int[width][height]; 
		
		setLayout(new GridLayout(height, width)); 
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				spaces[x][y] = createDefaultTextField(); 
				
				spaceLocations.put(spaces[x][y], new Point(x,y)); 
				add(spaces[x][y]);
			} 
		}
		
		enableDocumentListener(true); 
	}
	
	/**
	 * Creates a new board based on the data given. The
	 * data can contain DL, DW, TL, and TW score spaces. 
	 * @param source The Frame that's trying to use this
	 * panel. 
	 * @param data The pre-made board with space modifiers. 
	 * @throws Exception 
	 */
	public Board(Frame source, Human human, File data)
	{
		this(source, human); 
		
		Scanner scanner = null; 
		try 
		{
			scanner = new Scanner(data); 
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
		
		// _ represents a playable empty space, 
		// ~ represents a non-playable empty space,
		// DL/DW/TL/TW represents a modifier. 
		// First two items in file are width/height. 
		spaces = new JTextField
				[scanner.nextInt()][scanner.nextInt()]; 
		letters = new char[spaces.length][spaces[0].length];
		scoreArray = new int[spaces.length][spaces[0].length]; 
		modifierArray = new int[spaces.length][spaces[0].length]; 
		
		setLayout(new GridLayout(spaces[0].length, spaces.length)); 
		
		for (int y = 0; y < spaces[0].length; y++)
		{
			for (int x = 0; x < spaces.length; x++)
			{
				spaces[x][y] = createDefaultTextField(); 
				configureTextField(x, y, scanner.next());
				
				add(spaces[x][y]); 
				spaceLocations.put(spaces[x][y], new Point(x,y)); 
			}
		}
		
		enableDocumentListener(true); 
		
		scanner.close(); 
	}
	
	/**
	 * The human that will be keeping track of the documentListener. 
	 */
	private JTextField createDefaultTextField()
	{
		JTextField field = new JTextField(3);
		field.getDocument()
				.putProperty("owner", field);
		field.setEditable(false); 
		field.setHorizontalAlignment(
				JTextField.CENTER);
		field.setBorder(
				BorderFactory.createRaisedBevelBorder());
		field.setFont(Runner.LETTER_FONT);
		field.addFocusListener(this);
		
		configureActions(field); 
		
		return field; 
	}
	
	/**
	 * Whether or not the human should listen to DocumentListener
	 * inputs. This should be true on their turn, false on the 
	 * Robot's turn.
	 */
	private void enableDocumentListener(boolean enable)
	{
		for (int x = 0; x < spaces.length; x++)
		{
			for (int y = 0; y < spaces[x].length; y++)
			{
				if (enable) 
					spaces[x][y].getDocument().addDocumentListener(human);
				else
					spaces[x][y].getDocument().removeDocumentListener(human);
			}
		}
	}
	
	private void configureActions(JTextField field)
	{
		Action up = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				arrowKeysPressed(0, -1);
			}
		};
		Action down = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				arrowKeysPressed(0, 1);
			}
		};
		Action left = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				arrowKeysPressed(-1, 0);
			}
		};
		Action right = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				arrowKeysPressed(1, 0);
			}
		};
		Action enter = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				source.enterKeyPressed();
			}
		};
		
		field.getInputMap().put(KeyStroke.getKeyStroke("UP"), "UP");
		field.getActionMap().put("UP", up);
		field.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "DOWN");
		field.getActionMap().put("DOWN", down);
		field.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "LEFT");
		field.getActionMap().put("LEFT", left);
		field.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "RIGHT");
		field.getActionMap().put("RIGHT", right);	
		field.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "ENTER");
		field.getActionMap().put("ENTER", enter);
	}
	
	/**
	 * Edits the field to meet certain qualifications. 
	 * @param x The x coordinate of the JTextField we want to edit.
	 * @param y The y coordinate of the JTextField we want to edit.  
	 * @param key A short string telling us how to edit the
	 * JTextField. For instance, a key of "~" tells us we
	 * need to remove the border of a JTextField, or DW requires
	 * a specific background. 
	 */
	private void configureTextField(int x, int y, String key)
	{
		JTextField field = spaces[x][y]; 
		
		if (key.equals("_"))
		{
			// Nothing to do here.
			letters[x][y] = Runner.EMPTY; 
		}
		else if (key.equals("~"))
		{
			field.setBorder(BLANK_BORDER);
			letters[x][y] = '~'; 
		}
		else
		{
			letters[x][y] = Runner.EMPTY; 
			
			if (key.equals("DL"))
			{
				modifierArray[x][y] = Runner.DOUBLE_LETTER; 
				field.setBackground(
						Runner.DOUBLE_LETTER_COLOR);
				field.setText("DL");
				field.setForeground(Color.LIGHT_GRAY);
			}
			else if (key.equals("TL"))
			{
				modifierArray[x][y] = Runner.TRIPLE_LETTER; 
				field.setBackground(
						Runner.TRIPLE_LETTER_COLOR);
				field.setText("TL");
				field.setForeground(Color.LIGHT_GRAY);
			}
			else if (key.equals("DW"))
			{
				modifierArray[x][y] = Runner.DOUBLE_WORD; 
				field.setBackground(
						Runner.DOUBLE_WORD_COLOR); 
				field.setText("DW");
				field.setForeground(Color.LIGHT_GRAY);
			}
			else if (key.equals("TW"))
			{
				modifierArray[x][y] = Runner.TRIPLE_WORD; 
				field.setBackground(
						Runner.TRIPLE_WORD_COLOR); 
				field.setText("TW");
				field.setForeground(Color.LIGHT_GRAY);
			}
			else if (key.equals("+"))
			{
				modifierArray[x][y] = Runner.START; 
				field.setBackground(
						Runner.START_COLOR); 
				field.setText("+");
				field.setForeground(Color.LIGHT_GRAY);
				midPoint = new Point(x,y); 
			}
			else 
			{
				System.out.println(
						key + " is not an accepted tile.");
				System.exit(0); 
			}
		}
	}
	
	private HashMap<String, Integer> readLetterPointsFile()
	{
		HashMap<String, Integer> letterPoints = new HashMap<>(); 
		
		Scanner scanner = null;
		try
		{
			scanner = new Scanner(
					new File(Runner.LETTER_POINTS_FILE_NAME));
		}
		catch (FileNotFoundException ex) 
		{
			ex.printStackTrace();
			System.exit(0);
		}
		
		while (scanner.hasNext())
		{
			letterPoints.put(scanner.next(), scanner.nextInt()); 
		}
		scanner.close(); 
		
		return letterPoints; 
	}
	
	/**
	 * This will move the selected JTextField depending 
	 * on the direction.
	 * @param direction The direction, either up (-1),
	 * down (1), left (-1), or right (1), the selected 
	 * text box should go. 
	 */
	private void arrowKeysPressed(int xDir, int yDir)
	{
		if (selectedTextFieldLocation == null)
		{
			// Nothing is selected. 
			return; 
		}
		
		// We're going to set the JTextField below/above the 
		// currently selected one as focused. 
		Point newPoint = new Point(
				selectedTextFieldLocation.x + xDir, 
				selectedTextFieldLocation.y + yDir);
		
		// If the selected text box was at the edge of the
		// screen, and the user tried to go off the screen,
		// then we will ignore the request. 
		if (newPoint.x == spaces.length)
		{
			return; 
		}
		else if (newPoint.x == -1)
		{
			return; 
		}
		if (newPoint.y == spaces[0].length)
		{
			return; 
		}
		else if (newPoint.y == -1)
		{
			return;
		}
		
		spaces[newPoint.x][newPoint.y].requestFocus();
	}
	
	/**
	 * Called whenever the robot wants to begin/end
	 * their turn
	 * @param robotMoving Whether or not the robot is
	 * currently doing their turn. 
	 */
	public void robotMoveStateChanged(boolean robotMoving)
	{
		if (robotMoving)
		{
			interactedSpaces.clear(); 
			enableDocumentListener(false); 
		}
		else 
		{
			for (JTextField space : interactedSpaces)
			{
				processCheckedSpace(space); 
			}
			
			// End robot's turn. 
			turnNumber++;
			
			enableDocumentListener(true); 
		}
	}
	
	/**
	 * Called whenever the human wants to begin/end
	 * their move. 
	 * @param humanMoving Whether or not the human
	 * is currently doing their turn. 
	 */
	public void humanMoveStateChanged(boolean humanMoving)
	{
		if (humanMoving)
		{
			// Start human's turn. 
			// We need to allow the board tiles to
			// be editable. 
			for (JTextField[] row : spaces)
			{
				for (JTextField col : row)
				{
					// If this JTextField's border
					// is our empty border, then it
					// is not an editable area. It
					// also can't have been submitted
					// already. 
					if (col.getBorder() != BLANK_BORDER &&
						!submittedSpaces.contains(col))
					{
						col.setEditable(true);
					}
				}
			}
			
			interactedSpaces = new HashSet<>(); 
			selectedTextFieldLocation = null; 
		}
		else
		{
			// End human's turn. 
			// We need to make the tiles not 
			// editable anymore. 
			for (JTextField[] row : spaces)
			{
				for (JTextField col : row)
				{
					col.setEditable(false);
				}
			}
			
			JTextField[] items = new JTextField[interactedSpaces.size()]; 
			items = interactedSpaces.toArray(items); 
			for (int index = 0; index < items.length; index++)
			{
				processCheckedSpace(items[index]); 
			}
			
			// This temporarily removes focus from the object we last
			// interacted with, so that the "type here" pending line
			// won't show up. 
			spaces[selectedTextFieldLocation.x][selectedTextFieldLocation.y]
					.setFocusable(false);
			spaces[selectedTextFieldLocation.x][selectedTextFieldLocation.y]
					.setFocusable(true);
			
			turnNumber++;
		}
	}
	
	private void processCheckedSpace(JTextField space)
	{
		if (space.getText().length() == 1 && 
			Character.isLetter(space.getText().charAt(0)))
		{
			String letter = space.getText().toUpperCase();
			int points = letterPoint.get(letter);
			Point location = spaceLocations.get(space); 
			scoreArray[location.x][location.y] = points;
			
			space.setText(
				letter + Runner.SUB[points]);
			
			space.setBackground(Runner.PIECE_COLOR);
			submittedSpaces.add(space); 
			submittedLocations.add(location); 
			letters[location.x][location.y] = letter.charAt(0);  
			
			// We need to add every empty space adjacent to this
			// one to the anchor list, to signal to the robot
			// that it's somewhere they should put a letter.
			Point anchor = new Point(location.x-1, location.y); 
			if (location.x != 0 &&
				!submittedSpaces.contains(spaces[anchor.x][anchor.y])) 
			{
				anchorSpaces.add(anchor);
			}
			
			anchor = new Point(location.x, location.y-1); 
			if (location.y != 0 &&
				!submittedSpaces.contains(spaces[anchor.x][anchor.y]))
			{
				anchorSpaces.add(anchor); 
			}
			
			anchor = new Point(location.x+1, location.y); 
			if (location.x != spaces.length-1 && 
				!submittedSpaces.contains(spaces[anchor.x][anchor.y]))
			{
				anchorSpaces.add(anchor); 
			}
			
			anchor = new Point(location.x, location.y+1); 
			if (location.y != spaces[0].length-1 &&
				!submittedSpaces.contains(spaces[anchor.x][anchor.y]))
			{
				anchorSpaces.add(anchor);
			}
			
			// We also need to remove this space from the 
			// anchor list. 
			anchorSpaces.remove(location); 
		}
	}
	
	/**
	 * Checks the interactedSpaces list to see if
	 * the move made is an actual valid move.
	 */
	private boolean checkMove()
	{
		// Pieces should all be only letters (or a ?)
		// and should all be in a straight line. 
		HashSet<Integer> xVals = new HashSet<>();
		HashSet<Integer> yVals = new HashSet<>(); 
		 
		int minX = Integer.MAX_VALUE, 
			maxX = Integer.MIN_VALUE,
			minY = Integer.MAX_VALUE,
			maxY = Integer.MIN_VALUE;
		
		for (JTextField field : interactedSpaces)
		{
			String text = field.getText();
			
			if (text.length() != 1 || !Character.isLetter(text.charAt(0)))
			{
				return false; 
			}
			
			Point loc = spaceLocations.get(field); 
			xVals.add(loc.x); 
			yVals.add(loc.y); 
			
			if (loc.x < minX) minX = loc.x;
			if (loc.x > maxX) maxX = loc.x; 
			if (loc.y < minY) minY = loc.y; 
			if (loc.y > maxY) maxY = loc.y;
		}
		
		// xVals or yVals must have a size of 1. 
		if (xVals.size() == 1) // Vertical line of letters
		{
			boolean inStartLoc = false; 
			
			// From the smallest yVal to the largest, 
			// there must be no empty spaces. Let's add
			// all other filled spaces at xVal into the 
			// list. 
			for (int y = minY; y <= maxY; y++)
			{
				// If there's an empty space between letters... 
				if (spaces[minX][y].getText().length() == 0)
				{
					return false; 
				}
				
				if (modifierArray[minX][y] == Runner.START)
				{
					inStartLoc = true; 
				}
			}
			
			if (turnNumber == 0)
			{
				return inStartLoc; 
			}
			else 
			{
				return areThereNeighboringSpaces(); 
			}
		}
		else if (yVals.size() == 1)
		{
			boolean inStartLoc = false; 
			
			// From the smallest xVal to the largest, 
			// there must be no empty spaces. Let's add
			// all other filled spaces at yVal into the 
			// list. 
			for (int x = minX; x <= maxX; x++)
			{
				// If there's an empty space between letters. 
				if (spaces[x][minY].getText().length() == 0)
				{
					return false; 
				}
				
				// If the x,y loc is a start location 
				if (modifierArray[x][minY] == Runner.START)
				{
					inStartLoc = true; 
				}
			}
			
			if (turnNumber == 0)
			{
				return inStartLoc;
			}
			else 
			{
				return areThereNeighboringSpaces(); 
			}  
		}
		else 
		{
			return false; 
		}
	}
	
	/**
	 * This method checks to see that there's some
	 * neighboring spaces *already on the board* 
	 * next to the spaces added this turn. In order
	 * for a scrabble move to be valid, you need to
	 * build off of another word.
	 */
	private boolean areThereNeighboringSpaces()
	{
		for (JTextField space : interactedSpaces)
		{
			// Search each space around this one for
			// one that is contained in submittedSpaces. 
			Point loc = spaceLocations.get(space); 
			
			if (loc.x != 0 && 
					submittedSpaces.contains(
							spaces[loc.x-1][loc.y]))
			{
				return true; 
			}
			
			else if (loc.x != spaces.length-1 && 
					submittedSpaces.contains(
							spaces[loc.x+1][loc.y]))
			{
				return true; 
			}
				
			else if (loc.y != 0 &&
					submittedSpaces.contains(
							spaces[loc.x][loc.y-1]))
			{
				return true; 
			}
			
			else if (loc.y != spaces[0].length-1  && 
					submittedSpaces.contains(
							spaces[loc.x][loc.y+1]))
			{
				return true; 
			}
		}
		
		return false; 
	}
	
	/**
	 * If we're here, we've assumed the move made
	 * is valid, and that checkMove() has returned
	 * true.
	 * @return The amount of points the last move the
	 * player made should have gotten. 
	 */
	public int getScoreFromMove(boolean removeModifiers)
	{
		int score = 0; 
		
		// Lets get the points from the base word the 
		// user has placed down. 
		// We need the direction the word was placed.
		int index = 0, x1 = -1, y1 = -1, x2 = -1, y2 = -1; 
		for (JTextField field : interactedSpaces)
		{
			Point loc = spaceLocations.get(field);
			
			if (index == 0)
			{
				x1 = loc.x; 
				y1 = loc.y; 
				index++; 
			}
			else
			{
				x2 = loc.x; 
				y2 = loc.y; 
				break; 
			}
		}
		
		// This will be true if the player placed
		// down a single tile. This has effects
		// if there's 8 tiles surrounding an empty
		// space with a modifier; if the player 
		// places a tile in the middle, then they
		// should receive the modifier twice. 
		boolean bothVerticalAndHorizontal = 
				(x1 == x2 && y1 == y2); 
		
		LinkedList<Point[]> visitedWords = new LinkedList<>();
		if (x1 == x2) // Vertical word
		{
			Point[] baseWord = readCol(x1, y1); 
			score = scoreWord(baseWord, !bothVerticalAndHorizontal && removeModifiers);
			visitedWords.add(baseWord); 
		}
		if (y1 == y2) // Horizontal word 
		{
			Point[] baseWord = readRow(x1, y1); 
			score = scoreWord(baseWord, removeModifiers);
			visitedWords.add(baseWord); 
		}
		
		for (JTextField field : interactedSpaces)
		{
			Point loc = spaceLocations.get(field);
			
			Point[] row = readRow(loc.x, loc.y); 
			Point[] col = readCol(loc.x, loc.y); 
			
			if (row.length != 1 && !visitedWord(visitedWords, row))
			{
				score += scoreWord(row, removeModifiers);
				visitedWords.add(row); 
			}
			
			if (col.length != 1 && !visitedWord(visitedWords, col))
			{
				score += scoreWord(col, removeModifiers);
				visitedWords.add(col); 
			}
		}
		
		return score; 
	}
	
	/**
	 * Reads the entire row at this point, returning a
	 * list of Points that have a score attached. 
	 */
	private Point[] readRow(int x, int y)
	{
		// Crawl as far left as possible from this
		// location. 
		int minX = x; 
		while (minX != 0 && scoreArray[minX-1][y] != 0)
		{
			minX--; 
		}
		
		// Crawl right. 
		int maxX = x;
		while (maxX+1 < spaces.length && 
				scoreArray[maxX+1][y] != 0)
		{
			maxX++; 
		}
		
		Point[] points = new Point[maxX- minX + 1]; 
		for (int arri = 0, xi = minX; xi <= maxX; arri++, xi++)
		{
			points[arri] = new Point(xi, y); 
		}
		
		return points; 
	}
	
	/**
	 * Reads the entire column at this point, returning a
	 * list of Points that have a score attached. 
	 */
	private Point[] readCol(int x, int y)
	{
		// Crawl as far up as possible from this
		// location. 
		int minY = y; 
		while (minY != 0 && scoreArray[x][minY-1] != 0)
		{
			minY--; 
		}
		
		// Crawl down. 
		int maxY = y; 
		while (maxY+1 < spaces[0].length && 
				scoreArray[x][maxY+1] != 0)
		{
			maxY++; 
		}
		
		Point[] points = new Point[maxY - minY + 1]; 
		for (int arri = 0, yi = minY; yi <= maxY; arri++, yi++)
		{
			points[arri] = new Point(x, yi); 
		}
		
		return points; 
	}
	
	/**
	 * Returns a score, factoring in modifiers. 
	 * @param points The location of each letter in a word.
	 * @param removeMod whether or not we should remove the 
	 * modifier of a letter after we place a letter there.  
	 * @return The score the word should receive. 
	 */
	private int scoreWord(Point[] points, boolean removeMod)
	{
		int score = 0; 
		int numDoubleWord = 0, numTripleWord = 0;
		for (Point point : points)
		{
			int letterAmount = scoreArray[point.x][point.y];
			
			int modifier = modifierArray[point.x][point.y]; 
			if (modifier == Runner.DOUBLE_LETTER)
			{
				letterAmount *= 2; 
			}
			else if (modifier == Runner.TRIPLE_LETTER)
			{
				letterAmount *= 3; 
			}
			else if (modifier == Runner.DOUBLE_WORD)
			{
				numDoubleWord++; 
			}
			else if (modifier == Runner.TRIPLE_WORD)
			{
				numTripleWord++; 
			}
			
			// Since we've read the modifier, let's mark the
			// letter as "read". 
			if (removeMod) modifierArray[point.x][point.y] = 0;  
			
			score += letterAmount; 
		}
		
		if (numDoubleWord != 0)
		{
			score *= numDoubleWord * 2; 
		}
		if (numTripleWord != 0)
		{
			score *= numTripleWord * 3; 
		}
		
		return score; 
	}
	
	private boolean visitedWord(LinkedList<Point[]> history, Point[] word)
	{
		HashSet<Point> wordSet = getHashSetFromWord(word);
		
		for (Point[] pastWord : history)
		{
			HashSet<Point> pastWordSet = getHashSetFromWord(pastWord); 
			
			if (pastWordSet.size() == wordSet.size()) 
			{
				boolean same = true; 
				for (Point letter : wordSet)
				{
					if (!pastWordSet.contains(letter))
					{
						same = false; 
						break; 
					}
				}
				if (same)
				{
					return true; 
				}
			}
		}
		return false; 
	}
	
	private HashSet<Point> getHashSetFromWord(Point[] word)
	{
		HashSet<Point> wordSet = new HashSet<>(); 
		for (Point point : word)
		{
			wordSet.add(point);
		}
		return wordSet; 
	}
	
	public void placeLetter(JTextField field)
	{
		interactedSpaces.add(field);
		source.setMoveState(checkMove()); 
	}
	
	public void robotPlaceLetter(Point location, char letter, boolean customLetter)
	{
		// If robot wants to place a letter somewhere where
		// there is already a letter on it, then we should
		// keep track of it. 
		if (submittedLocations.contains(location))
		{
			robotTempStorage.put(location, letter); 
		}
		
		interactedSpaces.add(spaces[location.x][location.y]); 
		// We don't setMoveState because the robot 
		// will be placing a LOT of letters. 
		
		// Custom letters '?' are not worth any points. 
		if (!customLetter)
		{
			int points = letterPoint.get(letter + ""); 
			scoreArray[location.x][location.y] = points;
		}
	}
	
	public void removeLetter(JTextField field)
	{
		if (field.getText().length() == 0 && interactedSpaces.contains(field))
		{
			// The user was going to type something,
			// but decided against it. 
			interactedSpaces.remove(field); 
			field.setText("");
		}
		source.setMoveState(checkMove());
	}
	
	public void robotRemoveLetter(Point location)
	{
		Character c; 
		if ((c = robotTempStorage.get(location)) != null)
		{
			scoreArray[location.x][location.y] = letterPoint.get(c + ""); 
		}
		else
		{
			scoreArray[location.x][location.y] = 0;
		}
		
		interactedSpaces.remove(spaces[location.x][location.y]);
	}
	
	public void setLetterVisually(Point loc, char c)
	{
		spaces[loc.x][loc.y].setText(c + "");
		spaces[loc.x][loc.y].setForeground(Color.BLACK);
	}
	
	public void removeLetterVisually(Point loc)
	{
		spaces[loc.x][loc.y].setText("");
		
		switch (modifierArray[loc.x][loc.y])
		{
		case Runner.DOUBLE_LETTER:
			spaces[loc.x][loc.y].setText("DL"); 
			spaces[loc.x][loc.y].setForeground(Color.LIGHT_GRAY);
			break;
			
		case Runner.DOUBLE_WORD:
			spaces[loc.x][loc.y].setText("DW");
			spaces[loc.x][loc.y].setForeground(Color.LIGHT_GRAY);
			break;
			
		case Runner.TRIPLE_LETTER:
			spaces[loc.x][loc.y].setText("TL");
			spaces[loc.x][loc.y].setForeground(Color.LIGHT_GRAY);
			break;
			
		case Runner.TRIPLE_WORD:
			spaces[loc.x][loc.y].setText("TW");
			spaces[loc.x][loc.y].setForeground(Color.LIGHT_GRAY);
			break;
			
		case Runner.START:
			spaces[loc.x][loc.y].setText("+");
			spaces[loc.x][loc.y].setForeground(Color.LIGHT_GRAY);
			break; 
		}
	}
	
	public void focusGained(FocusEvent event)
	{
		selectedTextFieldLocation = 
				spaceLocations.get((JTextField)event.getSource()); 
		
		int x = selectedTextFieldLocation.x,
			y = selectedTextFieldLocation.y; 
		
		// If this JTextField has a modifier, then the 
		// text is removed.
		JTextField field = spaces[x][y]; 
		switch (modifierArray[x][y])
		{
		case Runner.DOUBLE_LETTER:
			if (field.getText().equals("DL"))
			{
				field.setForeground(Color.BLACK);
				field.setText(""); 
			}
			break;
			
		case Runner.DOUBLE_WORD:
			if (field.getText().equals("DW"))
			{
				field.setForeground(Color.BLACK);
				field.setText(""); 
			}
			break;
			
		case Runner.TRIPLE_LETTER:
			if (field.getText().equals("TL"))
			{
				field.setForeground(Color.BLACK);
				field.setText(""); 
			}
			break;
			
		case Runner.TRIPLE_WORD:
			if (field.getText().equals("TW"))
			{
				field.setForeground(Color.BLACK);
				field.setText(""); 
			}
			break;
			
		case Runner.START:
			if (field.getText().equals("+"))
			{
				field.setForeground(Color.BLACK);
				field.setText("");
			}
			break; 
		}
	}
	
	public void focusLost(FocusEvent event) 
	{
		// selectedTextFieldLocation hasn't updated yet,
		// so it's still at the last location the user 
		// selected. 
		
		int x = selectedTextFieldLocation.x,
			y = selectedTextFieldLocation.y;
		
		JTextField field = spaces[x][y]; 
		
		// If user hasn't typed anything. 
		if (field.getText().length() == 0) 
		{
			switch (modifierArray[x][y])
			{
			case Runner.DOUBLE_LETTER:
				field.setText("DL"); 
				field.setForeground(Color.LIGHT_GRAY);
				break;
				
			case Runner.DOUBLE_WORD:
				field.setText("DW");
				field.setForeground(Color.LIGHT_GRAY);
				break;
				
			case Runner.TRIPLE_LETTER:
				field.setText("TL");
				field.setForeground(Color.LIGHT_GRAY);
				break;
				
			case Runner.TRIPLE_WORD:
				field.setText("TW");
				field.setForeground(Color.LIGHT_GRAY);
				break;
				
			case Runner.START:
				field.setText("+");
				field.setForeground(Color.LIGHT_GRAY);
				break; 
			}
		}
	}
	
	/**
	 * Returns the amount of points made for the last move. 
	 */
	public int getPoints() 
	{
		return getScoreFromMove(true);
	}
	
	/**
	 * Returns a character representation of the entire
	 * board. Spaces where you cannot put a letter down 
	 * will be marked with a tilde '~'.
	 */
	public char[][] getBoard()
	{
		return letters; 
	}
	
	/**
	 * Returns a list of every letter's location placed 
	 * on the board already.
	 */
	public HashSet<Point> getSubmittedLocations()
	{
		return submittedLocations; 
	}
	
	public HashSet<Point> getAnchorSpaces()
	{
		return anchorSpaces; 
	}
	
	public int getNumTurns()
	{
		return turnNumber; 
	}
	
	public Point getMidPoint() 
	{
		return midPoint; 
	}
	
	/**
	 * Returns the width and height of this board in spaces (not
	 * pixels). 
	 */
	public Dimension getSize()
	{
		return new Dimension(spaces.length, spaces[0].length); 
	}
	
	/**
	 * @return The height of a space. The height and width should
	 * ideally be the same. 
	 */
	public int getSpaceSize()
	{
		return Integer.max(spaces[0][0].getHeight(), spaces[0][0].getWidth()); 
	}
}
