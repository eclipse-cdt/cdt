/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

/**
 * Provides the functionality to support debugger console.
 * 
 * @since: Oct 23, 2002
 */
public interface IDebuggerProcessSupport
{
	boolean supportsDebuggerProcess();
	
	boolean isDebuggerProcessDefault();
	
	void setDebuggerProcessDefault( boolean value );
}
