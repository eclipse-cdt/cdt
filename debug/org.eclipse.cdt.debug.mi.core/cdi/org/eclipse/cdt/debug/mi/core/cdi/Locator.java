/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDILocator;

public class Locator extends Location implements ICDILocator {

	public Locator(String file, String function, int line, BigInteger address) {
		super(file, function, line, address);
	}

	boolean equalFile(String oFile) {
		return equalString(oFile, getFile());
	}

	boolean equalFunction(String oFunction) {
		return equalString(oFunction, getFunction());
	}

	boolean equalLine(int oLine) {
		return oLine == getLineNumber();
	}

	boolean equalAddress(BigInteger oAddress) {
		if (oAddress == null && getAddress() == null) {
			return true;
		}
		if (oAddress != null && oAddress.equals(getAddress())) {
			return true;
		}
		return false;
	}

	boolean equalString(String f1, String f2) {
		if (f1 != null && f1.length() > 0 && f2 != null && f2.length() > 0) {
			return f1.equals(f2);
		} else if ((f1 == null || f1.length() == 0) && (f2 == null || f2.length() == 0)) {
			return true;
		}
		return false;		
	}

	public boolean equals(ICDILocator locator) {

		if (locator == this) {
			return true;
		}
		String oFile = locator.getFile();
		String oFunction = locator.getFunction();
		int oLine = locator.getLineNumber();
		BigInteger oAddress = locator.getAddress();

		if (equalFile(oFile) && equalFunction(oFunction) &&
				equalLine(oLine) && equalAddress(oAddress)) {
			return true;
		}
		return false;
	}
}
