package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIDataDisassembleInfo extends MIInfo {

	public class ASM {
		int address;
		String function;
		int offset;
		String instruction;
		int line;
		String file;
	}

	public MIDataDisassembleInfo(MIResultRecord rr) {
		super(rr);
	}

	public int getCount() {
		return 0;
	}

	public ASM[] getData() {
		return null;
	}
}
