/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 15, 2002
 */
public interface ICVariable extends IVariable
{
	int getFormat();
	
	void setFormat( int format ) throws DebugException;
	
	void reset() throws DebugException;
	
	boolean isEditable();
	
	boolean hasChildren();

	boolean isArray();

	int[] getArrayDimensions();

	boolean isStructure();

	boolean isCharacter();

	boolean isFloatingPointType();

	boolean isNaN();

	boolean isPositiveInfinity();

	boolean isNegativeInfinity();

	boolean isPointer();

	String getQualifiedName() throws DebugException;
}
