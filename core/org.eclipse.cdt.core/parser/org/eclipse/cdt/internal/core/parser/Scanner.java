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
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.cdt.core.parser.Backtrack;
import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.ILineOffsetReconciler;
import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryException;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;

/**
 * @author jcamelon
 *
 */

public class Scanner implements IScanner {
   
	protected final IParserLogService log;
	private final static String SCRATCH = "<scratch>";
	private Reader backupReader;
	private IProblemFactory problemFactory = new ScannerProblemFactory();

	protected void handleProblem( int problemID, String argument, int beginningOffset, boolean warning, boolean error ) throws ScannerException
	{
		handleProblem( problemID, argument, beginningOffset, warning, error, true );
	}

	protected void handleProblem( int problemID, String argument, int beginningOffset, boolean warning, boolean error, boolean extra ) throws ScannerException
	{
		Map arguments = new HashMap(); 
		if( argument != null )
		{
			String attributes [] = problemFactory.getRequiredAttributesForId( problemID );
			arguments.put( attributes[ 0 ], argument );
		}
		IProblem p = problemFactory.createProblem( problemID, beginningOffset, getCurrentOffset(), contextStack.getCurrentLineNumber(), getCurrentFile().toCharArray(), arguments, warning, error );
		if( (! requestor.acceptProblem( p )) && extra )
			throw new ScannerException( p );
	}

    public Scanner(Reader reader, String filename, IScannerInfo info, ISourceElementRequestor requestor, ParserMode parserMode, ParserLanguage language, IParserLogService log ) {
    	this.log = log;
		this.requestor = requestor;
		this.mode = parserMode;
		this.language = language;
		astFactory = ParserFactory.createASTFactory( mode, language );
		this.backupReader = reader;
		
		try {
			//this is a hack to get around a sudden EOF experience
			contextStack.push(
						new ScannerContext().initialize(
						new StringReader("\n"),
						START,
						ScannerContext.SENTINEL, null), requestor);

			if (filename == null)
				contextStack.push( new ScannerContext().initialize(reader, TEXT, ScannerContext.TOP, null ), requestor ); 
			else
				contextStack.push( new ScannerContext().initialize(reader, filename, ScannerContext.TOP, null ), requestor );
		} catch( ContextException ce ) {
			//won't happen since we aren't adding an include or a macro
		} 
		
		originalConfig = info;
		if( info.getDefinedSymbols() != null )
			definitions.putAll( info.getDefinedSymbols() );
			
		if( info.getIncludePaths() != null )
			overwriteIncludePath( info.getIncludePaths() );
            
    }

	public void addIncludePath(String includePath) {
		includePathNames.add(includePath);
		includePaths.add( new File( includePath ) );
	}

