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
import java.util.Map;

import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.DefaultErrorHandlingPolicies;
import org.eclipse.cdt.internal.core.parser.LineOffsetReconciler;
import org.eclipse.cdt.internal.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.Preprocessor;
import org.eclipse.cdt.internal.core.parser.QuickParseCallback;
import org.eclipse.cdt.internal.core.parser.Scanner;
import org.eclipse.cdt.internal.core.parser.TranslationOptions;
import org.eclipse.cdt.internal.core.parser.TranslationResult;
import org.eclipse.cdt.internal.core.parser.ast.complete.CompleteParseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.quick.QuickParseASTFactory;
import org.eclipse.cdt.internal.core.parser.problem.DefaultProblemFactory;
import org.eclipse.cdt.internal.core.parser.problem.ProblemReporter;


/**
 * @author jcamelon
 *
 */
public class ParserFactory {

	public static IASTFactory createASTFactory( ParserMode mode )
	{
		if( mode == ParserMode.QUICK_PARSE )
			return new QuickParseASTFactory(); 
		else
			return new CompleteParseASTFactory(); 
	}
	
    public static IParser createParser( IScanner scanner, ISourceElementRequestor callback, ParserMode mode )
    {
        return createParser(scanner, callback, mode, null, null);
    }
 	
	public static IParser createParser( IScanner scanner, ISourceElementRequestor callback, ParserMode mode, IProblemReporter problemReporter, ITranslationResult unitResult )
	{
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode ); 
		ISourceElementRequestor ourCallback = (( callback == null) ? new NullSourceElementRequestor() : callback );   
		return new Parser( scanner, ourCallback, ourMode, problemReporter, unitResult );
	}
 	
    public static IScanner createScanner( Reader input, String fileName, IScannerInfo config, ParserMode mode, ISourceElementRequestor requestor )
    {
        return createScanner(input, fileName, config, mode, requestor, null, null);
    }
    
	public static IScanner createScanner( Reader input, String fileName, IScannerInfo config, ParserMode mode, ISourceElementRequestor requestor, IProblemReporter problemReporter, ITranslationResult unitResult ) 
	{
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode );
		ISourceElementRequestor ourRequestor = (( requestor == null) ? new NullSourceElementRequestor() : requestor ); 
		IScanner s = new Scanner( input, fileName, config, problemReporter, unitResult, ourRequestor, ourMode );
		return s; 
	}
    
    public static IPreprocessor createPreprocessor( Reader input, String fileName, IScannerInfo info, ParserMode mode, ISourceElementRequestor requestor )
    {
        return createPreprocessor(input, fileName, info, mode, requestor, null, null);
    }
 	
	public static IPreprocessor createPreprocessor( Reader input, String fileName, IScannerInfo info, ParserMode mode, ISourceElementRequestor requestor, IProblemReporter problemReporter, ITranslationResult unitResult )
	{
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode ); 
		ISourceElementRequestor ourRequestor = (( requestor == null) ? new NullSourceElementRequestor() : requestor );
		IPreprocessor s = new Preprocessor( input, fileName, info, ourRequestor, problemReporter, unitResult, ourMode );
		return s;
	} 
	
	public static ILineOffsetReconciler createLineOffsetReconciler( Reader input )
	{
		return new LineOffsetReconciler( input ); 
	}
	
	public static IProblemReporter createProblemReporter( Map options )
	{			 
		ITranslationOptions cOptions = new TranslationOptions(options);
		IProblemReporter problemReporter = new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
				cOptions, 
				new DefaultProblemFactory()
		);
		
		return problemReporter; 
	}
	
	public static ITranslationResult createTranslationResult( String fileName/*ITranslationUnit tu*/ )
	{
		return new TranslationResult( fileName /* tu */ );
	}
	
	public static IQuickParseCallback createQuickParseCallback()
	{
		return new QuickParseCallback();
	}
}
