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
 * Created on Nov 6, 2003
 */
package org.eclipse.cdt.internal.core.parser.pst;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SpecializedSymbol extends ParameterizedSymbol implements ISpecializedSymbol {
	protected SpecializedSymbol( ParserSymbolTable table, String name ){
		super( table, name, TypeInfo.t_template );
	}
	
	protected SpecializedSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
}
