package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIThreadSelectInfo extends MIInfo {

	public class Frame {
		public class Arg {
			String name;
			String value;
		}
		int level;
		String function;
		Arg[] args;
	}
		
		 
	public MIThreadSelectInfo(MIOutput out) {
		super(out);
	}

	public String getNewThreadId() {
		return "";
	}

	public Frame getFrame() {
		return null;
	}
}
