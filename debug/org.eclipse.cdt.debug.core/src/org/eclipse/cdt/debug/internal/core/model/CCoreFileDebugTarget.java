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

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICDebugTargetType;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

/**
 * Enter type comment.
 * 
 * @since Aug 20, 2003
 */
public class CCoreFileDebugTarget extends CDebugTarget
{
	public CCoreFileDebugTarget( ILaunch launch,
								 ICDITarget cdiTarget,
								 String name,
								 IProcess debuggerProcess,
								 IFile file )
	{
		super( launch, 
			   ICDebugTargetType.TARGET_TYPE_LOCAL_CORE_DUMP, 
			   cdiTarget, 
			   name, 
			   null,
			   debuggerProcess,
			   file,
			   true,
			   false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate()
	{
		return !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException
	{
		setTerminating( true );
		terminated();
	}
}
