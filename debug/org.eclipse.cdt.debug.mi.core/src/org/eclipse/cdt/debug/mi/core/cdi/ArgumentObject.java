/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIArgumentObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;

/**
 */
public class ArgumentObject extends VariableObject implements ICDIArgumentObject {

	public ArgumentObject(String name, StackFrame frame, int pos, int depth) {
		super(name, frame, pos, depth);
	}

}
