/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI thread select parsing.
 */
public class MIThreadSelectInfo extends MIInfo {

	int threadId;
	MIFrame frame;

	public MIThreadSelectInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getNewThreadId() {
		return threadId;
	}

	public MIFrame getFrame() {
		return frame;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("new-thread-id")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String str = ((MIConst)value).getCString();
							try {
								threadId = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("frame")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MITuple) {
							frame = new MIFrame((MITuple)value);
						}
					}
				}
			}
		}
	}
}
