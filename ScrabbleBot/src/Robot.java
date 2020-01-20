import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * The AI that uses an algorithm to determine
 * the best move to use to get the most points
 * on a Scrabble board.
 */
public class Robot 
{
	/**
	 * The JFrame holding this project. Contains information
	 * for the letters in the Robot's rack. 
	 */
	private Frame frame; 
	
	/**
	 * The board holding the modifiers, letters, and letter 
	 * layout. 
	 */
	private Board boardArea; 
	
	private Trie dictionary; 
	
	private LinkedList<Character> rack; 
	
	private char[][] board; 
	
	private HashSet<Point> letters; 
	
	private HashSet<Point> anchorSpaces; 
	
	private PriorityQueue<Move> possibleMovesQueue;
	
	private Move[] possibleMoves; 
	
	private int index; 
	
	/**
	 * A completed word, points the word fit
	 * in, and a score.
	 */
	private class Move implements Comparable<Move>
	{
		private String word; 
		private LinkedList<Point> locs;
		private LinkedList<Boolean> customChars; 
		private int score; 
		
		private Move(String word, LinkedList<Point> locs, LinkedList<Boolean> customChars)
		{
			this.word = word; 
			this.locs = locs; 
			this.customChars = customChars; 
		}
		
		public Move clone()
		{
			LinkedList<Point> locsCopy = new LinkedList<>(); 
			for (Point loc : locs)
			{
				locsCopy.add(new Point(loc.x, loc.y));
			}
			LinkedList<Boolean> customCharsCopy = new LinkedList<>(); 
			for (Boolean bool : customChars)
			{
				customCharsCopy.add(bool); 
			}
			return new Move(word + "", locsCopy, customCharsCopy);
		}
		
		@Override
		public String toString()
		{
			return word + " " + locs + " " + score; 
		}
		
		@Override
		public int compareTo(Move other) 
		{
			return Integer.compare(other.score, score);
		}
	}
	
	/**
	 * @param frame The JFrame holding this project, including
	 * the board and the robot's rack.
	 * @param boardArea The board the letters will be placed on. 
	 */
	public Robot(Frame frame, Board boardArea)
	{
		this.frame = frame; 
		this.boardArea = boardArea; 
		
		dictionary = new Trie(Runner.DICTIONARY_FILE_NAME);
	}
	
	/**
	 * Determines which move to do, given the layout of the 
	 * board and the letters in our rack. 
	 */
	public void makeMove()
	{
		rack = frame.getRobotRack();
		board = boardArea.getBoard(); 
		letters = boardArea.getSubmittedLocations();
		possibleMovesQueue = new PriorityQueue<>();
		anchorSpaces = boardArea.getAnchorSpaces(); 
		
		index = 0; 
		
		if (boardArea.getNumTurns() == 0)
		{
			letters.add(new Point(boardArea.getMidPoint())); 
		}
		
		for (Point point : anchorSpaces)
		{
			for (int k = 0; k < rack.size(); k++)
			{
				char c = rack.remove(k); 
				
				String str = c + ""; 
				Move currentMove = new Move(str, new LinkedList<Point>(), new LinkedList<Boolean>()); 
				currentMove.locs.add(point); 
				currentMove.customChars.add(false);
				
				// Horizontal word
				{
					goLeft(currentMove, false);
					findLeftParts(currentMove, false); 

					Node cnode = dictionary.getRoot(); 
					for (char wordc : currentMove.word.toCharArray())
					{
						cnode = cnode.get(wordc);
						if (cnode == null)
						{
							break; 
						}
					}
					if (cnode != null)
					{
						Node findRightPartsNode =
								goRight(cnode, currentMove, false);
						if (findRightPartsNode != null && isPossibleForAWord(currentMove.word))
						{
							findRightParts(findRightPartsNode, currentMove, false); 
						}
					}
				}
				
				// Vertical word
				{
					goLeft(currentMove, true); 
					findLeftParts(currentMove, true); 
					
					Node cnode = dictionary.getRoot(); 
					for (char wordc : currentMove.word.toCharArray())
					{
						cnode = cnode.get(wordc);
						if (cnode == null)
						{
							break; 
						}
					}
					if (cnode != null)
					{
						Node findRightPartsNode =
								goRight(cnode, currentMove, true);
						if (findRightPartsNode != null && isPossibleForAWord(currentMove.word))
						{
							findRightParts(findRightPartsNode, currentMove, true); 
						}
					}
				}
				
				rack.add(k, c); 
			}
		}
		
		int index = 0; 
		
		possibleMoves = new Move[possibleMovesQueue.size()]; 
		possibleMoves = possibleMovesQueue.toArray(possibleMoves); 
		Arrays.sort(possibleMoves); 
		
		System.out.println();
		for (int k = 0; k < possibleMoves.length; k++)
		{
			System.out.println(possibleMoves[k]);
		}
		
		Move move = possibleMovesQueue.poll();
		Boolean[] customChars = new Boolean[move.customChars.size()];
		customChars = move.customChars.toArray(customChars); 
		for (Point loc : move.locs)
		{
			if (!letters.contains(loc))
			{
				boardArea.robotPlaceLetter(loc, move.word.charAt(index), customChars[index]);
				boardArea.setLetterVisually(loc, move.word.charAt(index));
			}
			index++; 
		}
	}
	
