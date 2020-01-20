import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class Test 
{
	public static void main(String[] args) 
	{
		PrintWriter writer = null; 
		Scanner scanner = null; 
		try
		{
			scanner = new Scanner(new File("Google 20k Words.dat")); 
			writer = new PrintWriter(new File("Google 20k Words.txt")); 
		}
		catch (Exception ex)
		{
			System.out.println("Problem");
			System.exit(0);
		}
		
		while (scanner.hasNext())
		{
			String word = scanner.next().toUpperCase();
			if (word.length() > 1)
			{
				writer.println(word); 
			}
		}
		scanner.close(); 
		writer.close();
	}
}
