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
import org.eclipse.cdt.internal.core.parser.ast.SymbolIterator;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.TypeFilter;

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
		
		IContainerSymbol thisContainer = (IContainerSymbol) ((ISymbolOwner)this).getSymbol();
		IContainerSymbol qualification = null;
		
		if( thisContainer.getSymbolTable().getParserMode() != ParserMode.COMPLETION_PARSE ){
			throw new ASTNotImplementedException();
		}
		
		if( context != null ){
			ISymbol sym = (IContainerSymbol) ((ISymbolOwner)context).getSymbol();
			if( sym == null || !(sym instanceof IContainerSymbol) ){
				throw new LookupError();
			}
			qualification =  (IContainerSymbol) sym;
		}
		
		ISymbolOwner owner = (ISymbolOwner) this;
		IExtensibleSymbol symbol = owner.getSymbol();
		if( symbol == null || !(symbol instanceof IContainerSymbol) ){
			throw new LookupError();
		}
	
		boolean lookInThis = false;
		
		TypeFilter filter = new TypeFilter();
		if( kind != null ){
			for( int i = 0; i < kind.length; i++ ){
				filter.addAcceptedType( kind[i] );
				if( kind[i] == LookupKind.THIS ){
					lookInThis = true;
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
		
		List lookupResults = null;
		try {
			if( lookInThis ){
				ISymbol thisPointer = thisContainer.lookup( ParserSymbolTable.THIS );
				ISymbol thisClass = ( thisPointer != null ) ? thisPointer.getTypeSymbol() : null; 
				if( thisClass != null && thisClass instanceof IContainerSymbol ){
					lookupResults = ((IContainerSymbol) thisClass).prefixLookup( filter, prefix, true );
				}
			} else if( qualification != null ){
				lookupResults = qualification.prefixLookup( filter, prefix, true );
			} else {
				lookupResults = thisContainer.prefixLookup( filter, prefix, false );
			}
		} catch (ParserSymbolTableException e) {
			throw new LookupError();
		}
		
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