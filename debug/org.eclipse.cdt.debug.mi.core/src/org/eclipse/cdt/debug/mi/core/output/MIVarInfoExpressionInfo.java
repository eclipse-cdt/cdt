package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIVarInfoExpressionInfo extends MIInfo {

	public MIVarInfoExpressionInfo(MIOutput record) {
		super(record);
	}

	public String getLanguage () {
		return "";
	}

	public String getExpression() {
		return "";
	}
}
