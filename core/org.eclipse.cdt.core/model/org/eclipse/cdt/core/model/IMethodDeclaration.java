package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

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
	boolean isConstructor();

	/**
	 * Returns whether this method is a destructor.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isDestructor();

	/**
	 * Returns whether this method is an operator method.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isOperator();

	/**
	 * Returns whether this method is declared pure virtual.
	 *
	 * <p>For example, a source method declared as <code>virtual void m() = 0;</code>. 
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isPureVirtual();

	/**
	 * Returns if this method is static or not
	 * @return boolean
	 */
	public boolean isStatic();
	
	/**
	 * Returns if this method is inline or not
	 * @return boolean
	 */
	public boolean isInline();
	
	/**
	 * Returns whether this method is declared virtual.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	boolean isVirtual();

	/**
	 * return true if the member is a friend.
	 */
	public boolean isFriend();

}
