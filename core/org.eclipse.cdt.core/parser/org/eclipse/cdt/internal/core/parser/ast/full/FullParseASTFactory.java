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
package org.eclipse.cdt.internal.core.parser.ast.full;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.TokenDuple;
import org.eclipse.cdt.internal.core.parser.Parser.Backtrack;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;

/**
 * @author jcamelon
 *
 */
public class FullParseASTFactory extends BaseASTFactory implements IASTFactory {

	private ParserSymbolTable pst = new ParserSymbolTable(); // symbol table
	
	public IASTUsingDirective createUsingDirective(
		IASTScope scope,
		TokenDuple duple)
		throws Backtrack {
		Iterator iter = duple.iterator();
		Token t1 = (Token)iter.next();
		IContainerSymbol symbol = null; 
		
		if( t1.getType() == Token.tCOLONCOLON )
			symbol = pst.getCompilationUnit();
		else
		{
			try
			{
				symbol = (IContainerSymbol)((IASTFScope)scope).getContainerSymbol().Lookup( t1.getImage() );
			}
			catch( ParserSymbolTableException pste )
			{
				handlePSTException( pste );
			}
		}
		
		while( iter.hasNext() )
		{
			Token t = (Token)iter.next(); 
			if( t.getType() == Token.tCOLONCOLON ) continue; 
			try
			{
				symbol = symbol.LookupNestedNameSpecifier( t.getImage() );
			}
			catch( ParserSymbolTableException pste )
			{
				handlePSTException( pste );
			}
		}
		
		try {
			((IASTFScope)scope).getContainerSymbol().addUsingDirective( symbol );
		} catch (ParserSymbolTableException pste) {
			handlePSTException( pste );
		}
		
		IASTUsingDirective astUD = new ASTUsingDirective( duple.toString() );
		return astUD;
	}
	
	public IASTASMDefinition createASMDefinition(
		IASTScope scope,
		String assembly,
		int first,
		int last) {
		IContainerSymbol containerSymbol = (IContainerSymbol)((IASTFScope)scope).getSymbol();
		ISymbol asmSymbol = pst.newSymbol( "", ParserSymbolTable.TypeInfo.t_asm );
		IASTFASMDefinition asmDefinition = new ASTASMDefinition( asmSymbol, assembly );
		asmSymbol.setASTNode( asmDefinition );
		
		try {
			containerSymbol.addSymbol(asmSymbol);
		} catch (ParserSymbolTableException e1) {
			//?
		}
		
		asmDefinition.setStartingOffset( first );
		asmDefinition.setEndingOffset( last );
		return asmDefinition;
	}

	public IASTNamespaceDefinition createNamespaceDefinition(
		IASTScope scope,
		String identifier, 
		int first, int nameOffset ) {
		
		IContainerSymbol namespaceSymbol = null; 
		
		pst.newContainerSymbol( identifier, ParserSymbolTable.TypeInfo.t_namespace );
		IASTFNamespaceDefinition namespaceDefinition = new ASTNamespaceDefinition( namespaceSymbol, identifier );
		namespaceDefinition.setStartingOffset( first ); 
		if( identifier != "" )
			namespaceDefinition.setNameOffset( nameOffset );
		return namespaceDefinition;
	}

	public IASTCompilationUnit createCompilationUnit() {
		IASTFCompilationUnit compilationUnit = new ASTCompilationUnit( pst.getCompilationUnit() );  
		return compilationUnit;
	}
	
	public IASTLinkageSpecification createLinkageSpecification(IASTScope scope, String spec) {
		IContainerSymbol symbol = pst.newContainerSymbol("", ParserSymbolTable.TypeInfo.t_linkage );
		IASTFLinkageSpecification linkage = new ASTLinkageSpecification( symbol, spec);
		return linkage;
	}
	
	/**
	 * @param pste
	 */
	private void handlePSTException(ParserSymbolTableException pste) throws Backtrack {
		throw new Backtrack(); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDeclaration createUsingDeclaration(IASTScope scope, boolean isTypeName, TokenDuple name) {
		// TODO Auto-generated method stub
		return null;
	}

}
