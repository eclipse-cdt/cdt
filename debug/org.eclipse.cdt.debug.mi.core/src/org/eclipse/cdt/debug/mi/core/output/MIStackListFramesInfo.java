package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIStackListFramesInfo extends MIInfo {

	public class Frame {
		int level;
		int addr;
		String function;
		String file;
		int line;
	}

	public MIStackListFramesInfo(MIOutput out) {
		super(out);
	}

	public Frame[] getFrames() {
		return null;
	}
}
