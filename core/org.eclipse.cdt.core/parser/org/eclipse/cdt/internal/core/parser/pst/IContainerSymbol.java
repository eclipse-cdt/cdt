/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayObjectMap;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IContainerSymbol extends ISymbol {
	
	/**
	 * Add a symbol to this container
	 * @param symbol
	 * @throws ParserSymbolTableException
	 *    Reason : r_BadTemplate if (14-2) the symbol is a template declaration is outside a namespace or class scope
	 *                        or if (14.5.2-3) the symbol is a member function template and is virtual
	 *             r_RedeclaredTemplateParam if (14.6.1-4) a template parameter is redeclared in its scope
	 *             r_InvalidOverload if there already exists a symbol with the same name and the new symbol does not
	 *                               hide the first symbol (3.3.7) or is not a valid function overload (3.4-1)
	 */
	public void addSymbol( ISymbol symbol ) throws ParserSymbolTableException;
	
	public void addTemplateId( ISymbol symbol, List args ) throws ParserSymbolTableException;

//	public boolean removeSymbol( ISymbol symbol );
	
	public boolean hasUsingDirectives();
	public List getUsingDirectives();
	
	/**
	 * Add a using directive to this symbol
	 * @param namespace
	 * @return
	 * @throws ParserSymbolTableException
	 *    Reason: r_InvalidUsing if (7.3.4) the using directive appears in class scope
	 *                           or if the symbol being added is not a namespace
	 */
	public IUsingDirectiveSymbol addUsingDirective( IContainerSymbol namespace ) throws ParserSymbolTableException;
	
	/**
	 * Add a using declaration to this symbol
	 * @param name
	 * @return
	 * @throws ParserSymbolTableException
	 *    Reason: r_InvalidUsing if (7.3.3-5) the name is a template-id
	 *                        or if (7.3.3-4) this using declaration is a member declaration and the name is not a
	 *                           member of a base class, or an anonymous union that is a member of a base class, or
	 *                           an enumerator for an enumeration which is a member of a base class
	 *                        or if the name specified can not be found
	 *             r_Ambiguous if more than one symbol with the given name is found and we can't resolve which one to use
	 *             r_BadTypeInfo if during lookup of the name, we come across a class inheriting from a symbol which is not an
	 *                            IDerivableContainerSymbol
	 *             r_CircularInheritance if during lookup of the name, we come across a class with a circular inheritance tree
	 */
	public IUsingDeclarationSymbol addUsingDeclaration( char[] name ) throws ParserSymbolTableException;
	public IUsingDeclarationSymbol addUsingDeclaration( char[] name, IContainerSymbol declContext ) throws ParserSymbolTableException;
			
	public CharArrayObjectMap getContainedSymbols();
	
	/**
	 * Lookup symbols matching the given prefix
	 * @param filter
	 * @param prefix
	 * @param qualified
	 * @param paramList TODO
	 * @return
	 * @throws ParserSymbolTableException
	 *   Reason: r_BadTypeInfo if during lookup, we come across a class inheriting from a symbol which is not an
	 *                            IDerivableContainerSymbol
	 *           r_CircularInheritance if during lookup, we come across a class with a circular inheritance tree
	 */
	public List prefixLookup( TypeFilter filter, char[] prefix, boolean qualified, List paramList ) throws ParserSymbolTableException;
	
	/**
	 * Lookups
	 * @throws ParserSymbolTableException
	 *   Reason:   r_Ambiguous if more than one symbol with the given name is found and we can't resolve which one to use
	 *             r_UnableToResolveFunction if an overloaded function is found and no parameter information has been provided
	 *             r_BadTypeInfo if during lookup of the name, we come across a class inheriting from a symbol which is not an
	 *                            IDerivableContainerSymbol
	 *             r_CircularInheritance if during lookup of the name, we come across a class with a circular inheritance tree
	 */
	public ISymbol elaboratedLookup( ITypeInfo.eType type, char[] name ) throws ParserSymbolTableException; 
	public ISymbol lookup( char[] name ) throws ParserSymbolTableException;
	public ISymbol lookupMemberForDefinition( char[] name ) throws ParserSymbolTableException;
	public IParameterizedSymbol lookupMethodForDefinition( char[] name, List parameters ) throws ParserSymbolTableException;
	public IContainerSymbol lookupNestedNameSpecifier( char[] name ) throws ParserSymbolTableException;
	public ISymbol qualifiedLookup( char[] name ) throws ParserSymbolTableException;
	public ISymbol qualifiedLookup( char[] name, ITypeInfo.eType t ) throws ParserSymbolTableException;
	public IParameterizedSymbol unqualifiedFunctionLookup( char[] name, List parameters ) throws ParserSymbolTableException;
	public IParameterizedSymbol memberFunctionLookup( char[] name, List parameters ) throws ParserSymbolTableException;
	public IParameterizedSymbol qualifiedFunctionLookup( char[] name, List parameters ) throws ParserSymbolTableException;
	
	/**
	 * 
	 * @param name
	 * @param arguments
	 * @return
	 * @throws ParserSymbolTableException
	 * In addition to the above lookup reasons, the following also can happen in lookupTemplate
	 *      r_Ambiguous if (14.5.4.1) more than one specialization can be used and none is more specializaed than all the others
	 *      r_BadTemplateArgument if (14.3.1, 14.3.2) a template argument is invalid
	 */
	public ISymbol lookupTemplateId( char[] name, List arguments ) throws ParserSymbolTableException;
	public ISymbol lookupFunctionTemplateId( char[] name, List parameters, List arguments, boolean forDefinition ) throws ParserSymbolTableException;
	
	public IContainerSymbol lookupTemplateIdForDefinition( char[] name, List arguments ) throws ParserSymbolTableException;
	
	/**
	 * 
	 * @param name
	 * @param templateParameters
	 * @param templateArguments
	 * @return
	 * @throws ParserSymbolTableException
	 * In addition to the Exception thrown in lookup functions, and lookupTemplate, lookupTemplateForMemberDefinition can also throw:
	 *      r_BadTemplateParameter if (14.5.1-3) the parameters provided can't be matched up to a template declaration
	 *      r_BadTemplate if the parameter and argument list can't be resolved to either the template or a specialization
	 */
//	public ITemplateFactory lookupTemplateForMemberDefinition( String name, List templateParameters, 
//			                                                                List templateArguments ) throws ParserSymbolTableException;
	
	public boolean isVisible( ISymbol symbol, IContainerSymbol qualifyingSymbol );
	
	public Iterator getContentsIterator();
}
