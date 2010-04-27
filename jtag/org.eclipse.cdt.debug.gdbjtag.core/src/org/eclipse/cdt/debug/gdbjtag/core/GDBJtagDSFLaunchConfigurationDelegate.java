/*******************************************************************************
 * Copyright (c) 2007 - 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core;

/**
 * @author Andy Jin
 *
 */

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The launch configuration delegate for the Jtag hardware debugging using
 * the DSF/GDB debugger framework.
 * <p>
 * This delegate only supports the org.eclipse.cdt.debug.gdbjtag.launchConfigurationType
 * launch configuration types.
 * <p>
 * It extends the standard DSF/GDB launch delegate <code>GdbLaunchDelegate</code>
 * but overrides the <code>getFinalLaunchSequence</code> method to return the Jtag
 * hardware debugging specific launch sequence.
 * @since 7.0
 */
@ThreadSafe
public class GDBJtagDSFLaunchConfigurationDelegate extends GdbLaunchDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate#getFinalLaunchSequence(org.eclipse.cdt.dsf.concurrent.DsfExecutor, org.eclipse.cdt.dsf.gdb.launching.GdbLaunch, org.eclipse.cdt.dsf.gdb.service.SessionType, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Sequence getFinalLaunchSequence(DsfExecutor executor,
			GdbLaunch launch, SessionType type, boolean attach,
			IProgressMonitor pm) {
		return new GDBJtagDSFFinalLaunchSequence(executor, launch, type, attach, pm);
	}

}
