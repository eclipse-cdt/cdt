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

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
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
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.scanner.BranchTracker;
import org.eclipse.cdt.internal.core.parser.scanner.ContextStack;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerContext;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerData;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionDirective;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionParseException;
import org.eclipse.cdt.internal.core.parser.token.ImagedToken;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

/**
 * @author Doug Schaefer
 *
 */
public class Scanner2 implements IScanner, IScannerData {

	/**
	 * @author jcamelon
	 *
	 */
	private static class InclusionData {

		public final IASTInclusion inclusion;
		public final CodeReader reader;
		

		/**
		 * @param reader
		 * @param inclusion
		 */
		public InclusionData(CodeReader reader, IASTInclusion inclusion ) {
			this.reader = reader; 
			this.inclusion = inclusion;
		}
	}
	
	private ISourceElementRequestor requestor;
	
	private ParserLanguage language;
	protected IParserLogService log;
	private IScannerExtension scannerExtension;
	
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
	private int[] bufferLineNums = new int[bufferInitialSize];
	
	// Utility
	private static String[] emptyStringArray = new String[0];
	private static char[] emptyCharArray = new char[0];
	private static EndOfFileException EOF = new EndOfFileException();
	
	PrintStream dlog;

	private ParserMode parserMode;

	private List workingCopies;
	
	{
//		try {
//			dlog = new PrintStream(new FileOutputStream("C:/dlog.txt"));
//		} catch (FileNotFoundException e) {
//		}
	}

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
				String symbolName = keys[i];
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
			
