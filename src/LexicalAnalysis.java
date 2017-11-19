/*created by: Jeremiah Akins 9/2016 for Compilers*/
import  java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LexicalAnalysis {
	/*flag for printing tokens and input, set to true for printing*/
	private Boolean print = false;
	private int lineNumber;
	private int commentCounter = 0;
	private int index;
	private BufferedReader sourceFileInBuffered;
	private Boolean isDivide = false;
	private String word = "";
	private ArrayList<String> tokenList = new ArrayList<String>();
		
		/*Constructor*/
		public LexicalAnalysis() {
		} //End of Constructor
/*===================================================================================================*/	
		/*readFile() reads in a file from a passed-in argument, assigns its contents to a buffer, and
		 * then returns a string containing the first line of the buffered file*/
		public String readFile(String argZero) throws IOException {
			File sourceFileIn = new File(argZero);
			try {
				sourceFileInBuffered = new BufferedReader(new FileReader(sourceFileIn));
			}
			catch(IOException ioe) {
				System.out.println("Error: " + ioe.getMessage());
			}
			//lineNumber++;
			String theFirstLine = sourceFileInBuffered.readLine();
			/*System.out.println("Input: " + theFirstLine);
			return theFirstLine;*/
			String eliminatedWhitespace = theFirstLine;
			if(theFirstLine == null) {
				return null;
			}
			while(theFirstLine.isEmpty()) {
				lineNumber++;
				theFirstLine = sourceFileInBuffered.readLine();
				eliminatedWhitespace = theFirstLine;
				if(theFirstLine == null) {
					break;
				}
			}
			eliminatedWhitespace = eliminatedWhitespace.trim();
			if(theFirstLine.length() > 0) {
				lineNumber++;
				if(eliminatedWhitespace.length() > 0 && print) {
					System.out.println("Input: " + theFirstLine);
				}
				return theFirstLine;
			} else {
				return null;
			}
		} //End of readFile()
/*===================================================================================================*/		
		/*getNextLine() returns a string containing the next line in the buffered reader*/
		public String getNextLine() throws IOException {
			String nextLine = sourceFileInBuffered.readLine();
			String eliminatedWhitespace = nextLine;
			if(nextLine == null) {
				return null;
			}
			while(nextLine.isEmpty()) {
				lineNumber++;
				nextLine = sourceFileInBuffered.readLine();
				eliminatedWhitespace = nextLine;
				if(nextLine == null) {
					break;
				}
			}
			eliminatedWhitespace = eliminatedWhitespace.trim();
			if(nextLine.length() > 0) {
				lineNumber++;
				if(eliminatedWhitespace.length() > 0 && print) {
					System.out.println("\nInput: " + nextLine);
				}
				return nextLine;
			} else {
				return null;
			}
		} //End of getNextToken()
/*===================================================================================================*/
		/*getToken() is the central method for retrieving and tokenizing the contents of the source file*/
		public String[]  getToken(String line) throws IOException {
			Boolean unhandledChar = false;
			for(int i = 0; i < line.length();) {
				i++;
				unhandledChar = false;
				if(index < line.length() && !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index)) && !Character.isWhitespace(line.charAt(index))) {
					line = this.handleSpecialSymbol(line);
					if(index > line.length()-1) {
						index--;
					}
					i = index;
				}
				if(index < line.length() && Character.isLetter(line.charAt(index))) {
					line = this.handleLetter(line);
					if(index == line.length()-1 && !Character.isLetter(line.charAt(index))) {
						unhandledChar = true;
					} else {
						i = index;
					}
				}
				if(index < line.length() && Character.isDigit(line.charAt(index))) {
					line = this.handleNumber(line);
					if(index == line.length()-1 && !Character.isDigit(line.charAt(index))) {
						unhandledChar = true;
					} else {
						i = index;
					}
				}
				if(index < line.length() && Character.isWhitespace(line.charAt(index))) {
					if(index < line.length()-1) {
						index++;
						if(index == line.length()-1) {
							unhandledChar = true;
						} else {
							i = index;
						}
					}
				}
				if(index == line.length()-1 && !unhandledChar) {
					String newLine = this.getNextLine();
					if(newLine != null) {
						line = newLine;
						index = 0;
						i = index;
					} else {
						break;
					}
				}
			}
			sourceFileInBuffered.close();
			Object[] tokenObject = tokenList.toArray();
			String[] token = new String[tokenObject.length];
			for(int i = 0; i < token.length; i++) {
				token[i] = tokenObject[i].toString();
			}
			return token;
		} //End of getToken()
