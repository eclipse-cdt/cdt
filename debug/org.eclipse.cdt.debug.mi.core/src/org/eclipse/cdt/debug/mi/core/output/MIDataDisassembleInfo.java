package org.eclipse.cdt.debug.mi.core.output;



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

	public MIDataDisassembleInfo(MIOutput rr) {
		super(rr);
	}

	public ASM[] getData() {
		return null;
	}
}
