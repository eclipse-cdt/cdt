package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 */
public class RegisterObject extends VariableObject implements ICDIRegisterObject {

	public RegisterObject(ICDITarget target, String name, int i) {
		super(target, name, null, i, 0);
	}

}
