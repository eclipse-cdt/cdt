/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface ICDebuggerManager {
	public ICDebugger createDebugger(String id) throws CoreException;
	public ICDebuggerInfo[] queryDebuggers(String platform_id);
}
