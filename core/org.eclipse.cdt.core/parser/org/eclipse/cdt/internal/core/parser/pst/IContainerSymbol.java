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
/*
 * Created on May 9, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.TemplateInstance;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.TypeInfo;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IContainerSymbol extends ISymbol {
	
	public void addSymbol( ISymbol symbol ) throws ParserSymbolTableException;

	public boolean hasUsingDirectives();
	public List getUsingDirectives();
	public void addUsingDirective( IContainerSymbol namespace ) throws ParserSymbolTableException;
	
	public ISymbol addUsingDeclaration( String name ) throws ParserSymbolTableException;
	public ISymbol addUsingDeclaration( String name, IContainerSymbol declContext ) throws ParserSymbolTableException;
			
	public Map getContainedSymbols();
	
	public ISymbol ElaboratedLookup( TypeInfo.eType type, String name ) throws ParserSymbolTableException; 
	public ISymbol Lookup( String name ) throws ParserSymbolTableException;
	public ISymbol LookupMemberForDefinition( String name ) throws ParserSymbolTableException;
	public IContainerSymbol LookupNestedNameSpecifier( String name ) throws ParserSymbolTableException;
	public ISymbol QualifiedLookup( String name ) throws ParserSymbolTableException;
	public IParameterizedSymbol UnqualifiedFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException;
	public IParameterizedSymbol MemberFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException;

	public TemplateInstance TemplateLookup( String name, LinkedList arguments ) throws ParserSymbolTableException;
	public TemplateInstance instantiate( LinkedList arguments ) throws ParserSymbolTableException;
}
