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
package org.eclipse.cdt.internal.core.parser;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.token.OffsetDuple;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author jcamelon
 */
public class SelectionParser extends ContextualParser {

	private OffsetDuple offsetRange;
	private IToken firstTokenOfDuple = null, lastTokenOfDuple = null;
	private IASTScope ourScope = null;
	private IASTCompletionNode.CompletionKind ourKind = null;
	private IASTNode ourContext = null;
	private ITokenDuple greaterContextDuple = null;
	private boolean pastPointOfSelection = false;
	private IASTNode contextNode = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleNewToken(org.eclipse.cdt.core.parser.IToken)
	 */
	protected void handleNewToken(IToken value) {
		if( value != null && scanner.isOnTopContext() )
		{
			TraceUtil.outputTrace(log, "IToken provided w/offsets ", null, value.getOffset(), " & ", value.getEndOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
			boolean change = false;
			if( value.getOffset() == offsetRange.getFloorOffset() )
			{
				TraceUtil.outputTrace(log, "Offset Floor Hit w/token \"", null, value.getCharImage(), "\"", null ); //$NON-NLS-1$ //$NON-NLS-2$
				firstTokenOfDuple = value;
				change = true;
			}
			if( value.getEndOffset() == offsetRange.getCeilingOffset() )
			{
				TraceUtil.outputTrace(log, "Offset Ceiling Hit w/token \"", null, value.getCharImage(), "\"", null ); //$NON-NLS-1$ //$NON-NLS-2$
				change = true;
				lastTokenOfDuple = value;
			}
			if( change && tokenDupleCompleted() )
			{
				if ( ourScope == null )
					ourScope = getCompletionScope();
				if( ourContext == null )
					ourContext = getCompletionContext();
				if( ourKind == null )
					ourKind = getCompletionKind();
			}
		}
	}

	
	
	
	/**
	 * @return
	 */
	protected boolean tokenDupleCompleted() {
		return lastTokenOfDuple != null && lastTokenOfDuple.getEndOffset() >= offsetRange.getCeilingOffset();
	}




	/**
	 * @param scanner
	 * @param callback
	 * @param mode
	 * @param language
	 * @param log
	 */
	public SelectionParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log, IParserExtension extension ) {
		super(scanner, callback, language, log,extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public ISelectionParseResult parse(int startingOffset, int endingOffset) {
		offsetRange = new OffsetDuple( startingOffset, endingOffset );
		translationUnit();
		return reconcileTokenDuple();
	}

	/**
	 * 
	 */
	protected ISelectionParseResult reconcileTokenDuple() throws ParseError {
		if( firstTokenOfDuple == null || lastTokenOfDuple == null )
			throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		
		if( getCompletionKind() == IASTCompletionNode.CompletionKind.UNREACHABLE_CODE )
			throw new ParseError( ParseError.ParseErrorKind.OFFSETDUPLE_UNREACHABLE );
		
		ITokenDuple duple = TokenFactory.createTokenDuple( firstTokenOfDuple, lastTokenOfDuple );
		
		if( ! duple.syntaxOfName() )
			throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		
		return provideSelectionNode(duple);
	}

	/**
	 * @param duple
	 * @return
	 */
	protected ISelectionParseResult provideSelectionNode(ITokenDuple duple) {
		
		ITokenDuple finalDuple = null;
		// reconcile the name to look up first
		if( ! duple.equals( greaterContextDuple ))
		{
			// 3 cases			
			// duple is prefix of greaterContextDuple
			// or duple is suffix of greaterContextDuple
			// duple is a sub-duple of greaterContextDuple
			if( duple.getFirstToken().equals( greaterContextDuple.getFirstToken() ))
				finalDuple = duple; //	=> do not use greaterContextDuple
			else if( duple.getLastToken().equals( greaterContextDuple.getLastToken() ))
				finalDuple = greaterContextDuple; //  => use greaterContextDuple
			else
				throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		}
		else
			finalDuple = greaterContextDuple;
		
		IASTNode node = lookupNode(finalDuple);
		if( node == null ) return null;
		if( !(node instanceof IASTOffsetableNamedElement )) return null;
		return new SelectionParseResult( (IASTOffsetableNamedElement) node, new String( ((IASTOffsetableElement)node).getFilename() )); 
	}




	/**
	 * @param finalDuple
	 * @return
	 */
	protected IASTNode lookupNode(ITokenDuple finalDuple) {
		if( contextNode == null ) return null;
		if( contextNode instanceof IASTDeclaration )
		{
			if( contextNode instanceof IASTOffsetableNamedElement )
			{
				if( ((IASTOffsetableNamedElement)contextNode).getName().equals( finalDuple.toString() ) )
					return contextNode;
			}
			if( contextNode instanceof IASTQualifiedNameElement )
			{
				String [] elementQualifiedName = ((IASTQualifiedNameElement)contextNode).getFullyQualifiedName();
				if( Arrays.equals( elementQualifiedName, finalDuple.toQualifiedName() ) )
					return contextNode;
			}
			try {
				if( ourKind == IASTCompletionNode.CompletionKind.NEW_TYPE_REFERENCE )
				{
					if( contextNode instanceof IASTVariable )
					{
						IASTInitializerClause initializer = ((IASTVariable)contextNode).getInitializerClause();
						if( initializer != null )
						{
							IASTExpression ownerExpression = initializer.findExpressionForDuple( finalDuple );
							return astFactory.lookupSymbolInContext( ourScope, finalDuple, ownerExpression );
						}
					}
				}
					
				return astFactory.lookupSymbolInContext( ourScope, finalDuple, null );
			} catch (ASTNotImplementedException e) {
				return null;
			}
		}
		else if( contextNode instanceof IASTExpression )
		{
			try {
				return astFactory.lookupSymbolInContext( ourScope, finalDuple, contextNode );
			} catch (ASTNotImplementedException e) {
				return null;
			}
		}
		return null;
	}




	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );	
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#checkEndOfFile()
	 */
	protected void checkEndOfFile() throws EndOfFileException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setGreaterNameContext(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	protected void setGreaterNameContext(ITokenDuple tokenDuple) {
		
		if( pastPointOfSelection ) return;
		if( greaterContextDuple == null && scanner.isOnTopContext() && lastTokenOfDuple != null && firstTokenOfDuple != null )
		{
			if( tokenDuple.getStartOffset() > lastTokenOfDuple.getEndOffset() )
			{
				pastPointOfSelection = true;
				return;
			}
			int tokensFound = 0;
			Iterator i = tokenDuple.iterator();
			while( i.hasNext() )
			{
				IToken token = (IToken) i.next();
				if( token == firstTokenOfDuple ) ++tokensFound;
				if( token == lastTokenOfDuple ) ++tokensFound;
			}
			if( tokensFound == 2 )
			{
				greaterContextDuple = tokenDuple;
				pastPointOfSelection = true;
			}
			
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#endDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	protected void endDeclaration(IASTDeclaration declaration)
			throws EndOfFileException {
		if( ! tokenDupleCompleted() )
			super.endDeclaration(declaration);
		else
		{
			contextNode = declaration;
			throw new EndOfFileException();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#endExpressionStatement(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	protected void endExpression(IASTExpression expression)
			throws EndOfFileException {
		if( ! tokenDupleCompleted() )
			super.endExpression(expression);
		else
		{
			contextNode = expression;
			throw new EndOfFileException();
		}
		
	}

	
	public static class SelectionParseResult implements ISelectionParseResult 
	{

		public SelectionParseResult( IASTOffsetableNamedElement node, String fileName )
		{
			this.node = node;
			this.fileName = fileName;
		}
		
		private final String fileName;
		private final IASTOffsetableNamedElement node;

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.IParser.ISelectionParseResult#getNode()
		 */
		public IASTOffsetableNamedElement getOffsetableNamedElement() {
			return node;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.IParser.ISelectionParseResult#getFilename()
		 */
		public String getFilename() {
			return fileName;
		}
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#endEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerator)
	 */
	protected void endEnumerator(IASTEnumerator enumerator) throws EndOfFileException {
		if( ! tokenDupleCompleted() )
			super.endEnumerator(enumerator);
		else
		{
			contextNode = enumerator;
			throw new EndOfFileException();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#endClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	protected void handleClassSpecifier(IASTClassSpecifier classSpecifier)
			throws EndOfFileException {
		if( ! tokenDupleCompleted() )
			super.handleClassSpecifier(classSpecifier);
		else
		{
			contextNode = classSpecifier;
			throw new EndOfFileException();
		}
	}
	
	protected void handleEnumeration(IASTEnumerationSpecifier enumeration) throws EndOfFileException {
		if( ! tokenDupleCompleted() )
			super.handleEnumeration(enumeration);
		else
		{
			contextNode = enumeration;
			throw new EndOfFileException();
		}
	}
}
