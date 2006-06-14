/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;

import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */

public interface ITemplateSymbol extends IParameterizedSymbol {
	
	/**
	 * 
	 * @param param
	 * @throws ParserSymbolTableException
	 * Reason:
	 *    r_BadTemplateParameter if the Parameter does not have type TypeInfo.t_templateParameter
	 *                           or if the parameter has the same name as the template
	 *                           or if the parameter is a non-type parameter and does not have a valid type (14.1-4, 14.1-7)
	 */
	public void addTemplateParameter( ISymbol param ) throws ParserSymbolTableException;
	
	public boolean	hasSpecializations();
	public void 	addSpecialization( ISpecializedSymbol spec );
	public List 	getSpecializations();
	
	public IContainerSymbol getTemplatedSymbol();
	
	public ObjectMap getDefinitionParameterMap();
	
	public IContainerSymbol findInstantiation( List arguments );
	public List findArgumentsFor( IContainerSymbol instance );
	
	public void addInstantiation( IContainerSymbol instance, List args );
	public void removeInstantiation( IContainerSymbol symbol );
	
	public void addExplicitSpecialization( ISymbol symbol, List args ) throws ParserSymbolTableException;
	
	/**
	 * 
	 * @param args
	 * @return
	 * @throws ParserSymbolTableException
	 * Reason:
	 *     r_BadTemplateArgument if an argument does not match the corresponding parameter type
	 *     r_Ambiguous if more than one specialization can be used but none is more specialized than all the others
	 */
	public ISymbol instantiate( List args ) throws ParserSymbolTableException;
	
	public IDeferredTemplateInstance deferredInstance( List args );
	
	public ObjectMap getExplicitSpecializations();

	/**
	 * @param symbol
	 * @param type
	 * @param kind
	 */
	public void registerDeferredInstatiation( Object obj0, Object obj1, DeferredKind kind, ObjectMap argMap );
	public int getNumberDeferredInstantiations();
	
	public static class DeferredKind{
		private DeferredKind( int v ){
			_val = v;
		}
		protected int _val;
		
		public static final DeferredKind RETURN_TYPE = new DeferredKind( 1 );
		public static final DeferredKind PARENT      = new DeferredKind( 2 );
		public static final DeferredKind TYPE_SYMBOL = new DeferredKind( 3 );
	}
}
