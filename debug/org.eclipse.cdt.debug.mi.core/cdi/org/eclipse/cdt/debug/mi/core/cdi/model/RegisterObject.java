package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 */
public class RegisterObject extends VariableObject implements ICDIRegisterObject {

	public RegisterObject(ICDITarget target, String name, int i) {
		super(target, name, null, i, 0);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getQualifiedName()
	 */
	public String getQualifiedName() throws CDIException {
		if (qualifiedName == null) {
			qualifiedName = "$" + getFullName(); //$NON-NLS-1$
		}
		return qualifiedName;
	}

}
