/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.core;

public interface ICDebuggerManager {
	public ICDebugger createDebugger(String id);
	public ICDebuggerInfo[] queryDebuggers(String platform_id);
}
