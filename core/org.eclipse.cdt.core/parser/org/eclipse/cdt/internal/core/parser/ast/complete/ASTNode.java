/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ast.SymbolIterator;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableError;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.TypeFilter;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author aniefer
 */
public class ASTNode implements IASTNode {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind, org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public ILookupResult lookup(String prefix, LookupKind[] kind, IASTNode context) throws LookupError, ASTNotImplementedException {

		if( ! ( this instanceof ISymbolOwner ) || ( context != null && !(context instanceof ISymbolOwner) ) ){
			return null;
		}
		
		IExtensibleSymbol symbol = ((ISymbolOwner)this).getSymbol();
		if( symbol == null || !(symbol instanceof IContainerSymbol) ){
			throw new LookupError();
		}
		IContainerSymbol thisContainer = (IContainerSymbol) symbol; 
		IContainerSymbol qualification = getQualificationSymbol(context);
		
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
		
		List lookupResults = performPrefixLookup(prefix, thisContainer, qualification, filter);
		
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
			}
		}
		
		SymbolIterator iterator = new SymbolIterator( lookupResults.iterator() );

		return new Result( prefix, iterator, lookupResults.size() );
	}
	
	/**
	 * @param prefix
	 * @param thisContainer
	 * @param qualification
	 * @param lookInThis
	 * @param filter
	 * @param lookupResults
	 * @return
	 * @throws LookupError
	 */
	protected List performPrefixLookup(String prefix, IContainerSymbol thisContainer, IContainerSymbol qualification, TypeFilter filter) throws LookupError {
		List results = null;
		try {
			if( qualification != null ){
				results = qualification.prefixLookup( filter, prefix, true );
			} else {
				results = thisContainer.prefixLookup( filter, prefix, false );
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
	protected IContainerSymbol getQualificationSymbol(IASTNode context) throws LookupError {
		if( context == null )
			return null;
	
		ISymbol sym = null;	
		if( context instanceof IASTTypedefDeclaration ||
			context instanceof IASTVariable           ||
			context instanceof IASTParameterDeclaration )
		{
			sym = ((ISymbolOwner)context).getSymbol();
			TypeInfo info = null;
			try{
				info = sym.getTypeInfo().getFinalType();
			} catch( ParserSymbolTableError e ){
				throw new LookupError();
			}
			sym = info.getTypeSymbol();
		}
		else
		{
			sym = (IContainerSymbol) ((ISymbolOwner)context).getSymbol();	
		}
		
		if( sym == null || !(sym instanceof IContainerSymbol) ){
			throw new LookupError();
		}
		return (IContainerSymbol) sym;
	}

	private class Result implements ILookupResult{
		private String prefix;
		private Iterator iterator;
		private int resultsNumber;

		public Result( String pref, Iterator iter, int resultsSize ){
			prefix = pref;
			iterator = iter;
			resultsNumber = resultsSize;
			
		}
		
		public String getPrefix() 	{	return prefix;	 }
		public Iterator getNodes() 	{	return iterator; }
		public int getResultsSize() { return resultsNumber; } 
	}
}