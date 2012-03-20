/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 * Warren Paul (Nokia) - Bug 217485, Bug 218342
 * Oyvind Harboe (oyvind.harboe@zylin.com) - Bug 225099
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Toggles a line breakpoint in a C/C++ editor.
 */
public class ToggleBreakpointAdapter extends AbstractToggleBreakpointAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#findLineBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, int)
	 */
	@Override
	protected ICLineBreakpoint findLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
		return CDIDebugModel.lineBreakpointExists( sourceHandle, resource, lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#createLineBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, int)
	 */
	@Override
	protected void createLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
		CDIDebugModel.createLineBreakpoint( sourceHandle, 
				resource,
				getBreakpointType(),
				lineNumber, 
				true, 
				0, 
				"", //$NON-NLS-1$
				true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#findFunctionBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, java.lang.String)
	 */
	@Override
	protected ICFunctionBreakpoint findFunctionBreakpoint( 
			String sourceHandle, 
			IResource resource, 
			String functionName ) throws CoreException {
		return CDIDebugModel.functionBreakpointExists( sourceHandle, resource, functionName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#createFunctionBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, java.lang.String, int, int, int)
	 */
	@Override
	protected void createFunctionBreakpoint( 
			String sourceHandle, 
			IResource resource, 
			String functionName, 
			int charStart, 
			int charEnd, 
			int lineNumber ) throws CoreException {
		CDIDebugModel.createFunctionBreakpoint( sourceHandle, 
				resource,
				getBreakpointType(),
				functionName,
				charStart,
				charEnd,
				lineNumber,
				true, 
				0, 
				"", //$NON-NLS-1$
				true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#findWatchpoint(java.lang.String, org.eclipse.core.resources.IResource, java.lang.String)
	 */
	@Override
	protected ICWatchpoint findWatchpoint( String sourceHandle, IResource resource, String expression ) throws CoreException {
		return CDIDebugModel.watchpointExists( sourceHandle, resource, expression );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#createWatchpoint(java.lang.String, org.eclipse.core.resources.IResource, int, int, int, boolean, boolean, java.lang.String, java.lang.String, java.math.BigInteger)
	 */
	@Override
	protected void createWatchpoint( 
			String sourceHandle, 
			IResource resource, 
			int charStart, 
			int charEnd, 
			int lineNumber, 
			boolean writeAccess,
			boolean readAccess, 
			String expression, 
			String memorySpace, 
			BigInteger range ) throws CoreException {

		CDIDebugModel.createWatchpoint( sourceHandle, 
				resource,
				charStart,
				charEnd,
				lineNumber,
				writeAccess, 
				readAccess,
				expression,
				memorySpace,
				range,
				true, 
				0, 
				"", //$NON-NLS-1$
				true );
	}

	protected int getBreakpointType() {
		return ICBreakpointType.REGULAR;
	}
}
