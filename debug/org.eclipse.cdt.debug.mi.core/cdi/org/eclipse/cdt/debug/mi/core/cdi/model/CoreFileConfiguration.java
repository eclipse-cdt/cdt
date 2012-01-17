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

public class CoreFileConfiguration extends CObject implements ICDITargetConfiguration {

	/**
	 * @param t
	 */
	public CoreFileConfiguration(Target t) {
		super(t);
	}

	@Override
	public boolean supportsTerminate() {
		return true;
	}

	@Override
	public boolean supportsDisconnect() {
		return false;
	}

	@Override
	public boolean supportsRestart() {
		return false;
	}

	@Override
	public boolean supportsStepping() {
		return false;
	}

	@Override
	public boolean supportsInstructionStepping() {
		return false;
	}

	@Override
	public boolean supportsBreakpoints() {
		return false;
	}

	@Override
	public boolean supportsRegisters() {
		return true;
	}

	@Override
	public boolean supportsRegisterModification() {
		return false;
	}

	@Override
	public boolean supportsMemoryRetrieval() {
		return true;
	}

	@Override
	public boolean supportsMemoryModification() {
		return false;
	}

	@Override
	public boolean supportsExpressionEvaluation() {
		return true;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsResume()
	 */
	@Override
	public boolean supportsResume() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSuspend()
	 */
	@Override
	public boolean supportsSuspend() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSharedLibrary()
	 */
	@Override
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
