package org.eclipse.cdt.debug.mi.core.output;



/**
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
