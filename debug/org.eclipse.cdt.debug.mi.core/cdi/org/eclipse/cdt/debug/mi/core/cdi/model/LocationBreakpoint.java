/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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

import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.cdi.Locator;

public abstract class LocationBreakpoint extends Breakpoint implements ICDIBreakpoint {

	ICDILocation fLocation;

	public LocationBreakpoint(Target target, int kind, ICDILocation loc, ICDICondition cond) {
		super(target, kind, cond);
		fLocation = loc;
	}

	public int getLineNumber() {
		if (miBreakpoints != null && miBreakpoints.length > 0) {
			return miBreakpoints[0].getLine();
		} else if (fLocation instanceof ICDILineLocation) {
			return ((ICDILineLocation)fLocation).getLineNumber();
		}
		return 0;
	}

	public String getFile() {
		if (miBreakpoints != null && miBreakpoints.length > 0) {
			return miBreakpoints[0].getFile();
		} else if (fLocation instanceof ICDILineLocation) {
			return ((ICDILineLocation)fLocation).getFile();
		} else if (fLocation instanceof ICDIFunctionLocation) {
			return ((ICDIFunctionLocation)fLocation).getFile();
		}
		return null;
	}

	public BigInteger getAddress() {
		if (miBreakpoints != null && miBreakpoints.length > 0) {
			BigInteger addr = BigInteger.ZERO;
			String a = miBreakpoints[0].getAddress();
			if (a != null) {
				addr = MIFormat.getBigInteger(a);
			}
			return addr;
		} else if (fLocation instanceof ICDIAddressLocation) {
			return ((ICDIAddressLocation)fLocation).getAddress();
		}
		return null;
	}

	public String getFunction() {
		// TODO: We need to review this but for now
		// we need to return what the cdt debug passed us
		// it is necessary when they do the comparison.
		if (fLocation instanceof ICDIFunctionLocation) {
			return ((ICDIFunctionLocation)fLocation).getFunction();
		} else if (miBreakpoints != null && miBreakpoints.length > 0) {
			return miBreakpoints[0].getFunction();
		} /*else if (fLocation instanceof ICDIFunctionLocation) {
			return ((ICDIFunctionLocation)fLocation).getFunction();
		}*/
		return null;
	}

	public ICDILocator getLocator() {
		return new Locator(getFile(), getFunction(), getLineNumber(), getAddress());
	}

}
