package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIStackInfoDepthInfo extends MIInfo {

	int depth;

	public MIStackInfoDepthInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getDepth() {
		return depth;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("depth")) {
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							String str = ((MIConst)val).getString();
							try {
								depth = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					}
				}
			}
		}
	}
}
