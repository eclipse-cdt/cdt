package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIThreadSelectInfo extends MIInfo {

	int threadId;
	MIFrame frame;

	public MIThreadSelectInfo(MIOutput out) {
		super(out);
	}

	public int getNewThreadId() {
		if (frame == null) {
			parse();
		}
		return threadId;
	}

	public MIFrame getFrame() {
		if (frame == null) {
			parse();
		}
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
					if (var.equals("new-thread-ids")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String str = ((MIConst)value).getString();
							try {
								threadId = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("frame")) {
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
