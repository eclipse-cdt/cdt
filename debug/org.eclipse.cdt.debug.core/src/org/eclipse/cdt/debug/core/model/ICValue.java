/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 *
 * Extends the IValue interface by C/C++ specific functionality. 
 * 
 * @since Sep 9, 2002
 */
public interface ICValue extends IValue
{
	/**
	 * Returns the underlying CDI value for this value.
	 */
	ICDIValue getUnderlyingValue();
	
	/**
	 * Returns the string representation of the underlying CDI value for this value.
	 */
	String getUnderlyingValueString();

	String computeDetail();

	void setChanged(  boolean changed ) throws DebugException;
}
