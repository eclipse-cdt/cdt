/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface ICDebugConfiguration {
	ICDebugger getDebugger() throws CoreException;
	String getName();
	String getID();
	String getPlatform();
	String[] getCPUList();
	String[] getModeList();
	boolean supportsCPU(String cpu);
	boolean supportsMode(String mode);
}
