package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIResult {
	String variable = "";
	MIValue value = null;
		
	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String var) {
		variable = var;
	}

	public MIValue getMIValue() {
		return value;
	}
	
	public void setMIValue(MIValue val) {
		value = val;
	}
}
