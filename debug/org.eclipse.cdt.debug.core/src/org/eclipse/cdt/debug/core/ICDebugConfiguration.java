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
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface ICDebugConfiguration {
	final static String CPU_NATIVE = "native"; //$NON-NLS-1$
	
	ICDebugger getDebugger() throws CoreException;
	String getName();
	String getID();
	String getPlatform();
	String[] getCPUList();
	String[] getModeList();
	boolean supportsCPU(String cpu);
	boolean supportsMode(String mode);
}
