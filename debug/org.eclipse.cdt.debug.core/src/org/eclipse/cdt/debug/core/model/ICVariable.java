/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
	
	ICType getType() throws DebugException;
	
	boolean isEditable();
	
	boolean hasChildren();

	boolean isEnabled();

	void setEnabled( boolean enabled ) throws DebugException;

	boolean canEnableDisable();

	boolean isArgument();
}
