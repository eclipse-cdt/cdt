/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 */
public class Location implements ICDILocation {

	long addr;
	String file = "";
	String function = "";
	int line;

	public Location(String f, String fnct, int l) {
		this(f, fnct, l, 0);
	}

	public Location(String f, String fnct, int l, long a) {
		if (f != null)
			file = f;
		if (fnct != null)
			function = fnct;
		line = l;
		addr = a;  
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getAddress()
	 */
	public long getAddress() {
		return addr;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFile()
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getFunction()
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getLineNumber()
	 */
	public int getLineNumber() {
		return line;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#equals(ICDILocation)
	 */
	public boolean equals(ICDILocation location) {
		String ofile = location.getFile();
		if (ofile == null) {
			ofile = "";
		}
		String ofunction = location.getFunction();
		if (ofunction == null) {
			ofunction = "";
		}

		if (file.equals(ofile) && line == location.getLineNumber()) {
			return true;
		}

		if (file.equals(ofile) && function.equals(ofunction)) {
			return true;
		}
		return super.equals(location);
	}

}
