/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface ICDebugConfiguration {
	public ICDebugger getDebugger() throws CoreException;
	public String getName();
	public String getID();
	public String[] getPlatforms();
}
