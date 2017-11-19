/*created by: Jeremiah Akins 10/2016 for Compilers*/
/*   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *
 * The parser/syntactical analyzer class will also do Code Generation as of 11/2016.	 	 *
 *  All code pertaining to Code Generation will be identified by comments.						 *
 *  Code Generation will follow along with the parser.														 *
 *  																																		 *
 *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   */
/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
import java.util.LinkedList; //for expressionList
/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/

public class SyntacticalAnalysis {
	/*flag for printing tokens and input, set to true for printing*/
	private Boolean print = true;
	private String[] tokens;
	private int index = 0;
	private Boolean proceed = true;
	private Boolean epsilon = false;
	private Boolean reject = false;
	private Boolean calledExpression = false;
	/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
	private String[][] codeTable = new String[150][4]; //set table length
	private int codeTableIndex = 0;
	private int codeTableIndexTemp = 0;
	private int paramCount = 0;
	private String functionName = "";
	private int tempCount = 0;
	//private Boolean didMulop = false;
	private int expressionStart = 0;
	private int expressionEnd = 0;
	private Boolean expressionRecursion = false;
	private LinkedList<String> expressionList = new LinkedList<String>(); //a list for all tokens in an expression
	private LinkedList<String> backPatchIndex = new LinkedList<String>(); //a list of indexes for back patching in the code table
	private LinkedList<String> whileLoopStart = new LinkedList<String>(); //a list of indexes for back patching in the code table
	/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/*Constructor*/
	public SyntacticalAnalysis() {
	} //End of Constructor
/*===================================================================================================*/	
	/*isID() verifies that the current token is an ID*/
	public Boolean isID(String token) {
		if(!tokens[index].equals("int") && !tokens[index].equals("float") && !tokens[index].equals("if") && !tokens[index].equals("else") && 
				!tokens[index].equals("return") && !tokens[index].equals("while") && !tokens[index].equals("void")) {
			for(int i = 0; i < tokens[index].length(); i++) {
				if(Character.isLetter(tokens[index].charAt(i))) {
				} else {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}//End of isID()
/*===================================================================================================*/	
	/*isNum() verifies that the current token is a digit*/
	public Boolean isNum(String token) {
		if(!tokens[index].equals("int") && !tokens[index].equals("float") && !tokens[index].equals("if") && !tokens[index].equals("else") && 
				!tokens[index].equals("return") && !tokens[index].equals("while") && !tokens[index].equals("void")) {
			for(int i = 0; i < tokens[index].length(); i++) {
				if(Character.isDigit(tokens[index].charAt(i))) {
				} else {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}//End of isNum()
/*===================================================================================================*/	
	/*isFloat() verifies that the current token is a floating point number*/
	public Boolean isFloat(String token) {
		if(!tokens[index].equals("int") && !tokens[index].equals("float") && !tokens[index].equals("if") && !tokens[index].equals("else") && 
				!tokens[index].equals("return") && !tokens[index].equals("while") && !tokens[index].equals("void")) {
			if(tokens[index].length() >= 3 && tokens[index].contains(".")) {
			} else {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}//End of isFloat()
/*===================================================================================================*/	
	public void parseTokens(String[] tokens) {
		this.tokens = tokens;
		//prints each token
		/*for(int i = index; i < this.tokens.length; i++) {
			if(print)  {
				System.out.println(this.tokens[i]);
			}
		}*/
		if(tokens.length != 0) {
			this.declarationList();
		}
		if(!reject && print) {
			System.out.println("ACCEPT");
		}
		if(reject && print) {
			reject = false;
			System.out.println("REJECT");
		}
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		this.printCodeTable();
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	}//End of parseTokens()
/*===================================================================================================*/	
	public void declarationList() {
		if(proceed && !epsilon) {
			this.declaration();
			if(index == tokens.length) {
				epsilon = true;
			}
			this.declarationList();
		} else if (proceed && epsilon) {
			epsilon = false;
		} else if(!proceed) {
			reject = true;
		}
	}//End of declarationList()
/*===================================================================================================*/	
	public void declaration() {
		if(2+index < tokens.length) {
			if(!tokens[index].equals("void") && (tokens[2+index].equals(";") || tokens[2+index].equals("["))) {
				this.varDeclaration();
			} else {
				this.funDeclaration();
			}
		} else {
			proceed = false;
		}
	}//End of declaration()
/*===================================================================================================*/	
	public void varDeclaration() {
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		codeTable[codeTableIndex][0] = "ALLOC";
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		this.typeSpecifier();
		if(1+index < tokens.length && this.isID(tokens[index]) && tokens[1+index].equals(";")) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][3] = tokens[index];
			codeTable[codeTableIndex][1] = "4";
			codeTableIndex++; //increment to the next row
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			index = index+2;
		} else if(4+index < tokens.length && this.isID(tokens[index]) && tokens[1+index].equals("[")) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][3] = tokens[index];
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			index = index+2;
			if(this.isNum(tokens[index]) && tokens[1+index].equals("]") && tokens[2+index].equals(";")) {
				/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
				codeTable[codeTableIndex][1] = Integer.toString(Integer.parseInt(tokens[index]) * 4);
				codeTableIndex++; //increment to the next row
				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
				index = index+3;
			} else {
				proceed = false;
			}
		} else {
			proceed = false;
		}
	}//End of varDeclaration()
/*===================================================================================================*/	
	public void typeSpecifier() {
		if(tokens[index].equals("int") || tokens[index].equals("float") || tokens[index].equals("void")) {
			index++;
		} else {
			proceed = false;
		}
	}//End of typeSpecifier()
/*===================================================================================================*/	
	public void funDeclaration() {
		this.typeSpecifier();
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		codeTable[codeTableIndex][0] = "FUNCT";
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		if(1+index < tokens.length && this.isID(tokens[index]) && tokens[1+index].equals("(")) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][3] = tokens[index];
			functionName = tokens[index];
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			index = index+2;
			this.params();
			if(1+index < tokens.length && tokens[index].equals(")") && proceed){
				index++;
				this.compoundStmt();
			} else {
				proceed = false;
			}
		} else {
			proceed = false;
		}
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		codeTable[codeTableIndex][0] = "END";
		codeTable[codeTableIndex][3] = functionName;
		functionName = ""; //reset functionName
		codeTableIndex++;
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	}//End of funDeclaration()
/*===================================================================================================*/	
	public void params() {
		if(!tokens[index].equals("void") && proceed) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTableIndexTemp = codeTableIndex;
			codeTableIndex++; //increment to the next row, so that the previous index is reserved for when the number are parameters are counted
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			this.paramList();
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndexTemp][1] = Integer.toString(paramCount);
			paramCount = 0; //reset function parameter counter
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		} else if(tokens[index].equals("void")) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][1] = "0";
			codeTableIndex++; //increment to the next row, the name of the function has been passed at this point (the function name goes in the last index of the row)
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			index++;
		} else {
			proceed = false;
		}
	}//End of params()
