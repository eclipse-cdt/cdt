/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;
import org.eclipse.cdt.debug.mi.core.output.MISrcAsm;

/**
 */
public class MixedInstruction extends CObject implements ICDIMixedInstruction {

	MISrcAsm srcAsm;
	
	public MixedInstruction (CTarget target, MISrcAsm a) {
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
			instructions[i] = new Instruction(getCTarget(), asms[i]);
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
