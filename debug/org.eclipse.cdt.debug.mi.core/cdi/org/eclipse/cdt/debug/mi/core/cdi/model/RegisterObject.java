package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 */
public class RegisterObject extends VariableObject implements ICDIRegisterObject {

	public RegisterObject(ICDITarget target, String name, int i) {
		super(target, name, null, i, 0);
	}

	public RegisterObject(ICDITarget target, String name, String fn, int i) {
		super(target, name, fn, null, i, 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.VariableObject#getFullName()
	 */
	public String getFullName() {
		if (fullName == null) {
			String n = getName();
			if (!n.startsWith("$")) { //$NON-NLS-1$
				fullName = "$" + n; //$NON-NLS-1$
			}
		}
		return fullName;
	}
}
