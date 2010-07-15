/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint2;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Watchpoint extends Breakpoint implements ICDIWatchpoint2 {

	int watchType;
	String expression;
	String memorySpace;
	BigInteger range;
	String derivedExpression;

	public Watchpoint(Target target, String expression, int type,
			int watchType, ICDICondition condition, boolean enabled) {
		this(target, expression, "", BigInteger.ZERO, type, watchType, condition, enabled); //$NON-NLS-1$
	}

	public Watchpoint(Target target, String expression, String memorySpace,
			BigInteger range, int type, int watchType, ICDICondition cond,
			boolean enabled) {
		super(target, type, cond, enabled);
		this.watchType = watchType;
		this.expression = expression;
		this.memorySpace = memorySpace;
		this.range = range;
		
		// If the range and/or memory space are specified, cast the expression, e.g.,
		// (@data char[4])(*0x402000)
		derivedExpression = ""; //$NON-NLS-1$
		boolean doSpecifyMemorySpace = memorySpace.length() > 0;
		boolean doSpecifyRange = range.compareTo(BigInteger.ZERO) > 0;
		boolean doSpecify = doSpecifyMemorySpace || doSpecifyRange;
		if ( doSpecify ) {
			derivedExpression += "("; //$NON-NLS-1$
			if ( doSpecifyMemorySpace ) {
				derivedExpression += "@" + memorySpace; //$NON-NLS-1$
				if ( doSpecifyRange ) {
					derivedExpression += " "; //$NON-NLS-1$
				}
			}
			if ( doSpecifyRange ) {
				derivedExpression += "char[" + range.toString() + "]";  //$NON-NLS-1$	//$NON-NLS-2$
			}
			derivedExpression += ")("; //$NON-NLS-1$
		}
		
		try {
			// Check if this an address watchpoint, and add a '*'
			Integer.decode(expression);
			derivedExpression += '*';
		} catch (NumberFormatException e) {
		}
		derivedExpression += expression;
		if ( doSpecify ) {
			derivedExpression += ")"; //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#getWatchExpression()
	 */
	public String getWatchExpression() throws CDIException {
		if (expression == null) {
			MIBreakpoint[] miPoints = getMIBreakpoints();
			if (miPoints != null && miPoints.length > 0) {
				return miPoints[0].getWhat();
			}
		}
		return expression;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isReadType()
	 */
	public boolean isReadType() {
		return ((watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ);
//		MIBreakpoint miPoint = getMIBreakpoint();
//		if (miPoint != null)
//			return getMIBreakpoint().isReadWatchpoint() || getMIBreakpoint().isAccessWatchpoint();
//		return ((watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpoint#isWriteType()
	 */
	public boolean isWriteType() {
		return ((watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE);
//		MIBreakpoint miPoint = getMIBreakpoint();
//		if (miPoint != null)
//			return getMIBreakpoint().isAccessWatchpoint() || getMIBreakpoint().isWriteWatchpoint();
//		return ((watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE);
	}

	public String getMemorySpace() throws CDIException {
		return memorySpace;
	}

	public BigInteger getRange() throws CDIException {
		return range;
	}

	public String getDerivedExpression() {
		return derivedExpression;
	}
}
