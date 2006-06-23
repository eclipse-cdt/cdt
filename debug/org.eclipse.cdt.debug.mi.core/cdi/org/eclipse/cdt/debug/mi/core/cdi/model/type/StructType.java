/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class StructType extends AggregateType implements ICDIStructType {

	/**
	 * @param typename
	 */
	public StructType(Target target, String typename) {
		super(target, typename);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType#isClass()
	 */
	public boolean isClass() {
		return getDetailTypeName().startsWith("class"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType#isStruct()
	 */
	public boolean isStruct() {
		return getDetailTypeName().startsWith("struct"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType#isUnion()
	 */
	public boolean isUnion() {
		return getDetailTypeName().startsWith("union"); //$NON-NLS-1$
	}

}
