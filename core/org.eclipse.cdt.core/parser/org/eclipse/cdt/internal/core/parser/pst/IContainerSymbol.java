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

import java.util.List;
import java.util.Map;


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
	
	public List prefixLookup( TypeInfo.eType type, String prefix, boolean qualified ) throws ParserSymbolTableException;
	
	public ISymbol elaboratedLookup( TypeInfo.eType type, String name ) throws ParserSymbolTableException; 
	public ISymbol lookup( String name ) throws ParserSymbolTableException;
	public ISymbol lookupMemberForDefinition( String name ) throws ParserSymbolTableException;
	public IContainerSymbol lookupNestedNameSpecifier( String name ) throws ParserSymbolTableException;
	public ISymbol qualifiedLookup( String name ) throws ParserSymbolTableException;
	public ISymbol qualifiedLookup( String name, TypeInfo.eType t ) throws ParserSymbolTableException;
	public IParameterizedSymbol unqualifiedFunctionLookup( String name, List parameters ) throws ParserSymbolTableException;
	public IParameterizedSymbol memberFunctionLookup( String name, List parameters ) throws ParserSymbolTableException;
	public IParameterizedSymbol qualifiedFunctionLookup( String name, List parameters ) throws ParserSymbolTableException;
	public TemplateInstance templateLookup( String name, List arguments ) throws ParserSymbolTableException;
	public TemplateInstance instantiate( List arguments ) throws ParserSymbolTableException;
}
