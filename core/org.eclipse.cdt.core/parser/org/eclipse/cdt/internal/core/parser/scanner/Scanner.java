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
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.Directives;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.IMacroDescriptor.MacroType;
import org.eclipse.cdt.core.parser.ast.ASTExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.internal.core.parser.IExpressionParser;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.ast.ASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.Token;

/**
 * @author jcamelon
 *
 */

public class Scanner implements IScanner {

	private static final NullSourceElementRequestor NULL_REQUESTOR = new NullSourceElementRequestor();
	private final static String SCRATCH = "<scratch>"; //$NON-NLS-1$
	protected final IScannerData scannerData;

	private boolean initialContextInitialized = false;

	protected IToken finalToken;
	private final IScannerExtension scannerExtension;
	private static final int NO_OFFSET_LIMIT = -1;
	private int offsetLimit = NO_OFFSET_LIMIT;
	private boolean limitReached = false; 
	
	protected void handleProblem( int problemID, String argument, int beginningOffset, boolean warning, boolean error ) throws ScannerException
	{
		handleProblem( problemID, argument, beginningOffset, warning, error, true );
	}

	protected void handleProblem( int problemID, String argument, int beginningOffset, boolean warning, boolean error, boolean extra ) throws ScannerException
	{
		Map arguments = new HashMap(); 
		if( argument != null )
		{
			String attributes [] = scannerData.getProblemFactory().getRequiredAttributesForId( problemID );
			arguments.put( attributes[ 0 ], argument );
		}
		
		IProblem problem = scannerData.getProblemFactory().createProblem( problemID, beginningOffset, getCurrentOffset(), scannerData.getContextStack().getCurrentLineNumber(), getCurrentFile().toCharArray(), arguments, warning, error );
		
		// trace log
		StringBuffer logMessage = new StringBuffer( "Scanner problem encountered: "); //$NON-NLS-1$
		logMessage.append( problem.getMessage() );
		scannerData.getLogService().traceLog( logMessage.toString() );
		
		if( (! scannerData.getClientRequestor().acceptProblem( problem )) && extra )
			throw new ScannerException( problem );
	}

	Scanner( Reader reader, String filename, Map definitions, List includePaths, ISourceElementRequestor requestor, ParserMode mode, ParserLanguage language, IParserLogService log, IScannerExtension extension )
	{
		String [] incs = (String [])includePaths.toArray(STRING_ARRAY);
    	scannerData = new ScannerData( this, log, requestor, mode, filename, reader, language, new ScannerInfo( definitions, incs ), new ContextStack( this, log ) );
    	
		scannerExtension = extension;
		if( scannerExtension instanceof GCCScannerExtension )
			((GCCScannerExtension)scannerExtension).setScannerData( scannerData );	 
		
		scannerData.setDefinitions( definitions );
		scannerData.setIncludePathNames( includePaths );
		scannerData.setASTFactory( ParserFactory.createASTFactory( this, scannerData.getParserMode(), language ) );
		try {
			//this is a hack to get around a sudden EOF experience
			scannerData.getContextStack().push(
						new ScannerContext(
						new StringReader("\n"), //$NON-NLS-1$
						START,
						ScannerContext.ContextKind.SENTINEL, null), requestor);

		} catch( ContextException ce ) {
			//won't happen since we aren't adding an include or a macro
		} 
		
	}
	
    public Scanner(Reader reader, String filename, IScannerInfo info, ISourceElementRequestor requestor, ParserMode parserMode, ParserLanguage language, IParserLogService log, IScannerExtension extension ) {
    	
    	scannerData = new ScannerData( this, log, requestor, parserMode, filename, reader, language, info, new ContextStack( this, log ) );

		scannerExtension = extension;
		if( scannerExtension instanceof GCCScannerExtension )
			((GCCScannerExtension)scannerExtension).setScannerData( scannerData );	 
		
		scannerData.setASTFactory( ParserFactory.createASTFactory( this, scannerData.getParserMode(), language ) );
		try {
			//this is a hack to get around a sudden EOF experience
			scannerData.getContextStack().push(
						new ScannerContext(
						new StringReader("\n"), //$NON-NLS-1$
						START,
						ScannerContext.ContextKind.SENTINEL, null), requestor);

		} catch( ContextException ce ) {
			//won't happen since we aren't adding an include or a macro
			// assert false
		} 
		
		
		log.traceLog( "Scanner constructed with the following configuration:"); //$NON-NLS-1$
		log.traceLog( "\tPreprocessor definitions from IScannerInfo: "); //$NON-NLS-1$

		if( info.getDefinedSymbols() != null )
		{
			Iterator i = info.getDefinedSymbols().keySet().iterator(); 
			Map m = info.getDefinedSymbols();
			int numberOfSymbolsLogged = 0; 
			while( i.hasNext() )
			{
				String symbolName = (String) i.next();
				Object value = m.get( symbolName );
				if( value instanceof String )
				{	
					addDefinition( symbolName, scannerExtension.initializeMacroValue((String) value));
					log.traceLog( "\t\tNAME = " + symbolName + " VALUE = " + value.toString() ); //$NON-NLS-1$ //$NON-NLS-2$
					++numberOfSymbolsLogged;
					
				}
				else if( value instanceof IMacroDescriptor )
					addDefinition( symbolName, (IMacroDescriptor)value);
			}
			if( numberOfSymbolsLogged == 0 )
				log.traceLog( "\t\tNo definitions specified."); //$NON-NLS-1$
			
		}
		else 
			log.traceLog( "\t\tNo definitions specified."); //$NON-NLS-1$
		
		
		log.traceLog( "\tInclude paths from IScannerInfo: "); //$NON-NLS-1$
		if( info.getIncludePaths() != null )
		{	
			overwriteIncludePath( info.getIncludePaths() );
			for( int i = 0; i < info.getIncludePaths().length; ++i )
				log.traceLog( "\t\tPATH: " + info.getIncludePaths()[i]); //$NON-NLS-1$
		}
		else 
			log.traceLog("\t\tNo include paths specified."); //$NON-NLS-1$
		
		setupBuiltInMacros();
    }

