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
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;
import org.eclipse.cdt.debug.mi.core.output.MISrcAsm;

/**
 */
public class MixedInstruction extends CObject implements ICDIMixedInstruction {

	MISrcAsm srcAsm;
	
	public MixedInstruction (Target target, MISrcAsm a) {
		super(target);
		srcAsm = a;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction#getFileName()
	 */
	public String getFileName() {
		return srcAsm.getFile();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction#getInstructions()
	 */
	public ICDIInstruction[] getInstructions() {
		MIAsm[] asms = srcAsm.getMIAsms();
		ICDIInstruction[] instructions = new ICDIInstruction[asms.length];
		for (int i = 0; i < asms.length; i++) {
			instructions[i] = new Instruction((Target)getTarget(), asms[i]);
		}
		return instructions;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction#getLineNumber()
	 */
	public int getLineNumber() {
		return srcAsm.getLine();
	}

}
