/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author jcamelon
 *
 */

public class Scanner implements IScanner {

	public IScanner initialize(Reader reader, String filename) {
		init(reader, filename);
		return this;
	}

	protected void init(Reader reader, String filename) {
		try {
			//this is a hack to get around a sudden EOF experience
			contextStack.push(
						new ScannerContext().initialize(
						new StringReader("\n"),
						START,
						ScannerContext.SENTINEL));

			if (filename == null)
				contextStack.push( new ScannerContext().initialize(reader, TEXT, ScannerContext.TOP ) ); 
			else
				contextStack.push( new ScannerContext().initialize(reader, filename, ScannerContext.TOP ) );
		} catch( ScannerException se ) {
			//won't happen since we aren't adding an include or a macro
		} 
	}

	public Scanner() {
	}

	protected Scanner(Reader reader, String filename, Hashtable defns) {
		initialize(reader, filename);
		definitions = defns;
	}



	public void addIncludePath(String includePath) {
		includePathNames.add(includePath);
		includePaths.add( new File( includePath ) );
	}

	public void overwriteIncludePath(List newIncludePaths) {
		includePathNames = null;
		includePaths = null; 
		includePathNames = new ArrayList();
		includePaths = new ArrayList(); 
		includePathNames.addAll(newIncludePaths);
		
		Iterator i = includePathNames.iterator(); 
		while( i.hasNext() )
		{
			String path = (String) i.next();
			includePaths.add( new File( path )); 
		}
		
		
	}

	public void addDefinition(String key, IMacroDescriptor macro) {
		definitions.put(key, macro);
	}

	public void addDefinition(String key, String value) {
		definitions.put(key, value);
	}

	public final Object getDefinition(String key) {
		return definitions.get(key);
	}

	public final Object[] getIncludePaths() {
		return includePathNames.toArray();
	}

	protected boolean skipOverWhitespace() throws ScannerException {
		int c = getChar();
		boolean result = false; 
		while ((c != NOCHAR) && ((c == ' ') || (c == '\t')))
		{
			c = getChar();
			result = true; 
		}
		if (c != NOCHAR)
			ungetChar(c);
		return result; 

	}

	protected String getRestOfPreprocessorLine() throws ScannerException {
		StringBuffer buffer = new StringBuffer();
		skipOverWhitespace();
		int c = getChar();

		while (true) {
			while ((c != '\n')
				&& (c != '\r')
				&& (c != '\\')
				&& (c != '/')
				&& (c != NOCHAR)) {
				buffer.append((char) c);
				c = getChar();
			}
			if (c == '/') {
				// we need to peek ahead at the next character to see if 
				// this is a comment or not
				int next = getChar();
				if (next == '/') {
					// single line comment
					skipOverTextUntilNewline();
					break;
				} else if (next == '*') {
					// multiline comment
					if (skipOverMultilineComment())
						break;
					else
						c = getChar();
					continue;
				} else {
					// we are not in a comment
					buffer.append((char) c);
					c = next;
					continue;
				}
			} else {
				if (c != '\\') {
					ungetChar(c);
				} else {
					c = getChar();
				}
				break;
			}
		}

		return buffer.toString();
	}

	protected void skipOverTextUntilNewline() {
		for (;;) {
			switch (getChar()) {
				case NOCHAR :
				case '\n' :
					return;
				case '\\' :
					getChar();
			}
		}
	}

	private void setCurrentToken(Token t) {
		if (currentToken != null)
			currentToken.setNext(t);
		currentToken = t;
	}

	protected void resetStorageBuffer()
	{
		if( storageBuffer != null ) 
			storageBuffer = null; 
	}

	protected Token newToken(int t, String i, IScannerContext c) {
		setCurrentToken(new Token(t, i, c));
		return currentToken;
	}

	protected Token newToken(int t, String i) {
		setCurrentToken(new Token(t, i));
		return currentToken;
	}
	
	protected String getNextIdentifier() throws ScannerException {
		StringBuffer buffer = new StringBuffer();
		skipOverWhitespace();
		int c = getChar();

		if (((c >= 'a') && (c <= 'z'))
			|| ((c >= 'A') && (c <= 'Z')) | (c == '_')) {
			buffer.append((char) c);

			c = getChar();
			while (((c >= 'a') && (c <= 'z'))
				|| ((c >= 'A') && (c <= 'Z'))
				|| ((c >= '0') && (c <= '9'))
				|| (c == '_')) {
				buffer.append((char) c);
				c = getChar();
			}
		}
		ungetChar(c);

		return buffer.toString();
	}

	protected void handleInclusion(String fileName, boolean useIncludePaths ) throws ScannerException {

		if( useIncludePaths ) // search include paths for this file
		{
			// iterate through the include paths 
			Iterator iter = includePaths.iterator();
	
			while (iter.hasNext()) {
	
				File pathFile = (File)iter.next();
				if (pathFile.isDirectory()) {
					String newPath = pathFile.getPath() + File.separatorChar + fileName;
	
					File includeFile = new File(newPath);
	
					if (includeFile.exists() && includeFile.isFile()) {
						try {
							FileReader inclusionReader =
								new FileReader(includeFile);
							contextStack.updateContext(inclusionReader, newPath, ScannerContext.INCLUSION );
							return;
						} catch (FileNotFoundException fnf) {
							// do nothing - check the next directory
						}
					}
				}
			}
		}
		else // local inclusion
		{
			String currentFilename = contextStack.getCurrentContext().getFilename(); 
			File currentIncludeFile = new File( currentFilename );
			String parentDirectory = currentIncludeFile.getParent();
			currentIncludeFile = null; 
			String fullPath = parentDirectory + File.separatorChar + fileName;
			File includeFile = new File( fullPath );
			if (includeFile.exists() && includeFile.isFile()) {
				try {
					FileReader inclusionReader =
						new FileReader(includeFile);
					contextStack.updateContext(inclusionReader, fullPath, ScannerContext.INCLUSION );
					return;
				} catch (FileNotFoundException fnf) {
					if (throwExceptionOnInclusionNotFound)
						throw new ScannerException("Cannot find inclusion " + fileName);	
				}
			}
		}
		
		if (throwExceptionOnInclusionNotFound)
			throw new ScannerException("Cannot find inclusion " + fileName);
	}

