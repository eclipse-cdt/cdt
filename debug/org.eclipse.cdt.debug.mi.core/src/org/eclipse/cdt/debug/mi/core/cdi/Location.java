package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICLocation;
import org.eclipse.cdt.debug.core.cdi.model.ICInstruction;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Location implements ICLocation {

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
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getAddress()
	 */
	public long getAddress() {
		return addr;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getFile()
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getFunction()
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getInstructions()
	 */
	public ICInstruction[] getInstructions() throws CDIException {
		return new ICInstruction[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getInstructions(int)
	 */
	public ICInstruction[] getInstructions(int maxCount) throws CDIException {
		return new ICInstruction[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICLocation#getLineNumber()
	 */
	public int getLineNumber() {
		return line;
	}
}
