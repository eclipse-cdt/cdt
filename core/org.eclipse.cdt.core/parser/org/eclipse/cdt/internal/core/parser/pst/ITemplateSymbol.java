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
import java.util.Map;

/**
 * @author aniefer
 */

public interface ITemplateSymbol extends IParameterizedSymbol {
	
	public void addTemplateParameter( ISymbol param ) throws ParserSymbolTableException;
	
	public boolean	hasSpecializations();
	public void 	addSpecialization( ISpecializedSymbol spec );
	public List 	getSpecializations();
	
	public IContainerSymbol getTemplatedSymbol();
	
	public Map getDefinitionParameterMap();
	
	public ISpecializedSymbol findSpecialization(List parameters, List arguments);
	public IContainerSymbol findInstantiation( List arguments );
	public List findArgumentsFor( IContainerSymbol instance );
	
	public void addInstantiation( IContainerSymbol instance, List args );
	public ISymbol instantiate( List args ) throws ParserSymbolTableException; 
	public IDeferredTemplateInstance deferredInstance( List args );

}