	public void overwriteIncludePath(String [] newIncludePaths) {
		if( newIncludePaths == null ) return;
		includePathNames = null;
		includePaths = null; 
		includePathNames = new ArrayList();
		includePaths = new ArrayList();
		
		for( int i = 0; i < newIncludePaths.length; ++i ) 
			includePathNames.add( newIncludePaths[i] );
		
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

	public final String[] getIncludePaths() {
		return (String[])includePathNames.toArray();
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
		boolean inString = false;
		boolean inChar = false;
		while (true) {
			while ((c != '\n')
				&& (c != '\r')
				&& (c != '\\')
				&& (c != '/')
				&& (c != '"' || ( c == '"' && inChar ) )
				&& (c != '\'' || ( c == '\'' && inString ) )
				&& (c != NOCHAR)) {
				buffer.append((char) c);
				c = getChar( true );
			}
			
			if (c == '/') {
				//only care about comments outside of a quote
				if( inString || inChar ){
					buffer.append( (char) c );
					c = getChar( true );
					continue;
				}
				
				// we need to peek ahead at the next character to see if 
				// this is a comment or not
				int next = getChar();
				if (next == '/') {
					// single line comment
					skipOverSinglelineComment();
					break;
				} else if (next == '*') {
					// multiline comment
					if (skipOverMultilineComment())
						break;
					else
						c = getChar( true );
					continue;
				} else {
					// we are not in a comment
					buffer.append((char) c);
					c = next;
					continue;
				}
			} else if( c == '"' ){
				inString = !inString;
				buffer.append((char) c);
				c = getChar( true );
				continue;
			} else if( c == '\'' ){
				inChar = !inChar;
				buffer.append((char) c);
				c = getChar( true );
				continue;
			} else if( c == '\\' ){
				c = getChar(true);
				if( c == '\r' ){
					c = getChar(true);
					if( c == '\n' ){
						c = getChar(true);		
					}
				} else if( c == '\n' ){ 
					c = getChar(true);
				} else {
					buffer.append('\\');
					if( c == '"' || c == '\'' ){
						buffer.append((char)c);
						c = getChar( true );
					}
				}
				continue;
			} else {
				ungetChar(c);
				break;
			}
		}

		return buffer.toString();
	}

	protected void skipOverTextUntilNewline() throws ScannerException {
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

	private void setCurrentToken(IToken t) {
		if (currentToken != null)
			currentToken.setNext(t);
		currentToken = t;
	}

	protected void resetStorageBuffer()
	{
		if( storageBuffer != null ) 
			storageBuffer = null; 
	}

	protected IToken newToken(int t, String i, IScannerContext c) {
		setCurrentToken(new Token(t, i, c));
		return currentToken;
	}

	protected IToken newToken(int t, String i) {
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

	protected void handleInclusion(String fileName, boolean useIncludePaths, int nameOffset, int beginOffset, int endOffset ) throws ScannerException {

		FileReader inclusionReader = null;
		String newPath = null; 
		if( useIncludePaths ) // search include paths for this file
		{
			// iterate through the include paths 
			Iterator iter = includePaths.iterator();
	
			while (iter.hasNext()) {
	
				File pathFile = (File)iter.next();
				String path = pathFile.getPath();
				if( !pathFile.exists() && path.indexOf('\"') != -1 )
				{
					StringTokenizer tokenizer = new StringTokenizer(path, "\"" );	//$NON-NLS-1$
					StringBuffer buffer = new StringBuffer(path.length() );
					while( tokenizer.hasMoreTokens() ){
						buffer.append( tokenizer.nextToken() );
					}
					pathFile = new File( buffer.toString() );
				}
				if (pathFile.isDirectory()) {
					newPath = pathFile.getPath() + File.separatorChar + fileName;
					File includeFile = new File(newPath);
					if (includeFile.exists() && includeFile.isFile()) {
						try {
							inclusionReader = new FileReader(includeFile);
							break;
						} catch (FileNotFoundException fnf) {
							// do nothing - check the next directory
						}
					}
				}
			}
			
			if (inclusionReader == null )
			{
				handleProblem( IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, fileName, beginOffset, false, true, true );
			}
		}
		else // local inclusion
		{
			String currentFilename = contextStack.getCurrentContext().getFilename(); 
			File currentIncludeFile = new File( currentFilename );
			String parentDirectory = currentIncludeFile.getParentFile().getAbsolutePath();
			currentIncludeFile = null; 
			newPath = parentDirectory + File.separatorChar + fileName;
			File includeFile = new File( newPath );
			if (includeFile.exists() && includeFile.isFile()) {
				try {
					inclusionReader =
						new FileReader(includeFile);
				} catch (FileNotFoundException fnf) {
					// the spec says that if finding in the local directory fails, search the include paths
					handleInclusion( fileName, true, nameOffset, beginOffset, endOffset );
				}
			}
			else
			{
				// the spec says that if finding in the local directory fails, search the include paths
				handleInclusion( fileName, true, nameOffset, beginOffset, endOffset );
			}
		}
		if (inclusionReader != null) {
			IASTInclusion inclusion = null;
            try
            {
                inclusion =
                    astFactory.createInclusion(
                        fileName,
                        newPath,
                        !useIncludePaths,
                        beginOffset,
                        nameOffset,
                        nameOffset + fileName.length(),
                        endOffset);
            }
            catch (Exception e)
            {
                /* do nothing */
            } 
			
			try
			{
				contextStack.updateContext(inclusionReader, newPath, ScannerContext.INCLUSION, inclusion, requestor );
			}
			catch (ContextException e1)
			{
				handleProblem( e1.getId(), fileName, beginOffset, false, true, true );
			}
		}
	}

	// constants
	private static final int NOCHAR = -1;

	private static final String TEXT = "<text>";
	private static final String START = "<initial reader>";
	private static final String EXPRESSION = "<expression>";
	private static final String PASTING = "<pasting>";

	private static final String DEFINED = "defined";
	private static final String POUND_DEFINE = "#define ";

	private ContextStack contextStack = new ContextStack();
	private IScannerContext lastContext = null;
	
	private IScannerInfo originalConfig; 
	private List includePathNames = new ArrayList();
	private List includePaths = new ArrayList();
	private Hashtable definitions = new Hashtable();
	private StringBuffer storageBuffer = null; 
	
	private int count = 0;
	private static HashMap cppKeywords = new HashMap();
	private static HashMap cKeywords = new HashMap(); 
	private static HashMap ppDirectives = new HashMap();

	private IToken currentToken = null;
	private IToken cachedToken = null;

	private boolean passOnToClient = true; 
	private BranchTracker branches = new BranchTracker();

	// these are scanner configuration aspects that we perhaps want to tweak
	// eventually, these should be configurable by the client, but for now
	// we can just leave it internal
	private boolean enableDigraphReplacement = true;
	private boolean enableTrigraphReplacement = true;
	private boolean enableTrigraphReplacementInStrings = true;
	private boolean throwExceptionOnBadCharacterRead = false; 
	private boolean atEOF = false;

	private boolean tokenizingMacroReplacementList = false;
	public void setTokenizingMacroReplacementList( boolean mr ){
		tokenizingMacroReplacementList = mr;
	}
	
	private final ParserMode mode;
	

	private int getChar() throws ScannerException
	{
		return getChar( false );
	}

	private int getChar( boolean insideString ) throws ScannerException {
		int c = NOCHAR;
		
		lastContext = contextStack.getCurrentContext();
		
		if (contextStack.getCurrentContext() == null)
			// past the end of file
			return c;

        c = accountForUndo(c);
		
		int baseOffset = lastContext.getOffset() - lastContext.undoStackSize() - 1;
		
		if (enableTrigraphReplacement && (!insideString || enableTrigraphReplacementInStrings)) {
			// Trigraph processing
			enableTrigraphReplacement = false;
			if (c == '?') {
				c = getChar(insideString);
				if (c == '?') {
					c = getChar(insideString);
					switch (c) {
						case '(':
							expandDefinition("??(", "[", baseOffset);
							c = getChar(insideString);
							break;
						case ')':
							expandDefinition("??)", "]", baseOffset);
							c = getChar(insideString);
							break;
						case '<':
							expandDefinition("??<", "{", baseOffset);
							c = getChar(insideString);
							break;
						case '>':
							expandDefinition("??>", "}", baseOffset);
							c = getChar(insideString);
							break;
						case '=':
							expandDefinition("??=", "#", baseOffset);
							c = getChar(insideString);
							break;
						case '/':
							expandDefinition("??/", "\\", baseOffset);
							c = getChar(insideString);
							break;
						case '\'':
							expandDefinition("??\'", "^", baseOffset);
							c = getChar(insideString);
							break;
						case '!':
							expandDefinition("??!", "|", baseOffset);
							c = getChar(insideString);
							break;
						case '-':
							expandDefinition("??-", "~", baseOffset);
							c = getChar(insideString);
							break;
						default:
							// Not a trigraph
							ungetChar(c);
							ungetChar('?');
							c = '?';	
					}
				} else {
					// Not a trigraph
					ungetChar(c);
					c = '?';
				}
			}
			enableTrigraphReplacement = true;
		} 
		
		if (!insideString)
		{
			if (c == '\\') {
				c = getChar(false);
				if (c == '\r') {
					c = getChar(false);
					if (c == '\n')
					{
						c = getChar(false);
					}
				} else if (c == '\n')
				{
					c = getChar(false);
 
				} else // '\' is not the last character on the line
				{
					ungetChar(c);
					c = '\\';
				}
			} else if (enableDigraphReplacement) {
				enableDigraphReplacement = false;
				// Digraph processing
				if (c == '<') {
					c = getChar(false);
					if (c == '%') {
						expandDefinition("<%", "{", baseOffset);
						c = getChar(false);
					} else if (c == ':') {
						expandDefinition("<:", "[", baseOffset);
						c = getChar(false);
					} else {
						// Not a digraph
						ungetChar(c);
						c = '<';
					}
				} else if (c == ':') {
					c = getChar(false);
					if (c == '>') {
						expandDefinition(":>", "]", baseOffset);
						c = getChar(false);
					} else {
						// Not a digraph
						ungetChar(c);
						c = ':';
					}
				} else if (c == '%') {
					c = getChar(false);
					if (c == '>') {
						expandDefinition("%>", "}", baseOffset);
						c = getChar(false);
					} else if (c == ':') {
						expandDefinition("%:", "#", baseOffset);
						c = getChar(false);
					} else {
						// Not a digraph
						ungetChar(c);
						c = '%';
					}
				}
				enableDigraphReplacement = true;
			}
		}
			
		return c;
	}



    protected int accountForUndo(int c)
    {
        boolean done;
        do {
        	done = true;
        
        	if (contextStack.getCurrentContext().undoStackSize() != 0 ) {
        		c = contextStack.getCurrentContext().popUndo();
        	} else {
        		try {
        			c = contextStack.getCurrentContext().read();
        			if (c == NOCHAR) {
        				if (contextStack.rollbackContext(requestor) == false) {
        					c = NOCHAR;
        					break;
        				} else {
        					done = false;
        				}
        			}
        		} catch (IOException e) {
        			if (contextStack.rollbackContext(requestor) == false) {
        				c = NOCHAR;
        			} else {
        				done = false;
        			}
        		}
        	}
        } while (!done);
        return c;
    }

	private void ungetChar(int c) throws ScannerException{
		contextStack.getCurrentContext().pushUndo(c);
		try
		{
			contextStack.undoRollback( lastContext, requestor );
		}
		catch (ContextException e)
		{
			handleProblem( e.getId(), contextStack.getCurrentContext().getFilename(), getCurrentOffset(), false, true, true );
		}
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

	protected void consumeUntilOutOfMacroExpansion() throws ScannerException
	{
		while( contextStack.getCurrentContext().getKind() == IScannerContext.MACROEXPANSION )
			getChar();
	}

	public IToken nextToken() throws ScannerException, EndOfFile {
		return nextToken( true, false ); 
	}

	public IToken nextToken(boolean pasting) throws ScannerException, EndOfFile {
		return nextToken( pasting, false ); 
	}

	public IToken nextToken( boolean pasting, boolean lookingForNextAlready ) throws ScannerException, EndOfFile
	{
		if( cachedToken != null ){
			setCurrentToken( cachedToken );
			cachedToken = null;
			return currentToken;	
		}
		
		count++;
		boolean possibleWideLiteral = true;
		boolean wideLiteral = false; 
		int c = getChar();

		while (c != NOCHAR) {
			if ( ! passOnToClient ) {
			
				
				while (c != NOCHAR && c != '#' ) 
				{
					c = getChar();
					if( c == '/' )
					{
						c = getChar();
						if( c == '/' )
						{
							skipOverSinglelineComment();
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
			} else if (c == 'L' && possibleWideLiteral ) { 
				int oldChar = c;
				c = getChar(); 
				if(!(c == '"' || c == '\'')) {
					// we have made a mistake
					ungetChar(c);
					c = oldChar;
					possibleWideLiteral = false; 
					continue;
				}
				wideLiteral = true;
				continue;
			} else if (c == '"') {
				int beginOffset = getCurrentOffset();
				// string
				StringBuffer buff = new StringBuffer(); 
				int beforePrevious = NOCHAR;
				int previous = c;
				c = getChar(true);

				for( ; ; )
				{
					if ( ( c =='"' ) && ( previous != '\\' || beforePrevious == '\\') ) break;
					if ( ( c == '\n' ) && ( previous != '\\' || beforePrevious == '\\') )
					{
						handleProblem( IProblem.SCANNER_UNBOUNDED_STRING, null, beginOffset, false, true, true );
					}
						
					if( c == NOCHAR) break;  
					buff.append((char) c);
					beforePrevious = previous;
					previous = c;
					c = getChar(true);
				}

				if (c != NOCHAR ) 
				{
					int type = wideLiteral ? IToken.tLSTRING : IToken.tSTRING;
										
					//If the next token is going to be a string as well, we need to concatenate
					//it with this token.
					IToken returnToken = newToken( type, buff.toString(), contextStack.getCurrentContext());
					
					if (!lookingForNextAlready) {
						IToken next = null;
						try{
							next = nextToken( true, true );
						} catch( EndOfFile e ){ 
							next = null;
						}
						
						while( next != null && next.getType()  == returnToken.getType() ){
							returnToken.setImage( returnToken.getImage() + next.getImage() ); 
							returnToken.setNext( null );
							currentToken = returnToken; 
							try{
								next = nextToken( true, true );
							} catch( EndOfFile e ){ 
								next = null;
							}
						}
						
						cachedToken = next;
					
					}
					
					currentToken = returnToken;
					returnToken.setNext( null );
									
					return returnToken; 
	
				} else 
					handleProblem( IProblem.SCANNER_UNBOUNDED_STRING, null, beginOffset, false, true, true );
				
		
			} else if (
				((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z')) | (c == '_')) {
                        
                int baseOffset = lastContext.getOffset() - lastContext.undoStackSize() - 1;
						
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
					return newToken(IToken.tINTEGER, handleDefinedMacro());
				
				Object mapping = definitions.get(ident);

				if (mapping != null) {
					if( contextStack.shouldExpandDefinition( POUND_DEFINE + ident ) ) {					
						expandDefinition(ident, mapping, baseOffset);
						c = getChar();
						continue;
					}
				}

				Object tokenTypeObject;
				
				if( language == ParserLanguage.CPP )
				 	tokenTypeObject = cppKeywords.get(ident);
				else
					tokenTypeObject = cKeywords.get(ident);
					
				int tokenType = IToken.tIDENTIFIER;
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
							try
							{
								contextStack.updateContext( new StringReader( storageBuffer.toString()), PASTING, IScannerContext.MACROEXPANSION, null, requestor );
							}
							catch (ContextException e)
							{
								handleProblem( e.getId(), contextStack.getCurrentContext().getFilename(), getCurrentOffset(), false, true, true );
							}
							storageBuffer = null;  
							c = getChar(); 
							continue;
						}
					}
				}

				return newToken(tokenType, ident, contextStack.getCurrentContext());
			} else if ((c >= '0') && (c <= '9') || c == '.' ) {
				int beginOffset = getCurrentOffset();
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
				
				if( ! firstCharZero && floatingPoint && !(c >= '0' && c <= '9') ){
					//if pasting, there could actually be a float here instead of just a .
					if( buff.toString().equals( "." ) ){
						if( c == '*' ){
							return newToken( IToken.tDOTSTAR, ".*", contextStack.getCurrentContext() );
						} else if( c == '.' ){
							if( getChar() == '.' )
								return newToken( IToken.tELIPSE, "..." );
							else
								handleProblem( IProblem.SCANNER_BAD_FLOATING_POINT, null, beginOffset, false, true, true );				
						} else {
							ungetChar( c );
							return newToken( IToken.tDOT, ".", contextStack.getCurrentContext() );
						}
					}
				} else if (c == 'x') {
					if( ! firstCharZero ) 
					{
						handleProblem( IProblem.SCANNER_BAD_HEX_FORMAT, null, beginOffset, false, true, true );
						c = getChar(); 
						continue;
					}
					buff.append( (char)c );
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
					
					floatingPoint = true;
					c= getChar(); 
					while ((c >= '0' && c <= '9')
					|| (hex
						&& ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))))
					{
						buff.append((char) c);
						c = getChar();
					}
				}
				

				if (c == 'e' || c == 'E' || (hex && (c == 'p' || c == 'P')))
				{
					if( ! floatingPoint ) floatingPoint = true; 
					// exponent type for floating point 
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
				} else {
					if( floatingPoint ){
						//floating-suffix
						if( c == 'l' || c == 'L' || c == 'f' || c == 'F' ){
							c = getChar();
						}
					} else {
						//integer suffix
						if( c == 'u' || c == 'U' ){
							c = getChar();
							if( c == 'l' || c == 'L')
								c = getChar();
							if( c == 'l' || c == 'L')
								c = getChar();
						} else if( c == 'l' || c == 'L' ){
							c = getChar();
							if( c == 'l' || c == 'L')
								c = getChar();
							if( c == 'u' || c == 'U' )
								c = getChar();
						}
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
							try
							{
								contextStack.updateContext( new StringReader( buff.toString()), PASTING, IScannerContext.MACROEXPANSION, null, requestor );
							}
							catch (ContextException e)
							{
								handleProblem( e.getId(), contextStack.getCurrentContext().getFilename(), getCurrentOffset(), false, true, true );
							}
							storageBuffer = null;  
							c = getChar(); 
							continue;
						}
					}
				}
				
				int tokenType;
				String result = buff.toString(); 
				
				if( floatingPoint && result.equals(".") )
					tokenType = IToken.tDOT;
				else
					tokenType = floatingPoint ? IToken.tFLOATINGPT : IToken.tINTEGER; 
				
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
						handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#  #", beginningOffset, false, true, true ); 
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
					if (true)
						handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#"+token, beginningOffset, false, true, true );							
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
							
							definitions.remove(toBeUndefined);
							
							skipOverTextUntilNewline();
							c = getChar();
							continue;
						case PreprocessorDirectives.IF :
							// get the rest of the line		
							int currentOffset = getCurrentOffset();
							String expression = getRestOfPreprocessorLine();
							
							boolean expressionEvalResult = false;
							try{
								expressionEvalResult = evaluateExpression(expression, currentOffset);
							} catch( ScannerException e ){}
							
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
							if( ! restOfLine.equals( "" ) && true )
								handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#endif " + restOfLine, beginningOffset, false, true, true );
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
							try
							{
								passOnToClient = branches.poundelse();
							}
							catch( EmptyStackException ese )
							{
								handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, 
									token, 
									beginningOffset, 
									false, true, true );  
							}

							skipOverTextUntilNewline();
							c = getChar();
							continue;

						case PreprocessorDirectives.ELIF :
							int co = getCurrentOffset();
							String elsifExpression = getRestOfPreprocessorLine().trim();

							if (elsifExpression.equals(""))
								handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#elif", beginningOffset, false, true, true );

							boolean elsifResult = false;
							elsifResult = evaluateExpression(elsifExpression, co );

							try
							{
								passOnToClient = branches.poundelif( elsifResult );
							}
							catch( EmptyStackException ese )
							{
								handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, 
									token + ' ' + elsifExpression, 
									beginningOffset, 
									false, true, true );  
							}
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
							handleProblem( IProblem.PREPROCESSOR_POUND_ERROR, getRestOfPreprocessorLine(), beginningOffset, false, true, true );
							c = getChar();
							continue;
						case PreprocessorDirectives.PRAGMA :
							skipOverTextUntilNewline();
							c = getChar();
							continue;
						case PreprocessorDirectives.BLANK :
							String remainderOfLine =
								getRestOfPreprocessorLine().trim();
							if (!remainderOfLine.equals("")) {
								handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "# " + remainderOfLine, beginningOffset, false, true, true );
							}
							c = getChar();
							continue;
						default :
							handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#" + token, beginningOffset, false, true, true );
					}
				}
			} else {
				switch (c) {
					case '\'' :
						return processCharacterLiteral( c, wideLiteral );
					case ':' :
						c = getChar();
						switch (c) {
							case ':' :
								return newToken(
									IToken.tCOLONCOLON,
									"::",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tCOLON,
									":",
									contextStack.getCurrentContext());
						}
					case ';' :
						return newToken(IToken.tSEMI, ";", contextStack.getCurrentContext());
					case ',' :
						return newToken(IToken.tCOMMA, ",", contextStack.getCurrentContext());
					case '?' :
						return newToken(IToken.tQUESTION, "?", contextStack.getCurrentContext());
					case '(' :
						return newToken(IToken.tLPAREN, "(", contextStack.getCurrentContext());
					case ')' :
						return newToken(IToken.tRPAREN, ")", contextStack.getCurrentContext());
					case '[' :
						return newToken(IToken.tLBRACKET, "[", contextStack.getCurrentContext());
					case ']' :
						return newToken(IToken.tRBRACKET, "]", contextStack.getCurrentContext());
					case '{' :
						return newToken(IToken.tLBRACE, "{", contextStack.getCurrentContext());
					case '}' :
						return newToken(IToken.tRBRACE, "}", contextStack.getCurrentContext());
					case '+' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tPLUSASSIGN,
									"+=",
									contextStack.getCurrentContext());
							case '+' :
								return newToken(
									IToken.tINCR,
									"++",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tPLUS,
									"+",
									contextStack.getCurrentContext());
						}
					case '-' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tMINUSASSIGN,
									"-=",
									contextStack.getCurrentContext());
							case '-' :
								return newToken(
									IToken.tDECR,
									"--",
									contextStack.getCurrentContext());
							case '>' :
								c = getChar();
								switch (c) {
									case '*' :
										return newToken(
											IToken.tARROWSTAR,
											"->*",
											contextStack.getCurrentContext());
									default :
										ungetChar(c);
										return newToken(
											IToken.tARROW,
											"->",
											contextStack.getCurrentContext());
								}
							default :
								ungetChar(c);
								return newToken(
									IToken.tMINUS,
									"-",
									contextStack.getCurrentContext());
						}
					case '*' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tSTARASSIGN,
									"*=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tSTAR,
									"*",
									contextStack.getCurrentContext());
						}
					case '%' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tMODASSIGN,
									"%=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tMOD,
									"%",
									contextStack.getCurrentContext());
						}
					case '^' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tXORASSIGN,
									"^=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tXOR,
									"^",
									contextStack.getCurrentContext());
						}
					case '&' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tAMPERASSIGN,
									"&=",
									contextStack.getCurrentContext());
							case '&' :
								return newToken(
									IToken.tAND,
									"&&",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tAMPER,
									"&",
									contextStack.getCurrentContext());
						}
					case '|' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tBITORASSIGN,
									"|=",
									contextStack.getCurrentContext());
							case '|' :
								return newToken(
									IToken.tOR,
									"||",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tBITOR,
									"|",
									contextStack.getCurrentContext());
						}
					case '~' :
						return newToken(IToken.tCOMPL, "~", contextStack.getCurrentContext());
					case '!' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tNOTEQUAL,
									"!=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tNOT,
									"!",
									contextStack.getCurrentContext());
						}
					case '=' :
						c = getChar();
						switch (c) {
							case '=' :
								return newToken(
									IToken.tEQUAL,
									"==",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tASSIGN,
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
											IToken.tSHIFTLASSIGN,
											"<<=",
											contextStack.getCurrentContext());
									default :
										ungetChar(c);
										return newToken(
											IToken.tSHIFTL,
											"<<",
											contextStack.getCurrentContext());
								}
							case '=' :
								return newToken(
									IToken.tLTEQUAL,
									"<=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(IToken.tLT, "<", contextStack.getCurrentContext());
						}
					case '>' :
						c = getChar();
						switch (c) {
							case '>' :
								c = getChar();
								switch (c) {
									case '=' :
										return newToken(
											IToken.tSHIFTRASSIGN,
											">>=",
											contextStack.getCurrentContext());
									default :
										ungetChar(c);
										return newToken(
											IToken.tSHIFTR,
											">>",
											contextStack.getCurrentContext());
								}
							case '=' :
								return newToken(
									IToken.tGTEQUAL,
									">=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(IToken.tGT, ">", contextStack.getCurrentContext());
						}
					case '.' :
						c = getChar();
						switch (c) {
							case '.' :
								c = getChar();
								switch (c) {
									case '.' :
										return newToken(
											IToken.tELIPSE,
											"...",
											contextStack.getCurrentContext());
									default :
										break;
								}
								break;
							case '*' :
								return newToken(
									IToken.tDOTSTAR,
									".*",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tDOT,
									".",
									contextStack.getCurrentContext());
						}
						break;
					case '/' :
						c = getChar();
						switch (c) {
							case '/' :
								skipOverSinglelineComment();
								c = getChar();
								continue;
							case '*' :
								skipOverMultilineComment();
								c = getChar();
								continue;
							case '=' :
								return newToken(
									IToken.tDIVASSIGN,
									"/=",
									contextStack.getCurrentContext());
							default :
								ungetChar(c);
								return newToken(
									IToken.tDIV,
									"/",
									contextStack.getCurrentContext());
						}
					default :
						handleProblem( IProblem.SCANNER_BAD_CHARACTER, new Character( (char)c ).toString(), getCurrentOffset(), false, true, throwExceptionOnBadCharacterRead ); 
						c = ' ';
						continue;
				}

				throw Parser.endOfFile;
			}
		}

		if (( getDepth() != 0) && !atEOF )
		{
			atEOF = true;
			handleProblem( IProblem.SCANNER_UNEXPECTED_EOF, null, getCurrentOffset(), false, true, true );
		}

		// we're done
		throw Parser.endOfFile;
	}



    /**
     * @param c
     * @param wideLiteral
     */
    protected IToken processCharacterLiteral(int c, boolean wideLiteral)
        throws ScannerException
    {
    	int beginOffset = getCurrentOffset();
        int type = wideLiteral ? IToken.tLCHAR : IToken.tCHAR;
        
        StringBuffer buffer = new StringBuffer(); 
        int prev = c; 
        int prevPrev = c;        
        c = getChar(true);
        
        for( ; ; )
        {
        	// error conditions
        	if( ( c == '\n' ) || 
        		( ( c == '\\' || c =='\'' )&& prev == '\\' ) || 
        	    ( c == NOCHAR ) )
        	{
        		handleProblem( IProblem.SCANNER_BAD_CHARACTER, new Character( (char)c ).toString(),beginOffset, false, true, throwExceptionOnBadCharacterRead );
        		c = ' ';
			}			
			// exit condition
			if ( ( c =='\'' ) && ( prev != '\\' || prevPrev == '\\' ) ) break;
			
        	buffer.append( (char)c);
        	prevPrev = prev;
        	prev = c;
        	c = getChar(true);
        }
        
        return newToken( type, buffer.toString(), contextStack.getCurrentContext());                      
    }



    protected String getCurrentFile()
	{
		return contextStack.getMostRelevantFileContext() != null ? contextStack.getMostRelevantFileContext().getFilename() : "";
	}


    protected int getCurrentOffset()
    {
        return contextStack.getMostRelevantFileContext() != null ? contextStack.getMostRelevantFileContext().getOffset() : -1;
    }


    protected static class endOfMacroTokenException extends Exception {};
    // the static instance we always use
    protected static endOfMacroTokenException endOfMacroToken = new endOfMacroTokenException();
    
    public IToken nextTokenForStringizing() throws ScannerException, EndOfFile
    {     
    	int beginOffset = getCurrentOffset();
        int c = getChar();
        StringBuffer tokenImage = new StringBuffer();

        try {
        while (c != NOCHAR) {

            if ((c == ' ') || (c == '\r') || (c == '\t') || (c == '\n')) {
                
                if (tokenImage.length() > 0) throw endOfMacroToken;                
                c = getChar();
                continue;
                
            } else if (c == '"') {
       
                if (tokenImage.length() > 0) throw endOfMacroToken;
                 
                // string
                StringBuffer buff = new StringBuffer(); 
                c = getChar(true);

                for( ; ; )
                {
                    if ( c =='"' ) break;
                    if( c == NOCHAR) break;  
                    buff.append((char) c);
                    c = getChar(true);
                }

                if (c != NOCHAR ) 
                {
                    return newToken( IToken.tSTRING, buff.toString(), contextStack.getCurrentContext());
    
                } else {
                	handleProblem( IProblem.SCANNER_UNBOUNDED_STRING, null, beginOffset, false, true, true );
                	c = getChar(); 
                	continue;
                }
        
            } else {
                switch (c) {
                    case '\'' :
	                    if (tokenImage.length() > 0) throw endOfMacroToken;
	                    return processCharacterLiteral( c, false );
                    case ',' :
                        if (tokenImage.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tCOMMA, ",", contextStack.getCurrentContext());
                    case '(' :
                        if (tokenImage.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tLPAREN, "(", contextStack.getCurrentContext());
                    case ')' :
                        if (tokenImage.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tRPAREN, ")", contextStack.getCurrentContext());
                    case '/' :
                        if (tokenImage.length() > 0) throw endOfMacroToken;
                        c = getChar();
                        switch (c) {
                            case '/' :
								skipOverSinglelineComment();
								c = getChar();
                                continue;
                            case '*' :
                                skipOverMultilineComment();
                                c = getChar();
                                continue;
                            default:
                                tokenImage.append('/');
                                continue;
                        }
                    default :
                        tokenImage.append((char)c);
                        c = getChar();
                }
            }
        }
        } catch (endOfMacroTokenException e) {
            // unget the first character after the end of token
            ungetChar(c);            
        }
        
        // return completed token
        if (tokenImage.length() > 0) {
            return newToken(IToken.tIDENTIFIER, tokenImage.toString(), contextStack.getCurrentContext());
        }
        
        // we're done
        throw Parser.endOfFile;
    }


	static {
		cppKeywords.put("and", new Integer(IToken.t_and));
		cppKeywords.put("and_eq", new Integer(IToken.t_and_eq));
		cppKeywords.put("asm", new Integer(IToken.t_asm));
		cppKeywords.put("auto", new Integer(IToken.t_auto));
		cppKeywords.put("bitand", new Integer(IToken.t_bitand));
		cppKeywords.put("bitor", new Integer(IToken.t_bitor));
		cppKeywords.put("bool", new Integer(IToken.t_bool));
		cppKeywords.put("break", new Integer(IToken.t_break));
		cppKeywords.put("case", new Integer(IToken.t_case));
		cppKeywords.put("catch", new Integer(IToken.t_catch));
		cppKeywords.put("char", new Integer(IToken.t_char));
		cppKeywords.put("class", new Integer(IToken.t_class));
		cppKeywords.put("compl", new Integer(IToken.t_compl));
		cppKeywords.put("const", new Integer(IToken.t_const));
		cppKeywords.put("const_cast", new Integer(IToken.t_const_cast));
		cppKeywords.put("continue", new Integer(IToken.t_continue));
		cppKeywords.put("default", new Integer(IToken.t_default));
		cppKeywords.put("delete", new Integer(IToken.t_delete));
		cppKeywords.put("do", new Integer(IToken.t_do));
		cppKeywords.put("double", new Integer(IToken.t_double));
		cppKeywords.put("dynamic_cast", new Integer(IToken.t_dynamic_cast));
		cppKeywords.put("else", new Integer(IToken.t_else));
		cppKeywords.put("enum", new Integer(IToken.t_enum));
		cppKeywords.put("explicit", new Integer(IToken.t_explicit));
		cppKeywords.put("export", new Integer(IToken.t_export));
		cppKeywords.put("extern", new Integer(IToken.t_extern));
		cppKeywords.put("false", new Integer(IToken.t_false));
		cppKeywords.put("float", new Integer(IToken.t_float));
		cppKeywords.put("for", new Integer(IToken.t_for));
		cppKeywords.put("friend", new Integer(IToken.t_friend));
		cppKeywords.put("goto", new Integer(IToken.t_goto));
		cppKeywords.put("if", new Integer(IToken.t_if));
		cppKeywords.put("inline", new Integer(IToken.t_inline));
		cppKeywords.put("int", new Integer(IToken.t_int));
		cppKeywords.put("long", new Integer(IToken.t_long));
		cppKeywords.put("mutable", new Integer(IToken.t_mutable));
		cppKeywords.put("namespace", new Integer(IToken.t_namespace));
		cppKeywords.put("new", new Integer(IToken.t_new));
		cppKeywords.put("not", new Integer(IToken.t_not));
		cppKeywords.put("not_eq", new Integer(IToken.t_not_eq));
		cppKeywords.put("operator", new Integer(IToken.t_operator));
		cppKeywords.put("or", new Integer(IToken.t_or));
		cppKeywords.put("or_eq", new Integer(IToken.t_or_eq));
		cppKeywords.put("private", new Integer(IToken.t_private));
		cppKeywords.put("protected", new Integer(IToken.t_protected));
		cppKeywords.put("public", new Integer(IToken.t_public));
		cppKeywords.put("register", new Integer(IToken.t_register));
		cppKeywords.put("reinterpret_cast", new Integer(IToken.t_reinterpret_cast));
		cppKeywords.put("return", new Integer(IToken.t_return));
		cppKeywords.put("short", new Integer(IToken.t_short));
		cppKeywords.put("signed", new Integer(IToken.t_signed));
		cppKeywords.put("sizeof", new Integer(IToken.t_sizeof));
		cppKeywords.put("static", new Integer(IToken.t_static));
		cppKeywords.put("static_cast", new Integer(IToken.t_static_cast));
		cppKeywords.put("struct", new Integer(IToken.t_struct));
		cppKeywords.put("switch", new Integer(IToken.t_switch));
		cppKeywords.put("template", new Integer(IToken.t_template));
		cppKeywords.put("this", new Integer(IToken.t_this));
		cppKeywords.put("throw", new Integer(IToken.t_throw));
		cppKeywords.put("true", new Integer(IToken.t_true));
		cppKeywords.put("try", new Integer(IToken.t_try));
		cppKeywords.put("typedef", new Integer(IToken.t_typedef));
		cppKeywords.put("typeid", new Integer(IToken.t_typeid));
		cppKeywords.put("typename", new Integer(IToken.t_typename));
		cppKeywords.put("union", new Integer(IToken.t_union));
		cppKeywords.put("unsigned", new Integer(IToken.t_unsigned));
		cppKeywords.put("using", new Integer(IToken.t_using));
		cppKeywords.put("virtual", new Integer(IToken.t_virtual));
		cppKeywords.put("void", new Integer(IToken.t_void));
		cppKeywords.put("volatile", new Integer(IToken.t_volatile));
		cppKeywords.put("wchar_t", new Integer(IToken.t_wchar_t));
		cppKeywords.put("while", new Integer(IToken.t_while));
		cppKeywords.put("xor", new Integer(IToken.t_xor));
		cppKeywords.put("xor_eq", new Integer(IToken.t_xor_eq));

		ppDirectives.put("#define", new Integer(PreprocessorDirectives.DEFINE));
		ppDirectives.put("#undef",new Integer(PreprocessorDirectives.UNDEFINE));
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

		cKeywords.put("auto", new Integer(IToken.t_auto));
		cKeywords.put("break", new Integer(IToken.t_break));
		cKeywords.put("case", new Integer(IToken.t_case));
		cKeywords.put("char", new Integer(IToken.t_char));
		cKeywords.put("const", new Integer(IToken.t_const));
		cKeywords.put("continue", new Integer(IToken.t_continue));
		cKeywords.put("default", new Integer(IToken.t_default));
		cKeywords.put("delete", new Integer(IToken.t_delete));
		cKeywords.put("do", new Integer(IToken.t_do));
		cKeywords.put("double", new Integer(IToken.t_double));
		cKeywords.put("else", new Integer(IToken.t_else));
		cKeywords.put("enum", new Integer(IToken.t_enum));
		cKeywords.put("extern", new Integer(IToken.t_extern));
		cKeywords.put("float", new Integer(IToken.t_float));
		cKeywords.put("for", new Integer(IToken.t_for));
		cKeywords.put("goto", new Integer(IToken.t_goto));
		cKeywords.put("if", new Integer(IToken.t_if));
		cKeywords.put("inline", new Integer(IToken.t_inline));
		cKeywords.put("int", new Integer(IToken.t_int));
		cKeywords.put("long", new Integer(IToken.t_long));
		cKeywords.put("register", new Integer(IToken.t_register));
		cKeywords.put("restrict", new Integer(IToken.t_restrict));
		cKeywords.put("return", new Integer(IToken.t_return));
		cKeywords.put("short", new Integer(IToken.t_short));
		cKeywords.put("signed", new Integer(IToken.t_signed));
		cKeywords.put("sizeof", new Integer(IToken.t_sizeof));
		cKeywords.put("static", new Integer(IToken.t_static));
		cKeywords.put("struct", new Integer(IToken.t_struct));
		cKeywords.put("switch", new Integer(IToken.t_switch));
		cKeywords.put("typedef", new Integer(IToken.t_typedef));
		cKeywords.put("union", new Integer(IToken.t_union));
		cKeywords.put("unsigned", new Integer(IToken.t_unsigned));
		cKeywords.put("void", new Integer(IToken.t_void));
		cKeywords.put("volatile", new Integer(IToken.t_volatile));
		cKeywords.put("while", new Integer(IToken.t_while));
		cKeywords.put("_Bool", new Integer(IToken.t__Bool));
		cKeywords.put("_Complex", new Integer(IToken.t__Complex));
		cKeywords.put("_Imaginary", new Integer(IToken.t__Imaginary));

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

	protected boolean evaluateExpression(String expression, int beginningOffset )
		throws ScannerException {
			
		if( mode == ParserMode.QUICK_PARSE )
		{
			if( expression.trim().equals( "0" ) )
				return false; 
			return true; 
		}
		else
		{	
			final NullSourceElementRequestor nullCallback = new NullSourceElementRequestor();
			IParser parser = null;
			try
			{
				IScanner trial =
					ParserFactory.createScanner(
						new StringReader(expression + ";"),
							EXPRESSION,
							new ScannerInfo( definitions, originalConfig.getIncludePaths()), 
							ParserMode.QUICK_PARSE, language, nullCallback, log );
	            parser = ParserFactory.createParser(trial, nullCallback, ParserMode.QUICK_PARSE, language, log );
			} catch( ParserFactoryException pfe )
			{
				// TODO - make INTERNAL IProblem
				// should never happen
			}
			try {
				IASTExpression exp = parser.expression(null);
				if( exp.evaluateExpression() == 0 )
					return false;
				return true;
			} catch( Backtrack backtrack  )
			{
				handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true, true ); 
			}
			catch (ExpressionEvaluationException e) {
				handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true, true );
			}
			return true; 
		}
	}

	
	protected void skipOverSinglelineComment() throws ScannerException {
		
		StringBuffer comment = new StringBuffer("//");
		int c;
		
		loop:
		for (;;) {
			c = getChar();
			comment.append((char)c);
			switch (c) {
				case NOCHAR :
				case '\n' :
					break loop;
				default :
					break;
			}
		}
		
	}

	protected boolean skipOverMultilineComment() throws ScannerException {
		int state = 0;
		boolean encounteredNewline = false;
		StringBuffer comment = new StringBuffer("/*");
		// simple state machine to handle multi-line comments
		// state 0 == no end of comment in site
		// state 1 == encountered *, expecting /
		// state 2 == we are no longer in a comment

		int c = getChar();
		comment.append((char)c);
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
			comment.append((char)c);
		}

		if (c == NOCHAR)
			handleProblem( IProblem.SCANNER_UNEXPECTED_EOF, null, getCurrentOffset(), false, true, true );
		
		ungetChar(c);

		return encounteredNewline;
	}

	protected void poundInclude( int beginningOffset ) throws ScannerException {

		skipOverWhitespace();				
		int baseOffset = lastContext.getOffset() - lastContext.undoStackSize();
		String includeLine = getRestOfPreprocessorLine();
		StringBuffer fileName = new StringBuffer();
		boolean useIncludePath = true;
		int startOffset = baseOffset;
		int endOffset = baseOffset; 
			
		if (! includeLine.equals("")) {
			Scanner helperScanner = new Scanner(
										new StringReader(includeLine), 
										null, 
										new ScannerInfo(definitions, originalConfig.getIncludePaths()), 
										new NullSourceElementRequestor(),
										mode,
										language, log );
			IToken t = null;
			
			try {
				t = helperScanner.nextToken(false);
			} catch (EndOfFile eof) {
				handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#include " + includeLine, beginningOffset, false, true, true );				
				return;
			}

			try {
				if (t.getType() == IToken.tSTRING) {
					fileName.append(t.getImage());
					startOffset = baseOffset + t.getOffset();
					endOffset = baseOffset + t.getEndOffset();
					useIncludePath = false;
					
					// This should throw EOF
					t = helperScanner.nextToken(false);
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#include " + includeLine, beginningOffset, false, true, true );					
					return;
				} else if (t.getType() == IToken.tLT) {
					
					try {
											
						t = helperScanner.nextToken(false);
						startOffset = baseOffset + t.getOffset();
						
						while (t.getType() != IToken.tGT) {
							fileName.append(t.getImage());
							helperScanner.skipOverWhitespace();
							int c = helperScanner.getChar();
							if (c == '\\') fileName.append('\\'); else helperScanner.ungetChar(c);
							t = helperScanner.nextToken(false);
						}
						
						endOffset = baseOffset + t.getEndOffset();
						
					} catch (EndOfFile eof) {
						handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#include " + includeLine, beginningOffset, false, true, true );
						return;
					}
					
					// This should throw EOF
					t = helperScanner.nextToken(false);
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#include " + includeLine, beginningOffset, false, true, true );
	
					return;
					
				} else 
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#include " + includeLine, beginningOffset, false, true, true );
			}
			catch( EndOfFile eof )
			{
				// good
			}
			
		} else 
			handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#include " + includeLine, beginningOffset, false, true, true );

		String f = fileName.toString();
		
		if( mode == ParserMode.QUICK_PARSE )
		{ 
			if( requestor != null )
			{
				IASTInclusion i = null;
                try
                {
                    i =
                        astFactory.createInclusion(
                            f,
                            "",
                            !useIncludePath,
                            beginningOffset,
                            startOffset,
                            startOffset + f.length(),
                            endOffset);
                }
                catch (Exception e)
                {
                    /* do nothing */
                }
                if( i != null )
                {
					i.enterScope( requestor );
					i.exitScope( requestor );
                }					 
			}
		}
		else
			handleInclusion(f.trim(), useIncludePath, startOffset, beginningOffset, endOffset); 
	}

	protected void poundDefine(int beginning) throws ScannerException, EndOfFile {
		skipOverWhitespace();
		// definition 
		String key = getNextIdentifier();
		int offset = contextStack.getCurrentContext().getOffset() - key.length() - contextStack.getCurrentContext().undoStackSize();

		// store the previous definition to check against later
		Object previousDefinition = definitions.get( key );

		// get the next character
		// the C++ standard says that macros must not put
		// whitespace between the end of the definition 
		// identifier and the opening parenthesis
		int c = getChar();
		if (c == '(') {
			StringBuffer buffer = new StringBuffer();
			c = getChar(true);
			while (c != ')') {
				if( c == '\\' ){
					c = getChar();
					if( c == '\r' )
						c = getChar();	
					
					if( c == '\n' ){
						c = getChar();
						continue;
					} else {
						ungetChar( c );
						handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, "#define " + buffer.toString() + '\\' + (char)c, beginning, false, true, true );
						return;
					}
				} else if( c == '\r' || c == '\n' ){
					handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, "#define " + buffer.toString() + (char)c, beginning, false, true, true );
					return;
				} else if( c == NOCHAR ){
					handleProblem( IProblem.SCANNER_UNEXPECTED_EOF, null, beginning, false, true, true );
					return;
				}
				
				buffer.append((char) c);
				c = getChar(true);
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
			
			if( ! replacementString.equals( "" ) )
			{
				IScanner helperScanner=null;
				try {
					helperScanner =
						ParserFactory.createScanner(
							new StringReader(replacementString),
							SCRATCH,
							new ScannerInfo(),
							mode,
							language,
							new NullSourceElementRequestor(), log);
				} catch (ParserFactoryException e1) {
				}
				helperScanner.setTokenizingMacroReplacementList( true );
				IToken t = helperScanner.nextToken(false);
	
				try {
					while (true) {
						//each # preprocessing token in the replacement list shall be followed
						//by a parameter as the next reprocessing token in the list
						if( t.getType() == tPOUND ){
							macroReplacementTokens.add( t );
							t = helperScanner.nextToken(false);
							int index = parameterIdentifiers.indexOf(t.getImage());
							if (index == -1 ) {
								//not found
								
								handleProblem( IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, "#define " + key + " " + replacementString,
									beginning, false, true, true ); 									
								return;
							}
						}
						
						macroReplacementTokens.add(t);
						t = helperScanner.nextToken(false);
					}
				}
				catch( EndOfFile eof )
				{
					// good
				}
			}

			IMacroDescriptor descriptor = new MacroDescriptor();
			descriptor.initialize(
				key,
				parameterIdentifiers,
				macroReplacementTokens,
				key + "(" + parameters + ")");
				
			checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning);
			addDefinition(key, descriptor);

		}
		else if ((c == '\n') || (c == '\r'))
		{
			checkValidMacroRedefinition(key, previousDefinition, "", beginning);				
			addDefinition( key, "" );
		}
		else if ((c == ' ') || (c == '\t') ) {
			// this is a simple definition 
			skipOverWhitespace();

			// get what we are to map the name to and add it to the definitions list
			String value = getRestOfPreprocessorLine();
			
			checkValidMacroRedefinition(key, previousDefinition, value, beginning);
			addDefinition( key, value ); 
		
		} else if (c == '/') {
			// this could be a comment	
			c = getChar();
			if (c == '/') // one line comment
				{
				skipOverSinglelineComment();
				checkValidMacroRedefinition(key, previousDefinition, "", beginning);
				addDefinition(key, "");
			} else if (c == '*') // multi-line comment
				{
				if (skipOverMultilineComment()) {
					// we have gone over a newline
					// therefore, this symbol was defined to an empty string
					checkValidMacroRedefinition(key, previousDefinition, "", beginning);
					addDefinition(key, "");
				} else {
					String value = getRestOfPreprocessorLine();
					
					checkValidMacroRedefinition(key, previousDefinition, "", beginning);
					addDefinition(key, value);
				}
			} else {
				// this is not a comment 
				// it is a bad statement
				handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, "#define " + key + " /" + getRestOfPreprocessorLine(), beginning, false, true, true );
				return;
			}
		} else {
			log.traceLog("Scanner : Encountered unexpected character " + ((char) c));
			handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, "#define " + key + (char)c + getRestOfPreprocessorLine(), beginning, false, true, true );
			return;
		}
		
		try
        {
            astFactory.createMacro( key, beginning, offset, offset + key.length(), contextStack.getCurrentContext().getOffset() ).acceptElement( requestor );
        }
        catch (Exception e)
        {
            /* do nothing */
        } 
	}



	protected void checkValidMacroRedefinition(
		String key,
		Object previousDefinition,
		Object newDefinition, int beginningOffset )
		throws ScannerException 
		{
			if( mode == ParserMode.COMPLETE_PARSE && previousDefinition != null ) 
			{
				if( newDefinition instanceof IMacroDescriptor )
				{
					if( previousDefinition instanceof IMacroDescriptor )
					{
						if( ((IMacroDescriptor)previousDefinition).compatible( (IMacroDescriptor) newDefinition ) )
							return; 		
					}					
				}
				else if( newDefinition instanceof String )
				{				 
					
					if( previousDefinition instanceof String ) 
					{
						Scanner previous = new Scanner( new StringReader( (String)previousDefinition ), "redef-test", new ScannerInfo(), new NullSourceElementRequestor(), 
							mode, language, log );
						Scanner current = new Scanner( new StringReader( (String)newDefinition ), "redef-test", new ScannerInfo(), new NullSourceElementRequestor(), 
													mode, language, log );
						for ( ; ; )
						{
							IToken p = null;
							IToken c = null;
							try
							{
								p = previous.nextToken(); 
								c = current.nextToken();
								
								if ( c.equals( p ) ) continue;
								break; 
								
							}
							catch( EndOfFile eof )
							{
								if( ( p != null ) && ( c == null ) )
									break;
								if( p == null )
								{
									try
									{
										c = current.nextToken(); 
										break; 
									}
									catch( EndOfFile eof2 )
									{
										return;
									}
								}
							}
						} 
					}
				}
				
				handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN, key, beginningOffset, false, true, true );
			}			
	}
    
    protected Vector getMacroParameters (String params, boolean forStringizing) throws ScannerException {
        
        Scanner tokenizer  = new Scanner(new StringReader(params), TEXT, new ScannerInfo( definitions, originalConfig.getIncludePaths() ), new NullSourceElementRequestor(), mode, language, log);
        tokenizer.setThrowExceptionOnBadCharacterRead(false);
        Vector parameterValues = new Vector();
        Token t = null;
        String str = new String();
        boolean space = false;
        int nParen = 0;
        
        try {
            while (true) {
				int c = tokenizer.getChar();
				if ((c != ' ') && (c != '\t') && (c != '\r') && (c != '\n')) {
					space = false;
				}
				if (c != NOCHAR) tokenizer.ungetChar(c);
				
                t = (Token)(forStringizing ? tokenizer.nextTokenForStringizing() : tokenizer.nextToken(false));
                if (t.type == IToken.tLPAREN) {
                    nParen++;
                } else if (t.type == IToken.tRPAREN) {
                    nParen--;
                } else if (t.type == IToken.tCOMMA && nParen == 0) {
                    parameterValues.add(str);
                    str = "";
                    space = false;
                    continue;
                }

                if (space)
                    str += ' ';

                switch (t.type) {
                    case IToken.tSTRING :  str += '\"' + t.image + '\"'; break;
                    case IToken.tLSTRING : str += "L\"" + t.image + '\"'; break;
                    case IToken.tCHAR :    str += '\'' + t.image + '\'';  break;
                    default :             str += t.image; break;
                }
                space = true;
            }
        } catch (EndOfFile e) {
            // Good
            parameterValues.add(str);
        }
        
        return parameterValues;
    }
    	
	protected void expandDefinition(String symbol, Object expansion, int symbolOffset) 
                    throws ScannerException 
    {
        // All the tokens generated by the macro expansion 
        // will have dimensions (offset and length) equal to the expanding symbol.
		if (expansion instanceof String ) {
			String replacementValue = (String) expansion;
			try
			{
				contextStack.updateContext( new StringReader(replacementValue), (POUND_DEFINE + symbol ), ScannerContext.MACROEXPANSION, null, requestor, symbolOffset, symbol.length());
			}
			catch (ContextException e)
			{
				handleProblem( e.getId(), contextStack.getCurrentContext().getFilename(), getCurrentOffset(), false, true, true );
				consumeUntilOutOfMacroExpansion();
				return;
			}
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

					if(bracketCount == 0 || c == NOCHAR)
						break;
					buffer.append((char) c);
					c = getChar( true );
				}
                
                // Position of the closing ')'
                int endMacroOffset = lastContext.getOffset() - lastContext.undoStackSize() - 1;
				
				String betweenTheBrackets = buffer.toString().trim();
                
                Vector parameterValues = getMacroParameters(betweenTheBrackets, false);
                Vector parameterValuesForStringizing = getMacroParameters(betweenTheBrackets, true);
                Token t = null;
                
				// create a string that represents what needs to be tokenized
				buffer = new StringBuffer();
				List tokens = macro.getTokenizedExpansion();
				List parameterNames = macro.getParameters();

				if (parameterNames.size() != parameterValues.size())
				{ 
					handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, symbol, getCurrentOffset(), false, true, true );	
					consumeUntilOutOfMacroExpansion();
					return;
				}				

				int numberOfTokens = tokens.size();

				for (int i = 0; i < numberOfTokens; ++i) {
					t = (Token) tokens.get(i);
					if (t.type == IToken.tIDENTIFIER) {

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
							handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macro.getName(), getCurrentOffset(), false, true, true  );
							return;
						} else {
							buffer.append('\"');
							String value = (String)parameterValuesForStringizing.elementAt(index);
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
						switch( t.type )
						{
							case IToken.tSTRING:  buffer.append('\"' + t.image + '\"');  break;
							case IToken.tLSTRING: buffer.append("L\"" + t.image + '\"');	break;
							case IToken.tCHAR:	 buffer.append('\'' + t.image + '\''); 	break;
							default:			 buffer.append(t.image);				break;
						}
					}
					
					boolean pastingNext = false;
					
					if( i != numberOfTokens - 1)
					{
						IToken t2 = (IToken) tokens.get(i+1);
						if( t2.getType() == tPOUNDPOUND ) {
							pastingNext = true;
							i++;
						}  
					}
					
					if( t.getType() != tPOUNDPOUND && ! pastingNext )
						if (i < (numberOfTokens-1)) // Do not append to the last one 
                        	buffer.append( " " ); 
				}
				String finalString = buffer.toString();
				try
				{
					contextStack.updateContext(
						new StringReader(finalString),
						POUND_DEFINE + macro.getSignature(), ScannerContext.MACROEXPANSION, null, requestor, symbolOffset, endMacroOffset - symbolOffset + 1 );
				}
				catch (ContextException e)
				{
					handleProblem( e.getId(), contextStack.getCurrentContext().getFilename(), getCurrentOffset(), false, true, true );
					consumeUntilOutOfMacroExpansion();
					return;
				}
			} else
			{ 
				handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, symbol, getCurrentOffset(), false, true, true );
				consumeUntilOutOfMacroExpansion();
				return;
			}			

		} else {
			log.traceLog(
				"Unexpected class stored in definitions table. "
					+ expansion.getClass().getName() );
		}

	}

	protected String handleDefinedMacro() throws ScannerException {
		int o = getCurrentOffset();
		skipOverWhitespace();

		int c = getChar();
		
		String definitionIdentifier = null;
		if (c == '(') {

			definitionIdentifier = getNextIdentifier(); 
			skipOverWhitespace(); 
			c = getChar();
			if (c != ')')
			{
				handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, "defined()", o, false, true, true);
				return "0";
			}
		}
		else
		{
			ungetChar(c); 
			definitionIdentifier = getNextIdentifier(); 
		}		

		if (definitions.get(definitionIdentifier) != null)
			return "1";

		return "0";
	}
	
	

	private ParserLanguage language = ParserLanguage.CPP; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IScanner#setCppNature(boolean)
	 */
	public void setLanguage( ParserLanguage value) {
		language = value; 
	}
	
	public void setThrowExceptionOnBadCharacterRead( boolean throwOnBad ){
		throwExceptionOnBadCharacterRead = throwOnBad;
	}
	
	private final ISourceElementRequestor requestor;
	private IASTFactory astFactory = null; 
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setASTFactory(org.eclipse.cdt.internal.core.parser.ast.IASTFactory)
	 */
	public void setASTFactory(IASTFactory f) {
		astFactory = f;	
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScanner#getLineNumberForOffset(int)
     */
    public int getLineNumberForOffset(int i)
    {
        ILineOffsetReconciler reconciler = ParserFactory.createLineOffsetReconciler( backupReader );
        return reconciler.getLineNumberForOffset(i);
    }
}
