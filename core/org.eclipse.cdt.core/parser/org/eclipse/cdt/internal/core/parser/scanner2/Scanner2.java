/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.scanner.BranchTracker;
import org.eclipse.cdt.internal.core.parser.scanner.ContextStack;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerContext;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerData;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionDirective;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionParseException;
import org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro.Expansion;
import org.eclipse.cdt.internal.core.parser.token.ImagedToken;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

/**
 * @author Doug Schaefer
 *
 */
public class Scanner2 implements IScanner, IScannerData {

	private ISourceElementRequestor requestor;
	private ParserMode parserMode;
	private ParserLanguage language;
	protected IParserLogService log;
	private IScannerExtension scannerExtension;
	private List workingCopies;
	private CharArrayObjectMap definitions = new CharArrayObjectMap(64);
	private String[] includePaths;
	int count;
	
	private ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();
	private final Map fileCache = new HashMap(100);
	
	// The context stack
	private static final int bufferInitialSize = 8;
	private int bufferStackPos = -1;
	private char[][] bufferStack = new char[bufferInitialSize][];
	private Object[] bufferData = new Object[bufferInitialSize];
	private int[] bufferPos = new int[bufferInitialSize];
	private int[] bufferLimit = new int[bufferInitialSize];
	
	// Utility
	private static String[] emptyStringArray = new String[0];
	private static char[] emptyCharArray = new char[0];
	private static EndOfFileException EOF = new EndOfFileException();

	public Scanner2(CodeReader reader,
					IScannerInfo info,
					ISourceElementRequestor requestor,
					ParserMode parserMode,
					ParserLanguage language,
					IParserLogService log,
					IScannerExtension extension,
					List workingCopies) {

		this.scannerExtension = extension;
		this.requestor = requestor;
		this.parserMode = parserMode;
		this.language = language;
		this.log = log;
		this.workingCopies = workingCopies;

		if (reader.filename != null)
			fileCache.put(reader.filename, reader);
		
		pushContext(reader.buffer, reader);

		setupBuiltInMacros();
		
		if (info.getDefinedSymbols() != null) {
			Map symbols = info.getDefinedSymbols();
			String[] keys = (String[])symbols.keySet().toArray(emptyStringArray);
			for (int i = 0; i < keys.length; ++i) {
				String symbolName = (String)keys[i];
				Object value = symbols.get(symbolName);

				if( value instanceof String ) {	
					//TODO add in check here for '(' and ')'
					addDefinition( symbolName, scannerExtension.initializeMacroValue(this, (String) value));
				} else if( value instanceof IMacroDescriptor )
					addDefinition( symbolName, (IMacroDescriptor)value);
			}
		}
		
		includePaths = info.getIncludePaths();

	}

	private void pushContext(char[] buffer) {
		if (++bufferStackPos == bufferStack.length) {
			int size = bufferStack.length * 2;
			
			char[][] oldBufferStack = bufferStack;
			bufferStack = new char[size][];
			System.arraycopy(oldBufferStack, 0, bufferStack, 0, oldBufferStack.length);
			
			Object[] oldBufferData = bufferData;
			bufferData = new Object[size];
			System.arraycopy(oldBufferData, 0, bufferData, 0, oldBufferData.length);
			
			int[] oldBufferPos = bufferPos;
			bufferPos = new int[size];
			System.arraycopy(oldBufferPos, 0, bufferPos, 0, oldBufferPos.length);
			
			int[] oldBufferLimit = bufferLimit;
			bufferLimit = new int[size];
			System.arraycopy(oldBufferLimit, 0, bufferLimit, 0, oldBufferLimit.length);
		}
		
		bufferStack[bufferStackPos] = buffer;
		bufferPos[bufferStackPos] = -1;
		bufferLimit[bufferStackPos] = buffer.length;
	}
	
	private void pushContext(char[] buffer, Object data) {
		pushContext(buffer);
		bufferData[bufferStackPos] = data;
	}
	
