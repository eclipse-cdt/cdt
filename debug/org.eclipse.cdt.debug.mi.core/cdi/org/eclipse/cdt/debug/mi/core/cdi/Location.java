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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 */
public class Location implements ICDILocation {

	IAddress addr;
	String file = ""; //$NON-NLS-1$
	String function = ""; //$NON-NLS-1$
	int line;

	public Location(String f, String fnct, int l) {
		this(f, fnct, l, null);
	}

	public Location(String f, String fnct, int l, IAddress a) {
		if (f != null)
			file = f;
		if (fnct != null)
			function = fnct;
		line = l;
		addr = a;  
	}

	public Location(IAddress address) {
		addr = address;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getAddress()
	 */
	public IAddress getAddress() {
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
		if (file.length() > 0 && ofile.length() > 0) {
			if (file.equals(ofile)) {
				int oline = location.getLineNumber();
				if (line != 0 && oline != 0) {
					if (line == oline) {
						return true;
					}
				}
				String ofunction = location.getFunction();
				if (function.length() > 0 && ofunction.length() > 0) {
					if (function.equals(ofunction)) {
						return true;
					}
				}
			}
		}
		IAddress oaddr = location.getAddress();
		if (addr != null && oaddr != null) { //IPF_TODO: check ZERO addresses
			if (addr.equals(oaddr)) {
				return true;
			}
		}
		return super.equals(location);
	}

}
