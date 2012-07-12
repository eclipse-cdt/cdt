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
public interface IEnumeration extends IBinding, IType {
    /**
     * Returns an array of the IEnumerators declared in this enumeration
     * @throws DOMException
     */
    IEnumerator[] getEnumerators() throws DOMException;
    
    /**
	 * @since 5.2
	 */
    long getMinValue();
    /**
	 * @since 5.2
	 */
    long getMaxValue();
}