/*===================================================================================================*/	
		/*handleSlashStar() handles all comments and will break and return if '/' is not proceeded 
		 * by a valid comment generating character unless found in nested comments, as in such 
		 * case that '/' may be used only as a divide*/
		public String handleSlashStar(String line) throws IOException {
			Boolean isNewLine;
			while(line != null && index < line.length()-1) {
				isNewLine = false;
				if(line.charAt(index) == '/') {
					/*------------------------------handles the case in which '/' is divide---------------------------------------------*/
					if(index > 0 && commentCounter == 0 &&  line.charAt(-1+index) != '/') {
						if(index < line.length()-1 && line.charAt(1+index) != '/' && line.charAt(1+index) != '*') {
							isDivide = true;
							break;
						} else if (index == line.length()-1) {
							isDivide = true;
							break;
						}
					}
					if(index == 0 && line.charAt(1+index) != '/' && line.charAt(1+index) != '*' && commentCounter == 0) {
						isDivide = true;
						break;
					}  else if (index == line.length()-1 && commentCounter == 0) {
						break;
					}
					/*--------------------------------------------------------------------------------------------------------------------------*/
					if(index == line.length()-1 && commentCounter > 0) {
						String nextLine = this.getNextLine();
						if(nextLine != null) {
							//System.out.println("will continue slash star operation with new line");
							line = nextLine;
							index = 0;
							isNewLine = true;
						}
					}
					if(index < line.length()-1 && line.charAt(1+index) == '/' && commentCounter == 0) {
						//System.out.println("the rest of the line is successfully void");
						String nextLine = this.getNextLine();
						if(nextLine != null) {
							line = nextLine;
							index = 0;
							isNewLine = true;
							break;
						} else {
							index = line.length()+1; //added fix on 10/15/2016 so that the last character of a single-line comment does not get analyzed
							//index++; //original code before fix
							return line;
						}
					} else if(index < line.length()-1 && line.charAt(1+index) == '*') {
						commentCounter++;
						index++;
						if(index == line.length()-1) {	//	Fix added 10/16/2016, would exit loop and not finish handling multilined comment if /* was the first and only thing on the current line
							String nextLine = this.getNextLine();
							if(nextLine != null) {
								//System.out.println("will continue slash star operation with new line");
								line = nextLine;
								index = 0;
								isNewLine = true;
							}
						}
					}
					if(index < line.length()-1 && !isNewLine) {
						index++;
					}
				} else {
					if (line.charAt(index) == '*') {
						if(index < line.length()-1 && line.charAt(1+index) == '/') {
							commentCounter--;
							index++;
							if(commentCounter == 0) {
								if(index == line.length()-1) {
									//System.out.println("slash star comment(s) is/are handled");
									return line;
								} else if (index < line.length()-1) {
									//System.out.println("slash star is handled, will continue from current index as normal");
									index++;
									if(!Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index)) && !Character.isWhitespace(line.charAt(index))) {
										this.handleSpecialSymbol(line); //handle symbol here since it can't be reached again if it is the last character on the line
										if(index > line.length()-1) {
											index--;
										}
									}
									line = line.substring(index);
									index = 0;
									break;
								}
							}
							if(!isNewLine && index < line.length()-1 && line.charAt(1+index) != '*') {
								index++;
							}
						}
					}
					if(!isNewLine && index < line.length()-1) {
						index++;
					}
					if(index == line.length()-1 && commentCounter > 0) {
						String nextLine = this.getNextLine();
						if(nextLine != null) {
							//System.out.println("will continue slash star operation with new line");
							line = nextLine;
							index = 0;
							isNewLine = true;
							if(line.length() == 1 /*&& Character.isWhitespace(line.charAt(0))*/) {	//	Fix added 10/16/2016, this is to account for a tabbed or spaced blank line where otherwise handling a multilined comment would exit the loop and not finish
								nextLine = this.getNextLine();
								if(nextLine != null) {
									//System.out.println("will continue slash star operation with new line");
									line = nextLine;
									index = 0;
									isNewLine = true;
								}
							}
						} else { //(in the event of a non-closed comment block) exits when nextLine is null, sets the current index to 0, and sets the line to a space so that out of bounds error does not occur, to prevent the last character from being printed upon exit
							index = 0;
							line = " ";
							break;
						}
					}
				}
			}
			if(line.charAt(index) == '/' && index == line.length()-1 && commentCounter == 0) {
				isDivide = true;
			}
			return line;
	} //End of handleSlashStar()
