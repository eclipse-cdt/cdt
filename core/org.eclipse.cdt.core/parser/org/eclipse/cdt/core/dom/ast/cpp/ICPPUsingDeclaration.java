/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * A using declaration introduces a name into the declarative region in which
 * it appears, that name is a synonym of some entity declared elsewhere
 *
 * The using declaration is both a declaration of a new binding and a reference to a
 * previously declared binding
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPUsingDeclaration extends ICPPBinding {
	/** @since 6.3 */
	public static final ICPPUsingDeclaration[] EMPTY_USING_DECL_ARRAY = {};

	/**
	 * Return an array of bindings that were declared by this using declaration.
	 * Each of these bindings delegates to some previously declared binding to which it
	 * is a reference.
	 */
	IBinding[] getDelegates();
}
