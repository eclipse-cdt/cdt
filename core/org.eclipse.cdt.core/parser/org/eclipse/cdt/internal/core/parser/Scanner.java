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
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class Scanner implements IScanner {

	public IScanner initialize(Reader reader, String filename) {
		init(reader, filename);
		return this;
	}

	protected void init(Reader reader, String filename) {
		// this is a hack to get around a sudden EOF experience
		contextStack.push(
			new ScannerContext().initialize(
				new StringReader("\n"),
				START,
				NOCHAR));
		if (filename == null)
			currentContext =
				new ScannerContext().initialize(reader, TEXT, NOCHAR);
		else
			currentContext =
				new ScannerContext().initialize(reader, filename, NOCHAR);
	}

	public Scanner() {
	}

	protected Scanner(Reader reader, String filename, Hashtable defns) {
		initialize(reader, filename);
		definitions = defns;
	}

	protected void updateContext(Reader reader, String filename) {
//		if (callback != null)
//			callback.inclusionBegin(filename); // not quite right ... fix me!!!

		contextStack.push(currentContext);
		currentContext =
			new ScannerContext().initialize(reader, filename, NOCHAR);
	}

	protected boolean rollbackContext() {
		try {
			currentContext.getReader().close();
		} catch (IOException ie) {
			System.out.println("Error closing reader");
		}

		if (contextStack.isEmpty()) {
			currentContext = null;
			return false;
		}

		//if (callback != null)
		//	callback.inclusionEnd();

		currentContext = (ScannerContext) contextStack.pop();
		return true;
	}

	public void addIncludePath(String includePath) {
		includePaths.add(includePath);
	}

	public void overwriteIncludePath(List newIncludePaths) {
		includePaths = null;
		includePaths = new ArrayList();
		includePaths.addAll(newIncludePaths);
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
		return includePaths.toArray();
	}

	protected void skipOverWhitespace() {
		int c = getChar();
		while ((c != NOCHAR) && ((c == ' ') || (c == '\t')))
			c = getChar();
		if (c != NOCHAR)
			ungetChar(c);

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

	protected Token newToken(int t, String i, IScannerContext c) {
		setCurrentToken(new Token(t, i, c));
		return currentToken;
	}

	protected Token newToken(int t, String i) {
		setCurrentToken(new Token(t, i));
		return currentToken;
	}

	protected String getNextIdentifier() {
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

	protected void handleInclusion(String fileName) throws ScannerException {
		// Skip over inclusions in quickScan mode
		if (quickScan)
			return;

		// iterate through the include paths 
		Iterator iter = includePaths.iterator();

		while (iter.hasNext()) {
			String path = (String) iter.next();

			java.io.File pathFile = new java.io.File(path);
			if (pathFile.isDirectory()) {
				String newPath = pathFile + "\\" + fileName;

				java.io.File includeFile = new java.io.File(newPath);

				if (includeFile.exists() && includeFile.isFile()) {
					try {
						FileReader inclusionReader =
							new FileReader(includeFile);
						//System.out.println( "Parsing inclusion file " + newPath );
						updateContext(inclusionReader, newPath);
						return;
					} catch (FileNotFoundException fnf) {
						// do nothing	
					}
				}
			}
		}
		if (throwExceptionOnInclusionNotFound)
			throw new ScannerException("Cannot find inclusion " + fileName);
	}

	// constants
	public static final int NOCHAR = -1;

	private static final String TEXT = "<text>";
	private static final String START = "<initial reader>";
	private static final String EXPRESSION = "<expression>";
	private static final String BAD_PP =
		"Invalid preprocessor directive encountered at offset ";
	private static final String DEFINED = "defined";
	private static final String POUND_DEFINE = "#define ";

	private IScannerContext currentContext;
	private Stack contextStack = new Stack();

	private List includePaths = new ArrayList();
	private Hashtable definitions = new Hashtable();
	private int count = 0;
	private static HashMap keywords = new HashMap();
	private static HashMap ppDirectives = new HashMap();

	private Token currentToken = null;

	private boolean passOnToClient = true; 
	private BranchTracker branches = new BranchTracker();

	// these are scanner configuration aspects that we perhaps want to tweak
	// eventually, these should be configurable by the client, but for now
	// we can just leave it internal
	private boolean throwExceptionPPError = true;
	private boolean throwExceptionOnRedefinition = false;
	private boolean throwExceptionOnBadPPDirective = true;
	private boolean throwExceptionOnInclusionNotFound = true;
	private boolean throwExceptionOnBadMacroExpansion = true;
	private boolean throwExceptionOnUnboundedString = true;
	private boolean throwExceptionOnEOFWithinMultilineComment = true;
	private boolean throwExceptionOnEOFWithoutBalancedEndifs = true;
	private boolean providedDefinedMacro = true;

	private boolean quickScan = false;
	public void setQuickScan(boolean qs) {
		quickScan = qs;
	}

	private IParserCallback callback;
	public void setCallback(IParserCallback c) {
		callback = c;
	}

	private int getChar() {
		int c = NOCHAR;

		boolean done;
		do {
			done = true;

			if (currentContext.getUndo() != NOCHAR) {
				c = currentContext.getUndo();
				currentContext.setUndo(NOCHAR);
			} else {
				try {
					c = currentContext.read();
					if (c == NOCHAR) {
						if (rollbackContext() == false) {
							c = NOCHAR;
							break;
						} else {
							done = false;
						}
					}
				} catch (IOException e) {
					if (rollbackContext() == false) {
						c = NOCHAR;
					} else {
						done = false;
					}
				}
			}
		} while (!done);

		if (c == '\\') {
			c = getChar();
			if (c == '\r') {
				c = getChar();
				if (c == '\n')
					c = getChar();
			} else if (c == '\n')
				c = getChar();
		}

		return c;
	}

	private void ungetChar(int c) {
		// Should really check whether there already is a char there
		// If so, we should be using a buffer, instead of a single char
		currentContext.setUndo(c);
	}

	public Token nextToken() throws ScannerException {

		count++;

		int c = getChar();

		while (c != NOCHAR) {
			if ( ! passOnToClient ) {
				while (c != '#') {
					c = getChar();
				}
			}

			if ((c == ' ') || (c == '\r') || (c == '\t') || (c == '\n')) {
				c = getChar();
				continue;
			} else if (
				((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z')) | (c == '_')) {
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

				if (providedDefinedMacro) {
					if (ident.equals(DEFINED)) {
						return newToken(Token.tINTEGER, handleDefinedMacro());
					}
				}

				Object mapping = definitions.get(ident);

				if (mapping != null) {
					expandDefinition(ident, mapping);
					c = getChar();
					continue;
				}

				Object tokenTypeObject = keywords.get(ident);
				int tokenType = Token.tIDENTIFIER;
				if (tokenTypeObject != null)
					tokenType = ((Integer) tokenTypeObject).intValue();

				return newToken(tokenType, ident, currentContext);
			} else if (c == '"') {
				// string
				StringBuffer buff = new StringBuffer();
				c = getChar();

				while (c != '"' && c != '\n') {
					buff.append((char) c);
					c = getChar();
				}

				if (c != '\n') {
					return newToken(
						Token.tSTRING,
						buff.toString(),
						currentContext);
				} else {
					if (throwExceptionOnUnboundedString)
						throw new ScannerException(
							"Unbounded string found at offset "
								+ currentContext.getOffset());
				}

			} else if ((c >= '0') && (c <= '9')) {
				StringBuffer buff = new StringBuffer();
				buff.append((char) c);

				c = getChar();
				boolean hex = false;
				if (c == 'x') {
					hex = true;
					c = getChar();
				}

				while ((c >= '0' && c <= '9')
					|| (hex
						&& ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))) {
					buff.append((char) c);
					c = getChar();
				}

				ungetChar(c);
				return newToken(
					Token.tINTEGER,
					buff.toString(),
					currentContext);
			} else if (c == '#') {
				// lets prepare for a preprocessor statement
				StringBuffer buff = new StringBuffer();
				buff.append((char) c);

				// we are allowed arbitrary whitespace after the '#' and before the rest of the text
				skipOverWhitespace();

				c = getChar();
				while (((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z')) | (c == '_')) {
					buff.append((char) c);
					c = getChar();
				}
				ungetChar(c);

				String token = buff.toString();

				Object directive = ppDirectives.get(token);
				if (directive == null) {
					if (throwExceptionOnBadPPDirective)
						throw new ScannerException(
							BAD_PP + currentContext.getOffset());

				} else {
					int type = ((Integer) directive).intValue();
					switch (type) {
						case PreprocessorDirectives.DEFINE :
							if ( ! passOnToClient ) {
								skipOverTextUntilNewline();
								c = getChar();
								continue;
							}

							poundDefine();

							c = getChar();
							continue;

						case PreprocessorDirectives.INCLUDE :
							if (! passOnToClient ) {
								skipOverTextUntilNewline();
								c = getChar();
								continue;
							}

							poundInclude();

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
							// TODO -- Should we throw an exception if we
							// do not have this in our table?
							definitions.remove(toBeUndefined);
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
							// TODO - make sure there is nothing after endif

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
								if (throwExceptionOnBadPPDirective)
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

							if (throwExceptionPPError) {
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
								if (throwExceptionOnBadPPDirective)
									throw new ScannerException(
										BAD_PP + currentContext.getOffset());
							}

							c = getChar();
							continue;
						default :
							if (throwExceptionOnBadPPDirective)
								throw new ScannerException(
									BAD_PP + currentContext.getOffset());

					}
				}
			} else {
				switch (c) {
					case ':' :
						c = getChar();
						switch (c) {
							case ':' :
								return newToken(
									Token.tCOLONCOLON,
									"::",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tCOLON,
									":",
									currentContext);
						}
					case ';' :
						return newToken(Token.tSEMI, ";", currentContext);
					case ',' :
						return newToken(Token.tCOMMA, ",", currentContext);
					case '?' :
						return newToken(Token.tQUESTION, "?", currentContext);
					case '(' :
						return newToken(Token.tLPAREN, "(", currentContext);
					case ')' :
						return newToken(Token.tRPAREN, ")", currentContext);
					case '[' :
						return newToken(Token.tLBRACKET, "[", currentContext);
					case ']' :
						return newToken(Token.tRBRACKET, "]", currentContext);
					case '{' :
						return newToken(Token.tLBRACE, "{", currentContext);
					case '}' :
						return newToken(Token.tRBRACE, "}", currentContext);
					case '+' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tPLUSASSIGN,
									"+=",
									currentContext);
							case '+' :
								return newToken(
									Token.tINCR,
									"++",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tPLUS,
									"+",
									currentContext);
						}
					case '-' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tMINUSASSIGN,
									"-=",
									currentContext);
							case '-' :
								return newToken(
									Token.tDECR,
									"--",
									currentContext);
							case '>' :
								c = getChar();
								switch (c) {
									case '*' :
										return newToken(
											Token.tARROWSTAR,
											"->*",
											currentContext);
									default :
										ungetChar(c);
										return newToken(
											Token.tARROW,
											"->",
											currentContext);
								}
							default :
								ungetChar(c);
								return newToken(
									Token.tMINUS,
									"-",
									currentContext);
						}
					case '*' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tSTARASSIGN,
									"*=",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tSTAR,
									"*",
									currentContext);
						}
					case '%' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tMODASSIGN,
									"%=",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tMOD,
									"%",
									currentContext);
						}
					case '^' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tXORASSIGN,
									"^=",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tXOR,
									"^",
									currentContext);
						}
					case '&' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tAMPERASSIGN,
									"&=",
									currentContext);
							case '&' :
								return newToken(
									Token.tAND,
									"&&",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tAMPER,
									"&",
									currentContext);
						}
					case '|' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tBITORASSIGN,
									"|=",
									currentContext);
							case '|' :
								return newToken(
									Token.tOR,
									"||",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tBITOR,
									"|",
									currentContext);
						}
					case '~' :
						return newToken(Token.tCOMPL, "~", currentContext);
					case '!' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tNOTEQUAL,
									"!=",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tNOT,
									"!",
									currentContext);
						}
					case '=' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									Token.tEQUAL,
									"==",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tASSIGN,
									"=",
									currentContext);
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
											currentContext);
									default :
										ungetChar(c);
										return newToken(
											Token.tSHIFTL,
											"<<",
											currentContext);
								}
							case '=' :
								return newToken(
									Token.tLTEQUAL,
									"<=",
									currentContext);
							default :
								ungetChar(c);
								return newToken(Token.tLT, "<", currentContext);
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
											currentContext);
									default :
										ungetChar(c);
										return newToken(
											Token.tSHIFTR,
											">>",
											currentContext);
								}
							case '=' :
								return newToken(
									Token.tGTEQUAL,
									">=",
									currentContext);
							default :
								ungetChar(c);
								return newToken(Token.tGT, ">", currentContext);
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
											currentContext);
									default :
										break;
								}
								break;
							case '*' :
								return newToken(
									Token.tDOTSTAR,
									".*",
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tDOT,
									".",
									currentContext);
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
									currentContext);
							default :
								ungetChar(c);
								return newToken(
									Token.tDIV,
									"/",
									currentContext);
						}
					default :
						break;
				}

				return newToken(
					Token.tEOF,
					"Bad Char: " + (char) c,
					currentContext);
			}
		}

		if (throwExceptionOnEOFWithoutBalancedEndifs && (getDepth() != 0))
			throw new ScannerException("End of file encountered without terminating #endif");

		// we're done
		if (currentToken != null)
			currentToken.setNext(Token.EOF);
		return Token.EOF;
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
						new StringReader(expression),
						EXPRESSION,
						definitions);
				Parser parser = new Parser(trial, evaluator);
				parser.expression();
				
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
	
			
			if (expressionEvalResult.getClass() == java.lang.Integer.class) {
				int i = ((Integer) expressionEvalResult).intValue();
				if (i == 0) {
					return false;
				}
				return true;
			} else if (
				expressionEvalResult.getClass() == java.lang.Boolean.class) {
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

	protected void poundInclude() throws ScannerException {
		skipOverWhitespace();
		int c = getChar();
		int offset;

		StringBuffer fileName = new StringBuffer();
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

			// TO DO: Make sure the directory of the current file is in the
			// inclusion paths.
		}
		
		String f = fileName.toString();
		
		if( quickScan )
		{ 
			if( callback != null )
			{
				offset = currentContext.getOffset() - f.length() - 1; // -1 for the end quote
				
				callback.inclusionBegin( f, offset );
				callback.inclusionEnd();  
			}
		}
		else
			handleInclusion(f.trim());
	}

	protected void poundDefine() throws ScannerException {
		skipOverWhitespace();
		// definition 
		String key = getNextIdentifier();
		int offset = currentContext.getOffset() - key.length();
		if( currentContext.getUndo() != Scanner.NOCHAR )
			offset -= 1;

		if (throwExceptionOnRedefinition) {
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
			Token t = helperScanner.nextToken();

			while (t.type != Token.tEOF) {
				macroReplacementTokens.add(t);
				t = helperScanner.nextToken();
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
			addDefinition(key, value);
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
				if (throwExceptionOnBadPPDirective)
					throw new ScannerException(
						BAD_PP + currentContext.getOffset());
			}
		} else {
			System.out.println("Unexpected character " + ((char) c));
			if (throwExceptionOnBadPPDirective)
				throw new ScannerException(BAD_PP + currentContext.getOffset());
		}

		// call the callback accordingly
		if( callback != null )
			callback.macro( key, offset );
	}

	protected void expandDefinition(String symbol, Object expansion)
		throws ScannerException {
		if (expansion.getClass() == String.class) {
			String replacementValue = (String) expansion;
			updateContext(
				new StringReader(replacementValue),
				POUND_DEFINE + symbol);
		} else if (expansion.getClass() == MacroDescriptor.class) {
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
				String betweenTheBrackets = buffer.toString();
				StringTokenizer tokenizer =
					new StringTokenizer(betweenTheBrackets, ",");
				Vector parameterValues = new Vector(tokenizer.countTokens());
				while (tokenizer.hasMoreTokens()) {
					parameterValues.add(tokenizer.nextToken().trim());
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
					Token t = (Token) tokens.get(i);
					if (t.type == Token.tIDENTIFIER) {
						String identifierName = t.image;

						// is this identifier in the parameterNames
						// list? 

						int index = parameterNames.indexOf(t.image);
						if (index == -1 ) {
							// not found
							// just add image to buffer
							buffer.append(t.image);
						} else {
							buffer.append(
								(String) parameterValues.elementAt(index));
						}
					} else {
						buffer.append(t.image);
					}
				}
				updateContext(
					new StringReader(buffer.toString()),
					POUND_DEFINE + macro.getSignature());
			} else {
				if (throwExceptionOnBadMacroExpansion)
					throw new ScannerException(
						"Improper use of macro " + symbol);

			}

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
