/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Feb 18, 2004
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;


/**
 * @author aniefer
 */
public class UsingDeclarationSymbol extends ExtensibleSymbol implements IUsingDeclarationSymbol {

	public UsingDeclarationSymbol( ParserSymbolTable table, List referenced, List declared ){
		super( table );
		referencedSymbol = referenced;
		declaredSymbol = declared; 
	}

	public List getReferencedSymbols() { return referencedSymbol; }
	public List getDeclaredSymbols()   { return declaredSymbol;   }
	
	private final List referencedSymbol;
	private final List declaredSymbol;
}
