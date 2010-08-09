/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Binding for c++ functions.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICPPFunction extends IFunction, ICPPBinding {

	/**
     * does this function have the mutable storage class specifier
     * @throws DOMException
     */
    public boolean isMutable() throws DOMException;
    
    /**
     * is this an inline function
     */
    public boolean isInline() throws DOMException;
    
    /**
     * Returns whether this function is declared as extern "C".
     * @since 5.0
     */
    public boolean isExternC() throws DOMException;
    
    /**
     * Returns the exception specification for this function or <code>null</code> if there
     * is no exception specification.
     * @since 5.1
     */
    public IType[] getExceptionSpecification() throws DOMException;
    
    /**
     * {@inheritDoc}
	 * @since 5.1
	 */
    public ICPPFunctionType getType() throws DOMException;
    
    /**
	 * @since 5.2
	 */
    public ICPPParameter[] getParameters() throws DOMException;
    
    /**
     * @since 5.2
	 */
    public int getRequiredArgumentCount() throws DOMException;
    
    /**
	 * @since 5.2
	 */
    public boolean hasParameterPack();

	/**
	 * Returns whether this is a function with a deleted function definition.
	 * @since 5.3
	 */
	public boolean isDeleted();
}
