/*
 * Created on Dec 5, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.Backtrack;
import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CompleteParser extends Parser {

	/**
	 * @param scanner
	 * @param callback
	 * @param mode
	 * @param language
	 * @param log
	 */
	public CompleteParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log) {
		super(scanner, callback, language, log);
		astFactory = ParserFactory.createASTFactory( ParserMode.COMPLETE_PARSE, language);
		scanner.setASTFactory(astFactory);
	}
	
	protected void handleFunctionBody(IASTScope scope, boolean isInlineFunction) throws Backtrack, EndOfFile
	{
		if ( isInlineFunction ) 
			skipOverCompoundStatement();
		else
			functionBody(scope);
	}
	
	protected void catchBlockCompoundStatement(IASTScope scope) throws Backtrack, EndOfFile 
	{
		compoundStatement(scope, true);
	}
	
}
