package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Location implements ICDILocation {

	long addr;
	String file = "";
	String function = "";
	int line;

	public Location(String f, String fnct, int l, long a) {
		file = f;
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getInstructions()
	 */
	public ICDIInstruction[] getInstructions() throws CDIException {
		return new ICDIInstruction[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getInstructions(int)
	 */
	public ICDIInstruction[] getInstructions(int maxCount) throws CDIException {
		return new ICDIInstruction[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#getLineNumber()
	 */
	public int getLineNumber() {
		return line;
	}
}
