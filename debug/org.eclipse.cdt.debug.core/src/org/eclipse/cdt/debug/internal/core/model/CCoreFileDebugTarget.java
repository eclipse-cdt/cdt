/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

/**
 * A debug target for the postmortem debugging.
 * @deprecated
 */
public class CCoreFileDebugTarget extends CDebugTarget {


	/**
	 * @param launch
	 * @param project
	 * @param cdiTarget
	 * @param name
	 * @param debuggeeProcess
	 * @param file
	 * @param allowsTerminate
	 * @param allowsDisconnect
	 */
	public CCoreFileDebugTarget(ILaunch launch, IProject project, ICDITarget cdiTarget, String name, IProcess debuggeeProcess, IBinaryExecutable file) {
		super(launch, project, cdiTarget, name, debuggeeProcess, file, false, false);
		setState(CDebugElementState.TERMINATED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#isPostMortem()
	 */
	public boolean isPostMortem() {
		return true;
	}
}
