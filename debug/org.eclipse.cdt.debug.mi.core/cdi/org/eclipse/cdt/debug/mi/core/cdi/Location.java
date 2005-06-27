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
package org.eclipse.cdt.debug.mi.core.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFileLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 */
public class Location /*implements ICDIFunctionLocation, ICDIAddressLocation */{

	BigInteger fAddress = null;
	String fFile = null;
	String fFunction = null;
	int fLine;

	/**
	 * File location
	 * @param file
	 */
	public Location(String file) {
		this(file, null, 0, null);
	}

	/**
	 * File:function location
	 * @param file
	 * @param function
	 */
	public Location(String file, String function) {
		this(file, function, 0, null);
	}

	/**
	 * File:line location
	 * @param file
	 * @param line
	 */
	public Location(String file, int line) {
		this (file, null, line, null);
	}

	/**
	 * Address location
	 * @param address
	 */
	public Location(BigInteger address) {
		this (null, null, 0, address);
	}

	protected Location(String file, String function, int line, BigInteger address) {
		fFile = file;
		fFunction = function;
		fLine = line;
		fAddress = address;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getAddress()
	 */
	public BigInteger getAddress() {
		return fAddress;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFile()
	 */
	public String getFile() {
		return fFile;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFunction()
	 */
	public String getFunction() {
		return fFunction;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getLineNumber()
	 */
	public int getLineNumber() {
		return fLine;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#equals(ICDILocation)
	 */
	public boolean equals(ICDILocation location) {
		if (location == this) {
			return true;
		}
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation)location;
			String oFile = lineLocation.getFile();
			if (oFile != null && oFile.length() > 0 && fFile != null && fFile.length() > 0 && oFile.equals(fFile)) {
				if (lineLocation.getLineNumber() == fLine) {
					return true;
				}
			} else if ((fFile == null || fFile.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (lineLocation.getLineNumber() == fLine) {
					return true;
				}
			}
		} else if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation funcLocation = (ICDIFunctionLocation)location;
			String oFile = funcLocation.getFile();
			String oFunction = funcLocation.getFunction();
			if (oFile != null && oFile.length() > 0 && fFile != null && fFile.length() > 0 && oFile.equals(fFile)) {
				if (oFunction != null && oFunction.length() > 0 && fFunction != null && fFunction.length() > 0 && oFunction.equals(fFunction)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (fFunction == null || fFunction.length() == 0)) {
					return true;
				}
			} else if ((fFile == null || fFile.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (oFunction != null && oFunction.length() > 0 && fFunction != null && fFunction.length() > 0 && oFunction.equals(fFunction)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (fFunction == null || fFunction.length() == 0)) {
					return true;
				}
			}
		} else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation)location;
			BigInteger oAddr = addrLocation.getAddress();
			if (oAddr != null && oAddr.equals(fAddress)) {
				return true;
			} else if (oAddr == null && fAddress == null) {
				return true;
			}
		} else if (location instanceof ICDIFileLocation) {
			ICDIFileLocation fileLocation = (ICDIFileLocation)location;
			String oFile = fileLocation.getFile();
			if (oFile != null && oFile.length() > 0 && fFile != null && fFile.length() > 0 && oFile.equals(fFile)) {
				return true;
			} else if ((fFile == null || fFile.length() == 0) && (oFile == null || oFile.length() == 0)) {
				return true;
			}			
		}
		return false;
	}

}
