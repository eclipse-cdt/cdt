package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICCondition;
import org.eclipse.cdt.debug.core.cdi.ICLocation;
import org.eclipse.cdt.debug.core.cdi.ICLocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICInstruction;
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

	ICLocation location;
	ICCondition condition;
	String threadId = "";
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#getCondition()
	 */
	public ICCondition getCondition() throws CDIException {
		if (condition == null) {
			condition = new ICCondition () {
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICCondition#getIgnoreCount()
				 */
				public int getIgnoreCount() {
					return miBreakPoint.getIgnoreCount();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICCondition#getExpression()
				 */
				public String getExpression() {
					return miBreakPoint.getWhat();
				}
			};
		}
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
		return miBreakPoint.isEnabled();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return miBreakPoint.getType().startsWith("hw");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return miBreakPoint.getDisposition().equals("del");
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
		if (enable == false && isEnabled() == true) { 
				mgr.disableBreakpoint(this);
		} else if (enable == true && isEnabled() == false) {
				mgr.enableBreakpoint(this);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocationBreakpoint#getLocation()
	 */
	public ICLocation getLocation() throws CDIException {
		if (location == null) {
			location = new ICLocation () {
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getAddress()
				 */
				public long getAddress() {
					return miBreakPoint.getAddress();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getFile()
				 */
				public String getFile() {
					return miBreakPoint.getFile();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getFunction()
				 */
				public String getFunction() {
					return miBreakPoint.getFunction();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getLineNumber()
				 */
				public int getLineNumber() {
					return miBreakPoint.getLine();
				}
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getInstructions(int)
				 */
				public ICInstruction[] getInstructions(int maxCount)
					throws CDIException {
					return new ICInstruction[0];
				}
				
				/**
				 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getInstructions()
				 */
				public ICInstruction[] getInstructions() throws CDIException {
					return new ICInstruction[0];
				}
				
			};
		}
		return location;
	}

}
