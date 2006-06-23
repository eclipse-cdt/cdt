/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;


/**
 * Represents the declaration method of a class
 */
public interface IMethodDeclaration extends IMember, IFunctionDeclaration {

	/**
	 * Returns whether this method is a constructor.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isConstructor() throws CModelException;

	/**
	 * Returns whether this method is a destructor.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isDestructor() throws CModelException;

	/**
	 * Returns whether this method is an operator method.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isOperator() throws CModelException;

	/**
	 * Returns whether this method is declared pure virtual.
	 *
	 * <p>For example, a source method declared as <code>virtual void m() = 0;</code>. 
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isPureVirtual() throws CModelException;

	/**
	 * Returns if this method is static or not
	 * @return boolean
	 */
	public boolean isStatic() throws CModelException;
	
	/**
	 * Returns if this method is inline or not
	 * @return boolean
	 */
	public boolean isInline() throws CModelException;
	
	/**
	 * Returns whether this method is declared virtual.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isVirtual() throws CModelException;

	/**
	 * return true if the member is a friend.
	 */
	public boolean isFriend() throws CModelException;

}
