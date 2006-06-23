/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to present a variable as an array of the same type.
 */
public interface ICastToArray extends ICastToType {

	/**
	 * Returns whether this element can be currently casted to array.
	 * 
	 * @return whether this element can be currently casted to array
	 */
	boolean canCastToArray();

	/**
	 * Performs the casting. The element is transformed to the array of the same type.
	 *  
	 * @param startIndex the index of the first element of the array. 0 means that 
	 * the original element is the first member of the array. 
	 * @param length tha array size
	 * @throws DebugException
	 */
	void castToArray( int startIndex, int length ) throws DebugException;
}
