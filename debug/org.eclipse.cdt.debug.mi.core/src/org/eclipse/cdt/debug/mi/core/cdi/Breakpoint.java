package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICCondition;
import org.eclipse.cdt.debug.core.cdi.ICLocation;
import org.eclipse.cdt.debug.core.cdi.ICLocationBreakpoint;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Breakpoint extends SessionObject implements ICLocationBreakpoint {

	int type;
	ICLocation location;
	ICCondition condition;
	String threadId = "";
	boolean enabled = false;
	MIBreakPoint miBreakPoint;

	public Breakpoint(BreakpointManager mgr, MIBreakPoint miBreak) {
		super((Session)mgr.getSession());
		miBreakPoint = miBreak;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#getCondition()
	 */
	public ICCondition getCondition() throws CDIException {
		return condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CDIException {
		return threadId;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CDIException {
		return enabled;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#setCondition(ICCondition)
	 */
	public void setCondition(ICCondition condition) throws CDIException {
		this.condition = condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean enable) throws CDIException {
		/*
		if (enable == false && enabled == true) {
			if (miBreak != null) { 
				MICommand cmd = new MIBreakDisable(miBreak.getNumber());
			}
		} else if (enable == true && enabled == false) {
			if (miBreak != null) {
				MICommand cmd = new MIBreakEnable(miBreak.getNumber());
			} else {
				MIBreakInsert cmd = new MIBreakInsert();
				miSession.postCommand(cmd);
				miBreak = cmd.getBreakInsertInfo();
			}
		}
		enabled = enable;
		*/
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocationBreakpoint#getLocation()
	 */
	public ICLocation getLocation() throws CDIException {
		return location;
	}

}
