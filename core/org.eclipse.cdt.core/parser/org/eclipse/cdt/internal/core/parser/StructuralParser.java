/*
 * Created on Dec 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.Backtrack;
import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class StructuralParser extends Parser implements IParser {

	/**
	 * @param scanner
	 * @param ourCallback
	 * @param language
	 * @param logService
	 */
	public StructuralParser(IScanner scanner, ISourceElementRequestor ourCallback, ParserLanguage language, IParserLogService logService) {
		super(scanner, ourCallback, language, logService);
		astFactory = ParserFactory.createASTFactory( ParserMode.COMPLETE_PARSE, language);
		scanner.setASTFactory(astFactory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleFunctionBody(org.eclipse.cdt.core.parser.ast.IASTScope, boolean)
	 */
	protected void handleFunctionBody(
		IASTScope scope,
		boolean isInlineFunction)
		throws Backtrack, EndOfFile {
		skipOverCompoundStatement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#catchBlockCompoundStatement(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	protected void catchBlockCompoundStatement(IASTScope scope)
		throws Backtrack, EndOfFile {
		skipOverCompoundStatement();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParserNotImplementedException {
		throw new ParserNotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public IASTNode parse(int startingOffset, int endingOffset) throws ParserNotImplementedException {
		throw new ParserNotImplementedException();
	}
	

}
