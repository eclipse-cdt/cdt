/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI stack list locals parsing.
 * -stack-list-locals 1
 * ^done,locals=[{name="p",value="0x8048600 \"ghislaine\""},{name="buf",value="\"'\", 'x' <repeats 24 times>, \"i,xxxxxxxxx\", 'a' <repeats 24 times>"},{name="buf2",value="\"\\\"?'\\\\()~\""},{name="buf3",value="\"alain\""},{name="buf4",value="\"\\t\\t\\n\\f\\r\""},{name="i",value="0"}]
 */
public class MIStackListLocalsInfo extends MIInfo {

	MIArg[] locals;

	public MIStackListLocalsInfo(MIOutput out) {
		super(out);
		parse();
	}

	public MIArg[] getLocals() {
		if (locals == null) {
			parse();
		}
		return locals;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("locals")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							locals = MIArg.getMIArgs((MIList)value);
						}
					}
				}
			}
		}
		if (locals == null) {
			locals = new MIArg[0];
		}
	}
}
