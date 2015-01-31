/*******************************************************************************
 * Copyright (c) 2015 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Marchi (Ericsson) - Add castToArray with length expression.
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * @since 7.6
 */
public interface ICastToArray2 extends ICastToArray {
	/**
	 * Performs the casting. The element is transformed to the array of the same type.
	 *  
	 * @param startIndex the index of the first element of the array. 0 means that 
	 * the original element is the first member of the array. 
	 * @param length an expression representing the array size
	 * @throws DebugException
	 * @since 7.6
	 */
	void castToArray( int startIndex, String lengthExpr ) throws DebugException;
}
