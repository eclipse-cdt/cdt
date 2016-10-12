/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IVariable;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPVariable extends IVariable, ICPPBinding {
    /**
     * Checks whether this variable has the mutable storage class specifier.
     */
    public boolean isMutable();

    /**
     * Checks whether this variable is declared as constexpr.
     * @since 6.2
     */
	public boolean isConstexpr();

    /**
     * Checks whether this variable is declared as extern "C".
     */
    public boolean isExternC();
}
