import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The human player, makes moves as a human would. Requires
 * user input.
 */
public class Human implements DocumentListener
{
	/**
	 * The board holding the modifiers, letters, and letter 
	 * layout. 
	 */
	private Board board; 
	
	/**
	 * Sets the Board, which contains the letters. This would
	 * be in the constructor, but that would make the Board null. 
	 */
	public void setBoard(Board board)
	{
		this.board = board; 
	}
	
	public void insertUpdate(DocumentEvent event)
	{
		JTextField field = 
				(JTextField)event.getDocument()
				.getProperty("owner");
		
		String text = field.getText();
		if (text.equals("DL") || text.equals("DW") ||
			text.equals("TL") || text.equals("TW") ||
			text.equals("+"))
		{
			// Then we ignore this
			return; 
		}
		
		board.placeLetter(field);
	}
	
	public void removeUpdate(DocumentEvent event)
	{
		JTextField field = 
				(JTextField)event.getDocument()
				.getProperty("owner");
		
		board.removeLetter(field);
	}

	public void changedUpdate(DocumentEvent event) { }
}
