package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICatchEvent;
import org.eclipse.cdt.debug.core.cdi.ICDICatchpoint;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.mi.core.output.MIBreakPoint;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Breakpoint extends SessionObject implements ICDILocationBreakpoint,
	ICDICatchpoint, ICDIWatchpoint {

	ICDILocation location;
	ICDICondition condition;
	MIBreakPoint miBreakPoint;
	BreakpointManager mgr;

	public Breakpoint(BreakpointManager m, MIBreakPoint miBreak) {
		super(m.getCSession());
		miBreakPoint = miBreak;
		mgr = m;
	}

	MIBreakPoint getMIBreakPoint() {
			return miBreakPoint;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getCondition()
	 */
	public ICDICondition getCondition() throws CDIException {
		if (condition == null) {
			condition = new ICDICondition () {
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDICondition#getIgnoreCount()
				 */
				public int getIgnoreCount() {
					return miBreakPoint.getIgnoreCount();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDICondition#getExpression()
				 */
				public String getExpression() {
					return miBreakPoint.getWhat();
				}
			};
		}
		return condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CDIException {
		return miBreakPoint.getThreadId();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CDIException {
		return miBreakPoint.isEnabled();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return miBreakPoint.isHardware();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return miBreakPoint.isTemporary();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setCondition(ICDICondition)
	 */
	public void setCondition(ICDICondition condition) throws CDIException {
		this.condition = condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean enable) throws CDIException {
		if (enable == false && isEnabled() == true) { 
				mgr.disableBreakpoint(this);
		} else if (enable == true && isEnabled() == false) {
				mgr.enableBreakpoint(this);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocationBreakpoint#getLocation()
	 */
	public ICDILocation getLocation() throws CDIException {
		if (location == null) {
			location = new ICDILocation () {
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getAddress()
				 */
				public long getAddress() {
					return miBreakPoint.getAddress();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFile()
				 */
				public String getFile() {
					return miBreakPoint.getFile();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFunction()
				 */
				public String getFunction() {
					return miBreakPoint.getFunction();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getLineNumber()
				 */
				public int getLineNumber() {
					return miBreakPoint.getLine();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getInstructions(int)
				 */
				public ICDIInstruction[] getInstructions(int maxCount)
					throws CDIException {
					return new ICDIInstruction[0];
				}
				
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getInstructions()
				 */
				public ICDIInstruction[] getInstructions() throws CDIException {
					return new ICDIInstruction[0];
				}
				
			};
		}
		return location;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDICatchpoint#getEvent()
	 */
	public ICDICatchEvent getEvent() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#getWatchExpression()
	 */
	public String getWatchExpression() throws CDIException {
		return miBreakPoint.getWhat();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isReadType()
	 */
	public boolean isReadType() {
		return miBreakPoint.isReadWatchpoint();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isWriteType()
	 */
	public boolean isWriteType() {
		return miBreakPoint.isAccessWatchpoint();
	}

}
