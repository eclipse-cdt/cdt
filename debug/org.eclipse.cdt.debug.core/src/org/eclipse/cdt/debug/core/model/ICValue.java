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
	static final public int TYPE_SIMPLE = 0;
	static final public int TYPE_ARRAY = 1;
	static final public int TYPE_STRUCTURE = 2;
	static final public int TYPE_STRING = 3;
	static final public int TYPE_POINTER = 4;
	static final public int TYPE_ARRAY_PARTITION = 5;
	static final public int TYPE_ARRAY_ENTRY = 7;
	static final public int TYPE_CHAR = 8;
	static final public int TYPE_KEYWORD = 9;

	/**
	 * Returns the type of this value.
	 * 
	 * @return the type of this value
	 */
	int getType();

	/**
	 * Returns the underlying CDI value for this value.
	 */
	ICDIValue getUnderlyingValue();
	
	void setChanged(  boolean changed ) throws DebugException;
}
