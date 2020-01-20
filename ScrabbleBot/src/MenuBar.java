import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class MenuBar extends JMenuBar implements ActionListener
{
	private JMenuItem newFile, load, save, saveAs; 
	
	public MenuBar()
	{
		JMenu file = new JMenu("File");
		
		newFile = new JMenuItem("New"); 
		newFile.addActionListener(this);
		load = new JMenuItem("Load"); 
		load.addActionListener(this);
		save = new JMenuItem("Save"); 
		save.addActionListener(this);
		saveAs = new JMenuItem("Save As"); 
		saveAs.addActionListener(this);
		
		file.add(newFile); 
		file.add(load);
		file.add(save); 
		file.add(saveAs); 
		
		add(file); 
	}
	
	private void createNewFile()
	{
		JFrame newFileFrame = new JFrame("New File");
		newFileFrame.setLayout(new BoxLayout(newFileFrame.getContentPane(), BoxLayout.Y_AXIS));
		newFileFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newFileFrame.setSize(400, 200);
		
		JPanel boardPanel = new JPanel(new FlowLayout());
		boardPanel.add(new JLabel("Board:")); 
		JButton boardFileButton = new JButton("Browse..."); 
		boardPanel.add(boardFileButton); 
		
		JPanel dictionaryPanel = new JPanel(new FlowLayout()); 
		dictionaryPanel.add(new JLabel("Dictionary:")); 
		JButton dictionaryFileButton = new JButton("Browse..."); 
		dictionaryPanel.add(dictionaryFileButton);
		
		JPanel letterPanel = new JPanel(new FlowLayout()); 
		letterPanel.add(new JLabel("Letter Amounts:")); 
		JButton letterFileButton = new JButton("Browse..."); 
		letterPanel.add(letterFileButton); 
		
		newFileFrame.add(boardPanel); 
		newFileFrame.add(dictionaryPanel);
		newFileFrame.add(letterPanel); 
		
		newFileFrame.setVisible(true); 
	}
	
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		if (source == newFile)
		{
			createNewFile(); 
		}
		else if (source == load)
		{
			System.out.println("Load");
		}
		else if (source == save)
		{
			System.out.println("Save");
		}
		else if (source == saveAs)
		{
			System.out.println("Save As");
		}
	}
}
