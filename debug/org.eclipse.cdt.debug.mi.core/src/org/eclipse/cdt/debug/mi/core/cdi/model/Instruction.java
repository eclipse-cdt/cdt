/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;

/**
 */
public class Instruction extends CObject implements ICDIInstruction  {

	MIAsm asm;
	
	public Instruction(ICDITarget target, MIAsm a) {
		super(target);
		asm = a;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getAdress()
	 */
	public long getAdress() {
		return asm.getAddress();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getFuntionName()
	 */
	public String getFuntionName() {
		return asm.getFunction();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getInstruction()
	 */
	public String getInstruction() {
		return asm.getInstruction();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getOffset()
	 */
	public long getOffset() {
		return asm.getOffset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getArgs()
	 */
	public String getArgs() {
		return asm.getArgs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getOpcode()
	 */
	public String getOpcode() {
		return asm.getOpcode();
	}
}
