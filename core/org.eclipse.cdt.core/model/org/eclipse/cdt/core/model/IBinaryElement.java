package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 */
public interface IBinaryElement extends ICElement {

	/**
 	 * Returns the address of the function. This method will return,
 	 * the address of a symbol for children of IBinaryObject.
 	 *
 	 * @exception CModelException if this element does not have address
 	 * information.
 	 */
 	long getAddress() throws CModelException;

	/**
 	 * Returns the binary object the element belongs to.
	 *
 	 */
	IBinary getBinary();
}