/*===================================================================================================*/	
		/*handleSpecialSymbol() analyzes the special symbol at the current index, handles it accordingly, and stores the symbol as a token*/
		public String handleSpecialSymbol(String line) throws IOException {
			while(index < line.length() && !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index)) && !Character.isWhitespace(line.charAt(index))) {
				switch(line.charAt(index)) {
					case '/':
						line = this.handleSlashStar(line);
						if(isDivide) {
							isDivide = false;
							tokenList.add(Character.toString(line.charAt(index)));
							if(print) {
								System.out.println(line.charAt(index));
							}
							if(index < line.length()-1) {
								index++;
							}
						}
						return line;
					case '+':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '-':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '*':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '=':
						if(index < line.length()-1 && line.charAt(1+index)== '=') {
							tokenList.add(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
							if(print) {
								System.out.println(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
							}
							index = index + 2;
							break;
						}
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '<':
						if(index < line.length()-1 && line.charAt(1+index)== '=') {
							tokenList.add(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
							if(print) {
								System.out.println(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
							}
							index = index + 2;
							break;
						}
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '>':
						if(index < line.length()-1 && line.charAt(1+index)== '=') {
							tokenList.add(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
							if(print) {
								System.out.println(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
							}
							index = index + 2;
							break;
						}
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case ';':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case ',':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '(':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case ')':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '{':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '}':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case '[':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					case ']':
						tokenList.add(Character.toString(line.charAt(index)));
						if(print) {
							System.out.println(line.charAt(index));
						}
						index++;
						break;
					default:
						if(line.charAt(index) == '!') {
							if(index < line.length()-1 && line.charAt(1+index) == '=') {
								tokenList.add(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
								if(print) {
									System.out.println(Character.toString(line.charAt(index)) + Character.toString(line.charAt(1+index)));
								}
								index = index + 2;
								break;
							}
							if(print) {
								System.out.println("Error: '" + line.charAt(index) + "' at index " + index +" on line " + lineNumber);
							}
							tokenList.add(Character.toString(line.charAt(index)));
							index++;
						} else {
							if(print) {
								System.out.println("Error: '" + line.charAt(index) + "' at index " + index +" on line " + lineNumber);
							}
							tokenList.add(Character.toString(line.charAt(index)));
							index++;
						}
						break;
				}
			}
			return line;
		} //End of handleSpecialSymbol()
/*===================================================================================================*/	
		/*handleSpecialSymbol() analyzes the letter at the current index, handles it accordingly, and stores the identifier or keyword as a token*/
		public String handleLetter(String line) throws IOException {
			while(Character.isLetter(line.charAt(index))) {
				switch(line.charAt(index)) {
					case 'e':
						if(word.equals("")) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else if(index == line.length()-1){
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								return line;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'l') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 's') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'e') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()-1){
										if(print) {
											System.out.println("Keyword: " + word);
										}
										tokenList.add(word);
										word = "";
										return line;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isWhitespace(line.charAt(index)) | !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index))) {
								if(print) {
									System.out.println("Keyword: " + word);
								}
								tokenList.add(word);
								word = "";
								return line;
							}
						} else if(Character.isLetter(line.charAt(index))){
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else {
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								String nextLine = this.getNextLine();
								if(nextLine != null) {
									line = nextLine;
									index = 0;
								} else {
									return line;
								}
							}
							break;
						}
						break;
					case 'f':
						if(word.equals("")) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else if(index == line.length()-1){
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								return line;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'l') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'o') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'a') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 't') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()-1){
										if(print) {
											System.out.println("Keyword: " + word);
										}
										tokenList.add(word);
										word = "";
										return line;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isWhitespace(line.charAt(index)) | !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index))) {
								if(print) {
									System.out.println("Keyword: " + word);
								}
								tokenList.add(word);
								word = "";
								return line;
							}
						} else if(Character.isLetter(line.charAt(index))){
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else {
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								String nextLine = this.getNextLine();
								if(nextLine != null) {
									line = nextLine;
									index = 0;
								} else {
									return line;
								}
							}
							break;
						}
						break;
					case 'i':
						if(word.equals("")) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
								if(Character.isLetter(line.charAt(index)) && line.charAt(index) == 'f') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
										if(Character.isWhitespace(line.charAt(index)) | !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index))) {
											if(print) {
												System.out.println("Keyword: " + word);
											}
											tokenList.add(word);
											word = "";
											return line;
										}
									}
								}
							} else if(index == line.length()-1){
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								return line;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'n') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 't') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()-1){
										if(print) {
											System.out.println("Keyword: " + word);
										}
										tokenList.add(word);
										word = "";
										return line;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isWhitespace(line.charAt(index)) | !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index))) {
								if(print) {
									System.out.println("Keyword: " + word);
								}
								tokenList.add(word);
								word = "";
								return line;
							}
						} else if(Character.isLetter(line.charAt(index))){
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else {
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								String nextLine = this.getNextLine();
								if(nextLine != null) {
									line = nextLine;
									index = 0;
								} else {
									return line;
								}
							}
							break;
						}
						break;
					case 'r':
						if(word.equals("")) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else if(index == line.length()-1){
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								return line;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'e') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 't') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'u') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'r') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'n') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()-1){
										if(print) {
											System.out.println("Keyword: " + word);
										}
										tokenList.add(word);
										word = "";
										return line;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isWhitespace(line.charAt(index)) | !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index))) {
								if(print) {
									System.out.println("Keyword: " + word);
								}
								tokenList.add(word);
								word = "";
								return line;
							}
						} else if(Character.isLetter(line.charAt(index))){
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else {
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								String nextLine = this.getNextLine();
								if(nextLine != null) {
									line = nextLine;
									index = 0;
								} else {
									return line;
								}
							}
							break;
						}
						break;
					case 'v':
						if(word.equals("")) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else if(index == line.length()-1){
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								return line;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'o') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'i') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'd') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()-1){
										if(print) {
											System.out.println("Keyword: " + word);
										}
										tokenList.add(word);
										word = "";
										return line;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isWhitespace(line.charAt(index)) | !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index))) {
								if(print) {
									System.out.println("Keyword: " + word);
								}
								tokenList.add(word);
								word = "";
								return line;
							}
						} else if(Character.isLetter(line.charAt(index))){
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else {
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								String nextLine = this.getNextLine();
								if(nextLine != null) {
									line = nextLine;
									index = 0;
								} else {
									return line;
								}
							}
							break;
						}
						break;
					case 'w':
						if(word.equals("")) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else if(index == line.length()-1){
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								return line;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'h') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'i') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'l') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else {
										break;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isLetter(line.charAt(index))) {
								if(line.charAt(index) == 'e') {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									} else if(index == line.length()-1){
										if(print) {
											System.out.println("Keyword: " + word);
										}
										tokenList.add(word);
										word = "";
										return line;
									}
								} else {
									word += Character.toString(line.charAt(index));
									if(index < line.length()-1) {
										index++;
									}
									break;
								}
							} else {
								break;
							}
							/*-----------------------------------------------------------------------------*/
							if(Character.isWhitespace(line.charAt(index)) | !Character.isLetter(line.charAt(index)) && !Character.isDigit(line.charAt(index))) {
								if(print) {
									System.out.println("Keyword: " + word);
								}
								tokenList.add(word);
								word = "";
								return line;
							}
						} else if(Character.isLetter(line.charAt(index))){
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else {
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								String nextLine = this.getNextLine();
								if(nextLine != null) {
									line = nextLine;
									index = 0;
								} else {
									return line;
								}
							}
							break;
						}
						break;
					default:
						if((Character.isLetter(line.charAt(index)))) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else {
								tokenList.add(word);
								if(print) {
									System.out.println("ID: " + word);
								}
								word = "";
								return line;
							}
						}
						break;
				}
			}
			if(!word.equals("")) {
				tokenList.add(word);
				if(print) {
					System.out.println("ID: " + word);
				}
				word = "";
			}
			return line;
		} //End of handleLetter()