	/**
	 * @param vertical Whether or not we're looking for an 
	 * up-and-down word (vertical). False is a left-and-right 
	 * word (horizontal).
	 */
	private void findLeftParts(Move move, boolean vertical)
	{
		Point leftMostLoc = move.locs.getFirst(); 
		
		// Try to place a tile to the left/above.
		if ((!vertical && leftMostLoc.x > 0 && 
			  board[leftMostLoc.x-1][leftMostLoc.y] == Runner.EMPTY) ||
			 (vertical && leftMostLoc.y > 0 && 
			  board[leftMostLoc.x][leftMostLoc.y-1] == Runner.EMPTY))
		{
			for (int k = 0; k < rack.size(); k++)
			{
				char firstItem = rack.remove(k); 
				if (firstItem == '?')
				{
					for (char l : Runner.ALPHABET)
					{
						findLeftParts(move, leftMostLoc, vertical, l); 
					}
				}
				else
				{
					findLeftParts(move, leftMostLoc, vertical, firstItem);
				}
				
				// Put our first letter back in the rack.
				rack.add(k, firstItem);
			}
		}
	}
	
	private void findLeftParts(Move move, Point leftMostLoc, boolean vertical, char letter)
	{
		Move newMove = move.clone();
		newMove.word = letter + newMove.word;
		if (!vertical)
		{
			newMove.locs.addFirst(new Point(leftMostLoc.x-1, leftMostLoc.y));
		}
		else
		{
			newMove.locs.addFirst(new Point(leftMostLoc.x, leftMostLoc.y-1));
		}
		newMove.customChars.addFirst(false);
		
		// With this addition, go as far left as possible. 
		goLeft(newMove, vertical); 
		
		Node node = dictionary.getRoot();
		
		// Try to build a word using what we have. If we can't
		// build a word with what we just made, then don't go
		// any further!
		for (int si = 0; si < newMove.word.length(); si++)
		{
			if (node == null)
			{
				break; 
			}
			node = node.get(newMove.word.charAt(si));
		}
		
		// This word IS possible, so let's keep going!
		if (node != null)
		{
			// Before moving forward, move to the right. 
			if ((node = goRight(node, newMove, vertical)) == null)
			{
				return; // No possible word can form from this location. 
			}
			
			// Check if the new node ends. We could make a word here. 
			if (node.ends)
			{
				nodeEnds(newMove, vertical); 
			}
			
			// The word may be made up of only left parts. 
			findRightParts(node, newMove, vertical);
		}
		
		// No matter what, let's try and go left further.
		if (rack.size() > 0)
		{
			findLeftParts(newMove, vertical);
		}
	}
	
	/**
	 * @param node Our current node.
	 */
	private void findRightParts(Node node, Move move, boolean vertical)
	{
		// If we can't place a move to the right, there's no
		// use in trying. 
		if ((!vertical && move.locs.getLast().x+1 >= board.length) ||
			 (vertical && move.locs.getLast().y+1 >= board[0].length))
		{
			return; 
		}
		
		char boardAtIndex; 
		if (!vertical)
		{
			boardAtIndex = board[move.locs.getLast().x+1][move.locs.getLast().y]; 
		}
		else
		{
			boardAtIndex = board[move.locs.getLast().x][move.locs.getLast().y+1]; 
		}
		
		if (boardAtIndex == '~')
		{
			return; 
		}
		
		for (int index = 0; index < rack.size(); index++)
		{
			// Clone the move so we're not modifying the original.
			Move newMove = move.clone();
			Point loc; 
			if (!vertical)
			{
				loc = new Point(newMove.locs.getLast().x+1, newMove.locs.getLast().y);
			}
			else
			{
				loc = new Point(newMove.locs.getLast().x, newMove.locs.getLast().y+1);
			}
			newMove.locs.add(loc); 
			newMove.customChars.add(false);
			
			char letterFromRack = rack.remove(index); 
			if (letterFromRack == '?')
			{
				for (char l : Runner.ALPHABET)
				{
					findRightParts(node, newMove, vertical, l); 
				}
			}
			else
			{
				findRightParts(node, newMove, vertical, letterFromRack); 
			}
			
			// Remove letter from word, and place letter 
			// back on to the rack. 
			rack.add(index, letterFromRack); 
		}
	}
	