/*===================================================================================================*/	
	public void paramList() {
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		paramCount++;
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		this.param();
		this.paramListPrime();
	}//End of paramList()
/*===================================================================================================*/	
	public void paramListPrime() {
		if(index < tokens.length && tokens[index].equals(",")) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			paramCount++;
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			index++;
			this.param();
			this.paramListPrime();
		}
	}//End of paramListPrime()
/*===================================================================================================*/	
	public void param() {
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		codeTable[codeTableIndex][0] = "ALLOC";
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		this.typeSpecifier();
		if(1+index < tokens.length && this.isID(tokens[index]) && !tokens[1+index].equals("[")) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][3] = tokens[index];
			codeTable[codeTableIndex][1] = "4";
			codeTableIndex++; //increment to the next row
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			index++;
		} else if(2+index < tokens.length && this.isID(tokens[index]) && tokens[1+index].equals("[") && tokens[2+index].equals("]")) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][3] = tokens[index];
			codeTable[codeTableIndex][1] = "4";
			codeTableIndex++; //increment to the next row
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			index = 3+index;
		} else {
			proceed = false;
		}
	}//End of param()
/*===================================================================================================*/	
	public void compoundStmt() {
		if(index < tokens.length && tokens[index].equals("{") && proceed) {
			index++;
			this.localDeclarations();
			this.statementList();
			if(index < tokens.length && tokens[index].equals("}")) {
				index++;
			}
		} else {
			proceed = false;
		}
	}//End of compoundStmt()
/*===================================================================================================*/	
	public void localDeclarations() {
		if(index < tokens.length && (tokens[index].equals("int") || tokens[index].equals("float")) && proceed) {
			this.varDeclaration();
			this.localDeclarations();
		}
	}//End of localDeclarations()
/*===================================================================================================*/	
	public void statementList() {
		if(index < tokens.length && !tokens[index].equals("}") && proceed) {
			this.statement();		
			this.statementList();
		}
	}//End of statementList()