			int [] oldBufferLineNums = bufferLineNums;
			bufferLineNums = new int[size];
			System.arraycopy( oldBufferLineNums, 0, bufferLineNums, 0, oldBufferLineNums.length);
			
		}
		
		bufferStack[bufferStackPos] = buffer;
		bufferPos[bufferStackPos] = -1;
		bufferLineNums[ bufferStackPos ] = 0;
		bufferLimit[bufferStackPos] = buffer.length;
	}
	
	private void pushContext(char[] buffer, Object data) {
		pushContext(buffer);
		bufferData[bufferStackPos] = data;
		if( data instanceof InclusionData )
			requestor.enterInclusion( ((InclusionData)data).inclusion ); 
	}
	
	private void popContext() {
		bufferStack[bufferStackPos] = null;
		if( bufferData[bufferStackPos] instanceof InclusionData )
			requestor.enterInclusion( ((InclusionData)bufferData[bufferStackPos]).inclusion );
		bufferData[bufferStackPos] = null;
		bufferLineNums[bufferStackPos] = 0;
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
		return Collections.EMPTY_MAP;
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
		return bufferStackPos == 0;
	}

	private IToken lastToken;
	private IToken nextToken;
	private boolean finished = false;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#nextToken()
	 */
	public IToken nextToken() throws ScannerException, EndOfFileException {
		if (finished)
		{
			if( offsetBoundary == -1 )
				throw EOF;			
			throwOLRE();
		}
		
		if (nextToken == null) {
			nextToken = fetchToken();
			if (nextToken == null)
			{
				if( offsetBoundary == -1 )
					throw EOF;
				throwOLRE();
			}
		}
		
		if (lastToken != null)
			lastToken.setNext(nextToken);
		IToken oldToken = lastToken;
		lastToken = nextToken;
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
				String t1 = lastToken.getImage();
				String t2 = token2.getImage();
				char[] pb = new char[t1.length() + t2.length()];
				t1.getChars(0, t1.length(), pb, 0);
				t2.getChars(0, t2.length(), pb, t1.length());
				pushContext(pb);
				lastToken = oldToken;
				nextToken = null;
				return nextToken();
			}
		} else if (lastToken.getType() == IToken.tSTRING || lastToken.getType() ==IToken.tLSTRING ) {
			while (nextToken != null && ( nextToken.getType() == IToken.tSTRING || nextToken.getType() == IToken.tLSTRING )) {
				// Concatenate the adjacent strings
				int tokenType = IToken.tSTRING; 
				if( lastToken.getType() == IToken.tLSTRING || nextToken.getType() == IToken.tLSTRING )
					tokenType = IToken.tLSTRING;
				lastToken = new ImagedToken(tokenType, (lastToken.getImage() + nextToken.getImage()).toCharArray(), nextToken.getEndOffset(), getCurrentFilename() ); //TODO Fix this
				if (oldToken != null)
					oldToken.setNext(lastToken);
				nextToken = fetchToken();
			}
		}
		
		return lastToken;
	}
	
	/**
	 * 
	 */
	private void throwOLRE() throws OffsetLimitReachedException {
		if( lastToken != null && lastToken.getEndOffset() != offsetBoundary )
			throw new OffsetLimitReachedException( (IToken)null );
		throw new OffsetLimitReachedException( lastToken );
	}

	// Return null to signify end of file
	private IToken fetchToken() throws ScannerException {
		++count;
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
				case '\n':
					++bufferLineNums[bufferStackPos];
					continue;
					
				case 'L':
					if (pos + 1 < limit && buffer[pos + 1] == '"')
						return scanString();
					if (pos + 1 < limit && buffer[pos + 1] == '\'')
						return scanCharLiteral(true);
					
					IToken t = scanIdentifier();
					if (t instanceof MacroExpansionToken)
						continue;
					return t;
					
				
				case '"':
					return scanString();
					
				case '\'':
					return scanCharLiteral(false);

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
					t = scanIdentifier();
					if (t instanceof MacroExpansionToken)
						continue;
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
										return new SimpleToken(IToken.tELLIPSIS, bufferPos[bufferStackPos] + 1 , getCurrentFilename()  );
									}
								}
							case '*':
								++bufferPos[bufferStackPos];
								return new SimpleToken(IToken.tDOTSTAR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tDOT, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
					
				case '#':
					if (pos + 1 < limit && buffer[pos + 1] == '#') {
						++bufferPos[bufferStackPos];
						return new SimpleToken(IToken.tPOUNDPOUND, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
					}
					
					// Should really check to make sure this is the first
					// non whitespace character on the line
					handlePPDirective(pos);
					continue;
				
				case '{':
					return new SimpleToken(IToken.tLBRACE, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '}':
					return new SimpleToken(IToken.tRBRACE, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '[':
					return new SimpleToken(IToken.tLBRACKET, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case ']':
					return new SimpleToken(IToken.tRBRACKET, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '(':
					return new SimpleToken(IToken.tLPAREN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case ')':
					return new SimpleToken(IToken.tRPAREN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );

				case ';':
					return new SimpleToken(IToken.tSEMI, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case ':':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == ':') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tCOLONCOLON, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tCOLON, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
					
				case '?':
					return new SimpleToken(IToken.tQUESTION, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '+':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '+') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tINCR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tPLUSASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tPLUS, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '-':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '>') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '*') {
									bufferPos[bufferStackPos] += 2;
									return new SimpleToken(IToken.tARROWSTAR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
								}
							}
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tARROW, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						} else if (buffer[pos + 1] == '-') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tDECR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tMINUSASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tMINUS, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '*':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tSTARASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tSTAR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '/':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tDIVASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tDIV, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '%':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tMODASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tMOD, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '^':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tXORASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tXOR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '&':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '&') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tAND, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tAMPERASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tAMPER, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '|':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '|') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tOR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tBITORASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tBITOR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '~':
					return new SimpleToken(IToken.tCOMPL, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '!':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tNOTEQUAL, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tNOT, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '=':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tEQUAL, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '<':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tLTEQUAL, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						} else if (buffer[pos + 1] == '<') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '=') {
									bufferPos[bufferStackPos] += 2;
									return new SimpleToken(IToken.tSHIFTLASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
								}
							}
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tSHIFTL, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tLT, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case '>':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tGTEQUAL, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						} else if (buffer[pos + 1] == '>') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '=') {
									bufferPos[bufferStackPos] += 2;
									return new SimpleToken(IToken.tSHIFTRASSIGN, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
								}
							}
							++bufferPos[bufferStackPos];
							return new SimpleToken(IToken.tSHIFTR, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
						}
					}
					return new SimpleToken(IToken.tGT, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
				
				case ',':
					return new SimpleToken(IToken.tCOMMA, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );

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
			}
			else if( c == '\\' && bufferPos[bufferStackPos] + 1 < buffer.length && buffer[ bufferPos[bufferStackPos] + 1 ] == '\n')
			{
				// escaped newline
				++bufferPos[bufferStackPos];
				len += 2;
				continue;
			}
			break;
		}

		--bufferPos[bufferStackPos];
		
		// Check for macro expansion
		Object expObject = definitions.get(buffer, start, len);
			
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
		
		if (expObject != null) {
			if (expObject instanceof FunctionStyleMacro) {
				handleFunctionStyleMacro((FunctionStyleMacro)expObject, true);
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
		char [] result = removedEscapedNewline( CharArrayUtils.extract( buffer, start, len ) );
		if (tokenType == keywords.undefined)
			return new ImagedToken(IToken.tIDENTIFIER, result, bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
		return new SimpleToken(tokenType, start + len, getCurrentFilename() );
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
		
		return new ImagedToken(tokenType, CharArrayUtils.extract(buffer, stringStart, stringLen), stringStart + stringLen+ 1, getCurrentFilename() );
	}

	private IToken scanCharLiteral(boolean b) {
		char[] buffer = bufferStack[bufferStackPos];
		int start = bufferPos[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];

		int tokenType = IToken.tCHAR;
		int length = 1;
		if (buffer[bufferPos[bufferStackPos]] == 'L') {
			++bufferPos[bufferStackPos];
			tokenType = IToken.tLCHAR;
			++length;
		}

		if (start >= limit) {
			return new ImagedToken(tokenType, emptyCharArray, start, getCurrentFilename() );
		}

		
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
		
		
		char[] image = length > 0
			? CharArrayUtils.extract(buffer, start, length)
			: emptyCharArray;

		return new ImagedToken(tokenType, image, start + length+ 1 , getCurrentFilename() );
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
					++bufferPos[bufferStackPos];
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
				case 'L':
				case 'l':
					// unsigned suffix
					suffixLoop: 
					while(++bufferPos[bufferStackPos]  < limit) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case 'U':
							case 'u':
							case 'l':
							case 'L':
								break;
							default:
								
								break suffixLoop;
						}
					}
					break;
					
				default:
					// not part of our number
			}
			
			// If we didn't continue in the switch, we're done
			break;
		}
		
		--bufferPos[bufferStackPos];
		
		return new ImagedToken(isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER,
				CharArrayUtils.extract(buffer, start,
						bufferPos[bufferStackPos] - start + 1), bufferPos[bufferStackPos]+ 1, getCurrentFilename() );
	}
	
	private void handlePPDirective(int pos) throws ScannerException {
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
				break;
			}
			--bufferPos[bufferStackPos];
			int len = bufferPos[bufferStackPos] - start + 1;
			int type = ppKeywords.get(buffer, start, len);
			if (type != ppKeywords.undefined) {
				switch (type) {
					case ppInclude:
						handlePPInclude(pos,false);
						return;
					case ppInclude_next:
						handlePPInclude(pos, true);
						return;
					case ppDefine:
						handlePPDefine(pos);
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
						if (expressionEvaluator.evaluate(buffer, start, len, definitions) == 0) {
							if (dlog != null) dlog.println("#if <FALSE> " + new String(buffer,start+1,len-1)); //$NON-NLS-1$
							skipOverConditionalCode(true);
						} else
							if (dlog != null) dlog.println("#if <TRUE> " + new String(buffer,start+1,len-1)); //$NON-NLS-1$
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

	private void handlePPInclude(int pos2, boolean next) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		skipOverWhiteSpace();
		int startOffset = pos2;
		int pos = ++bufferPos[bufferStackPos];
		if (pos >= limit)
			return;

		boolean local = false;
		String filename = null;
		
		int endOffset = startOffset;
		int nameOffset = 0;
		int nameEndOffset = 0;
		
		int nameLine= 0, startLine= 0, endLine = 0; 
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
			nameOffset = start;
			nameEndOffset = start + length;
			endOffset = start + length + 1;
		} else if (c == '<') {
			local = false;
			int start = bufferPos[bufferStackPos] + 1;
			int length = 0;

			while (++bufferPos[bufferStackPos] < limit &&
					buffer[bufferPos[bufferStackPos]] != '>')
				++length;
			endOffset = start + length + 1;
			nameOffset = start;
			nameEndOffset = start + length;

			filename = new String(buffer, start, length);
		}
		else
		{
			// handle macro expansions
			int startPos = pos;
			int len = 1;
			while (++bufferPos[bufferStackPos] < limit) {
				c = buffer[bufferPos[bufferStackPos]];
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
						|| c == '_' || (c >= '0' && c <= '9')) {
					++len;
					continue;
				}
				else if( c == '\\' && bufferPos[bufferStackPos] + 1 < buffer.length && buffer[ bufferPos[bufferStackPos] + 1 ] == '\n')
				{
					// escaped newline
					++bufferPos[bufferStackPos];
					len += 2;
					continue;
				}
				break;
			}
			
			
			Object expObject = definitions.get(buffer, startPos, len );
			
			if (expObject != null) {
				--bufferPos[bufferStackPos];
				if (expObject instanceof FunctionStyleMacro) 
				{
					filename = new String( handleFunctionStyleMacro((FunctionStyleMacro)expObject, false) );
				}
				else if ( expObject instanceof ObjectStyleMacro )
				{
					filename = new String( ((ObjectStyleMacro)expObject).expansion );
				}
			}
		}
		 
		if( filename == null || filename == EMPTY_STRING )
		{
			//TODO IProblem
			return;
		}
		// TODO else we need to do macro processing on the rest of the line

		skipToNewLine();

		if( parserMode == ParserMode.QUICK_PARSE )
		{
			IASTInclusion inclusion = getASTFactory().createInclusion( new String( filename ), EMPTY_STRING, local, startOffset, startLine, nameOffset, nameEndOffset, nameLine, endOffset, endLine, getCurrentFilename() );
			requestor.enterInclusion( inclusion );
			requestor.exitInclusion( inclusion );
		}
		else
		{
			CodeReader reader = null;
			
			if (local) {
				// create an include path reconciled to the current directory
				String finalPath = ScannerUtility.createReconciledPath( new File( new String( getCurrentFilename() ) ).getParentFile().getAbsolutePath(), filename );
				reader = (CodeReader)fileCache.get(finalPath);
				if (reader == null)
					reader = ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
				if (reader != null) {
					if (reader.filename != null)
						fileCache.put(reader.filename, reader);
					if (dlog != null) dlog.println("#include \"" + finalPath + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					IASTInclusion inclusion = getASTFactory().createInclusion( new String( filename ), new String( reader.filename ), local, startOffset, startLine, nameOffset, nameEndOffset, nameLine, endOffset, endLine, getCurrentFilename() );
					pushContext(reader.buffer, new InclusionData( reader, inclusion ));
					return;
				}
			}
			
			// iterate through the include paths
			// foundme has odd logic but if we're not include_next, then we are looking for the
			// first occurance, otherwise, we're looking for the one after us
			boolean foundme = !next;
			if (includePaths != null)
				for (int i = 0; i < includePaths.length; ++i) {
					String finalPath = ScannerUtility.createReconciledPath(includePaths[i], filename);
					if (!foundme) {
						if (finalPath.equals(((InclusionData)bufferData[bufferStackPos]).reader.filename)) {
							foundme = true;
							continue;
						}
					} else {
						reader = (CodeReader)fileCache.get(finalPath);
						if (reader == null)
							reader = ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
						if (reader != null) {
							if (reader.filename != null)
								fileCache.put(reader.filename, reader);
							if (dlog != null) dlog.println("#include <" + finalPath + ">"); //$NON-NLS-1$ //$NON-NLS-2$
							IASTInclusion inclusion = getASTFactory().createInclusion( new String( filename ), new String( reader.filename ), local, startOffset, startLine, nameOffset, nameEndOffset, nameLine, endOffset, endLine, getCurrentFilename() );
							pushContext(reader.buffer, new InclusionData( reader, inclusion ));
							return;
						}
					}
				}
		
			// TODO raise a problem
			//if (reader == null)
			//	handleProblem( IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, filename, beginOffset, false, true );
		}
		
	}
	
	private void handlePPDefine(int pos2) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		int startingOffset = pos2;
		int startingLine = 0, endingLine = 0, nameLine = 0;
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
			}  
			break;
		}
		--bufferPos[bufferStackPos];
		char[] name = new char[idlen];
		System.arraycopy(buffer, idstart, name, 0, idlen);
		if (dlog != null) dlog.println("#define " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
		
		// Now check for function style macro to store the arguments
		char[][] arglist = null;
		int pos = bufferPos[bufferStackPos];
		if (pos + 1 < limit && buffer[pos + 1] == '(') {
			++bufferPos[bufferStackPos];
			arglist = new char[4][];
			int currarg = -1;
			while (bufferPos[bufferStackPos] < limit) {
				pos = bufferPos[bufferStackPos];
				skipOverWhiteSpace();
				if (++bufferPos[bufferStackPos] >= limit)
					return;
				c = buffer[bufferPos[bufferStackPos]];
				if (c == ')') {
					break;
				} else if (c == ',') {
					continue;
				} else if (c == '.'
						&& pos + 1 < limit && buffer[pos + 1] == '.'
						&& pos + 2 < limit && buffer[pos + 2] == '.') {
					// varargs
					// TODO - something better
					bufferPos[bufferStackPos] += 2;
					arglist[++currarg] = "...".toCharArray(); //$NON-NLS-1$
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
		
		boolean encounteredMultilineComment = false;
		while (bufferPos[bufferStackPos] + 1 < limit
				&& buffer[bufferPos[bufferStackPos] + 1] != '\n') {
			skipOverNonWhiteSpace();
			textend = bufferPos[bufferStackPos];
			if( skipOverWhiteSpace() )
				encounteredMultilineComment = true;
		}

		int textlen = textend - textstart + 1;
		char[] text = emptyCharArray;
		if (textlen > 0) {
			text = new char[textlen];
			System.arraycopy(buffer, textstart, text, 0, textlen);
		}
		
		if( encounteredMultilineComment )
			text = removeMultilineCommentFromBuffer( text );
		text = removedEscapedNewline( text );
			
		// Throw it in
		definitions.put(name, 	arglist == null
				? new ObjectStyleMacro(name, text)
						: new FunctionStyleMacro(name, text, arglist) );
		
		requestor.acceptMacro( getASTFactory().createMacro( new String( name ), startingOffset, startingLine, idstart, idstart + idlen, nameLine, textstart + textlen, endingLine, null, getCurrentFilename() )); //TODO - IMacroDescriptor? 
		
	}
	
	
	/**
	 * @param text
	 * @return
	 */
	private char[] removedEscapedNewline(char[] text) {
		if( CharArrayUtils.indexOf( '\n', text ) == -1 )
			return text;
		char [] result = new char[ text.length ];
		Arrays.fill( result, ' ');
		int counter = 0;
		for( int i = 0;  i < text.length; ++i )
		{
			if( text[i] == '\\' && i+ 1 < text.length && text[i+1] == '\n' )
				++i;
			else
				result[ counter++ ] = text[i];
		}
		return CharArrayUtils.trim( result );
	}

	/**
	 * @param text
	 * @return
	 */
	private char[] removeMultilineCommentFromBuffer(char[] text) {
		char [] result = new char[ text.length ];
		Arrays.fill( result, ' ');
		int resultCount = 0;
		for( int i = 0; i < text.length; ++i )
		{
			if( text[i] == '/' && ( i+1 < text.length ) && text[i+1] == '*')
			{
				i += 2;
				while( i < text.length && text[i] != '*' && i+1 < text.length && text[i+1] != '/')
					++i;
				++i;
			}
			else
				result[resultCount++] = text[i];
				
		}
		return CharArrayUtils.trim( result );
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
			} 
			break;
			
		}
		--bufferPos[bufferStackPos];

		skipToNewLine();
		
		definitions.remove(buffer, idstart, idlen);
		if (dlog != null) dlog.println("#undef " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
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
			} 
			break;
			
		}
		--bufferPos[bufferStackPos];

		skipToNewLine();

		if ((definitions.get(buffer, idstart, idlen) != null) == positive) {
			if (dlog != null) dlog.println((positive ? "#ifdef" : "#ifndef") //$NON-NLS-1$ //$NON-NLS-2$
					+ " <TRUE> " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
			// continue on
			return;
		}
		
		if (dlog != null) dlog.println((positive ? "#ifdef" : "#ifndef") //$NON-NLS-1$ //$NON-NLS-2$
				+ " <FALSE> " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
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
			
			if (++bufferPos[bufferStackPos] >= limit)
				return;
			
			char c = buffer[bufferPos[bufferStackPos]];
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
						break;
					}
					--bufferPos[bufferStackPos];
					int len = bufferPos[bufferStackPos] - start + 1;
					int type = ppKeywords.get(buffer, start, len);
					if (type != ppKeywords.undefined) {
						switch (type) {
							case ppIfdef:
							case ppIfndef:
							case ppIf:
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
			} else if (c != '\n')
				skipToNewLine();
		}
	}
	
	private boolean skipOverWhiteSpace() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		boolean encounteredMultiLineComment = false;
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
							// leave the new line there
							--bufferPos[bufferStackPos];
							return false;
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
									encounteredMultiLineComment = true;
									break;
								}
							}
							continue;
						}
					}
					break;
				case '\\':
					if (pos + 1 < limit && buffer[pos + 1] == '\n') {
						// \n is a whitespace
						++bufferPos[bufferStackPos];
						continue;
					}
					break;
			}
			
			// fell out of switch without continuing, we're done
			--bufferPos[bufferStackPos];
			return encounteredMultiLineComment;
		}
		return encounteredMultiLineComment;
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
				case '/':
					int pos = bufferPos[bufferStackPos];
					if( pos +1 < limit && ( buffer[pos+1] == '/' ) || ( buffer[pos+1] == '*') )
					{
						--bufferPos[bufferStackPos];
						return;
					}
					break;
													
				case '\\':
					pos = bufferPos[bufferStackPos];
					if (pos + 1 < limit && buffer[pos + 1] == '\n') {
						// \n is whitespace
						--bufferPos[bufferStackPos];
						return;
					}
					break;
				case '"':
					boolean escaped = false; 
					if( bufferPos[bufferStackPos] -1  > 0 && buffer[bufferPos[bufferStackPos] -1 ] == '\\' )
						escaped = true;
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
							case '\n':
								if( !escaped )
									break loop;
							case '/': 
								if( escaped && ( bufferPos[bufferStackPos] +1 < limit ) && 
										( buffer[bufferPos[ bufferStackPos ] + 1] == '/' ||
										  buffer[bufferPos[ bufferStackPos ] + 1] == '*' ) )
								{
									--bufferPos[bufferStackPos];
									return;
								}
						
							default:
								escaped = false;
						}
					}
					break;
				case '\'':
					escaped = false;
					loop:
					while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case '\\':
								escaped = !escaped;
								continue;
							case '\'':
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
				case '<':
				case '>':
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
			} 
			break;

		}
		--bufferPos[bufferStackPos];
	}

	private void skipToNewLine() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		int pos = ++bufferPos[bufferStackPos];
		
		if (pos < limit && buffer[pos] == '\n')
			return;
		
		boolean escaped = false;
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
					} 
					return;
					
			}
			escaped = false;
		}
	}
	
	private char[] handleFunctionStyleMacro(FunctionStyleMacro macro, boolean pushContext) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];

		skipOverWhiteSpace();
		while( 	buffer[bufferPos[bufferStackPos]] == '\\' && 
				bufferPos[bufferStackPos] + 1 < buffer.length && 
				buffer[bufferPos[bufferStackPos]+1] == '\n' ) 
		{
			bufferPos[bufferStackPos] += 2;
			skipOverWhiteSpace();
		}

		if (++bufferPos[bufferStackPos] >= limit
				|| buffer[bufferPos[bufferStackPos]] != '(' )
			return emptyCharArray;
		
		char[][] arglist = macro.arglist;
		int currarg = -1;
		CharArrayObjectMap argmap = new CharArrayObjectMap(arglist.length);
		
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
				argmap.put(arglist[currarg], emptyCharArray);
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
				if (c == '(' || c == '<')
					++argparens;
				else if (c == '>')
					--argparens;
				else if (c == ')') {
					if (argparens == 0)
						break;
					--argparens;
				} else if (c == ',') {
					if (argparens == 0) {
						break;
					}
				} else if (c == '\n') {
					// eat it and continue
					continue;
				} else {
					// start of next macro arg
					--bufferPos[bufferStackPos];
					continue;
				}
				
				skipOverWhiteSpace();
				while (++bufferPos[bufferStackPos] < limit) {
					if (buffer[bufferPos[bufferStackPos]] != '\n')
						break;
					skipOverWhiteSpace();
				}
				--bufferPos[bufferStackPos];
			}
			
			char[] arg = emptyCharArray;
			int arglen = argend - argstart + 1;
			if (arglen > 0) {
				arg = new char[arglen];
				System.arraycopy(buffer, argstart, arg, 0, arglen);
			}
			argmap.put(arglist[currarg], arg);
			
			if (c == ')')
				break;
		}
		
		int size = expandFunctionStyleMacro(macro.expansion, argmap, null);
		char[] result = new char[size];
		expandFunctionStyleMacro(macro.expansion, argmap, result);
		if( pushContext )
			pushContext(result, macro);
		return result;
	}

	private int expandFunctionStyleMacro(
			char[] expansion,
			CharArrayObjectMap argmap,
			char[] result) {

		// The current position in the expansion string that we are looking at
		int pos = -1;
		// The last position in the expansion string that was copied over
		int lastcopy = -1;
		// The current write offset in the result string - also tells us the length of the result string
		int outpos = 0;
		// The first character in the current block of white space - there are times when we don't
		// want to copy over the whitespace
		int wsstart = -1;
		
		int limit = expansion.length;
		
		while (++pos < limit) {
			char c = expansion[pos];
			
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {

				wsstart = -1;
				int idstart = pos;
				while (++pos < limit) {
					c = expansion[pos];
					if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
							|| (c >= '0' && c <= '9') || c == '_')) {
						break;
					}
				}
				--pos;
				
				Object repObject = argmap.get(expansion, idstart, pos - idstart + 1);
				if (repObject != null) {
					// copy what we haven't so far
					if (++lastcopy < idstart) {
						int n = idstart - lastcopy;
						if (result != null)
							System.arraycopy(expansion, lastcopy, result, outpos, n);
						outpos += n;
					}
					
					// copy the argument replacement value
					char[] rep = (char[]) repObject;
					if (result != null)
						System.arraycopy(rep, 0, result, outpos, rep.length);
					outpos += rep.length;

					lastcopy = pos;
				}
				
			} else if (c >= '0' && c < '9') {
				
				// skip over numbers - note the expanded definition of a number
				// to include alphanumeric characters - gcc seems to operate this way
				wsstart = -1;
				while (++pos < limit) {
					c = expansion[pos];
					if (!((c >= '0' && c <= '9') || c == '.' || c == '_') 
							|| (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
						break;
				}

			} else if (c == '"') {
				
				// skip over strings
				wsstart = -1;
				boolean escaped = false;
				while (++pos < limit) {
					c = expansion[pos];
					if (c == '"') {
						if (!escaped)
							break;
					} else if (c == '\\') {
						escaped = !escaped;
					}
					escaped = false;
				}
				
			} else if (c == '\'') {

				// skip over character literals
				wsstart = -1;
				boolean escaped = false;
				while (++pos < limit) {
					c = expansion[pos];
					if (c == '\'') {
						if (!escaped)
							break;
					} else if (c == '\\') {
						escaped = !escaped;
					}
					escaped = false;
				}
				
			} else if (c == ' ' || c == '\t') {

				// obvious whitespace
				if (wsstart < 0)
					wsstart = pos;
				
			} else if (c == '/' && pos + 1 < limit) {

				// less than obvious, comments are whitespace
				c = expansion[++pos];
				if (c == '/') {
					// copy up to here or before the last whitespace
					++lastcopy;
					int n = wsstart < 0
						? pos - 1 - lastcopy
						: wsstart - lastcopy;
					if (result != null)
						System.arraycopy(expansion, lastcopy, result, outpos, n);
					outpos += n;

					// skip the rest
					lastcopy = expansion.length - 1;
				} else if (c == '*') {
					if (wsstart < 1)
						wsstart = pos - 1;
					while (++pos < limit) {
						if (expansion[pos] == '*'
								&& pos + 1 < limit
								&& expansion[pos + 1] == '/') {
							++pos;
							break;
						}
					}
				} else 
					wsstart = -1;

				
			} else if (c == '\\' && pos + 1 < limit
					&& expansion[pos + 1] == 'n') {
				// skip over this
				++pos;
				
			} else if (c == '#') {
				
				if (pos + 1 < limit && expansion[pos + 1] == '#') {
					++pos;
					// skip whitespace
					if (wsstart < 0)
						wsstart = pos - 1;
					while (++pos < limit) {
						switch (expansion[pos]) {
							case ' ':
							case '\t':
								continue;
							
							case '/':
								if (pos + 1 < limit) {
									c = expansion[pos + 1];
									if (c == '/')
										// skip over everything
										pos = expansion.length;
									else if (c == '*') {
										++pos;
										while (++pos < limit) {
											if (expansion[pos] == '*'
													&& pos + 1 < limit
													&& expansion[pos + 1] == '/') {
												++pos;
												break;
											}
										}
										continue;
									}
								}
						}
						break;
					}

					// copy everything up to the whitespace
					int n = wsstart - (++lastcopy);
					if (n > 0 && result != null)
						System.arraycopy(expansion, lastcopy, result, outpos, n);
					outpos += n;
					
					// skip over the ## and the whitespace around it
					lastcopy = --pos;
					wsstart = -1;

				} else {
					// stringify
					
					// copy what we haven't so far
					if (++lastcopy < pos) {
						int n = pos - lastcopy;
						if (result != null)
							System.arraycopy(expansion, lastcopy, result, outpos, n);
						outpos += n;
					}

					++pos;
					
					// skip whitespace
					while (++pos < limit) {
						switch (expansion[pos]) {
							case ' ':
							case '\t':
								continue;
							//TODO handle comments
						}
						break;
					}
					--pos;
					
					// grab the identifier
					c = expansion[pos];
					int idstart = pos;
					if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'X') || c == '_') {
						while (++pos < limit) {
							if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'X')
									|| (c >= '0' && c <= '9') || c == '_')
								break;
						}
					} // else TODO something
					--pos;
					int idlen = pos - idstart + 1;
					char[] argvalue = (char[])argmap.get(expansion, idstart, idlen);
					if (argvalue != null) {
						if (result != null) {
							result[outpos] = '"';
							System.arraycopy(argvalue, 0, result, outpos + 1, argvalue.length);
							result[outpos + argvalue.length + 1] = '"';
						}
						outpos += argvalue.length + 2;
					}
					lastcopy = pos;
				}
			} else {
				
				// not sure what it is but it sure ain't whitespace
				wsstart = -1;
			}
			
		}

		if (wsstart < 0 && ++lastcopy < expansion.length) {
			int n = expansion.length - lastcopy;
			if (result != null)
				System.arraycopy(expansion, lastcopy, result, outpos, n);
			outpos += n;
		}
		
		return outpos;
	}

	// standard built-ins
	private static final ObjectStyleMacro __cplusplus
		= new ObjectStyleMacro("__cplusplus".toCharArray(), "1".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __STDC__
		= new ObjectStyleMacro("__STDC__".toCharArray(), "1".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	
	// gcc built-ins
	private static final ObjectStyleMacro __inline__
		= new ObjectStyleMacro("__inline__".toCharArray(), "inline".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __extension__
		= new ObjectStyleMacro("__extension__".toCharArray(), emptyCharArray); //$NON-NLS-1$
	private static final ObjectStyleMacro __asm__
		= new ObjectStyleMacro("__asm__".toCharArray(), "asm".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __restrict__
		= new ObjectStyleMacro("__restrict__".toCharArray(), "restrict".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __restrict
		= new ObjectStyleMacro("__restrict".toCharArray(), "restrict".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __volatile__
		= new ObjectStyleMacro("__volatile__".toCharArray(), "volatile".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final FunctionStyleMacro __attribute__
		= new FunctionStyleMacro(
				"__attribute__".toCharArray(), //$NON-NLS-1$
				emptyCharArray,
				new char[][] { "arg".toCharArray() }); //$NON-NLS-1$
	private static final FunctionStyleMacro _Pragma = new FunctionStyleMacro( 
			"_Pragma".toCharArray(),  //$NON-NLS-1$
			emptyCharArray, 
			new char[][] { "arg".toCharArray() } ); //$NON-NLS-1$

	private IASTFactory astFactory;

	private int offsetBoundary = -1;
	
	protected void setupBuiltInMacros() {

		definitions.put(__STDC__.name, __STDC__);
		if( language == ParserLanguage.CPP )
			definitions.put(__cplusplus.name, __cplusplus);

		// gcc extensions
		definitions.put(__inline__.name, __inline__);
		definitions.put(__extension__.name, __extension__);
		definitions.put(__attribute__.name, __attribute__);
		definitions.put(__restrict__.name, __restrict__);
		definitions.put(__restrict.name, __restrict);
		definitions.put(__volatile__.name, __volatile__);
		if( language == ParserLanguage.CPP )
			definitions.put(__asm__.name, __asm__);
		else
			definitions.put(_Pragma.name, _Pragma );
		
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
	public final void setASTFactory(IASTFactory f) {
		astFactory = f;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setOffsetBoundary(int)
	 */
	public final void setOffsetBoundary(int offset) {
		offsetBoundary = offset;
		bufferLimit[0] = offset;
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
	public final IASTFactory getASTFactory() {
		if( astFactory == null )
			astFactory = ParserFactory.createASTFactory( parserMode, language );
		return astFactory;
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
		return requestor;
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
		return language;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getLogService()
	 */
	public IParserLogService getLogService() {
		return log;
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
		return parserMode;
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
		if( workingCopies == null ) return EmptyIterator.EMPTY_ITERATOR;
		return workingCopies.iterator();
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
	
	private final char[] getCurrentFilename() {
		for( int i = bufferStackPos; i >= 0; --i )
		{
			if( bufferData[i] instanceof InclusionData )
				return ((InclusionData)bufferData[i]).reader.filename;
			if( bufferData[i] instanceof CodeReader )
				return ((CodeReader)bufferData[i]).filename;
		}
		return emptyCharArray;

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
		keywords.put("auto".toCharArray(), IToken.t_auto); //$NON-NLS-1$
		keywords.put("break".toCharArray(), IToken.t_break); //$NON-NLS-1$
		keywords.put("case".toCharArray(), IToken.t_case); //$NON-NLS-1$
		keywords.put("char".toCharArray(), IToken.t_char); //$NON-NLS-1$
		keywords.put("const".toCharArray(), IToken.t_const); //$NON-NLS-1$
		keywords.put("continue".toCharArray(), IToken.t_continue); //$NON-NLS-1$
		keywords.put("default".toCharArray(), IToken.t_default); //$NON-NLS-1$
		keywords.put("do".toCharArray(), IToken.t_do); //$NON-NLS-1$
		keywords.put("double".toCharArray(), IToken.t_double); //$NON-NLS-1$
		keywords.put("else".toCharArray(), IToken.t_else); //$NON-NLS-1$
		keywords.put("enum".toCharArray(), IToken.t_enum); //$NON-NLS-1$
		keywords.put("extern".toCharArray(), IToken.t_extern); //$NON-NLS-1$
		keywords.put("float".toCharArray(), IToken.t_float); //$NON-NLS-1$
		keywords.put("for".toCharArray(), IToken.t_for); //$NON-NLS-1$
		keywords.put("goto".toCharArray(), IToken.t_goto); //$NON-NLS-1$
		keywords.put("if".toCharArray(), IToken.t_if); //$NON-NLS-1$
		keywords.put("inline".toCharArray(), IToken.t_inline); //$NON-NLS-1$
		keywords.put("int".toCharArray(), IToken.t_int); //$NON-NLS-1$
		keywords.put("long".toCharArray(), IToken.t_long); //$NON-NLS-1$
		keywords.put("register".toCharArray(), IToken.t_register); //$NON-NLS-1$
		keywords.put("return".toCharArray(), IToken.t_return); //$NON-NLS-1$
		keywords.put("short".toCharArray(), IToken.t_short); //$NON-NLS-1$
		keywords.put("signed".toCharArray(), IToken.t_signed); //$NON-NLS-1$
		keywords.put("sizeof".toCharArray(), IToken.t_sizeof); //$NON-NLS-1$
		keywords.put("static".toCharArray(), IToken.t_static); //$NON-NLS-1$
		keywords.put("struct".toCharArray(), IToken.t_struct); //$NON-NLS-1$
		keywords.put("switch".toCharArray(), IToken.t_switch); //$NON-NLS-1$
		keywords.put("typedef".toCharArray(), IToken.t_typedef); //$NON-NLS-1$
		keywords.put("union".toCharArray(), IToken.t_union); //$NON-NLS-1$
		keywords.put("unsigned".toCharArray(), IToken.t_unsigned); //$NON-NLS-1$
		keywords.put("void".toCharArray(), IToken.t_void); //$NON-NLS-1$
		keywords.put("volatile".toCharArray(), IToken.t_volatile); //$NON-NLS-1$
		keywords.put("while".toCharArray(), IToken.t_while); //$NON-NLS-1$

		// ANSI C keywords
		keywords.put("restrict".toCharArray(), IToken.t_restrict); //$NON-NLS-1$
		keywords.put("_Bool".toCharArray(), IToken.t__Bool); //$NON-NLS-1$
		keywords.put("_Complex".toCharArray(), IToken.t__Complex); //$NON-NLS-1$
		keywords.put("_Imaginary".toCharArray(), IToken.t__Imaginary); //$NON-NLS-1$

		// C++ Keywords
		keywords.put("asm".toCharArray(), IToken.t_asm); //$NON-NLS-1$
		keywords.put("bool".toCharArray(), IToken.t_bool); //$NON-NLS-1$
		keywords.put("catch".toCharArray(), IToken.t_catch); //$NON-NLS-1$
		keywords.put("class".toCharArray(), IToken.t_class); //$NON-NLS-1$
		keywords.put("const_cast".toCharArray(), IToken.t_const_cast); //$NON-NLS-1$
		keywords.put("delete".toCharArray(), IToken.t_delete); //$NON-NLS-1$
		keywords.put("dynamic_cast".toCharArray(), IToken.t_dynamic_cast); //$NON-NLS-1$
		keywords.put("explicit".toCharArray(), IToken.t_explicit); //$NON-NLS-1$
		keywords.put("export".toCharArray(), IToken.t_export); //$NON-NLS-1$
		keywords.put("false".toCharArray(), IToken.t_false); //$NON-NLS-1$
		keywords.put("friend".toCharArray(), IToken.t_friend); //$NON-NLS-1$
		keywords.put("mutable".toCharArray(), IToken.t_mutable); //$NON-NLS-1$
		keywords.put("namespace".toCharArray(), IToken.t_namespace); //$NON-NLS-1$
		keywords.put("new".toCharArray(), IToken.t_new); //$NON-NLS-1$
		keywords.put("operator".toCharArray(), IToken.t_operator); //$NON-NLS-1$
		keywords.put("private".toCharArray(), IToken.t_private); //$NON-NLS-1$
		keywords.put("protected".toCharArray(), IToken.t_protected); //$NON-NLS-1$
		keywords.put("public".toCharArray(), IToken.t_public); //$NON-NLS-1$
		keywords.put("reinterpret_cast".toCharArray(), IToken.t_reinterpret_cast); //$NON-NLS-1$
		keywords.put("static_cast".toCharArray(), IToken.t_static_cast); //$NON-NLS-1$
		keywords.put("template".toCharArray(), IToken.t_template); //$NON-NLS-1$
		keywords.put("this".toCharArray(), IToken.t_this); //$NON-NLS-1$
		keywords.put("throw".toCharArray(), IToken.t_throw); //$NON-NLS-1$
		keywords.put("true".toCharArray(), IToken.t_true); //$NON-NLS-1$
		keywords.put("try".toCharArray(), IToken.t_try); //$NON-NLS-1$
		keywords.put("typeid".toCharArray(), IToken.t_typeid); //$NON-NLS-1$
		keywords.put("typename".toCharArray(), IToken.t_typename); //$NON-NLS-1$
		keywords.put("using".toCharArray(), IToken.t_using); //$NON-NLS-1$
		keywords.put("virtual".toCharArray(), IToken.t_virtual); //$NON-NLS-1$
		keywords.put("wchar_t".toCharArray(), IToken.t_wchar_t); //$NON-NLS-1$

		// C++ operator alternative
		keywords.put("and".toCharArray(), IToken.t_and); //$NON-NLS-1$
		keywords.put("and_eq".toCharArray(), IToken.t_and_eq); //$NON-NLS-1$
		keywords.put("bitand".toCharArray(), IToken.t_bitand); //$NON-NLS-1$
		keywords.put("bitor".toCharArray(), IToken.t_bitor); //$NON-NLS-1$
		keywords.put("compl".toCharArray(), IToken.t_compl); //$NON-NLS-1$
		keywords.put("not".toCharArray(), IToken.t_not); //$NON-NLS-1$
		keywords.put("not_eq".toCharArray(), IToken.t_not_eq); //$NON-NLS-1$
		keywords.put("or".toCharArray(), IToken.t_or); //$NON-NLS-1$
		keywords.put("or_eq".toCharArray(), IToken.t_or_eq); //$NON-NLS-1$
		keywords.put("xor".toCharArray(), IToken.t_xor); //$NON-NLS-1$
		keywords.put("xor_eq".toCharArray(), IToken.t_xor_eq); //$NON-NLS-1$
		
		// Preprocessor keywords
		ppKeywords = new CharArrayIntMap(16, -1);
		ppKeywords.put("if".toCharArray(), ppIf); //$NON-NLS-1$
		ppKeywords.put("ifdef".toCharArray(), ppIfdef); //$NON-NLS-1$
		ppKeywords.put("ifndef".toCharArray(), ppIfndef); //$NON-NLS-1$
		ppKeywords.put("elif".toCharArray(), ppElif); //$NON-NLS-1$
		ppKeywords.put("else".toCharArray(), ppElse); //$NON-NLS-1$
		ppKeywords.put("endif".toCharArray(), ppEndif); //$NON-NLS-1$
		ppKeywords.put("include".toCharArray(), ppInclude); //$NON-NLS-1$
		ppKeywords.put("define".toCharArray(), ppDefine); //$NON-NLS-1$
		ppKeywords.put("undef".toCharArray(), ppUndef); //$NON-NLS-1$
		ppKeywords.put("error".toCharArray(), ppError); //$NON-NLS-1$
		ppKeywords.put("include_next".toCharArray(), ppInclude_next); //$NON-NLS-1$
	}
}
