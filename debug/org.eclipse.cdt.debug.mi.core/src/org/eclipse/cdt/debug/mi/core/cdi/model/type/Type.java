/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;

/**
 */
public abstract class Type extends CObject implements ICDIType {

	String typename;
	String detailName;

	public Type(ICDITarget target, String name) {
		super(target);
		typename = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIType#getTypeName()
	 */
	public String getTypeName() {
		return typename;
	}

	public void setDetailTypeName(String name) {
		detailName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIType#getDetailTypeName()
	 */
	public String getDetailTypeName() {
		if (detailName == null) {
			return getTypeName();
		}
		return detailName;
	}
}
