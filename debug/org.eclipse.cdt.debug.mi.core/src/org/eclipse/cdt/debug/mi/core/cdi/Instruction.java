package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.mi.core.output.MIAsm;

/**
 */
public class Instruction extends CObject implements ICDIInstruction  {

	MIAsm asm;
	
	public Instruction(CTarget target, MIAsm a) {
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

}
