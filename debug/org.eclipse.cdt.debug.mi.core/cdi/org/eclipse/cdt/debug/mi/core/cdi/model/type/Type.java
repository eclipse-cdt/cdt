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
