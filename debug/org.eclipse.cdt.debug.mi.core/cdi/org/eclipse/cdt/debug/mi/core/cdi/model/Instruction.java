/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;

/**
 */
public class Instruction extends CObject implements ICDIInstruction  {

	MIAsm asm;
	
	public Instruction(Target target, MIAsm a) {
		super(target);
		asm = a;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction#getAdress()
	 */
	public BigInteger getAdress() {
		return MIFormat.getBigInteger(asm.getAddress());
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
