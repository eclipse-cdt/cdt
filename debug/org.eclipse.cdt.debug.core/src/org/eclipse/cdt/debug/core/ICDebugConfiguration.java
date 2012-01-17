/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface ICDebugConfiguration {
	final static String CPU_NATIVE = "native"; //$NON-NLS-1$
	
	/**
	 * @return
	 * @throws CoreException
	 * @deprecated
	 */
	@Deprecated
	ICDebugger getDebugger() throws CoreException;
	
	ICDIDebugger createDebugger() throws CoreException;
	String getName();
	String getID();
	String getPlatform();
	String[] getCPUList();
	String[] getModeList();
	String[] getCoreFileExtensions();
	boolean supportsCPU(String cpu);
	boolean supportsMode(String mode);
	
	/**
	 * Returns a list of supported build configuration ids.
	 * Returns an empty array if a list has not been specified,
	 * which means that this debug configuration supports all
	 * build configurations.
	 */
	String[] getSupportedBuildConfigPatterns();
	
}
