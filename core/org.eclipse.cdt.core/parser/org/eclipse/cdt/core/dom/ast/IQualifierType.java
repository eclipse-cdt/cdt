/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface used to qualify types.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IQualifierType extends IType {
    /**
     * Returns whether this is a const type
     */
    public boolean isConst();
    
    /** 
     * Returns whether this is a volatile type
     */
    public boolean isVolatile();
    
    /** 
     * Returns the type that this is qualifying
     */
    public IType getType();
}