	private void popContext() {
		bufferStack[bufferStackPos] = null;
		--bufferStackPos;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#addDefinition(java.lang.String, org.eclipse.cdt.core.parser.IMacroDescriptor)
	 */
	public void addDefinition(String key, IMacroDescriptor macroToBeAdded) {
		//definitions.put(key.toCharArray(), macroToBeAdded);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#addDefinition(java.lang.String, java.lang.String)
	 */
	public void addDefinition(String key, String value) {
		char[] ckey = key.toCharArray();
		definitions.put(ckey, new ObjectStyleMacro(ckey, value.toCharArray()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getCount()
	 */
	public int getCount() {
		return count;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getDefinition(java.lang.String)
	 */
	public IMacroDescriptor getDefinition(String key) {
		return (IMacroDescriptor)definitions.get(key.toCharArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getDefinitions()
	 */
	public Map getDefinitions() {
		return null;
	}

	public CharArrayObjectMap getRealDefinitions() {
		return definitions;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getDepth()
	 */
	public int getDepth() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getIncludePaths()
	 */
	public String[] getIncludePaths() {
		return includePaths;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#isOnTopContext()
	 */
	public boolean isOnTopContext() {
		// TODO Auto-generated method stub
		return false;
	}

	private IToken nextToken;
	private boolean finished = false;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#nextToken()
	 */
	public IToken nextToken() throws ScannerException, EndOfFileException {
		if (finished)
			throw EOF;
		
		if (nextToken == null) {
			nextToken = fetchToken();
			if (nextToken == null)
				throw EOF;
		}
		
		IToken token = nextToken;
		
		nextToken = fetchToken();
		if (nextToken == null)
			finished = true;
		else if (nextToken.getType() == IToken.tPOUNDPOUND) {
			// time for a pasting
			IToken token2 = fetchToken();
			if (token2 == null) {
				nextToken = null;
				finished = true;
			} else {
				String t1 = token.getImage();
				String t2 = token2.getImage();
				char[] pb = new char[t1.length() + t2.length()];
				t1.getChars(0, t1.length(), pb, 0);
				t2.getChars(0, t2.length(), pb, t1.length());
				pushContext(pb);
				nextToken = null;
				return nextToken();
			}
		} else if (token.getType() == IToken.tSTRING) {
			while (nextToken != null && nextToken.getType() == IToken.tSTRING) {
				// Concatenate the adjacent strings
				String t1 = token.getImage();
				String t2 = nextToken.getImage();
				token = new ImagedToken(IToken.tSTRING, t1 + t2);
				nextToken = fetchToken();
			}
		}
		
		// TODO Check if token is ## and proceed with pasting
		
		return token;
	}
	
	// Return null to signify end of file
	private IToken fetchToken() throws ScannerException {
		
		contextLoop:
		while (bufferStackPos >= 0) {
			
			// Find the first thing we would care about
			skipOverWhiteSpace();
			
			while (++bufferPos[bufferStackPos] >= bufferLimit[bufferStackPos]) {
				// We're at the end of a context, pop it off and try again
				popContext();
				continue contextLoop;
			}
 
			// Tokens don't span buffers, stick to our current one
			char[] buffer = bufferStack[bufferStackPos];
			int limit = bufferLimit[bufferStackPos];
			int pos = bufferPos[bufferStackPos];
	
			switch (buffer[pos]) {
				case 'L':
					if (pos + 1 < limit && buffer[pos + 1] == '"')
						return scanString();
					else {
						IToken t = scanIdentifier();
						if (t instanceof MacroExpansionToken)
							continue;
						else
							return t;
					}
				
				case '"':
					return scanString();
					
				case '\'':
					return scanCharLiteral();

				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':
				case 'm':
				case 'n':
				case 'o':
				case 'p':
				case 'q':
				case 'r':
				case 's':
				case 't':
				case 'u':
				case 'v':
				case 'w':
				case 'x':
				case 'y':
				case 'z':
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
				case 'H':
				case 'I':
				case 'J':
				case 'K':
				case 'M':
				case 'N':
				case 'O':
				case 'P':
				case 'Q':
				case 'R':
				case 'S':
				case 'T':
				case 'U':
				case 'V':
				case 'W':
				case 'X':
				case 'Y':
				case 'Z':
				case '_':
					IToken t = scanIdentifier();
					if (t instanceof MacroExpansionToken)
						continue;
					else
						return t;
				
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					return scanNumber();

				case '.':
					if (pos + 1 < limit) {
						switch (buffer[pos + 1]) {
							case '0':
							case '1':
							case '2':
							case '3':
							case '4':
							case '5':
							case '6':
							case '7':
							case '8':
							case '9':
								return scanNumber();
							
							case '.':
								if (pos + 2 < limit) {
									if (buffer[pos + 2] == '.') {
										bufferPos[bufferStackPos] += 2;
										return new SimpleToken(IToken.tELLIPSIS);
									}
								}
							case '*':
								++bufferPos[bufferStackPos];
								return new SimpleToken(IToken.tDOTSTAR);
						}
					}
					return new SimpleToken(IToken.tDOT);
					
				case '#':
					if (pos + 1 < limit && buffer[pos + 1] == '#') {
						++bufferPos[bufferStackPos];
						return new SimpleToken(IToken.tPOUNDPOUND);
					}
					
					// Should really check to make sure this is the first
					// non whitespace character on the line
					handlePPDirective();
					continue;
				
				case '{':
					return new SimpleToken(IToken.tLBRACE);
				
				case '}':
					return new SimpleToken(IToken.tRBRACE);
				
				case '[':
					return new SimpleToken(IToken.tLBRACKET);
				
				case ']':
					return new SimpleToken(IToken.tRBRACKET);
				
				case '(':
					return new SimpleToken(IToken.tLPAREN);
				
				case ')':
					return new SimpleToken(IToken.tRPAREN);

				case ';':
					return new SimpleToken(IToken.tSEMI);
				
				case ':':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == ':') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tCOLONCOLON);
						}
					}
					return new SimpleToken(IToken.tCOLON);
					
				case '?':
					return new SimpleToken(IToken.tQUESTION);
				
				case '+':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '+') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tINCR);
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tPLUSASSIGN);
						}
					}
					return new SimpleToken(IToken.tPLUS);
				
				case '-':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '>') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '*') {
									bufferPos[bufferStackPos] += 2;
									return new SimpleToken(IToken.tARROWSTAR);
								}
							}
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tARROW);
						} else if (buffer[pos + 1] == '-') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tDECR);
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tMINUSASSIGN);
						}
					}
					return new SimpleToken(IToken.tMINUS);
				
				case '*':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tSTARASSIGN);
						}
					}
					return new SimpleToken(IToken.tSTAR);
				
				case '/':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tDIVASSIGN);
						}
					}
					return new SimpleToken(IToken.tDIV);
				
				case '%':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tMODASSIGN);
						}
					}
					return new SimpleToken(IToken.tMOD);
				
				case '^':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tXORASSIGN);
						}
					}
					return new SimpleToken(IToken.tXOR);
				
				case '&':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '&') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tAND);
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tAMPERASSIGN);
						}
					}
					return new SimpleToken(IToken.tAMPER);
				
				case '|':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '|') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tOR);
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tBITORASSIGN);
						}
					}
					return new SimpleToken(IToken.tBITOR);
				
				case '~':
					return new SimpleToken(IToken.tCOMPL);
				
				case '!':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tNOTEQUAL);
						}
					}
					return new SimpleToken(IToken.tNOT);
				
				case '=':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tEQUAL);
						}
					}
					return new SimpleToken(IToken.tASSIGN);
				
				case '<':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tLTEQUAL);
						} else if (buffer[pos + 1] == '<') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '=') {
									bufferPos[bufferStackPos] += 2;
									return new SimpleToken(IToken.tSHIFTLASSIGN);
								}
							}
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tSHIFTL);
						}
					}
					return new SimpleToken(IToken.tLT);
				
				case '>':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tGTEQUAL);
						} else if (buffer[pos + 1] == '>') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '=') {
									bufferPos[bufferStackPos] += 2;
									return new SimpleToken(IToken.tSHIFTRASSIGN);
								}
							}
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tSHIFTR);
						}
					}
					return new SimpleToken(IToken.tGT);
				
				case ',':
					return new SimpleToken(IToken.tCOMMA);

				default:
					// skip over anything we don't handle
			}
		}

	// We've run out of contexts, our work is done here
		return null;
	}

	private IToken scanIdentifier() {
		char[] buffer = bufferStack[bufferStackPos];
		int start = bufferPos[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		int len = 1;
		
		while (++bufferPos[bufferStackPos] < limit) {
			char c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9')) {
				++len;
				continue;
			} else {
				break;
			}
		}

		--bufferPos[bufferStackPos];
		
		// Check for macro expansion
		Object expObject = null;
		if (bufferData[bufferStackPos] instanceof FunctionStyleMacro.Expansion) {
			// first check if name is a macro arg
			expObject = ((FunctionStyleMacro.Expansion)bufferData[bufferStackPos])
				.definitions.get(buffer, start, len);
		}

		if (expObject == null) {
			// now check regular macros
			expObject = definitions.get(buffer, start, len);
			
			// but not if it has been expanded on the stack already
			// i.e. recursion avoidance
			if (expObject != null)
				for (int stackPos = bufferStackPos; stackPos >= 0; --stackPos)
					if (bufferData[stackPos] != null
							&& bufferData[stackPos] instanceof ObjectStyleMacro
							&& CharArrayUtils.equals(buffer, start, len,
									((ObjectStyleMacro)bufferData[stackPos]).name)) {
						expObject = null;
						break;
					}
		}
		
		if (expObject != null) {
			if (expObject instanceof FunctionStyleMacro) {
				handleFunctionStyleMacro((FunctionStyleMacro)expObject);
			} else if (expObject instanceof ObjectStyleMacro) {
				ObjectStyleMacro expMacro = (ObjectStyleMacro)expObject;
				char[] expText = expMacro.expansion;
				if (expText.length > 0)
					pushContext(expText, expMacro);
			} else if (expObject instanceof char[]) {
				char[] expText = (char[])expObject;
				if (expText.length > 0)
					pushContext(expText);
			}
			return new MacroExpansionToken();
		}
		
		int tokenType = keywords.get(buffer, start, len);
		if (tokenType == keywords.undefined)
			return new ImagedToken(IToken.tIDENTIFIER, new String(buffer, start, len));
		else
			return new SimpleToken(tokenType);
	}
	
	private IToken scanString() {
		char[] buffer = bufferStack[bufferStackPos];
		
		int tokenType = IToken.tSTRING;
		if (buffer[bufferPos[bufferStackPos]] == 'L') {
			++bufferPos[bufferStackPos];
			tokenType = IToken.tLSTRING;
		}
		
		int stringStart = bufferPos[bufferStackPos] + 1;
		int stringLen = 0;
		boolean escaped = false;
		loop:
		while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
			++stringLen;
			char c = buffer[bufferPos[bufferStackPos]];
			if (c == '"') {
				if (!escaped)
					break;
				}
			else if (c == '\\') {
				escaped = !escaped;
				continue;
			}
			escaped = false;
		}
		--stringLen;

		// We should really throw an exception if we didn't get the terminating
		// quote before the end of buffer
		
		return new ImagedToken(tokenType, new String(buffer, stringStart, stringLen));
	}

	private IToken scanCharLiteral() {
		char[] buffer = bufferStack[bufferStackPos];
		int start = bufferPos[bufferStackPos] + 1;
		int limit = bufferLimit[bufferStackPos];

		if (start >= limit) {
			return new ImagedToken(IToken.tCHAR, new String(emptyCharArray));
		}

		int length = 0;
		boolean escaped = false;
		while (++bufferPos[bufferStackPos] < limit) {
			++length;
			int pos = bufferPos[bufferStackPos];
			if (buffer[pos] == '\'') {
				if (!escaped)
					break;
			} else if (buffer[pos] == '\\') {
				escaped = !escaped;
				continue;
			}
			escaped = false;
		}
		--length;
		
		char[] image = length > 0
			? CharArrayUtils.extract(buffer, start, length)
			: emptyCharArray;

		return new ImagedToken(IToken.tCHAR, new String(image));
	}
	
	private IToken scanNumber() {
		char[] buffer = bufferStack[bufferStackPos];
		int start = bufferPos[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		boolean isFloat = buffer[start] == '.';
		boolean hasExponent = false;
		
		boolean isHex = false;
		if (buffer[start] == '0' && start + 1 < limit) {
			switch (buffer[start + 1]) {
				case 'x':
				case 'X':
					isHex = true;
					++bufferPos[bufferStackPos];
			}
		}
		
		while (++bufferPos[bufferStackPos] < limit) {
			int pos = bufferPos[bufferStackPos];
			switch (buffer[pos]) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					continue;
				
				case '.':
					if (isFloat)
						// second dot
						break;
					
					isFloat = true;
					continue;
				
				case 'E':
				case 'e':
					if (isHex)
						// a hex 'e'
						continue;
					
					if (hasExponent)
						// second e
						break;
				
					if (pos + 1 >= limit)
						// ending on the e?
						break;
					
					switch (buffer[pos + 1]) {
						case '+':
						case '-':
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							// looks like a good exponent
							isFloat = true;
							hasExponent = true;
							++bufferPos[bufferStackPos];
							continue;
						default:
							// no exponent number?
							break;
					}
					break;
				
				case 'a':
				case 'A':
				case 'b':
				case 'B':
				case 'c':
				case 'C':
				case 'd':
				case 'D':
					if (isHex)
						continue;
					
					// not ours
					--bufferPos[bufferStackPos];
					break;
					
				case 'f':
				case 'F':
					if (isHex)
						continue;
					
					// must be float suffix
					break;

				case 'p':
				case 'P':
					// Hex float exponent prefix
					if (!isFloat || !isHex) {
						--bufferPos[bufferStackPos];
						break;
					}
					
					if (hasExponent)
						// second p
						break;
				
					if (pos + 1 >= limit)
						// ending on the p?
						break;
					
					switch (buffer[pos + 1]) {
						case '+':
						case '-':
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							// looks like a good exponent
							isFloat = true;
							hasExponent = true;
							++bufferPos[bufferStackPos];
							continue;
						default:
							// no exponent number?
							break;
					}
					break;
 
				case 'u':
				case 'U':
					// unsigned suffix
					break;
				
				case 'l':
				case 'L':
					if (pos + 1 < limit) {
						switch (buffer[pos + 1]) {
							case 'l':
							case 'L':
								// long long
								++bufferPos[bufferStackPos];
						}
					}
					// long or long long
					break;
					
				default:
					// not part of our number
			}
			
			// If we didn't continue in the switch, we're done
			break;
		}
		
		--bufferPos[bufferStackPos];
		
		return new ImagedToken(isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER,
				new String(buffer, start, bufferPos[bufferStackPos] - start + 1));
	}
	
	private void handlePPDirective() throws ScannerException {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
	
		skipOverWhiteSpace();
		
		// find the directive
		int start = ++bufferPos[bufferStackPos];
		
		// if new line or end of buffer, we're done
		if (start >= limit || buffer[start] == '\n')
			return;

		char c = buffer[start];
		if ((c >= 'a' && c <= 'z')) {
			while (++bufferPos[bufferStackPos] < limit) {
				c = buffer[bufferPos[bufferStackPos]];
				if ((c >= 'a' && c <= 'z') || c == '_')
					continue;
				else
					break;
			}
			--bufferPos[bufferStackPos];
			int len = bufferPos[bufferStackPos] - start + 1;
			int type = ppKeywords.get(buffer, start, len);
			if (type != ppKeywords.undefined) {
				switch (type) {
					case ppInclude:
						handlePPInclude(false);
						return;
					case ppInclude_next:
						handlePPInclude(true);
						return;
					case ppDefine:
						handlePPDefine();
						return;
					case ppUndef:
						handlePPUndef();
						return;
					case ppIfdef:
						handlePPIfdef(true);
						return;
					case ppIfndef:
						handlePPIfdef(false);
						return;
					case ppIf:
						start = bufferPos[bufferStackPos];
						skipToNewLine();
						len = bufferPos[bufferStackPos] - start;
						if (expressionEvaluator.evaluate(buffer, start, len, definitions) == 0)
							skipOverConditionalCode(true);
						return;
					case ppElse:
					case ppElif:
						// Condition must have been true, skip over the rest
						skipToNewLine();
						skipOverConditionalCode(false);
						return;
					case ppError:
						throw new ScannerException(null);
				}
			}
		}

		// don't know, chew up to the end of line
		// includes endif which is immatereal at this point
		skipToNewLine();
	}		

	private void handlePPInclude(boolean next) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		skipOverWhiteSpace();
		
		int pos = ++bufferPos[bufferStackPos];
		if (pos >= limit)
			return;

		boolean local = false;
		String filename = null;
		char c = buffer[pos];
		if (c == '"') {
			local = true;
			int start = bufferPos[bufferStackPos] + 1;
			int length = 0;
			boolean escaped = false;
			while (++bufferPos[bufferStackPos] < limit) {
				++length;
				c = buffer[bufferPos[bufferStackPos]];
				if (c == '"') {
					if (!escaped)
						break;
				} else if (c == '\\') {
					escaped = !escaped;
					continue;
				}
				escaped = false;
			}
			--length;
			
			filename = new String(buffer, start, length);
		} else if (c == '<') {
			local = false;
			int start = bufferPos[bufferStackPos] + 1;
			int length = 0;

			while (++bufferPos[bufferStackPos] < limit &&
					buffer[bufferPos[bufferStackPos]] != '>')
				++length;

			filename = new String(buffer, start, length);
		}
		// TODO else we need to do macro processing on the rest of the line

		skipToNewLine();

		CodeReader reader = null;
		
		if (local) {
			
		}
		
		// iterate through the include paths
		// foundme has odd logic but if we're not include_next, then we are looking for the
		// first occurance, otherwise, we're looking for the one after us
		boolean foundme = !next;
		if (includePaths != null)
			for (int i = 0; i < includePaths.length; ++i) {
				String finalPath = ScannerUtility.createReconciledPath(includePaths[i], filename);
				if (!foundme) {
					if (finalPath.equals(((CodeReader)bufferData[bufferStackPos]).filename)) {
						foundme = true;
						continue;
					}
				} else {
					reader = (CodeReader)fileCache.get(finalPath);
					if (reader == null) {
						reader = ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
						if (reader != null) {
							if (reader.filename != null)
								fileCache.put(reader.filename, reader);
							pushContext(reader.buffer, reader);
							return;
						}
					}
				}
			}
		
		// TODO raise a problem
		//if (reader == null)
		//	handleProblem( IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, filename, beginOffset, false, true );
		
	}
	
	private void handlePPDefine() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		skipOverWhiteSpace();
		
		// get the Identifier
		int idstart = ++bufferPos[bufferStackPos];
		if (idstart >= limit)
			return;
		
		char c = buffer[idstart];
		if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_')) {
			skipToNewLine();
			return;
		}

		int idlen = 1;
		while (++bufferPos[bufferStackPos] < limit) {
			c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9')) {
				++idlen;
				continue;
			} else {
				break;
			}
		}
		--bufferPos[bufferStackPos];
		char[] name = new char[idlen];
		System.arraycopy(buffer, idstart, name, 0, idlen);
		
		// Now check for function style macro to store the arguments
		char[][] arglist = null;
		int pos = bufferPos[bufferStackPos];
		if (pos + 1 < limit && buffer[pos + 1] == '(') {
			++bufferPos[bufferStackPos];
			arglist = new char[4][];
			int currarg = -1;
			while (bufferPos[bufferStackPos] < limit) {
				skipOverWhiteSpace();
				if (++bufferPos[bufferStackPos] >= limit)
					return;
				c = buffer[bufferPos[bufferStackPos]];
				if (c == ')') {
					break;
				} else if (c == ',') {
					continue;
				} else if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_')) {
					// yuck
					skipToNewLine();
					return;
				}
				int argstart = bufferPos[bufferStackPos];
				skipOverIdentifier();
				if (++currarg == arglist.length) {
					char[][] oldarglist = arglist;
					arglist = new char[oldarglist.length * 2][];
					System.arraycopy(oldarglist, 0, arglist, 0, oldarglist.length);
				}
				int arglen = bufferPos[bufferStackPos] - argstart + 1;
				char[] arg = new char[arglen];
				System.arraycopy(buffer, argstart, arg, 0, arglen);
				arglist[currarg] = arg;
			}
		}

		// Capture the replacement text
		skipOverWhiteSpace();
		int textstart = bufferPos[bufferStackPos] + 1;
		int textend = textstart - 1;
		
		while (bufferPos[bufferStackPos] + 1 < limit
				&& buffer[bufferPos[bufferStackPos] + 1] != '\n') {
			skipOverNonWhiteSpace();
			textend = bufferPos[bufferStackPos];
			skipOverWhiteSpace();
		}

		int textlen = textend - textstart + 1;
		char[] text = emptyCharArray;
		if (textlen > 0) {
			text = new char[textlen];
			System.arraycopy(buffer, textstart, text, 0, textlen);
		}
			
		// Throw it in
		definitions.put(name,
				arglist == null
				? new ObjectStyleMacro(name, text)
				: new FunctionStyleMacro(name, text, arglist));
	}
	
	private void handlePPUndef() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		skipOverWhiteSpace();
		
		// get the Identifier
		int idstart = ++bufferPos[bufferStackPos];
		if (idstart >= limit)
			return;
		
		char c = buffer[idstart];
		if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_')) {
			skipToNewLine();
			return;
		}

		int idlen = 1;
		while (++bufferPos[bufferStackPos] < limit) {
			c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9')) {
				++idlen;
				continue;
			} else {
				break;
			}
		}
		--bufferPos[bufferStackPos];

		skipToNewLine();
		
		definitions.remove(buffer, idstart, idlen);
	}
	
	private void handlePPIfdef(boolean positive) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		skipOverWhiteSpace();
		
		// get the Identifier
		int idstart = ++bufferPos[bufferStackPos];
		if (idstart >= limit)
			return;
		
		char c = buffer[idstart];
		if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_')) {
			skipToNewLine();
			return;
		}

		int idlen = 1;
		while (++bufferPos[bufferStackPos] < limit) {
			c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9')) {
				++idlen;
				continue;
			} else {
				break;
			}
		}
		--bufferPos[bufferStackPos];

		skipToNewLine();
		
		if ((definitions.get(buffer, idstart, idlen) != null) == positive)
			// continue on
			return;

		// skip over this group
		skipOverConditionalCode(true);
	}

	// checkelse - if potential for more, otherwise skip to endif
	private void skipOverConditionalCode(boolean checkelse) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		int nesting = 0;
		
		while (bufferPos[bufferStackPos] < limit) {
			skipOverWhiteSpace();
			char c = buffer[++bufferPos[bufferStackPos]];
			if (c == '#') {
				skipOverWhiteSpace();
				
				// find the directive
				int start = ++bufferPos[bufferStackPos];
				
				// if new line or end of buffer, we're done
				if (start >= limit || buffer[start] == '\n')
					continue;
				
				c = buffer[start];
				if ((c >= 'a' && c <= 'z')) {
					while (++bufferPos[bufferStackPos] < limit) {
						c = buffer[bufferPos[bufferStackPos]];
						if ((c >= 'a' && c <= 'z'))
							continue;
						else
							break;
					}
					--bufferPos[bufferStackPos];
					int len = bufferPos[bufferStackPos] - start + 1;
					int type = ppKeywords.get(buffer, start, len);
					if (type != ppKeywords.undefined) {
						switch (type) {
							case ppIfdef:
							case ppIfndef:
								++nesting;
								break;
							case ppElse:
								if (checkelse && nesting == 0) {
									skipToNewLine();
									return;
								}
								break;
							case ppElif:
								if (checkelse && nesting == 0) {
									// check the condition
									start = bufferPos[bufferStackPos];
									skipToNewLine();
									len = bufferPos[bufferStackPos] - start;
									if (expressionEvaluator.evaluate(buffer, start, len, definitions) != 0)
										// condition passed, we're good
										return;
								}
								break;
							case ppEndif:
								if (nesting > 0) {
									--nesting;
								} else {
									skipToNewLine();
									return;
								}
								break;
						}
					}
				}
			}
			
			skipToNewLine();
		}
	}
	
	private void skipOverWhiteSpace() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		while (++bufferPos[bufferStackPos] < limit) {
			int pos = bufferPos[bufferStackPos];
			switch (buffer[pos]) {
				case ' ':
				case '\t':
				case '\r':
					continue;
				case '/':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '/') {
							// C++ comment, skip rest of line
							skipToNewLine();
							return;
						} else if (buffer[pos + 1] == '*') {
							// C comment, find closing */
							for (bufferPos[bufferStackPos] += 2;
									bufferPos[bufferStackPos] < limit;
									++bufferPos[bufferStackPos]) {
								pos = bufferPos[bufferStackPos];
								if (buffer[pos] == '*'
										&& pos + 1 < limit
										&& buffer[pos + 1] == '/') {
									++bufferPos[bufferStackPos];
									break;
								}
							}
						}
					}
					continue;
				case '\\':
					if (pos + 1 < limit && buffer[pos + 1] == '\n') {
						// \n is a whitespace
						++bufferPos[bufferStackPos];
						continue;
					}
			}
			
			// fell out of switch without continuing, we're done
			--bufferPos[bufferStackPos];
			return;
		}
	}
	
	private void skipOverNonWhiteSpace() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		while (++bufferPos[bufferStackPos] < limit) {
			switch (buffer[bufferPos[bufferStackPos]]) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					--bufferPos[bufferStackPos];
					return;
				case '\\':
					int pos = bufferPos[bufferStackPos];
					if (pos + 1 < limit && buffer[pos + 1] == '\n') {
						// \n is whitespace
						--bufferPos[bufferStackPos];
						return;
					}
					break;
				case '"':
					boolean escaped = false;
					loop:
					while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case '\\':
								escaped = !escaped;
								continue;
							case '"':
								if (escaped) {
									escaped = false;
									continue;
								}
								break loop;
							default:
								escaped = false;
						}
					}

			}
		}
		--bufferPos[bufferStackPos];
	}

	private void skipOverMacroArg() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		while (++bufferPos[bufferStackPos] < limit) {
			switch (buffer[bufferPos[bufferStackPos]]) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
				case ',':
				case ')':
				case '(':
					--bufferPos[bufferStackPos];
					return;
				case '\\':
					int pos = bufferPos[bufferStackPos];
					if (pos + 1 < limit && buffer[pos + 1] == '\n') {
						// \n is whitespace
						--bufferPos[bufferStackPos];
						return;
					}
					break;
				case '"':
					boolean escaped = false;
					loop:
					while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case '\\':
								escaped = !escaped;
								continue;
							case '"':
								if (escaped) {
									escaped = false;
									continue;
								}
								break loop;
							default:
								escaped = false;
						}
					}
					break;
			}
		}
		--bufferPos[bufferStackPos];
	}

	private void skipOverIdentifier() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		while (++bufferPos[bufferStackPos] < limit) {
			char c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9')) {
				continue;
			} else {
				break;
			}

		}
		--bufferPos[bufferStackPos];
	}

	private void skipToNewLine() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		int pos = bufferPos[bufferStackPos];
		if (pos >= limit || buffer[pos] == '\n')
			return;
		
		boolean escaped = false;
		boolean inComment = false;
		while (++bufferPos[bufferStackPos] < limit) {
			switch (buffer[bufferPos[bufferStackPos]]) {
				case '/':
					pos = bufferPos[bufferStackPos];
					if (pos + 1 < limit && buffer[pos + 1] == '*') {
						++bufferPos[bufferStackPos];
						while (++bufferPos[bufferStackPos] < limit) {
							pos = bufferPos[bufferStackPos];
							if (buffer[pos] == '*'
									&& pos + 1 < limit
									&& buffer[pos + 1] == '/') {
								++bufferPos[bufferStackPos];
								break;
							}
						}
					}
					break;
				case '\\':
					escaped = !escaped;
					continue;
				case '\n':
					if (escaped) {
						escaped = false;
						break;
					} else { 
						return;
					}
			}
			escaped = false;
		}
	}
	
	private void handleFunctionStyleMacro(FunctionStyleMacro macro) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		if (++bufferPos[bufferStackPos] >= limit
				|| buffer[bufferPos[bufferStackPos]] != '(')
			return;

		FunctionStyleMacro.Expansion exp = macro.new Expansion();
		char[][] arglist = macro.arglist;
		int currarg = -1;

		while (bufferPos[bufferStackPos] < limit) {
			if (++currarg >= arglist.length || arglist[currarg] == null)
				// too many args
				break;

			skipOverWhiteSpace();
			
			int pos = ++bufferPos[bufferStackPos];
			char c = buffer[pos];
			if (c == ')') {
				// end of macro
				break;
			} else if (c == ',') {
				// empty arg
				exp.definitions.put(arglist[currarg], emptyCharArray);
				continue;
			}
			
			// peel off the arg
			--bufferPos[bufferStackPos];
			int argend = bufferPos[bufferStackPos];
			int argstart = argend + 1;
			
			// Loop looking for end of argument
			int argparens = 0;
			while (bufferPos[bufferStackPos] < limit) {
				skipOverMacroArg();
				argend = bufferPos[bufferStackPos];
				skipOverWhiteSpace();
				
				if (++bufferPos[bufferStackPos] >= limit)
					break;
				c = buffer[bufferPos[bufferStackPos]];
				if (c == '(')
					++argparens;
				else if (c == ')') {
					if (argparens == 0)
						break;
					--argparens;
				} else if (c == ',')
					break;
			}
			
			char[] arg = emptyCharArray;
			int arglen = argend - argstart + 1;
			if (arglen > 0) {
				arg = new char[arglen];
				System.arraycopy(buffer, argstart, arg, 0, arglen);
			}
			exp.definitions.put(arglist[currarg], arg);
			
			if (c == ')')
				break;
		}
		
		char[] expText = macro.expansion;
		if (expText.length > 0)
			pushContext(expText, exp);
	}

	// gcc built-ins
	private static final ObjectStyleMacro __cplusplus
		= new ObjectStyleMacro("__cplusplus".toCharArray(), "1".toCharArray());
	private static final ObjectStyleMacro __STDC_HOSTED__
		= new ObjectStyleMacro("__STDC_HOSTED__".toCharArray(), "0".toCharArray());
	private static final ObjectStyleMacro __STDC_VERSION__
		= new ObjectStyleMacro("__STDC_VERSION__".toCharArray(), "199001L".toCharArray());
	private static final FunctionStyleMacro __attribute__
		= new FunctionStyleMacro(
				"__attribute__".toCharArray(),
				emptyCharArray,
				new char[][] { "arg".toCharArray() });
	
	// standard built-ins
	private static final ObjectStyleMacro __STDC__
		= new ObjectStyleMacro("__STDC__".toCharArray(), "1".toCharArray());
	
	protected void setupBuiltInMacros() {
		
		// gcc extensions
		if( language == ParserLanguage.CPP )
			definitions.put(__cplusplus.name, __cplusplus);
		definitions.put(__STDC_HOSTED__.name, __STDC_HOSTED__);
		definitions.put(__STDC_VERSION__.name, __STDC_VERSION__);
		definitions.put(__attribute__.name, __attribute__);
		
		/*
		
		// add these to private table
		if( scannerData.getScanner().getDefinition( __ATTRIBUTE__) == null )
			scannerData.getPrivateDefinitions().put( __ATTRIBUTE__, ATTRIBUTE_MACRO); 
		
		if( scannerData.getScanner().getDefinition( __DECLSPEC) == null )
			scannerData.getPrivateDefinitions().put( __DECLSPEC, DECLSPEC_MACRO );

		if( scannerData.getScanner().getDefinition( __EXTENSION__ ) == null )
			scannerData.getPrivateDefinitions().put( __EXTENSION__, EXTENSION_MACRO);
		
		if( scannerData.getScanner().getDefinition( __CONST__ ) == null )
		scannerData.getPrivateDefinitions().put( __CONST__, __CONST__MACRO);
		if( scannerData.getScanner().getDefinition( __CONST ) == null )
		scannerData.getPrivateDefinitions().put( __CONST, __CONST_MACRO);
		if( scannerData.getScanner().getDefinition( __INLINE__ ) == null )
		scannerData.getPrivateDefinitions().put( __INLINE__, __INLINE__MACRO);
		if( scannerData.getScanner().getDefinition( __SIGNED__ ) == null )
		scannerData.getPrivateDefinitions().put( __SIGNED__, __SIGNED__MACRO);
		if( scannerData.getScanner().getDefinition( __VOLATILE__ ) == null )
		scannerData.getPrivateDefinitions().put( __VOLATILE__, __VOLATILE__MACRO);
		ObjectMacroDescriptor __RESTRICT_MACRO = new ObjectMacroDescriptor( __RESTRICT, Keywords.RESTRICT );
		if( scannerData.getScanner().getDefinition( __RESTRICT ) == null )
		scannerData.getPrivateDefinitions().put( __RESTRICT, __RESTRICT_MACRO);
		if( scannerData.getScanner().getDefinition( __RESTRICT__ ) == null )
		scannerData.getPrivateDefinitions().put( __RESTRICT__, __RESTRICT__MACRO);
		if( scannerData.getScanner().getDefinition( __TYPEOF__ ) == null )
		scannerData.getPrivateDefinitions().put( __TYPEOF__, __TYPEOF__MACRO);
		if( scannerData.getLanguage() == ParserLanguage.CPP )
			if( scannerData.getScanner().getDefinition( __ASM__ ) == null )
			scannerData.getPrivateDefinitions().put( __ASM__, __ASM__MACRO);
		*/
		
		// standard extensions
		definitions.put(__STDC__.name, __STDC__);

		/*
		if( getDefinition(__STDC__) == null )
			addDefinition( __STDC__, STDC_MACRO ); 
		
		if( language == ParserLanguage.C )
		{
			if( getDefinition(__STDC_HOSTED__) == null )
				addDefinition( __STDC_HOSTED__, STDC_HOSTED_MACRO); 
			if( getDefinition( __STDC_VERSION__) == null )
				addDefinition( __STDC_VERSION__, STDC_VERSION_MACRO); 
		}
		else
			if( getDefinition( __CPLUSPLUS ) == null )
					addDefinition( __CPLUSPLUS, CPLUSPLUS_MACRO); //$NON-NLS-1$
		
		if( getDefinition(__FILE__) == null )
			addDefinition(  __FILE__, 
					new DynamicMacroDescriptor( __FILE__, new DynamicMacroEvaluator() {
						public String execute() {
							return contextStack.getMostRelevantFileContext().getContextName();
						}				
					} ) );
		
		if( getDefinition( __LINE__) == null )
			addDefinition(  __LINE__, 
					new DynamicMacroDescriptor( __LINE__, new DynamicMacroEvaluator() {
						public String execute() {
							return new Integer( contextStack.getCurrentLineNumber() ).toString();
						}				
			} ) );
		
		
		if( getDefinition(  __DATE__ ) == null )
			addDefinition(  __DATE__, 
					new DynamicMacroDescriptor( __DATE__, new DynamicMacroEvaluator() {
						
						public String getMonth()
						{
							if( Calendar.MONTH == Calendar.JANUARY ) return  "Jan" ; //$NON-NLS-1$
							if( Calendar.MONTH == Calendar.FEBRUARY) return "Feb"; //$NON-NLS-1$
							if( Calendar.MONTH == Calendar.MARCH) return "Mar"; //$NON-NLS-1$
							if( Calendar.MONTH == Calendar.APRIL) return "Apr"; //$NON-NLS-1$
							if( Calendar.MONTH == Calendar.MAY) return "May"; //$NON-NLS-1$
							if( Calendar.MONTH == Calendar.JUNE) return "Jun"; //$NON-NLS-1$
							if( Calendar.MONTH ==  Calendar.JULY) return "Jul"; //$NON-NLS-1$
							if( Calendar.MONTH == Calendar.AUGUST) return "Aug"; //$NON-NLS-1$
							if( Calendar.MONTH ==  Calendar.SEPTEMBER) return "Sep"; //$NON-NLS-1$
							if( Calendar.MONTH ==  Calendar.OCTOBER) return "Oct"; //$NON-NLS-1$
							if( Calendar.MONTH ==  Calendar.NOVEMBER) return "Nov"; //$NON-NLS-1$
							if( Calendar.MONTH ==  Calendar.DECEMBER) return "Dec"; //$NON-NLS-1$
							return ""; //$NON-NLS-1$
						}
						
						public String execute() {
							StringBuffer result = new StringBuffer();
							result.append( getMonth() );
							result.append(" "); //$NON-NLS-1$
							if( Calendar.DAY_OF_MONTH < 10 )
								result.append(" "); //$NON-NLS-1$
							result.append(Calendar.DAY_OF_MONTH);
							result.append(" "); //$NON-NLS-1$
							result.append( Calendar.YEAR );
							return result.toString();
						}				
					} ) );
		
		if( getDefinition( __TIME__) == null )
			addDefinition(  __TIME__, 
					new DynamicMacroDescriptor( __TIME__, new DynamicMacroEvaluator() {
						
						
						public String execute() {
							StringBuffer result = new StringBuffer();
							if( Calendar.AM_PM == Calendar.PM )
								result.append( Calendar.HOUR + 12 );
							else
							{	
								if( Calendar.HOUR < 10 )
									result.append( '0');
								result.append(Calendar.HOUR);
							}
							result.append(':');
							if( Calendar.MINUTE < 10 )
								result.append( '0');
							result.append(Calendar.MINUTE);
							result.append(':');
							if( Calendar.SECOND < 10 )
								result.append( '0');
							result.append(Calendar.SECOND);
							return result.toString();
						}				
					} ) );
		
		*/
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#nextToken(boolean)
	 */
	public IToken nextToken(boolean next) throws ScannerException,
			EndOfFileException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#nextTokenForStringizing()
	 */
	public IToken nextTokenForStringizing() throws ScannerException,
			EndOfFileException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#overwriteIncludePath(java.lang.String[])
	 */
	public void overwriteIncludePath(String[] newIncludePaths) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#setASTFactory(org.eclipse.cdt.core.parser.ast.IASTFactory)
	 */
	public void setASTFactory(IASTFactory f) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setOffsetBoundary(int)
	 */
	public void setOffsetBoundary(int offset) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setScannerContext(org.eclipse.cdt.internal.core.parser.scanner.IScannerContext)
	 */
	public void setScannerContext(IScannerContext context) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setThrowExceptionOnBadCharacterRead(boolean)
	 */
	public void setThrowExceptionOnBadCharacterRead(boolean throwOnBad) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setTokenizingMacroReplacementList(boolean)
	 */
	public void setTokenizingMacroReplacementList(boolean b) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getASTFactory()
	 */
	public IASTFactory getASTFactory() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getBranchTracker()
	 */
	public BranchTracker getBranchTracker() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getClientRequestor()
	 */
	public ISourceElementRequestor getClientRequestor() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getContextStack()
	 */
	public ContextStack getContextStack() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getFileCache()
	 */
	public Map getFileCache() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getIncludePathNames()
	 */
	public List getIncludePathNames() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getInitialReader()
	 */
	public CodeReader getInitialReader() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getLanguage()
	 */
	public ParserLanguage getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getLogService()
	 */
	public IParserLogService getLogService() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getOriginalConfig()
	 */
	public IScannerInfo getOriginalConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getParserMode()
	 */
	public ParserMode getParserMode() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getPrivateDefinitions()
	 */
	public Map getPrivateDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getProblemFactory()
	 */
	public IProblemFactory getProblemFactory() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getPublicDefinitions()
	 */
	public Map getPublicDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getScanner()
	 */
	public IScanner getScanner() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getWorkingCopies()
	 */
	public Iterator getWorkingCopies() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#parseInclusionDirective(java.lang.String, int)
	 */
	public InclusionDirective parseInclusionDirective(String restOfLine,
			int offset) throws InclusionParseException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#setDefinitions(java.util.Map)
	 */
	public void setDefinitions(Map map) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#setIncludePathNames(java.util.List)
	 */
	public void setIncludePathNames(List includePathNames) {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getCurrentFileIndex()
	 */
	public int getCurrentFileIndex() {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getCurrentFilename()
	 */
	public char[] getCurrentFilename() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getFilenameForIndex(int)
	 */
	public String getFilenameForIndex(int index) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static CharArrayIntMap keywords;
	private static CharArrayIntMap ppKeywords;
	private static final int ppIf		= 0;
	private static final int ppIfdef	= 1;
	private static final int ppIfndef	= 2;
	private static final int ppElif		= 3;
	private static final int ppElse		= 4;
	private static final int ppEndif	= 5;
	private static final int ppInclude	= 6;
	private static final int ppDefine	= 7;
	private static final int ppUndef	= 8;
	private static final int ppError	= 9;
	private static final int ppInclude_next = 10;
	
	static {
		keywords = new CharArrayIntMap(IToken.tLAST, -1);
		
		// Common keywords
		keywords.put("auto".toCharArray(), IToken.t_auto);
		keywords.put("break".toCharArray(), IToken.t_break);
		keywords.put("case".toCharArray(), IToken.t_case);
		keywords.put("char".toCharArray(), IToken.t_char);
		keywords.put("const".toCharArray(), IToken.t_const);
		keywords.put("continue".toCharArray(), IToken.t_continue);
		keywords.put("default".toCharArray(), IToken.t_default);
		keywords.put("do".toCharArray(), IToken.t_do);
		keywords.put("double".toCharArray(), IToken.t_double);
		keywords.put("else".toCharArray(), IToken.t_else);
		keywords.put("enum".toCharArray(), IToken.t_enum);
		keywords.put("extern".toCharArray(), IToken.t_extern);
		keywords.put("float".toCharArray(), IToken.t_float);
		keywords.put("for".toCharArray(), IToken.t_for);
		keywords.put("goto".toCharArray(), IToken.t_goto);
		keywords.put("if".toCharArray(), IToken.t_if);
		keywords.put("inline".toCharArray(), IToken.t_inline);
		keywords.put("int".toCharArray(), IToken.t_int);
		keywords.put("long".toCharArray(), IToken.t_long);
		keywords.put("register".toCharArray(), IToken.t_register);
		keywords.put("return".toCharArray(), IToken.t_return);
		keywords.put("short".toCharArray(), IToken.t_short);
		keywords.put("signed".toCharArray(), IToken.t_signed);
		keywords.put("sizeof".toCharArray(), IToken.t_sizeof);
		keywords.put("static".toCharArray(), IToken.t_static);
		keywords.put("struct".toCharArray(), IToken.t_struct);
		keywords.put("switch".toCharArray(), IToken.t_switch);
		keywords.put("typedef".toCharArray(), IToken.t_typedef);
		keywords.put("union".toCharArray(), IToken.t_union);
		keywords.put("unsigned".toCharArray(), IToken.t_unsigned);
		keywords.put("void".toCharArray(), IToken.t_void);
		keywords.put("volatile".toCharArray(), IToken.t_volatile);
		keywords.put("while".toCharArray(), IToken.t_while);

		// ANSI C keywords
		keywords.put("restrict".toCharArray(), IToken.t_restrict);
		keywords.put("_Bool".toCharArray(), IToken.t__Bool);
		keywords.put("_Complex".toCharArray(), IToken.t__Complex);
		keywords.put("_Imaginary".toCharArray(), IToken.t__Imaginary);

		// C++ Keywords
		keywords.put("asm".toCharArray(), IToken.t_asm);
		keywords.put("bool".toCharArray(), IToken.t_bool);
		keywords.put("catch".toCharArray(), IToken.t_catch);
		keywords.put("class".toCharArray(), IToken.t_class);
		keywords.put("const_cast".toCharArray(), IToken.t_const_cast);
		keywords.put("delete".toCharArray(), IToken.t_delete);
		keywords.put("dynamic_cast".toCharArray(), IToken.t_dynamic_cast);
		keywords.put("explicit".toCharArray(), IToken.t_explicit);
		keywords.put("export".toCharArray(), IToken.t_export);
		keywords.put("false".toCharArray(), IToken.t_false);
		keywords.put("friend".toCharArray(), IToken.t_friend);
		keywords.put("mutable".toCharArray(), IToken.t_mutable);
		keywords.put("namespace".toCharArray(), IToken.t_namespace);
		keywords.put("new".toCharArray(), IToken.t_new);
		keywords.put("operator".toCharArray(), IToken.t_operator);
		keywords.put("private".toCharArray(), IToken.t_private);
		keywords.put("protected".toCharArray(), IToken.t_protected);
		keywords.put("public".toCharArray(), IToken.t_public);
		keywords.put("reinterpret_cast".toCharArray(), IToken.t_reinterpret_cast);
		keywords.put("static_cast".toCharArray(), IToken.t_static_cast);
		keywords.put("template".toCharArray(), IToken.t_template);
		keywords.put("this".toCharArray(), IToken.t_this);
		keywords.put("throw".toCharArray(), IToken.t_throw);
		keywords.put("true".toCharArray(), IToken.t_true);
		keywords.put("try".toCharArray(), IToken.t_try);
		keywords.put("typeid".toCharArray(), IToken.t_typeid);
		keywords.put("typename".toCharArray(), IToken.t_typename);
		keywords.put("using".toCharArray(), IToken.t_using);
		keywords.put("virtual".toCharArray(), IToken.t_virtual);
		keywords.put("wchar_t".toCharArray(), IToken.t_wchar_t);

		// C++ operator alternative
		keywords.put("and".toCharArray(), IToken.t_and);
		keywords.put("and_eq".toCharArray(), IToken.t_and_eq);
		keywords.put("bitand".toCharArray(), IToken.t_bitand);
		keywords.put("bitor".toCharArray(), IToken.t_bitor);
		keywords.put("compl".toCharArray(), IToken.t_compl);
		keywords.put("not".toCharArray(), IToken.t_not);
		keywords.put("not_eq".toCharArray(), IToken.t_not_eq);
		keywords.put("or".toCharArray(), IToken.t_or);
		keywords.put("or_eq".toCharArray(), IToken.t_or_eq);
		keywords.put("xor".toCharArray(), IToken.t_xor);
		keywords.put("xor_eq".toCharArray(), IToken.t_xor_eq);
		
		// Preprocessor keywords
		ppKeywords = new CharArrayIntMap(16, -1);
		ppKeywords.put("if".toCharArray(), ppIf);
		ppKeywords.put("ifdef".toCharArray(), ppIfdef);
		ppKeywords.put("ifndef".toCharArray(), ppIfndef);
		ppKeywords.put("elif".toCharArray(), ppElif);
		ppKeywords.put("else".toCharArray(), ppElse);
		ppKeywords.put("endif".toCharArray(), ppEndif);
		ppKeywords.put("include".toCharArray(), ppInclude);
		ppKeywords.put("define".toCharArray(), ppDefine);
		ppKeywords.put("undef".toCharArray(), ppUndef);
		ppKeywords.put("error".toCharArray(), ppError);
		ppKeywords.put("include_next".toCharArray(), ppInclude_next);
	}
}
