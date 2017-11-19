/*created by: Jeremiah Akins 9/2016 for Compilers*/
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException{
		LexicalAnalysis LexiAnal = new LexicalAnalysis();
		SyntacticalAnalysis SyntaxAnal = new SyntacticalAnalysis();
		
		String[] tokens = null;
		try {
			tokens = LexiAnal.getToken(LexiAnal.readFile(args[0]));
		} catch (IOException ioe) {
			System.out.println("There has been an error while trying to read a file. Please make sure that the file to be compiled is in the same directory as the compiler program being executed./n" + ioe.getMessage());
			ioe.printStackTrace();
		}
		SyntaxAnal.parseTokens(tokens);
		System.exit(0);
	}
}