/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Rational Software and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM - Rational Software
 ******************************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserFactory;
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
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionDirective;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionParseException;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author jcamelon
 *
 */

public final class Scanner implements IScanner, IScannerData {

	protected static final EndOfFileException EOF = new EndOfFileException();
	private ScannerStringBuffer strbuff = new ScannerStringBuffer(100);
	protected static final String HEX_PREFIX = "0x"; //$NON-NLS-1$
	private static final ObjectMacroDescriptor CPLUSPLUS_MACRO = new ObjectMacroDescriptor( __CPLUSPLUS, "199711L"); //$NON-NLS-1$
	private static final ObjectMacroDescriptor STDC_VERSION_MACRO = new ObjectMacroDescriptor( __STDC_VERSION__, "199001L"); //$NON-NLS-1$
	protected static final ObjectMacroDescriptor STDC_HOSTED_MACRO = new ObjectMacroDescriptor( __STDC_HOSTED__, "0"); //$NON-NLS-1$
	protected static final ObjectMacroDescriptor STDC_MACRO = new ObjectMacroDescriptor( __STDC__,  "1"); //$NON-NLS-1$
	private static final NullSourceElementRequestor NULL_REQUESTOR = new NullSourceElementRequestor();
	private final static String SCRATCH = "<scratch>"; //$NON-NLS-1$
	private final List workingCopies;
	protected final ContextStack contextStack;
	private IASTFactory astFactory = null;
	private ISourceElementRequestor requestor;
	private ParserMode parserMode;
	private final CodeReader reader;
	private final ParserLanguage language;
	protected IParserLogService log;
	private final IProblemFactory problemFactory = new ScannerProblemFactory();
	private Map definitions = new Hashtable();
	private BranchTracker branches = new BranchTracker();
	private final IScannerInfo originalConfig;
	private List includePathNames = new ArrayList();

	private final Map privateDefinitions = new Hashtable();

	private boolean initialContextInitialized = false;

	protected IToken finalToken;
	private final IScannerExtension scannerExtension;
	private static final int NO_OFFSET_LIMIT = -1;
	private int offsetLimit = NO_OFFSET_LIMIT;
	private boolean limitReached = false; 
	private IScannerContext currentContext;
	
	private final Map fileCache = new HashMap(100);
	
	public void setScannerContext(IScannerContext context) {
		currentContext = context;
	}
	
	protected void handleProblem( int problemID, String argument, int beginningOffset, boolean warning, boolean error ) throws ScannerException
	{
		handleProblem( problemID, argument, beginningOffset, warning, error, true );
	}

	protected void handleProblem( int problemID, String argument, int beginningOffset, boolean warning, boolean error, boolean extra ) throws ScannerException
	{
		IProblem problem = problemFactory.createProblem( 
				problemID, 
				beginningOffset, 
				getCurrentOffset(), 
				contextStack.getCurrentLineNumber(), 
				getCurrentFile().toCharArray(), 
				argument, 
				warning, 
				error );
		
		// trace log
		TraceUtil.outputTrace(log, "Scanner problem encountered: ", problem, null, null, null ); //$NON-NLS-1$
		
		if( (! requestor.acceptProblem( problem )) && extra )
			throw new ScannerException( problem );
	}

	Scanner( CodeReader reader, Map definitions, List includePaths, ISourceElementRequestor requestor, ParserMode mode, ParserLanguage language, IParserLogService log, IScannerExtension extension )
	{
		String [] incs = (String [])includePaths.toArray(STRING_ARRAY);
		this.log = log;
		this.requestor = requestor;
		this.parserMode = mode;
		this.reader = reader;
		this.language = language;
		this.originalConfig = new ScannerInfo( definitions, incs );
		this.contextStack = new ContextStack( this, log );
		this.workingCopies = null;
		this.scannerExtension = extension;
		this.definitions = definitions;
		this.includePathNames = includePaths;
		
		if (reader.isFile())
			fileCache.put(reader.filename, reader);
		
		setupBuiltInMacros();
	}
	
