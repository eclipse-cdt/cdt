/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Configuration implements ICDIConfiguration {

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsBreakpoints()
	 */
	public boolean supportsBreakpoints() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsDisconnect()
	 */
	public boolean supportsDisconnect() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsExpressionEvaluation()
	 */
	public boolean supportsExpressionEvaluation() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsInstructionStepping()
	 */
	public boolean supportsInstructionStepping() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryModification()
	 */
	public boolean supportsMemoryModification() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsMemoryRetrieval()
	 */
	public boolean supportsMemoryRetrieval() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisterModification()
	 */
	public boolean supportsRegisterModification() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRegisters()
	 */
	public boolean supportsRegisters() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsRestart()
	 */
	public boolean supportsRestart() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsStepping()
	 */
	public boolean supportsStepping() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsSuspendResume()
	 */
	public boolean supportsSuspendResume() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIConfiguration#supportsTerminate()
	 */
	public boolean supportsTerminate() {
		return true;
	}
}
