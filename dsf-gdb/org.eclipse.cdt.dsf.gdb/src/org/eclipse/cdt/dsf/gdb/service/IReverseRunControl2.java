/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

/** @since 5.0 */
public interface IReverseRunControl2 extends IReverseRunControl {

    /**
     * Get the reverse debugging method.
     */
    void getReverseTraceMethod(ICommandControlDMContext context, DataRequestMonitor<ReverseDebugMethod> rm);

    /**
     * Change reverse debugging method based on the method parameter.
     */
    void enableReverseMode(ICommandControlDMContext context, ReverseDebugMethod traceMethod, RequestMonitor rm);
    
	/**
	 * Change reverse debugging method as soon as the program is suspended at the breakpoint location
	 * Note, using the break point id to determine the stop location would be sufficient although in the case
	 * where multiple break points are inserted in the same location, gdb will only report one of them.
	 * 
	 * Having the MIBreakpoint will give us access to the address, file and line number as well which can be used
	 * as alternatives to determine a matched location.
	 * 
	 * This method is specially useful when using async mode with i.e. with GDB 7,12, activating reverse debugging 
	 * when the target is running may trigger an unresponsive GDB 
	 * 
	 * @since 5.2
	 */
	public default void enableReverseModeAtBpLocation(final IContainerDMContext containerContext,
			final ReverseDebugMethod traceMethod, MIBreakpoint bp, boolean triggerContinue) throws CoreException {

		throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED,
				"Enabling Reverse Debug mode at location is not supported", null));//$NON-NLS-1$
	}
}
