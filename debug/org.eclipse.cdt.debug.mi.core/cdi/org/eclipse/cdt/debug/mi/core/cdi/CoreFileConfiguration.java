/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;

public class CoreFileConfiguration implements ICDIConfiguration {

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
		return false;
	}
}
