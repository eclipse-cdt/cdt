/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.extension.ExtensionDialect;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.core.parser.extension.IParserExtensionFactory;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserExtensionFactory;
import org.eclipse.cdt.internal.core.parser.QuickParseCallback;
import org.eclipse.cdt.internal.core.parser.StructuralParseCallback;
import org.eclipse.cdt.internal.core.parser.ast.complete.CompleteParseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.quick.QuickParseASTFactory;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.Scanner2;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;


/**
 * @author jcamelon
 *
 */
public class ParserFactory {
	
	private static IParserExtensionFactory extensionFactory = new ParserExtensionFactory( ExtensionDialect.GCC );
	
	public static IASTFactory createASTFactory( ParserMode mode, ParserLanguage language )
	{
		if( mode == ParserMode.QUICK_PARSE )
			return new QuickParseASTFactory(extensionFactory.createASTExtension( mode ));
		return new CompleteParseASTFactory( language, mode, extensionFactory.createASTExtension( mode )); 
	}
	
	
	
    /**
     * @param scanner		tokenizer to retrieve C/C++ tokens
     * @param callback		the callback that reports results to the client
     * @param mode			the parser mode you wish to use
     * @param language		C or C++
     * @param log			a log utility to output errors
     * @return
     * @throws ParserFactoryError - erroneous input provided
     */
    public static IParser createParser( IScanner scanner, ISourceElementRequestor callback, ParserMode mode, ParserLanguage language, IParserLogService log ) throws ParserFactoryError
    {
    	if( scanner == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_SCANNER );
		if( language == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_LANGUAGE );
		IParserLogService logService = ( log == null ) ? createDefaultLogService() : log;
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode ); 
		ISourceElementRequestor ourCallback = (( callback == null) ? new NullSourceElementRequestor() : callback );
		IParserExtension extension = extensionFactory.createParserExtension();
		return new Parser( scanner, ourMode, ourCallback, language, logService, extension );
    }
 	 	
    /**
     * @param input			the java.io.Reader that reads the source-code input you want parsed
     * @param fileName		the absolute path of the file you are parsing (necessary for determining location of local inclusions) 
     * @param config		represents the include-paths and preprocessor definitions you wish to initialize the scanner with
     * @param mode			the parser mode you wish to use
     * @param language		C or C++
     * @param requestor		the callback that reports results to the client
     * @param log			a log utility to output errors
     * @param workingCopies	a java.util.List of IWorkingCopy buffers if you wish for include files to use CDT Working Copies rather than saved files
     * @return
     * @throws ParserFactoryError - erroneous input provided
     */
    public static IScanner createScanner( CodeReader code, IScannerInfo config, ParserMode mode, ParserLanguage language, ISourceElementRequestor requestor, IParserLogService log, List workingCopies ) throws ParserFactoryError
    {
    	if( config == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_CONFIG );
    	if( language == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_LANGUAGE );
    	IParserLogService logService = ( log == null ) ? createDefaultLogService() : log;
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode );
		ISourceElementRequestor ourRequestor = (( requestor == null) ? new NullSourceElementRequestor() : requestor );
		IScannerConfiguration configuration  = null;
		if( language == ParserLanguage.C )
		    configuration = new GCCScannerConfiguration();
		else
		    configuration = new GPPScannerConfiguration();

		return new Scanner2( code, config, ourRequestor, ourMode, language, logService, workingCopies, configuration );
    }
    
    public static IScanner createScanner( String fileName, IScannerInfo config, ParserMode mode, ParserLanguage language, ISourceElementRequestor requestor, IParserLogService log, List workingCopies ) throws ParserFactoryError, IOException
	{
    	return createScanner(new CodeReader(fileName), config, mode, language, requestor, log, workingCopies);
	}
	
	public static IQuickParseCallback createQuickParseCallback()
	{
		return new QuickParseCallback();
	}

	public static IQuickParseCallback createStructuralParseCallback()
	{
		return new StructuralParseCallback();
	}
	
	public static IParserLogService createDefaultLogService()
	{
		return defaultLogService;
	}
	
	public static Set getKeywordSet( KeywordSetKey key, ParserLanguage language )
	{
		return KeywordSets.getKeywords( key, language ); 
	}
	
	private static IParserLogService defaultLogService = new DefaultLogService();
}
