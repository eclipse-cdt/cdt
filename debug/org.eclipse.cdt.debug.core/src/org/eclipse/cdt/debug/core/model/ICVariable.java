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
	
	ICType getType() throws DebugException;
	
	boolean isEditable();
	
	boolean hasChildren();

	String getQualifiedName() throws DebugException;

	boolean isEnabled();

	void setEnabled( boolean enabled ) throws DebugException;

	boolean canEnableDisable();
}