	private void findRightParts(Node node, Move newMove, boolean vertical, char letter)
	{
		// Place letter at end of our word. 
		newMove.word = newMove.word + letter; 
		
		// Go to this place in the Trie. 
		Node child = node.get(letter);
		
		// If child isn't null, then this portion of the
		// word is possible.
		if (child != null)
		{
			if ((child = goRight(child, newMove, vertical)) != null)
			{
				// Let's try to expand the word more. 
				findRightParts(child, newMove, vertical); 
				
				// If the child ends here, then we've found a 
				// complete word. 
				if (child.ends)
				{
					nodeEnds(newMove, vertical); 
				} 
			}
		}
	}
	
	/**
	 * Returns whether or not this String exists, at least in
	 * part, in the Trie.
	 */
	private boolean isPossibleForAWord(String word)
	{
		Node node = dictionary.getRoot();
		for (int index = 0; index < word.length(); index++)
		{
			node = node.get(word.charAt(index)); 
			if (node == null)
			{
				return false; 
			}
		}
		return true; 
	}
	
	/**
	 * From the last position, keep moving to the right until
	 * we hit the wall, or an empty space.
	 * @return A valid node if this word is possible, null if
	 * otherwise. 
	 */
	private Node goRight(Node node, Move move, boolean vertical)
	{
		// If the space to the right of here is already
		// filled, then we need to go all the way to the
		// right. 
		Point currentLoc = move.locs.getLast(); 
		while ((!vertical && currentLoc.x + 1 < board.length &&
			board[currentLoc.x+1][currentLoc.y] != Runner.EMPTY &&
			board[currentLoc.x+1][currentLoc.y] != '~') ||
				(vertical && currentLoc.y + 1 < board[0].length &&
			board[currentLoc.x][currentLoc.y+1] != Runner.EMPTY &&
			board[currentLoc.x][currentLoc.y+1] != '~'))
		{
			Point nextLoc; 
			if (!vertical)
			{
				nextLoc = new Point(currentLoc.x+1, currentLoc.y); 
			}
			else
			{
				nextLoc = new Point(currentLoc.x, currentLoc.y+1); 
			}
			move.locs.addLast(nextLoc);
			move.customChars.addLast(false);
			
			char letter; 
			if (!vertical)
			{
				letter = board[currentLoc.x+1][currentLoc.y]; 
			}
			else
			{
				letter = board[currentLoc.x][currentLoc.y+1]; 
			}
			move.word = move.word + letter; 
			node = node.get(letter);
			
			if (node == null)
			{
				return null; 
			}
			else
			{
				currentLoc = nextLoc;
			}
		}
		return node; 
	}
	
	private void goLeft(Move move, boolean vertical)
	{
		Point currentPoint = move.locs.getFirst(); 
		while ((!vertical && currentPoint.x > 0 &&
			board[currentPoint.x-1][currentPoint.y] != Runner.EMPTY &&
			board[currentPoint.x-1][currentPoint.y] != '~') ||
				(vertical && currentPoint.y > 0 &&
			board[currentPoint.x][currentPoint.y-1] != Runner.EMPTY &&
			board[currentPoint.x][currentPoint.y-1] != '~'))
		{
			Point nextPoint;
			if (!vertical)
			{
				nextPoint = new Point(currentPoint.x-1, currentPoint.y); 
			}
			else
			{
				nextPoint = new Point(currentPoint.x, currentPoint.y-1); 
			}
			move.locs.addFirst(nextPoint);
			move.customChars.addFirst(false);
			move.word = board[nextPoint.x][nextPoint.y] + move.word;  
			currentPoint = nextPoint; 
		}
	}
	
