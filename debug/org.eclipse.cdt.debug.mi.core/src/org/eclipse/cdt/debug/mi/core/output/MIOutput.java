package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIOutput {

	public static final String terminator = "(gdb)\n";
	String token = "";
 
	public String getToken() {
		return token;
	}

	public MIResultRecord getMIResultRecord() {
		return null;
	}

	public MIOOBRecord[] getMIOOBRecords() {
		return null;
	}
}
