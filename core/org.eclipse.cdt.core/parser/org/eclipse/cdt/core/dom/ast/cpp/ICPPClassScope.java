/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
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


/**
 * Interface for class scopes.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICPPClassScope extends ICPPScope {
	/**
	 * Get the binding for the class this scope is associated with
	 * 
	 */
	ICPPClassType getClassType();

	/**
	 * Returns an array of methods that were implicitly added to this class
	 * scope. These methods may or may not have been explicitly declared in the
	 * code. The methods that will be implicitly declared are: the default
	 * constructor, copy constructor, copy assignment operator, and destructor
	 * 
	 */
	public ICPPMethod[] getImplicitMethods();
	
	/**
	 * Returns the array of constructors, including implicit ones.
	 * @since 5.1
	 */
	public ICPPConstructor[] getConstructors();
}