	// constants
	private static final int NOCHAR = -1;

	private static final String TEXT = "<text>";
	private static final String START = "<initial reader>";
	private static final String EXPRESSION = "<expression>";
	private static final String PASTING = "<pasting>";
	private static final String BAD_PP =
		"Invalid preprocessor directive encountered at offset ";
	private static final String DEFINED = "defined";
	private static final String POUND_DEFINE = "#define ";

	private ContextStack contextStack = new ContextStack();
	private IScannerContext lastContext = null;
	
	private List includePathNames = new ArrayList();
	private List includePaths = new ArrayList();
	private Hashtable definitions = new Hashtable();
	private StringBuffer storageBuffer = null; 
	
	private int count = 0;
	private static HashMap keywords = new HashMap();
	private static HashMap ppDirectives = new HashMap();

	private Token currentToken = null;

	private boolean passOnToClient = true; 
	private BranchTracker branches = new BranchTracker();

	// these are scanner configuration aspects that we perhaps want to tweak
	// eventually, these should be configurable by the client, but for now
	// we can just leave it internal
	private boolean throwExceptionOnBadPreprocessorSyntax = true;
	private boolean throwExceptionOnInclusionNotFound = true;
	private boolean throwExceptionOnBadMacroExpansion = true;
	private boolean throwExceptionOnUnboundedString = true;
	private boolean throwExceptionOnEOFWithinMultilineComment = true;
	private boolean throwExceptionOnEOFWithoutBalancedEndifs = true;
	private boolean throwExceptionOnBadCharacterRead = false; 
	private boolean atEOF = false;

	private boolean tokenizingMacroReplacementList = false;
	public void setTokenizingMacroReplacementList( boolean mr ){
		tokenizingMacroReplacementList = mr;
	}
	
	private boolean quickScan = false;
	public void setQuickScan(boolean qs) {
		quickScan = qs;
	}

	private IParserCallback callback;
	public void setCallback(IParserCallback c) {
		callback = c;
	}

	private int getChar()
	{
		return getChar( false );
	}

	private int getChar( boolean insideString ) {
		int c = NOCHAR;
		
		lastContext = contextStack.getCurrentContext();
		
		if (contextStack.getCurrentContext() == null)
			// past the end of file
			return c;

		boolean done;
		do {
			done = true;

			if (contextStack.getCurrentContext().undoStackSize() != 0 ) {
				c = contextStack.getCurrentContext().popUndo();
			} else {
				try {
					c = contextStack.getCurrentContext().read();
					if (c == NOCHAR) {
						if (contextStack.rollbackContext() == false) {
							c = NOCHAR;
							break;
						} else {
							done = false;
						}
					}
				} catch (IOException e) {
					if (contextStack.rollbackContext() == false) {
						c = NOCHAR;
					} else {
						done = false;
					}
				}
			}
		} while (!done);

		if( ! insideString )
		{
			if (c == '\\') {
				c = getChar(false);
				if (c == '\r') {
					c = getChar(false);
					if (c == '\n')
						c = getChar(false);
				} else if (c == '\n')
					c = getChar(false);
			}
		}
		return c;
	}

	private void ungetChar(int c) throws ScannerException{
		// Should really check whether there already is a char there
		// If so, we should be using a buffer, instead of a single char
		contextStack.getCurrentContext().pushUndo(c);
		contextStack.undoRollback( lastContext );
	}

	protected boolean lookAheadForTokenPasting() throws ScannerException
	{
		int c = getChar(); 
		if( c == '#' )
		{
			c = getChar(); 
			if( c == '#' )
			{
				return true; 
			}
			else
			{
				ungetChar( c );
			}
		}

		ungetChar( c );
		return false; 

	}

	

	public Token nextToken() throws ScannerException, Parser.EndOfFile {
		return nextToken( true ); 
	}


