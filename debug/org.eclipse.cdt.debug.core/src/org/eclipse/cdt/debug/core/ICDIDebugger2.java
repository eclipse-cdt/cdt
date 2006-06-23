/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core; 

import java.io.File;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
 
/**
 * Replacement for deprecated <code>ICDIDebugger</code>.
 * 
 * @since 3.1 experimental
 */
public interface ICDIDebugger2 extends ICDIDebugger {
	
	/**
	 * <code>null</code> can be passed as <code>executable</code> allowing debuggers to create session without executables, 
	 * or load executables later during the session.
	 */
	public ICDISession createSession(ILaunch launch, File executable, IProgressMonitor monitor) throws CoreException;
}
