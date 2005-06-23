/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.internal.core.parser.ast.SymbolIterator;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableError;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.TypeFilter;

/**
 * @author aniefer
 */
public class ASTNode implements IASTNode {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind, org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public ILookupResult lookup(String prefix, LookupKind[] kind, IASTNode context, IASTExpression functionParameters) throws LookupError, ASTNotImplementedException {

		if( ! ( this instanceof ISymbolOwner ) ){
			return null;
		}
		
		IExtensibleSymbol symbol = ((ISymbolOwner)this).getSymbol();
		if( symbol == null || !(symbol instanceof IContainerSymbol) ){
			throw new LookupError();
		}
		IContainerSymbol thisContainer = (IContainerSymbol) symbol; 
		IContainerSymbol qualification = ( context != null ) ? ((ASTNode)context).getLookupQualificationSymbol() : null;
		
		// trying to dereference a context of unknown type
		if (context != null && qualification == null)
			return null;
		
		List parameters = createLookupParameterList( functionParameters );
		
		int paramIndex = ( parameters != null ) ? parameters.size() : 0;
		
		if( thisContainer.getSymbolTable().getParserMode() != ParserMode.COMPLETION_PARSE ){
			throw new ASTNotImplementedException();
		}
		
		TypeFilter filter = new TypeFilter();
		if( kind != null ){
			for( int i = 0; i < kind.length; i++ ){
				filter.addAcceptedType( kind[i] );
				if( kind[i] == LookupKind.THIS ){
					filter.setLookingInThis( true );
					if( kind.length == 1 ){
						filter.addAcceptedType( LookupKind.ALL );
					}
				} else {
					filter.addAcceptedType( kind[i] );
				}
			}	
		} else {
			filter.addAcceptedType( LookupKind.ALL );
		}
		
		List lookupResults = performPrefixLookup(prefix.toCharArray(), thisContainer, qualification, filter, parameters);
		
		if(lookupResults == null)
			return null;
		
		//filter out things that are not visible and things that don't have AST nodes attached
		ListIterator iter = lookupResults.listIterator();
		while( iter.hasNext() ){
			ISymbol s = (ISymbol) iter.next();
			if( !thisContainer.isVisible( s, qualification ) ||
			    s.getASTExtension() == null ||
				s.getASTExtension().getPrimaryDeclaration() == null )
			{
				iter.remove();
				continue;
			}
			
			if( context != null && ((ASTNode)context).shouldFilterLookupResult( s ) )
				iter.remove();
			
		}
		
		SymbolIterator iterator = new SymbolIterator( lookupResults.iterator() );

		return new Result( prefix, iterator, lookupResults.size(), paramIndex );
	}
	
	/**
	 * @param prefix
	 * @param thisContainer
	 * @param qualification
	 * @param filter
	 * @param paramList TODO
	 * @param lookInThis
	 * @param lookupResults
	 * @return
	 * @throws LookupError
	 */
	protected List performPrefixLookup(char[] prefix, IContainerSymbol thisContainer, IContainerSymbol qualification, TypeFilter filter, List paramList) throws LookupError {
		List results = null;
		try {
			if( qualification != null ){
				results = qualification.prefixLookup( filter, prefix, true, paramList );
			} else {
				results = thisContainer.prefixLookup( filter, prefix, false, paramList );
			}
		} catch (ParserSymbolTableException e) {
			throw new LookupError();
		} catch (ParserSymbolTableError e ){
			throw new LookupError();
		}
		return results;
	}

	/**
	 * @param context
	 * @param qualification
	 * @return
	 * @throws LookupError
	 */
	public IContainerSymbol getLookupQualificationSymbol() throws LookupError {
		throw new LookupError();
	}
	
	public boolean shouldFilterLookupResult( ISymbol symbol ){
		return false;
	}

	public List createLookupParameterList( IASTExpression parameterExpression ){
		if( parameterExpression == null )
			return null;
		
		List params = new ArrayList();
		ASTExpression exp = (ASTExpression) parameterExpression;
		while( exp != null ){
			params.add( exp.getResultType().getResult() );
			exp = (ASTExpression) exp.getRHSExpression();
		}
		return params;
	}
	
	private class Result implements ILookupResult{
		private String prefix;
		private Iterator iterator;
		private int resultsNumber;
		private int parameterIndex;

		public Result( String pref, Iterator iter, int resultsSize, int paramIndex ){
			prefix = pref;
			iterator = iter;
			resultsNumber = resultsSize;
			parameterIndex = paramIndex;
		}
		
		public String getPrefix() 	{	return prefix;	 }
		public Iterator getNodes() 	{	return iterator; }
		public int getResultsSize() { return resultsNumber; }
		public int getIndexOfNextParameter() { return parameterIndex; }
	}
}