/*===================================================================================================*/	
	public void statement() {
		if(this.isID(tokens[index]) || tokens[index].equals("(") && proceed) {
			this.expressionStmt();
		} else if(tokens[index].equals("{") && proceed) {
			this.compoundStmt();
		} else if(tokens[index].equals("if") && proceed) {
			this.selectionStmt();
		} else if(tokens[index].equals("while") && proceed) {
			this.iterationStmt();
		} else if(tokens[index].equals("return") && proceed) {
			this.returnStmt();
		} else {
			proceed = false;
		}
	}//End of statement()
/*===================================================================================================*/	
	public void expressionStmt() {
		if(this.isID(tokens[index])) {
			this.expression();
			if(index < tokens.length && tokens[index].equals(";") && proceed) {
					index++;
			} else {
				proceed = false;
			}
		} else if(index < tokens.length && tokens[index].equals(";") && proceed) {
			index++;
		} else {
			proceed = false;
		}
	}//End of expressionStmt()
/*===================================================================================================*/	
	public void selectionStmt() {
		if(tokens[index].equals("if")) {
			index++;
			if(tokens[index].equals("(")) {
				index++;
			} else {
				proceed = false;
			}
			this.expression();
			if(tokens[index].equals(")")) {
				index++;
			} else {
				proceed = false;
			}
			this.statement();
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[Integer.parseInt(backPatchIndex.getLast())][3] = Integer.toString(codeTableIndex); //branch around if statement
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			if(tokens[index].equals("else")) {
				index++;
				/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
				codeTable[Integer.parseInt(backPatchIndex.getLast())][3] = Integer.toString(codeTableIndex+1); //skip a row to leave space for the branch around else statement
				backPatchIndex.removeLast();
				codeTable[codeTableIndex][0] = "BR";
				codeTable[codeTableIndex][3] = "?";				
				backPatchIndex.add(Integer.toString(codeTableIndex));
				codeTableIndex++; //increment to next row
				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
				this.statement();
				/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
				codeTable[Integer.parseInt(backPatchIndex.getLast())][3] = Integer.toString(codeTableIndex); //branch around if statement
				backPatchIndex.removeLast();
				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			} else {
				/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
				backPatchIndex.removeLast();
				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			}
		} else {
			proceed = false;
		}
	}//End of selectionStmt()
/*===================================================================================================*/	
	public void iterationStmt() {
		if(tokens[index].equals("while") && tokens[1+index].equals("(")) {
			index = index+2;
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			whileLoopStart.add(Integer.toString(codeTableIndex));
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			this.expression();
			if(tokens[index].equals(")")) {
				index++;
				this.statement();
				/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
				codeTable[codeTableIndex][0] = "BR";
				codeTable[codeTableIndex][3] = whileLoopStart.getLast();
				whileLoopStart.removeLast();
				codeTableIndex++; //increment to next row
				codeTable[Integer.parseInt(backPatchIndex.getLast())][3] = Integer.toString(codeTableIndex);
				backPatchIndex.removeLast();
				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			} else {
				proceed = false;
			}
		} else {
			proceed = false;
		}
	}//End of iterationStmt(
/*===================================================================================================*/	
	public void returnStmt() {
		if(1+index < tokens.length && tokens[index].equals("return") && tokens[1+index].equals(";")) {
			index = index+2;
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][0] = "RET";
			codeTableIndex++; //increment to the next row
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		} else if(tokens[index].equals("return")) {
			index++;
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			String tempTemp = ("t" + Integer.toString(tempCount)); //temporary variable to use to check if return value is a single token
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			this.expression();
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			codeTable[codeTableIndex][0] = "RET";
			if(!tempTemp.equals(("t" + Integer.toString(tempCount)))) { //check if temporary value has been altered, if so, then the value is not old and an expression was returned
				codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount)); //store the new temporary in the code table
			} else {
				codeTable[codeTableIndex][3] = tokens[index-1]; //the return value must have been a single token, so store that token in the code table
				expressionList.clear();
			}
			codeTableIndex++; //increment to the next row
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			if(tokens[index].equals(";")) {
				index++;
			} else {
				proceed = false;
			}
		} else {
			proceed = false;
		}
	}//End of returnStatement()
