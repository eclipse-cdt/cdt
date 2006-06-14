/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on May 9, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IDerivableContainerSymbol extends IContainerSymbol {

	public void addParent( ISymbol parent );
	public void addParent( ISymbol parent, boolean virtual, ASTAccessVisibility visibility, int offset, List references );
	public List getParents();
	public boolean hasParents();
	
	/**
	 * 
	 * @param constructor
	 * @throws ParserSymbolTableException
	 * Reason:
	 *    r_BadTypeInfo if the symbol being added is not of type TypeInfo.t_constructor
	 *    r_InvalidOverload if the constructor being added is not a valid overload of existing constructors
	 */
	public void addConstructor( IParameterizedSymbol constructor ) throws ParserSymbolTableException;
	public void addCopyConstructor() throws ParserSymbolTableException;
	
	/**
	 * 
	 * @param parameters
	 * @return
	 * @throws ParserSymbolTableException
	 * Reason:
	 *    r_Ambiguous if more than one constructor can be used with the given parameters
	 */
	public IParameterizedSymbol lookupConstructor( List parameters ) throws ParserSymbolTableException;
	
	public List getConstructors();
	
	public void addFriend( ISymbol friend );
	
	/**
	 * Lookups
	 * @throws ParserSymbolTableException
	 *   Reason:   r_Ambiguous if more than one symbol with the given name is found and we can't resolve which one to use
	 *             r_UnableToResolveFunction if an overloaded function is found and no parameter information has been provided
	 *             r_BadTypeInfo if during lookup of the name, we come across a class inheriting from a symbol which is not an
	 *                            IDerivableContainerSymbol
	 *             r_CircularInheritance if during lookup of the name, we come across a class with a circular inheritance tree
	 */
	public ISymbol lookupForFriendship( char[] name ) throws ParserSymbolTableException;
	public IParameterizedSymbol lookupFunctionForFriendship( char[] name, List parameters ) throws ParserSymbolTableException;
	
	public List getFriends();
	
	public interface IParentSymbol extends Cloneable {
		public Object clone();
		public void setParent( ISymbol parent );
		public ISymbol getParent();
		public boolean isVirtual();
		public void setVirtual( boolean virtual );
		
		public ASTAccessVisibility getAccess();
		public int getOffset();
		public List getReferences();
	}
}
