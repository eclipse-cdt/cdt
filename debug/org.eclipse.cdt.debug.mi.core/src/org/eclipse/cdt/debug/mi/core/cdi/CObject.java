/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 */
public class CObject implements ICDIObject {

	CTarget target;
	
	public CObject(CTarget t) {
		target = t;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
	 */
	public ICDITarget getTarget() {
		return target;
	}
	
	public CTarget getCTarget() {
			return target;
	}

}