/*===================================================================================================*/	
	public void expression() {
		if(tokens[index].equals("(") || this.isNum(tokens[index]) || this.isFloat(tokens[index]) || (1+index < tokens.length && tokens[1+index].equals("("))) {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			if(!expressionRecursion) {
				expressionStart = index;
			}
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			this.simpleExpression();
		} else {
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			if(!expressionRecursion) {
				expressionStart = index;
			}
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			this.var();
			if(index < tokens.length && tokens[index].equals("=") && proceed) {
				index++;
				/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
				expressionRecursion = true;
				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
				this.expression();
				/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
				expressionRecursion = false; //reset flag
				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			} else if(!tokens[index].equals(";")) {
				this.simpleExpression();
			}
		}
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		if(tokens[index].equals(")") && tokens[index+1].equals("{") || tokens[index].equals(";") && tokens[index-1].equals(")")) { // "||" was added to satisfy error with returning a function call - 11/27/2016
			expressionRecursion = false;
		}
		if(!expressionRecursion && expressionStart != 0) { // added "expressionStart != 0" to resolve out of bounds error - 11/26/2016
		expressionEnd = index-1;
		this.orderOfOperations(expressionStart, expressionEnd);
		expressionStart = 0; //reset
		expressionEnd = 0; //reset
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	}//End of expression()
/*===================================================================================================*/	
	public void var() {
		if(this.isID(tokens[index]) && proceed) {
			index++;
			if(tokens[index].equals("[")) {
					index++;
					calledExpression = true;
					/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
					if(expressionStart != 0) {
						expressionRecursion = true;
					}
					/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
					this.expression();
			}
			if(tokens[index].equals("]") && calledExpression) {
				calledExpression = false;
				index++;
			}		
		} else {
			proceed = false;
		}
	}//End of var()
/*===================================================================================================*/	
	public void simpleExpression() {
		if(index < tokens.length && (tokens[index].equals(";") || (!tokens[index].equals("<=") && !tokens[index].equals("<") && 
				!tokens[index].equals(">") && !tokens[index].equals(">=") && !tokens[index].equals("==") && !tokens[index].equals("!=")))) {
			this.additiveExpression();
			//added11/26/2016 to resolve issue with parsing
			if(index < tokens.length && (!tokens[index].equals(";") && (tokens[index].equals("<=") || tokens[index].equals("<") || 
					tokens[index].equals(">") || tokens[index].equals(">=") || tokens[index].equals("==") || tokens[index].equals("!=")))) {
				this.relop();
				this.additiveExpression();
			}
		} else {
			this.additiveExpression();
			this.relop();
			this.additiveExpression();
		}
	}//End of simpleExpression()
/*===================================================================================================*/	
	public void relop() {
		if(tokens[index].equals("<=") && proceed) {
			index++;
		} else if(tokens[index].equals("<") && proceed) {
			index++;
		} else if(tokens[index].equals(">") && proceed) {
			index++;
		} else if(tokens[index].equals(">=") && proceed) {
			index++;
		} else if(tokens[index].equals("==") && proceed) {
			index++;
		} else if(tokens[index].equals("!=") && proceed) {
			index++;
		} else {
			proceed = false;
		}
	}//End of relop()
/*===================================================================================================*/	
	public void additiveExpression() {
		this.term();
		this.additiveExpressionPrime();
	}//End of additiveExpression()
/*===================================================================================================*/	
	public void additiveExpressionPrime() {
		if (proceed && tokens[index].equals("+") || proceed && tokens[index].equals("-")) {
			this.addop();
			this.term();
			this.additiveExpressionPrime();
		}
	}//End of additiveExpressionPrime()
/*===================================================================================================*/	
	public void addop() {
		if(tokens[index].equals("+") || tokens[index].equals("-")) {
			index++;
		} else {
			proceed = false;
		}
	}//End of addop()
/*===================================================================================================*/	
	public void term() {
		this.factor();
		this.termPrime();
	}//End of term()
/*===================================================================================================*/	
	public void termPrime() {
		if(tokens[index].equals("*") && proceed || tokens[index].equals("/") && proceed) {
			this.mulop();
			this.factor();
			this.termPrime();
		}
	}//End of termPrime()
/*===================================================================================================*/	
	public void mulop() {
		if(tokens[index].equals("*") || tokens[index].equals("/")) {
			index++;
		} else {
			proceed = false;
		}
	}//End of mulop()
/*===================================================================================================*/	
	public void factor() {
		if(index < tokens.length && tokens[index].equals("(") && proceed) {
			index++;
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			if(expressionStart != 0) {
				expressionRecursion = true;
			}
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			this.expression();
			if(index < tokens.length && tokens[index].equals(")")) {
				index++;
			} else {
				proceed = false;
			}
		} else if(this.isID(tokens[index]) && proceed) {
			if(1+index < tokens.length && tokens[1+index].equals("(")) {
				this.call();
			} else {
				this.var();
			}
		} else if(this.isNum(tokens[index]) && proceed) {
			index++;
		} else if(this.isFloat(tokens[index]) && proceed) {
			index++;
		} else if(tokens[index-1].equals("]") || (this.isID(tokens[index--])) && proceed) {
			//Do nothing except increment index  back to where it was because var should already been handled.
			if(!tokens[index-1].equals("]")) {
				index++;
			}
		}
		else {
			proceed = false;
		}
	}//End of factor()
/*===================================================================================================*/	
	public void call() {
		index++; // isID() was confirmed prior to being called
		if(tokens[index].equals("(")) {
			index++;
			this.args();
		} else {
			proceed = false;
		}
		if(tokens[index].equals(")")) {
			index++;
		} else {
			proceed = false;
		}
	}//End of call()
/*===================================================================================================*/	
	public void args() {
		if(!tokens[index].equals(")")) {
			this.argsList();
		}
	}//End of args()
/*===================================================================================================*/	
	public void argsList() {
		/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
		if(expressionStart != 0) {
			expressionRecursion = true;
		}
		/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
		this.expression();
		this.argsListPrime();
	}//End of argsList()
/*===================================================================================================*/	
	public void argsListPrime() {
		if(tokens[index].equals(",")) {
			index++;
			/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
			if(expressionStart != 0) {
				expressionRecursion = true;
			}
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
			this.expression();
			this.argsListPrime();
		}
	}//End of argsListPrime()
/*===================================================================================================*/	
	/*------------------------------------------------------------Code Generation----------------------------------------------------------------------------*/
	public void printCodeTable() {
		for(int i = 0; i < codeTable.length; i++) {
			System.out.format(" %1$03d      %2$-15s %3$-15s %4$-15s %5$-15s\n", i, codeTable[i][0], codeTable[i][1], codeTable[i][2], codeTable[i][3]);
		}
	}//End of printCodeTable()
/*===================================================================================================*/	
	public Boolean codeTableContains(String comparand) {
		for(int i = 0; i < codeTable.length; i++) {
			for(int j =0; j < 4; j++) {
				if(codeTable[i][0] == null) {	//for efficiency, so that unnecessary checking doesn't occur. 
					return false;
				} else if(codeTable[i][j].equals(comparand)) {
					return true;
				}
			}
		}
		return false;
	}//End of codeTableContains()
/*===================================================================================================*/	
	public void orderOfOperations(int expressionStart, int expressionEnd) {
		int index = -1; //expressionList index
		int subStartIndex = 0; //index of where "(" started in the expressionList
		//String buffer = "";
		String arrayID = "";
		LinkedList<String> subList = new LinkedList<String>(); //sub list for parenthesis
		for(int i = expressionStart; i <= expressionEnd; i++) { //add tokens in expression to expressionList
			if(tokens[i+1].equals("[")) { //for arrays
				arrayID = tokens[i];
				i = i+2;
				while(!tokens[i].equals("]")) {
					subList.add(tokens[i]);
					i++;
				}
				
				
				while(subList.indexOf("*") != -1) { //Multiplication
					index = subList.indexOf("*");
					codeTable[codeTableIndex][0] = "MULT";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				while(subList.indexOf("/") != -1) { //Division
					index = subList.indexOf("/");
					codeTable[codeTableIndex][0] = "DIV";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				while(subList.indexOf("+") != -1) { //Addition
					index = subList.indexOf("+");
					codeTable[codeTableIndex][0] = "ADD";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				while(subList.indexOf("-") != -1) { //Subtraction
					index = subList.indexOf("-");
					codeTable[codeTableIndex][0] = "SUB";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				codeTable[codeTableIndex][0] = "MULT";
				codeTable[codeTableIndex][1] = subList.getFirst();
				codeTable[codeTableIndex][2] = "4";
				tempCount++;
				codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
				codeTableIndex++; //increment to next row
				codeTable[codeTableIndex][0] = "DISP";
				codeTable[codeTableIndex][1] = arrayID;
				codeTable[codeTableIndex][2] = ("t" + Integer.toString(tempCount));
				tempCount++;
				codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
				codeTableIndex++; //increment to next row
				subList.clear(); //clear subList so that it can be used again
				expressionList.add(("t" + Integer.toString(tempCount)));
				
				
			} else {
				expressionList.add(tokens[i]);
			}
		}
		//adds what's in parenthesis to a sub list, removes the items from the expressionList, and adds the calculation back to the expressionList at the index in which the parenthesis started
		while(expressionList.indexOf("(") != -1) { //parenthesis & function calls
			if(expressionList.indexOf("(") != 0 && this.isIdentifier(expressionList.get(expressionList.indexOf("(")-1))) { //if function call then check and handle arguments and replace the index of call with calculated temporary
				int functionNameIndex = expressionList.indexOf("(")-1;
				int argumentCount = 1; //if used, there will be at least 1 argument
				codeTable[codeTableIndex][0] = "CALL";
				codeTable[codeTableIndex][2] = expressionList.get(functionNameIndex);
				expressionList.remove(functionNameIndex);
				expressionList.remove(functionNameIndex);
				if(expressionList.get(functionNameIndex).equals(")")) { //if function call passes no arguments
					expressionList.remove(functionNameIndex);
					codeTable[codeTableIndex][1] = "0"; //no arguments
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					expressionList.add(functionNameIndex, ("t" + Integer.toString(tempCount)));
					codeTableIndex++; //increment to next row
				} else {
					int increment = functionNameIndex; //increment to get through function call arguments
					String functionTemp = ""; //temporary variable to store the current function's temporary value in
					while(!expressionList.get(increment).equals(")")) {
						if(expressionList.get(increment).equals(",")) {
							argumentCount++; //increment the argumentCount
						}
						increment++; //increment to the next token in the argument expression
					}
					codeTable[codeTableIndex][1] = Integer.toString(argumentCount);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					codeTableIndex++; //increment to next row
					functionTemp = ("t" + Integer.toString(tempCount));
					

					while(!expressionList.get(functionNameIndex).equals(")")) { //continue through the arguments until the end
						while(!expressionList.get(functionNameIndex).equals(",") && !expressionList.get(functionNameIndex).equals(")")) { //handle the argument until the next one appears, then clear subList
							subList.add(expressionList.get(functionNameIndex));
							expressionList.remove(functionNameIndex);
						}
						
						
						while(subList.indexOf("*") != -1) { //Multiplication
							index = subList.indexOf("*");
							codeTable[codeTableIndex][0] = "MULT";
							codeTable[codeTableIndex][1] = subList.get(index-1);
							subList.remove(index-1);
							codeTable[codeTableIndex][2] = subList.get(index);
							subList.remove(index);
							tempCount++;
							codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
							subList.remove(index-1);
							subList.add(index-1, ("t" + Integer.toString(tempCount)));		
							codeTableIndex++; //increment to next row
						}
						while(subList.indexOf("/") != -1) { //Division
							index = subList.indexOf("/");
							codeTable[codeTableIndex][0] = "DIV";
							codeTable[codeTableIndex][1] = subList.get(index-1);
							subList.remove(index-1);
							codeTable[codeTableIndex][2] = subList.get(index);
							subList.remove(index);
							tempCount++;
							codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
							subList.remove(index-1);
							subList.add(index-1, ("t" + Integer.toString(tempCount)));		
							codeTableIndex++; //increment to next row
						}
						while(subList.indexOf("+") != -1) { //Addition
							index = subList.indexOf("+");
							codeTable[codeTableIndex][0] = "ADD";
							codeTable[codeTableIndex][1] = subList.get(index-1);
							subList.remove(index-1);
							codeTable[codeTableIndex][2] = subList.get(index);
							subList.remove(index);
							tempCount++;
							codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
							subList.remove(index-1);
							subList.add(index-1, ("t" + Integer.toString(tempCount)));		
							codeTableIndex++; //increment to next row
						}
						while(subList.indexOf("-") != -1) { //Subtraction
							index = subList.indexOf("-");
							codeTable[codeTableIndex][0] = "SUB";
							codeTable[codeTableIndex][1] = subList.get(index-1);
							subList.remove(index-1);
							codeTable[codeTableIndex][2] = subList.get(index);
							subList.remove(index);
							tempCount++;
							codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
							subList.remove(index-1);
							subList.add(index-1, ("t" + Integer.toString(tempCount)));		
							codeTableIndex++; //increment to next row
						}
						codeTable[codeTableIndex][0] = "ARG";
						codeTable[codeTableIndex][3] = subList.getFirst();
						codeTableIndex++; //increment to next row
						subList.clear();
						if(!expressionList.get(functionNameIndex).equals(")")) {
							expressionList.remove(functionNameIndex); //remove "," to get to the next argument
						}
					}
					expressionList.remove(functionNameIndex); //remove ")" to end handling function call arguments and to continue expression
					expressionList.add(functionNameIndex, functionTemp);
					functionTemp = ""; //reset functionTemp
				}
			} else {
				index = expressionList.indexOf("(");
				subStartIndex = index;
				expressionList.remove(index);
				while(!expressionList.get(index).equals(")")) {
					subList.add(expressionList.get(index));
					expressionList.remove(index);
				}
				expressionList.remove(index);
				
				
				while(subList.indexOf("*") != -1) { //Multiplication
					index = subList.indexOf("*");
					codeTable[codeTableIndex][0] = "MULT";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				while(subList.indexOf("/") != -1) { //Division
					index = subList.indexOf("/");
					codeTable[codeTableIndex][0] = "DIV";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				while(subList.indexOf("+") != -1) { //Addition
					index = subList.indexOf("+");
					codeTable[codeTableIndex][0] = "ADD";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				while(subList.indexOf("-") != -1) { //Subtraction
					index = subList.indexOf("-");
					codeTable[codeTableIndex][0] = "SUB";
					codeTable[codeTableIndex][1] = subList.get(index-1);
					subList.remove(index-1);
					codeTable[codeTableIndex][2] = subList.get(index);
					subList.remove(index);
					tempCount++;
					codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
					subList.remove(index-1);
					subList.add(index-1, ("t" + Integer.toString(tempCount)));		
					codeTableIndex++; //increment to next row
				}
				expressionList.add(subStartIndex, subList.getFirst());
				subList.clear();
			}
		}
		
		//standard procedure without parenthesis
		while(expressionList.indexOf("*") != -1) { //Multiplication
			index = expressionList.indexOf("*");
			codeTable[codeTableIndex][0] = "MULT";
			codeTable[codeTableIndex][1] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTable[codeTableIndex][2] = expressionList.get(index);
			expressionList.remove(index);
			tempCount++;
			codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
			expressionList.remove(index-1);
			expressionList.add(index-1, ("t" + Integer.toString(tempCount)));		
			codeTableIndex++; //increment to next row
		}
		while(expressionList.indexOf("/") != -1) { //Division
			index = expressionList.indexOf("/");
			codeTable[codeTableIndex][0] = "DIV";
			codeTable[codeTableIndex][1] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTable[codeTableIndex][2] = expressionList.get(index);
			expressionList.remove(index);
			tempCount++;
			codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
			expressionList.remove(index-1);
			expressionList.add(index-1, ("t" + Integer.toString(tempCount)));		
			codeTableIndex++; //increment to next row
		}
		while(expressionList.indexOf("+") != -1) { //Addition
			index = expressionList.indexOf("+");
			codeTable[codeTableIndex][0] = "ADD";
			codeTable[codeTableIndex][1] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTable[codeTableIndex][2] = expressionList.get(index);
			expressionList.remove(index);
			tempCount++;
			codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
			expressionList.remove(index-1);
			expressionList.add(index-1, ("t" + Integer.toString(tempCount)));		
			codeTableIndex++; //increment to next row
		}
		while(expressionList.indexOf("-") != -1) { //Subtraction
			index = expressionList.indexOf("-");
			codeTable[codeTableIndex][0] = "SUB";
			codeTable[codeTableIndex][1] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTable[codeTableIndex][2] = expressionList.get(index);
			expressionList.remove(index);
			tempCount++;
			codeTable[codeTableIndex][3] = ("t" + Integer.toString(tempCount));
			expressionList.remove(index-1);
			expressionList.add(index-1, ("t" + Integer.toString(tempCount)));		
			codeTableIndex++; //increment to next row
		}
		if(expressionList.indexOf("=") != -1) { //Assignment
			index = expressionList.indexOf("=");
			codeTable[codeTableIndex][0] = "ASSGN";
			expressionList.remove(index);
			codeTable[codeTableIndex][1] = expressionList.get(index);
			expressionList.remove(index);
			codeTable[codeTableIndex][3] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTableIndex++; //increment to next row
		}
		if(expressionList.indexOf("<=") != -1) { //Comparisons
			index = expressionList.indexOf("<=");
			codeTable[codeTableIndex][0] = "COMP";
			expressionList.remove(index);
			codeTable[codeTableIndex][1] = expressionList.get(index);
			expressionList.remove(index);
			codeTable[codeTableIndex][3] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTableIndex++; //increment to next row
			
			codeTable[codeTableIndex][0] = "BLE";
			codeTable[codeTableIndex][3] = Integer.toString(codeTableIndex+2);
			codeTableIndex++; //increment to next row
			codeTable[codeTableIndex][0] = "BR";
			codeTable[codeTableIndex][3] = "?";
			backPatchIndex.add(Integer.toString(codeTableIndex));
			codeTableIndex++; //increment to next row
		} else if(expressionList.indexOf("<") != -1) { //Comparisons
			index = expressionList.indexOf("<");
			codeTable[codeTableIndex][0] = "COMP";
			expressionList.remove(index);
			codeTable[codeTableIndex][1] = expressionList.get(index);
			expressionList.remove(index);
			codeTable[codeTableIndex][3] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTableIndex++; //increment to next row
			
			codeTable[codeTableIndex][0] = "BLT";
			codeTable[codeTableIndex][3] = Integer.toString(codeTableIndex+2);
			codeTableIndex++; //increment to next row
			codeTable[codeTableIndex][0] = "BR";
			codeTable[codeTableIndex][3] = "?";
			backPatchIndex.add(Integer.toString(codeTableIndex));
			codeTableIndex++; //increment to next row
		} else if(expressionList.indexOf(">") != -1) { //Comparisons
			index = expressionList.indexOf(">");
			codeTable[codeTableIndex][0] = "COMP";
			expressionList.remove(index);
			codeTable[codeTableIndex][1] = expressionList.get(index);
			expressionList.remove(index);
			codeTable[codeTableIndex][3] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTableIndex++; //increment to next row
			
			codeTable[codeTableIndex][0] = "BGT";
			codeTable[codeTableIndex][3] = Integer.toString(codeTableIndex+2);
			codeTableIndex++; //increment to next row
			codeTable[codeTableIndex][0] = "BR";
			codeTable[codeTableIndex][3] = "?";
			backPatchIndex.add(Integer.toString(codeTableIndex));
			codeTableIndex++; //increment to next row
		} else if(expressionList.indexOf(">=") != -1) { //Comparisons
			index = expressionList.indexOf(">=");
			codeTable[codeTableIndex][0] = "COMP";
			expressionList.remove(index);
			codeTable[codeTableIndex][1] = expressionList.get(index);
			expressionList.remove(index);
			codeTable[codeTableIndex][3] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTableIndex++; //increment to next row
			
			codeTable[codeTableIndex][0] = "BGE";
			codeTable[codeTableIndex][3] = Integer.toString(codeTableIndex+2);
			codeTableIndex++; //increment to next row
			codeTable[codeTableIndex][0] = "BR";
			codeTable[codeTableIndex][3] = "?";
			backPatchIndex.add(Integer.toString(codeTableIndex));
			codeTableIndex++; //increment to next row
		} else if(expressionList.indexOf("==") != -1) { //Comparisons
			index = expressionList.indexOf("==");
			codeTable[codeTableIndex][0] = "COMP";
			expressionList.remove(index);
			codeTable[codeTableIndex][1] = expressionList.get(index);
			expressionList.remove(index);
			codeTable[codeTableIndex][3] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTableIndex++; //increment to next row
			
			codeTable[codeTableIndex][0] = "BEQ";
			codeTable[codeTableIndex][3] = Integer.toString(codeTableIndex+2);
			codeTableIndex++; //increment to next row
			codeTable[codeTableIndex][0] = "BR";
			codeTable[codeTableIndex][3] = "?";
			backPatchIndex.add(Integer.toString(codeTableIndex));
			codeTableIndex++; //increment to next row
		} else if(expressionList.indexOf("!=") != -1) { //Comparisons
			index = expressionList.indexOf("!=");
			codeTable[codeTableIndex][0] = "COMP";
			expressionList.remove(index);
			codeTable[codeTableIndex][1] = expressionList.get(index);
			expressionList.remove(index);
			codeTable[codeTableIndex][3] = expressionList.get(index-1);
			expressionList.remove(index-1);
			codeTableIndex++; //increment to next row
			
			codeTable[codeTableIndex][0] = "BNE";
			codeTable[codeTableIndex][3] = Integer.toString(codeTableIndex+2);
			codeTableIndex++; //increment to next row
			codeTable[codeTableIndex][0] = "BR";
			codeTable[codeTableIndex][3] = "?";
			backPatchIndex.add(Integer.toString(codeTableIndex));
			codeTableIndex++; //increment to next row
		}
	}//End of codeTableContains()
/*===================================================================================================*/	
	/*isIdentifier() verifies that the token passed in is an Identifier*/
	public Boolean isIdentifier(String token) {
		if(!token.equals("int") && !token.equals("float") && !token.equals("if") && !token.equals("else") && 
				!token.equals("return") && !token.equals("while") && !token.equals("void")) {
			for(int i = 0; i < token.length(); i++) {
				if(Character.isLetter(token.charAt(i))) {
				} else {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}//End of isIdentifier()
/*===================================================================================================*/	
	/*--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
}
