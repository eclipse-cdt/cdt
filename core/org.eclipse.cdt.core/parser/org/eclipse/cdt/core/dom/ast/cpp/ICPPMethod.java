/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Base interface for methods, also used for constructors.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPMethod extends ICPPFunction, ICPPMember {
	public static final ICPPMethod[] EMPTY_CPPMETHOD_ARRAY = {};
	
	/**
	 * Returns whether this method is declared to be virtual. Does not detect whether
	 * the method is virtual because of overriding a virtual method from a base class.
	 */
	public boolean isVirtual();
	
	/**
	 * Is this a destructor?
	 * 
	 * Returns true if its name starts with '~' 
	 */
	public boolean isDestructor();

	/**
	 * Returns whether this is an implicit method (constructor, assignment operator, etc.)
	 * @since 4.0
	 */
	public boolean isImplicit();
	
	/**
	 * Returns whether this is an explicit constructor or an explicit conversion operator.
	 * @since 5.3
	 */
	boolean isExplicit();

	/**
	 * Returns whether this is a pure abstract method
	 * @since 5.1
	 */
	public boolean isPureVirtual();

	/**
	 * Returns whether this type is declared override.
	 * 
	 * @since 5.4
	 */
	public boolean isOverride();

	/**
	 * Returns whether this type is declared final.
	 * 
	 * @since 5.4
	 */
	public boolean isFinal();
}
