/*
 * Created on Dec 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.internal.core.parser.Parser;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ContextualParser extends Parser implements IParser {

	private CompletionKind kind;
	private IASTScope scope;
	private IASTNode context;

	/**
	 * @param scanner
	 * @param callback
	 * @param language
	 * @param log
	 */
	public ContextualParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log) {
		super(scanner, callback, language, log);
		astFactory = ParserFactory.createASTFactory( ParserMode.COMPLETE_PARSE, language);
		scanner.setASTFactory(astFactory);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParserNotImplementedException {
		scanner.setOffsetBoundary(offset);
		translationUnit();
		return new CompletionNode( getCompletionKind(), getCompletionScope(), getCompletionContext(), getCompletionPrefix() );
	}

	/**
	 * @return
	 */
	private String getCompletionPrefix() {
		return lastToken == null ? "" : lastToken.getImage();
	}

	/**
	 * @return
	 */
	private IASTNode getCompletionContext() {
		return context;
	}

	/**
	 * @return
	 */
	private IASTScope getCompletionScope() {
		return scope;
	}

	/**
	 * @return
	 */
	private IASTCompletionNode.CompletionKind getCompletionKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public IASTNode parse(int startingOffset, int endingOffset) throws ParserNotImplementedException {
		scanner.setOffsetBoundary(endingOffset);
		translationUnit();
		return getCompletionContext();
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCurrentScope(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	protected void setCurrentScope(IASTScope scope) {
		this.scope = scope;
	}
	
	protected void setCompletionContext( IASTNode node )
	{
		this.context = node;
	}
	
	protected void setCompletionKind( IASTCompletionNode.CompletionKind kind )
	{
		this.kind = kind;
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
