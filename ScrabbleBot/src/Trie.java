import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Trie 
{
	private static String SOURCE_FILE_NAME 
		= "Collins Scrabble Words (2019).txt"; 
	
	private Node root; 
	
	public Trie(String sourceFileName)
	{
		Scanner scanner = null;
		try
		{
			scanner = new Scanner(new File(sourceFileName)); 
		}
		catch (FileNotFoundException ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
		
		root = new Node('!', false); 
		
		while (scanner.hasNext())
		{
			add(scanner.next()); 
		}
	}
	
	public void add(String word)
	{
		traverse(root, word); 
	}
	
	private void traverse(Node node, String str)
	{
		// This word exists in part already. Set this
		// index as a possible ending area. 
		if (str.length() == 0)
		{
			node.ends = true; 
		}
		
		// Word doesn't exist yet. 
		else
		{
			Node next = node.get(str.charAt(0)); 
			
			if (next == null) // Child doesn't exist. 
			{
				for (int k = 0; k < str.length()-1; k++)
				{
					next = new Node(str.charAt(k), false); 
					node.add(next);
					node = next; 
				}
				node.add(new Node(str.charAt(str.length()-1), true));
			}
			else
			{
				traverse(next, str.substring(1)); 
			}
		}
	}
	
	public boolean contains(String word)
	{
		Node currentNode = root; 
		for (char c : word.toCharArray())
		{
			currentNode = currentNode.get(c); 
			if (currentNode == null)
			{
				return false; 
			}
		}
		return currentNode.ends; 
	}
	
	public Node getRoot()
	{
		return root; 
	}
	
	private void checkTrie(String fileName)
	{
		Scanner scanner = null; 
		try
		{
			scanner = new Scanner(new File(fileName)); 
		}
		catch (FileNotFoundException ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
		
		while (scanner.hasNext())
		{
			String word = scanner.next(); 
			if (!contains(word))
			{
				System.out.println(word);
			}
			else
			{
				System.out.println("Contains: " + word);
			}
		}
	}
	
	public static void main(String[] args) 
	{
		System.out.println("Started");
		Trie trie = new Trie(SOURCE_FILE_NAME); 
		trie.checkTrie(SOURCE_FILE_NAME); 
		System.out.println("Finished");
	}
}

class Node
{
	char c;
	boolean ends; 
	HashMap<Character, Node> children; 
	
	Node(char c, boolean ends)
	{
		this.c = c; 
		this.ends = ends; 
		
		children = new HashMap<>();
	}
	
	void add(Node child)
	{
		children.put(child.c, child); 
	}
	
	Node get(Character key)
	{
		return children.get(key); 
	}
	
	public String toString()
	{
		return ends + ""; 
	}
}
