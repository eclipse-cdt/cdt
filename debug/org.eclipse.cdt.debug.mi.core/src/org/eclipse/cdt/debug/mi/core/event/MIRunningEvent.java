package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MITuple;
import org.eclipse.cdt.debug.mi.core.output.MIValue;

/**
 *
 *  ^running
 */
public class MIRunningEvent extends MIEvent {

	boolean isStep;

	public MIRunningEvent() {
		this(false);
	}

	public MIRunningEvent(boolean step) {
		isStep = step;
	}

	public boolean isStepping() {
		return isStep;
	}

	public String toString() {
		return "Running";
	}
}
