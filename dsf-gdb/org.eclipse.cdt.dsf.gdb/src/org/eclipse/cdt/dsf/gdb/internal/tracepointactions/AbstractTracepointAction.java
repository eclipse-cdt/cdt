/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.tracepointactions;

import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @since 3.0
 */
public abstract class AbstractTracepointAction extends AbstractBreakpointAction implements ITracepointAction {
	/**
	 * Tracepoint commands, by default, are not executed in Eclipse, but are executed by the backend.
	 */
	@Override
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		return new Status(IStatus.OK, GdbPlugin.PLUGIN_ID, null);
	}
}
