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
		return false;
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
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsInstructionStepping()
	 */
	public boolean supportsInstructionStepping() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsMemoryModification()
	 */
	public boolean supportsMemoryModification() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsMemoryRetrieval()
	 */
	public boolean supportsMemoryRetrieval() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsRegisterModification()
	 */
	public boolean supportsRegisterModification() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsRegisters()
	 */
	public boolean supportsRegisters() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsRestart()
	 */
	public boolean supportsRestart() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsStepping()
	 */
	public boolean supportsStepping() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsSuspendResume()
	 */
	public boolean supportsSuspendResume() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration#supportsTerminate()
	 */
	public boolean supportsTerminate() {
		return false;
	}

}
