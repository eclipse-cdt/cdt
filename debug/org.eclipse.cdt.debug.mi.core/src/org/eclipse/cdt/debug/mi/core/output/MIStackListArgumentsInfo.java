package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIStackListArgumentsInfo extends MIInfo {

	public class Frame {
		public class Args {
			String name;
			String value;
		}
		int level;
		Args[] args;
	}

	public MIStackListArgumentsInfo(MIOutput out) {
		super(out);
	}

	public Frame[] getFrames() {
		return null;
	}
}
