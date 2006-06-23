/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;

/**
 * GlobalVariableDescriptor
 */
public class GlobalVariableDescriptor extends VariableDescriptor implements ICDIGlobalVariableDescriptor {

	/**
	 * @param target
	 * @param thread
	 * @param stack
	 * @param n
	 * @param fn
	 * @param pos
	 * @param depth
	 */
	public GlobalVariableDescriptor(Target target, Thread thread,
			StackFrame stack, String n, String fn, int pos, int depth) {
		super(target, thread, stack, n, fn, pos, depth);
	}

}
