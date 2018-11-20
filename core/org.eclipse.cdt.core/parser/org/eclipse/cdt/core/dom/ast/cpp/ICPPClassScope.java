/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Interface for class scopes.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICPPClassScope extends ICPPScope {
	/**
	 * Returns the binding for the class this scope is associated with.
	 */
	public ICPPClassType getClassType();

	/**
	 * Returns an array of methods that were implicitly added to this class
	 * scope. These methods may or may not have been explicitly declared in
	 * the code. The methods that will be implicitly declared are: the default
	 * constructor, copy constructor, copy assignment operator, and destructor
	 */
	public ICPPMethod[] getImplicitMethods();

	/**
	 * Returns the array of constructors, including implicit ones.
	 * @since 5.1
	 */
	public ICPPConstructor[] getConstructors();
}