    public Scanner(CodeReader reader, IScannerInfo info, ISourceElementRequestor requestor, ParserMode parserMode, ParserLanguage language, IParserLogService log, IScannerExtension extension, List workingCopies ) {
    	
    	this.log = log;
    	this.requestor = requestor;
    	this.parserMode = parserMode;
    	this.reader = reader;
    	this.language = language;
    	this.originalConfig = info;
    	this.contextStack = new ContextStack( this, log );
    	this.workingCopies = workingCopies;
    	this.scannerExtension = extension;
		this.astFactory = ParserFactory.createASTFactory( this, parserMode, language );
		
		if (reader.isFile())
			fileCache.put(reader.filename, reader);
		
		TraceUtil.outputTrace(log, "Scanner constructed with the following configuration:"); //$NON-NLS-1$
		TraceUtil.outputTrace(log, "\tPreprocessor definitions from IScannerInfo: "); //$NON-NLS-1$

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
					//TODO add in check here for '(' and ')'
					addDefinition( symbolName, scannerExtension.initializeMacroValue(this, (String) value));
					TraceUtil.outputTrace(log,  "\t\tNAME = ", symbolName, " VALUE = ", value.toString() ); //$NON-NLS-1$ //$NON-NLS-2$
					++numberOfSymbolsLogged;
					
				}
				else if( value instanceof IMacroDescriptor )
					addDefinition( symbolName, (IMacroDescriptor)value);
			}
			if( numberOfSymbolsLogged == 0 )
				TraceUtil.outputTrace(log, "\t\tNo definitions specified."); //$NON-NLS-1$
			
		}
		else 
			TraceUtil.outputTrace(log, "\t\tNo definitions specified."); //$NON-NLS-1$
		
		
		TraceUtil.outputTrace( log, "\tInclude paths from IScannerInfo: "); //$NON-NLS-1$
		if( info.getIncludePaths() != null )
		{	
			overwriteIncludePath( info.getIncludePaths() );
			for( int i = 0; i < info.getIncludePaths().length; ++i )
				TraceUtil.outputTrace( log, "\t\tPATH: ", info.getIncludePaths()[i], null, null); //$NON-NLS-1$
		}
		else 
			TraceUtil.outputTrace(log, "\t\tNo include paths specified."); //$NON-NLS-1$
		
		setupBuiltInMacros();
    }

    public final ParserLanguage getLanguage() {
    	return language;
    }

    public final Map getPrivateDefinitions() {
    	return privateDefinitions;
    }
    
    public final ContextStack getContextStack() {
    	return contextStack;
    }
    
    public final IParserLogService getLogService() {
    	return log;
    }

    public final List getIncludePathNames() {
    	return includePathNames;
    }

    public final Map getPublicDefinitions() {
    	return definitions;
    }
    
    public final ParserMode getParserMode() {
    	return parserMode;
    }
    
    public final ISourceElementRequestor getClientRequestor() {
    	return requestor;
    }
    
    public final Iterator getWorkingCopies() {
    	return workingCopies != null ? workingCopies.iterator() : EmptyIterator.EMPTY_ITERATOR; 
    }
    /**
	 * 
	 */
	protected void setupBuiltInMacros() {
		
		scannerExtension.setupBuiltInMacros(this);
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
		
		
	}

	private void setupInitialContext()
    {
    	IScannerContext context = null;
    	try
    	{
    		if( offsetLimit == NO_OFFSET_LIMIT )
    			context = new ScannerContextTop(reader);
    		else
    			context = new LimitedScannerContext( this, reader, offsetLimit, 0 );
    		contextStack.pushInitialContext( context ); 
    	} catch( ContextException  ce )
    	{
    		handleInternalError();
    	}
    	initialContextInitialized = true;   	
    }
	
	public void addIncludePath(String includePath) {
		includePathNames.add(includePath);
	}

	public void overwriteIncludePath(String [] newIncludePaths) {
		if( newIncludePaths == null ) return;
		includePathNames = new ArrayList(newIncludePaths.length);
		
		for( int i = 0; i < newIncludePaths.length; ++i )
		{
			String path = newIncludePaths[i];
			
			File file = new File( path );
			
			if( !file.exists() && path.indexOf('\"') != -1 )
			{
				StringTokenizer tokenizer = new StringTokenizer(path, "\"" );	//$NON-NLS-1$
				strbuff.startString();
				while( tokenizer.hasMoreTokens() ){
					strbuff.append( tokenizer.nextToken() );
				}
				file = new File( strbuff.toString() );
			}

			if( file.exists() && file.isDirectory() )
				includePathNames.add( path );
			
		}		
	}

	public void addDefinition(String key, IMacroDescriptor macro) {
		definitions.put(key, macro);
	}

	public void addDefinition(String key, String value) {
		addDefinition(key, new ObjectMacroDescriptor( key, value ));
	}

	public final IMacroDescriptor getDefinition(String key) {
		IMacroDescriptor descriptor = (IMacroDescriptor) definitions.get(key);
		if( descriptor != null )
			return descriptor;
		return (IMacroDescriptor) privateDefinitions.get(key);
	}

	public final String[] getIncludePaths() {
		return (String[])includePathNames.toArray();
	}

	protected boolean skipOverWhitespace() throws ScannerException {
		int c = getChar(false);
		boolean result = false; 
		while ((c != NOCHAR) && ((c == ' ') || (c == '\t')))
		{
			c = getChar(false);
			result = true;
		}
		if (c != NOCHAR)
			ungetChar(c);
		return result; 

	}

	protected String getRestOfPreprocessorLine() throws ScannerException, EndOfFileException {

		skipOverWhitespace();
		int c = getChar(false);
		if (c == '\n') 
			return ""; //$NON-NLS-1$
		strbuff.startString();
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
				strbuff.append(c);
				c = getChar( true );
			}
			
			if (c == '/') {
				//only care about comments outside of a quote
				if( inString || inChar ){
					strbuff.append( c );
					c = getChar( true );
					continue;
				}
				
				// we need to peek ahead at the next character to see if 
				// this is a comment or not
				int next = getChar(false);
				if (next == '/') {
					// single line comment
					skipOverSinglelineComment();
					break;
				} else if (next == '*') {
					// multiline comment
					skipOverMultilineComment();
					c = getChar( true );
					continue;
				} else {
					// we are not in a comment
					strbuff.append(c);
					c = next;
					continue;
				}
			} else if( c == '"' ){
				inString = !inString;
				strbuff.append(c);
				c = getChar( true );
				continue;
			} else if( c == '\'' ){
				inChar = !inChar;
				strbuff.append(c);
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
					strbuff.append('\\');
					if( c == '"' || c == '\'' ){
						strbuff.append(c);
						c = getChar( true );
					}
				}
				continue;
			} else {
				ungetChar(c);
				break;
			}
		}

		return strbuff.toString();
	}

	protected void skipOverTextUntilNewline() throws ScannerException {
		for (;;) {
			switch (getChar(false)) {
				case NOCHAR :
				case '\n' :
					return;
				case '\\' :
					getChar(false);
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

	protected IToken newToken(int t, String i) {
		IToken token = TokenFactory.createUniquelyImagedToken(t, i, this );
		setCurrentToken(token);
		return currentToken;
	}

	protected IToken newConstantToken(int t) {
		setCurrentToken( TokenFactory.createToken(t, this));
		return currentToken;
	}
	
	protected String getNextIdentifier() throws ScannerException {
		strbuff.startString();
		skipOverWhitespace();
		int c = getChar(false);

		if (((c >= 'a') && (c <= 'z'))
			|| ((c >= 'A') && (c <= 'Z')) | (c == '_')) {
			strbuff.append(c);

			c = getChar(false);
			while (((c >= 'a') && (c <= 'z'))
				|| ((c >= 'A') && (c <= 'Z'))
				|| ((c >= '0') && (c <= '9'))
				|| (c == '_')) {
				strbuff.append(c);
				c = getChar(false);
			}
		}
		ungetChar(c);

		return strbuff.toString();
	}

	protected void handleInclusion(String fileName, boolean useIncludePaths, int beginOffset, int startLine, int nameOffset, int nameLine, int endOffset, int endLine ) throws ScannerException {

		CodeReader duple = null;
		totalLoop:	for( int i = 0; i < 2; ++i )
		{
			if( useIncludePaths ) // search include paths for this file
			{
				// iterate through the include paths 
				Iterator iter = includePathNames.iterator();
		
				while (iter.hasNext()) {
		
					String path = (String)iter.next();
					String finalPath = ScannerUtility.createReconciledPath(path, fileName);
					duple = (CodeReader)fileCache.get(finalPath);
					if (duple == null) {
						duple = ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
						if (duple != null && duple.isFile())
							fileCache.put(duple.filename, duple);
					}
					if( duple != null )
						break totalLoop;
				}
				
				if (duple == null )
					handleProblem( IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, fileName, beginOffset, false, true );
	
			}
			else // local inclusion
			{
				String finalPath = ScannerUtility.createReconciledPath(new File( currentContext.getContextName() ).getParentFile().getAbsolutePath(), fileName);
				duple = (CodeReader)fileCache.get(finalPath);
				if (duple == null) {
					duple = ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
					if (duple != null && duple.isFile())
						fileCache.put(duple.filename, duple);
				}
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
                	getASTFactory().createInclusion(
                        fileName,
                        duple.filename,
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
				contextStack.updateInclusionContext(
					duple,
					inclusion, 
					requestor);
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
			String currentFilename = currentContext.getFilename(); 
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
//	private boolean enableDigraphReplacement = true;
//	private boolean enableTrigraphReplacement = true;
//	private boolean enableTrigraphReplacementInStrings = true;
	private boolean throwExceptionOnBadCharacterRead = false; 

	private boolean tokenizingMacroReplacementList = false;
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private Map tempMap = new HashMap(); //$NON-NLS-1$
	public void setTokenizingMacroReplacementList( boolean mr ){
		tokenizingMacroReplacementList = mr;
	}
	
	public final int getCharacter() throws ScannerException
	{
		if( ! initialContextInitialized )
			setupInitialContext();
		
		return getChar(false);
	}
	
	final int getChar() throws ScannerException {
		return getChar(false);
	}
	
	private final int getChar( boolean insideString ) throws ScannerException {	
		
		lastContext = currentContext;
		
		if (lastContext.getKind() == IScannerContext.ContextKind.SENTINEL)
			// past the end of file
			return NOCHAR;
		
    	int c = currentContext.getChar();
    	if (c != NOCHAR)
    		return c;
    	
    	if (currentContext.isFinal())
    		return c;
    	
    	while (contextStack.rollbackContext(requestor)) {
    		c = currentContext.getChar();
    		if (c != NOCHAR)
    			return c;
    		if (currentContext.isFinal())
        		return c;
    	}

    	return NOCHAR;

  		/*
		if (enableTrigraphReplacement && (!insideString || enableTrigraphReplacementInStrings)) {
			// Trigraph processing
			enableTrigraphReplacement = false;
			if (c == '?') {
				c = getChar(insideString);
				if (c == '?') {
					c = getChar(insideString);
					switch (c) {
						case '(':
							expandDefinition("??(", "[", lastContext.getOffset() - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case ')':
							expandDefinition("??)", "]", lastContext.getOffset() - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '<':
							expandDefinition("??<", "{", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '>':
							expandDefinition("??>", "}", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '=':
							expandDefinition("??=", "#", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '/':
							expandDefinition("??/", "\\", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '\'':
							expandDefinition("??\'", "^", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '!':
							expandDefinition("??!", "|", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
							c = getChar(insideString);
							break;
						case '-':
							expandDefinition("??-", "~", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
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
						expandDefinition("<%", "{", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else if (c == ':') {
						expandDefinition("<:", "[", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else {
						// Not a digraph
						ungetChar(c);
						c = '<';
					}
				} else if (c == ':') {
					c = getChar(false);
					if (c == '>') {
						expandDefinition(":>", "]", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else {
						// Not a digraph
						ungetChar(c);
						c = ':';
					}
				} else if (c == '%') {
					c = getChar(false);
					if (c == '>') {
						expandDefinition("%>", "}", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
						c = getChar(false);
					} else if (c == ':') {
						expandDefinition("%:", "#", lastContext.getOffset()  - 1); //$NON-NLS-1$ //$NON-NLS-2$
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
		return c;*/
	}

	final void ungetChar(int c) {
		currentContext.ungetChar(c);
		if( lastContext != currentContext)
			contextStack.undoRollback( lastContext, requestor );
	}

	protected boolean lookAheadForTokenPasting() throws ScannerException
	{
		int c = getChar(false); 
		if( c == '#' )
		{
			c = getChar(false); 
			if( c == '#' )
				return true; 
			ungetChar( c );
		}

		ungetChar( c );
		return false; 

	}

	protected void consumeUntilOutOfMacroExpansion() throws ScannerException
	{
		while( currentContext.getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			getChar(false);
	}

	public IToken nextToken() throws ScannerException, EndOfFileException {
		return nextToken( true ); 
	}
	
	public boolean pasteIntoInputStream(String buff) throws ScannerException, EndOfFileException
	{
		// we have found ## in the input stream -- so save the results
		if( lookAheadForTokenPasting() )
		{
			if( storageBuffer == null )
				storageBuffer = new StringBuffer(buff);
			else
				storageBuffer.append( buff ); 
			return true;
		}

		// a previous call has stored information, so we will add to it
		if( storageBuffer != null )
		{
			storageBuffer.append( buff.toString() );
			try
			{
				contextStack.updateMacroContext( 
					storageBuffer.toString(), 
					PASTING,
					requestor, -1, -1 );
			}
			catch (ContextException e)
			{
				handleProblem( e.getId(), currentContext.getContextName(), getCurrentOffset(), false, true  );
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
			// consume the \ \r and then continue
			return c;
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
		strbuff.startString(); 
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

			strbuff.append(c);
			beforePrevious = previous;
			previous = c;
			c = getChar(true);
		}

		int type = wideLiteral ? IToken.tLSTRING : IToken.tSTRING;
							
		//If the next token is going to be a string as well, we need to concatenate
		//it with this token.  This will be recursive for as many strings as need to be concatenated
		
		String result = strbuff.toString();
		IToken returnToken = newToken( type, result );
			
		IToken next = null;
		try{
			next = nextToken( true );
			if ( next != null && 
					(next.getType() == IToken.tSTRING || 
				     next.getType() == IToken.tLSTRING ))  {
				returnToken.setImage(result + next.getImage());
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
		strbuff.startString();
		
		boolean hex = false;
		boolean floatingPoint = ( c == '.' ) ? true : false;
		boolean firstCharZero = ( c== '0' )? true : false; 
			
		strbuff.append(c);

		int firstChar = c;
		c = getChar(false);
		
		if( ! firstCharZero && floatingPoint && !(c >= '0' && c <= '9') ){
			//if pasting, there could actually be a float here instead of just a .
			if( firstChar == '.' ) { 
				if( c == '*' ){
					return newConstantToken( IToken.tDOTSTAR );
				} else if( c == '.' ){
					if( getChar(false) == '.' )
						return newConstantToken( IToken.tELLIPSIS );
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
			strbuff.append(c);
			hex = true;
			c = getChar(false);
		}

		while ((c >= '0' && c <= '9')
			|| (hex
				&& ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))) {
			strbuff.append(c);
			c = getChar(false);
		}
		
		if( c == '.' )
		{
			strbuff.append(c);
			
			floatingPoint = true;
			c= getChar(false); 
			while ((c >= '0' && c <= '9')
			|| (hex
				&& ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))))
			{
				strbuff.append(c);
				c = getChar(false);
			}
		}
		

		if (c == 'e' || c == 'E' || (hex && (c == 'p' || c == 'P')))
		{
			if( ! floatingPoint ) floatingPoint = true; 
			// exponent type for floating point 
			strbuff.append(c);
			c = getChar(false); 
			
			// optional + or - 
			if( c == '+' || c == '-' )
			{
				strbuff.append(c );
				c = getChar(false); 
			}
			
			// digit sequence of exponent part 
			while ((c >= '0' && c <= '9') )
			{
				strbuff.append(c);
				c = getChar(false);
			}
			
			// optional suffix 
			if( c == 'l' || c == 'L' || c == 'f' || c == 'F' )
			{
				strbuff.append(c );
				c = getChar(false); 
			}
		} else {
			if( floatingPoint ){
				//floating-suffix
				if( c == 'l' || c == 'L' || c == 'f' || c == 'F' ){
					c = getChar(false);
				}
			} else {
				//integer suffix
				if( c == 'u' || c == 'U' ){
					c = getChar(false);
					if( c == 'l' || c == 'L')
						c = getChar(false);
					if( c == 'l' || c == 'L')
						c = getChar(false);
				} else if( c == 'l' || c == 'L' ){
					c = getChar(false);
					if( c == 'l' || c == 'L')
						c = getChar(false);
					if( c == 'u' || c == 'U' )
						c = getChar(false);
				}
			}
		}

		ungetChar( c );
	
		String result = strbuff.toString(); 
		
		if( pasting && pasteIntoInputStream(result))
			return null;

		if( floatingPoint && result.equals(".") ) //$NON-NLS-1$
			return newConstantToken( IToken.tDOT );
		
		int tokenType = floatingPoint ? IToken.tFLOATINGPT : IToken.tINTEGER;
		if( tokenType == IToken.tINTEGER && hex )
		{
			if( result.equals( HEX_PREFIX ) )
			{
				handleProblem( IProblem.SCANNER_BAD_HEX_FORMAT, HEX_PREFIX, beginOffset, false, true );
				return null;
			}
		}
		
		return newToken(
			tokenType,
			result);
	}
	public IToken processPreprocessor() throws ScannerException, EndOfFileException
	{
		int c;
		int beginningOffset = currentContext.getOffset() - 1;
		int beginningLine = contextStack.getCurrentLineNumber();

		// we are allowed arbitrary whitespace after the '#' and before the rest of the text
		boolean skipped = skipOverWhitespace();

		c = getChar(false);
		
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
		
		strbuff.startString();
		strbuff.append('#');		
		while (((c >= 'a') && (c <= 'z'))
			|| ((c >= 'A') && (c <= 'Z')) || (c == '_') ) {
			strbuff.append(c);
			c = getChar(false);
		}
		
		ungetChar(c);

		String token = strbuff.toString();

		if( isLimitReached() )
			handleCompletionOnPreprocessorDirective(token);
		
		Object directive = ppDirectives.get(token);
		if (directive == null) {
			if( scannerExtension.canHandlePreprocessorDirective( token ) )
				scannerExtension.handlePreprocessorDirective( this, token, getRestOfPreprocessorLine() );
			else
			{
				if( passOnToClient )
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, token, beginningOffset, false, true );
			}
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
				
				if( branches.queryCurrentBranchForIf() )
				    expressionEvalResult = evaluateExpression(expression, currentOffset);
				
				passOnToClient = branches.poundIf( expressionEvalResult ); 
				return null;

			case PreprocessorDirectives.IFDEF :
				//TODO add in content assist stuff here
				
				String definition = getNextIdentifier();
				if( isLimitReached() )
					handleCompletionOnDefinition( definition );
					
				if (getDefinition(definition) == null) {
					// not defined	
					passOnToClient = branches.poundIf( false );
					skipOverTextUntilNewline();
				} else 
					// continue along, act like nothing is wrong :-)
					passOnToClient = branches.poundIf( true ); 
				return null;
				
			case PreprocessorDirectives.ENDIF :
				String restOfLine = getRestOfPreprocessorLine().trim();
				if( isLimitReached() )
					handleInvalidCompletion();
				
				if( ! restOfLine.equals( "" )  ) //$NON-NLS-1$
				{	
					strbuff.startString();
					strbuff.append("#endif "); //$NON-NLS-1$
					strbuff.append( restOfLine );
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, strbuff.toString(), beginningOffset, false, true );
				}
				try{
					passOnToClient = branches.poundEndif();
				}
				catch( EmptyStackException ese )
				{
					handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, 
						token, 
						beginningOffset, 
						false, true );  
				}
				return null;
				
			case PreprocessorDirectives.IFNDEF :
				//TODO add in content assist stuff here

				String definition2 = getNextIdentifier();
				if( isLimitReached() )
					handleCompletionOnDefinition( definition2 );
				
				if (getDefinition(definition2) != null) {
					// not defined	
					skipOverTextUntilNewline();
					passOnToClient = branches.poundIf( false );
					if( isLimitReached() )
						handleInvalidCompletion();
					
				} else
					// continue along, act like nothing is wrong :-)
					passOnToClient = branches.poundIf( true ); 		
				return null;

			case PreprocessorDirectives.ELSE :
				try
				{
					passOnToClient = branches.poundElse();
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
				if( branches.queryCurrentBranchForElif() )
					elsifResult = evaluateExpression(elifExpression, co );

				try
				{
					passOnToClient = branches.poundElif( elsifResult );
				}
				catch( EmptyStackException ese )
				{
					strbuff.startString();
					strbuff.append( token );
					strbuff.append( ' ' );
					strbuff.append( elifExpression );
					handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, 
						strbuff.toString(), 
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
				String restOfErrorLine = getRestOfPreprocessorLine();
				if( isLimitReached() )
					handleInvalidCompletion();	

				handleProblem( IProblem.PREPROCESSOR_POUND_ERROR, restOfErrorLine, beginningOffset, false, true );
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
					strbuff.startString();
					strbuff.append( "# "); //$NON-NLS-1$
					strbuff.append( remainderOfLine );
					handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, strbuff.toString(), beginningOffset, false, true);
				}
				return null;
				
			default :
				strbuff.startString();
				strbuff.append( "# "); //$NON-NLS-1$
				strbuff.append( token );
				handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, strbuff.toString(), beginningOffset, false, true );
				return null;
		}
	}
	
	// buff contains \\u or \\U
	protected boolean processUniversalCharacterName() throws ScannerException
	{
		// first octet is mandatory
		for( int i = 0; i < 4; ++i )
		{
			int c = getChar(false);
			if( ! isHex( c ))
				return false;
			strbuff.append( c );
		}
		
		Vector v = new Vector();
		Overall: for( int i = 0; i < 4; ++i )
		{
			int c = getChar(false);
			if( ! isHex( c ))
			{
				ungetChar( c );
				break;
			}	
			v.add( new Character((char) c ));
		}

		if( v.size() == 4 )
		{
			for( int i = 0; i < 4; ++i )
				strbuff.append( ((Character)v.get(i)).charValue());
		}
		else
		{
			for( int i = v.size() - 1; i >= 0; --i )
				ungetChar( ((Character)v.get(i)).charValue() );
		}
		return true;
	}
	
	/**
	 * @param c
	 * @return
	 */
	private boolean isHex(int c) {
		switch( c )
		{
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
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				return true;
			default:
				return false;
								
		}
	}

	protected IToken processKeywordOrIdentifier(boolean pasting) throws ScannerException, EndOfFileException
	{ 
        int baseOffset = lastContext.getOffset() - 1;
				
		// String buffer is slow, we need a better way such as memory mapped files
		int c = getChar(false);				
		
		for( ; ; )
		{
			// do the least expensive tests first!
			while (	( scannerExtension.offersDifferentIdentifierCharacters() && 
					  scannerExtension.isValidIdentifierCharacter(c) ) || 
					  isValidIdentifierCharacter(c) ) {
				strbuff.append(c);
				c = getChar(false);
				if (c == '\\') {
					c = consumeNewlineAfterSlash();
				}
			}
			if( c == '\\')
			{
				int next = getChar(false);
				if( next == 'u' || next == 'U')
				{
					strbuff.append( '\\');
					strbuff.append( next );
					if( !processUniversalCharacterName() )	
						return null;
					c = getChar(false);
					continue; // back to top of loop
				}
				ungetChar( next );
			}
			break;
		}
			
		ungetChar(c);

		String ident = strbuff. toString();

		if (ident.equals(DEFINED))
			return newToken(IToken.tINTEGER, handleDefinedMacro());
		
		if( ident.equals(_PRAGMA) && language == ParserLanguage.C )
		{
			handlePragmaOperator(); 
			return null;
		}
			
		if (!disableMacroExpansion) {
			IMacroDescriptor mapping = getDefinition(ident);
	
			if (mapping != null && !isLimitReached() && !mapping.isCircular() )
				if( contextStack.shouldExpandDefinition( ident ) ) {					
					expandDefinition(ident, mapping, baseOffset);
					return null;
				}
		}
		if( pasting && pasteIntoInputStream(ident))
			return null;
		
		Object tokenTypeObject;
		if( language == ParserLanguage.CPP )
		 	tokenTypeObject = cppKeywords.get(ident);
		else
			tokenTypeObject = cKeywords.get(ident);

		if (tokenTypeObject != null)
			return newConstantToken(((Integer) tokenTypeObject).intValue());
		if( scannerExtension.isExtensionKeyword( language, ident ) )
			return newExtensionToken( scannerExtension.createExtensionToken(this, ident ));
		return newToken(IToken.tIDENTIFIER, ident);
	}
	
	/**
	 * @param token
	 * @return
	 */
	protected IToken newExtensionToken(IToken token) {
		setCurrentToken( token );
		return currentToken;
	}

	/**
	 * @param c
	 * @return
	 */
	protected boolean isValidIdentifierCharacter(int c) {
		return ((c >= 'a') && (c <= 'z'))
		|| ((c >= 'A') && (c <= 'Z'))
		|| ((c >= '0') && (c <= '9'))
		|| (c == '_') || Character.isUnicodeIdentifierPart( (char)c);
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
		
		int c = getChar(false);

		while (c != NOCHAR) {
			if ( ! passOnToClient ) {
				while (c != NOCHAR && c != '#' ) 
				{
					c = getChar(false);
					if( c == '/' )
					{
						c = getChar(false);
						if( c == '/' )
						{
							skipOverSinglelineComment();
							c = getChar(false);
							continue;
						}
						else if( c == '*' )
						{
							skipOverMultilineComment();
							c = getChar(false);
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
					c = getChar(false);
					continue;
				case ':' :
					c = getChar(false);
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
					c = getChar(false);
					if (c == '?')
					{
						// trigraph
						c = getChar(false);
						switch (c) {
							case '=':
								// this is the same as the # case
								token = processPreprocessor();
								if (token == null) 
								{
									c = getChar(false);
									continue;
								}
								return token;
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
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tPLUSASSIGN);
						case '+' : return newConstantToken(IToken.tINCR);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tPLUS);
					}
				case '-' :
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tMINUSASSIGN);
						case '-' : return newConstantToken(IToken.tDECR);
						case '>' :
							c = getChar(false);
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
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tSTARASSIGN);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tSTAR);
					}
				case '%' :
					c = getChar(false);
					switch (c) {			
						case '=' : return newConstantToken(IToken.tMODASSIGN);
						
						// Diagraph
						case '>' : return newConstantToken(IToken.tRBRACE); 
						case ':' :
							// this is the same as the # case
							token = processPreprocessor();
							if (token == null) 
							{
								c = getChar(false);
								continue;
							}
							return token;
						default :
							ungetChar(c);
							return newConstantToken(IToken.tMOD);
					}
				case '^' :
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tXORASSIGN);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tXOR);
					}
				case '&' :
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tAMPERASSIGN);
						case '&' : return newConstantToken(IToken.tAND);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tAMPER);
					}
				case '|' :
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tBITORASSIGN);
						case '|' : return newConstantToken(IToken.tOR);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tBITOR);
					}
				case '~' : return newConstantToken(IToken.tCOMPL);
				case '!' :
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tNOTEQUAL);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tNOT);
					}
				case '=' :
					c = getChar(false);
					switch (c) {
						case '=' : return newConstantToken(IToken.tEQUAL);
						default :
							ungetChar(c);
							return newConstantToken(IToken.tASSIGN);
					}
				case '<' :					
					c = getChar(false);
					switch (c) {
						case '<' :
							c = getChar(false);
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
							strbuff.startString();
							strbuff.append('<');
							strbuff.append(c);
							String query = strbuff.toString();
							if( scannerExtension.isExtensionOperator( language, query ) )
								return newExtensionToken( scannerExtension.createExtensionToken( this, query ));
							ungetChar(c);
							if( forInclusion )
								temporarilyReplaceDefinitionsMap();
							return newConstantToken(IToken.tLT);
					}
				case '>' :
					c = getChar(false);
					switch (c) {
						case '>' :
							c = getChar(false);
							switch (c) {
								case '=' : return newConstantToken(IToken.tSHIFTRASSIGN);
								default :
									ungetChar(c);
									return newConstantToken(IToken.tSHIFTR);
							}
						case '=' : return newConstantToken(IToken.tGTEQUAL);
						default :
							strbuff.startString();
							strbuff.append('>');
							strbuff.append( (char)c);
							String query = strbuff.toString();
							if( scannerExtension.isExtensionOperator( language, query ) )
								return newExtensionToken( scannerExtension.createExtensionToken( this, query ));
							ungetChar(c);
							if( forInclusion )
								temporarilyReplaceDefinitionsMap();
							return newConstantToken(IToken.tGT);
					}
				case '.' :
					c = getChar(false);
					switch (c) {
						case '.' :
							c = getChar(false);
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
					c = getChar(false);
					switch (c) {
						case '/' :
							skipOverSinglelineComment();
							c = getChar(false);
							continue;
						case '*' :
							skipOverMultilineComment();
							c = getChar(false);
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
						c = getChar(false);
						continue;
					}
					return token;
						
				case 'L' :
					// check for wide literal
					c = getChar(false); 
					if (c == '"')
						token = processStringLiteral(true);
					else if (c == '\'')
						return processCharacterLiteral( c, true );
					else
					{
						// This is not a wide literal -- it must be a token or keyword
						ungetChar(c);
						strbuff.startString();
						strbuff.append('L');
						token = processKeywordOrIdentifier(pasting);
					}
					if (token == null) 
					{
						c = getChar(false);
						continue;
					}
					return token;
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
					// 'L' is handled elsewhere
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
					strbuff.startString();
					strbuff.append( c );
					token = processKeywordOrIdentifier(pasting);
					if (token == null) 
					{
						c = getChar(false);
						continue;
					}
					return token;
				case '"' :
					token = processStringLiteral(false);
					if (token == null) 
					{
						c = getChar(false);
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
						c = getChar(false);
						continue;
					}
					return token;
						
				default:
					if ( 	( scannerExtension.offersDifferentIdentifierCharacters() && 
							  scannerExtension.isValidIdentifierStartCharacter(c) ) || 
							 isValidIdentifierStartCharacter(c)  ) 
					{
						strbuff.startString();
						strbuff.append( c );
						token = processKeywordOrIdentifier(pasting);
						if (token == null) 
						{
							c = getChar(false);
							continue;
						}
						return token;
					}
					else if( c == '\\' )
					{
						int next = getChar(false);
						strbuff.startString();
						strbuff.append( '\\');
						strbuff.append( next );

						if( next == 'u' || next =='U' )
						{
							if( !processUniversalCharacterName() )
							{
								handleProblem( IProblem.SCANNER_BAD_CHARACTER, strbuff.toString(), getCurrentOffset(), false, true, throwExceptionOnBadCharacterRead );
								c = getChar(false);
								continue;
							}
							token = processKeywordOrIdentifier( pasting );
							if (token == null) 
							{
								c = getChar(false);
								continue;
							}
							return token;
						}
						ungetChar( next );
						handleProblem( IProblem.SCANNER_BAD_CHARACTER, strbuff.toString(), getCurrentOffset(), false, true, throwExceptionOnBadCharacterRead );
					}
					
					handleProblem( IProblem.SCANNER_BAD_CHARACTER, new Character( (char)c ).toString(), getCurrentOffset(), false, true, throwExceptionOnBadCharacterRead ); 
					c = getChar(false);
					continue;			
			}
		}

		// we're done
		throwEOF(null);
		return null;
	}



    /**
	 * @param c
	 * @return
	 */
	protected boolean isValidIdentifierStartCharacter(int c) {
		return Character.isLetter((char)c) || ( c == '_');
	}

	/**
	 * @param definition
	 */
	protected void handleCompletionOnDefinition(String definition) throws EndOfFileException {
		IASTCompletionNode node = new ASTCompletionNode( IASTCompletionNode.CompletionKind.MACRO_REFERENCE, 
				null, null, definition, KeywordSets.getKeywords(KeywordSetKey.EMPTY, language), EMPTY_STRING, null );
		
		throwEOF( node ); 
	}

	/**
	 * @param expression2
	 */
	protected void handleCompletionOnExpression(String expression) throws EndOfFileException {
		int completionPoint = expression.length() + 2;
		IASTCompletionNode.CompletionKind kind = IASTCompletionNode.CompletionKind.MACRO_REFERENCE;
		
		String prefix = EMPTY_STRING;
		
		if( ! expression.trim().equals(EMPTY_STRING))
		{	
			IScanner subScanner = new Scanner(
					new CodeReader(expression.toCharArray()),
					getTemporaryHashtable(), 
					Collections.EMPTY_LIST, 
					NULL_REQUESTOR, 
					ParserMode.QUICK_PARSE, 
					language, 
					NULL_LOG_SERVICE, 
					scannerExtension );
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
				KeywordSets.getKeywords(((kind == IASTCompletionNode.CompletionKind.NO_SUCH_KIND )? KeywordSetKey.EMPTY : KeywordSetKey.MACRO), language), EMPTY_STRING, null );
		
		throwEOF( node );
	}

	/**
	 * @return
	 */
	private Map getTemporaryHashtable() {
		tempMap.clear();
		return tempMap = new HashMap();
	}

	protected void handleInvalidCompletion() throws EndOfFileException
	{
		throwEOF( new ASTCompletionNode( IASTCompletionNode.CompletionKind.UNREACHABLE_CODE, null, null, EMPTY_STRING, KeywordSets.getKeywords(KeywordSetKey.EMPTY, language ) , EMPTY_STRING, null)); 
	}
	
	protected void handleCompletionOnPreprocessorDirective( String prefix ) throws EndOfFileException 
	{
		throwEOF( new ASTCompletionNode( IASTCompletionNode.CompletionKind.NO_SUCH_KIND, null, null, prefix, KeywordSets.getKeywords(KeywordSetKey.PP_DIRECTIVE, language ), EMPTY_STRING, null));
	}
	/**
	 * @param key
	 */
	protected void removeSymbol(String key) {
		definitions.remove(key);
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
        
        strbuff.startString(); 
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
			
        	strbuff.append(c);
        	prevPrev = prev;
        	prev = c;
        	c = getChar(true);
        }
        
        return newToken( type, strbuff.toString());                      
    }

    

    protected String getCurrentFile()
	{
		return contextStack.getMostRelevantFileContext() != null ? contextStack.getMostRelevantFileContext().getContextName() : ""; //$NON-NLS-1$
	}


    protected int getCurrentOffset()
    {
        return contextStack.getMostRelevantFileContext() != null ? contextStack.getMostRelevantFileContext().getOffset() : -1;
    }


    protected static class endOfMacroTokenException extends Exception {}
    // the static instance we always use
    protected static endOfMacroTokenException endOfMacroToken = new endOfMacroTokenException();
    
    public IToken nextTokenForStringizing() throws ScannerException, EndOfFileException
    {     
    	int beginOffset = getCurrentOffset();
        int c = getChar(false);
        strbuff.startString();

        try {
        	while (c != NOCHAR) {
                switch (c) {
                	case ' ' :
                	case '\r' :
                	case '\t' :
                	case '\n' :
                		 if (strbuff.length() > 0) throw endOfMacroToken;                
                         c = getChar(false);
                         continue;
                	case '"' :
    	                if (strbuff.length() > 0) throw endOfMacroToken;
    	                 
    	                // string
    	                strbuff.startString(); 
    	                c = getChar(true);

    	                for( ; ; )
    	                {
    	                    if ( c =='"' ) break;
    	                    if( c == NOCHAR) break;  
    	                    strbuff.append(c);
    	                    c = getChar(true);
    	                }

    	                if (c != NOCHAR ) 
    	                {
    	                    return newToken( IToken.tSTRING, strbuff.toString());
    	    
    	                }
    	                handleProblem( IProblem.SCANNER_UNBOUNDED_STRING, null, beginOffset, false, true );
    	                c = getChar(false); 
    	                continue;
    	        
                    case '\'' :
	                    if (strbuff.length() > 0) throw endOfMacroToken;
	                    return processCharacterLiteral( c, false );
                    case ',' :
                        if (strbuff.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tCOMMA, ","); //$NON-NLS-1$
                    case '(' :
                        if (strbuff.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tLPAREN, "("); //$NON-NLS-1$
                    case ')' :
                        if (strbuff.length() > 0) throw endOfMacroToken;
                        return newToken(IToken.tRPAREN, ")"); //$NON-NLS-1$
                    case '/' :
                        if (strbuff.length() > 0) throw endOfMacroToken;
                        c = getChar(false);
                        switch (c) {
                            case '/' :
								skipOverSinglelineComment();
								c = getChar(false);
                                continue;
                            case '*' :
                                skipOverMultilineComment();
                                c = getChar(false);
                                continue;
                            default:
                                strbuff.append('/');
                                continue;
                        }
                    default :
                        strbuff.append(c);
                        c = getChar(false);
                }
            }
        } catch (endOfMacroTokenException e) {
            // unget the first character after the end of token
            ungetChar(c);            
        }
        
        // return completed token
        if (strbuff.length() > 0) {
            return newToken(IToken.tIDENTIFIER, strbuff.toString());
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
				throw EOF;
			
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
		return branches.getDepth(); 
	}

	protected boolean evaluateExpressionNew(String expression, int beginningOffset ) 
		throws ScannerException {
		  
		// TODO John HELP!  something has changed.   If I turn this to true, My tests finish early (but the JUnits pass!)
		IScannerContext context = new ScannerContextTopString(expression, EXPRESSION, ';', true);
		contextStack.cs_push(context);

		ISourceElementRequestor savedRequestor =  requestor;
		IParserLogService savedLog = log;
		log = NULL_LOG_SERVICE;
		requestor = NULL_REQUESTOR;
		
		
		boolean savedPassOnToClient = passOnToClient;
		ParserMode savedParserMode = parserMode;
		IASTFactory savedFactory = astFactory;

		
		passOnToClient = true;
		parserMode = ParserMode.QUICK_PARSE;
		
		IExpressionParser parser = InternalParserUtil.createExpressionParser(this, language, NULL_LOG_SERVICE);
		try {
			IASTExpression exp = parser.expression(null, null, null);
			return (exp.evaluateExpression() != 0);
		} catch( BacktrackException backtrack  )
		{
				return false;
		} catch (ASTExpressionEvaluationException e) {
				return false;			
		} catch (EndOfFileException e) {
				return false;
		} finally {
			contextStack.cs_pop();
			requestor = savedRequestor;
			passOnToClient = savedPassOnToClient;
			parserMode = savedParserMode;
			astFactory = savedFactory;
			log = savedLog;
		}
	}
	
	protected boolean evaluateExpressionOld(String expression, int beginningOffset )
		throws ScannerException {

		IExpressionParser parser = null;
		strbuff.startString();
		strbuff.append(expression);
		strbuff.append(';');
		   
		IScanner trial = new Scanner(
				new CodeReader(strbuff.toString().toCharArray()), 
				definitions,
				includePathNames,
				NULL_REQUESTOR,
				ParserMode.QUICK_PARSE, 
				language,
				NULL_LOG_SERVICE,
				scannerExtension );
		
        parser = InternalParserUtil.createExpressionParser(trial, language, NULL_LOG_SERVICE);
		try {
			IASTExpression exp = parser.expression(null, null, null);
			return (exp.evaluateExpression() != 0);
		} catch( BacktrackException backtrack  )
		{
			if( parserMode == ParserMode.QUICK_PARSE )
				return false;
			handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true ); 
		}
		catch (ASTExpressionEvaluationException e) {
			if( parserMode == ParserMode.QUICK_PARSE )
				return false;			
			handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true );
		} catch (EndOfFileException e) {
			if( parserMode == ParserMode.QUICK_PARSE )
				return false;
			handleProblem( IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, expression, beginningOffset, false, true );
		}
		return true; 
	
	}
	protected boolean evaluateExpression(String expression, int beginningOffset )
		throws ScannerException {

//		boolean old_e = evaluateExpressionOld(expression, beginningOffset);
		boolean new_e = evaluateExpressionNew(expression, beginningOffset);

//		if (old_e != new_e) {
//			System.out.println("Ouch " + expression + " New: " + new_e + " Old: " + old_e);
//		}
//		if (true)
			return new_e;
//		else 
//			return old_e;
	}

	
	protected void skipOverSinglelineComment() throws ScannerException, EndOfFileException {
		int c;
		
		loop:
		for (;;) {
			c = getChar(false);
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
		// simple state machine to handle multi-line comments
		// state 0 == no end of comment in site
		// state 1 == encountered *, expecting /
		// state 2 == we are no longer in a comment

		int c = getChar(false);
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
			c = getChar(false);
		}

		if ( state != 2)
			if (c == NOCHAR && !isLimitReached() )
				handleProblem( IProblem.SCANNER_UNEXPECTED_EOF, null, getCurrentOffset(), false, true  );
			else if( c== NOCHAR ) // limit reached
				handleInvalidCompletion();
		
		ungetChar(c);

		return encounteredNewline;
	}
	
	private static final InclusionParseException INCLUSION_PARSE_EXCEPTION  = new InclusionParseException(); 

	public InclusionDirective parseInclusionDirective( String includeLine, int baseOffset ) throws InclusionParseException 
	{
		if (includeLine.equals(""))  //$NON-NLS-1$
			throw INCLUSION_PARSE_EXCEPTION ;
		
		ISourceElementRequestor savedRequestor = requestor;
		try
		{
			IScannerContext context = new ScannerContextTopString( includeLine, "INCLUDE", true );  //$NON-NLS-1$
			contextStack.cs_push(context);
			requestor = NULL_REQUESTOR;
			
			boolean useIncludePath = true;
			StringBuffer localStringBuff = new StringBuffer(100);
			int startOffset = baseOffset, endOffset = baseOffset;

			IToken t = null;
			
			try {
				t = nextToken(false);
			} catch (EndOfFileException eof) {
				throw INCLUSION_PARSE_EXCEPTION ;
			} 

			try {
				if (t.getType() == IToken.tSTRING) {
					localStringBuff.append(t.getImage());
					startOffset = baseOffset + t.getOffset();
					endOffset = baseOffset + t.getEndOffset();
					useIncludePath = false;
					
					// This should throw EOF
					t = nextToken(false);
					contextStack.cs_pop();
					requestor = savedRequestor;
					throw INCLUSION_PARSE_EXCEPTION ;
				} else if (t.getType() == IToken.tLT) {
					disableMacroExpansion = true;
					try {
											
						t = nextToken(false);
						startOffset = baseOffset + t.getOffset();
						
						while (t.getType() != IToken.tGT) {
							localStringBuff.append(t.getImage());
							skipOverWhitespace();
							int c = getChar();
							if (c == '\\') 
								localStringBuff.append('\\'); 
							else 
								ungetChar(c);
							t = nextToken(false);
						}
						
						endOffset = baseOffset + t.getEndOffset();
						
					} catch (EndOfFileException eof) {
						throw INCLUSION_PARSE_EXCEPTION ;
					}
					
					// This should throw EOF
					t = nextToken(false);

					throw INCLUSION_PARSE_EXCEPTION ;
					
				} else 
					throw INCLUSION_PARSE_EXCEPTION ;
			}
			catch( EndOfFileException eof )
			{
				// good
			} 
			
			return new InclusionDirective( localStringBuff.toString(), useIncludePath, startOffset, endOffset );
		}
		catch( ScannerException se )
		{
			throw INCLUSION_PARSE_EXCEPTION ;
		} finally {
			contextStack.cs_pop();
			requestor = savedRequestor;
			disableMacroExpansion = false;
		}
	}
	protected void poundInclude( int beginningOffset, int startLine ) throws ScannerException, EndOfFileException {
		skipOverWhitespace();				
		int baseOffset = lastContext.getOffset() ;
		int nameLine = contextStack.getCurrentLineNumber();
		String includeLine = getRestOfPreprocessorLine();
		if( isLimitReached() )
			handleInvalidCompletion();

		int endLine = contextStack.getCurrentLineNumber();

		ScannerUtility.InclusionDirective directive = null;
		try
		{
			directive = parseInclusionDirective( includeLine, baseOffset );
		}
		catch( ScannerUtility.InclusionParseException ipe )
		{
			strbuff.startString();
			strbuff.append( "#include "); //$NON-NLS-1$
			strbuff.append( includeLine );
			handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, strbuff.toString(), beginningOffset, false, true );
			return;
		}
		
		if( parserMode == ParserMode.QUICK_PARSE )
		{ 
			if( requestor != null )
			{
				IASTInclusion i = null;
                try
                {
                    i = getASTFactory().createInclusion(
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
					i.enterScope( requestor, null );
					i.exitScope( requestor, null );
                }					 
			}
		}
		else
			handleInclusion(directive.getFilename().trim(), directive.useIncludePaths(), beginningOffset, startLine, directive.getStartOffset(), nameLine, directive.getEndOffset(), endLine); 
	}

	protected Map definitionsBackupMap = null; 
	
	protected void temporarilyReplaceDefinitionsMap()
	{
		definitionsBackupMap = definitions;
		definitions = Collections.EMPTY_MAP;
	}
	
	protected void restoreDefinitionsMap()
	{
		definitions = definitionsBackupMap;
		definitionsBackupMap = null;
	}


	protected boolean forInclusion = false;
	private final static IParserLogService NULL_LOG_SERVICE = new NullLogService();
	private static final String [] STRING_ARRAY = new String[0];
	private static final IToken [] EMPTY_TOKEN_ARRAY = new IToken[0];
	private static final int START_BUFFER_SIZE = 8;
	private IToken[] tokenArrayBuffer = new IToken[START_BUFFER_SIZE];
	/**
	 * @param b
	 */
	protected void setForInclusion(boolean b)
	{
		forInclusion = b;
	}

	public boolean disableMacroExpansion = false;
	
	protected IToken[] tokenizeReplacementString( int beginning, String key, String replacementString, String[] parameterIdentifiers ) 
	{
		if( replacementString.trim().equals( "" ) )  //$NON-NLS-1$
			return EMPTY_TOKEN_ARRAY;
		IToken [] macroReplacementTokens = getTokenBuffer();
		int currentIndex = 0;
		IScannerContext context = new ScannerContextTopString(replacementString, SCRATCH, true);
		contextStack.cs_push(context);
		ISourceElementRequestor savedRequestor =  requestor;
		IParserLogService savedLog = log;
		
		setTokenizingMacroReplacementList( true );
		disableMacroExpansion = true;
		requestor = NULL_REQUESTOR;
		log = NULL_LOG_SERVICE;
		
		try {
			IToken t = null;
			try {
				t = nextToken(false);
			} catch (ScannerException e) {
			} catch (EndOfFileException e) {
			}
			
			if( t == null )
				return EMPTY_TOKEN_ARRAY;
			
			try {
				while (true) {
					//each # preprocessing token in the replacement list shall be followed
					//by a parameter as the next reprocessing token in the list
					if( t.getType() == tPOUND ){
						if( currentIndex == macroReplacementTokens.length )
						{
							IToken [] doubled = new IToken[macroReplacementTokens.length * 2];
							System.arraycopy( macroReplacementTokens, 0, doubled, 0, macroReplacementTokens.length );
							macroReplacementTokens = doubled;
						}
						macroReplacementTokens[currentIndex++] = t;
						t = nextToken(false);
						if( parameterIdentifiers != null )
						{	
							int index = findIndex( parameterIdentifiers, t.getImage());
							if (index == -1 ) {
								//not found
								
								if( beginning != NO_OFFSET_LIMIT )
								{	
									strbuff.startString();
									strbuff.append( POUND_DEFINE );
									strbuff.append( key );
									strbuff.append( ' ' );
									strbuff.append( replacementString );
									handleProblem( IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, strbuff.toString(),
											beginning, false, true ); 									
									return EMPTY_TOKEN_ARRAY;
								}
							}
						}
					}
					
					if( currentIndex == macroReplacementTokens.length )
					{
						IToken [] doubled = new IToken[macroReplacementTokens.length * 2];
						System.arraycopy( macroReplacementTokens, 0, doubled, 0, macroReplacementTokens.length );
						macroReplacementTokens = doubled;
					}
					macroReplacementTokens[currentIndex++] = t;
					t = nextToken(false);
				}
			}
			catch( EndOfFileException eof )
			{
			}
			catch( ScannerException sc )
			{
			}
			
			IToken [] result = new IToken[ currentIndex ];
			System.arraycopy( macroReplacementTokens, 0, result, 0, currentIndex );
			return result;
		}
		finally {
			contextStack.cs_pop();
			setTokenizingMacroReplacementList( false );
			requestor = savedRequestor;
			log = savedLog;
			disableMacroExpansion = false;
		}
	}
	
	/**
	 * @return
	 */
	IToken[] getTokenBuffer() {
		Arrays.fill( tokenArrayBuffer, null );
		return tokenArrayBuffer;
	}

	protected IMacroDescriptor createObjectMacroDescriptor(String key, String value ) {
		IToken t = null;
		if( !value.trim().equals( "" ) )  //$NON-NLS-1$
			t = TokenFactory.createUniquelyImagedToken( IToken.tIDENTIFIER, value, this );
	
		return new ObjectMacroDescriptor( key,  
				t, 
				value);
	}

	protected void poundDefine(int beginning, int beginningLine ) throws ScannerException, EndOfFileException {
		// definition 
		String key = getNextIdentifier();
		int offset = currentContext.getOffset() - key.length();
		int nameLine = contextStack.getCurrentLineNumber();

		// store the previous definition to check against later
		IMacroDescriptor previousDefinition = getDefinition( key );
		IMacroDescriptor descriptor = null;
		// get the next character
		// the C++ standard says that macros must not put
		// whitespace between the end of the definition 
		// identifier and the opening parenthesis
		int c = getChar(false);
		if (c == '(') {
			strbuff.startString();
			c = getChar(true);
			while (c != ')') {
				if( c == '\\' ){
					c = getChar(false);
					if( c == '\r' )
						c = getChar(false);	
					
					if( c == '\n' ){
						c = getChar(false);
						continue;
					} 
					ungetChar( c );
					String line = strbuff.toString();
					strbuff.startString();
					strbuff.append( POUND_DEFINE );
					strbuff.append( line );
					strbuff.append( '\\');
					strbuff.append( c );
					handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, strbuff.toString(), beginning, false, true);
					return;
				} else if( c == '\r' || c == '\n' || c == NOCHAR ){
					String line = strbuff.toString();
					strbuff.startString();
					strbuff.append( POUND_DEFINE );
					strbuff.append( line );
					strbuff.append( '\\');
					strbuff.append( c );
					handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, strbuff.toString(), beginning, false, true );
					return;
				}
				
				strbuff.append(c);
				c = getChar(true);
			}
            
			String parameters = strbuff.toString();

			// replace StringTokenizer later -- not performant
			StringTokenizer tokenizer = new StringTokenizer(parameters, ","); //$NON-NLS-1$
			String []parameterIdentifiers =	new String[tokenizer.countTokens()];
			int ct = 0;
			while (tokenizer.hasMoreTokens()) {
				parameterIdentifiers[ ct++ ] = tokenizer.nextToken().trim();
			}

			skipOverWhitespace();

			IToken [] macroReplacementTokens = null;
			String replacementString = getRestOfPreprocessorLine();
			// TODO:  This tokenization could be done live, instead of using a sub-scanner.
			
			macroReplacementTokens = ( ! replacementString.equals( "" ) ) ?  //$NON-NLS-1$
										tokenizeReplacementString( beginning, key, replacementString, parameterIdentifiers ) :
											EMPTY_TOKEN_ARRAY;
			
			descriptor = new FunctionMacroDescriptor(
				key,
				parameterIdentifiers,
				macroReplacementTokens,
				replacementString);
				
			checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning);
			addDefinition(key, descriptor);

		}
		else if ((c == '\n') || (c == '\r'))
		{
			descriptor = createObjectMacroDescriptor(key, ""); //$NON-NLS-1$
			checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning);
			addDefinition( key, descriptor ); 
		}
		else if ((c == ' ') || (c == '\t') ) {
			// this is a simple definition 
			skipOverWhitespace();

			// get what we are to map the name to and add it to the definitions list
			String value = getRestOfPreprocessorLine();
			
			descriptor = createObjectMacroDescriptor(key, value);
			checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning);
			addDefinition( key, descriptor ); 
		
		} else if (c == '/') {
			// this could be a comment	
			c = getChar(false);
			if (c == '/') // one line comment
				{
				skipOverSinglelineComment();
				descriptor = createObjectMacroDescriptor(key, ""); //$NON-NLS-1$
				checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning);
				addDefinition(key, descriptor); 
			} else if (c == '*') // multi-line comment
				{
				if (skipOverMultilineComment()) {
					// we have gone over a newline
					// therefore, this symbol was defined to an empty string
					descriptor = createObjectMacroDescriptor(key, ""); //$NON-NLS-1$
					checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning); 
					addDefinition(key, descriptor);
				} else {
					String value = getRestOfPreprocessorLine();
					
					descriptor = createObjectMacroDescriptor(key, value);
					checkValidMacroRedefinition(key, previousDefinition, descriptor, beginning); 
					addDefinition(key, descriptor);
				}
			} else {
				// this is not a comment 
				// it is a bad statement
				StringBuffer potentialErrorMessage = new StringBuffer( POUND_DEFINE );
				potentialErrorMessage.append( key );
				potentialErrorMessage.append( " /"); //$NON-NLS-1$
				potentialErrorMessage.append( getRestOfPreprocessorLine() );
				handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, potentialErrorMessage.toString(), beginning, false, true );
				return;
			}
		} else {
			StringBuffer potentialErrorMessage = new StringBuffer( POUND_DEFINE );
			potentialErrorMessage.append( key );
			potentialErrorMessage.append( (char)c );
			potentialErrorMessage.append( getRestOfPreprocessorLine() );
			handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, potentialErrorMessage.toString(), beginning, false, true );
			return;
		}
		
		try
        {
			getASTFactory().createMacro( key, beginning, beginningLine, offset, offset + key.length(), nameLine, currentContext.getOffset(), contextStack.getCurrentLineNumber(), descriptor ).acceptElement( requestor, null );
        }
        catch (Exception e)
        {
            /* do nothing */
        } 
	}

	protected void checkValidMacroRedefinition(
		String key,
		IMacroDescriptor previousDefinition,
		IMacroDescriptor newDefinition, int beginningOffset )
		throws ScannerException 
		{
			if( parserMode != ParserMode.QUICK_PARSE && previousDefinition != null ) 
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
        
		// split params up into single arguments
        int nParen = 0;
        Vector parameters = new Vector();
        strbuff.startString();
		for (int i = 0; i < params.length(); i++) {
			char c = params.charAt(i);
			switch (c) {
				case '(' :
					nParen++;
					break;
				case ')' :
					nParen--;
					break;
				case ',' :
					if (nParen == 0) {
						parameters.add(strbuff.toString());
						strbuff.startString();
						continue;
					}
					break;					
				default :
					break;
			}
			strbuff.append( c );
		}
		parameters.add(strbuff.toString());
		
		setThrowExceptionOnBadCharacterRead(false);
		ISourceElementRequestor savedRequestor =  requestor;
		IParserLogService savedLog = log;
		log = NULL_LOG_SERVICE;
		requestor = NULL_REQUESTOR;
		
		
        Vector parameterValues = new Vector();
		for (int i = 0; i < parameters.size(); i++) {
			IScannerContext context = new ScannerContextTopString((String)parameters.elementAt(i), TEXT, true);
	        contextStack.cs_push(context);     
	        IToken t = null;
	        StringBuffer strBuff2 = new StringBuffer();
	        boolean space = false;
	       
	        try {
	            while (true) {
					int c = getCharacter();
					if ((c != ' ') && (c != '\t') && (c != '\r') && (c != '\n')) {
						space = false;
					}
					if (c != NOCHAR) ungetChar(c);
					t = (forStringizing ? nextTokenForStringizing() : nextToken(false));
	
	                if (space)
	                    strBuff2.append( ' ' );
	
	                switch (t.getType()) {
	                    case IToken.tSTRING :
	                    	strBuff2.append('\"');
	                    	strBuff2.append(t.getImage());
	                    	strBuff2.append('\"'); 
	                    	break;
	                    case IToken.tLSTRING :
	                    	strBuff2.append( "L\""); //$NON-NLS-1$
	                    	strBuff2.append(t.getImage());
	                    	strBuff2.append('\"');	
	                    	break;
	                    case IToken.tCHAR :    
	                    	strBuff2.append('\'');
	                    	strBuff2.append(t.getImage());
	                    	strBuff2.append('\''); 
	                    	break;
	                    default :             
	                    	strBuff2.append( t.getImage()); 
	                    	break;
	                }
	                space = true;
	            }
	        }
	        catch (EndOfFileException e) {
	            // Good
	        	contextStack.cs_pop();     	
	            parameterValues.add(strBuff2.toString());
	        }
		}
		setThrowExceptionOnBadCharacterRead(true);
		requestor = savedRequestor;
		log = savedLog;
        return parameterValues;
    }
    
	protected void expandDefinition(String symbol, String expansion, int symbolOffset ) throws ScannerException
	{
		expandDefinition( symbol, 
				new ObjectMacroDescriptor( 	symbol,
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
				contextStack.updateMacroContext( 
					replacementValue, 
					symbol,
					requestor,
					symbolOffset, 
					symbol.length());
			}
			catch (ContextException e)
			{
				handleProblem( e.getId(), currentContext.getContextName(), getCurrentOffset(), false, true );
				consumeUntilOutOfMacroExpansion();
				return;
			}
		} else if (expansion.getMacroType() == MacroType.FUNCTION_LIKE ) {
			skipOverWhitespace();
			int c = getChar(false);

			if (c == '(') {
				strbuff.startString();
				int bracketCount = 1;
				c = getChar(false);

				while (true) {
					if (c == '(')
						++bracketCount;
					else if (c == ')')
						--bracketCount;

					if(bracketCount == 0 || c == NOCHAR)
						break;
					strbuff.append(c);
					c = getChar( true );
				}
                
				
				String betweenTheBrackets = strbuff.toString().trim();
                
                if (expansion.getExpansionSignature() == EMPTY_STRING) {
                	return;  //
                }
                // Position of the closing ')'
                int endMacroOffset = lastContext.getOffset()  - 1;

                Vector parameterValues = getMacroParameters(betweenTheBrackets, false);
                Vector parameterValuesForStringizing = null;
                SimpleToken t = null;
                
				// create a string that represents what needs to be tokenized
				
				IToken [] tokens = expansion.getTokenizedExpansion();
				String [] parameterNames = expansion.getParameters();

				if (parameterNames.length != parameterValues.size())
				{ 
					handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, symbol, getCurrentOffset(), false, true  );	
					consumeUntilOutOfMacroExpansion();
					return;
				}				

				strbuff.startString();
				
				int numberOfTokens = tokens.length;

				for (int i = 0; i < numberOfTokens; ++i) {
					t = (SimpleToken) tokens[i];
					if (t.getType() == IToken.tIDENTIFIER) {

						// is this identifier in the parameterNames
						// list? 
						int index = findIndex( parameterNames, t.getImage() );
						if (index == -1 ) {
							// not found
							// just add image to buffer
							strbuff.append(t.getImage() );
						} else {
							strbuff.append(
								(String) parameterValues.elementAt(index) );
						}
					} else if (t.getType() == tPOUND) {
						//next token should be a parameter which needs to be turned into
						//a string literal
						if( parameterValuesForStringizing == null)
						{
							String cache = strbuff.toString();
							parameterValuesForStringizing = getMacroParameters(betweenTheBrackets, true);
							strbuff.startString();
							strbuff.append(cache);
						}
						++i;
						if( tokens.length == i ){
							handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, expansion.getName(), getCurrentOffset(), false, true );
							return;
						} 
							
						t = (SimpleToken) tokens[ i ];
						int index = findIndex( parameterNames, t.getImage());
						if( index == -1 ){
							handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, expansion.getName(), getCurrentOffset(), false, true );
							return;
						} 
						strbuff.append('\"');
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
								strbuff.append(' ');
							} 
							//a \ character is inserted before each " and \
							if( ch == '\"' || ch == '\\' ){
								strbuff.append('\\');
								strbuff.append(ch);
							} else {
								strbuff.append(ch);
							}
						}
						strbuff.append('\"');
						
					} else {
						switch( t.getType() )
						{
							case IToken.tSTRING:
								strbuff.append('\"');
								strbuff.append(t.getImage());
								strbuff.append('\"');  
								break;
							case IToken.tLSTRING: 
								strbuff.append("L\""); //$NON-NLS-1$
								strbuff.append(t.getImage());
								strbuff.append('\"');  
								break;
							case IToken.tCHAR:	 
								strbuff.append('\'');
								strbuff.append(t.getImage());
								strbuff.append('\'');  
								
								break;
							default:			 
								strbuff.append(t.getImage());				
								break;
						}
					}
					
					boolean pastingNext = false;
					
					if( i != numberOfTokens - 1)
					{
						IToken t2 = tokens[i+1];
						if( t2.getType() == tPOUNDPOUND ) {
							pastingNext = true;
							i++;
						}  
					}
					
					if( t.getType() != tPOUNDPOUND && ! pastingNext )
						if (i < (numberOfTokens-1)) // Do not append to the last one 
                        	strbuff.append( ' ' ); 
				}
				String finalString = strbuff.toString();
				try
				{
					contextStack.updateMacroContext(
						finalString,
						expansion.getName(),
						requestor,
						symbolOffset, 
						endMacroOffset - symbolOffset + 1 );
				}
				catch (ContextException e)
				{
					handleProblem( e.getId(), currentContext.getContextName(), getCurrentOffset(), false, true );
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
			TraceUtil.outputTrace(log, "Unexpected type of MacroDescriptor stored in definitions table: ", null, expansion.getMacroType().toString(), null, null); //$NON-NLS-1$
		}

	}

	/**
	 * @param parameterNames
	 * @param image
	 * @return
	 */
	private int findIndex(String[] parameterNames, String image) {
		for( int i = 0; i < parameterNames.length; ++i )
			if( parameterNames[i].equals( image ) )
				return i;

		return -1;
	}

	protected String handleDefinedMacro() throws ScannerException {
		int o = getCurrentOffset();
		skipOverWhitespace();

		int c = getChar(false);
		
		String definitionIdentifier = null;
		if (c == '(') {

			definitionIdentifier = getNextIdentifier(); 
			skipOverWhitespace(); 
			c = getChar(false);
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
		astFactory = f;
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
		return Collections.unmodifiableMap(definitions);
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
		return ( currentContext.getKind() == IScannerContext.ContextKind.TOP );
	}	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getCurrentFilename()
	 */
	public char[] getCurrentFilename() {
		return getCurrentFile().toCharArray();
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "Scanner @"); //$NON-NLS-1$
		if( currentContext != null )
			buffer.append( currentContext.toString());
		else
			buffer.append( "EOF"); //$NON-NLS-1$
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getCurrentFileIndex()
	 */
	public int getCurrentFileIndex() {
		return contextStack.getMostRelevantFileContextIndex();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getFilenameForIndex(int)
	 */
	public String getFilenameForIndex(int index) {
		if( index < 0 ) return EMPTY_STRING;
		return contextStack.getInclusionFilename(index);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getASTFactory()
	 */
	public IASTFactory getASTFactory() {
		if( astFactory == null )
			astFactory = ParserFactory.createASTFactory( this, parserMode, language );
		return astFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getBranchTracker()
	 */
	public BranchTracker getBranchTracker() {
		return branches;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getInitialReader()
	 */
	public CodeReader getInitialReader() {
		return reader;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getOriginalConfig()
	 */
	public IScannerInfo getOriginalConfig() {
		return originalConfig;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getProblemFactory()
	 */
	public IProblemFactory getProblemFactory() {
		return problemFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getScanner()
	 */
	public IScanner getScanner() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#setDefinitions(java.util.Map)
	 */
	public void setDefinitions(Map map) {
		definitions = map;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#setIncludePathNames(java.util.List)
	 */
	public void setIncludePathNames(List includePathNames) {
		this.includePathNames = includePathNames;
	}
	
	public Map getFileCache() {
		return fileCache;
	}
}
