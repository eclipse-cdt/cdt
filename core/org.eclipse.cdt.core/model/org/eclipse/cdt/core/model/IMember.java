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

	///**
	//* Returns the structure in which this member is declared, or <code>null</code>
	//* if this member is not declared in a type (for example, a top-level type).
	//*/
	//IStructure belongsTo() throws CModelException;

	/**
	 * Returns true if the member as class scope.
	 * For example static methods in C++ have class scope 
	 * 
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean hasClassScope() throws CModelException;

	/**
	 * Returns whether this method/field is declared constant.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean isConst() throws CModelException;

	/**
	 * Returns the access Control of the member. The access qualifier
	 * can be examine using the AccessControl class.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @see IAccessControl
	 */
	public int getAccessControl() throws CModelException;
}
