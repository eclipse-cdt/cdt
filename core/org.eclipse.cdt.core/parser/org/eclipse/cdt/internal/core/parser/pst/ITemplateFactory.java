/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;

/**
 * @author aniefer
 **/

public interface ITemplateFactory {
	
	public void addSymbol( ISymbol symbol ) throws ParserSymbolTableException;
	
	public ISymbol lookupMemberForDefinition( String name ) throws ParserSymbolTableException;
	public IParameterizedSymbol lookupMemberFunctionForDefinition( String name, List params ) throws ParserSymbolTableException;
	
	public ITemplateFactory lookupTemplateForMemberDefinition( String name, List templateParameters, 
			                                                                List templateArguments ) throws ParserSymbolTableException;
	public ITemplateSymbol getPrimaryTemplate();
	
	public ISymbol lookupParam( String name ) throws ParserSymbolTableException;
}
