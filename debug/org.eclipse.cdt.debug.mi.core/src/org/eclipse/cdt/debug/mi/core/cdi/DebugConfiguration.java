/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DebugConfiguration implements ICDIDebugConfiguration {

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsBreakpoints()
	 */
	public boolean supportsBreakpoints() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsDisconnect()
	 */
	public boolean supportsDisconnect() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsExpressionEvaluation()
	 */
	public boolean supportsExpressionEvaluation() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsInstructionStepping()
	 */
	public boolean supportsInstructionStepping() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsMemoryModification()
	 */
	public boolean supportsMemoryModification() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsMemoryRetrieval()
	 */
	public boolean supportsMemoryRetrieval() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsRegisterModification()
	 */
	public boolean supportsRegisterModification() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsRegisters()
	 */
	public boolean supportsRegisters() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsRestart()
	 */
	public boolean supportsRestart() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsStepping()
	 */
	public boolean supportsStepping() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsSuspendResume()
	 */
	public boolean supportsSuspendResume() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsTerminate()
	 */
	public boolean supportsTerminate() {
		return true;
	}
}
