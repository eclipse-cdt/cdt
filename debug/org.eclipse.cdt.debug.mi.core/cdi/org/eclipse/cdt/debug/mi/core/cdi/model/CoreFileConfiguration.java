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
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;

public class CoreFileConfiguration extends CObject implements ICDITargetConfiguration {

	/**
	 * @param t
	 */
	public CoreFileConfiguration(Target t) {
		super(t);
	}

	public boolean supportsTerminate() {
		return true;
	}

	public boolean supportsDisconnect() {
		return false;
	}

	public boolean supportsRestart() {
		return false;
	}

	public boolean supportsStepping() {
		return false;
	}

	public boolean supportsInstructionStepping() {
		return false;
	}

	public boolean supportsBreakpoints() {
		return false;
	}

	public boolean supportsRegisters() {
		return true;
	}

	public boolean supportsRegisterModification() {
		return false;
	}

	public boolean supportsMemoryRetrieval() {
		return true;
	}

	public boolean supportsMemoryModification() {
		return false;
	}

	public boolean supportsExpressionEvaluation() {
		return true;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsResume()
	 */
	public boolean supportsResume() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSuspend()
	 */
	public boolean supportsSuspend() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSharedLibrary()
	 */
	public boolean supportsSharedLibrary() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#terminateSessionOnExit()
	 */
	public boolean terminateSessionOnExit() {
		return true;
	}
}
