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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(variable);
		if (value != null) {
			String v = value.toString();
			buffer.append('=');
			if (v.charAt(0) == '[' || v.charAt(0) =='{') {
				buffer.append(v); 
			} else {
				buffer.append("\"" + value.toString() + "\""); 
			}
		}
		return buffer.toString();
	}
}
