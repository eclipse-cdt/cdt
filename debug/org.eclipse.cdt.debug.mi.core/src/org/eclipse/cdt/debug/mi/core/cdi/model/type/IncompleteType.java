/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;


/**
 */
public class IncompleteType extends Type {

	/**
	 * @param name
	 */
	public IncompleteType(ICDITarget target, String name) {
		super(target, name);
	}

}
