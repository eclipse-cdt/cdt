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

import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongType;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class LongType extends IntegralType implements ICDILongType {

	/**
	 * @param typename
	 */
	public LongType(Target target, String typename) {
		this(target, typename, false);
	}

	public LongType(Target target, String typename, boolean usigned) {
		super(target, typename, usigned);
	}
}
