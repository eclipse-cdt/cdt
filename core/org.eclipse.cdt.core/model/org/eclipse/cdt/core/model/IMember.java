package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Common protocol for C elements that can be members of types.
 * This set consists of <code>IType</code>, <code>IMethod</code>, 
 * <code>IField</code>.
 */
public interface IMember extends ICElement, ISourceReference, ISourceManipulation {

	static final int V_PUBLIC = 0;
	static final int V_PROTECTED = 1;
	static final int V_PRIVATE = 2;


	/**
	 * Returns true if the member has class scope. For example static methods in
	 * C++ have class scope
	 * 
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean hasClassScope();

	/**
	 * Returns whether this method/field is declared constant.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean isConst();
	
	/**
	 * Returns if this member is volatile or not
	 * @return boolean
	 */
	public boolean isVolatile();

	/**
	 * Returns the access Control of the member. The access qualifier
	 * can be examine using the AccessControl class.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @see IAccessControl
	 */
	public int getAccessControl();
	/**
	 * Returns the member's visibility
	 * V_PRIVATE = 0 V_PROTECTED = 1 V_PUBLIC = 2
	 * @return int
	 */
	public int getVisibility();	
	
}
