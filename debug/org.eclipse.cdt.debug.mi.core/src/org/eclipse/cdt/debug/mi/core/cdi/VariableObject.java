/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIVariableObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;

/**
 */
public class VariableObject implements ICDIVariableObject {

	String name;
	int position;
	StackFrame frame;
	int stackdepth;

	public VariableObject(String n, StackFrame stack, int pos, int depth) {
		name = n;
		frame = stack;
		position = pos;
		stackdepth = depth;
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