	protected Token nextToken( boolean pasting ) throws ScannerException, Parser.EndOfFile
	{
	
		count++;
		boolean madeMistake = false; 
		int c = getChar();

		while (c != NOCHAR) {
			if ( ! passOnToClient ) {
				
				int state = 0; 
				
				while (c != NOCHAR && c != '#' ) 
				{
					c = getChar();
					if( c == '/' )
					{
						c = getChar();
						if( c == '/' )
						{
							while (c != '\n' && c != NOCHAR)
								c = getChar();
							continue;
						}
						else if( c == '*' )
						{
							skipOverMultilineComment();
							c = getChar();
							continue;
						}
					}
				}
				
				if( c == NOCHAR ) continue;
			}

			if ((c == ' ') || (c == '\r') || (c == '\t') || (c == '\n')) {
				c = getChar();
				continue;
			} else if (c == '"' || ( c == 'L' && ! madeMistake ) ) {
			
				boolean wideString = false; 
				if( c == 'L' )
				{
					int oldChar =c;
					wideString = true;
					c = getChar(); 
					if( c != '"' )
					{
						// we have made a mistake
						ungetChar( c );
						c = oldChar;
						madeMistake = true; 
						continue;
					}
				} 
				 
				// string
				StringBuffer buff = new StringBuffer(); 
				int beforePrevious = NOCHAR;
				int previous = c;
				c = getChar(true);

				for( ; ; )
				{
					if ( ( c =='"' ) && ( previous != '\\' || beforePrevious == '\\') ) break;
					if( c == NOCHAR) break;  
					buff.append((char) c);
					beforePrevious = previous;
					previous = c;
					c = getChar(true);
				}

				if (c != NOCHAR ) 
				{
					int type = wideString ? Token.tLSTRING : Token.tSTRING;
					return newToken(
						type,
						buff.toString(),
						contextStack.getCurrentContext());
	
				} else {
					if (throwExceptionOnUnboundedString)
						throw new ScannerException(
							"Unbounded string" );
				}
		
			} else if (
				((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z')) | (c == '_')) {
						
				if( madeMistake ) madeMistake = false;
				// String buffer is slow, we need a better way such as memory mapped files
				StringBuffer buff = new StringBuffer(); 
				buff.append((char) c);

				c = getChar();				
				
				while (((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z'))
					|| ((c >= '0') && (c <= '9'))
					|| (c == '_')) {
					buff.append((char) c);
					c = getChar();
				}

				ungetChar(c);

				String ident = buff.toString();

				if (ident.equals(DEFINED)) 
					return newToken(Token.tINTEGER, handleDefinedMacro());
				
				Object mapping = definitions.get(ident);

				if (mapping != null) {
					if( contextStack.shouldExpandDefinition( POUND_DEFINE + ident ) ) {					
						expandDefinition(ident, mapping);
						c = getChar();
						continue;
					}
				}

				Object tokenTypeObject = keywords.get(ident);
				int tokenType = Token.tIDENTIFIER;
				if (tokenTypeObject != null)
					tokenType = ((Integer) tokenTypeObject).intValue();

				if( pasting )
				{
					if( lookAheadForTokenPasting() )
					{
						if( storageBuffer == null )
							storageBuffer = buff;
						else
							storageBuffer.append( ident ); 
							 
						c = getChar(); 
						continue; 	
					}
					else
					{
						if( storageBuffer != null )
						{
							storageBuffer.append( ident );
							contextStack.updateContext( new StringReader( storageBuffer.toString()), PASTING, IScannerContext.MACROEXPANSION );
							storageBuffer = null;  
							c = getChar(); 
							continue;
						}
					}
				}

				return newToken(tokenType, ident, contextStack.getCurrentContext());
			} else if ((c >= '0') && (c <= '9') || c == '.' ) {
				StringBuffer buff;
				
				if( pasting )
				{
					if( storageBuffer != null )
						buff = storageBuffer;
					else
						buff = new StringBuffer();
				}
				else
					buff = new StringBuffer();
				
				
				boolean hex = false;
				boolean floatingPoint = ( c == '.' ) ? true : false;
				boolean firstCharZero = ( c== '0' )? true : false; 
					
				buff.append((char) c);

				c = getChar();
				
				if (c == 'x') {
					if( ! firstCharZero && floatingPoint )
					{
						ungetChar( c ); 
						return newToken( Token.tDOT, ".", contextStack.getCurrentContext() ); 
					}
					else if( ! firstCharZero ) 
						throw new ScannerException( "Invalid Hexidecimal @ offset " + contextStack.getCurrentContext().getOffset() );
					
					hex = true;
					c = getChar();
				}

				while ((c >= '0' && c <= '9')
					|| (hex
						&& ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))) {
					buff.append((char) c);
					c = getChar();
				}
				
				if( c == '.' )
				{
					buff.append( (char)c);
					if( floatingPoint || hex ) 	{
						if( buff.toString().equals( "..") && getChar() == '.' ) 
							return newToken( Token.tELIPSE, "..." ); 
						throw new ScannerException( "Invalid floating point @ offset " + contextStack.getCurrentContext().getOffset() );						
					} 
					
					floatingPoint = true;
					c= getChar(); 
					while ((c >= '0' && c <= '9') )
					{
						buff.append((char) c);
						c = getChar();
					}
				}
				

				if( c == 'e' || c == 'E' )
				{
					if( ! floatingPoint ) floatingPoint = true; 
					// exponent type for flaoting point 
					buff.append((char)c);
					c = getChar(); 
					
					// optional + or - 
					if( c == '+' || c == '-' )
					{
						buff.append( (char)c );
						c = getChar(); 
					}
					
					// digit sequence of exponent part 
					while ((c >= '0' && c <= '9') )
					{
						buff.append((char) c);
						c = getChar();
					}
					
					// optional suffix 
					if( c == 'l' || c == 'L' || c == 'f' || c == 'F' )
					{
						buff.append( (char)c );
						c = getChar(); 
					}
					
					
				}

				ungetChar( c );
				
				if( pasting )
				{
					if( lookAheadForTokenPasting() )
					{
						storageBuffer = buff; 
						c = getChar(); 
						continue; 	
					}
					else
					{
						if( storageBuffer != null )
						{
							contextStack.updateContext( new StringReader( buff.toString()), PASTING, IScannerContext.MACROEXPANSION );
							storageBuffer = null;  
							c = getChar(); 
							continue; 
						}
					}
				}
				
				int tokenType;
				String result = buff.toString(); 
				
				if( floatingPoint && result.equals(".") )
					tokenType = Token.tDOT;
				else
					tokenType = floatingPoint ? Token.tFLOATINGPT : Token.tINTEGER; 
				
				return newToken(
					tokenType,
					result,
					contextStack.getCurrentContext());
				
			} else if (c == '#') {
				int beginningOffset = contextStack.getCurrentContext().getOffset() - 1;
				// lets prepare for a preprocessor statement
				StringBuffer buff = new StringBuffer();
				buff.append((char) c);

				// we are allowed arbitrary whitespace after the '#' and before the rest of the text
				boolean skipped = skipOverWhitespace();

				c = getChar();
				
				if( c == '#' )
				{
					if( skipped )
						throw new ScannerException(BAD_PP + contextStack.getCurrentContext().getOffset()); 
					else 
						return newToken( tPOUNDPOUND, "##" );
				} else if( tokenizingMacroReplacementList ) {
					ungetChar( c ); 
					return newToken( tPOUND, "#" );
				}
				
				while (((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z')) || (c == '_') ) {
					buff.append((char) c);
					c = getChar();
				}
				ungetChar(c);

				String token = buff.toString();

				Object directive = ppDirectives.get(token);
				if (directive == null) {
					if (throwExceptionOnBadPreprocessorSyntax)
						throw new ScannerException(
							BAD_PP + contextStack.getCurrentContext().getOffset());
				} else {
					int type = ((Integer) directive).intValue();
					switch (type) {
						case PreprocessorDirectives.DEFINE :
							if ( ! passOnToClient ) {
								skipOverTextUntilNewline();
								c = getChar();
								continue;
							}

							poundDefine(beginningOffset);

							c = getChar();
							continue;

						case PreprocessorDirectives.INCLUDE :
							if (! passOnToClient ) {
								skipOverTextUntilNewline();
								c = getChar();
								continue;
							}

							poundInclude( beginningOffset );

							c = getChar();
							continue;
						case PreprocessorDirectives.UNDEFINE :
							if (! passOnToClient) {
								skipOverTextUntilNewline();
								c = getChar();
								continue;
							}
							skipOverWhitespace();
							// definition 
							String toBeUndefined = getNextIdentifier();
							
							if( ( definitions.remove(toBeUndefined) == null ) && ! quickScan ) 
								throw new ScannerException( "Attempt to #undef symbol " + toBeUndefined + " when it was never defined");
							
							skipOverTextUntilNewline();
							c = getChar();
							continue;
						case PreprocessorDirectives.IF :
							// get the rest of the line		
							String expression = getRestOfPreprocessorLine();
							
							boolean expressionEvalResult =	evaluateExpression(expression);
							passOnToClient = branches.poundif( expressionEvalResult ); 
							c = getChar();
							continue;



						case PreprocessorDirectives.IFDEF :
							skipOverWhitespace();
							String definition = getNextIdentifier();
							Object mapping = definitions.get(definition);

							if (mapping == null) {
								// not defined	
								passOnToClient = branches.poundif( false );
								skipOverTextUntilNewline();
							} else {
								passOnToClient = branches.poundif( true ); 
								// continue along, act like nothing is wrong :-)
								c = getChar();
							}
							continue;
						case PreprocessorDirectives.ENDIF :
							String restOfLine = getRestOfPreprocessorLine().trim();
							if( ! restOfLine.equals( "" ) && throwExceptionOnBadPreprocessorSyntax )
								throw new ScannerException( BAD_PP + contextStack.getCurrentContext().getOffset() );
							passOnToClient = branches.poundendif(); 
							c = getChar();
							continue;

						case PreprocessorDirectives.IFNDEF :
							skipOverWhitespace();
							String def = getNextIdentifier();
							Object map = definitions.get(def);

							if (map != null) {
								// not defined	
								skipOverTextUntilNewline();
								passOnToClient = branches.poundif( false ); 
							} else {
								passOnToClient = branches.poundif( true ); 
								// continue along, act like nothing is wrong :-)
								c = getChar();
							}
							continue;

						case PreprocessorDirectives.ELSE :
							passOnToClient = branches.poundelse(); 

							skipOverTextUntilNewline();
							c = getChar();
							continue;

						case PreprocessorDirectives.ELIF :

							String elsifExpression = getRestOfPreprocessorLine();

							if (elsifExpression.equals(""))
								if (throwExceptionOnBadPreprocessorSyntax)
									throw new ScannerException("Malformed #elsif clause");

							boolean elsifResult =
									evaluateExpression(elsifExpression );

							passOnToClient = branches.poundelif( elsifResult ); 
							c = getChar();
							continue;

						case PreprocessorDirectives.LINE :
							//TO DO 
							skipOverTextUntilNewline();
							c = getChar();
							continue;
						case PreprocessorDirectives.ERROR :
							if (! passOnToClient) {
								skipOverTextUntilNewline();
								c = getChar();
								continue;
							}

							String error = getRestOfPreprocessorLine();

							if (!quickScan) {
								throw new ScannerException("#error " + error);
							}
							c = getChar();
							continue;
						case PreprocessorDirectives.PRAGMA :
							//TO DO 
							skipOverTextUntilNewline();
							c = getChar();
							continue;
						case PreprocessorDirectives.BLANK :
							String remainderOfLine =
								getRestOfPreprocessorLine().trim();
							if (!remainderOfLine.equals("")) {
								if (throwExceptionOnBadPreprocessorSyntax)
									throw new ScannerException(
										BAD_PP + contextStack.getCurrentContext().getOffset());
							}

							c = getChar();
							continue;
						default :
							if (throwExceptionOnBadPreprocessorSyntax)
								throw new ScannerException(
									BAD_PP + contextStack.getCurrentContext().getOffset());

					}
				}
			} else {
				switch (c) {
					case '\'' :
						c = getChar(); 
						int next = getChar(); 
						if( next == '\'' )
							return newToken( Token.tCHAR, new Character( (char)c ).toString(), contextStack.getCurrentContext() ); 
						else
							if( throwExceptionOnBadCharacterRead )
								throw new ScannerException( "Invalid character '" + (char)c + "' read @ offset " + contextStack.getCurrentContext().getOffset() + " of file " + contextStack.getCurrentContext().getFilename() );
					case ':' :
						c = getChar();
						switch (c) {
							case ':' :
								return newToken(
									Token.tCOLONCOLON,
									"::",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tCOLON,
									":",
									contextStack.getCurrentContext());
						}
					case ';' :
						return newToken(Token.tSEMI, ";", contextStack.getCurrentContext());
					case ',' :
						return newToken(Token.tCOMMA, ",", contextStack.getCurrentContext());
					case '?' :
						return newToken(Token.tQUESTION, "?", contextStack.getCurrentContext());
					case '(' :
						return newToken(Token.tLPAREN, "(", contextStack.getCurrentContext());
					case ')' :
						return newToken(Token.tRPAREN, ")", contextStack.getCurrentContext());
					case '[' :
						return newToken(Token.tLBRACKET, "[", contextStack.getCurrentContext());
					case ']' :
						return newToken(Token.tRBRACKET, "]", contextStack.getCurrentContext());
					case '{' :
						return newToken(Token.tLBRACE, "{", contextStack.getCurrentContext());
					case '}' :
						return newToken(Token.tRBRACE, "}", contextStack.getCurrentContext());
					case '+' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tPLUSASSIGN,
									"+=",
									contextStack.getCurrentContext());
							case '+' :
								return newToken(
									Token.tINCR,
									"++",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tPLUS,
									"+",
									contextStack.getCurrentContext());
						}
					case '-' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tMINUSASSIGN,
									"-=",
									contextStack.getCurrentContext());
							case '-' :
								return newToken(
									Token.tDECR,
									"--",
									contextStack.getCurrentContext());
							case '>' :
								c = getChar();
								switch (c) {
									case '*' :
										return newToken(
											Token.tARROWSTAR,
											"->*",
											contextStack.getCurrentContext());
									default :
										ungetChar(c);
										return newToken(
											Token.tARROW,
											"->",
											contextStack.getCurrentContext());
								}
							default :
								ungetChar(c);
								return newToken(
									Token.tMINUS,
									"-",
									contextStack.getCurrentContext());
						}
					case '*' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tSTARASSIGN,
									"*=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tSTAR,
									"*",
									contextStack.getCurrentContext());
						}
					case '%' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tMODASSIGN,
									"%=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tMOD,
									"%",
									contextStack.getCurrentContext());
						}
					case '^' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tXORASSIGN,
									"^=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tXOR,
									"^",
									contextStack.getCurrentContext());
						}
					case '&' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tAMPERASSIGN,
									"&=",
									contextStack.getCurrentContext());
							case '&' :
								return newToken(
									Token.tAND,
									"&&",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tAMPER,
									"&",
									contextStack.getCurrentContext());
						}
					case '|' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tBITORASSIGN,
									"|=",
									contextStack.getCurrentContext());
							case '|' :
								return newToken(
									Token.tOR,
									"||",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tBITOR,
									"|",
									contextStack.getCurrentContext());
						}
					case '~' :
						return newToken(Token.tCOMPL, "~", contextStack.getCurrentContext());
					case '!' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tNOTEQUAL,
									"!=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tNOT,
									"!",
									contextStack.getCurrentContext());
						}
					case '=' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tEQUAL,
									"==",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tASSIGN,
									"=",
									contextStack.getCurrentContext());
						}
					case '<' :
						c = getChar();
						switch (c) {
							case '<' :
								c = getChar();
								switch (c) {
									case '=' :
										return newToken(
											Token.tSHIFTLASSIGN,
											"<<=",
											contextStack.getCurrentContext());
									default :
										ungetChar(c);
										return newToken(
											Token.tSHIFTL,
											"<<",
											contextStack.getCurrentContext());
								}
							case '=' :
								return newToken(
									Token.tLTEQUAL,
									"<=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(Token.tLT, "<", contextStack.getCurrentContext());
						}
					case '>' :
						c = getChar();
						switch (c) {
							case '>' :
								c = getChar();
								switch (c) {
									case '=' :
										return newToken(
											Token.tSHIFTRASSIGN,
											">>=",
											contextStack.getCurrentContext());
									default :
										ungetChar(c);
										return newToken(
											Token.tSHIFTR,
											">>",
											contextStack.getCurrentContext());
								}
							case '=' :
								return newToken(
									Token.tGTEQUAL,
									">=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(Token.tGT, ">", contextStack.getCurrentContext());
						}
					case '.' :
						c = getChar();
						switch (c) {
							case '.' :
								c = getChar();
								switch (c) {
									case '.' :
										return newToken(
											Token.tELIPSE,
											"...",
											contextStack.getCurrentContext());
									default :
										break;
								}
								break;
							case '*' :
								return newToken(
									Token.tDOTSTAR,
									".*",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tDOT,
									".",
									contextStack.getCurrentContext());
						}
						break;
					case '/' :
						c = getChar();
						switch (c) {
							case '/' :
								c = getChar();
								while (c != '\n' && c != NOCHAR)
									c = getChar();
								continue;
							case '*' :
								skipOverMultilineComment();
								c = getChar();
								continue;
							case '=' :
								return newToken(
									Token.tDIVASSIGN,
									"/=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									Token.tDIV,
									"/",
									contextStack.getCurrentContext());
						}
					default :
						// Bad character
						if( throwExceptionOnBadCharacterRead )
							throw new ScannerException( "Invalid character '" + (char)c + "' read @ offset " + contextStack.getCurrentContext().getOffset() + " of file " + contextStack.getCurrentContext().getFilename() );
						else
						{
							c = ' ';
							continue;
						}
				}

				throw Parser.endOfFile;
			}
		}

		if (throwExceptionOnEOFWithoutBalancedEndifs && ( getDepth() != 0) && !atEOF )
		{
			atEOF = true;
			throw new ScannerException("End of file encountered without terminating #endif");
		}

		// we're done
		throw Parser.endOfFile;
	}

	static {
		keywords.put("and", new Integer(Token.t_and));
		keywords.put("and_eq", new Integer(Token.t_and_eq));
		keywords.put("asm", new Integer(Token.t_asm));
		keywords.put("auto", new Integer(Token.t_auto));
		keywords.put("bitand", new Integer(Token.t_bitand));
		keywords.put("bitor", new Integer(Token.t_bitor));
		keywords.put("bool", new Integer(Token.t_bool));
		keywords.put("break", new Integer(Token.t_break));
		keywords.put("case", new Integer(Token.t_case));
		keywords.put("catch", new Integer(Token.t_catch));
		keywords.put("char", new Integer(Token.t_char));
		keywords.put("class", new Integer(Token.t_class));
		keywords.put("compl", new Integer(Token.t_compl));
		keywords.put("const", new Integer(Token.t_const));
		keywords.put("const_cast", new Integer(Token.t_const_cast));
		keywords.put("continue", new Integer(Token.t_continue));
		keywords.put("default", new Integer(Token.t_default));
		keywords.put("delete", new Integer(Token.t_delete));
		keywords.put("do", new Integer(Token.t_do));
		keywords.put("double", new Integer(Token.t_double));
		keywords.put("dynamic_cast", new Integer(Token.t_dynamic_cast));
		keywords.put("else", new Integer(Token.t_else));
		keywords.put("enum", new Integer(Token.t_enum));
		keywords.put("explicit", new Integer(Token.t_explicit));
		keywords.put("export", new Integer(Token.t_export));
		keywords.put("extern", new Integer(Token.t_extern));
		keywords.put("false", new Integer(Token.t_false));
		keywords.put("float", new Integer(Token.t_float));
		keywords.put("for", new Integer(Token.t_for));
		keywords.put("friend", new Integer(Token.t_friend));
		keywords.put("goto", new Integer(Token.t_goto));
		keywords.put("if", new Integer(Token.t_if));
		keywords.put("inline", new Integer(Token.t_inline));
		keywords.put("int", new Integer(Token.t_int));
		keywords.put("long", new Integer(Token.t_long));
		keywords.put("mutable", new Integer(Token.t_mutable));
		keywords.put("namespace", new Integer(Token.t_namespace));
		keywords.put("new", new Integer(Token.t_new));
		keywords.put("not", new Integer(Token.t_not));
		keywords.put("not_eq", new Integer(Token.t_not_eq));
		keywords.put("operator", new Integer(Token.t_operator));
		keywords.put("or", new Integer(Token.t_or));
		keywords.put("or_eq", new Integer(Token.t_or_eq));
		keywords.put("private", new Integer(Token.t_private));
		keywords.put("protected", new Integer(Token.t_protected));
		keywords.put("public", new Integer(Token.t_public));
		keywords.put("register", new Integer(Token.t_register));
		keywords.put("reinterpret_cast", new Integer(Token.t_reinterpret_cast));
		keywords.put("return", new Integer(Token.t_return));
		keywords.put("short", new Integer(Token.t_short));
		keywords.put("signed", new Integer(Token.t_signed));
		keywords.put("sizeof", new Integer(Token.t_sizeof));
		keywords.put("static", new Integer(Token.t_static));
		keywords.put("static_cast", new Integer(Token.t_static_cast));
		keywords.put("struct", new Integer(Token.t_struct));
		keywords.put("switch", new Integer(Token.t_switch));
		keywords.put("template", new Integer(Token.t_template));
		keywords.put("this", new Integer(Token.t_this));
		keywords.put("throw", new Integer(Token.t_throw));
		keywords.put("true", new Integer(Token.t_true));
		keywords.put("try", new Integer(Token.t_try));
		keywords.put("typedef", new Integer(Token.t_typedef));
		keywords.put("typeid", new Integer(Token.t_typeid));
		keywords.put("typename", new Integer(Token.t_typename));
		keywords.put("union", new Integer(Token.t_union));
		keywords.put("unsigned", new Integer(Token.t_unsigned));
		keywords.put("using", new Integer(Token.t_using));
		keywords.put("virtual", new Integer(Token.t_virtual));
		keywords.put("void", new Integer(Token.t_void));
		keywords.put("volatile", new Integer(Token.t_volatile));
		keywords.put("wchar_t", new Integer(Token.t_wchar_t));
		keywords.put("while", new Integer(Token.t_while));
		keywords.put("xor", new Integer(Token.t_xor));
		keywords.put("xor_eq", new Integer(Token.t_xor_eq));

		ppDirectives.put("#define", new Integer(PreprocessorDirectives.DEFINE));
		ppDirectives.put(
			"#undef",
			new Integer(PreprocessorDirectives.UNDEFINE));
		ppDirectives.put("#if", new Integer(PreprocessorDirectives.IF));
		ppDirectives.put("#ifdef", new Integer(PreprocessorDirectives.IFDEF));
		ppDirectives.put("#ifndef", new Integer(PreprocessorDirectives.IFNDEF));
		ppDirectives.put("#else", new Integer(PreprocessorDirectives.ELSE));
		ppDirectives.put("#endif", new Integer(PreprocessorDirectives.ENDIF));
		ppDirectives.put(
			"#include",
			new Integer(PreprocessorDirectives.INCLUDE));
		ppDirectives.put("#line", new Integer(PreprocessorDirectives.LINE));
		ppDirectives.put("#error", new Integer(PreprocessorDirectives.ERROR));
		ppDirectives.put("#pragma", new Integer(PreprocessorDirectives.PRAGMA));
		ppDirectives.put("#elif", new Integer(PreprocessorDirectives.ELIF));
		ppDirectives.put("#", new Integer(PreprocessorDirectives.BLANK));

	}

	static public class PreprocessorDirectives {
		static public final int DEFINE = 0;
		static public final int UNDEFINE = 1;
		static public final int IF = 2;
		static public final int IFDEF = 3;
		static public final int IFNDEF = 4;
		static public final int ELSE = 5;
		static public final int ENDIF = 6;
		static public final int INCLUDE = 7;
		static public final int LINE = 8;
		static public final int ERROR = 9;
		static public final int PRAGMA = 10;
		static public final int BLANK = 11;
		static public final int ELIF = 12;
	}

	public final int getCount() {
		return count;
	}

	public final int getDepth() {
		return branches.getDepth(); 
	}

	protected boolean evaluateExpression(String expression )
		throws ScannerException {
			
		if( quickScan )
		{
			if( expression.trim().equals( "0" ) )
				return false; 
			return true; 
		}
		else
		{	
			Object expressionEvalResult = null;
			try {
				ExpressionEvaluator evaluator = new ExpressionEvaluator();
				Scanner trial =
					new Scanner(
						// Semicolon makes this valid C (hopefully)
						new StringReader(expression + ";"),
						EXPRESSION,
						definitions);
				Parser parser = new Parser(trial, evaluator);
				parser.expression(null); 
				
				expressionEvalResult = evaluator.getResult();
	
			} catch (Exception e ) {
				throw new ScannerException(
					"Expression "
						+ expression
						+ " evaluates to an undefined value");			
			} finally
			{
				if (expressionEvalResult == null)
					throw new ScannerException(
						"Expression "
							+ expression
							+ " evaluates to an undefined value");			
			}
	
			
			if (expressionEvalResult instanceof Integer ) {
				int i = ((Integer) expressionEvalResult).intValue();
				if (i == 0) {
					return false;
				}
				return true;
			} else if (
				expressionEvalResult instanceof Boolean ) {
				return ((Boolean) expressionEvalResult).booleanValue();
			} else {
				throw new ScannerException(
					"Unexpected expression type - we do not expect "
						+ expressionEvalResult.getClass().getName());
			}
		}
	}

	protected boolean skipOverMultilineComment() throws ScannerException {
		int state = 0;
		boolean encounteredNewline = false;
		// simple state machine to handle multi-line comments
		// state 0 == no end of comment in site
		// state 1 == encountered *, expecting /
		// state 2 == we are no longer in a comment

		int c = getChar();
		while (state != 2 && c != NOCHAR) {
			if (c == '\n')
				encounteredNewline = true;

			switch (state) {
				case 0 :
					if (c == '*')
						state = 1;
					break;
				case 1 :
					if (c == '/')
						state = 2;
					else if (c != '*')
						state = 0;
					break;
			}
			c = getChar();
		}

		if (c == NOCHAR) {
			if (throwExceptionOnEOFWithinMultilineComment)
				throw new ScannerException("Encountered EOF while in multiline comment");
		}

		ungetChar(c);

		return encounteredNewline;
	}

	protected void poundInclude( int beginningOffset ) throws ScannerException {
		skipOverWhitespace();
		int c = getChar();
		int offset;

		StringBuffer fileName = new StringBuffer();
		boolean useIncludePath = true;
		if (c == '<') {
			c = getChar();
			while ((c != '>')) {
				fileName.append((char) c);
				c = getChar();
			}
		}
		else if (c == '"') {
			c = getChar();
			while ((c != '"')) {
				fileName.append((char) c);
				c = getChar();
			}
			useIncludePath = false; 
			// TO DO: Make sure the directory of the current file is in the
			// inclusion paths.
		}
		
		String f = fileName.toString();
		
		if( quickScan )
		{ 
			if( callback != null )
			{
				offset = contextStack.getCurrentContext().getOffset() - f.length() - 1; // -1 for the end quote
				
				callback.inclusionBegin( f, offset, beginningOffset );
				callback.inclusionEnd();  
			}
		}
		else
			handleInclusion(f.trim(), useIncludePath );
	}

	protected void poundDefine(int beginning) throws ScannerException, Parser.EndOfFile {
		skipOverWhitespace();
		// definition 
		String key = getNextIdentifier();
		int offset = contextStack.getCurrentContext().getOffset() - key.length() - contextStack.getCurrentContext().undoStackSize();

		if (!quickScan) {
			String checkForRedefinition = (String) definitions.get(key);
			if (checkForRedefinition != null) {
				throw new ScannerException(
					"Preprocessor symbol "
						+ key
						+ " has already been defined to "
						+ checkForRedefinition
						+ " cannot redefined.");
			}
		}

		// get the next character
		// the C++ standard says that macros must not put
		// whitespace between the end of the definition 
		// identifier and the opening parenthesis
		int c = getChar();
		if (c == '(') {
			StringBuffer buffer = new StringBuffer();
			c = getChar();
			while (c != ')') {
				buffer.append((char) c);
				c = getChar();
			}

			String parameters = buffer.toString();

			// replace StringTokenizer later -- not performant
			StringTokenizer tokenizer = new StringTokenizer(parameters, ",");
			ArrayList parameterIdentifiers =
				new ArrayList(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				parameterIdentifiers.add(tokenizer.nextToken().trim());
			}

			skipOverWhitespace();

			ArrayList macroReplacementTokens = new ArrayList();
			String replacementString = getRestOfPreprocessorLine();
			Scanner helperScanner = new Scanner();
			helperScanner.initialize(
				new StringReader(replacementString),
				null);
			helperScanner.setTokenizingMacroReplacementList( true );
			Token t = helperScanner.nextToken(false);

			try {
				while (true) {
					//each # preprocessing token in the replacement list shall be followed
					//by a parameter as the next reprocessing token in the list
					if( t.type == tPOUND ){
						macroReplacementTokens.add( t );
						t = helperScanner.nextToken(false);
						int index = parameterIdentifiers.indexOf(t.image);
						if (index == -1 ) {
							//not found
							if (throwExceptionOnBadPreprocessorSyntax)
								throw new ScannerException(
									BAD_PP + contextStack.getCurrentContext().getOffset());
							return;
						}
					}
					
					macroReplacementTokens.add(t);
					t = helperScanner.nextToken(false);
				}
			} catch (Parser.EndOfFile e) {
				// Good
			}

			IMacroDescriptor descriptor = new MacroDescriptor();
			descriptor.initialize(
				key,
				parameterIdentifiers,
				macroReplacementTokens,
				key + "(" + parameters + ")");
			addDefinition(key, descriptor);

		} else if ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r')) {
			// this is a simple definition 
			skipOverWhitespace();

			// get what we are to map the name to and add it to the definitions list
			String value = getRestOfPreprocessorLine();
			addDefinition( key, value ); 				
		
		} else if (c == '/') {
			// this could be a comment	
			c = getChar();
			if (c == '/') // one line comment
				{
				skipOverTextUntilNewline();
				addDefinition(key, "");
			} else if (c == '*') // multi-line comment
				{
				if (skipOverMultilineComment()) {
					// we have gone over a newline
					// therefore, this symbol was defined to an empty string
					addDefinition(key, "");
				} else {
					String value = getRestOfPreprocessorLine();
					addDefinition(key, value);
				}
			} else {
				// this is not a comment 
				// it is a bad statement
				if (throwExceptionOnBadPreprocessorSyntax)
					throw new ScannerException(
						BAD_PP + contextStack.getCurrentContext().getOffset());
			}
		} else {
			System.out.println("Unexpected character " + ((char) c));
			if (throwExceptionOnBadPreprocessorSyntax)
				throw new ScannerException(BAD_PP + contextStack.getCurrentContext().getOffset());
		}

		// call the callback accordingly
		if( callback != null )
			callback.macro( key, offset, beginning, contextStack.getCurrentContext().getOffset() );
	}
	
	protected void expandDefinition(String symbol, Object expansion)
		throws ScannerException {
		if (expansion instanceof String ) {
			String replacementValue = (String) expansion;
			contextStack.updateContext( new StringReader(replacementValue), (POUND_DEFINE + symbol ), ScannerContext.MACROEXPANSION );
		} else if (expansion instanceof IMacroDescriptor ) {
			IMacroDescriptor macro = (IMacroDescriptor) expansion;
			skipOverWhitespace();
			int c = getChar();

			if (c == '(') {
				StringBuffer buffer = new StringBuffer();
				int bracketCount = 1;
				c = getChar();

				while (true) {
					if (c == '(')
						++bracketCount;
					else if (c == ')')
						--bracketCount;

					if (bracketCount == 0)
						break;
					buffer.append((char) c);
					c = getChar();
				}
				
				String betweenTheBrackets = buffer.toString().trim();

				Scanner tokenizer = new Scanner( new StringReader(betweenTheBrackets), TEXT, definitions );
				Vector parameterValues = new Vector();
				Token t = null;
				String str = new String();
				boolean space = false;
				try{
					while (true) {
						t = tokenizer.nextToken(false);
						if( t.type == Token.tCOMMA )
						{
							parameterValues.add( str );
							str = "";
							space = false;
							continue;
						}
											
						if( space )
							str += ' ';
							
						if( t.type == Token.tSTRING )	
							str += '\"' + t.image + '\"';							
						else 
							str += t.image;
							
						space = true;
					}
				} catch (Parser.EndOfFile e) {
					// Good
					parameterValues.add( str );
				}
				// create a string that represents what needs to be tokenized
				buffer = new StringBuffer();
				List tokens = macro.getTokenizedExpansion();
				List parameterNames = macro.getParameters();

				if (parameterNames.size() != parameterValues.size()) {
					if (throwExceptionOnBadMacroExpansion)
						throw new ScannerException(
							"Improper use of macro " + symbol);
				}

				int numberOfTokens = tokens.size();

				for (int i = 0; i < numberOfTokens; ++i) {
					t = (Token) tokens.get(i);
					if (t.type == Token.tIDENTIFIER) {
						String identifierName = t.image;

						// is this identifier in the parameterNames
						// list? 
						int index = parameterNames.indexOf(t.image);
						if (index == -1 ) {
							// not found
							// just add image to buffer
							buffer.append(t.image );
						} else {
							buffer.append(
								(String) parameterValues.elementAt(index) );
						}
					} else if (t.type == tPOUND) {
						//next token should be a parameter which needs to be turned into
						//a string literal
						t = (Token) tokens.get( ++i );
						int index = parameterNames.indexOf(t.image);
						if( index == -1 ){
							if (throwExceptionOnBadMacroExpansion)
								throw new ScannerException(	"Improper use of the # preprocessing token." );	
						} else {
							buffer.append('\"');
							String value = (String)parameterValues.elementAt(index);
							char val [] = value.toCharArray();
							char ch;
							int length = value.length();
							for( int j = 0; j < length; j++ ){
								ch = val[j];
								if( ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' ){
									//Each occurance of whitespace becomes a single space character
									while( ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' ){
										ch = val[++j];
									}
									buffer.append(' ');
								} 
								//a \ character is inserted before each " and \
								if( ch == '\"' || ch == '\\' ){
									buffer.append('\\');
									buffer.append(ch);
								} else {
									buffer.append(ch);
								}
							}
							buffer.append('\"');
						}
					} else {
						buffer.append(t.image);
					}
					
					boolean pastingNext = false;
					
					if( i != numberOfTokens - 1)
					{
						Token t2 = (Token) tokens.get(i+1);
						if( t2.getType() == tPOUNDPOUND )
							pastingNext = true;  
					}
					
					if( t.getType() != tPOUNDPOUND && ! pastingNext )
						buffer.append( " " ); 
				}
				String finalString = buffer.toString(); 
				contextStack.updateContext(
					new StringReader(finalString),
					POUND_DEFINE + macro.getSignature(), ScannerContext.MACROEXPANSION );
			} else 
				if (throwExceptionOnBadMacroExpansion)
					throw new ScannerException(
						"Improper use of macro " + symbol);

		} else {
			System.out.println(
				"Unexpected class stored in definitions table. "
					+ expansion.getClass().getName());
		}

	}

	protected String handleDefinedMacro() throws ScannerException {
		skipOverWhitespace();

		int c = getChar();

		if (c != '(') {
			if (throwExceptionOnBadMacroExpansion)
				throw new ScannerException("Improper use of macro defined()");
		}

		StringBuffer buffer = new StringBuffer();
		c = getChar();
		while ((c != NOCHAR) && (c != ')')) {
			buffer.append((char) c);
			c = getChar();
		}
		if (c == NOCHAR) {
			if (throwExceptionOnBadMacroExpansion)
				throw new ScannerException("Improper use of macro defined()");
		}

		String definitionIdentifier = buffer.toString().trim();

		if (definitions.get(definitionIdentifier) != null)
			return "1";

		return "0";
	}
}
