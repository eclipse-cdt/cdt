/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.IRequest;

/**
 * View model types for which the "Add Watchpoint (C/C++)" action is applicable
 * should implement this interface. The action is a popupMenu/objectContribution
 * that targets this type.
 * 
 * @since 7.2
 */
public interface ICWatchpointTarget {

	/** IRequest object used in the asynchronous method {@link ICWatchpointTarget#getSize()} */
	interface GetSizeRequest extends IRequest {
		int getSize(); // returns -1 if size not available
		void setSize(int size);
	};
	
	interface CanCreateWatchpointRequest extends IRequest {
		boolean getCanCreate();
		void setCanCreate(boolean value);
	};

	/**
	 * Determine if a watchpoint can be set on the element. The result does not
	 * guarantee an attempt to set such a watchpoint will succeed. This is
	 * merely a way to find out whether it makes sense to even attempt it. For
	 * example, an expression that's not an l-value should return false. The
	 * implementation may choose to go even further and check that the target
	 * supports watchpoints (at all or at that particular location).
	 */
	void canSetWatchpoint(CanCreateWatchpointRequest request);
	
	/**
	 * Get the expression or the name of the variable
	 */
	String getExpression();

	/**
	 * Asynchronous method to retrieve the size of the variable/expression, in
	 * bytes.
	 * 
	 * @param request
	 *            the async request object
	 */
	void getSize(GetSizeRequest request);
}
