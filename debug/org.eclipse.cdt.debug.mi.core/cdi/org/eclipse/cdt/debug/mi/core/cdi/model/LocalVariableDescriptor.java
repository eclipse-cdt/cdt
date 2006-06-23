/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;

/**
 * LocalVariableDescriptor
 */
public class LocalVariableDescriptor extends VariableDescriptor implements
		ICDILocalVariableDescriptor {

	/**
	 * @param target
	 * @param n
	 * @param fn
	 * @param stack
	 * @param pos
	 * @param depth
	 */
	public LocalVariableDescriptor(Target target, Thread thread, StackFrame frame, String n, String fn, int pos, int depth) {
		super(target, thread, frame, n, fn, pos, depth);
	}

}
