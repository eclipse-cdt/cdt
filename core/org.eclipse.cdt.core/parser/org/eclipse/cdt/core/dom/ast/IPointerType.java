/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPointerType extends IType {
    /**
     * get the type that this is a pointer to
     */
    public IType getType();
    
    /**
     * Returns whether the pointer is const qualified.
     */
    public boolean isConst();
    
    /** 
     * Returns whether the pointer is volatile qualified.
     */
    public boolean isVolatile();
    
	/**
	 * Returns whether the pointer is qualified to be restrict.
	 * For c++ this is a gnu-extension.
	 * @since 5.3
	 */
	boolean isRestrict();

}
