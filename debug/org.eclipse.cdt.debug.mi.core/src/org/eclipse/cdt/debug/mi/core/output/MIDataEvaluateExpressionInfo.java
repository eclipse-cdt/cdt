package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIDataEvaluateExpressionInfo extends MIInfo{

	String expr;

	public MIDataEvaluateExpressionInfo(MIOutput rr) {
		super(rr);
	}

	public String getExpression() {
		if (expr == null) {
			parse();
		}
		return expr;
	}

	void parse() {
		expr = "";
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("value")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							expr = ((MIConst)value).getString();
						}
					}
				}
			}
		}
	}
}
