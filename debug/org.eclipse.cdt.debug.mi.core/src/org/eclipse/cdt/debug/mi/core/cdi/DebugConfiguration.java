package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DebugConfiguration implements ICDebugConfiguration {

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsBreakpoints()
	 */
	public boolean supportsBreakpoints() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsDisconnect()
	 */
	public boolean supportsDisconnect() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsExpressionEvaluation()
	 */
	public boolean supportsExpressionEvaluation() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsInstructionStepping()
	 */
	public boolean supportsInstructionStepping() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsMemoryModification()
	 */
	public boolean supportsMemoryModification() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsMemoryRetrieval()
	 */
	public boolean supportsMemoryRetrieval() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsRegisterModification()
	 */
	public boolean supportsRegisterModification() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsRegisters()
	 */
	public boolean supportsRegisters() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsRestart()
	 */
	public boolean supportsRestart() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsStepping()
	 */
	public boolean supportsStepping() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsSuspendResume()
	 */
	public boolean supportsSuspendResume() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration#supportsTerminate()
	 */
	public boolean supportsTerminate() {
		return false;
	}

}
