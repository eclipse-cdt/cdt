/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFunctionType extends IType {
    /**
     * Returns the return type of this function type
     */
    public IType getReturnType();
    
    /**
     * Returns the adjusted parameter types
     * ISO C99 6.7.5.3, ISO C++98 8.3.4-3 
     */
    public IType[] getParameterTypes();
}
