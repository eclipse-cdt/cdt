/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;

/**
 */
public class VariableObject extends CObject implements ICDIVariableObject {

	String name;
	int position;
	StackFrame frame;
	int stackdepth;

	public VariableObject(ICDITarget target, String n, StackFrame stack, int pos, int depth) {
		super(target);
		name = n;
		frame = stack;
		position = pos;
		stackdepth = depth;
	}

	public ICDITarget getTarget() {
		return target;
	}

	public StackFrame getStackFrame() {
		return frame;
	}

	public int getPosition() {
		return position;
	}

	public int getStackDepth() {
		return stackdepth;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableObject#getName()
	 */
	public String getName() {
		return name;
	}

}
