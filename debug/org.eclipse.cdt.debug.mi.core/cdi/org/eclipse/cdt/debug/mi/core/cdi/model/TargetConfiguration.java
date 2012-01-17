/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.mi.core.MIInferior;
import org.eclipse.cdt.debug.mi.core.MIProcess;
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 */
public class TargetConfiguration extends CObject implements ICDITargetConfiguration {
	
	public TargetConfiguration(Target target) {
		super(target);
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsBreakpoints()
	 */
	@Override
	public boolean supportsBreakpoints() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsDisconnect()
	 */
	@Override
	public boolean supportsDisconnect() {
		MISession miSession = ((Target)getTarget()).getMISession();
		return miSession.isAttachSession() ? true : false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsExpressionEvaluation()
	 */
	@Override
	public boolean supportsExpressionEvaluation() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsInstructionStepping()
	 */
	@Override
	public boolean supportsInstructionStepping() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryModification()
	 */
	@Override
	public boolean supportsMemoryModification() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryRetrieval()
	 */
	@Override
	public boolean supportsMemoryRetrieval() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisterModification()
	 */
	@Override
	public boolean supportsRegisterModification() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisters()
	 */
	@Override
	public boolean supportsRegisters() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSharedLibrary()
	 */
	@Override
	public boolean supportsSharedLibrary() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRestart()
	 */
	@Override
	public boolean supportsRestart() {
		MISession miSession = ((Target)getTarget()).getMISession();
		return miSession.isAttachSession() ? false : true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsStepping()
	 */
	@Override
	public boolean supportsStepping() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsTerminate()
	 */
	@Override
	public boolean supportsTerminate() {
		return true;
		
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsResume()
	 */
	@Override
	public boolean supportsResume() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSuspend()
	 */
	@Override
	public boolean supportsSuspend() {
		String os = null;
		try {
			os = System.getProperty("os.name", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (SecurityException e) {
		}
		Target target = (Target)getTarget();
		MISession miSession = target.getMISession();
		MIProcess gdb = miSession.getGDBProcess();
		MIInferior inferior = miSession.getMIInferior();
		if (gdb.canInterrupt(inferior)) {
			// If we attached sending a control-c,
			// seems to alays work.
			if (miSession.isAttachSession()) {
				return true;
			}

			// If we have a pty, sending a control-c will work
			// except for solaris.
			if (os.equals("SunOS")) { //$NON-NLS-1$
				if (inferior.getTTY() != null) {
					// FIXME: bug in Solaris gdb when using -tty, sending a control-c
					// does not work.
					return false;
				}
				return true;
			}
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#terminateSessionOnExit()
	 */
	public boolean terminateSessionOnExit() {
		return true;
	}
}
