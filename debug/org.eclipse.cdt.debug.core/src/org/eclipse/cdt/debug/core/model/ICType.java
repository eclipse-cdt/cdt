/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Enter type comment.
 * 
 * @since Jun 10, 2003
 */
public interface ICType extends IAdaptable
{
	String getName();
	
	boolean isArray();

	int[] getArrayDimensions();

	boolean isStructure();

	boolean isCharacter();

	boolean isFloatingPointType();

	boolean isPointer();

	void dispose();
}