    /**
	 * 
	 */
	protected void setupBuiltInMacros() {
		
		scannerExtension.setupBuiltInMacros(scannerData.getLanguage());
		if( getDefinition(__STDC__) == null )
			addDefinition( __STDC__, new ObjectMacroDescriptor( __STDC__,  "1") ); //$NON-NLS-1$
		
		if( scannerData.getLanguage() == ParserLanguage.C )
		{
			if( getDefinition(__STDC_HOSTED__) == null )
				addDefinition( __STDC_HOSTED__, new ObjectMacroDescriptor( __STDC_HOSTED__, "0")); //$NON-NLS-1$
			if( getDefinition( __STDC_VERSION__) == null )
				addDefinition( __STDC_VERSION__, new ObjectMacroDescriptor( __STDC_VERSION__, "199001L")); //$NON-NLS-1$
		}
		else
			if( getDefinition( __CPLUSPLUS ) == null )
					addDefinition( __CPLUSPLUS, new ObjectMacroDescriptor( __CPLUSPLUS, "199711L")); //$NON-NLS-1$
		
		if( getDefinition(__FILE__) == null )
			addDefinition(  __FILE__, 
					new DynamicMacroDescriptor( __FILE__, new DynamicMacroEvaluator() {
						public String execute() {
							return scannerData.getContextStack().getMostRelevantFileContext().getFilename();
						}				
					} ) );
		
		if( getDefinition( __LINE__) == null )
			addDefinition(  __LINE__, 
					new DynamicMacroDescriptor( __LINE__, new DynamicMacroEvaluator() {
						public String execute() {
							return new Integer( scannerData.getContextStack().getCurrentLineNumber() ).toString();
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
		
		
	}

	private void setupInitialContext()
    {
    	String resolvedFilename = scannerData.getInitialFilename() == null ? TEXT : scannerData.getInitialFilename();
    	IScannerContext context = null;
    	try
    	{
    		if( offsetLimit == NO_OFFSET_LIMIT )
    			context = new ScannerContext(scannerData.getInitialReader(), resolvedFilename, ScannerContext.ContextKind.TOP, null );
    		else
    			context = new LimitedScannerContext( this, scannerData.getInitialReader(), resolvedFilename, ScannerContext.ContextKind.TOP, offsetLimit );
    		scannerData.getContextStack().push( context, scannerData.getClientRequestor() ); 
    	} catch( ContextException  ce )
    	{
    		handleInternalError();
    	}
    	initialContextInitialized = true;   	
    }
	
	public void addIncludePath(String includePath) {
		scannerData.getIncludePathNames().add(includePath);
	}

	public void overwriteIncludePath(String [] newIncludePaths) {
		if( newIncludePaths == null ) return;
		scannerData.setIncludePathNames(new ArrayList());
		
		for( int i = 0; i < newIncludePaths.length; ++i )
		{
			String path = newIncludePaths[i];
			
			File file = new File( path );
			
			if( !file.exists() && path.indexOf('\"') != -1 )
			{
				StringTokenizer tokenizer = new StringTokenizer(path, "\"" );	//$NON-NLS-1$
				StringBuffer buffer = new StringBuffer(path.length() );
				while( tokenizer.hasMoreTokens() ){
					buffer.append( tokenizer.nextToken() );
				}
				file = new File( buffer.toString() );
			}

			if( file.exists() && file.isDirectory() )
				scannerData.getIncludePathNames().add( path );
			
		}		
	}

	public void addDefinition(String key, IMacroDescriptor macro) {
		scannerData.getDefinitions().put(key, macro);
	}

	public void addDefinition(String key, String value) {
		StringBuffer signatureBuffer  = new StringBuffer();
		signatureBuffer.append( key );
		signatureBuffer.append( ' ' );
		signatureBuffer.append( value );
		
		addDefinition(key,
				new ObjectMacroDescriptor( key, signatureBuffer.toString(), 
						tokenizeReplacementString( NO_OFFSET_LIMIT, key, value, null ), value ));
	}

	public final IMacroDescriptor getDefinition(String key) {
		return (IMacroDescriptor) scannerData.getDefinitions().get(key); 
	}

	public final String[] getIncludePaths() {
		return (String[])scannerData.getIncludePathNames().toArray();
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

	protected String getRestOfPreprocessorLine() throws ScannerException, EndOfFileException {
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
		finalToken = t;
		currentToken = t;
	}
	
	protected void resetStorageBuffer()
	{
		if( storageBuffer != null ) 
			storageBuffer = null; 
	}

	protected IToken newToken(int t, String i, IScannerContext c) {
		setCurrentToken(new Token(t, i, c, scannerData.getContextStack().getCurrentLineNumber()));
		return currentToken;
	}

	protected IToken newConstantToken(int t) {
		setCurrentToken(
				new Token(
						t, 
						scannerData.getContextStack().getCurrentContext(),
						scannerData.getContextStack().getCurrentLineNumber()
						)
				);
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

	protected void handleInclusion(String fileName, boolean useIncludePaths, int beginOffset, int startLine, int nameOffset, int nameLine, int endOffset, int endLine ) throws ScannerException {

		CodeReader duple = null;
		totalLoop:	for( int i = 0; i < 2; ++i )
		{
			if( useIncludePaths ) // search include paths for this file
			{
				// iterate through the include paths 
				Iterator iter = scannerData.getIncludePathNames().iterator();
		
				while (iter.hasNext()) {
		
					String path = (String)iter.next();
					duple = ScannerUtility.createReaderDuple( path, fileName, scannerData.getClientRequestor() );
					if( duple != null )
						break totalLoop;
				}
				
				if (duple == null )
					handleProblem( IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, fileName, beginOffset, false, true );
	
			}
			else // local inclusion
			{
				duple = ScannerUtility.createReaderDuple( new File( scannerData.getContextStack().getCurrentContext().getFilename() ).getParentFile().getAbsolutePath(), fileName, scannerData.getClientRequestor() );
				if( duple != null )
					break totalLoop;
				useIncludePaths = true;
				continue totalLoop;
			}
		}
		
		if (duple!= null) {
			IASTInclusion inclusion = null;
            try
            {
                inclusion =
                    scannerData.getASTFactory().createInclusion(
                        fileName,
                        duple.getFilename(),
                        !useIncludePaths,
                        beginOffset,
                        startLine,
                        nameOffset,
                        nameOffset + fileName.length(), nameLine, endOffset, endLine);
            }
            catch (Exception e)
            {
                /* do nothing */
            } 
			
			try
			{
				scannerData.getContextStack().updateContext(duple.getUnderlyingReader(), duple.getFilename(), ScannerContext.ContextKind.INCLUSION, inclusion, scannerData.getClientRequestor() );
			}
			catch (ContextException e1)
			{
				handleProblem( e1.getId(), fileName, beginOffset, false, true );
			}
		}
	}

/*	protected void handleInclusion(String fileName, boolean useIncludePaths, int beginOffset, int startLine, int nameOffset, int nameLine, int endOffset, int endLine ) throws ScannerException {
// if useIncludePaths is true then 
//     #include <foo.h>
//  else
//     #include "foo.h"
		
		Reader inclusionReader = null;
		File includeFile = null;
		
		if( !useIncludePaths ) {  // local inclusion is checked first 			
			String currentFilename = scannerData.getContextStack().getCurrentContext().getFilename(); 
			File currentIncludeFile = new File( currentFilename );
			String parentDirectory = currentIncludeFile.getParentFile().getAbsolutePath();
			currentIncludeFile = null; 
			
			//TODO remove ".." and "." segments 
			includeFile = new File( parentDirectory, fileName );
			if (includeFile.exists() && includeFile.isFile()) {
				try {
					inclusionReader = new BufferedReader(new FileReader(includeFile));
				} catch (FileNotFoundException fnf) {
					inclusionReader = null;
				}
			}
		}
			
		// search include paths for this file
		// iterate through the include paths 
		Iterator iter = scannerData.getIncludePathNames().iterator();

		while ((inclusionReader == null) && iter.hasNext()) {
			String path = (String)iter.next();
			//TODO remove ".." and "." segments
			includeFile = new File (path, fileName);
			if (includeFile.exists() && includeFile.isFile()) {
				try {
					inclusionReader = new BufferedReader(new FileReader(includeFile));
				} catch (FileNotFoundException fnf) {
					inclusionReader = null;
				}
			}
		}
		
		if (inclusionReader == null )
			handleProblem( IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, fileName, beginOffset, false, true );

		else {
			IASTInclusion inclusion = null;
            try
            {
                inclusion =
                    scannerData.getASTFactory().createInclusion(
                        fileName,
                        includeFile.getPath(),
                        !useIncludePaths,
                        beginOffset,
                        startLine,
                        nameOffset,
                        nameOffset + fileName.length(), nameLine, endOffset, endLine);
            }
            catch (Exception e)
            {
                 do nothing 
            } 
			
			try
			{
				scannerData.getContextStack().updateContext(inclusionReader, includeFile.getPath(), ScannerContext.ContextKind.INCLUSION, inclusion, scannerData.getClientRequestor() );
			}
			catch (ContextException e1)
			{
				handleProblem( e1.getId(), fileName, beginOffset, false, true );
			}
		}
	}
*/
	// constants
	private static final int NOCHAR = -1;

	private static final String TEXT = "<text>"; //$NON-NLS-1$
	private static final String START = "<initial reader>"; //$NON-NLS-1$
	private static final String EXPRESSION = "<expression>"; //$NON-NLS-1$
	private static final String PASTING = "<pasting>"; //$NON-NLS-1$

	private static final String DEFINED = "defined"; //$NON-NLS-1$
	private static final String _PRAGMA = "_Pragma"; //$NON-NLS-1$
	private static final String POUND_DEFINE = "#define "; //$NON-NLS-1$

	private IScannerContext lastContext = null;
	 
	private StringBuffer storageBuffer = null; 
	
	private int count = 0;
	private static HashMap cppKeywords = new HashMap();
	private static HashMap cKeywords = new HashMap(); 
	private static HashMap ppDirectives = new HashMap();

	private IToken currentToken = null;
	private IToken cachedToken = null;

	private boolean passOnToClient = true; 
	

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
	
	public int getCharacter() throws ScannerException
	{
		if( ! initialContextInitialized )
			setupInitialContext();
		
		return getChar();
	}
	
	int getChar() throws ScannerException
	{
		return getChar( false );
	}

	private int getChar( boolean insideString ) throws ScannerException {	
		int c = NOCHAR;
		
		lastContext = scannerData.getContextStack().getCurrentContext();
		
		if (scannerData.getContextStack().getCurrentContext() == null)
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
							expandDefinition("??(", "[", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case ')':
							expandDefinition("??)", "]", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '<':
							expandDefinition("??<", "{", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '>':
							expandDefinition("??>", "}", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '=':
							expandDefinition("??=", "#", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '/':
							expandDefinition("??/", "\\", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '\'':
							expandDefinition("??\'", "^", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '!':
							expandDefinition("??!", "|", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '-':
							expandDefinition("??-", "~", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
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
			if (enableDigraphReplacement) {
				enableDigraphReplacement = false;
				// Digraph processing
				if (c == '<') {
					c = getChar(false);
					if (c == '%') {
						expandDefinition("<%", "{", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else if (c == ':') {
						expandDefinition("<:", "[", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else {
						// Not a digraph
						ungetChar(c);
						c = '<';
					}
				} else if (c == ':') {
					c = getChar(false);
					if (c == '>') {
						expandDefinition(":>", "]", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else {
						// Not a digraph
						ungetChar(c);
						c = ':';
					}
				} else if (c == '%') {
					c = getChar(false);
					if (c == '>') {
						expandDefinition("%>", "}", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else if (c == ':') {
						expandDefinition("%:", "#", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
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
        
        	if (scannerData.getContextStack().getCurrentContext().undoStackSize() != 0 ) {
        		c = scannerData.getContextStack().getCurrentContext().popUndo();
        	} else {
        		try {
        			c = scannerData.getContextStack().getCurrentContext().read();
        			if (c == NOCHAR) {
        				if (scannerData.getContextStack().rollbackContext(scannerData.getClientRequestor()) == false) {
        					c = NOCHAR;
        					break;
        				} else {
        					done = false;
        				}
        			}
        		} catch (IOException e) {
        			if (scannerData.getContextStack().rollbackContext(scannerData.getClientRequestor()) == false) {
        				c = NOCHAR;
        			} else {
        				done = false;
        			}
        		}
        	}
        } while (!done);
        return c;
    }

	void ungetChar(int c) throws ScannerException{
		scannerData.getContextStack().getCurrentContext().pushUndo(c);
		try
		{
			scannerData.getContextStack().undoRollback( lastContext, scannerData.getClientRequestor() );
		}
		catch (ContextException e)
		{
			handleProblem( e.getId(), scannerData.getContextStack().getCurrentContext().getFilename(), getCurrentOffset(), false, true );
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
		while( scannerData.getContextStack().getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			getChar();
	}

	public IToken nextToken() throws ScannerException, EndOfFileException {
		return nextToken( true ); 
	}
	
	public boolean pasteIntoInputStream(StringBuffer buff) throws ScannerException, EndOfFileException
	{
		// we have found ## in the input stream -- so save the results
		if( lookAheadForTokenPasting() )
		{
			if( storageBuffer == null )
				storageBuffer = buff;
			else
				storageBuffer.append( buff.toString() ); 
			return true;
		}

		// a previous call has stored information, so we will add to it
		if( storageBuffer != null )
		{
			storageBuffer.append( buff.toString() );
			try
			{
				scannerData.getContextStack().updateContext( new StringReader( storageBuffer.toString()), PASTING, IScannerContext.ContextKind.MACROEXPANSION, null, scannerData.getClientRequestor() );
			}
			catch (ContextException e)
			{
				handleProblem( e.getId(), scannerData.getContextStack().getCurrentContext().getFilename(), getCurrentOffset(), false, true  );
			}
			storageBuffer = null; 
			return true;
		}
	
		// there is no need to save the results -- we will not concatenate
		return false;
	}
	
	public int consumeNewlineAfterSlash() throws ScannerException
	{
		int c;
		c = getChar(false);
		if (c == '\r') 
		{
			c = getChar(false);
			if (c == '\n')
			{
				// consume \ \r \n and then continue
				return getChar(true);
			}
			else
			{
				// consume the \ \r and then continue
				return c;
			}
		}
		
		if (c == '\n')
		{
			// consume \ \n and then continue
			return getChar(true);
		} 
 
		// '\' is not the last character on the line
		ungetChar(c);
		return '\\';	
	}
	
	public IToken processStringLiteral(boolean wideLiteral) throws ScannerException, EndOfFileException
	{
		int beginOffset = getCurrentOffset();
		StringBuffer buff = new StringBuffer(); 
		int beforePrevious = NOCHAR;
		int previous = '"';
		int c = getChar(true);

		for( ; ; ) {
			if (c == '\\') 
				c = consumeNewlineAfterSlash();

			
			if ( ( c == '"' ) && ( previous != '\\' || beforePrevious == '\\') ) break;
			if ( ( c == NOCHAR ) || (( c == '\n' ) && ( previous != '\\' || beforePrevious == '\\')) )
			{
				// TODO : we could probably return the partial string -- it might cause 
				// the parse to get by...
				
				handleProblem( IProblem.SCANNER_UNBOUNDED_STRING, null, beginOffset, false, true );
				return null;
			}

			buff.append((char) c);
			beforePrevious = previous;
			previous = c;
			c = getChar(true);
		}

		int type = wideLiteral ? IToken.tLSTRING : IToken.tSTRING;
							
		//If the next token is going to be a string as well, we need to concatenate
		//it with this token.  This will be recursive for as many strings as need to be concatenated
		
		IToken returnToken = newToken( type, buff.toString(), scannerData.getContextStack().getCurrentContext());
			
		IToken next = null;
		try{
			next = nextToken( true );
			if ( next != null && 
					(next.getType() == IToken.tSTRING || 
				     next.getType() == IToken.tLSTRING ))  {
				StringBuffer buffer = new StringBuffer( returnToken.getImage() );
				buffer.append( next.getImage() );
				returnToken.setImage( buffer.toString() ); 
			}	
			else
				cachedToken = next;
		} catch( EndOfFileException e ){ 
			next = null;
		}
		
		currentToken = returnToken;
		returnToken.setNext( null );									
		return returnToken; 		
	}
	public IToken processNumber(int c, boolean pasting) throws ScannerException, EndOfFileException
	{
		// pasting happens when a macro appears in the middle of a number
		// we will "store" the first part of the number in the "pasting" buffer
		// until we have the full monty to evaluate
		// for example 
		// #define F1 3
		// #define F2 F1##F1
		// int x = F2;

		int beginOffset = getCurrentOffset();
		StringBuffer buff = new StringBuffer();
		
		boolean hex = false;
		boolean floatingPoint = ( c == '.' ) ? true : false;
		boolean firstCharZero = ( c== '0' )? true : false; 
			
		buff.append((char) c);

		c = getChar();
		
		if( ! firstCharZero && floatingPoint && !(c >= '0' && c <= '9') ){
			//if pasting, there could actually be a float here instead of just a .
			if( buff.toString().equals( "." ) ){ //$NON-NLS-1$
				if( c == '*' ){
					return newConstantToken( IToken.tDOTSTAR );
				} else if( c == '.' ){
					if( getChar() == '.' )
						return newConstantToken( IToken.tELLIPSIS );
					else
						handleProblem( IProblem.SCANNER_BAD_FLOATING_POINT, null, beginOffset, false, true );				
				} else {
					ungetChar( c );
					return newConstantToken( IToken.tDOT ); 
				}
			}
		} else if (c == 'x') {
			if( ! firstCharZero ) 
			{
				handleProblem( IProblem.SCANNER_BAD_HEX_FORMAT, null, beginOffset, false, true );
				return null;
//				c = getChar(); 
//				continue;
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
		
		if( pasting && pasteIntoInputStream(buff))
			return null;
		
		int tokenType;
		String result = buff.toString(); 
		
		if( floatingPoint && result.equals(".") ) //$NON-NLS-1$
			tokenType = IToken.tDOT;
		else
			tokenType = floatingPoint ? IToken.tFLOATINGPT : IToken.tINTEGER; 
		
		return newToken(
			tokenType,
			result,
			scannerData.getContextStack().getCurrentContext());
	}
	public IToken processPreprocessor() throws ScannerException, EndOfFileException
	{
		int c;
		int beginningOffset = scannerData.getContextStack().getCurrentContext().getOffset() - 1;
		int beginningLine = scannerData.getContextStack().getCurrentLineNumber();
		// lets prepare for a preprocessor statement
		StringBuffer buff = new StringBuffer();
		buff.append('#');

		// we are allowed arbitrary whitespace after the '#' and before the rest of the text
		boolean skipped = skipOverWhitespace();

		c = getChar();
		
		if( c == '#' )
		{
			if( skipped )
				handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#  #", beginningOffset, false, true );  //$NON-NLS-1$
			else 
				return newConstantToken( tPOUNDPOUND ); //$NON-NLS-1$
		} else if( tokenizingMacroReplacementList ) {
			ungetChar( c ); 
			return newConstantToken( tPOUND ); //$NON-NLS-1$
		}
		
		while (((c >= 'a') && (c <= 'z'))
			|| ((c >= 'A') && (c <= 'Z')) || (c == '_') ) {
			buff.append((char) c);
			c = getChar();
		}
		
		ungetChar(c);

		String token = buff.toString();

		if( isLimitReached() )
			handleCompletionOnPreprocessorDirective(token);
		
		Object directive = ppDirectives.get(token);
		if (directive == null) {
			if( scannerExtension.canHandlePreprocessorDirective( token ) )
				scannerExtension.handlePreprocessorDirective( token, getRestOfPreprocessorLine() );
			StringBuffer buffer = new StringBuffer( "#"); //$NON-NLS-1$
			buffer.append( token );
			handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, buffer.toString(), beginningOffset, false, true );
			return null;
		}

		int type = ((Integer) directive).intValue();
		switch (type) {
			case PreprocessorDirectives.DEFINE :
				if ( ! passOnToClient ) {
					skipOverTextUntilNewline();
					if( isLimitReached() )
						handleInvalidCompletion();
					return null;
				}
				poundDefine(beginningOffset, beginningLine);
				return null;

			case PreprocessorDirectives.INCLUDE :
				if (! passOnToClient ) {
					skipOverTextUntilNewline();
					if( isLimitReached() )
						handleInvalidCompletion();
					return null;
				}
				poundInclude( beginningOffset, beginningLine );
				return null;
				
			case PreprocessorDirectives.UNDEFINE :
				if (! passOnToClient) {
					
					skipOverTextUntilNewline();
					if( isLimitReached() )
						handleInvalidCompletion();
					return null;
				}
				skipOverWhitespace();
				removeSymbol(getNextIdentifier());
				skipOverTextUntilNewline();
				return null;
				
			case PreprocessorDirectives.IF :
				//TODO add in content assist stuff here
				// get the rest of the line		
				int currentOffset = getCurrentOffset();
				String expression = getRestOfPreprocessorLine();

				
				if( isLimitReached() )
					handleCompletionOnExpression( expression );
				
				if (expression.trim().equals("")) //$NON-NLS-1$
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#if", beginningOffset, false, true  ); //$NON-NLS-1$
				
				boolean expressionEvalResult = false;
				
				if( scannerData.getBranchTracker().queryCurrentBranchForIf() )
				    expressionEvalResult = evaluateExpression(expression, currentOffset);
				
				passOnToClient = scannerData.getBranchTracker().poundIf( expressionEvalResult ); 
				return null;

			case PreprocessorDirectives.IFDEF :
				//TODO add in content assist stuff here
				skipOverWhitespace();
				
				String definition = getNextIdentifier();
				if( isLimitReached() )
					handleCompletionOnDefinition( definition );
					
				if (getDefinition(definition) == null) {
					// not defined	
					passOnToClient = scannerData.getBranchTracker().poundIf( false );
					skipOverTextUntilNewline();
				} else 
					// continue along, act like nothing is wrong :-)
					passOnToClient = scannerData.getBranchTracker().poundIf( true ); 
				return null;
				
			case PreprocessorDirectives.ENDIF :
				String restOfLine = getRestOfPreprocessorLine().trim();
				if( isLimitReached() )
					handleInvalidCompletion();
				
				if( ! restOfLine.equals( "" )  ) //$NON-NLS-1$
				{	
					StringBuffer buffer = new StringBuffer("#endif "); //$NON-NLS-1$
					buffer.append( restOfLine );
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, buffer.toString(), beginningOffset, false, true );
				}
				passOnToClient = scannerData.getBranchTracker().poundEndif(); 
				return null;
				
			case PreprocessorDirectives.IFNDEF :
				//TODO add in content assist stuff here
				skipOverWhitespace();
				
				String definition2 = getNextIdentifier();
				if( isLimitReached() )
					handleCompletionOnDefinition( definition2 );
				
				if (getDefinition(definition2) != null) {
					// not defined	
					skipOverTextUntilNewline();
					passOnToClient = scannerData.getBranchTracker().poundIf( false );
					if( isLimitReached() )
						handleInvalidCompletion();
					
				} else
					// continue along, act like nothing is wrong :-)
					passOnToClient = scannerData.getBranchTracker().poundIf( true ); 		
				return null;

			case PreprocessorDirectives.ELSE :
				try
				{
					passOnToClient = scannerData.getBranchTracker().poundElse();
				}
				catch( EmptyStackException ese )
				{
					handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, 
						token, 
						beginningOffset, 
						false, true );  
				}

				skipOverTextUntilNewline();
				if( isLimitReached() )
					handleInvalidCompletion();
				return null;

			case PreprocessorDirectives.ELIF :
				//TODO add in content assist stuff here
				int co = getCurrentOffset();
				String elifExpression = getRestOfPreprocessorLine();
				if( isLimitReached() )
					handleCompletionOnExpression( elifExpression );
				
				
				if (elifExpression.equals("")) //$NON-NLS-1$
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, "#elif", beginningOffset, false, true  ); //$NON-NLS-1$

				boolean elsifResult = false;
				if( scannerData.getBranchTracker().queryCurrentBranchForElif() )
					elsifResult = evaluateExpression(elifExpression, co );

				try
				{
					passOnToClient = scannerData.getBranchTracker().poundElif( elsifResult );
				}
				catch( EmptyStackException ese )
				{
					StringBuffer buffer = new StringBuffer( token );
					buffer.append( ' ' );
					buffer.append( elifExpression );
					handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, 
						buffer.toString(), 
						beginningOffset, 
						false, true );  
				}
				return null;

			case PreprocessorDirectives.LINE :
				skipOverTextUntilNewline();
				if( isLimitReached() )
					handleInvalidCompletion();
				return null;
				
			case PreprocessorDirectives.ERROR :
				if (! passOnToClient) {
					skipOverTextUntilNewline();
					if( isLimitReached() )
						handleInvalidCompletion();	
					return null;
				}
				handleProblem( IProblem.PREPROCESSOR_POUND_ERROR, getRestOfPreprocessorLine(), beginningOffset, false, true );
				return null;
				
			case PreprocessorDirectives.PRAGMA :
				skipOverTextUntilNewline();
				if( isLimitReached() )
					handleInvalidCompletion();
				return null;
				
			case PreprocessorDirectives.BLANK :
				String remainderOfLine =
					getRestOfPreprocessorLine().trim();
				if (!remainderOfLine.equals("")) { //$NON-NLS-1$
					StringBuffer buffer = new StringBuffer( "# "); //$NON-NLS-1$
					buffer.append( remainderOfLine );
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, buffer.toString(), beginningOffset, false, true);
				}
				return null;
				
			default :
				StringBuffer buffer = new StringBuffer( "# "); //$NON-NLS-1$
				buffer.append( token );
				handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, buffer.toString(), beginningOffset, false, true );
				return null;
		}
	}
	
	public IToken processKeywordOrLiteral(int c, boolean pasting) throws ScannerException, EndOfFileException
	{       
        int baseOffset = lastContext.getOffset() - lastContext.undoStackSize() - 1;
				
		// String buffer is slow, we need a better way such as memory mapped files
		StringBuffer buff = new StringBuffer(); 
		buff.append((char) c);

		c = getChar();				
		
		while (
				Character.isUnicodeIdentifierPart( (char)c)
//			((c >= 'a') && (c <= 'z'))
//			|| ((c >= 'A') && (c <= 'Z'))
//			|| ((c >= '0') && (c <= '9'))
//			|| (c == '_')
			) {
			buff.append((char) c);
			c = getChar();
			if (c == '\\') {
				c = consumeNewlineAfterSlash();

			}
		}

		ungetChar(c);

		String ident = buff. toString();

		if (ident.equals(DEFINED))
			return newToken(IToken.tINTEGER, handleDefinedMacro());
		
		if( ident.equals(_PRAGMA) && scannerData.getLanguage() == ParserLanguage.C )
		{
			handlePragmaOperator(); 
			return null;
		}
			
		IMacroDescriptor mapping = getDefinition(ident);

		if (mapping != null) {
			StringBuffer buffer = new StringBuffer(POUND_DEFINE); 
			buffer.append( ident );
			if( scannerData.getContextStack().shouldExpandDefinition( buffer.toString()  ) ) {					
				expandDefinition(ident, mapping, baseOffset);
				return null;
			}
		}

		if( pasting && pasteIntoInputStream(buff)){
			return null;
		}
		
		Object tokenTypeObject;
		if( scannerData.getLanguage() == ParserLanguage.CPP )
		 	tokenTypeObject = cppKeywords.get(ident);
		else
			tokenTypeObject = cKeywords.get(ident);

		if (tokenTypeObject != null)
			return newConstantToken(((Integer) tokenTypeObject).intValue());
		else
			return newToken(IToken.tIDENTIFIER, ident, scannerData.getContextStack().getCurrentContext());
	}
	
	public IToken nextToken( boolean pasting ) throws ScannerException, EndOfFileException 
	{
		if( ! initialContextInitialized )
			setupInitialContext();
		
		if( cachedToken != null ){
			setCurrentToken( cachedToken );
			cachedToken = null;
			return currentToken;	
		}
		
		IToken token;
		count++;
		
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
				
				if( c == NOCHAR )
				{
					if( isLimitReached() )
						handleInvalidCompletion();
					continue;
				}
			}

			switch (c) {
				case ' ' :
				case '\r' :
				case '\t' :
				case '\n' :
					c = getChar();
					continue;
				case ':' :
					c = getChar();
					switch (c) {
						case ':' : return newConstantToken(IToken.tCOLONCOLON);
						// Diagraph
						case '>' :  return newConstantToken(IToken.tRBRACKET);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tCOLON);
					}
				case ';' : return newConstantToken(IToken.tSEMI); 
				case ',' : return newConstantToken(IToken.tCOMMA); 
				case '?' : 
					c = getChar();
					if (c == '?')
					{
						// trigraph
						c = getChar();
						switch (c) {
							case '=':
								// this is the same as the # case
								token = processPreprocessor();
								if (token == null) 
								{
									c = getChar();
									continue;
								}
								return token;

//							case '/':
//								expandDefinition("??/", "\\", baseOffset); //$NON-NLS-1$ //$NON-NLS-2$
//								c = getChar(insideString);
//								break;
							default:
								// Not a trigraph
								ungetChar(c);
								ungetChar('?');
								return newConstantToken(IToken.tQUESTION); 
						}
					} 

					ungetChar(c);
					return newConstantToken(IToken.tQUESTION); 
					
				case '(' : return newConstantToken(IToken.tLPAREN); 
				case ')' : return newConstantToken(IToken.tRPAREN); 
				case '[' : return newConstantToken(IToken.tLBRACKET); 
				case ']' : return newConstantToken(IToken.tRBRACKET); 
				case '{' : return newConstantToken(IToken.tLBRACE); 
				case '}' : return newConstantToken(IToken.tRBRACE); 
				case '+' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tPLUSASSIGN);
						case '+' : return newConstantToken(IToken.tINCR);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tPLUS);
					}
				case '-' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tMINUSASSIGN);
						case '-' : return newConstantToken(IToken.tDECR);
						case '>' :
							c = getChar();
							switch (c) {
								case '*' : return newConstantToken(IToken.tARROWSTAR);
								default :
									ungetChar(c);
									return newConstantToken(IToken.tARROW);
							}
						default :
							ungetChar(c);
							return newConstantToken(IToken.tMINUS);
					}
				case '*' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tSTARASSIGN);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tSTAR);
					}
				case '%' :
					c = getChar();
					switch (c) {			
						case '=' : return newConstantToken(IToken.tMODASSIGN);
						
						// Diagraph
						case '>' : return newConstantToken(IToken.tRBRACE); 
						case ':' :
							// this is the same as the # case
							token = processPreprocessor();
							if (token == null) 
							{
								c = getChar();
								continue;
							}
							return token;
						default :
							ungetChar(c);
							return newConstantToken(IToken.tMOD);
					}
				case '^' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tXORASSIGN);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tXOR);
					}
				case '&' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tAMPERASSIGN);
						case '&' : return newConstantToken(IToken.tAND);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tAMPER);
					}
				case '|' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tBITORASSIGN);
						case '|' : return newConstantToken(IToken.tOR);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tBITOR);
					}
				case '~' : return newConstantToken(IToken.tCOMPL);
				case '!' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tNOTEQUAL);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tNOT);
					}
				case '=' :
					c = getChar();
					switch (c) {
						case '=' : return newConstantToken(IToken.tEQUAL);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tASSIGN);
					}
				case '<' :					
					c = getChar();
					switch (c) {
						case '<' :
							c = getChar();
							switch (c) {
								case '=' : return newConstantToken(IToken.tSHIFTLASSIGN);
								default :
									ungetChar(c);
									return newConstantToken(IToken.tSHIFTL);
							}
						case '=' : return newConstantToken(IToken.tLTEQUAL);
						
						// Diagraphs
						case '%' : return newConstantToken(IToken.tLBRACE);
						case ':' : return newConstantToken(IToken.tLBRACKET); 
								
						default :
							ungetChar(c);
							if( forInclusion )
								temporarilyReplaceDefinitionsMap();
							return newConstantToken(IToken.tLT);
					}
				case '>' :
					c = getChar();
					switch (c) {
						case '>' :
							c = getChar();
							switch (c) {
								case '=' : return newConstantToken(IToken.tSHIFTRASSIGN);
								default :
									ungetChar(c);
									return newConstantToken(IToken.tSHIFTR);
							}
						case '=' : return newConstantToken(IToken.tGTEQUAL);
						default :
							ungetChar(c);
							if( forInclusion )
								restoreDefinitionsMap();
							return newConstantToken(IToken.tGT);
					}
				case '.' :
					c = getChar();
					switch (c) {
						case '.' :
							c = getChar();
							switch (c) {
								case '.' : return newConstantToken(IToken.tELLIPSIS);
								default :
									// TODO : there is something missing here!
									break;
							}
							break;
						case '*' : return newConstantToken(IToken.tDOTSTAR);
						case '0' :	
						case '1' :	
						case '2' :	
						case '3' :	
						case '4' :	
						case '5' :	
						case '6' :	
						case '7' :	
						case '8' :	
						case '9' :	
							ungetChar(c);
							return processNumber('.', pasting);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tDOT);
					}
					break;
					
//				The logic around the escape \ is fuzzy.   There is code in getChar(boolean) and
//					in consumeNewLineAfterSlash().  It currently works, but is fragile.
//				case '\\' :
//					c = consumeNewlineAfterSlash();
//					
//					// if we are left with the \ we can skip it.
//					if (c == '\\')
//						c = getChar();
//					continue;
					
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
						case '=' : return newConstantToken(IToken.tDIVASSIGN);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tDIV);
					}
				case '0' :	
				case '1' :	
				case '2' :	
				case '3' :	
				case '4' :	
				case '5' :	
				case '6' :	
				case '7' :	
				case '8' :	
				case '9' :	
					token = processNumber(c, pasting);
					if (token == null)
					{
						c = getChar();
						continue;
					}
					return token;
						
				case 'L' :
					// check for wide literal
					c = getChar(); 
					if (c == '"')
						token = processStringLiteral(true);
					else if (c == '\'')
						return processCharacterLiteral( c, true );
					else
					{
						// This is not a wide literal -- it must be a token or keyword
						ungetChar(c);
						token = processKeywordOrLiteral('L', pasting);
					}
					if (token == null) 
					{
						c = getChar();
						continue;
					}
					return token;
				case '"' :
					token = processStringLiteral(false);
					if (token == null) 
					{
						c = getChar();
						continue;
					}
					return token;
				case '\'' : return processCharacterLiteral( c, false );
				case '#':
					// This is a special case -- the preprocessor is integrated into 
					// the scanner.  If we get a null token, it means that everything
					// was handled correctly and we can go on to the next characgter.
					
					token = processPreprocessor();
					if (token == null) 
					{
						c = getChar();
						continue;
					}
					return token;
						
				default:
					if (
							// JOHNC - Accept non-ASCII Input
//						((c >= 'a') && (c <= 'z'))
//							|| ((c >= 'A') && (c <= 'Z')) || (c == '_')) {
							Character.isLetter((char)c) || ( c == '_') 
					) 
					{
						token = processKeywordOrLiteral(c, pasting);
						if (token == null) 
						{
							c = getChar();
							continue;
						}
						return token;
					}
					
					handleProblem( IProblem.SCANNER_BAD_CHARACTER, new Character( (char)c ).toString(), getCurrentOffset(), false, true, throwExceptionOnBadCharacterRead ); 
					c = getChar();
					continue;			
			}
		}
		if (( getDepth() != 0) && !atEOF )
		{
			atEOF = true;
			handleProblem( IProblem.SCANNER_UNEXPECTED_EOF, null, getCurrentOffset(), false, true );
		}

		// we're done
		throwEOF(null);
		return null;
	}



    /**
	 * @param definition
	 */
	protected void handleCompletionOnDefinition(String definition) throws EndOfFileException {
		IASTCompletionNode node = new ASTCompletionNode( IASTCompletionNode.CompletionKind.MACRO_REFERENCE, 
				null, null, definition, KeywordSets.getKeywords(KeywordSets.Key.EMPTY, scannerData.getLanguage()) );
		
		throwEOF( node ); 
	}

	/**
	 * @param expression2
	 */
	protected void handleCompletionOnExpression(String expression) throws EndOfFileException {
		int completionPoint = expression.length() + 2;
		IASTCompletionNode.CompletionKind kind = IASTCompletionNode.CompletionKind.MACRO_REFERENCE;
		
		String prefix = ""; //$NON-NLS-1$
		
		if( ! expression.trim().equals("")) //$NON-NLS-1$
		{	
			IScanner subScanner = new Scanner( new StringReader(expression), SCRATCH, EMPTY_MAP, EMPTY_LIST, NULL_REQUESTOR, ParserMode.QUICK_PARSE, scannerData.getLanguage(), NULL_LOG_SERVICE, scannerExtension );
			IToken lastToken = null;
			while( true )
			{	
				try
				{
					lastToken = subScanner.nextToken();
				}
				catch( EndOfFileException eof )
				{
					// ok
					break;
				} catch (ScannerException e) {
					handleInternalError();
					break;
				}
			}
					
			
			if( ( lastToken != null ))
			{
				if( ( lastToken.getType() == IToken.tIDENTIFIER ) 
					&& ( lastToken.getEndOffset() == completionPoint ) )
					prefix = lastToken.getImage();
				else if( ( lastToken.getEndOffset() == completionPoint ) && 
					( lastToken.getType() != IToken.tIDENTIFIER ) )
					kind = IASTCompletionNode.CompletionKind.NO_SUCH_KIND;

					
			}
		}
		
		IASTCompletionNode node = new ASTCompletionNode( kind, 
				null, null, prefix, 
				KeywordSets.getKeywords(((kind == IASTCompletionNode.CompletionKind.NO_SUCH_KIND )? KeywordSets.Key.EMPTY : KeywordSets.Key.MACRO), scannerData.getLanguage()) );
		
		throwEOF( node );
	}

	protected void handleInvalidCompletion() throws EndOfFileException
	{
		throwEOF( new ASTCompletionNode( IASTCompletionNode.CompletionKind.UNREACHABLE_CODE, null, null, "", KeywordSets.getKeywords(KeywordSets.Key.EMPTY, scannerData.getLanguage()) )); //$NON-NLS-1$
	}
	
	protected void handleCompletionOnPreprocessorDirective( String prefix ) throws EndOfFileException 
	{
		throwEOF( new ASTCompletionNode( IASTCompletionNode.CompletionKind.NO_SUCH_KIND, null, null, prefix, KeywordSets.getKeywords(KeywordSets.Key.PP_DIRECTIVE, scannerData.getLanguage() )));
	}
	/**
	 * @param key
	 */
	protected void removeSymbol(String key) {
		scannerData.getDefinitions().remove(key);
	}

	/**
	 * 
	 */
	protected void handlePragmaOperator() throws ScannerException, EndOfFileException
	{
		// until we know what to do with pragmas, do the equivalent as 
		// to what we do for #pragma blah blah blah (ignore it)
		getRestOfPreprocessorLine();
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
        		c = '\'';
			}			
			// exit condition
			if ( ( c =='\'' ) && ( prev != '\\' || prevPrev == '\\' ) ) break;
			
        	buffer.append( (char)c);
        	prevPrev = prev;
        	prev = c;
        	c = getChar(true);
        }
        
        return newToken( type, buffer.toString(), scannerData.getContextStack().getCurrentContext());                      
    }



    protected String getCurrentFile()
	{
		return scannerData.getContextStack().getMostRelevantFileContext() != null ? scannerData.getContextStack().getMostRelevantFileContext().getFilename() : ""; //$NON-NLS-1$
	}


    protected int getCurrentOffset()
    {
        return scannerData.getContextStack().getMostRelevantFileContext() != null ? scannerData.getContextStack().getMostRelevantFileContext().getOffset() : -1;
    }


    protected static class endOfMacroTokenException extends Exception {}
    // the static instance we always use
    protected static endOfMacroTokenException endOfMacroToken = new endOfMacroTokenException();
    
    public IToken nextTokenForStringizing() throws ScannerException, EndOfFileException
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
                    return newToken( IToken.tSTRING, buff.toString(), scannerData.getContextStack().getCurrentContext());
    
                } else {
                	handleProblem( IProblem.SCANNER_UNBOUNDED_STRING, null, beginOffset, false, true );
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
                        return newToken(IToken.tCOMMA, ",", scannerData.getContextStack().getCurrentContext()); //$NON-NLS-1$
                    case '(' :
                        if (tokenImage.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tLPAREN, "(", scannerData.getContextStack().getCurrentContext()); //$NON-NLS-1$
                    case ')' :
                        if (tokenImage.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tRPAREN, ")", scannerData.getContextStack().getCurrentContext()); //$NON-NLS-1$
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
            return newToken(IToken.tIDENTIFIER, tokenImage.toString(), scannerData.getContextStack().getCurrentContext());
        }
        
        // we're done
        throwEOF(null);
        return null;
    }


	/**
	 * 
	 */
	protected void throwEOF(IASTCompletionNode node) throws EndOfFileException, OffsetLimitReachedException {
		if( node == null )
		{	
			if( offsetLimit == NO_OFFSET_LIMIT )
				throw new EndOfFileException();
			
			if( finalToken != null && finalToken.getEndOffset() == offsetLimit )
				throw new OffsetLimitReachedException(finalToken);
			throw new OffsetLimitReachedException( (IToken)null );
		}
		throw new OffsetLimitReachedException( node );
	}


	static {
		cppKeywords.put( Keywords.AND, new Integer(IToken.t_and));
		cppKeywords.put( Keywords.AND_EQ, new Integer(IToken.t_and_eq));
		cppKeywords.put( Keywords.ASM, new Integer(IToken.t_asm));
		cppKeywords.put( Keywords.AUTO, new Integer(IToken.t_auto));
		cppKeywords.put( Keywords.BITAND, new Integer(IToken.t_bitand));
		cppKeywords.put( Keywords.BITOR, new Integer(IToken.t_bitor));
		cppKeywords.put( Keywords.BOOL, new Integer(IToken.t_bool));
		cppKeywords.put( Keywords.BREAK, new Integer(IToken.t_break));
		cppKeywords.put( Keywords.CASE, new Integer(IToken.t_case));
		cppKeywords.put( Keywords.CATCH, new Integer(IToken.t_catch));
		cppKeywords.put( Keywords.CHAR, new Integer(IToken.t_char));
		cppKeywords.put( Keywords.CLASS, new Integer(IToken.t_class));
		cppKeywords.put( Keywords.COMPL, new Integer(IToken.t_compl));
		cppKeywords.put( Keywords.CONST, new Integer(IToken.t_const));
		cppKeywords.put( Keywords.CONST_CAST, new Integer(IToken.t_const_cast));
		cppKeywords.put( Keywords.CONTINUE, new Integer(IToken.t_continue));
		cppKeywords.put( Keywords.DEFAULT, new Integer(IToken.t_default));
		cppKeywords.put( Keywords.DELETE, new Integer(IToken.t_delete));
		cppKeywords.put( Keywords.DO, new Integer(IToken.t_do));
		cppKeywords.put( Keywords.DOUBLE, new Integer(IToken.t_double));
		cppKeywords.put( Keywords.DYNAMIC_CAST, new Integer(IToken.t_dynamic_cast));
		cppKeywords.put( Keywords.ELSE, new Integer(IToken.t_else));
		cppKeywords.put( Keywords.ENUM, new Integer(IToken.t_enum));
		cppKeywords.put( Keywords.EXPLICIT, new Integer(IToken.t_explicit));
		cppKeywords.put( Keywords.EXPORT, new Integer(IToken.t_export));
		cppKeywords.put( Keywords.EXTERN, new Integer(IToken.t_extern));
		cppKeywords.put( Keywords.FALSE, new Integer(IToken.t_false));
		cppKeywords.put( Keywords.FLOAT, new Integer(IToken.t_float));
		cppKeywords.put( Keywords.FOR, new Integer(IToken.t_for));
		cppKeywords.put( Keywords.FRIEND, new Integer(IToken.t_friend));
		cppKeywords.put( Keywords.GOTO, new Integer(IToken.t_goto));
		cppKeywords.put( Keywords.IF, new Integer(IToken.t_if));
		cppKeywords.put( Keywords.INLINE, new Integer(IToken.t_inline));
		cppKeywords.put( Keywords.INT, new Integer(IToken.t_int));
		cppKeywords.put( Keywords.LONG, new Integer(IToken.t_long));
		cppKeywords.put( Keywords.MUTABLE, new Integer(IToken.t_mutable));
		cppKeywords.put( Keywords.NAMESPACE, new Integer(IToken.t_namespace));
		cppKeywords.put( Keywords.NEW, new Integer(IToken.t_new));
		cppKeywords.put( Keywords.NOT, new Integer(IToken.t_not));
		cppKeywords.put( Keywords.NOT_EQ, new Integer(IToken.t_not_eq));
		cppKeywords.put( Keywords.OPERATOR, new Integer(IToken.t_operator));
		cppKeywords.put( Keywords.OR, new Integer(IToken.t_or));
		cppKeywords.put( Keywords.OR_EQ, new Integer(IToken.t_or_eq));
		cppKeywords.put( Keywords.PRIVATE, new Integer(IToken.t_private));
		cppKeywords.put( Keywords.PROTECTED, new Integer(IToken.t_protected));
		cppKeywords.put( Keywords.PUBLIC, new Integer(IToken.t_public));
		cppKeywords.put( Keywords.REGISTER, new Integer(IToken.t_register));
		cppKeywords.put( Keywords.REINTERPRET_CAST, new Integer(IToken.t_reinterpret_cast));
		cppKeywords.put( Keywords.RETURN, new Integer(IToken.t_return));
		cppKeywords.put( Keywords.SHORT, new Integer(IToken.t_short));
		cppKeywords.put( Keywords.SIGNED, new Integer(IToken.t_signed));
		cppKeywords.put( Keywords.SIZEOF, new Integer(IToken.t_sizeof));
		cppKeywords.put( Keywords.STATIC, new Integer(IToken.t_static));
		cppKeywords.put( Keywords.STATIC_CAST, new Integer(IToken.t_static_cast));
		cppKeywords.put( Keywords.STRUCT, new Integer(IToken.t_struct));
		cppKeywords.put( Keywords.SWITCH, new Integer(IToken.t_switch));
		cppKeywords.put( Keywords.TEMPLATE, new Integer(IToken.t_template));
		cppKeywords.put( Keywords.THIS, new Integer(IToken.t_this));
		cppKeywords.put( Keywords.THROW, new Integer(IToken.t_throw));
		cppKeywords.put( Keywords.TRUE, new Integer(IToken.t_true));
		cppKeywords.put( Keywords.TRY, new Integer(IToken.t_try));
		cppKeywords.put( Keywords.TYPEDEF, new Integer(IToken.t_typedef));
		cppKeywords.put( Keywords.TYPEID, new Integer(IToken.t_typeid));
		cppKeywords.put( Keywords.TYPENAME, new Integer(IToken.t_typename));
		cppKeywords.put( Keywords.UNION, new Integer(IToken.t_union));
		cppKeywords.put( Keywords.UNSIGNED, new Integer(IToken.t_unsigned));
		cppKeywords.put( Keywords.USING, new Integer(IToken.t_using));
		cppKeywords.put( Keywords.VIRTUAL, new Integer(IToken.t_virtual));
		cppKeywords.put( Keywords.VOID, new Integer(IToken.t_void));
		cppKeywords.put( Keywords.VOLATILE, new Integer(IToken.t_volatile));
		cppKeywords.put( Keywords.WCHAR_T, new Integer(IToken.t_wchar_t));
		cppKeywords.put( Keywords.WHILE, new Integer(IToken.t_while));
		cppKeywords.put( Keywords.XOR, new Integer(IToken.t_xor));
		cppKeywords.put( Keywords.XOR_EQ, new Integer(IToken.t_xor_eq));

		ppDirectives.put(Directives.POUND_DEFINE, new Integer(PreprocessorDirectives.DEFINE));
		ppDirectives.put(Directives.POUND_UNDEF,new Integer(PreprocessorDirectives.UNDEFINE));
		ppDirectives.put(Directives.POUND_IF, new Integer(PreprocessorDirectives.IF));
		ppDirectives.put(Directives.POUND_IFDEF, new Integer(PreprocessorDirectives.IFDEF));
		ppDirectives.put(Directives.POUND_IFNDEF, new Integer(PreprocessorDirectives.IFNDEF));
		ppDirectives.put(Directives.POUND_ELSE, new Integer(PreprocessorDirectives.ELSE));
		ppDirectives.put(Directives.POUND_ENDIF, new Integer(PreprocessorDirectives.ENDIF));
		ppDirectives.put(Directives.POUND_INCLUDE, new Integer(PreprocessorDirectives.INCLUDE));
		ppDirectives.put(Directives.POUND_LINE, new Integer(PreprocessorDirectives.LINE));
		ppDirectives.put(Directives.POUND_ERROR, new Integer(PreprocessorDirectives.ERROR));
		ppDirectives.put(Directives.POUND_PRAGMA, new Integer(PreprocessorDirectives.PRAGMA));
		ppDirectives.put(Directives.POUND_ELIF, new Integer(PreprocessorDirectives.ELIF));
		ppDirectives.put(Directives.POUND_BLANK, new Integer(PreprocessorDirectives.BLANK));

		cKeywords.put( Keywords.AUTO, new Integer(IToken.t_auto));
		cKeywords.put( Keywords.BREAK, new Integer(IToken.t_break));
		cKeywords.put( Keywords.CASE, new Integer(IToken.t_case));
		cKeywords.put( Keywords.CHAR, new Integer(IToken.t_char));
		cKeywords.put( Keywords.CONST, new Integer(IToken.t_const));
		cKeywords.put( Keywords.CONTINUE, new Integer(IToken.t_continue));
		cKeywords.put( Keywords.DEFAULT, new Integer(IToken.t_default));
		cKeywords.put( Keywords.DELETE, new Integer(IToken.t_delete));
		cKeywords.put( Keywords.DO, new Integer(IToken.t_do));
		cKeywords.put( Keywords.DOUBLE, new Integer(IToken.t_double));
		cKeywords.put( Keywords.ELSE, new Integer(IToken.t_else));
		cKeywords.put( Keywords.ENUM, new Integer(IToken.t_enum));
		cKeywords.put( Keywords.EXTERN, new Integer(IToken.t_extern));
		cKeywords.put( Keywords.FLOAT, new Integer(IToken.t_float));
		cKeywords.put( Keywords.FOR, new Integer(IToken.t_for));
		cKeywords.put( Keywords.GOTO, new Integer(IToken.t_goto));
		cKeywords.put( Keywords.IF, new Integer(IToken.t_if));
		cKeywords.put( Keywords.INLINE, new Integer(IToken.t_inline));
		cKeywords.put( Keywords.INT, new Integer(IToken.t_int));
		cKeywords.put( Keywords.LONG, new Integer(IToken.t_long));
		cKeywords.put( Keywords.REGISTER, new Integer(IToken.t_register));
		cKeywords.put( Keywords.RESTRICT, new Integer(IToken.t_restrict));
		cKeywords.put( Keywords.RETURN, new Integer(IToken.t_return));
		cKeywords.put( Keywords.SHORT, new Integer(IToken.t_short));
		cKeywords.put( Keywords.SIGNED, new Integer(IToken.t_signed));
		cKeywords.put( Keywords.SIZEOF, new Integer(IToken.t_sizeof));
		cKeywords.put( Keywords.STATIC, new Integer(IToken.t_static));
		cKeywords.put( Keywords.STRUCT, new Integer(IToken.t_struct));
		cKeywords.put( Keywords.SWITCH, new Integer(IToken.t_switch));
		cKeywords.put( Keywords.TYPEDEF, new Integer(IToken.t_typedef));
		cKeywords.put( Keywords.UNION, new Integer(IToken.t_union));
		cKeywords.put( Keywords.UNSIGNED, new Integer(IToken.t_unsigned));
		cKeywords.put( Keywords.VOID, new Integer(IToken.t_void));
		cKeywords.put( Keywords.VOLATILE, new Integer(IToken.t_volatile));
		cKeywords.put( Keywords.WHILE, new Integer(IToken.t_while));
		cKeywords.put( Keywords._BOOL, new Integer(IToken.t__Bool));
		cKeywords.put( Keywords._COMPLEX, new Integer(IToken.t__Complex));
		cKeywords.put( Keywords._IMAGINARY, new Integer(IToken.t__Imaginary));

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
		return scannerData.getBranchTracker().getDepth(); 
	}

	protected boolean evaluateExpression(String expression, int beginningOffset )
		throws ScannerException {
			
		if( scannerData.getParserMode() == ParserMode.QUICK_PARSE )
		{
			if( expression.trim().equals( "0" ) ) //$NON-NLS-1$
				return false; 
			
			return true; 
		}
		else
		{	
			IExpressionParser parser = null;
			StringBuffer expressionBuffer = new StringBuffer( expression );
			expressionBuffer.append( ';');
		   
			IScanner trial = new Scanner( 
					new StringReader(expressionBuffer.toString()), 
					EXPRESSION, 
					scannerData.getDefinitions(), 
					scannerData.getIncludePathNames(),					
					NULL_REQUESTOR,
					ParserMode.QUICK_PARSE, 
					scannerData.getLanguage(),  
					NULL_LOG_SERVICE,
					scannerExtension );
			
	        parser = InternalParserUtil.createExpressionParser(trial, scannerData.getLanguage(), NULL_LOG_SERVICE);
			try {
				IASTExpression exp = parser.expression(null);
				if( exp.evaluateExpression() == 0 )
					return false;
				return true;
			} catch( BacktrackException backtrack  )
			{
				handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true ); 
			}
			catch (ASTExpressionEvaluationException e) {
				handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true );
			} catch (EndOfFileException e) {
				handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true );
			}
			return true; 
		}
	}

	
	protected void skipOverSinglelineComment() throws ScannerException, EndOfFileException {
		
		StringBuffer comment = new StringBuffer("//"); //$NON-NLS-1$
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
		if( c== NOCHAR && isLimitReached() )
			handleInvalidCompletion();
		
	}

	protected boolean skipOverMultilineComment() throws ScannerException, EndOfFileException {
		int state = 0;
		boolean encounteredNewline = false;
		StringBuffer comment = new StringBuffer("/*"); //$NON-NLS-1$
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

		if (c == NOCHAR && !isLimitReached() )
			handleProblem( IProblem.SCANNER_UNEXPECTED_EOF, null, getCurrentOffset(), false, true  );
		else if( c== NOCHAR ) // limit reached
			handleInvalidCompletion();
		
		ungetChar(c);

		return encounteredNewline;
	}

	protected void poundInclude( int beginningOffset, int startLine ) throws ScannerException, EndOfFileException {
		StringBuffer potentialErrorLine = new StringBuffer( "#include "); //$NON-NLS-1$
		skipOverWhitespace();				
		int baseOffset = lastContext.getOffset() - lastContext.undoStackSize();
		int nameLine = scannerData.getContextStack().getCurrentLineNumber();
		String includeLine = getRestOfPreprocessorLine();
		int endLine = scannerData.getContextStack().getCurrentLineNumber();
		potentialErrorLine.append( includeLine );
		
		ScannerUtility.InclusionDirective directive = null;
		try
		{
			directive = ScannerUtility.parseInclusionDirective( scannerData, scannerExtension, includeLine, baseOffset );
		}
		catch( ScannerUtility.InclusionParseException ipe )
		{
			handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, potentialErrorLine.toString(), beginningOffset, false, true );
			return;
		}
		
		if( scannerData.getParserMode() == ParserMode.QUICK_PARSE )
		{ 
			if( scannerData.getClientRequestor() != null )
			{
				IASTInclusion i = null;
                try
                {
                    i =
                    	scannerData.getASTFactory().createInclusion(
                            directive.getFilename(),
                            "", //$NON-NLS-1$
                            !directive.useIncludePaths(),
                            beginningOffset,
                            startLine,
                            directive.getStartOffset(),
                            directive.getStartOffset() + directive.getFilename().length(), nameLine, directive.getEndOffset(), endLine);
                }
                catch (Exception e)
                {
                    /* do nothing */
                }
                if( i != null )
                {
					i.enterScope( scannerData.getClientRequestor() );
					i.exitScope( scannerData.getClientRequestor() );
                }					 
			}
		}
		else
			handleInclusion(directive.getFilename().trim(), directive.useIncludePaths(), beginningOffset, startLine, directive.getStartOffset(), nameLine, directive.getEndOffset(), endLine); 
	}

	protected static final Hashtable EMPTY_MAP = new Hashtable();
	protected static final List EMPTY_LIST = new ArrayList();
	protected Map definitionsBackupMap = null; 
	
	protected void temporarilyReplaceDefinitionsMap()
	{
		definitionsBackupMap = scannerData.getDefinitions();
		scannerData.setDefinitions( EMPTY_MAP );
	}
	
	protected void restoreDefinitionsMap()
	{
		scannerData.setDefinitions( definitionsBackupMap );
		definitionsBackupMap = null;
	}


	protected boolean forInclusion = false;
	private final static IParserLogService NULL_LOG_SERVICE = new NullLogService();
	private static final String [] STRING_ARRAY = new String[0];
	/**
	 * @param b
	 */
	protected void setForInclusion(boolean b)
	{
		forInclusion = b;
	}

	protected List tokenizeReplacementString( int beginning, String key, String replacementString, List parameterIdentifiers ) 
	{
		List macroReplacementTokens = new ArrayList();
		if( replacementString.trim().equals( "" ) )  //$NON-NLS-1$
			return macroReplacementTokens;
		IScanner helperScanner=null;
		try {
			helperScanner = new Scanner( 
						new StringReader(replacementString),
						SCRATCH,
						EMPTY_MAP, EMPTY_LIST, 
						NULL_REQUESTOR, 
						scannerData.getParserMode(),
						scannerData.getLanguage(),
						NULL_LOG_SERVICE, scannerExtension);
		} catch (ParserFactoryError e1) {
		}
		helperScanner.setTokenizingMacroReplacementList( true );
		IToken t = null;
		try {
			t = helperScanner.nextToken(false);
		} catch (ScannerException e) {
		} catch (EndOfFileException e) {
		}
		
		if( t == null )
			return macroReplacementTokens;
		
		try {
			while (true) {
				//each # preprocessing token in the replacement list shall be followed
				//by a parameter as the next reprocessing token in the list
				if( t.getType() == tPOUND ){
					macroReplacementTokens.add( t );
					t = helperScanner.nextToken(false);
					if( parameterIdentifiers != null )
					{	
						int index = parameterIdentifiers.indexOf(t.getImage());
						if (index == -1 ) {
							//not found
							
							if( beginning != NO_OFFSET_LIMIT )
							{	
								StringBuffer buffer = new StringBuffer( POUND_DEFINE );
								buffer.append( key );
								buffer.append( ' ' );
								buffer.append( replacementString );
								handleProblem( IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, buffer.toString(),
										beginning, false, true ); 									
								return null;
							}
						}
					}
				}
				
				macroReplacementTokens.add(t);
				t = helperScanner.nextToken(false);
			}
		}
		catch( EndOfFileException eof )
		{
		}
		catch( ScannerException sc )
		{
		}
		
		return macroReplacementTokens;
	}
	
	
	protected void poundDefine(int beginning, int beginningLine ) throws ScannerException, EndOfFileException {
		StringBuffer potentialErrorMessage = new StringBuffer( POUND_DEFINE );
		skipOverWhitespace();
		// definition 
		String key = getNextIdentifier();
		int offset = scannerData.getContextStack().getCurrentContext().getOffset() - key.length() - scannerData.getContextStack().getCurrentContext().undoStackSize();
		int nameLine = scannerData.getContextStack().getCurrentLineNumber();

		// store the previous definition to check against later
		IMacroDescriptor previousDefinition = getDefinition( key );
		IMacroDescriptor descriptor = null;
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
						potentialErrorMessage.append( buffer );
						potentialErrorMessage.append( '\\');
						potentialErrorMessage.append( (char)c );
						handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, potentialErrorMessage.toString(), beginning, false, true);
						return;
					}
				} else if( c == '\r' || c == '\n' ){
					potentialErrorMessage.append( buffer );
					potentialErrorMessage.append( '\\');
					potentialErrorMessage.append( (char)c );
					handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, potentialErrorMessage.toString(), beginning, false, true );
					return;
				} else if( c == NOCHAR ){
					handleProblem( IProblem.SCANNER_UNEXPECTED_EOF, null, beginning, false, true );
					return;
				}
				
				buffer.append((char) c);
				c = getChar(true);
			}
            
			String parameters = buffer.toString();

			// replace StringTokenizer later -- not performant
			StringTokenizer tokenizer = new StringTokenizer(parameters, ","); //$NON-NLS-1$
			ArrayList parameterIdentifiers =
				new ArrayList(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				parameterIdentifiers.add(tokenizer.nextToken().trim());
			}

			skipOverWhitespace();

			List macroReplacementTokens = null;
			String replacementString = getRestOfPreprocessorLine();
			
			
			macroReplacementTokens = ( ! replacementString.equals( "" ) ) ?  //$NON-NLS-1$
										tokenizeReplacementString( beginning, key, replacementString, parameterIdentifiers ) :
										EMPTY_LIST;
			
			StringBuffer fullSignature = new StringBuffer( POUND_DEFINE);
			fullSignature.append( key );
			fullSignature.append( '(');
			fullSignature.append( parameters );
			fullSignature.append( ") "); //$NON-NLS-1$
			fullSignature.append( replacementString );
			descriptor = new FunctionMacroDescriptor(
				key,
				parameterIdentifiers,
				macroReplacementTokens,
				fullSignature.toString(), 
				replacementString);
				
			checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning);
			addDefinition(key, descriptor);

		}
		else if ((c == '\n') || (c == '\r'))
		{
			checkValidMacroRedefinition(key, previousDefinition, "", beginning);				 //$NON-NLS-1$
			addDefinition( key, "" ); //$NON-NLS-1$
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
				checkValidMacroRedefinition(key, previousDefinition, "", beginning); //$NON-NLS-1$
				addDefinition(key, ""); //$NON-NLS-1$
			} else if (c == '*') // multi-line comment
				{
				if (skipOverMultilineComment()) {
					// we have gone over a newline
					// therefore, this symbol was defined to an empty string
					checkValidMacroRedefinition(key, previousDefinition, "", beginning); //$NON-NLS-1$
					addDefinition(key, ""); //$NON-NLS-1$
				} else {
					String value = getRestOfPreprocessorLine();
					
					checkValidMacroRedefinition(key, previousDefinition, "", beginning); //$NON-NLS-1$
					addDefinition(key, value);
				}
			} else {
				// this is not a comment 
				// it is a bad statement
				potentialErrorMessage.append( key );
				potentialErrorMessage.append( " /"); //$NON-NLS-1$
				potentialErrorMessage.append( getRestOfPreprocessorLine() );
				handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, potentialErrorMessage.toString(), beginning, false, true );
				return;
			}
		} else {
			potentialErrorMessage = new StringBuffer(); 
			potentialErrorMessage.append( "#define"); //$NON-NLS-1$
			potentialErrorMessage.append( key );
			potentialErrorMessage.append( (char)c );
			potentialErrorMessage.append( getRestOfPreprocessorLine() );
			handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, potentialErrorMessage.toString(), beginning, false, true );
			return;
		}
		
		try
        {
			scannerData.getASTFactory().createMacro( key, beginning, beginningLine, offset, offset + key.length(), nameLine, scannerData.getContextStack().getCurrentContext().getOffset(), scannerData.getContextStack().getCurrentLineNumber(), descriptor ).acceptElement( scannerData.getClientRequestor() );
        }
        catch (Exception e)
        {
            /* do nothing */
        } 
	}

	protected void checkValidMacroRedefinition(
			String key,
			IMacroDescriptor previousDefinition,
			String newDefinition, int beginningOffset )
	throws ScannerException 
	{
		StringBuffer buffer = new StringBuffer(key);
		buffer.append( ' ');
		buffer.append(newDefinition);
		IMacroDescriptor newMacro = new ObjectMacroDescriptor( key, buffer.toString(), 
				tokenizeReplacementString( NO_OFFSET_LIMIT, key, newDefinition, null ), newDefinition );
		checkValidMacroRedefinition( key, previousDefinition, newMacro, beginningOffset );
	}
	
	
	protected void checkValidMacroRedefinition(
			String key,
			String previousDefinition,
			String newDefinition, int beginningOffset )
	throws ScannerException 
	{
		StringBuffer oldMacro = new StringBuffer( key );
		oldMacro.append( ' ');
		StringBuffer newMacro = new StringBuffer( oldMacro.toString() );
		oldMacro.append( previousDefinition );
		newMacro.append( newDefinition );
		IMacroDescriptor prevMacroDescriptor = new ObjectMacroDescriptor( key, oldMacro.toString(), 
				tokenizeReplacementString( NO_OFFSET_LIMIT, key, previousDefinition, null ), previousDefinition );
		IMacroDescriptor newMacroDescriptor = new ObjectMacroDescriptor( key, newMacro.toString(), 
				tokenizeReplacementString( NO_OFFSET_LIMIT, key, newDefinition, null ), newDefinition );
		checkValidMacroRedefinition( key, prevMacroDescriptor, newMacroDescriptor, beginningOffset );
	}
	

	protected void checkValidMacroRedefinition(
		String key,
		IMacroDescriptor previousDefinition,
		IMacroDescriptor newDefinition, int beginningOffset )
		throws ScannerException 
		{
			if( scannerData.getParserMode() != ParserMode.QUICK_PARSE && previousDefinition != null ) 
			{
				if( previousDefinition.compatible( newDefinition ) ) 
					return; 							
				
				handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN, key, beginningOffset, false, true );
			}			
	}
    
    /**
	 * 
	 */
	protected void handleInternalError() {
		// TODO Auto-generated method stub
		
	}

	protected Vector getMacroParameters (String params, boolean forStringizing) throws ScannerException {
        
        Scanner tokenizer  = new Scanner(new StringReader(params), TEXT, scannerData.getDefinitions(), scannerData.getIncludePathNames(), NULL_REQUESTOR, scannerData.getParserMode(), scannerData.getLanguage(), NULL_LOG_SERVICE, (IScannerExtension)scannerExtension.clone() );
        tokenizer.setThrowExceptionOnBadCharacterRead(false);
        Vector parameterValues = new Vector();
        Token t = null;
        StringBuffer buffer = new StringBuffer();
        boolean space = false;
        int nParen = 0;
        
        try {
            while (true) {
				int c = tokenizer.getCharacter();
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
                    parameterValues.add(buffer.toString());
                    buffer = new StringBuffer();
                    space = false;
                    continue;
                }

                if (space)
                    buffer.append( ' ' );

                switch (t.type) {
                    case IToken.tSTRING :
                    	buffer.append('\"');
                    	buffer.append(t.getImage());
                    	buffer.append('\"'); 
                    	break;
                    case IToken.tLSTRING :
                    	buffer.append( "L\""); //$NON-NLS-1$
                    	buffer.append(t.getImage());
                    	buffer.append('\"');	
                    	break;
                    case IToken.tCHAR :    
                    	buffer.append('\'');
                    	buffer.append(t.getImage());
                    	buffer.append('\''); 
                    	break;
                    default :             
                    	buffer.append( t.getImage()); 
                    	break;
                }
                space = true;
            }
        }
        catch (EndOfFileException e) {
            // Good
            parameterValues.add(buffer.toString());
        }

        
        return parameterValues;
    }
    
	protected void expandDefinition(String symbol, String expansion, int symbolOffset ) throws ScannerException
	{
		StringBuffer fullSignatureBuffer = new StringBuffer();
		fullSignatureBuffer.append( symbol );
		fullSignatureBuffer.append( ' ');
		fullSignatureBuffer.append( expansion );
		List tokens = tokenizeReplacementString(NO_OFFSET_LIMIT, symbol, expansion, null );
		expandDefinition( symbol, 
				new ObjectMacroDescriptor( 	symbol, 
											fullSignatureBuffer.toString(), 
											tokens, 
											expansion ), 
							symbolOffset);
	}
	
	protected void expandDefinition(String symbol, IMacroDescriptor expansion, int symbolOffset) 
                    throws ScannerException 
    {
        // All the tokens generated by the macro expansion 
        // will have dimensions (offset and length) equal to the expanding symbol.
		if ( expansion.getMacroType() == MacroType.OBJECT_LIKE || expansion.getMacroType() == MacroType.INTERNAL_LIKE ) {
			String replacementValue = expansion.getExpansionSignature();
			try
			{
				StringBuffer buffer = new StringBuffer( POUND_DEFINE );
				buffer.append( symbol );
				scannerData.getContextStack().updateContext( new StringReader(replacementValue), buffer.toString(), ScannerContext.ContextKind.MACROEXPANSION, null, scannerData.getClientRequestor(), symbolOffset, symbol.length());
			}
			catch (ContextException e)
			{
				handleProblem( e.getId(), scannerData.getContextStack().getCurrentContext().getFilename(), getCurrentOffset(), false, true );
				consumeUntilOutOfMacroExpansion();
				return;
			}
		} else if (expansion.getMacroType() == MacroType.FUNCTION_LIKE ) {
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
				List tokens = expansion.getTokenizedExpansion();
				List parameterNames = expansion.getParameters();

				if (parameterNames.size() != parameterValues.size())
				{ 
					handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, symbol, getCurrentOffset(), false, true  );	
					consumeUntilOutOfMacroExpansion();
					return;
				}				

				int numberOfTokens = tokens.size();

				for (int i = 0; i < numberOfTokens; ++i) {
					t = (Token) tokens.get(i);
					if (t.type == IToken.tIDENTIFIER) {

						// is this identifier in the parameterNames
						// list? 
						int index = parameterNames.indexOf(t.getImage());
						if (index == -1 ) {
							// not found
							// just add image to buffer
							buffer.append(t.getImage() );
						} else {
							buffer.append(
								(String) parameterValues.elementAt(index) );
						}
					} else if (t.type == tPOUND) {
						//next token should be a parameter which needs to be turned into
						//a string literal
						t = (Token) tokens.get( ++i );
						int index = parameterNames.indexOf(t.getImage());
						if( index == -1 ){
							handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, expansion.getName(), getCurrentOffset(), false, true );
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
							case IToken.tSTRING:
								buffer.append('\"');
								buffer.append(t.getImage());
								buffer.append('\"');  
								break;
							case IToken.tLSTRING: 
								buffer.append("L\""); //$NON-NLS-1$
								buffer.append(t.getImage());
								buffer.append('\"');  
								break;
							case IToken.tCHAR:	 
								buffer.append('\'');
								buffer.append(t.getImage());
								buffer.append('\'');  
								
								break;
							default:			 
								buffer.append(t.getImage());				
								break;
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
                        	buffer.append( " " );  //$NON-NLS-1$
				}
				String finalString = buffer.toString();
				try
				{
					StringBuffer completeSignature = new StringBuffer( POUND_DEFINE );
					completeSignature.append( expansion.getCompleteSignature() );
					scannerData.getContextStack().updateContext(
						new StringReader(finalString),
						completeSignature.toString(), ScannerContext.ContextKind.MACROEXPANSION, null, scannerData.getClientRequestor(), symbolOffset, endMacroOffset - symbolOffset + 1 );
				}
				catch (ContextException e)
				{
					handleProblem( e.getId(), scannerData.getContextStack().getCurrentContext().getFilename(), getCurrentOffset(), false, true );
					consumeUntilOutOfMacroExpansion();
					return;
				}
			} else
			{ 
				handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, symbol, getCurrentOffset(), false, true );
				consumeUntilOutOfMacroExpansion();
				return;
			}			

		} 
		else {
			StringBuffer logMessage = new StringBuffer( "Unexpected type of MacroDescriptor stored in definitions table: " ); //$NON-NLS-1$
			logMessage.append( expansion.getMacroType()  );
			scannerData.getLogService().traceLog( logMessage.toString() ); 
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
				handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, "defined()", o, false, true ); //$NON-NLS-1$
				return "0"; //$NON-NLS-1$
			}
		}
		else
		{
			ungetChar(c); 
			definitionIdentifier = getNextIdentifier(); 
		}		

		if (getDefinition(definitionIdentifier) != null)
			return "1"; //$NON-NLS-1$

		return "0"; //$NON-NLS-1$
	}
		
	public void setThrowExceptionOnBadCharacterRead( boolean throwOnBad ){
		throwExceptionOnBadCharacterRead = throwOnBad;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setASTFactory(org.eclipse.cdt.internal.core.parser.ast.IASTFactory)
	 */
	public void setASTFactory(IASTFactory f) {
		scannerData.setASTFactory(f);	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setOffsetBoundary(int)
	 */
	public void setOffsetBoundary(int offset) {
		offsetLimit = offset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getDefinitions()
	 */
	public Map getDefinitions() {
		return Collections.unmodifiableMap(scannerData.getDefinitions());
	}

	/**
	 * @param b
	 */
	public void setOffsetLimitReached(boolean b) {
		limitReached = b;
	}
	
	protected boolean isLimitReached()
	{
		if( offsetLimit == NO_OFFSET_LIMIT ) return false;
		return limitReached;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#isOnTopContext()
	 */
	public boolean isOnTopContext() {
		return ( scannerData.getContextStack().getCurrentContext() == scannerData.getContextStack().getTopContext() );
	}	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getCurrentFilename()
	 */
	public char[] getCurrentFilename() {
		return getCurrentFile().toCharArray();
	}

}