/*===================================================================================================*/	
		/*handleNumber() analyzes the number at the current index, handles it accordingly, and stores the digit or float as a token*/
		public String handleNumber(String line) throws IOException {
			while(Character.isDigit(line.charAt(index))) {
				if(word.equals("")) {
					word += Character.toString(line.charAt(index));
					if(index < line.length()-1) {
						index++;
					} else if(index == line.length()-1){
						tokenList.add(word);
						if(print) {
							System.out.println("Num: " + word);
						}
						word = "";
						return line;
					}
				} else if(Character.isDigit(line.charAt(index))){
					word += Character.toString(line.charAt(index));
					if(index < line.length()-1) {
						index++;
					} else {
						tokenList.add(word);
						if(print) {
							System.out.println("Num: " + word);
						}
						word = "";
						String nextLine = this.getNextLine();
						if(nextLine != null) {
							line = nextLine;
							index = 0;
						} else {
							return line;
						}
					}
				}
			}
			/*--------------------------------Float---------------------------------------*/
			if(index !=0 && line.charAt(index) == '.' && index < line.length()-1 && Character.isDigit(line.charAt(1+index))) {
				word += Character.toString(line.charAt(index));
				index++;
				while(Character.isDigit(line.charAt(index))) {
					word += Character.toString(line.charAt(index));
					if(index < line.length()-1) {
						index++;
					} else if(index == line.length()-1){
						tokenList.add(word);
						if(print) {
							System.out.println("Float: " + word);
						}
						word = "";
						return line;
					}
					/*--------------------------------E----------------------------------*/
					if((line.charAt(index) == 'E' && index < line.length()-1 && Character.isDigit(line.charAt(1+index))) ||
							(line.charAt(index) == 'E' && 1+index < line.length()-1 && (line.charAt(1+index) == '+' || line.charAt(1+index) == '-') && Character.isDigit(line.charAt(2+index)))) {
						word += Character.toString(line.charAt(index));
						index++;
						while(Character.isDigit(line.charAt(index))) {
							word += Character.toString(line.charAt(index));
							if(index < line.length()-1) {
								index++;
							} else if(index == line.length()-1){
								tokenList.add(word);
								if(print) {
									System.out.println("Float: " + word);
								}
								word = "";
								return line;
							}
						}
						if((line.charAt(index) == '+' || line.charAt(index) == '-') && index < line.length()-1 && Character.isDigit(line.charAt(1+index))) {
							word += Character.toString(line.charAt(index));
							index++;
							while(Character.isDigit(line.charAt(index))) {
								word += Character.toString(line.charAt(index));
								if(index < line.length()-1) {
									index++;
								} else if(index == line.length()-1){
									tokenList.add(word);
									if(print) {
										System.out.println("Float: " + word);
									}
									word = "";
									return line;
								}
							}
						}
					}
					/*-------------------------------------------------------------------*/
				}
				if(!word.equals("")) {
					tokenList.add(word);
					if(print) {
						System.out.println("Float: " + word);
					}
					word = "";
				}
				return line;
			}
			/*----------------------------------------------------------------------------------*/
			if(!word.equals("")) {
				tokenList.add(word);
				if(print) {
					System.out.println("Num: " + word);
				}
				word = "";
			}
			return line;
		} //End of handleNumber()
 /*===================================================================================================*/
}
