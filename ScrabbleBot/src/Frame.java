import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class Frame extends JFrame implements ActionListener, MouseWheelListener
{	
	/**
	 * The area the score is kept for the Robot
	 * and Human. 
	 */
	private JPanel scoreArea; 
	
	/**
	 * The two labels that hold the score of the 
	 * human and the robot
	 */
	private JLabel robotScoreLabel, humanScoreLabel; 
	
	private int robotScore, humanScore; 
	
	private Board boardArea; 
	
	/**
	 * The AI making the educated moves. 
	 */
	private Robot robot; 
	
	/**
	 * Displays the letters the robot can use to
	 * build a word. 
	 */
	private JPanel robotLettersArea; 
	
	private JTextField[] robotLetters; 
	
	/**
	 * The area decisions on who's going will be 
	 * made in. 
	 */
	private JPanel decisionArea; 
	
	/**
	 * The robot generates a list of possible moves. 
	 * This navigates those moves. 
	 */
	private JButton goRightRobot, goLeftRobot; 
	
	/**
	 * Click one button to tell the system who's 
	 * going. Click it again to end their turn. 
	 */
	private JButton robotButton, humanButton; 
	
	/**
	 * Corresponds to the robotButton and humanButton. 
	 * The robotButton will display the text "Start
	 * Robot's Turn" if robotTurnStarted is false. 
	 */
	private boolean robotTurnStarted, humanTurnStarted; 
	
	/**
	 * Describes how much larger the width is compared
	 * to the height. Multiply the width by this value
	 * to get the height. 
	 */
	private double widthToHeightRatio;
	
	public Frame()
	{
		setTitle("ScrabbleBot"); 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700,700); 
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); 
		addMouseWheelListener(this); 
		setResizable(false); // User will do this themselves through mouse wheel.
		setJMenuBar(new MenuBar()); 
		
		robotScore = 0; 
		humanScore = 0;
		
		Human human = new Human(); 
		boardArea = new Board(this, human, new File(Runner.BOARD_FILE_NAME)); 
		human.setBoard(boardArea);
		
		scoreArea = createScoreArea();
		robot = new Robot(this, boardArea); 
		robotLettersArea = createRobotLettersArea(boardArea.getSize().width); 
		decisionArea = createDecisionArea(); 
		
		add(scoreArea); 
		add(boardArea); 
		add(robotLettersArea);
		add(decisionArea); 
		
		setVisible(true); 
		
		updateSizes(); 
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent event)
			{
				windowResized(event); 
			}
		});
	}
	
	/**
	 * This will be called after the JFrame is rendered. 
	 * Once it is rendered, we can receive component.getSize()
	 * from each JPanel included. We do this to adjust the 
	 * size of the screen to be manageable. 
	 */
	public void updateSizes()
	{
		setVisible(false);
		
		// We need the height of a single space on the board. The 
		// height and width should be the same thing. 
		int spaceSize = boardArea.getSpaceSize(), 
			boardWidthPixels = spaceSize * boardArea.getSize().width;
		
		scoreArea.setMaximumSize(new Dimension(
				boardWidthPixels, humanScoreLabel.getHeight()));
		robotLettersArea.setMaximumSize(
				new Dimension(boardWidthPixels, spaceSize));
		decisionArea.setMaximumSize(
				decisionArea.getSize());
		
		Dimension size = new Dimension(boardWidthPixels, 
			humanScoreLabel.getHeight() + 
			boardWidthPixels + 
			spaceSize + 
			decisionArea.getHeight()); 
		setSize(size);
//		setMinimumSize(size);
		
		widthToHeightRatio = (double)getHeight() / (double)getWidth(); 
		
		setVisible(true); 
	}
	
	/**
	 * Creates the area the scores for the players will
	 * be set in. 
	 */
	private JPanel createScoreArea()
	{
		JPanel scoreArea = new JPanel(new FlowLayout()); 
		
		robotScoreLabel = new JLabel("Robot score: " + robotScore);
		humanScoreLabel = new JLabel("Human score: " + humanScore); 
		
		scoreArea.add(robotScoreLabel); 
		scoreArea.add(humanScoreLabel); 
		
		return scoreArea; 
	}
	
	public void incrementScore(int robotPoints, int humanPoints)
	{
		robotScore += robotPoints; 
		humanScore += humanPoints;
		
		robotScoreLabel.setText("Robot score: " + robotScore);
		humanScoreLabel.setText("Human score: " + humanScore);
	}
	
	private JPanel createRobotLettersArea(int boardWidth)
	{
		JPanel robotLettersArea = 
				new JPanel(new GridLayout(0, boardWidth)); 
		robotLettersArea.setBorder(
				BorderFactory.createRaisedBevelBorder());
		
		robotLetters = new JTextField[Runner.MAX_LETTERS_IN_RACK]; 
		
		int startIndex = boardWidth / 2 - Runner.MAX_LETTERS_IN_RACK / 2; 
		for (int k = 0; k < startIndex; k++)
		{
			// We want our letters to be centered on the screen.
			// We fill the space with empty, non-editable text 
			// fields. 
			JTextField fillerTextField = new JTextField(2); 
			fillerTextField.setBorder(new LineBorder(Runner.DEFAULT_SPACE_COLOR));
			fillerTextField.setEditable(false); 
			robotLettersArea.add(fillerTextField); 
		}
		
		for (int numLetter = 0; 
				numLetter < Runner.MAX_LETTERS_IN_RACK; 
				numLetter++)
		{
			JTextField textField = new JTextField(2); 
			textField.setBackground(Runner.PIECE_COLOR);
			textField.setFont(Runner.LETTER_FONT);
			textField.setHorizontalAlignment(
					JTextField.CENTER);
			
			robotLetters[numLetter] = textField; 
			robotLettersArea.add(textField); 
		}
		return robotLettersArea; 
	}
	
	private JPanel createDecisionArea()
	{
		JPanel decisionArea = new JPanel(new FlowLayout()); 
		
		goRightRobot = new JButton(new ImageIcon("ScrabbleBot/right.png"));
		goLeftRobot = new JButton(new ImageIcon("ScrabbleBot/left.png"));
		robotButton = new JButton("Start Robot's turn");
		humanButton = new JButton("Start Human's turn"); 
		
		goRightRobot.addActionListener(this); 
		goLeftRobot.addActionListener(this);
		robotButton.addActionListener(this);
		humanButton.addActionListener(this);
		
		robotTurnStarted = false; 
		humanTurnStarted = false; 
		goLeftRobot.setEnabled(false);
		goRightRobot.setEnabled(false); 
		
		decisionArea.add(goLeftRobot); 
		decisionArea.add(goRightRobot); 
		decisionArea.add(robotButton); 
		decisionArea.add(humanButton);
		
		return decisionArea; 
	}
	
	/**
	 * If the human player is making their move,
	 * the "end human turn" button should be off
	 * if the move they have laid out isn't valid.
	 * @param isValid Whether or not the player's
	 * move is valid. 
	 */
	public void setMoveState(boolean isValid)
	{
		if (robotTurnStarted)	
		{
			robotButton.setEnabled(isValid);
		}
		else if (humanTurnStarted)
		{
			humanButton.setEnabled(isValid); 
		}
	}
	
	/**
	 * Returns the letters in the Robot's rack.
	 */
	public LinkedList<Character> getRobotRack()
	{
		LinkedList<Character> letters = new LinkedList<>(); 
		
		for (JTextField field : robotLetters)
		{
			String text = field.getText(); 
			if (text.length() > 0)
			{
				char letter = text.toUpperCase().charAt(0); 
				if (Character.isLetter(letter) || letter == '?')
					letters.add(letter); 
			}
		}
		
		return letters; 
	}
	
	/**
	 * This event will occur when the user presses
	 * the enter key while focused on a JTextField. 
	 * This should be the same as pressing the "submit
	 * human turn" or "submit robot turn" key.  
	 */
	public void enterKeyPressed()
	{
		if (robotTurnStarted && robotButton.isEnabled())
		{
			robotButton.doClick(); 
		}
		else if (humanTurnStarted && humanButton.isEnabled())
		{
			humanButton.doClick(); 
		}
	}
	
	/**
	 * Called when the user resizes the window. 
	 */
	public void windowResized(ComponentEvent event)
	{
		Rectangle bounds = event.getComponent().getBounds(); 
		event.getComponent().setBounds(bounds.x, bounds.y, bounds.width, (int) (bounds.width * widthToHeightRatio));
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == robotButton)
		{
			if (!robotTurnStarted)
			{
				// There must be something in the rack. If there
				// isn't, ignore this request. 
				if (getRobotRack().size() == 0)
				{
					return; 
				}
				
				// Start the robot's turn
				robotTurnStarted = true; 
				robotButton.setText("End Robot's Turn");
				
				boardArea.robotMoveStateChanged(robotTurnStarted);
				robot.makeMove(); 
				
				goRightRobot.setEnabled(true);
			}
			else 
			{
				// End the robot's turn
				robotTurnStarted = false;
				robotButton.setText("Start Robot's Turn");
				boardArea.robotMoveStateChanged(robotTurnStarted);
				
				incrementScore(boardArea.getPoints(), 0); 
				
				goLeftRobot.setEnabled(false);
				goRightRobot.setEnabled(false);
			}
			humanButton.setEnabled(!robotTurnStarted);
		}
		else if (event.getSource() == humanButton)
		{
			if (!humanTurnStarted)
			{
				// Start the human's turn
				humanTurnStarted = true; 
				humanButton.setText("End Human's Turn");
				humanButton.setEnabled(false);
				boardArea.humanMoveStateChanged(humanTurnStarted);
			}
			else 
			{
				// End the human's turn
				humanTurnStarted = false; 
				humanButton.setText("Start Human's Turn");
				boardArea.humanMoveStateChanged(humanTurnStarted);
				
				incrementScore(0, boardArea.getPoints()); 
			}
			robotButton.setEnabled(!humanTurnStarted);
		}
		else if (event.getSource() == goLeftRobot)
		{
			goRightRobot.setEnabled(true);
			robot.changeMove(true);
			if (robot.getIndex() == 0)
			{
				goLeftRobot.setEnabled(false); 
			}
		}
		else if (event.getSource() == goRightRobot)
		{
			goLeftRobot.setEnabled(true);
			robot.changeMove(false);
			if (robot.getIndex() == robot.getLength()-1)
			{
				goRightRobot.setEnabled(false);
			}
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		// If the wheel moves up, increase screen size. 
		// Down, decrease screen size. 
		if (e.getWheelRotation() < 0)
		{
			setSize(getSize().width + 10, getSize().height);
		}
		else
		{
			setSize(getSize().width - 10, getSize().height);
		}
	}
}