	private void nodeEnds(Move move, boolean vertical)
	{
		// Word must be in dictionary
		if (!dictionary.contains(move.word))
		{
			return; 
		}
		
		// FIRST, check to see if this horizontal
		// word created any vertical words. 
		boolean valid = true; 
		int letterIndex = 0; 
		for (Point point : move.locs)
		{
			if (!checkWord(point, move.word.charAt(letterIndex++), !vertical))
			{
				valid = false; 
				break; 
			}
		}
		if (valid)
		{
			letterIndex = 0; 
			Boolean[] customChars = new Boolean[move.customChars.size()]; 
			customChars = move.customChars.toArray(customChars); 
			for (Point point : move.locs)
			{ 
				if (!letters.contains(point))
				{
					boardArea.robotPlaceLetter(point, move.word.charAt(letterIndex), customChars[letterIndex++]);
				}
			}
			move.score = boardArea.getScoreFromMove(false);
			possibleMovesQueue.add(move); 
			
			for (Point point : move.locs)
			{
				if (!letters.contains(point))
				{
					boardArea.robotRemoveLetter(point); 
				}
			}
		}
	}
	
	/**
	 * Checks with the Trie if the word at the point is
	 * valid. 
	 * @param board The temporary version of the board; this
	 * time, with our letters from this move included. 
	 * @param vertical Whether or not the word is vertical
	 * or horizontal. 
	 */
	private boolean checkWord(Point start, char c, boolean vertical)
	{
		// If this point is off the screen, it isn't valid. 
		if (start.x < 0 || start.x >= board.length ||
			start.y < 0 || start.y >= board[0].length)
		{
			return false; 
		}
		
		String word = c + ""; 
		if (vertical)
		{
			// Go as far up as possible.
			int topY = start.y;
			while (true)
			{
				if (topY != 0 && 
					board[start.x][topY-1] != '~' &&
					board[start.x][topY-1] != Runner.EMPTY)
				{
					topY--; 
					word = board[start.x][topY] + word; 
				}
				else
				{
					break; 
				}
			}
			
			// Go as far down as possible. 
			int bottomY = start.y; 
			while (true)
			{
				if (bottomY+1 < board[0].length && 
					board[start.x][bottomY+1] != '~' &&
					board[start.x][bottomY+1] != Runner.EMPTY)
				{
					bottomY++; 
					word = word + board[start.x][bottomY]; 
				}
				else
				{
					break; 
				}
			}
			
			if (word.length() == 1)
			{
				return true; 
			}
			
			// Let's check this word in the DAWG. If it's valid,
			// return true. 
			return dictionary.contains(word);
		}
		else
		{
			// Go as far left as possible.
			int leftX = start.x;
			while (true)
			{
				if (leftX != 0 && 
					board[leftX-1][start.y] != '~' && 
					board[leftX-1][start.y] != Runner.EMPTY)
				{
					leftX--; 
					word = board[leftX][start.y] + word; 
				}
				else
				{
					break; 
				}
			}
			
			// Go as far right as possible. 
			int rightX = start.x; 
			while (true)
			{
				if (rightX+1 < board.length && 
					board[rightX+1][start.y] != '~' &&
					board[rightX+1][start.y] != Runner.EMPTY)
				{
					rightX++; 
					word = word + board[rightX][start.y]; 
				}
				else
				{
					break; 
				}
			}
			
			if (word.length() == 1)
			{
				return true; 
			}
			
			// Let's check this word in the DAWG. If it's valid,
			// return true. 
			return dictionary.contains(word);
		}
	}
	
	public void changeMove(boolean up)
	{
		for (Point loc : possibleMoves[index].locs)
		{
			if (!letters.contains(loc))
			{
				boardArea.robotRemoveLetter(loc);
				boardArea.removeLetterVisually(loc);
			}
		}
		
		if (up)
		{
			index--; 
		}
		else
		{
			index++; 
		}
		
		String str = possibleMoves[index].word; 
		int wordi = 0; 
		for (Point loc : possibleMoves[index].locs)
		{
			if (!letters.contains(loc))
			{
				boardArea.robotPlaceLetter(loc, str.charAt(wordi), false); // TODO
				boardArea.setLetterVisually(loc, str.charAt(wordi));
			}
			wordi++; 
		}
	}
	
	public int getIndex() { return index; }
	public int getLength() { return possibleMoves.length; }
}
