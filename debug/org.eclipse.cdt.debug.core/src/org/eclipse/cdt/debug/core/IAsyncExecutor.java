/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

/**
 * Enter type comment.
 * 
 * @since: Jan 27, 2003
 */
public interface IAsyncExecutor
{
	void asyncExec( Runnable runnable );
}
