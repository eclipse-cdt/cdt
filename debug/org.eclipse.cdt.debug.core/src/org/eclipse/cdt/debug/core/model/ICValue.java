/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.model.IValue;

/**
 *
 * Extends the IValue interface by C/C++ specific functionality. 
 * 
 * @since Sep 9, 2002
 */
public interface ICValue extends IValue
{
	String evaluateAsExpression();

	boolean isNaN();

	boolean isPositiveInfinity();

	boolean isNegativeInfinity();
}
