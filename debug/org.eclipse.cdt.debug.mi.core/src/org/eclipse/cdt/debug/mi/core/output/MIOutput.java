package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIOutput {

	public static final String terminator = "(gdb)\n";
	public static final MIOOBRecord[] nullOOBRecord = new MIOOBRecord[0];
	MIResultRecord rr = null;
	MIOOBRecord[] oobs = null;
 

	public MIResultRecord getMIResultRecord() {
		return rr;
	}

	public void setMIResultRecord(MIResultRecord res) {
		rr = res ;
	}

	public MIOOBRecord[] getMIOOBRecords() {
		if (oobs == null)
			return nullOOBRecord;
		return oobs;
	}

	public void setMIOOBRecords(MIOOBRecord [] bands) {
		oobs = bands;
	}
}
