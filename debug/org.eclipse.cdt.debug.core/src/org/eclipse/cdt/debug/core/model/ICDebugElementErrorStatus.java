/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

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
