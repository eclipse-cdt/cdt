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

/**
 * Represents the status of a debug element.
 * 
 * @since May 2, 2003
 */
public interface ICDebugElementErrorStatus
{
	public static final int OK = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;

	boolean isOK();

	int getSeverity();
	
	String getMessage();
}
