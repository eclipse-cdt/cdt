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

import java.io.Reader;

import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.LineOffsetReconciler;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.Preprocessor;
import org.eclipse.cdt.internal.core.parser.QuickParseCallback;
import org.eclipse.cdt.internal.core.parser.Scanner;
import org.eclipse.cdt.internal.core.parser.ast.complete.CompleteParseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.quick.QuickParseASTFactory;


/**
 * @author jcamelon
 *
 */
public class ParserFactory {

	public static IASTFactory createASTFactory( ParserMode mode, ParserLanguage language )
	{
		if( mode == ParserMode.QUICK_PARSE )
			return new QuickParseASTFactory(); 
		else
			return new CompleteParseASTFactory( language ); 
	}
	
    public static IParser createParser( IScanner scanner, ISourceElementRequestor callback, ParserMode mode, ParserLanguage language, IParserLogService log ) throws ParserFactoryException
    {
    	if( scanner == null ) throw new ParserFactoryException( ParserFactoryException.Kind.NULL_SCANNER );
		if( language == null ) throw new ParserFactoryException( ParserFactoryException.Kind.NULL_LANGUAGE );
		IParserLogService logService = ( log == null ) ? createDefaultLogService() : log;
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode ); 
		ISourceElementRequestor ourCallback = (( callback == null) ? new NullSourceElementRequestor() : callback );   
		return new Parser( scanner, ourCallback, ourMode, language, logService );
    }
 	 	
    public static IScanner createScanner( Reader input, String fileName, IScannerInfo config, ParserMode mode, ParserLanguage language, ISourceElementRequestor requestor, IParserLogService log ) throws ParserFactoryException
    {
    	if( input == null ) throw new ParserFactoryException( ParserFactoryException.Kind.NULL_READER );
    	if( fileName == null ) throw new ParserFactoryException( ParserFactoryException.Kind.NULL_FILENAME );
    	if( config == null ) throw new ParserFactoryException( ParserFactoryException.Kind.NULL_CONFIG );
    	if( language == null ) throw new ParserFactoryException( ParserFactoryException.Kind.NULL_LANGUAGE );
    	IParserLogService logService = ( log == null ) ? createDefaultLogService() : log;
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode );
		ISourceElementRequestor ourRequestor = (( requestor == null) ? new NullSourceElementRequestor() : requestor ); 
		IScanner s = new Scanner( input, fileName, config, ourRequestor, ourMode, language, logService );
		return s; 
    }
    
    public static IPreprocessor createPreprocessor( Reader input, String fileName, IScannerInfo info, ParserMode mode, ParserLanguage language, ISourceElementRequestor requestor, IParserLogService logService )
    {
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode ); 
		ISourceElementRequestor ourRequestor = (( requestor == null) ? new NullSourceElementRequestor() : requestor );
		IPreprocessor s = new Preprocessor( input, fileName, info, ourRequestor, ourMode, language, logService );
		return s;
    }
	
	public static ILineOffsetReconciler createLineOffsetReconciler( Reader input )
	{
		return new LineOffsetReconciler( input ); 
	}
	
	public static IQuickParseCallback createQuickParseCallback()
	{
		return new QuickParseCallback();
	}
		
	public static IParserLogService createDefaultLogService()
	{
		return defaultLogService;
	}
	
	private static IParserLogService defaultLogService = new DefaultLogService();
}
