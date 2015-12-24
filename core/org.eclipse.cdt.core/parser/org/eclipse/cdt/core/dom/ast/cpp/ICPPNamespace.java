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

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * This interface represents a C++ namespace
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPNamespace extends ICPPBinding {
	/**
	 * Returns the scope object associated with this namespace
	 */
	public ICPPNamespaceScope getNamespaceScope();
	
	/**
	 * Returns an array of the all the bindings declared in this namespace.
	 */
	public IBinding[] getMemberBindings();

	/**
	 * Returns whether this is an inline namespace.
	 * @since 5.3
	 */
	public boolean isInline();
}
