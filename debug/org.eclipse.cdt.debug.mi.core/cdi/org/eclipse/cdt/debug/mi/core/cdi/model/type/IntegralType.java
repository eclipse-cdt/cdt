/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public abstract class IntegralType extends Type implements ICDIIntegralType {

	boolean unSigned;

	public IntegralType(Target target, String typename, boolean isUnsigned) {
		super(target, typename);
		unSigned = isUnsigned;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIIntegralType#isUnsigned()
	 */
	public boolean isUnsigned() {
		return unSigned;
	}

}
