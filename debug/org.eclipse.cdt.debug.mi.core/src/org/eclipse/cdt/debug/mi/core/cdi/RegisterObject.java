package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject;

/**
 */
public class RegisterObject implements ICDIRegisterObject {

	int index;
	String name;

	public RegisterObject(String n, int i) {
		name = n;
		index = i;
	}

	public int getId() {
		return index;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject#getName()
	 */
	public String getName() {
		return name;
	}

}
